package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.PrioridadTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.feature.familiar.viewmodel.FiltroTarea
import com.tfg.umeegunero.feature.familiar.viewmodel.TareasFamiliaViewModel
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasFamiliaScreen(
    navController: NavController,
    familiarId: String,
    viewModel: TareasFamiliaViewModel = hiltViewModel()
) {
    // Inicializar ViewModel
    LaunchedEffect(familiarId) {
        viewModel.inicializar(familiarId)
    }
    
    // Observar estado de la UI
    val uiState by viewModel.uiState.collectAsState()
    
    // Scope para el Scaffold
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar mensajes de error o éxito
    LaunchedEffect(uiState.error, uiState.mensaje) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensajes()
            }
        }
        
        uiState.mensaje?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensajes()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas Escolares") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Selector de alumno (solo visible si hay más de un alumno)
                    if (uiState.alumnos.size > 1) {
                        var expandido by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expandido = true }) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Seleccionar alumno",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            
                            DropdownMenu(
                                expanded = expandido,
                                onDismissRequest = { expandido = false }
                            ) {
                                uiState.alumnos.forEach { alumno ->
                                    DropdownMenuItem(
                                        text = { Text(alumno.nombreCompleto) },
                                        onClick = {
                                            viewModel.cargarTareas(alumno.id)
                                            expandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros
            FiltrosTareasBar(
                filtroSeleccionado = uiState.filtroSeleccionado,
                onFiltroSelected = { viewModel.cambiarFiltro(it) }
            )
            
            // Indicador de carga
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Contenido principal
                if (uiState.tareasFiltradas.isEmpty()) {
                    // Mensaje cuando no hay tareas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = when (uiState.filtroSeleccionado) {
                                    FiltroTarea.TODAS -> "No hay tareas asignadas"
                                    FiltroTarea.PENDIENTES -> "No hay tareas pendientes"
                                    FiltroTarea.EN_PROGRESO -> "No hay tareas en progreso"
                                    FiltroTarea.COMPLETADAS -> "No hay tareas completadas"
                                    FiltroTarea.RETRASADAS -> "No hay tareas retrasadas"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Lista de tareas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(uiState.tareasFiltradas) { tarea ->
                            TareaCard(
                                tarea = tarea,
                                onTareaClick = { 
                                    // Navegar a la pantalla de detalle de tarea
                                    navController.navigate(
                                        AppScreens.DetalleTareaAlumno.createRoute(tarea.id)
                                    )
                                },
                                onRevisarTarea = { comentario ->
                                    viewModel.marcarTareaComoRevisada(tarea.id, comentario)
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Espacio adicional al final de la lista
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FiltrosTareasBar(
    filtroSeleccionado: FiltroTarea,
    onFiltroSelected: (FiltroTarea) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = filtroSeleccionado.ordinal,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        FiltroTarea.values().forEach { filtro ->
            Tab(
                selected = filtroSeleccionado == filtro,
                onClick = { onFiltroSelected(filtro) },
                text = { 
                    Text(
                        text = when (filtro) {
                            FiltroTarea.TODAS -> "Todas"
                            FiltroTarea.PENDIENTES -> "Pendientes"
                            FiltroTarea.EN_PROGRESO -> "En progreso"
                            FiltroTarea.COMPLETADAS -> "Completadas"
                            FiltroTarea.RETRASADAS -> "Retrasadas"
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaCard(
    tarea: Tarea,
    onTareaClick: () -> Unit,
    onRevisarTarea: (String) -> Unit
) {
    var mostrarDialogoRevision by remember { mutableStateOf(false) }
    var comentarioRevision by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onTareaClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Primera fila: título y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título con indicador de prioridad
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (tarea.prioridad) {
                                    PrioridadTarea.ALTA -> Color.Red
                                    PrioridadTarea.MEDIA -> Color(0xFFFFA500) // Orange
                                    PrioridadTarea.BAJA -> Color(0xFF4CAF50) // Green
                                    PrioridadTarea.URGENTE -> Color(0xFF5C0000) // Dark Red
                                    else -> Color.Gray
                                }
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = tarea.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (tarea.estado == EstadoTarea.COMPLETADA) 
                                        TextDecoration.LineThrough 
                                        else TextDecoration.None
                    )
                }
                
                // Botón de revisión
                if (!tarea.revisadaPorFamiliar) {
                    IconButton(onClick = { mostrarDialogoRevision = true }) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Marcar como revisada",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "Ya revisada",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Descripción
            if (tarea.descripcion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tarea.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Asignatura
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = tarea.asignatura ?: "Sin asignatura",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Fecha de entrega
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaEntrega = tarea.fechaEntrega?.toDate()
                    val esRetrasada = fechaEntrega != null && 
                                      fechaEntrega.before(Calendar.getInstance().time) && 
                                      tarea.estado != EstadoTarea.COMPLETADA
                    
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (esRetrasada) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = if (fechaEntrega != null) dateFormat.format(fechaEntrega) else "Sin fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (esRetrasada) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Etiqueta de estado y revisión
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de revisión
                if (tarea.revisadaPorFamiliar) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Revisada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Etiqueta de estado
                Surface(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    shape = RoundedCornerShape(4.dp),
                    color = when {
                        tarea.estado == EstadoTarea.COMPLETADA -> Color(0xFF4CAF50) // Green
                        tarea.estado == EstadoTarea.EN_PROGRESO -> Color(0xFF2196F3) // Blue
                        tarea.fechaEntrega?.toDate()?.before(Calendar.getInstance().time) == true -> Color.Red
                        else -> Color(0xFFFFA500) // Orange para PENDIENTE
                    }
                ) {
                    Text(
                        text = when {
                            tarea.estado == EstadoTarea.COMPLETADA -> "Completada"
                            tarea.estado == EstadoTarea.EN_PROGRESO -> "En progreso"
                            tarea.fechaEntrega?.toDate()?.before(Calendar.getInstance().time) == true -> "Retrasada"
                            else -> "Pendiente"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
    
    // Diálogo para revisar tarea
    if (mostrarDialogoRevision) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRevision = false },
            title = { Text("Revisar tarea") },
            text = {
                Column {
                    Text("Marcar esta tarea como revisada por ti")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comentarioRevision,
                        onValueChange = { comentarioRevision = it },
                        label = { Text("Comentario (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRevisarTarea(comentarioRevision)
                        mostrarDialogoRevision = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoRevision = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Modelos de datos para las tareas
data class AlumnoBasico(
    val id: String,
    val nombre: String,
    val apellidos: String
)

data class Tarea(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fechaEntrega: Date,
    val asignatura: String,
    val profesorNombre: String,
    val estado: EstadoTarea,
    val prioridad: PrioridadTarea,
    val alumnoId: String
)

enum class EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA
}

enum class PrioridadTarea {
    ALTA,
    MEDIA,
    BAJA,
    URGENTE
}

enum class FiltroTarea {
    TODAS,
    PENDIENTES,
    EN_PROGRESO,
    COMPLETADAS,
    RETRASADAS
} 