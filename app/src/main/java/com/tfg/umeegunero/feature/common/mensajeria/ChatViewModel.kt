package com.tfg.umeegunero.feature.common.mensajeria

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.local.MensajeEntity
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.service.NotificationService
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.FirebaseFunctionsException
import java.util.UUID

/**
 * Estado de la UI para la pantalla de chat
 */
data class ChatUiState(
    val isLoading: Boolean = true,
    val usuario: Usuario? = null,
    val participante: Usuario? = null,
    val participanteId: String = "",
    val alumnoId: String? = null,
    val mensajes: List<UnifiedMessage> = emptyList(),
    val conversacionId: String = "",
    val textoMensaje: String = "",
    val adjuntos: List<Uri> = emptyList(),
    val enviandoMensaje: Boolean = false,
    val error: String? = null,
    val esFamiliar: Boolean = false
)

/**
 * ViewModel para la gestión de la pantalla de chat
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val chatRepository: ChatRepository,
    private val unifiedMessageRepository: UnifiedMessageRepository,
    private val notificacionRepository: NotificacionRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val TAG = "ChatViewModel"
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con los datos necesarios
     */
    fun inicializar(conversacionId: String, participanteId: String, alumnoId: String? = null) {
        _uiState.update { it.copy(
            isLoading = true,
            conversacionId = conversacionId,
            participanteId = participanteId,
            alumnoId = alumnoId
        ) }
        
        viewModelScope.launch {
            try {
                // Cargar usuario actual
                cargarUsuarioActual()
                
                // Cargar participante
                cargarParticipante(participanteId)
                
                // Cargar mensajes
                cargarMensajesUnificados(conversacionId)
                
                // Marcar mensajes como leídos
                if (conversacionId.isNotEmpty()) {
                    marcarMensajesComoLeidos(conversacionId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al cargar la conversación: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Carga el usuario actual
     */
    private suspend fun cargarUsuarioActual() {
        try {
            usuarioRepository.getUsuarioActual().collectLatest<Result<Usuario>> { result ->
                when (result) {
                    is Result.Success<*> -> {
                        val usuario = result.data as Usuario
                        val esFamiliar = usuario.perfiles.any { it.tipo == TipoUsuario.FAMILIAR }
                        
                        _uiState.update { it.copy(
                            usuario = usuario,
                            esFamiliar = esFamiliar
                        ) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al cargar el usuario: ${result.exception?.message}") }
                    }
                    is Result.Loading<*> -> {
                        // Mantener estado de carga
                    }
                    else -> {
                        // No hacer nada para otros casos
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al cargar el usuario: ${e.message}") }
        }
    }

    /**
     * Carga los datos del participante en la conversación
     */
    private suspend fun cargarParticipante(participanteId: String) {
        try {
            val result = usuarioRepository.obtenerUsuarioPorId(participanteId)
            when (result) {
                is Result.Success<*> -> {
                    _uiState.update { it.copy(participante = result.data as Usuario) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = "Error al cargar el participante: ${result.exception?.message}") }
                }
                else -> {}
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al cargar el participante: ${e.message}") }
        }
    }

    /**
     * Carga los mensajes de la conversación desde el repositorio unificado
     */
    private suspend fun cargarMensajesUnificados(conversacionId: String) {
        if (conversacionId.isEmpty()) {
            _uiState.update { it.copy(mensajes = emptyList(), isLoading = false) }
            return
        }
        
        try {
            val result = unifiedMessageRepository.getMessagesFromConversation(conversacionId).collect { result ->
                when (result) {
                    is Result.Success<List<UnifiedMessage>> -> {
                        val messages = result.data
                        _uiState.update { it.copy(
                            mensajes = messages.sortedBy { mensaje -> mensaje.timestamp },
                            isLoading = false
                        ) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al cargar los mensajes: ${result.message}",
                            isLoading = false
                        ) }
                        Timber.e("Error al cargar mensajes unificados: ${result.message}")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                error = "Error al cargar los mensajes: ${e.message}",
                isLoading = false
            ) }
            Timber.e(e, "Error al cargar mensajes")
        }
    }

    /**
     * Marca los mensajes como leídos
     */
    private suspend fun marcarMensajesComoLeidos(conversacionId: String) {
        val currentUser = _uiState.value.usuario ?: return
        
        try {
            // Marcar todos los mensajes no leídos como leídos
            val mensajesNoLeidos = _uiState.value.mensajes.filter { 
                it.status == MessageStatus.UNREAD && it.senderId != currentUser.dni 
            }
            
            mensajesNoLeidos.forEach { mensaje ->
                unifiedMessageRepository.markAsRead(mensaje.id)
            }
            
            // También actualizar en el repositorio local (compatibilidad)
            chatRepository.marcarTodosComoLeidos(conversacionId, currentUser.dni)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al marcar mensajes como leídos: ${e.message}") }
            Timber.e(e, "Error al marcar mensajes como leídos")
        }
    }

    /**
     * Actualiza el texto del mensaje
     */
    fun actualizarTextoMensaje(texto: String) {
        _uiState.update { it.copy(textoMensaje = texto) }
    }

    /**
     * Añade un adjunto al mensaje
     */
    fun añadirAdjunto(uri: Uri) {
        _uiState.update { it.copy(adjuntos = it.adjuntos + uri) }
    }

    /**
     * Elimina un adjunto del mensaje
     */
    fun eliminarAdjunto(uri: Uri) {
        _uiState.update { it.copy(adjuntos = it.adjuntos.filter { adjunto -> adjunto != uri }) }
    }

    /**
     * Envía un mensaje en la conversación actual usando el sistema unificado
     */
    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.conversacionId.isEmpty()) return
        
        // actualizar estado
        _uiState.update { it.copy(textoMensaje = "", enviandoMensaje = true) }
        
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.usuario ?: throw Exception("Usuario no disponible")
                
                // Preparar mensaje unificado
                val unifiedMessage = UnifiedMessage(
                    id = UUID.randomUUID().toString(),
                    title = "",  // Los chats no necesitan título
                    content = text,
                    senderId = currentUser.dni,
                    senderName = currentUser.nombre + " " + currentUser.apellidos,
                    receiverId = _uiState.value.participanteId,
                    receiversIds = emptyList(),  // Es un mensaje directo, no grupal
                    timestamp = Timestamp.now(),
                    type = MessageType.CHAT,
                    priority = MessagePriority.NORMAL,
                    status = MessageStatus.PENDING,
                    conversationId = _uiState.value.conversacionId,
                    metadata = if (_uiState.value.alumnoId != null) 
                        mapOf("alumnoId" to _uiState.value.alumnoId!!) 
                    else 
                        emptyMap(),
                    attachments = emptyList()  // Manejo de adjuntos pendiente
                )
                
                // Enviar mensaje usando el repositorio unificado
                val result = unifiedMessageRepository.sendMessage(unifiedMessage)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar mensajes
                        cargarMensajesUnificados(_uiState.value.conversacionId)
                        
                        // Limpiar adjuntos
                        _uiState.update { it.copy(
                            adjuntos = emptyList(),
                            enviandoMensaje = false
                        ) }
                        
                        // La notificación ahora se envía automáticamente desde la Cloud Function
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al enviar el mensaje: ${result.message}",
                            enviandoMensaje = false
                        ) }
                        Timber.e("Error al enviar mensaje: ${result.message}")
                    }
                    else -> {
                        _uiState.update { it.copy(enviandoMensaje = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al enviar el mensaje: ${e.message}",
                    enviandoMensaje = false
                ) }
                Timber.e(e, "Error al enviar mensaje")
            }
        }
    }

    /**
     * Borra el error actual
     */
    fun borrarError() {
        _uiState.update { it.copy(error = null) }
    }
} 