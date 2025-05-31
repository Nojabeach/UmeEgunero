package com.tfg.umeegunero.feature.centro.screen

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
import com.tfg.umeegunero.feature.common.users.viewmodel.ListFamiliaresViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.CentroColor
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Pantalla que muestra el listado de familiares del centro
 * Permite visualizar, editar y eliminar familiares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaFamiliaresScreen(
    navController: NavController,
    viewModel: ListFamiliaresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Estados para diálogos
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedFamiliar by remember { mutableStateOf<Usuario?>(null) }
    
    // Cargar los familiares al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarFamiliares()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Familiares") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CentroColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(AppScreens.EditFamiliar.route) },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Nuevo Familiar") },
                containerColor = CentroColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CentroColor
                )
            } else if (uiState.familiares.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = CentroColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay familiares registrados",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Añade nuevos familiares utilizando el botón +",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Filtros
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mostrar solo activos",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = uiState.soloActivos,
                            onCheckedChange = { viewModel.aplicarFiltros(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CentroColor,
                                checkedTrackColor = CentroColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    // Lista de familiares
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        
                        items(
                            items = uiState.familiares,
                            key = { it.dni }
                        ) { familiar ->
                            FamiliarListItem(
                                familiar = familiar,
                                onItemClick = {
                                    navController.navigate(AppScreens.UserDetail.createRoute(familiar.dni))
                                },
                                onEditClick = {
                                    navController.navigate(AppScreens.EditFamiliar.createRoute(familiar.dni))
                                },
                                onDeleteClick = {
                                    selectedFamiliar = familiar
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(88.dp)) } // Espacio para el FAB
                    }
                }
            }
            
            // Mostrar error si existe
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        action = {
                            TextButton(
                                onClick = { viewModel.cargarFamiliares() }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteConfirmDialog && selectedFamiliar != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar familiar") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${selectedFamiliar?.nombre} ${selectedFamiliar?.apellidos}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedFamiliar?.dni?.let { dni ->
                            viewModel.eliminarFamiliar(dni)
                            scope.launch {
                                snackbarHostState.showSnackbar("Familiar eliminado correctamente")
                            }
                        }
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Item de la lista de familiares
 */
@Composable
fun FamiliarListItem(
    familiar: Usuario,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (familiar.activo) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o iniciales
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (familiar.activo) CentroColor else Color.Gray
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = familiar.nombre.firstOrNull()?.toString()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del familiar
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${familiar.nombre} ${familiar.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = familiar.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!familiar.telefono.isNullOrBlank()) {
                    Text(
                        text = familiar.telefono ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Acciones
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = CentroColor
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