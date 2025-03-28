package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.VinculacionFamiliarViewModel
import com.tfg.umeegunero.navigation.AppScreens
import androidx.compose.foundation.selection.selectable
import java.util.*

/**
 * Pantalla para gestionar las vinculaciones entre alumnos y familiares
 * Permite al administrador del centro asociar familiares a alumnos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VinculacionFamiliarScreen(
    navController: NavController,
    viewModel: VinculacionFamiliarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFamiliaresDialog by remember { mutableStateOf(false) }
    var showVinculacionDialog by remember { mutableStateOf(false) }
    var showFamiliarFilterDialog by remember { mutableStateOf(false) }
    var alumnoSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    
    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarAlumnos()
        viewModel.cargarFamiliares()
    }
    
    // Mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Mostrar mensajes de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.clearMensaje()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vinculación Familiar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Acción para filtrar
                    IconButton(onClick = { showFamiliarFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar"
                        )
                    }
                    
                    // Acción para añadir familiar
                    IconButton(
                        onClick = { 
                            navController.navigate(
                                AppScreens.AddUser.createRoute(
                                    isAdminApp = false,
                                    tipoUsuario = TipoUsuario.FAMILIAR.toString()
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Añadir Familiar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Pestañas para alternar entre alumnos y familiares
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        text = { Text("Alumnos") },
                        icon = { Icon(Icons.Default.School, contentDescription = null) }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        text = { Text("Familiares") },
                        icon = { Icon(Icons.Default.People, contentDescription = null) }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Muestra alumnos o familiares según la pestaña seleccionada
                when (uiState.selectedTab) {
                    0 -> AlumnosTab(
                        alumnos = uiState.alumnosFiltrados,
                        onAlumnoClick = { alumno ->
                            alumnoSeleccionado = alumno
                            showFamiliaresDialog = true
                            viewModel.cargarFamiliaresPorAlumno(alumno.dni)
                        },
                        onAddVinculacionClick = { alumno ->
                            alumnoSeleccionado = alumno
                            showVinculacionDialog = true
                        }
                    )
                    1 -> FamiliaresTab(
                        familiares = uiState.familiaresFiltrados,
                        onFamiliarClick = { familiar ->
                            viewModel.cargarAlumnosPorFamiliar(familiar.dni)
                            navController.navigate(AppScreens.UserDetail.createRoute(familiar.dni))
                        }
                    )
                }
            }
            
            // Botón flotante para añadir alumnos o familiares según la pestaña
            FloatingActionButton(
                onClick = {
                    val route = if (uiState.selectedTab == 0) {
                        AppScreens.AddAlumno.route
                    } else {
                        AppScreens.AddUser.createRoute(
                            isAdminApp = false,
                            tipoUsuario = TipoUsuario.FAMILIAR.toString()
                        )
                    }
                    navController.navigate(route)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (uiState.selectedTab == 0) "Añadir Alumno" else "Añadir Familiar"
                )
            }
        }
    }
    
    // Diálogo para mostrar los familiares de un alumno
    if (showFamiliaresDialog && alumnoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showFamiliaresDialog = false },
            title = { Text("Familiares de ${alumnoSeleccionado?.nombre} ${alumnoSeleccionado?.apellidos}") },
            text = {
                Column {
                    if (uiState.familiaresDelAlumno.isEmpty()) {
                        Text(
                            text = "Este alumno no tiene familiares vinculados",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LazyColumn {
                            items(uiState.familiaresDelAlumno) { familiar ->
                                FamiliarItem(
                                    familiar = familiar,
                                    onRemoveClick = {
                                        alumnoSeleccionado?.dni?.let { alumnoDni ->
                                            viewModel.desvincularFamiliar(alumnoDni, familiar.dni)
                                        }
                                        showFamiliaresDialog = false
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alumnoSeleccionado?.let {
                            showVinculacionDialog = true
                        }
                        showFamiliaresDialog = false
                    }
                ) {
                    Text("Añadir Familiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFamiliaresDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
    
    // Diálogo para vincular un alumno con un familiar
    if (showVinculacionDialog && alumnoSeleccionado != null) {
        var selectedFamiliar by remember { mutableStateOf<Usuario?>(null) }
        var selectedParentesco by remember { mutableStateOf(SubtipoFamiliar.PADRE) }
        
        AlertDialog(
            onDismissRequest = { showVinculacionDialog = false },
            title = { Text("Vincular Familiar") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Seleccione un familiar para ${alumnoSeleccionado?.nombre} ${alumnoSeleccionado?.apellidos}")
                    
                    // Selector de familiar
                    ExposedDropdownMenuBox(
                        expanded = uiState.isFamiliarDropdownExpanded,
                        onExpandedChange = { viewModel.toggleFamiliarDropdown() }
                    ) {
                        OutlinedTextField(
                            value = selectedFamiliar?.let { "${it.nombre} ${it.apellidos}" } ?: "Seleccione un familiar",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isFamiliarDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = uiState.isFamiliarDropdownExpanded,
                            onDismissRequest = { viewModel.toggleFamiliarDropdown() }
                        ) {
                            // Filtramos los familiares que ya están vinculados
                            val familiaresDisponibles = uiState.familiares.filter { familiar ->
                                !uiState.familiaresDelAlumno.any { it.dni == familiar.dni }
                            }
                            
                            if (familiaresDisponibles.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay familiares disponibles") },
                                    onClick = { viewModel.toggleFamiliarDropdown() }
                                )
                            } else {
                                familiaresDisponibles.forEach { familiar ->
                                    DropdownMenuItem(
                                        text = { Text("${familiar.nombre} ${familiar.apellidos}") },
                                        onClick = {
                                            selectedFamiliar = familiar
                                            viewModel.toggleFamiliarDropdown()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selector de parentesco
                    Text("Tipo de Parentesco")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SubtipoFamiliar.values().forEach { subtipo ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .selectable(
                                        selected = selectedParentesco == subtipo,
                                        onClick = { selectedParentesco = subtipo }
                                    )
                                    .padding(8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedParentesco == subtipo,
                                    onClick = { selectedParentesco = subtipo }
                                )
                                Text(subtipo.name.lowercase().capitalize())
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFamiliar?.let { familiar ->
                            alumnoSeleccionado?.dni?.let { alumnoDni ->
                                viewModel.vincularFamiliar(
                                    alumnoDni = alumnoDni,
                                    familiarDni = familiar.dni,
                                    parentesco = selectedParentesco
                                )
                            }
                        }
                        showVinculacionDialog = false
                    },
                    enabled = selectedFamiliar != null
                ) {
                    Text("Vincular")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVinculacionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para filtrar familiares
    if (showFamiliarFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFamiliarFilterDialog = false },
            title = { Text("Filtrar") },
            text = {
                Column {
                    // Checkbox para mostrar solo activos
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSoloActivos() }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = uiState.soloActivos,
                            onCheckedChange = { viewModel.toggleSoloActivos() }
                        )
                        Text("Mostrar solo usuarios activos")
                    }
                    
                    // Campo de búsqueda
                    OutlinedTextField(
                        value = uiState.searchText,
                        onValueChange = { viewModel.updateSearchText(it) },
                        label = { Text("Buscar por nombre o DNI") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFamiliarFilterDialog = false }) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        viewModel.resetFiltros()
                        showFamiliarFilterDialog = false 
                    }
                ) {
                    Text("Restablecer")
                }
            }
        )
    }
}

@Composable
fun AlumnosTab(
    alumnos: List<Usuario>,
    onAlumnoClick: (Usuario) -> Unit,
    onAddVinculacionClick: (Usuario) -> Unit
) {
    if (alumnos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay alumnos disponibles",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alumnos) { alumno ->
                AlumnoItem(
                    alumno = alumno,
                    onItemClick = { onAlumnoClick(alumno) },
                    onVincularClick = { onAddVinculacionClick(alumno) }
                )
            }
            
            // Espacio para el FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun FamiliaresTab(
    familiares: List<Usuario>,
    onFamiliarClick: (Usuario) -> Unit
) {
    if (familiares.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay familiares disponibles",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(familiares) { familiar ->
                FamiliarListItem(
                    familiar = familiar,
                    onItemClick = { onFamiliarClick(familiar) }
                )
            }
            
            // Espacio para el FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoItem(
    alumno: Usuario,
    onItemClick: () -> Unit,
    onVincularClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumno.nombre.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del alumno
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = alumno.dni,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botón para vincular
            IconButton(onClick = onVincularClick) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Vincular familiar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FamiliarListItem(
    familiar: Usuario,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = familiar.nombre.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = familiar.dni,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val subtipoFamiliar = familiar.perfiles
                    .firstOrNull { it.tipo == TipoUsuario.FAMILIAR }
                    ?.subtipo?.name?.lowercase()?.capitalize()
                
                if (subtipoFamiliar != null) {
                    Text(
                        text = subtipoFamiliar,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Icono de información
            IconButton(onClick = onItemClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ver detalles",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FamiliarItem(
    familiar: Usuario,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = familiar.nombre.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Información del familiar
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${familiar.nombre} ${familiar.apellidos}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Mostrar el tipo de relación
            val subtipoFamiliar = familiar.perfiles
                .firstOrNull { it.tipo == TipoUsuario.FAMILIAR }
                ?.subtipo?.name?.lowercase()?.capitalize()
            
            if (subtipoFamiliar != null) {
                Text(
                    text = subtipoFamiliar,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Botón para eliminar vínculo
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar vínculo",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Extensión para capitalizar strings
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
} 