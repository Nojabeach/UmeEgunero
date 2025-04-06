package com.tfg.umeegunero.feature.profesor.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.local.entity.ChatMensajeEntity
import com.tfg.umeegunero.data.local.entity.ConversacionEntity
import com.tfg.umeegunero.data.model.AttachmentType
import com.tfg.umeegunero.data.model.InteractionStatus
import com.tfg.umeegunero.data.model.Notificacion
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.local.MensajeEntity
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.feature.profesor.screen.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import timber.log.Timber

/**
 * Modelo de datos para alumno simplificado
 */
data class AlumnoInfo(
    val dni: String,
    val nombreCompleto: String,
    val padre: ContactoInfo? = null,
    val madre: ContactoInfo? = null
)

/**
 * Modelo de datos para un contacto
 */
data class ContactoInfo(
    val dni: String,
    val nombre: String
)

/**
 * Estado UI para la pantalla de chat.
 */
data class ChatProfesorUiState(
    val mensajes: List<ChatMessage> = emptyList(),
    val conversacion: ConversacionEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val familiarNombre: String = "",
    val familiarId: String = "",
    val isTyping: Boolean = false,
    val lastSeen: String = "",
    val notificationEnabled: Boolean = true,
    val usuario: Usuario? = null,
    val alumno: AlumnoInfo? = null,
    val enviandoMensaje: Boolean = false
)

/**
 * ViewModel para la pantalla de chat de profesores.
 * Gestiona la lógica de negocio y el estado de la UI.
 */
@HiltViewModel
class ChatProfesorViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val notificacionRepository: NotificacionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val TAG = "ChatProfesorViewModel"
    
    // Obtener parámetros de navegación
    private val conversacionId: String = savedStateHandle["conversacionId"] ?: ""
    private val familiarId: String = savedStateHandle["familiarId"] ?: ""
    private val familiarNombre: String = savedStateHandle["familiarNombre"] ?: "Familiar"
    private val alumnoId: String? = savedStateHandle["alumnoId"]
    
    // Estado mutable interno
    private val _uiState = MutableStateFlow(
        ChatProfesorUiState(
            familiarId = familiarId,
            familiarNombre = familiarNombre
        )
    )
    
    // Estado público expuesto a la UI
    val uiState: StateFlow<ChatProfesorUiState> = _uiState
    
    init {
        // Cargar usuario actual (profesor)
        cargarUsuarioActual()
        
        // Cargar conversación
        cargarConversacion()
        
        // Cargar alumno si hay alumnoId
        if (alumnoId != null) {
            cargarAlumno(alumnoId)
        }
        
        // Cargar mensajes
        cargarMensajes()
        
        // Marcar mensajes como leídos
        if (conversacionId.isNotEmpty()) {
            marcarMensajesComoLeidos()
        }
    }
    
    /**
     * Carga el usuario actual (profesor)
     */
    private fun cargarUsuarioActual() {
        viewModelScope.launch {
            try {
                usuarioRepository.getUsuarioActual().collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val usuario = result.data
                            _uiState.update { it.copy(usuario = usuario) }
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(
                                error = "Error al cargar usuario: ${result.exception?.message}"
                            )}
                        }
                        else -> { /* No hacer nada */ }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar usuario", e)
            }
        }
    }
    
    /**
     * Carga los datos del alumno.
     */
    private fun cargarAlumno(alumnoId: String) {
        viewModelScope.launch {
            try {
                val result = alumnoRepository.obtenerAlumnoPorId(alumnoId)
                when (result) {
                    is Result.Success -> {
                        // El resultado puede ser un objeto Alumno o un Map, dependiendo de la implementación
                        val alumnoData = result.data ?: return@launch
                        
                        // Extraer datos del alumno independientemente del tipo de retorno
                        var dni = ""
                        var nombre = ""
                        var apellidos = ""
                        var padreId: String? = null
                        var madreId: String? = null
                        
                        // Determinar el tipo de dato y extraer la información
                        // No usar 'is' directamente para evitar problemas de compatibilidad
                        val isMap = alumnoData != null && alumnoData::class.java.name.contains("Map")
                        
                        if (isMap) {
                            // Si es un mapa, extraer los valores como tal
                            val dataMap = alumnoData as? Map<*, *>
                            if (dataMap != null) {
                                dni = (dataMap["dni"] as? String) ?: ""
                                nombre = (dataMap["nombre"] as? String) ?: ""
                                apellidos = (dataMap["apellidos"] as? String) ?: ""
                                padreId = dataMap["padreId"] as? String
                                madreId = dataMap["madreId"] as? String
                            }
                        } else {
                            // Si es un objeto, intentar acceder a las propiedades mediante reflexión
                            try {
                                // Usar reflexión para extraer los datos independientemente del tipo de objeto
                                val dniField = alumnoData.javaClass.getDeclaredField("dni")
                                dniField.isAccessible = true
                                dni = (dniField.get(alumnoData) as? String) ?: ""
                                
                                val nombreField = alumnoData.javaClass.getDeclaredField("nombre")
                                nombreField.isAccessible = true
                                nombre = (nombreField.get(alumnoData) as? String) ?: ""
                                
                                val apellidosField = alumnoData.javaClass.getDeclaredField("apellidos")
                                apellidosField.isAccessible = true
                                apellidos = (apellidosField.get(alumnoData) as? String) ?: ""
                                
                                // Intentar obtener padreId
                                try {
                                    val padreIdField = alumnoData.javaClass.getDeclaredField("padreId")
                                    padreIdField.isAccessible = true
                                    padreId = padreIdField.get(alumnoData) as? String
                                } catch (e: Exception) {
                                    Log.d(TAG, "Campo padreId no encontrado", e)
                                }
                                
                                // Intentar obtener madreId
                                try {
                                    val madreIdField = alumnoData.javaClass.getDeclaredField("madreId")
                                    madreIdField.isAccessible = true
                                    madreId = madreIdField.get(alumnoData) as? String
                                } catch (e: Exception) {
                                    Log.d(TAG, "Campo madreId no encontrado", e)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al acceder a propiedades del alumno", e)
                                // Usar valores por defecto en caso de error
                                dni = ""
                                nombre = "Alumno"
                                apellidos = ""
                            }
                        }
                        
                        // Ahora obtenemos información de contactos si existen
                        var padreInfo: ContactoInfo? = null
                        var madreInfo: ContactoInfo? = null
                        
                        // Cargar datos del padre si existe
                        if (padreId != null) {
                            val padreResult = usuarioRepository.obtenerUsuarioPorId(padreId)
                            if (padreResult is Result.Success && padreResult.data != null) {
                                // Extraer información del padre de forma segura
                                val padreData = padreResult.data
                                var padreNombre = ""
                                var padreApellidos = ""
                                var padreDni = ""
                                
                                val isPadreMap = padreData != null && padreData::class.java.name.contains("Map")
                                if (isPadreMap) {
                                    val padreMap = padreData as? Map<*, *>
                                    if (padreMap != null) {
                                        padreNombre = (padreMap["nombre"] as? String) ?: ""
                                        padreApellidos = (padreMap["apellidos"] as? String) ?: ""
                                        padreDni = (padreMap["dni"] as? String) ?: ""
                                    }
                                } else {
                                    try {
                                        // Usar reflexión para obtener datos
                                        val nombreField = padreData.javaClass.getDeclaredField("nombre")
                                        nombreField.isAccessible = true
                                        padreNombre = (nombreField.get(padreData) as? String) ?: ""
                                        
                                        val apellidosField = padreData.javaClass.getDeclaredField("apellidos")
                                        apellidosField.isAccessible = true
                                        padreApellidos = (apellidosField.get(padreData) as? String) ?: ""
                                        
                                        val dniField = padreData.javaClass.getDeclaredField("dni")
                                        dniField.isAccessible = true
                                        padreDni = (dniField.get(padreData) as? String) ?: ""
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error al acceder a propiedades del padre", e)
                                    }
                                }
                                
                                padreInfo = ContactoInfo(
                                    dni = padreDni,
                                    nombre = "$padreNombre $padreApellidos".trim()
                                )
                            }
                        }
                        
                        // Cargar datos de la madre si existe
                        if (madreId != null) {
                            val madreResult = usuarioRepository.obtenerUsuarioPorId(madreId)
                            if (madreResult is Result.Success && madreResult.data != null) {
                                // Extraer información de la madre de forma segura
                                val madreData = madreResult.data
                                var madreNombre = ""
                                var madreApellidos = ""
                                var madreDni = ""
                                
                                val isMadreMap = madreData != null && madreData::class.java.name.contains("Map")
                                if (isMadreMap) {
                                    val madreMap = madreData as? Map<*, *>
                                    if (madreMap != null) {
                                        madreNombre = (madreMap["nombre"] as? String) ?: ""
                                        madreApellidos = (madreMap["apellidos"] as? String) ?: ""
                                        madreDni = (madreMap["dni"] as? String) ?: ""
                                    }
                                } else {
                                    try {
                                        // Usar reflexión para obtener datos
                                        val nombreField = madreData.javaClass.getDeclaredField("nombre")
                                        nombreField.isAccessible = true
                                        madreNombre = (nombreField.get(madreData) as? String) ?: ""
                                        
                                        val apellidosField = madreData.javaClass.getDeclaredField("apellidos")
                                        apellidosField.isAccessible = true
                                        madreApellidos = (apellidosField.get(madreData) as? String) ?: ""
                                        
                                        val dniField = madreData.javaClass.getDeclaredField("dni")
                                        dniField.isAccessible = true
                                        madreDni = (dniField.get(madreData) as? String) ?: ""
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error al acceder a propiedades de la madre", e)
                                    }
                                }
                                
                                madreInfo = ContactoInfo(
                                    dni = madreDni,
                                    nombre = "$madreNombre $madreApellidos".trim()
                                )
                            }
                        }
                        
                        // Crear el objeto AlumnoInfo con la información obtenida
                        val alumnoInfo = AlumnoInfo(
                            dni = dni,
                            nombreCompleto = "$nombre $apellidos".trim(),
                            padre = padreInfo,
                            madre = madreInfo
                        )
                        
                        // Actualizar el estado
                        _uiState.update { it.copy(alumno = alumnoInfo) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al cargar datos del alumno: ${result.exception?.message}"
                        )}
                    }
                    else -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar alumno", e)
            }
        }
    }
    
    /**
     * Carga los datos de la conversación.
     */
    private fun cargarConversacion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val conversacion = if (conversacionId.isNotEmpty()) {
                    chatRepository.getConversacionById(conversacionId)
                } else {
                    // Obtener ID del profesor
                    val profesorId = _uiState.value.usuario?.dni
                    if (profesorId == null) {
                        _uiState.update { it.copy(
                            error = "No se pudo obtener el ID del profesor",
                            isLoading = false
                        )}
                        return@launch
                    }
                    
                    // Crear nueva conversación si no existe
                    val result = chatRepository.getOrCreateConversacion(
                        usuario1Id = profesorId,
                        usuario2Id = familiarId,
                        nombreUsuario1 = _uiState.value.usuario?.nombre ?: "Profesor",
                        nombreUsuario2 = familiarNombre,
                        alumnoId = alumnoId
                    )
                    
                    when (result) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            _uiState.update { it.copy(
                                error = "Error al crear conversación: ${result.exception?.message}",
                                isLoading = false
                            ) }
                            null
                        }
                        else -> null
                    }
                }
                
                _uiState.update { it.copy(
                    conversacion = conversacion,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar conversación", e)
                _uiState.update { it.copy(
                    error = "Error al cargar conversación: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga los mensajes de la conversación
     */
    private fun cargarMensajes() {
        if (conversacionId.isEmpty()) return
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val mensajesEntities = chatRepository.getMensajesByConversacionId(conversacionId)
                
                val mensajes = mensajesEntities.map { entity ->
                    ChatMessage(
                        id = entity.id,
                        senderId = entity.emisorId,
                        text = entity.texto,
                        timestamp = entity.timestamp,
                        isRead = entity.leido,
                        readTimestamp = entity.fechaLeido,
                        attachmentType = when (entity.tipoAdjunto) {
                            "IMAGE" -> AttachmentType.IMAGE
                            "PDF" -> AttachmentType.PDF
                            "AUDIO" -> AttachmentType.AUDIO
                            "LOCATION" -> AttachmentType.LOCATION
                            else -> null
                        },
                        attachmentUrl = entity.adjuntos.firstOrNull(),
                        interactionStatus = InteractionStatus.NONE
                    )
                }.sortedBy { it.timestamp }
                
                _uiState.update { it.copy(
                    mensajes = mensajes,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar mensajes", e)
                _uiState.update { it.copy(
                    error = "Error al cargar mensajes: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Marca todos los mensajes como leídos.
     */
    private fun marcarMensajesComoLeidos() {
        if (conversacionId.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val usuario = _uiState.value.usuario ?: return@launch
                chatRepository.marcarTodosComoLeidos(conversacionId, usuario.dni)
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar mensajes como leídos", e)
            }
        }
    }
    
    /**
     * Envía un mensaje
     */
    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return
        
        val usuario = _uiState.value.usuario ?: return
        val receptorId = _uiState.value.familiarId
        
        _uiState.update { it.copy(enviandoMensaje = true) }
        
        viewModelScope.launch {
            try {
                // Verificar si ya tenemos una conversación
                val convId = if (conversacionId.isNotEmpty()) {
                    conversacionId
                } else {
                    // Obtener o crear conversación
                    val conversacionResult = chatRepository.obtenerOCrearConversacion(
                        usuarioId = usuario.dni,
                        otroUsuarioId = receptorId,
                        alumnoId = alumnoId
                    )
                    
                    when (conversacionResult) {
                        is Result.Success -> conversacionResult.data
                        else -> {
                            _uiState.update { it.copy(
                                error = "Error al crear conversación",
                                enviandoMensaje = false
                            ) }
                            return@launch
                        }
                    }
                }
                
                // Crear mensaje
                val mensaje = MensajeEntity(
                    id = "", // Se generará automáticamente
                    emisorId = usuario.dni,
                    receptorId = receptorId,
                    timestamp = System.currentTimeMillis(),
                    texto = texto,
                    leido = false,
                    fechaLeido = null,
                    conversacionId = convId,
                    alumnoId = alumnoId,
                    tipoAdjunto = "TEXTO",
                    adjuntos = emptyList()
                )
                
                // Enviar mensaje
                val result = chatRepository.enviarMensaje(mensaje)
                
                when (result) {
                    is Result.Success -> {
                        // Crear notificación para el receptor
                        val alumnoNombre = _uiState.value.alumno?.nombreCompleto ?: "un alumno"
                        val notificacion = Notificacion(
                            titulo = "Mensaje del profesor ${usuario.nombre}",
                            mensaje = "Sobre $alumnoNombre: ${texto.take(50)}${if (texto.length > 50) "..." else ""}",
                            usuarioDestinatarioId = receptorId,
                            tipo = TipoNotificacion.MENSAJE,
                            leida = false,
                            fecha = Timestamp.now(),
                            remitente = usuario.nombre,
                            remitenteId = usuario.dni,
                            accion = "chat/$convId",
                            metadata = mapOf(
                                "conversacionId" to convId,
                                "alumnoId" to (alumnoId ?: "")
                            )
                        )
                        
                        notificacionRepository.crearNotificacion(notificacion)
                        
                        // Recargar mensajes
                        cargarMensajes()
                        
                        // Actualizar estado
                        _uiState.update { it.copy(
                            enviandoMensaje = false,
                            error = null
                        ) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al enviar mensaje: ${result.exception?.message}",
                            enviandoMensaje = false
                        ) }
                    }
                    else -> {
                        _uiState.update { it.copy(enviandoMensaje = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al enviar mensaje: ${e.message}",
                    enviandoMensaje = false
                ) }
                Log.e(TAG, "Error al enviar mensaje", e)
            }
        }
    }
    
    /**
     * Actualiza el estado de interacción de un mensaje.
     */
    fun actualizarEstadoInteraccion(mensajeId: String, estado: InteractionStatus) {
        viewModelScope.launch {
            try {
                chatRepository.actualizarEstadoInteraccion(mensajeId, estado)
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar estado de interacción", e)
            }
        }
    }
    
    /**
     * Simula cambios en el estado de "typing" del familiar.
     */
    fun simularTyping(isTyping: Boolean) {
        _uiState.update { it.copy(isTyping = isTyping) }
    }
    
    /**
     * Actualiza el estado de "última vez visto" del familiar.
     */
    fun actualizarLastSeen(lastSeen: String) {
        _uiState.update { it.copy(lastSeen = lastSeen) }
    }
    
    /**
     * Actualiza la configuración de notificaciones.
     */
    fun actualizarNotificacionesConfig(enabled: Boolean) {
        _uiState.update { it.copy(notificationEnabled = enabled) }
    }
    
    /**
     * Borra el error actual
     */
    fun borrarError() {
        _uiState.update { it.copy(error = null) }
    }
} 