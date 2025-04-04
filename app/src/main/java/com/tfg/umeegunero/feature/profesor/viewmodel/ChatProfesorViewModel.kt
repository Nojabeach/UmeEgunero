package com.tfg.umeegunero.feature.profesor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.local.entity.ChatMensajeEntity
import com.tfg.umeegunero.data.local.entity.ConversacionEntity
import com.tfg.umeegunero.data.model.AttachmentType
import com.tfg.umeegunero.data.model.InteractionStatus
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.feature.profesor.screen.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

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
    val notificationEnabled: Boolean = true
)

/**
 * ViewModel para la pantalla de chat de profesores.
 * Gestiona la lógica de negocio y el estado de la UI.
 */
@HiltViewModel
class ChatProfesorViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val TAG = "ChatProfesorViewModel"
    
    // Obtener parámetros de navegación
    private val conversacionId: String = savedStateHandle["conversacionId"] ?: ""
    private val familiarId: String = savedStateHandle["familiarId"] ?: ""
    private val familiarNombre: String = savedStateHandle["familiarNombre"] ?: "Familiar"
    private val profesorId: String = "profesor456" // En una app real, esto vendría del repositorio de sesión
    
    // Estado mutable interno
    private val _uiState = MutableStateFlow(
        ChatProfesorUiState(
            familiarId = familiarId,
            familiarNombre = familiarNombre
        )
    )
    
    // Estado público expuesto a la UI
    val uiState: StateFlow<ChatProfesorUiState> = _uiState
    
    // Mensajes como Flow
    val mensajes: Flow<List<ChatMessage>> = chatRepository.getMensajesByConversacionId(conversacionId)
        .map { mensajes ->
            mensajes.map { it.toChatMessage() }
                .sortedBy { it.timestamp }
        }
        .catch { e ->
            Log.e(TAG, "Error al cargar mensajes", e)
            _uiState.value = _uiState.value.copy(error = "Error al cargar mensajes: ${e.message}")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // Cargar conversación
        cargarConversacion()
        
        // Marcar mensajes como leídos
        marcarMensajesComoLeidos()
        
        // Observar mensajes y actualizar UI
        viewModelScope.launch {
            mensajes.collect { mensajesList ->
                _uiState.value = _uiState.value.copy(
                    mensajes = mensajesList,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * Carga los datos de la conversación.
     */
    private fun cargarConversacion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val conversacion = if (conversacionId.isNotEmpty()) {
                    chatRepository.getConversacionById(conversacionId)
                } else {
                    // Crear nueva conversación si no existe
                    val result = chatRepository.getOrCreateConversacion(
                        usuario1Id = profesorId,
                        usuario2Id = familiarId,
                        nombreUsuario1 = "Profesor",
                        nombreUsuario2 = familiarNombre
                    )
                    
                    when (result) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                error = "Error al crear conversación: ${result.exception?.message}",
                                isLoading = false
                            )
                            null
                        }
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true
                            )
                            null
                        }
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    conversacion = conversacion,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar conversación", e)
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar conversación: ${e.message}",
                    isLoading = false
                )
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
                chatRepository.marcarTodosComoLeidos(conversacionId, profesorId)
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar mensajes como leídos", e)
            }
        }
    }
    
    /**
     * Envía un nuevo mensaje.
     */
    fun enviarMensaje(
        texto: String,
        tipoAdjunto: AttachmentType? = null,
        urlAdjunto: String? = null
    ) {
        viewModelScope.launch {
            if (texto.isBlank() && urlAdjunto == null) return@launch
            
            try {
                val conversacionActual = _uiState.value.conversacion
                    ?: return@launch
                
                chatRepository.enviarMensaje(
                    conversacionId = conversacionActual.id,
                    emisorId = profesorId,
                    receptorId = familiarId,
                    texto = texto,
                    tipoAdjunto = tipoAdjunto?.name,
                    urlAdjunto = urlAdjunto
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar mensaje", e)
                _uiState.value = _uiState.value.copy(
                    error = "Error al enviar mensaje: ${e.message}"
                )
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
        _uiState.value = _uiState.value.copy(isTyping = isTyping)
    }
    
    /**
     * Actualiza el estado de "última vez visto" del familiar.
     */
    fun actualizarLastSeen(lastSeen: String) {
        _uiState.value = _uiState.value.copy(lastSeen = lastSeen)
    }
    
    /**
     * Actualiza la configuración de notificaciones.
     */
    fun actualizarNotificacionesConfig(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationEnabled = enabled)
    }
    
    /**
     * Extiende ChatMensajeEntity para convertirlo a ChatMessage.
     */
    private fun ChatMensajeEntity.toChatMessage(): ChatMessage {
        val attachmentType = when (this.tipoAdjunto) {
            "IMAGE" -> AttachmentType.IMAGE
            "PDF" -> AttachmentType.PDF
            "AUDIO" -> AttachmentType.AUDIO
            "LOCATION" -> AttachmentType.LOCATION
            else -> null
        }
        
        val interactionStatus = try {
            InteractionStatus.valueOf(this.interaccionEstado)
        } catch (e: Exception) {
            InteractionStatus.NONE
        }
        
        return ChatMessage(
            id = this.id,
            senderId = this.emisorId,
            text = this.texto,
            timestamp = this.timestamp,
            isRead = this.leido,
            readTimestamp = this.fechaLeido,
            attachmentType = attachmentType,
            attachmentUrl = this.urlAdjunto,
            interactionStatus = interactionStatus,
            isTranslated = this.estaTraducido,
            originalText = this.textoOriginal
        )
    }
} 