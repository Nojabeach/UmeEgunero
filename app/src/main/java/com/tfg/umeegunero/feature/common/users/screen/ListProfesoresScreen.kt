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
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.common.users.viewmodel.ListProfesoresViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

/**
 * Pantalla que muestra el listado de profesores del sistema
 * Permite visualizar, filtrar y gestionar profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListProfesoresScreen(
    navController: NavController,
    viewModel: ListProfesoresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedProfesor by remember { mutableStateOf<Usuario?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.cargarProfesores()
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
                title = { Text("Profesores") },
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
                    IconButton(onClick = { 
                        navController.navigate(AppScreens.AddUser.createRoute(
                            isAdminApp = false, 
                            tipoUsuario = TipoUsuario.PROFESOR.toString()
                        ))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir profesor"
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
                        tipoUsuario = TipoUsuario.PROFESOR.toString()
                    ))
                },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Nuevo Profesor") },
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
            
            // Mensaje cuando no hay profesores
            if (!uiState.isLoading && uiState.profesores.isEmpty()) {
                EmptyProfesoresList(
                    onAddClicked = {
                        navController.navigate(AppScreens.AddUser.createRoute(
                            isAdminApp = false, 
                            tipoUsuario = TipoUsuario.PROFESOR.toString()
                        ))
                    }
                )
            }
            
            // Lista de profesores
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                items(
                    items = uiState.profesores,
                    key = { it.dni }
                ) { profesor ->
                    ProfesorListItem(
                        profesor = profesor,
                        onItemClick = {
                            navController.navigate(AppScreens.UserDetail.createRoute(profesor.dni))
                        },
                        onEditClick = {
                            navController.navigate(AppScreens.EditUser.createRoute(profesor.dni))
                        },
                        onDeleteClick = {
                            selectedProfesor = profesor
                            showDeleteConfirmDialog = true
                        }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(88.dp)) } // Espacio para el FAB
            }
            
            // Diálogo de eliminación
            if (showDeleteConfirmDialog && selectedProfesor != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { 
                        Text("¿Estás seguro de que deseas eliminar al profesor ${selectedProfesor?.nombre} ${selectedProfesor?.apellidos}?") 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedProfesor?.let { profesor ->
                                    viewModel.eliminarProfesor(profesor.dni)
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
            
            // Diálogo de filtros
            if (showFilterDialog) {
                FilterDialog(
                    onDismiss = { showFilterDialog = false },
                    onApplyFilters = { activos ->
                        viewModel.aplicarFiltros(activos)
                        showFilterDialog = false
                    },
                    mostrarSoloActivos = uiState.soloActivos
                )
            }
        }
    }
}

@Composable
private fun ProfesorListItem(
    profesor: Usuario,
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
                    text = profesor.nombre.firstOrNull()?.uppercase() ?: "P",
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
                    text = "${profesor.nombre} ${profesor.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = profesor.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!profesor.activo) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Inactivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
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
private fun EmptyProfesoresList(onAddClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No hay profesores",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Añade profesores para gestionar la docencia",
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
            
            Text("Añadir Profesor")
        }
    }
}

@Composable
private fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (Boolean) -> Unit,
    mostrarSoloActivos: Boolean
) {
    var soloActivos by remember { mutableStateOf(mostrarSoloActivos) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar profesores") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mostrar solo profesores activos",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Switch(
                        checked = soloActivos,
                        onCheckedChange = { soloActivos = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Aplica filtros para encontrar profesores específicos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onApplyFilters(soloActivos) }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ListProfesoresScreenPreview() {
    UmeEguneroTheme {
        ListProfesoresScreen(navController = rememberNavController())
    }
} 