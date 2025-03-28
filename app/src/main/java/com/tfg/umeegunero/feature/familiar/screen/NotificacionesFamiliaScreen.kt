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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesFamiliaScreen(
    navController: NavController
) {
    // Estado para los filtros
    var filtroSeleccionado by remember { mutableStateOf(FiltroNotificacion.TODAS) }
    
    // Datos de ejemplo para las notificaciones
    val notificaciones = remember {
        listOf(
            Notificacion(
                id = "1",
                titulo = "Nueva tarea asignada",
                mensaje = "Se ha asignado una nueva tarea de matemáticas con entrega para el viernes",
                fechaHora = Calendar.getInstance().apply { add(Calendar.HOUR, -2) }.timeInMillis,
                tipo = TipoNotificacion.TAREA,
                leida = false,
                alumnoId = "1",
                alumnoNombre = "Ana López",
                accionId = "tarea_1"
            ),
            Notificacion(
                id = "2",
                titulo = "Recordatorio de reunión",
                mensaje = "Recordatorio: Reunión de padres y profesores mañana a las 17:00",
                fechaHora = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.timeInMillis,
                tipo = TipoNotificacion.EVENTO,
                leida = true,
                alumnoId = null,
                alumnoNombre = null,
                accionId = "reunion_3"
            ),
            Notificacion(
                id = "3",
                titulo = "Calificación actualizada",
                mensaje = "Se ha publicado la calificación del examen de ciencias naturales. Carlos ha obtenido un 8.5",
                fechaHora = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }.timeInMillis,
                tipo = TipoNotificacion.CALIFICACION,
                leida = true,
                alumnoId = "2",
                alumnoNombre = "Carlos López",
                accionId = "calificacion_5"
            ),
            Notificacion(
                id = "4",
                titulo = "Registro de asistencia",
                mensaje = "Ana ha llegado 10 minutos tarde hoy a la primera clase",
                fechaHora = Calendar.getInstance().apply { add(Calendar.HOUR, -5) }.timeInMillis,
                tipo = TipoNotificacion.ASISTENCIA,
                leida = false,
                alumnoId = "1",
                alumnoNombre = "Ana López",
                accionId = null
            ),
            Notificacion(
                id = "5",
                titulo = "Nuevo mensaje del profesor",
                mensaje = "La profesora Laura ha enviado un mensaje sobre el progreso de Carlos en matemáticas",
                fechaHora = Calendar.getInstance().apply { add(Calendar.MINUTE, -30) }.timeInMillis,
                tipo = TipoNotificacion.MENSAJE,
                leida = false,
                alumnoId = "2",
                alumnoNombre = "Carlos López",
                accionId = "chat_prof123"
            )
        )
    }
    
    // Filtrar notificaciones según el filtro seleccionado
    val notificacionesFiltradas = notificaciones.filter { notificacion ->
        when (filtroSeleccionado) {
            FiltroNotificacion.TODAS -> true
            FiltroNotificacion.NO_LEIDAS -> !notificacion.leida
            FiltroNotificacion.MENSAJES -> notificacion.tipo == TipoNotificacion.MENSAJE
            FiltroNotificacion.TAREAS -> notificacion.tipo == TipoNotificacion.TAREA
            FiltroNotificacion.EVENTOS -> notificacion.tipo == TipoNotificacion.EVENTO
            FiltroNotificacion.OTROS -> notificacion.tipo != TipoNotificacion.MENSAJE && 
                                      notificacion.tipo != TipoNotificacion.TAREA && 
                                      notificacion.tipo != TipoNotificacion.EVENTO
        }
    }.sortedByDescending { it.fechaHora }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Botón para marcar todas como leídas
                    IconButton(onClick = { /* Marcar todas como leídas */ }) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Marcar todas como leídas"
                        )
                    }
                    
                    // Botón para eliminar notificaciones
                    IconButton(onClick = { /* Eliminar notificaciones */ }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar notificaciones"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de filtros
            FiltrosNotificacionesBar(
                filtroSeleccionado = filtroSeleccionado,
                onFiltroSelected = { filtroSeleccionado = it }
            )
            
            // Contenido principal
            if (notificacionesFiltradas.isEmpty()) {
                // Mensaje cuando no hay notificaciones
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
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = when (filtroSeleccionado) {
                                FiltroNotificacion.TODAS -> "No tienes notificaciones"
                                FiltroNotificacion.NO_LEIDAS -> "No tienes notificaciones sin leer"
                                FiltroNotificacion.MENSAJES -> "No tienes mensajes nuevos"
                                FiltroNotificacion.TAREAS -> "No tienes notificaciones de tareas"
                                FiltroNotificacion.EVENTOS -> "No tienes notificaciones de eventos"
                                FiltroNotificacion.OTROS -> "No tienes otras notificaciones"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Lista de notificaciones
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(notificacionesFiltradas) { notificacion ->
                        NotificacionItem(
                            notificacion = notificacion,
                            onNotificacionClick = {
                                // Navegar a la acción correspondiente
                                notificacion.accionId?.let { accionId ->
                                    when (notificacion.tipo) {
                                        TipoNotificacion.MENSAJE -> {
                                            val profesorId = accionId.removePrefix("chat_")
                                            navController.navigate("${AppScreens.ChatFamilia.route}/$profesorId")
                                        }
                                        TipoNotificacion.TAREA -> {
                                            val alumnoId = notificacion.alumnoId ?: return@let
                                            navController.navigate("${AppScreens.TareasFamilia.route}/$alumnoId")
                                        }
                                        TipoNotificacion.EVENTO -> {
                                            navController.navigate(AppScreens.CalendarioFamilia.route)
                                        }
                                        TipoNotificacion.CALIFICACION, TipoNotificacion.ASISTENCIA -> {
                                            val alumnoId = notificacion.alumnoId ?: return@let
                                            navController.navigate("${AppScreens.DetalleAlumnoFamilia.route}/$alumnoId")
                                        }
                                        TipoNotificacion.GENERAL -> {
                                            // No hacer nada o navegar a una pantalla general
                                        }
                                    }
                                }
                            },
                            onMarcarLeida = { /* Marcar como leída */ }
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
fun FiltrosNotificacionesBar(
    filtroSeleccionado: FiltroNotificacion,
    onFiltroSelected: (FiltroNotificacion) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = filtroSeleccionado.ordinal,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        FiltroNotificacion.values().forEach { filtro ->
            Tab(
                selected = filtroSeleccionado == filtro,
                onClick = { onFiltroSelected(filtro) },
                text = { 
                    Text(
                        text = when (filtro) {
                            FiltroNotificacion.TODAS -> "Todas"
                            FiltroNotificacion.NO_LEIDAS -> "No leídas"
                            FiltroNotificacion.MENSAJES -> "Mensajes"
                            FiltroNotificacion.TAREAS -> "Tareas"
                            FiltroNotificacion.EVENTOS -> "Eventos"
                            FiltroNotificacion.OTROS -> "Otros"
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    onNotificacionClick: () -> Unit,
    onMarcarLeida: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onNotificacionClick,
        colors = CardDefaults.cardColors(
            containerColor = if (!notificacion.leida) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono de tipo de notificación
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notificacion.tipo.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notificacion.tipo.icon,
                    contentDescription = null,
                    tint = notificacion.tipo.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Título
                Text(
                    text = notificacion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Normal
                )
                
                // Alumno (si aplica)
                if (notificacion.alumnoNombre != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notificacion.alumnoNombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Mensaje
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Fecha
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatearFechaRelativa(notificacion.fechaHora),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Indicador de no leída y menú de opciones
            if (!notificacion.leida) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { onMarcarLeida() }
            ) {
                Icon(
                    imageVector = if (notificacion.leida) Icons.Default.MoreVert else Icons.Default.Done,
                    contentDescription = if (notificacion.leida) "Opciones" else "Marcar como leída",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Función para formatear fecha relativa
fun formatearFechaRelativa(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Hace menos de un minuto"
        diff < 60 * 60 * 1000 -> {
            val minutes = diff / (60 * 1000)
            "Hace $minutes ${if (minutes == 1L) "minuto" else "minutos"}"
        }
        diff < 24 * 60 * 60 * 1000 -> {
            val hours = diff / (60 * 60 * 1000)
            "Hace $hours ${if (hours == 1L) "hora" else "horas"}"
        }
        diff < 48 * 60 * 60 * 1000 -> "Ayer"
        else -> {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
}

// Modelos de datos para las notificaciones
data class Notificacion(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val fechaHora: Long,
    val tipo: TipoNotificacion,
    val leida: Boolean,
    val alumnoId: String?,
    val alumnoNombre: String?,
    val accionId: String? // Identificador para navegar a una acción específica
)

enum class TipoNotificacion(val color: Color, val icon: ImageVector) {
    MENSAJE(Color(0xFF2196F3), Icons.Default.Message),
    TAREA(Color(0xFFF57C00), Icons.Default.Assignment),
    EVENTO(Color(0xFF4CAF50), Icons.Default.Event),
    CALIFICACION(Color(0xFF9C27B0), Icons.Default.Star),
    ASISTENCIA(Color(0xFFE91E63), Icons.Default.Person),
    GENERAL(Color(0xFF607D8B), Icons.Default.Notifications)
}

enum class FiltroNotificacion {
    TODAS,
    NO_LEIDAS,
    MENSAJES,
    TAREAS,
    EVENTOS,
    OTROS
}

// Objeto con referencias a AppScreens
object AppScreens {
    object ChatFamilia {
        const val route = "chat_familia"
    }
    
    object TareasFamilia {
        const val route = "tareas_familia"
    }
    
    object CalendarioFamilia {
        const val route = "calendario_familia"
    }
    
    object DetalleAlumnoFamilia {
        const val route = "detalle_alumno_familia"
    }
} 