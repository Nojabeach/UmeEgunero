package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.feature.common.academico.viewmodel.ListCursosViewModel
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import com.tfg.umeegunero.ui.components.EmptyStateMessage
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.navigation.Screens
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Pantalla que muestra el listado de cursos de un centro educativo
 * Permite añadir, editar y eliminar cursos
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel para la gestión de cursos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCursosScreen(
    navController: NavController,
    viewModel: ListCursosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var cursoToDelete by remember { mutableStateOf<Curso?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.cargarCursos()
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Gestión de Cursos",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screens.AddCurso.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir curso"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar el indicador de carga
            if (uiState.isLoading) {
                LoadingIndicator(fullScreen = true)
            } else if (uiState.cursos.isEmpty()) {
                // Mostrar mensaje si no hay cursos
                EmptyStateMessage(
                    message = "No hay cursos disponibles",
                    icon = Icons.Default.School,
                    buttonText = "Añadir curso",
                    onButtonClick = { navController.navigate(Screens.AddCurso.route) }
                )
            } else {
                // Mostrar la lista de cursos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.cursos) { curso ->
                        CursoCard(
                            curso = curso,
                            onEditClick = {
                                navController.navigate(Screens.EditCurso.createRoute(curso.id))
                            },
                            onDeleteClick = {
                                cursoToDelete = curso
                                showConfirmDialog = true
                            },
                            onVerClasesClick = {
                                navController.navigate(Screens.ListClases.createRoute(curso.id))
                            }
                        )
                    }
                }
            }

            // Mostrar mensaje de error si existe
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = error,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar curso
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Eliminar curso") },
            text = { Text("¿Estás seguro de que deseas eliminar el curso '${cursoToDelete?.nombre}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        cursoToDelete?.let { curso ->
                            viewModel.eliminarCurso(curso.id)
                            showConfirmDialog = false
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Componente que muestra la información de un curso en forma de tarjeta
 * 
 * @param curso Curso a mostrar
 * @param onEditClick Acción al hacer clic en el botón de editar
 * @param onDeleteClick Acción al hacer clic en el botón de eliminar
 * @param onVerClasesClick Acción al hacer clic en el botón de ver clases
 */
@Composable
fun CursoCard(
    curso: Curso,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVerClasesClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = curso.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Año académico: ${curso.anioAcademico}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Edad: ${curso.edadMinima} - ${curso.edadMaxima} años",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar curso",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar curso",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (curso.descripcion.isNotEmpty()) {
                Text(
                    text = curso.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onVerClasesClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = "Ver clases")
            }
        }
    }
} 