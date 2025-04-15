package com.tfg.umeegunero.feature.centro.screen

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.VinculacionFamiliarViewModel
import androidx.compose.foundation.selection.selectable
import com.tfg.umeegunero.ui.components.LoadingIndicator
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider
import com.tfg.umeegunero.util.AppUtils.capitalizeFirst

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
    var showFamiliaresDialog by remember { mutableStateOf(false) }
    var showVinculacionDialog by remember { mutableStateOf(false) }
    var alumnoSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var familiarSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Manejar mensajes y errores
    LaunchedEffect(uiState.error, uiState.mensaje) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearError()
            }
        }
        
        uiState.mensaje?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = mensaje,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMensaje()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vinculación familiar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator(fullScreen = true)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Gestión de vinculaciones alumno-familiar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Sección de Alumnos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Cabecera de alumnos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alumnos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Buscador de alumnos
                        OutlinedTextField(
                            value = uiState.filtroAlumnos,
                            onValueChange = { viewModel.updateFiltroAlumnos(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar alumno...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            },
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Lista de alumnos
                        if (uiState.alumnosFiltrados.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay alumnos disponibles",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.alumnosFiltrados) { alumno ->
                                    AlumnoListItem(
                                        alumno = alumno,
                                        onItemClick = {
                                            alumnoSeleccionado = alumno
                                            alumno.dni.let { viewModel.cargarFamiliaresPorAlumno(it) }
                                            showFamiliaresDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sección de Familiares
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Cabecera de familiares
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Familiares",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Buscador de familiares
                        OutlinedTextField(
                            value = uiState.filtroFamiliares,
                            onValueChange = { viewModel.updateFiltroFamiliares(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar familiar...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            },
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Lista de familiares
                        if (uiState.familiaresFiltrados.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay familiares disponibles",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.familiaresFiltrados) { familiar ->
                                    FamiliarListItem(
                                        familiar = familiar,
                                        onItemClick = {
                                            familiarSeleccionado = familiar
                                            viewModel.cargarAlumnosPorFamiliar(familiar.dni)
                                            // Aquí podríamos mostrar los alumnos vinculados al familiar
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo para ver los familiares de un alumno
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
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
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
        var dropdownExpanded by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showVinculacionDialog = false },
            title = { Text("Vincular Familiar") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Alumno: ${alumnoSeleccionado?.nombre} ${alumnoSeleccionado?.apellidos}",
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Selector de familiar
                    Text("Selecciona un familiar")
                    
                    Box {
                        OutlinedTextField(
                            value = selectedFamiliar?.let { "${it.nombre} ${it.apellidos}" } ?: "",
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Familiar") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Mostrar opciones"
                                    )
                                }
                            }
                        )
                        
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            // Filtramos los familiares que ya están vinculados
                            val familiaresDisponibles = uiState.familiares.filter { familiar ->
                                !uiState.familiaresDelAlumno.any { it.dni == familiar.dni }
                            }
                            
                            if (familiaresDisponibles.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay familiares disponibles") },
                                    onClick = { dropdownExpanded = false }
                                )
                            } else {
                                familiaresDisponibles.forEach { familiar ->
                                    DropdownMenuItem(
                                        text = { Text("${familiar.nombre} ${familiar.apellidos}") },
                                        onClick = {
                                            selectedFamiliar = familiar
                                            dropdownExpanded = false
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
                                Text(subtipo.name.lowercase().let { capitalizeFirst(it) })
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
}

@Composable
fun AlumnoListItem(
    alumno: Usuario,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumno.nombre.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del alumno
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "DNI: ${alumno.dni}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botón para ver familiares/vincular
            IconButton(onClick = onItemClick) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Ver familiares",
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                
                Text(
                    text = "DNI: ${familiar.dni}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Mostrar el tipo de familiar
                val tipoFamiliar = familiar.perfiles
                    .firstOrNull { it.tipo == TipoUsuario.FAMILIAR }
                    ?.subtipo?.name?.lowercase()?.capitalize()
                
                if (tipoFamiliar != null) {
                    Text(
                        text = tipoFamiliar,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Botón para ver información
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
                contentDescription = "Desvincular",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Función auxiliar para capitalizar strings
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
} 