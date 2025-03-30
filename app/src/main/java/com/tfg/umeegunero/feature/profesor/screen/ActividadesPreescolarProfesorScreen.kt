package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoActividad
import com.tfg.umeegunero.feature.profesor.viewmodel.ActividadesPreescolarProfesorViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.FiltroActividadProfesor
import com.tfg.umeegunero.ui.theme.colorCategoriaActividad

/**
 * Pantalla para que los profesores gestionen actividades preescolares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadesPreescolarProfesorScreen(
    profesorId: String,
    profesorNombre: String,
    onBackClick: () -> Unit,
    viewModel: ActividadesPreescolarProfesorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Inicializar ViewModel cuando se inicia la pantalla
    LaunchedEffect(profesorId) {
        viewModel.inicializar(profesorId, profesorNombre)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividades Preescolares") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // Mostrar diálogo para crear nueva actividad
                        viewModel.mostrarDialogoCrearActividad()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear actividad"
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Mostrar diálogo para crear nueva actividad
                    viewModel.mostrarDialogoCrearActividad()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear actividad",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Indicador de carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    // Mensaje de error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.inicializar(profesorId, profesorNombre) }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Selector de clase
                        ClaseSelector(
                            clases = uiState.clases,
                            claseSeleccionadaId = uiState.claseSeleccionadaId,
                            onClaseSelected = { viewModel.seleccionarClase(it) }
                        )
                        
                        // Filtros
                        FiltrosActividades(
                            filtroSeleccionado = uiState.filtroSeleccionado,
                            categoriaSeleccionada = uiState.categoriaSeleccionada,
                            onFiltroSeleccionado = { viewModel.aplicarFiltro(it) },
                            onCategoriaSeleccionada = { viewModel.aplicarFiltroCategoria(it) }
                        )
                        
                        // Lista de actividades
                        if (uiState.actividadesFiltradas.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hay actividades para mostrar",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Crea una nueva actividad pulsando el botón +",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(uiState.actividadesFiltradas) { actividad ->
                                    ActividadItem(
                                        actividad = actividad,
                                        onClick = { viewModel.editarActividad(actividad.id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo para crear/editar actividad
    if (uiState.mostrarDialogoNuevaActividad) {
        DialogoCrearEditarActividad(
            actividad = uiState.actividadEnEdicion,
            alumnos = uiState.alumnos,
            modoEdicion = uiState.modoEdicion,
            onDismiss = { viewModel.ocultarDialogoNuevaActividad() },
            onGuardar = { actividad ->
                if (uiState.modoEdicion) {
                    viewModel.actualizarActividad(actividad)
                } else {
                    viewModel.crearActividad(actividad)
                }
            },
            onEliminar = { 
                if (uiState.modoEdicion && uiState.actividadEnEdicion != null) {
                    viewModel.eliminarActividad(uiState.actividadEnEdicion!!.id)
                }
            }
        )
    }
}

@Composable
fun ClaseSelector(
    clases: List<com.tfg.umeegunero.feature.profesor.viewmodel.ClasePreescolarInfo>,
    claseSeleccionadaId: String,
    onClaseSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val claseSeleccionada = clases.find { it.id == claseSeleccionadaId }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Clase",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = claseSeleccionada?.nombre ?: "Selecciona una clase",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    clases.forEach { clase ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = clase.nombre + " (${clase.numAlumnos} alumnos)"
                                ) 
                            },
                            onClick = {
                                onClaseSelected(clase.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FiltrosActividades(
    filtroSeleccionado: FiltroActividadProfesor,
    categoriaSeleccionada: CategoriaActividad?,
    onFiltroSeleccionado: (FiltroActividadProfesor) -> Unit,
    onCategoriaSeleccionada: (CategoriaActividad?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Filtros",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Filtros principales
        ScrollableTabRow(
            selectedTabIndex = FiltroActividadProfesor.values().indexOf(filtroSeleccionado),
            edgePadding = 0.dp
        ) {
            FiltroActividadProfesor.values().forEachIndexed { index, filtro ->
                Tab(
                    selected = filtroSeleccionado == filtro,
                    onClick = { onFiltroSeleccionado(filtro) },
                    text = {
                        Text(
                            text = when (filtro) {
                                FiltroActividadProfesor.TODAS -> "Todas"
                                FiltroActividadProfesor.PENDIENTES -> "Pendientes"
                                FiltroActividadProfesor.REALIZADAS -> "Realizadas"
                                FiltroActividadProfesor.RECIENTES -> "Recientes"
                                FiltroActividadProfesor.MIS_ACTIVIDADES -> "Mis actividades"
                            }
                        )
                    }
                )
            }
        }
        
        // Filtros por categoría
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = categoriaSeleccionada == null,
                onClick = { onCategoriaSeleccionada(null) },
                label = { Text("Todas") }
            )
            
            CategoriaActividad.values().forEach { categoria ->
                FilterChip(
                    selected = categoriaSeleccionada == categoria,
                    onClick = { onCategoriaSeleccionada(categoria) },
                    label = { Text(categoria.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorCategoriaActividad(categoria).copy(alpha = 0.8f),
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun ActividadItem(
    actividad: ActividadPreescolar,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorCategoriaActividad(actividad.categoria))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (actividad.categoria) {
                        CategoriaActividad.JUEGO -> Icons.Default.VideogameAsset
                        CategoriaActividad.MOTOR -> Icons.AutoMirrored.Filled.DirectionsRun
                        CategoriaActividad.LENGUAJE -> Icons.Default.RecordVoiceOver
                        CategoriaActividad.MUSICA -> Icons.Default.MusicNote
                        CategoriaActividad.ARTE -> Icons.Default.Palette
                        CategoriaActividad.EXPLORACION -> Icons.Default.Explore
                        CategoriaActividad.AUTONOMIA -> Icons.Default.PersonOutline
                        CategoriaActividad.OTRA -> Icons.Default.MoreHoriz
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido de la actividad
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = actividad.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = actividad.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Estado
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (actividad.estado) {
                                    EstadoActividad.PENDIENTE -> "Pendiente"
                                    EstadoActividad.REALIZADA -> "Realizada"
                                    EstadoActividad.CANCELADA -> "Cancelada"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = when (actividad.estado) {
                                EstadoActividad.PENDIENTE -> MaterialTheme.colorScheme.errorContainer
                                EstadoActividad.REALIZADA -> MaterialTheme.colorScheme.primaryContainer
                                EstadoActividad.CANCELADA -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                    
                    // Fecha
                    Text(
                        text = actividad.fechaCreacion.let { 
                            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(it.toDate())
                        } ?: "Sin fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar"
                )
            }
        }
    }
}

@Composable
fun DialogoCrearEditarActividad(
    actividad: ActividadPreescolar?,
    alumnos: List<com.tfg.umeegunero.feature.profesor.viewmodel.AlumnoPreescolarProfesorInfo>,
    modoEdicion: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (ActividadPreescolar) -> Unit,
    onEliminar: () -> Unit
) {
    var titulo by remember { mutableStateOf(actividad?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(actividad?.descripcion ?: "") }
    var categoria by remember { mutableStateOf(actividad?.categoria ?: CategoriaActividad.JUEGO) }
    var alumnoSeleccionadoId by remember { mutableStateOf(actividad?.alumnoId ?: "") }
    var estado by remember { mutableStateOf(actividad?.estado ?: EstadoActividad.PENDIENTE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (modoEdicion) "Editar actividad" else "Nueva actividad") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selección de alumno
                Text(
                    text = "Alumno",
                    style = MaterialTheme.typography.labelLarge
                )
                
                var expandedAlumno by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expandedAlumno,
                    onExpandedChange = { expandedAlumno = it }
                ) {
                    OutlinedTextField(
                        value = alumnos.find { it.id == alumnoSeleccionadoId }?.let { "${it.nombre} ${it.apellidos}" } 
                            ?: "Selecciona un alumno",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlumno)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedAlumno,
                        onDismissRequest = { expandedAlumno = false }
                    ) {
                        alumnos.forEach { alumno ->
                            DropdownMenuItem(
                                text = { Text("${alumno.nombre} ${alumno.apellidos}") },
                                onClick = {
                                    alumnoSeleccionadoId = alumno.id
                                    expandedAlumno = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selección de categoría
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelLarge
                )
                
                var expandedCategoria by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expandedCategoria,
                    onExpandedChange = { expandedCategoria = it }
                ) {
                    OutlinedTextField(
                        value = categoria.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        CategoriaActividad.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    categoria = cat
                                    expandedCategoria = false
                                }
                            )
                        }
                    }
                }
                
                // Si está en modo edición, mostrar selector de estado
                if (modoEdicion) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Estado",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    var expandedEstado by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedEstado,
                        onExpandedChange = { expandedEstado = it }
                    ) {
                        OutlinedTextField(
                            value = estado.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedEstado,
                            onDismissRequest = { expandedEstado = false }
                        ) {
                            EstadoActividad.values().forEach { est ->
                                DropdownMenuItem(
                                    text = { Text(est.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        estado = est
                                        expandedEstado = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevaActividad = actividad?.copy(
                        titulo = titulo,
                        descripcion = descripcion,
                        categoria = categoria,
                        alumnoId = alumnoSeleccionadoId,
                        estado = estado
                    ) ?: ActividadPreescolar(
                        id = "",
                        titulo = titulo,
                        descripcion = descripcion,
                        categoria = categoria,
                        alumnoId = alumnoSeleccionadoId,
                        profesorId = "",
                        claseId = "",
                        estado = EstadoActividad.PENDIENTE,
                        fechaCreacion = Timestamp.now()
                    )
                    
                    onGuardar(nuevaActividad)
                    onDismiss()
                },
                enabled = titulo.isNotBlank() && descripcion.isNotBlank() && alumnoSeleccionadoId.isNotBlank()
            ) {
                Text(if (modoEdicion) "Actualizar" else "Crear")
            }
        },
        dismissButton = {
            Row {
                if (modoEdicion) {
                    Button(
                        onClick = {
                            onEliminar()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
} 