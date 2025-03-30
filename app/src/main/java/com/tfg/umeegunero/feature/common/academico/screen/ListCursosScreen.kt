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
import com.tfg.umeegunero.navigation.AppScreens
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
                onClick = {
                    navController.navigate(AppScreens.AddCurso.createRoute(viewModel.uiState.value.centroId))
                },
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
                    onButtonClick = {
                        navController.navigate(AppScreens.AddCurso.createRoute(viewModel.uiState.value.centroId))
                    }
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
                                navController.navigate(AppScreens.AddCurso.createRoute(viewModel.uiState.value.centroId))
                            },
                            onDeleteClick = {
                                cursoToDelete = curso
                                showConfirmDialog = true
                            },
                            onVerClasesClick = {
                                navController.navigate(AppScreens.GestionClases.createRoute(curso.id))
                            }
                        )
                    }
                }
            }

            // Mostrar mensaje de error si existe
            if (uiState.error != null) {
                LaunchedEffect(uiState.error) {
                    scope.launch {
                        snackbarHostState.showSnackbar(uiState.error!!)
                        viewModel.clearError()
                    }
                }
            }

            // Diálogo de confirmación para eliminar curso
            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Está seguro que desea eliminar el curso ${cursoToDelete?.nombre}? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                cursoToDelete?.let { curso ->
                                    viewModel.eliminarCurso(curso.id)
                                    showConfirmDialog = false
                                    cursoToDelete = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
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
    }
}

/**
 * Tarjeta que muestra la información de un curso académico
 *
 * @param curso Datos del curso a mostrar
 * @param onEditClick Callback para el evento de edición
 * @param onDeleteClick Callback para el evento de eliminación
 * @param onVerClasesClick Callback para navegar a las clases del curso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CursoCard(
    curso: Curso,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVerClasesClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: Título y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = curso.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                
                if (curso.activo) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Activo",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "Inactivo",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Descripción
            Text(
                text = curso.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = curso.anioAcademico,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = "Edad: ${curso.edadMinima}-${curso.edadMaxima} años",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fila de acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Botón para ver clases
                TextButton(
                    onClick = onVerClasesClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Ver clases",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Ver clases")
                }
                
                // Botón para editar
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar curso",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Botón para eliminar
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar curso",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
} 