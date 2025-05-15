package com.tfg.umeegunero.feature.familiar.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CornerSize
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

/**
 * Estado UI para la pantalla de notificaciones del familiar
 */
data class NotificacionesFamiliaUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notificaciones: List<Notificacion> = emptyList(),
    val notificacionesFiltradas: List<Notificacion> = emptyList(),
    val filtroSeleccionado: FiltroNotificacion = FiltroNotificacion.TODAS,
    val modoSeleccion: Boolean = false,
    val notificacionesSeleccionadas: Set<String> = emptySet()
)

/**
 * ViewModel para la pantalla de notificaciones del familiar
 */
@HiltViewModel
class NotificacionesFamiliaViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificacionesFamiliaUiState())
    val uiState: StateFlow<NotificacionesFamiliaUiState> = _uiState.asStateFlow()
    
    init {
        cargarNotificaciones()
    }
    
    /**
     * Carga las notificaciones del usuario desde Firestore
     */
    fun cargarNotificaciones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener la información del usuario",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Simulamos obtener mensajes
                // En una implementación real, usaríamos el repositorio
                val mensajes = listOf(
                    UnifiedMessage(
                        id = "1",
                        title = "Recordatorio de reunión",
                        content = "Le recordamos la reunión de padres mañana a las 17:00h",
                        type = MessageType.ANNOUNCEMENT,
                        timestamp = Timestamp.now(),
                        senderName = "Centro Escolar",
                        status = MessageStatus.UNREAD
                    ),
                    UnifiedMessage(
                        id = "2",
                        title = "Nota: Evaluación trimestral",
                        content = "Se han publicado las calificaciones del trimestre",
                        type = MessageType.ANNOUNCEMENT,
                        timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000)),
                        senderName = "Profesora de Matemáticas",
                        status = MessageStatus.READ
                    ),
                    UnifiedMessage(
                        id = "3",
                        title = "Material necesario",
                        content = "Para la actividad de mañana, recordad traer ropa cómoda",
                        type = MessageType.DAILY_RECORD,
                        timestamp = Timestamp(Date(System.currentTimeMillis() - 172800000)),
                        senderName = "Profesora de Educación Física",
                        status = MessageStatus.UNREAD
                    )
                )
                
                // Convertir mensajes a notificaciones
                val notificaciones = mensajes.map { mensaje ->
                    Notificacion(
                        id = mensaje.id,
                        titulo = mensaje.title,
                        mensaje = mensaje.content,
                        fecha = mensaje.timestamp.toDate(),
                        tipo = when (mensaje.type) {
                            MessageType.CHAT -> TipoNotificacion.MENSAJE
                            MessageType.ANNOUNCEMENT -> TipoNotificacion.GENERAL
                            MessageType.DAILY_RECORD -> TipoNotificacion.TAREA
                            MessageType.ATTENDANCE -> TipoNotificacion.ASISTENCIA
                            else -> TipoNotificacion.GENERAL
                        },
                        leida = mensaje.status != MessageStatus.UNREAD,
                        remitente = mensaje.senderName
                    )
                }.sortedByDescending { it.fecha }
                
                _uiState.update { it.copy(
                    notificaciones = notificaciones,
                    notificacionesFiltradas = notificaciones,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar notificaciones")
                _uiState.update { it.copy(
                    error = "Error: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Cambia el filtro de notificaciones
     */
    fun cambiarFiltro(filtro: FiltroNotificacion) {
        _uiState.update { currentState ->
            val notificacionesFiltradas = when (filtro) {
                FiltroNotificacion.TODAS -> currentState.notificaciones
                FiltroNotificacion.NO_LEIDAS -> currentState.notificaciones.filter { !it.leida }
                FiltroNotificacion.MENSAJES -> currentState.notificaciones.filter { it.tipo == TipoNotificacion.MENSAJE }
                FiltroNotificacion.TAREAS -> currentState.notificaciones.filter { it.tipo == TipoNotificacion.TAREA }
                FiltroNotificacion.EVENTOS -> currentState.notificaciones.filter { it.tipo == TipoNotificacion.EVENTO }
                FiltroNotificacion.OTROS -> currentState.notificaciones.filter { 
                    it.tipo != TipoNotificacion.MENSAJE && 
                    it.tipo != TipoNotificacion.TAREA && 
                    it.tipo != TipoNotificacion.EVENTO 
                }
            }
            
            currentState.copy(
                filtroSeleccionado = filtro,
                notificacionesFiltradas = notificacionesFiltradas
            )
        }
    }
    
    /**
     * Marca una notificación como leída
     */
    fun marcarComoLeida(id: String) {
        viewModelScope.launch {
            try {
                // En una implementación real, actualizar en Firestore
                // messageRepository.markMessageAsRead(id)
                
                // Actualizar en la UI
                _uiState.update { state ->
                    val notificacionesActualizadas = state.notificaciones.map { 
                        if (it.id == id) it.copy(leida = true) else it 
                    }
                    
                    // Aplicar filtro actual a las notificaciones actualizadas
                    val filtradas = when (state.filtroSeleccionado) {
                        FiltroNotificacion.TODAS -> notificacionesActualizadas
                        FiltroNotificacion.NO_LEIDAS -> notificacionesActualizadas.filter { !it.leida }
                        FiltroNotificacion.MENSAJES -> notificacionesActualizadas.filter { it.tipo == TipoNotificacion.MENSAJE }
                        FiltroNotificacion.TAREAS -> notificacionesActualizadas.filter { it.tipo == TipoNotificacion.TAREA }
                        FiltroNotificacion.EVENTOS -> notificacionesActualizadas.filter { it.tipo == TipoNotificacion.EVENTO }
                        FiltroNotificacion.OTROS -> notificacionesActualizadas.filter { 
                            it.tipo != TipoNotificacion.MENSAJE && 
                            it.tipo != TipoNotificacion.TAREA && 
                            it.tipo != TipoNotificacion.EVENTO 
                        }
                    }
                    
                    state.copy(
                        notificaciones = notificacionesActualizadas,
                        notificacionesFiltradas = filtradas
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar notificación como leída")
            }
        }
    }
    
    /**
     * Cambia el modo de selección
     */
    fun cambiarModoSeleccion(activar: Boolean) {
        _uiState.update { it.copy(
            modoSeleccion = activar,
            notificacionesSeleccionadas = if (!activar) emptySet() else it.notificacionesSeleccionadas
        ) }
    }
    
    /**
     * Alternar selección de una notificación
     */
    fun toggleSeleccion(id: String) {
        _uiState.update { state ->
            val seleccionadas = state.notificacionesSeleccionadas.toMutableSet()
            if (seleccionadas.contains(id)) {
                seleccionadas.remove(id)
            } else {
                seleccionadas.add(id)
            }
            state.copy(notificacionesSeleccionadas = seleccionadas)
        }
    }
    
    /**
     * Eliminar notificaciones seleccionadas
     */
    fun eliminarSeleccionadas() {
        viewModelScope.launch {
            val idsAEliminar = _uiState.value.notificacionesSeleccionadas
            
            if (idsAEliminar.isEmpty()) return@launch
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Eliminar en Firestore (llamadas en paralelo)
                idsAEliminar.forEach { id ->
                    messageRepository.deleteMessage(id)
                }
                
                // Actualizar la UI
                _uiState.update { state ->
                    val notificacionesActualizadas = state.notificaciones.filter { 
                        !idsAEliminar.contains(it.id) 
                    }
                    
                    // Aplicar filtro actual a las notificaciones actualizadas
                    val filtradas = when (state.filtroSeleccionado) {
                        FiltroNotificacion.TODAS -> notificacionesActualizadas
                        FiltroNotificacion.NO_LEIDAS -> notificacionesActualizadas.filter { !it.leida }
                        FiltroNotificacion.MENSAJES -> notificacionesActualizadas.filter { it.tipo == TipoNotificacion.MENSAJE }
                        FiltroNotificacion.TAREAS -> notificacionesActualizadas.filter { it.tipo == TipoNotificacion.TAREA }
                        FiltroNotificacion.EVENTOS -> notificacionesActualizadas.filter { it.tipo == TipoNotificacion.EVENTO }
                        FiltroNotificacion.OTROS -> notificacionesActualizadas.filter { 
                            it.tipo != TipoNotificacion.MENSAJE && 
                            it.tipo != TipoNotificacion.TAREA && 
                            it.tipo != TipoNotificacion.EVENTO 
                        }
                    }
                    
                    state.copy(
                        notificaciones = notificacionesActualizadas,
                        notificacionesFiltradas = filtradas,
                        notificacionesSeleccionadas = emptySet(),
                        modoSeleccion = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar notificaciones")
                _uiState.update { it.copy(
                    error = "Error al eliminar notificaciones: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificacionesFamiliaScreen(
    navController: NavController,
    viewModel: NotificacionesFamiliaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado para mostrar diálogo de confirmación de eliminación
    var mostrarDialogoConfirmacion by rememberSaveable { mutableStateOf(false) }
    
    // SnackbarHostState para mostrar mensajes de error
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Efecto para mostrar errores en el Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Mostrar diálogo de confirmación para eliminar notificaciones
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Eliminar notificaciones") },
            text = { Text("¿Estás seguro de eliminar ${uiState.notificacionesSeleccionadas.size} notificaciones?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarSeleccionadas()
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
                    if (uiState.modoSeleccion) {
                        // Indicador de cantidad seleccionada
                        Text(
                            text = "${uiState.notificacionesSeleccionadas.size} seleccionadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Botón para eliminar
                        IconButton(
                            onClick = { 
                                if (uiState.notificacionesSeleccionadas.isNotEmpty()) {
                                    mostrarDialogoConfirmacion = true
                                }
                            },
                            enabled = uiState.notificacionesSeleccionadas.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar seleccionados",
                                tint = if (uiState.notificacionesSeleccionadas.isEmpty()) 
                                    Color.White.copy(alpha = 0.5f) else Color.White
                            )
                        }
                        
                        // Botón para cancelar selección
                        IconButton(onClick = { viewModel.cambiarModoSeleccion(false) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar selección"
                            )
                        }
                    } else {
                        // Botón para activar modo selección
                        IconButton(onClick = { viewModel.cambiarModoSeleccion(true) }) {
                            Icon(
                                imageVector = Icons.Default.CheckBox,
                                contentDescription = "Seleccionar"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Filtros
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                FiltroNotificacion.values().forEach { filtro ->
                    SegmentedButton(
                        selected = uiState.filtroSeleccionado == filtro,
                        onClick = { viewModel.cambiarFiltro(filtro) },
                        shape = when (filtro) {
                            FiltroNotificacion.TODAS -> MaterialTheme.shapes.small 
                                .copy(topStart = CornerSize(8.dp), bottomStart = CornerSize(8.dp))
                            FiltroNotificacion.OTROS -> MaterialTheme.shapes.small
                                .copy(topEnd = CornerSize(8.dp), bottomEnd = CornerSize(8.dp))
                            else -> MaterialTheme.shapes.small
                        }
                    ) {
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
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FamiliarColor)
            }
        } else if (uiState.notificacionesFiltradas.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = FamiliarColor.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No hay notificaciones",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Cuando recibas notificaciones, aparecerán aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = uiState.notificacionesFiltradas,
                    key = { it.id }
                ) { notificacion ->
                    val isSelected = uiState.notificacionesSeleccionadas.contains(notificacion.id)
                    
                    NotificacionItem(
                        notificacion = notificacion,
                        isSelected = isSelected,
                        isSelectionMode = uiState.modoSeleccion,
                        onClick = {
                            if (uiState.modoSeleccion) {
                                viewModel.toggleSeleccion(notificacion.id)
                            } else {
                                // Marcar como leída y navegar si es necesario
                                if (!notificacion.leida) {
                                    viewModel.marcarComoLeida(notificacion.id)
                                }
                                
                                // Navegación según tipo
                                notificacion.accion?.let { accion ->
                                    // Implementar navegación según la acción
                                }
                            }
                        },
                        onLongClick = {
                            // Si no estamos en modo selección, activarlo y seleccionar este item
                            if (!uiState.modoSeleccion) {
                                viewModel.cambiarModoSeleccion(true)
                                viewModel.toggleSeleccion(notificacion.id)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else if (!notificacion.leida) 
                MaterialTheme.colorScheme.surface
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notificacion.leida) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de tipo de notificación
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(notificacion.tipo.color)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Cabecera: título y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificacion.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Fecha formateada
                    Text(
                        text = formatDate(notificacion.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Remitente (si existe)
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
            
            // Indicador de selección
            if (isSelectionMode) {
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = if (isSelected) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Seleccionado" else "No seleccionado",
                    tint = if (isSelected) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// Formato de fecha para notificaciones
fun formatDate(date: Date): String {
    val now = Calendar.getInstance()
    val notificationDate = Calendar.getInstance().apply { time = date }
    
    return when {
        // Hoy: mostrar solo la hora
        now.get(Calendar.YEAR) == notificationDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == notificationDate.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // Ayer: mostrar "Ayer" y la hora
        now.get(Calendar.YEAR) == notificationDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) - notificationDate.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "Ayer ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
        }
        // Este año: mostrar día y mes
        now.get(Calendar.YEAR) == notificationDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("d MMM", Locale("es")).format(date)
        }
        // Otros años: mostrar día, mes y año
        else -> {
            SimpleDateFormat("d MMM yyyy", Locale("es")).format(date)
        }
    }
} 