package com.tfg.umeegunero.feature.familiar.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import timber.log.Timber
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.ExperimentalFoundationApi
import com.tfg.umeegunero.ui.theme.FamiliarColor

// Enumeración de tipos de notificación
enum class TipoNotificacion(val color: Color) {
    MENSAJE(Color(0xFF2196F3)), // Azul
    TAREA(Color(0xFF4CAF50)),   // Verde
    EVENTO(Color(0xFFFF9800)),  // Naranja
    CALIFICACION(Color(0xFFFFEB3B)), // Amarillo
    ASISTENCIA(Color(0xFF9C27B0)), // Púrpura
    GENERAL(Color(0xFF607D8B))  // Gris azulado
}

// Enumeración de filtros disponibles
enum class FiltroNotificacion {
    TODAS,
    NO_LEIDAS,
    MENSAJES,
    TAREAS,
    EVENTOS,
    OTROS
}

// Modelo de datos para una notificación
data class Notificacion(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val fecha: Date,
    val tipo: TipoNotificacion,
    val leida: Boolean = false,
    val remitente: String = "",
    val accion: String? = null // URL, ruta de navegación o null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificacionesFamiliaScreen(
    navController: NavController
) {
    // Estado para las notificaciones
    val notificacionesBase = remember { generarNotificacionesDePrueba() }
    
    // Estado para el filtro seleccionado
    var filtroSeleccionado by rememberSaveable { mutableStateOf(FiltroNotificacion.TODAS) }
    
    // Estado para el modo de selección
    var modoSeleccion by rememberSaveable { mutableStateOf(false) }
    
    // Estado para las notificaciones seleccionadas
    var notificacionesSeleccionadas by rememberSaveable { mutableStateOf(emptySet<String>()) }
    
    // Estado para mostrar diálogo de confirmación de eliminación
    var mostrarDialogoConfirmacion by rememberSaveable { mutableStateOf(false) }
    
    // Función para marcar una notificación como leída
    val marcarComoLeida = { id: String ->
        // En un entorno real, esto haría una llamada a la base de datos
        // Por ahora simulamos el cambio en la UI
        notificacionesBase.find { it.id == id }?.let {
            it.copy(leida = true)
        }
    }
    
    // Filtrar notificaciones según el filtro seleccionado
    val notificacionesFiltradas = when (filtroSeleccionado) {
        FiltroNotificacion.TODAS -> notificacionesBase
        FiltroNotificacion.NO_LEIDAS -> notificacionesBase.filter { !it.leida }
        FiltroNotificacion.MENSAJES -> notificacionesBase.filter { it.tipo == TipoNotificacion.MENSAJE }
        FiltroNotificacion.TAREAS -> notificacionesBase.filter { it.tipo == TipoNotificacion.TAREA }
        FiltroNotificacion.EVENTOS -> notificacionesBase.filter { it.tipo == TipoNotificacion.EVENTO }
        FiltroNotificacion.OTROS -> notificacionesBase.filter { 
            it.tipo != TipoNotificacion.MENSAJE && 
            it.tipo != TipoNotificacion.TAREA && 
            it.tipo != TipoNotificacion.EVENTO 
        }
    }
    
    // Mostrar diálogo de confirmación para eliminar notificaciones
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Eliminar notificaciones") },
            text = { 
                Text(
                    "¿Estás seguro de que quieres eliminar ${notificacionesSeleccionadas.size} ${if (notificacionesSeleccionadas.size == 1) "notificación" else "notificaciones"}?"
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Eliminar notificaciones seleccionadas (simulación)
                        // notificacionesBase.removeAll { it.id in notificacionesSeleccionadas }
                        
                        // Resetear estados
                        modoSeleccion = false
                        notificacionesSeleccionadas = emptySet()
                        mostrarDialogoConfirmacion = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
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
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // Modo de selección
                    if (modoSeleccion) {
                        // Contador de seleccionados
                        Text(
                            text = "${notificacionesSeleccionadas.size} seleccionadas",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = Color.White
                        )
                        
                        // Botón para cancelar la selección
                        IconButton(onClick = { 
                            modoSeleccion = false
                            notificacionesSeleccionadas = emptySet()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar selección"
                            )
                        }
                        
                        // Botón para eliminar seleccionadas
                        IconButton(
                            onClick = { mostrarDialogoConfirmacion = true },
                            enabled = notificacionesSeleccionadas.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar seleccionadas"
                            )
                        }
                    } else {
                        // Botón para marcar todo como leído
                        IconButton(onClick = {
                            // En un entorno real, esto haría una llamada a la base de datos
                            // notificacionesBase.forEach { it.leida = true }
                        }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Marcar todo como leído"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(SnackbarHostState()) }
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
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay notificaciones",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Las notificaciones importantes aparecerán aquí",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            isSelected = notificacionesSeleccionadas.contains(notificacion.id),
                            selectionMode = modoSeleccion,
                            onNotificacionClick = {
                                if (modoSeleccion) {
                                    // En modo selección, togglear selección
                                    notificacionesSeleccionadas = if (notificacionesSeleccionadas.contains(notificacion.id)) {
                                        notificacionesSeleccionadas - notificacion.id
                                    } else {
                                        notificacionesSeleccionadas + notificacion.id
                                    }
                                } else {
                                    // En modo normal, abrir detalle (falta implementar)
                                    if (notificacion.accion != null) {
                                        // Navegar a la acción correspondiente (ej: detalle de evento)
                                        // Aquí iría un navController.navigate(notificacion.accion)
                                    }
                                }
                            },
                            onLongClick = {
                                // Activar modo selección
                                if (!modoSeleccion) {
                                    modoSeleccion = true
                                    notificacionesSeleccionadas = setOf(notificacion.id)
                                }
                            },
                            onMarcarLeida = { marcarComoLeida(notificacion.id) }
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
        contentColor = FamiliarColor
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onNotificacionClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onMarcarLeida: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    onNotificacionClick() 
                    if (!notificacion.leida && !selectionMode) {
                        onMarcarLeida()
                    }
                },
                onLongClick = { onLongClick() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                !notificacion.leida -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox o icono dependiendo del modo
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onNotificacionClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                // Icono de tipo de notificación
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(notificacion.tipo.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (notificacion.tipo) {
                            TipoNotificacion.MENSAJE -> Icons.Default.Message
                            TipoNotificacion.TAREA -> Icons.Default.Assignment
                            TipoNotificacion.EVENTO -> Icons.Default.Event
                            TipoNotificacion.CALIFICACION -> Icons.Default.Star
                            TipoNotificacion.ASISTENCIA -> Icons.Default.Person
                            TipoNotificacion.GENERAL -> Icons.Default.Notifications
                        },
                        contentDescription = notificacion.tipo.name,
                        tint = notificacion.tipo.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Título con indicador de no leído
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notificacion.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (!notificacion.leida) {
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(notificacion.tipo.color)
                            )
                        }
                    }
                    
                    // Fecha
                    Text(
                        text = formatDate(notificacion.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Remitente
                if (notificacion.remitente.isNotEmpty()) {
                    Text(
                        text = "De: ${notificacion.remitente}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Mensaje
                Text(
                    text = notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (!notificacion.leida) 0.9f else 0.7f
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Formato de fecha para notificaciones
private fun formatDate(date: Date): String {
    val now = Calendar.getInstance()
    val notifDate = Calendar.getInstance().apply { time = date }
    
    return when {
        // Hoy - mostrar hora
        now.get(Calendar.DAY_OF_YEAR) == notifDate.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == notifDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // Ayer
        now.get(Calendar.DAY_OF_YEAR) - notifDate.get(Calendar.DAY_OF_YEAR) == 1 &&
                now.get(Calendar.YEAR) == notifDate.get(Calendar.YEAR) -> {
            "Ayer"
        }
        // Esta semana - mostrar día
        now.get(Calendar.WEEK_OF_YEAR) == notifDate.get(Calendar.WEEK_OF_YEAR) &&
                now.get(Calendar.YEAR) == notifDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("EEEE", Locale("es", "ES")).format(date).capitalize()
        }
        // Otros casos - mostrar fecha completa
        else -> {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    }
}

// Generar datos de prueba
private fun generarNotificacionesDePrueba(): List<Notificacion> {
    val calendar = Calendar.getInstance()
    val ahora = calendar.time
    
    // Ayer
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val ayer = calendar.time
    
    // Hace 2 días
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val haceDosDias = calendar.time
    
    // Hace una semana
    calendar.time = ahora
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val haceSemana = calendar.time
    
    // Fecha futura para evento próximo
    calendar.time = ahora
    calendar.add(Calendar.DAY_OF_YEAR, 5)
    val fechaFutura = calendar.time
    
    return listOf(
        Notificacion(
            id = "1",
            titulo = "Reunión de padres y profesores",
            mensaje = "Se ha programado una reunión para discutir el progreso del primer trimestre el próximo lunes a las 17:00.",
            fecha = ahora,
            tipo = TipoNotificacion.EVENTO,
            leida = false,
            remitente = "Javier Fernández (Director)",
            accion = "eventos/123"
        ),
        Notificacion(
            id = "2",
            titulo = "Nuevo mensaje del tutor",
            mensaje = "Pablo ha mostrado un gran avance en matemáticas este mes. Estamos muy contentos con su progreso.",
            fecha = ahora,
            tipo = TipoNotificacion.MENSAJE,
            leida = false,
            remitente = "Ana García (Tutora)",
            accion = "mensajes/456"
        ),
        Notificacion(
            id = "3",
            titulo = "Recordatorio de excursión",
            mensaje = "Recuerde enviar la autorización firmada para la excursión al Museo de Ciencias antes del jueves.",
            fecha = ayer,
            tipo = TipoNotificacion.TAREA,
            leida = true,
            remitente = "Secretaría",
            accion = "tareas/789"
        ),
        Notificacion(
            id = "4",
            titulo = "Comida de hoy",
            mensaje = "Su hijo ha comido muy bien hoy: primer plato completo, segundo plato parcial y postre completo.",
            fecha = ayer,
            tipo = TipoNotificacion.GENERAL,
            leida = true,
            remitente = "Comedor escolar"
        ),
        Notificacion(
            id = "5",
            titulo = "Día no lectivo",
            mensaje = "Le recordamos que el próximo viernes 24 es día no lectivo por festividad local.",
            fecha = haceDosDias,
            tipo = TipoNotificacion.EVENTO,
            leida = true,
            remitente = "Dirección del centro",
            accion = "calendario/festivos"
        ),
        Notificacion(
            id = "6",
            titulo = "Taller de lectura",
            mensaje = "Invitamos a los padres al taller de fomento de lectura que se realizará el próximo sábado de 10:00 a 12:00.",
            fecha = haceSemana,
            tipo = TipoNotificacion.EVENTO,
            leida = true,
            remitente = "Departamento de Lengua",
            accion = "eventos/789"
        ),
        Notificacion(
            id = "7",
            titulo = "Festival de fin de curso",
            mensaje = "Comenzamos los preparativos para el festival de fin de curso. Próximamente más información.",
            fecha = fechaFutura,
            tipo = TipoNotificacion.EVENTO,
            leida = false,
            remitente = "Coordinación de Actividades",
            accion = "eventos/101"
        ),
        Notificacion(
            id = "8",
            titulo = "Solicitud de tutoría",
            mensaje = "La profesora Carmen solicita una tutoría para hablar sobre el rendimiento en la asignatura de inglés.",
            fecha = ahora,
            tipo = TipoNotificacion.MENSAJE,
            leida = false,
            remitente = "Carmen Vázquez (Profesora de inglés)",
            accion = "tutorias/202"
        )
    )
}

// Extensión para capitalizar la primera letra
private fun String.capitalize(): String {
    return if (this.isEmpty()) this
    else this.substring(0, 1).uppercase() + this.substring(1)
} 