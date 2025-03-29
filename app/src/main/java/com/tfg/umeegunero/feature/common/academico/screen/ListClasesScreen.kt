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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.feature.common.academico.viewmodel.ListClasesViewModel
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import com.tfg.umeegunero.ui.components.EmptyStateMessage
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.navigation.Screens
import kotlinx.coroutines.launch

/**
 * Pantalla que muestra el listado de clases de un curso específico
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel para la gestión de clases
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListClasesScreen(
    navController: NavController,
    viewModel: ListClasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var claseToDelete by remember { mutableStateOf<Clase?>(null) }

    // Obtener el cursoId del argumento de navegación
    val cursoId = remember { 
        navController.currentBackStackEntry?.arguments?.getString("cursoId") ?: ""
    }

    // Cargar las clases al iniciar la pantalla
    LaunchedEffect(key1 = cursoId) {
        if (cursoId.isNotEmpty()) {
            viewModel.cargarClases(cursoId)
        }
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Clases del curso: ${uiState.nombreCurso}",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screens.AddClase.createRoute(cursoId)) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir clase"
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
            // Mostrar indicador de carga
            if (uiState.isLoading) {
                LoadingIndicator(fullScreen = true)
            } else if (uiState.clases.isEmpty()) {
                // Mostrar mensaje si no hay clases
                EmptyStateMessage(
                    message = "No hay clases disponibles para este curso",
                    icon = Icons.Default.Group,
                    buttonText = "Añadir clase",
                    onButtonClick = { navController.navigate(Screens.AddClase.createRoute(cursoId)) }
                )
            } else {
                // Mostrar la lista de clases
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.clases) { clase ->
                        ClaseCard(
                            clase = clase,
                            onEditClick = {
                                navController.navigate(Screens.EditClase.createRoute(clase.id))
                            },
                            onDeleteClick = {
                                claseToDelete = clase
                                showConfirmDialog = true
                            },
                            onVerAlumnosClick = {
                                // Acción para ver alumnos (pendiente implementación)
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

    // Diálogo de confirmación para eliminar clase
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Eliminar clase") },
            text = { Text("¿Estás seguro de que deseas eliminar la clase '${claseToDelete?.nombre}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        claseToDelete?.let { clase ->
                            viewModel.eliminarClase(clase.id)
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
 * Componente que muestra la información de una clase en forma de tarjeta
 * 
 * @param clase Clase a mostrar
 * @param onEditClick Acción al hacer clic en el botón de editar
 * @param onDeleteClick Acción al hacer clic en el botón de eliminar
 * @param onVerAlumnosClick Acción al hacer clic en el botón de ver alumnos
 */
@Composable
fun ClaseCard(
    clase: Clase,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVerAlumnosClick: () -> Unit
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
                        text = "Clase ${clase.nombre}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Aula: ${clase.aula}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Capacidad: ${clase.capacidadMaxima} alumnos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (clase.alumnosIds.isNotEmpty()) {
                        Text(
                            text = "Alumnos: ${clase.alumnosIds.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (clase.horario.isNotEmpty()) {
                        Text(
                            text = "Horario: ${clase.horario}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar clase",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar clase",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onVerAlumnosClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(text = "Ver alumnos")
            }
        }
    }
} 