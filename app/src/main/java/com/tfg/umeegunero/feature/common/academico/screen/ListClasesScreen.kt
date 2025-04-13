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
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.feature.common.academico.viewmodel.ListClasesViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import com.tfg.umeegunero.ui.components.EmptyStateMessage
import com.tfg.umeegunero.ui.components.LoadingIndicator
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Pantalla que muestra el listado de clases de un curso
 * Permite añadir, editar y eliminar clases
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

    LaunchedEffect(key1 = true) {
        viewModel.cargarClases()
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Gestión de Clases",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController.navigate(AppScreens.AddClase.createRoute(uiState.cursoId, "0"))
                },
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
            // Mostrar el indicador de carga
            if (uiState.isLoading) {
                LoadingIndicator(fullScreen = true)
            } else if (uiState.clases.isEmpty()) {
                // Mostrar mensaje si no hay clases
                EmptyStateMessage(
                    message = "No hay clases disponibles en este curso",
                    icon = Icons.Default.Class,
                    buttonText = "Añadir clase",
                    onButtonClick = { 
                        navController.navigate(AppScreens.AddClase.createRoute(uiState.cursoId, "0"))
                    }
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
                                navController.navigate(AppScreens.EditClase.createRoute(clase.id))
                            },
                            onDeleteClick = {
                                claseToDelete = clase
                                showConfirmDialog = true
                            },
                            onVerDetallesClick = {
                                navController.navigate(AppScreens.DetalleClase.createRoute(clase.id))
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

            // Diálogo de confirmación para eliminar clase
            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Está seguro que desea eliminar la clase ${claseToDelete?.nombre}? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                claseToDelete?.let { clase ->
                                    viewModel.eliminarClase(clase.id)
                                    showConfirmDialog = false
                                    claseToDelete = null
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
 * Tarjeta que muestra la información de una clase
 *
 * @param clase Datos de la clase a mostrar
 * @param onEditClick Callback para el evento de edición
 * @param onDeleteClick Callback para el evento de eliminación
 * @param onVerDetallesClick Callback para ver detalles de la clase
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaseCard(
    clase: Clase,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVerDetallesClick: () -> Unit
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
                    text = "Clase ${clase.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                
                if (clase.activo) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Activa",
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
                            text = "Inactiva",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Room,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = clase.aula,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = clase.horario,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Capacidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = "Capacidad máxima: ${clase.capacidadMaxima} alumnos",
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
                    text = "Alumnos: ${clase.alumnosIds.size}",
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
                // Botón para ver detalles
                TextButton(
                    onClick = onVerDetallesClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Ver detalles",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Detalles")
                }
                
                // Botón para editar
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar clase",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Botón para eliminar
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar clase",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
} 