package com.tfg.umeegunero.feature.common.users.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.common.users.viewmodel.ListAdministradoresCentroViewModel
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.launch

/**
 * Pantalla que muestra el listado de administradores de centros educativos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListAdministradoresCentroScreen(
    navController: NavController,
    viewModel: ListAdministradoresCentroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedAdmin by remember { mutableStateOf<Usuario?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.cargarAdministradoresCentro()
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
                title = { Text("Administradores de Centro") },
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
                        tipoUsuario = TipoUsuario.ADMIN_CENTRO.toString()
                    ))
                },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Nuevo Admin Centro") },
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
            
            // Mensaje cuando no hay administradores de centro
            if (!uiState.isLoading && uiState.adminsCentro.isEmpty()) {
                EmptyAdminsCentroList(
                    onAddClicked = {
                        navController.navigate(AppScreens.AddUser.createRoute(
                            isAdminApp = false, 
                            tipoUsuario = TipoUsuario.ADMIN_CENTRO.toString()
                        ))
                    }
                )
            }
            
            // Lista de administradores de centro
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                items(
                    items = uiState.adminsCentro,
                    key = { it.dni }
                ) { admin ->
                    AdminCentroListItem(
                        admin = admin,
                        onItemClick = {
                            navController.navigate(AppScreens.UserDetail.createRoute(admin.dni))
                        },
                        onEditClick = {
                            navController.navigate(AppScreens.EditUser.createRoute(admin.dni))
                        },
                        onDeleteClick = {
                            selectedAdmin = admin
                            showDeleteConfirmDialog = true
                        }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(88.dp)) } // Espacio para el FAB
            }
            
            // Diálogo de eliminación
            if (showDeleteConfirmDialog && selectedAdmin != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Confirmar eliminación") },
                    text = { 
                        Text("¿Estás seguro de que deseas eliminar al administrador de centro ${selectedAdmin?.nombre} ${selectedAdmin?.apellidos}?") 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                selectedAdmin?.let { admin ->
                                    viewModel.eliminarAdminCentro(admin.dni)
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
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    title = { Text("Filtrar administradores") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = uiState.filtroNombre,
                                onValueChange = { viewModel.updateFiltroNombre(it) },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = uiState.filtroCentro,
                                onValueChange = { viewModel.updateFiltroCentro(it) },
                                label = { Text("Centro") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.aplicarFiltros()
                                showFilterDialog = false
                            }
                        ) {
                            Text("Aplicar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.limpiarFiltros()
                                showFilterDialog = false
                            }
                        ) {
                            Text("Limpiar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyAdminsCentroList(onAddClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No hay administradores de centro",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Añade un nuevo administrador para comenzar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddClicked,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Añadir administrador")
        }
    }
}

@Composable
fun AdminCentroListItem(
    admin: Usuario,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Información del administrador de centro
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "${admin.nombre} ${admin.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = admin.email ?: "Sin email",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    admin.telefono?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
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