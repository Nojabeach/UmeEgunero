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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasFamiliaScreen(
    navController: NavController,
    alumnoId: String = "1" // Parámetro de ejemplo, en una app real vendría de la navegación
) {
    // Estado para los filtros
    var filtroSeleccionado by remember { mutableStateOf(FiltroTarea.TODAS) }
    
    // Estado para gestionar la selección de alumno (útil para familias con varios hijos)
    var alumnoSeleccionadoId by remember { mutableStateOf(alumnoId) }
    
    // Datos de ejemplo para alumnos (para familias con varios hijos)
    val alumnos = remember {
        listOf(
            AlumnoBasico(id = "1", nombre = "Ana", apellidos = "López García"),
            AlumnoBasico(id = "2", nombre = "Carlos", apellidos = "López García")
        )
    }
    
    // Datos de ejemplo para tareas
    val tareas = remember {
        listOf(
            Tarea(
                id = "1",
                titulo = "Proyecto de ciencias",
                descripcion = "Realizar un proyecto sobre el sistema solar con materiales reciclados",
                fechaEntrega = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }.time,
                asignatura = "Ciencias Naturales",
                profesorNombre = "Laura García",
                estado = EstadoTarea.PENDIENTE,
                prioridad = PrioridadTarea.ALTA,
                alumnoId = "1"
            ),
            Tarea(
                id = "2",
                titulo = "Ejercicios de matemáticas",
                descripcion = "Completar los ejercicios de la página 45 del libro de matemáticas",
                fechaEntrega = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }.time,
                asignatura = "Matemáticas",
                profesorNombre = "Manuel Sánchez",
                estado = EstadoTarea.EN_PROGRESO,
                prioridad = PrioridadTarea.MEDIA,
                alumnoId = "1"
            ),
            Tarea(
                id = "3",
                titulo = "Lectura y resumen",
                descripcion = "Leer el capítulo 3 del libro 'El principito' y hacer un resumen de una página",
                fechaEntrega = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }.time,
                asignatura = "Lengua",
                profesorNombre = "Ana Martínez",
                estado = EstadoTarea.PENDIENTE,
                prioridad = PrioridadTarea.BAJA,
                alumnoId = "1"
            ),
            Tarea(
                id = "4",
                titulo = "Maqueta histórica",
                descripcion = "Crear una maqueta de un monumento histórico estudiado en clase",
                fechaEntrega = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                asignatura = "Historia",
                profesorNombre = "Pablo Ruiz",
                estado = EstadoTarea.COMPLETADA,
                prioridad = PrioridadTarea.ALTA,
                alumnoId = "1"
            ),
            Tarea(
                id = "5",
                titulo = "Vocabulario en inglés",
                descripcion = "Aprender el vocabulario de la unidad 5 y prepararse para el examen oral",
                fechaEntrega = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 4) }.time,
                asignatura = "Inglés",
                profesorNombre = "María Gómez",
                estado = EstadoTarea.PENDIENTE,
                prioridad = PrioridadTarea.MEDIA,
                alumnoId = "2"
            )
        )
    }
    
    // Filtrar tareas según el filtro seleccionado y el alumno seleccionado
    val tareasFiltradas = tareas.filter { tarea ->
        tarea.alumnoId == alumnoSeleccionadoId && when (filtroSeleccionado) {
            FiltroTarea.TODAS -> true
            FiltroTarea.PENDIENTES -> tarea.estado == EstadoTarea.PENDIENTE
            FiltroTarea.EN_PROGRESO -> tarea.estado == EstadoTarea.EN_PROGRESO
            FiltroTarea.COMPLETADAS -> tarea.estado == EstadoTarea.COMPLETADA
            FiltroTarea.RETRASADAS -> {
                val fechaActual = Calendar.getInstance().time
                tarea.fechaEntrega.before(fechaActual) && tarea.estado != EstadoTarea.COMPLETADA
            }
        }
    }
    
    // Scope para el Scaffold
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
                    if (alumnos.size > 1) {
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
                                alumnos.forEach { alumno ->
                                    DropdownMenuItem(
                                        text = { Text("${alumno.nombre} ${alumno.apellidos}") },
                                        onClick = {
                                            alumnoSeleccionadoId = alumno.id
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
                filtroSeleccionado = filtroSeleccionado,
                onFiltroSelected = { filtroSeleccionado = it }
            )
            
            // Contenido principal
            if (tareasFiltradas.isEmpty()) {
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
                            text = when (filtroSeleccionado) {
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
                    items(tareasFiltradas) { tarea ->
                        TareaCard(
                            tarea = tarea,
                            onTareaClick = { 
                                // Navegar al detalle de la tarea o ejecutar alguna acción
                                scope.launch {
                                    snackbarHostState.showSnackbar("Viendo detalle de tarea: ${tarea.titulo}")
                                }
                            },
                            onTareaStateChange = { nuevoEstado ->
                                // Aquí se actualizaría el estado de la tarea en un caso real
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tarea marcada como $nuevoEstado")
                                }
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

@Composable
fun TareaCard(
    tarea: Tarea,
    onTareaClick: () -> Unit,
    onTareaStateChange: (EstadoTarea) -> Unit
) {
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
                
                // Checkbox o indicador de estado
                Checkbox(
                    checked = tarea.estado == EstadoTarea.COMPLETADA,
                    onCheckedChange = { isChecked ->
                        onTareaStateChange(
                            if (isChecked) EstadoTarea.COMPLETADA else EstadoTarea.PENDIENTE
                        )
                    }
                )
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
                        text = tarea.asignatura,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Fecha de entrega
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaActual = Calendar.getInstance().time
                    val esRetrasada = tarea.fechaEntrega.before(fechaActual) && 
                                     tarea.estado != EstadoTarea.COMPLETADA
                    
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (esRetrasada) Color.Red else MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = dateFormat.format(tarea.fechaEntrega),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (esRetrasada) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Etiqueta de estado
            if (tarea.estado != EstadoTarea.PENDIENTE || 
                tarea.fechaEntrega.before(Calendar.getInstance().time)) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(4.dp),
                    color = when {
                        tarea.estado == EstadoTarea.COMPLETADA -> Color(0xFF4CAF50) // Green
                        tarea.estado == EstadoTarea.EN_PROGRESO -> Color(0xFF2196F3) // Blue
                        tarea.fechaEntrega.before(Calendar.getInstance().time) -> Color.Red
                        else -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        text = when {
                            tarea.estado == EstadoTarea.COMPLETADA -> "Completada"
                            tarea.estado == EstadoTarea.EN_PROGRESO -> "En progreso"
                            tarea.fechaEntrega.before(Calendar.getInstance().time) -> "Retrasada"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
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
    BAJA
}

enum class FiltroTarea {
    TODAS,
    PENDIENTES,
    EN_PROGRESO,
    COMPLETADAS,
    RETRASADAS
} 