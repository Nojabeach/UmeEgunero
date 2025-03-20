package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * Pantalla de notificaciones del sistema
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController
) {
    // Estado para filtrar notificaciones
    var filtroSeleccionado by remember { mutableStateOf(FiltroNotificacion.TODAS) }
    
    // Estado de la snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Datos simulados (en una implementación real vendrían del ViewModel)
    val notificaciones = remember {
        generarNotificacionesEjemplo()
    }
    
    // Filtrar las notificaciones según el filtro seleccionado
    val notificacionesFiltradas = when (filtroSeleccionado) {
        FiltroNotificacion.TODAS -> notificaciones
        FiltroNotificacion.NO_LEIDAS -> notificaciones.filter { !it.leida }
        FiltroNotificacion.SISTEMA -> notificaciones.filter { it.tipo == TipoNotificacion.SISTEMA }
        FiltroNotificacion.USUARIOS -> notificaciones.filter { it.tipo == TipoNotificacion.USUARIO }
        FiltroNotificacion.CENTROS -> notificaciones.filter { it.tipo == TipoNotificacion.CENTRO }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // Botón para marcar todas como leídas
                    IconButton(onClick = {
                        // Aquí se marcarían todas como leídas (en el ViewModel real)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Todas las notificaciones marcadas como leídas")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Marcar todas como leídas",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Menú de filtros
                    var expandedMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar notificaciones",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        FiltroNotificacion.values().forEach { filtro ->
                            DropdownMenuItem(
                                text = { Text(filtro.titulo) },
                                onClick = {
                                    filtroSeleccionado = filtro
                                    expandedMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = filtro.icono,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    if (filtroSeleccionado == filtro) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
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
                    // Aquí se implementaría la creación de una nueva notificación
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Funcionalidad para crear notificación")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nueva notificación",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contador
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NotificacionContador(
                        icono = Icons.Default.Notifications,
                        titulo = "Total",
                        cantidad = notificaciones.size,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Divider(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp)
                    )
                    
                    NotificacionContador(
                        icono = Icons.Default.MarkEmailUnread,
                        titulo = "No leídas",
                        cantidad = notificaciones.count { !it.leida },
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Divider(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp)
                    )
                    
                    NotificacionContador(
                        icono = Icons.Default.Today,
                        titulo = "Hoy",
                        cantidad = notificaciones.count { it.fechaHora.toLocalDate() == LocalDateTime.now().toLocalDate() },
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Lista de notificaciones
            if (notificacionesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay notificaciones",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notificacionesFiltradas) { notificacion ->
                        NotificacionItem(
                            notificacion = notificacion,
                            onMarkAsRead = {
                                // Aquí se marcaría como leída (en el ViewModel real)
                            },
                            onDelete = {
                                // Aquí se eliminaría la notificación (en el ViewModel real)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra un contador de notificaciones
 */
@Composable
fun NotificacionContador(
    icono: ImageVector,
    titulo: String,
    cantidad: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = cantidad.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Componente que muestra una notificación individual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotacionFlecha by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, 
        label = "rotacionFlecha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notificacion.leida) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono del tipo de notificación
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(notificacion.tipo.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = notificacion.tipo.icono,
                        contentDescription = null,
                        tint = notificacion.tipo.color
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notificacion.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    Text(
                        text = notificacion.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatoFecha(notificacion.fechaHora),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (!notificacion.leida) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    modifier = Modifier.rotate(rotacionFlecha)
                )
            }
            
            // Contenido expandido
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Divider()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!notificacion.leida) {
                            TextButton(
                                onClick = onMarkAsRead
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MarkEmailRead,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Marcar como leída")
                            }
                        }
                        
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Genera datos de ejemplo para la pantalla
 */
private fun generarNotificacionesEjemplo(): List<Notificacion> {
    val notificaciones = mutableListOf<Notificacion>()
    
    // Notificaciones del sistema
    notificaciones.add(
        Notificacion(
            id = 1,
            titulo = "Actualización del sistema",
            mensaje = "Se ha lanzado una nueva actualización del sistema con mejoras de rendimiento y nuevas funcionalidades.",
            tipo = TipoNotificacion.SISTEMA,
            fechaHora = LocalDateTime.now().minusHours(2),
            leida = false
        )
    )
    
    notificaciones.add(
        Notificacion(
            id = 2,
            titulo = "Mantenimiento programado",
            mensaje = "El sistema estará en mantenimiento el próximo domingo de 2:00 a 4:00 AM. Durante este periodo, algunas funcionalidades podrían no estar disponibles.",
            tipo = TipoNotificacion.SISTEMA,
            fechaHora = LocalDateTime.now().minusDays(1),
            leida = true
        )
    )
    
    // Notificaciones de usuarios
    notificaciones.add(
        Notificacion(
            id = 3,
            titulo = "Nuevo registro de profesor",
            mensaje = "El profesor Carlos Martínez ha completado su registro en el sistema y está pendiente de aprobación.",
            tipo = TipoNotificacion.USUARIO,
            fechaHora = LocalDateTime.now().minusHours(5),
            leida = false
        )
    )
    
    notificaciones.add(
        Notificacion(
            id = 4,
            titulo = "Solicitud de acceso",
            mensaje = "Ana López ha solicitado acceso como familiar para ver la información del alumno Miguel García.",
            tipo = TipoNotificacion.USUARIO,
            fechaHora = LocalDateTime.now().minusDays(2),
            leida = true
        )
    )
    
    // Notificaciones de centros
    notificaciones.add(
        Notificacion(
            id = 5,
            titulo = "Nuevo centro educativo",
            mensaje = "Se ha registrado el centro educativo 'IES Valle del Miro' y está pendiente de verificación.",
            tipo = TipoNotificacion.CENTRO,
            fechaHora = LocalDateTime.now().minusHours(12),
            leida = false
        )
    )
    
    notificaciones.add(
        Notificacion(
            id = 6,
            titulo = "Actualización de datos",
            mensaje = "El centro 'Colegio San Pablo' ha actualizado su información de contacto y dirección.",
            tipo = TipoNotificacion.CENTRO,
            fechaHora = LocalDateTime.now().minusDays(3),
            leida = true
        )
    )
    
    // Añadir algunas notificaciones aleatorias
    val titulos = listOf(
        "Recordatorio importante", 
        "Incidencia reportada", 
        "Nueva funcionalidad", 
        "Alerta de seguridad",
        "Confirmación de cambios",
        "Solicitud de revisión"
    )
    
    val mensajes = listOf(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam euismod, dui vel.",
        "Se ha detectado un problema en el módulo de calificaciones que requiere atención inmediata.",
        "Hay una nueva solicitud pendiente de aprobación en el sistema.",
        "Los cambios solicitados han sido implementados correctamente.",
        "El periodo de matriculación para el próximo curso académico está abierto hasta el 30 de junio."
    )
    
    val tipos = TipoNotificacion.values()
    
    repeat(8) { index ->
        notificaciones.add(
            Notificacion(
                id = 7L + index,
                titulo = titulos[Random.nextInt(titulos.size)],
                mensaje = mensajes[Random.nextInt(mensajes.size)],
                tipo = tipos[Random.nextInt(tipos.size)],
                fechaHora = LocalDateTime.now().minusDays(Random.nextLong(5)).minusHours(Random.nextLong(12)),
                leida = Random.nextBoolean()
            )
        )
    }
    
    return notificaciones.sortedByDescending { it.fechaHora }
}

/**
 * Formatea una fecha para mostrarla en la interfaz
 */
private fun formatoFecha(fechaHora: LocalDateTime): String {
    val hoy = LocalDateTime.now().toLocalDate()
    val ayer = hoy.minusDays(1)
    
    return when (fechaHora.toLocalDate()) {
        hoy -> "Hoy ${fechaHora.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        ayer -> "Ayer ${fechaHora.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }
}

/**
 * Enumeración para los tipos de notificaciones
 */
enum class TipoNotificacion(val titulo: String, val icono: ImageVector, val color: Color) {
    SISTEMA("Sistema", Icons.Default.Settings, Color(0xFF2196F3)), // Azul
    USUARIO("Usuarios", Icons.Default.Person, Color(0xFFF44336)), // Rojo
    CENTRO("Centros", Icons.Default.Business, Color(0xFF4CAF50))  // Verde
}

/**
 * Enumeración para los filtros de notificaciones
 */
enum class FiltroNotificacion(val titulo: String, val icono: ImageVector) {
    TODAS("Todas", Icons.Default.Notifications),
    NO_LEIDAS("No leídas", Icons.Default.MarkEmailUnread),
    SISTEMA("Sistema", Icons.Default.Settings),
    USUARIOS("Usuarios", Icons.Default.Person),
    CENTROS("Centros", Icons.Default.Business)
}

/**
 * Clase de datos para representar una notificación
 */
data class Notificacion(
    val id: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: TipoNotificacion,
    val fechaHora: LocalDateTime,
    val leida: Boolean
) 