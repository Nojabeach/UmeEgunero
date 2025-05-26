package com.tfg.umeegunero.feature.common.users.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.feature.common.users.viewmodel.ListAlumnosViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.feature.common.users.viewmodel.ListAlumnosUiState

/**
 * Pantalla que muestra el listado de alumnos del sistema
 * Permite visualizar, filtrar y gestionar alumnos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAlumnosScreen(
    navController: NavController,
    centroId: String,
    viewModel: ListAlumnosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedAlumno by remember { mutableStateOf<Alumno?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.cargarAlumnos()
    }
    
    // Manejo de errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(mensaje)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alumnos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    navController.navigate(AppScreens.AddUser.createRoute(
                        isAdminApp = false, 
                        tipoUsuario = TipoUsuario.ALUMNO.toString(),
                        centroId = centroId,
                        centroBloqueado = true
                    ))
                },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Nuevo Alumno") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loader para cuando está cargando
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Mensaje cuando no hay alumnos
            if (!uiState.isLoading && uiState.filteredAlumnos.isEmpty() && uiState.allAlumnos.isNotEmpty()) {
                NoResultsFound()
            } else if (!uiState.isLoading && uiState.allAlumnos.isEmpty()) {
                EmptyAlumnosList(
                    onAddClicked = {
                        navController.navigate(AppScreens.AddUser.createRoute(
                            isAdminApp = false, 
                            tipoUsuario = TipoUsuario.ALUMNO.toString(),
                            centroId = centroId,
                            centroBloqueado = true
                        ))
                    }
                )
            }
            
            // Lista de alumnos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                items(
                    items = uiState.filteredAlumnos,
                    key = { it.dni }
                ) { alumno ->
                    AlumnoListItem(
                        alumno = alumno,
                        onItemClick = {
                            navController.navigate(AppScreens.DetalleAlumno.createRoute(alumno.dni)) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onEditClick = {
                            navController.navigate(AppScreens.AddUser.createRoute(
                                isAdminApp = false, 
                                tipoUsuario = TipoUsuario.ALUMNO.toString(),
                                centroId = centroId,
                                centroBloqueado = true,
                                dniUsuario = alumno.dni
                            )) {
                                launchSingleTop = true
                            }
                        },
                        onDeleteClick = {
                            selectedAlumno = alumno
                            showDeleteConfirmDialog = true
                        }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(88.dp)) } // Espacio para el FAB
            }
            
            // Diálogo de eliminación simple
            if (showDeleteConfirmDialog && selectedAlumno != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { 
                        Text("¿Estás seguro de que deseas eliminar al alumno ${selectedAlumno?.nombre} ${selectedAlumno?.apellidos}?") 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedAlumno?.let { alumno ->
                                    viewModel.iniciarEliminacionAlumno(alumno)
                                    showDeleteConfirmDialog = false
                                }
                            }
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo para eliminar familiares
            if (uiState.showDeleteFamiliarDialog && uiState.alumnoAEliminar != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.cancelarEliminacionFamiliares() },
                    title = { Text("Eliminar alumno y familiares") },
                    text = { 
                        Column {
                            Text("El alumno ${uiState.alumnoAEliminar?.nombre} ${uiState.alumnoAEliminar?.apellidos} tiene los siguientes familiares vinculados:")
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            uiState.familiaresDelAlumno.forEach { familiar ->
                                Text(
                                    text = "• ${familiar.nombre} ${familiar.apellidos}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "¿Deseas eliminar también a los familiares? (Solo se eliminarán si no tienen otros alumnos vinculados)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    confirmButton = {
                        Column {
                            TextButton(
                                onClick = {
                                    viewModel.confirmarEliminacionConFamiliares(true)
                                }
                            ) {
                                Text("Eliminar alumno y familiares", color = MaterialTheme.colorScheme.error)
                            }
                            
                            TextButton(
                                onClick = {
                                    viewModel.confirmarEliminacionConFamiliares(false)
                                }
                            ) {
                                Text("Solo eliminar alumno", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.cancelarEliminacionFamiliares() }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            
            // Diálogo de filtros
            if (showFilterDialog) {
                FilterDialog(
                    uiState = uiState,
                    onDismiss = { showFilterDialog = false },
                    onDniChange = viewModel::updateDniFilter,
                    onNombreChange = viewModel::updateNombreFilter,
                    onApellidosChange = viewModel::updateApellidosFilter,
                    onCursoChange = viewModel::updateCursoFilter,
                    onClaseChange = viewModel::updateClaseFilter,
                    onSoloActivosChange = viewModel::updateSoloActivosFilter
                )
            }
        }
    }
}

@Composable
private fun AlumnoListItem(
    alumno: Alumno,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Inicial
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumno.nombre.firstOrNull()?.uppercase() ?: "A",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Información
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = alumno.dni,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (alumno.curso.isNotBlank() || alumno.clase.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${alumno.curso} - ${alumno.clase}".trim(' ','-'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (!alumno.activo) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Inactivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Acciones
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAlumnosList(onAddClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Face,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No hay alumnos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Añade alumnos para gestionar el centro",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Añadir Alumno")
        }
    }
}

@Composable
private fun NoResultsFound() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sin resultados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No se encontraron alumnos que coincidan con los filtros aplicados.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilterDialog(
    uiState: ListAlumnosUiState,
    onDismiss: () -> Unit,
    onDniChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onCursoChange: (String) -> Unit,
    onClaseChange: (String) -> Unit,
    onSoloActivosChange: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar alumnos") },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.dniFilter,
                    onValueChange = onDniChange,
                    label = { Text("Filtrar por DNI") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.nombreFilter,
                    onValueChange = onNombreChange,
                    label = { Text("Filtrar por Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.apellidosFilter,
                    onValueChange = onApellidosChange,
                    label = { Text("Filtrar por Apellidos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.cursoFilter,
                    onValueChange = onCursoChange,
                    label = { Text("Filtrar por Curso") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.claseFilter,
                    onValueChange = onClaseChange,
                    label = { Text("Filtrar por Clase") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mostrar solo alumnos activos",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.soloActivos,
                        onCheckedChange = onSoloActivosChange
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Los filtros se aplican automáticamente al escribir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        dismissButton = {
            {}
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ListAlumnosScreenPreview() {
    UmeEguneroTheme {
        ListAlumnosScreen(navController = rememberNavController(), centroId = "centro_preview")
    }
} 