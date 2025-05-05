package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.feature.common.comunicacion.model.MessagePriority
import com.tfg.umeegunero.feature.common.comunicacion.model.MessageStatus
import com.tfg.umeegunero.feature.common.comunicacion.model.MessageType
import com.tfg.umeegunero.feature.common.comunicacion.model.RecipientItem
import com.tfg.umeegunero.feature.common.comunicacion.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.comunicacion.model.UnifiedMessageRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Estructura para representar a un destinatario seleccionado
 */
data class ReceiverInfo(
    val id: String,
    val name: String
)

/**
 * Estructura para representar resultados de búsqueda de usuarios
 */
data class UserSearchResult(
    val id: String,
    val name: String,
    val userType: String
)

/**
 * Estado de la UI para la pantalla de nuevo mensaje
 */
data class NewMessageUiState(
    val recipientId: String = "",
    val recipients: List<RecipientItem> = emptyList(),
    val searchResults: List<RecipientItem> = emptyList(),
    val subject: String = "",
    val content: String = "",
    val messageType: MessageType = MessageType.CHAT,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val conversationId: String = "",
    val replyingTo: UnifiedMessage? = null,
    val attachments: List<Map<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isSent: Boolean = false,
    val error: String? = null,
    val conversationStarted: Boolean = false,
    val searchQuery: String = "",
    val showTypeMenu: Boolean = false,
    val isReply: Boolean = false,
    val isSending: Boolean = false,
    val messageSent: Boolean = false,
    val titleError: String? = null,
    val contentError: String? = null
) {
    /**
     * Indica si el mensaje puede ser enviado (validación)
     */
    val canSendMessage: Boolean
        get() = subject.isNotBlank() && content.isNotBlank() && 
                (recipients.isNotEmpty() || replyingTo != null)
}

/**
 * ViewModel para la pantalla de creación de nuevo mensaje
 */
@HiltViewModel
class NewMessageViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NewMessageUiState())
    val uiState: StateFlow<NewMessageUiState> = _uiState.asStateFlow()
    
    init {
        // Cargar usuarios para la búsqueda
        searchUsers("")
    }
    
    /**
     * Actualiza el título del mensaje
     */
    fun updateTitle(title: String) {
        _uiState.update { 
            it.copy(
                subject = title,
                titleError = if (title.isBlank()) "El título es obligatorio" else null
            )
        }
    }
    
    /**
     * Actualiza el contenido del mensaje
     */
    fun updateContent(content: String) {
        _uiState.update { 
            it.copy(
                content = content,
                contentError = if (content.isBlank()) "El contenido es obligatorio" else null
            )
        }
    }
    
    /**
     * Establece el tipo de mensaje
     */
    fun setMessageType(type: MessageType) {
        _uiState.update { it.copy(messageType = type) }
    }
    
    /**
     * Establece la prioridad del mensaje
     */
    fun updatePriority(priority: MessagePriority) {
        _uiState.update { it.copy(priority = priority) }
    }
    
    /**
     * Actualiza la consulta de búsqueda y busca usuarios
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchUsers(query)
    }
    
    /**
     * Busca usuarios para agregar como destinatarios
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSearching = true) }
                
                // Validar que tenemos un usuario actual
                val currentUser = authRepository.getCurrentUser() ?: return@launch
                
                // Implementar la búsqueda de usuarios
                val usuariosResult = usuarioRepository.buscarUsuariosPorNombreOCorreo(query, 10)
                
                if (usuariosResult is Result.Success) {
                    val usuariosFiltrados = usuariosResult.data.filter { it.dni != currentUser.dni }
                    _uiState.update { 
                        it.copy(
                            searchResults = usuariosFiltrados.map { usuario ->
                                RecipientItem(
                                    id = usuario.dni,
                                    name = "${usuario.nombre} ${usuario.apellidos}",
                                    email = usuario.email,
                                    avatarUrl = usuario.avatarUrl
                                )
                            },
                            isSearching = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        error = "Error al buscar usuarios"
                    )}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al buscar usuarios")
                _uiState.update { it.copy(
                    isSearching = false,
                    error = "Error al buscar usuarios: ${e.message}"
                )}
            }
        }
    }
    
    /**
     * Añade un destinatario a la lista de seleccionados
     */
    fun addReceiver(id: String, name: String) {
        val currentReceivers = _uiState.value.recipients.toMutableList()
        if (currentReceivers.none { it.id == id }) {
            currentReceivers.add(RecipientItem(id, name, "", ""))
            _uiState.update { it.copy(recipients = currentReceivers) }
        }
    }
    
    /**
     * Elimina un destinatario de la lista de seleccionados
     */
    fun removeReceiver(id: String) {
        val currentReceivers = _uiState.value.recipients.toMutableList()
        currentReceivers.removeAll { it.id == id }
        _uiState.update { it.copy(recipients = currentReceivers) }
    }
    
    /**
     * Carga un mensaje original para responder
     */
    fun loadOriginalMessage(messageId: String) {
        viewModelScope.launch {
            try {
                when (val result = messageRepository.getMessageById(messageId)) {
                    is Result.Success -> {
                        val originalMessage = result.data
                        _uiState.update { 
                            it.copy(
                                replyingTo = originalMessage,
                                isReply = true,
                                subject = if (!originalMessage.title.startsWith("RE: ")) 
                                    "RE: ${originalMessage.title}" else originalMessage.title,
                                recipients = listOf(
                                    RecipientItem(
                                        id = originalMessage.senderId,
                                        name = originalMessage.senderName,
                                        email = "",
                                        avatarUrl = ""
                                    )
                                ),
                                messageType = originalMessage.type
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(error = "Error al cargar mensaje original: ${result.message}")
                        }
                    }
                    is Result.Loading -> {
                        // No necesitamos manejar este estado aquí
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar mensaje original")
                _uiState.update { 
                    it.copy(error = "Error al cargar mensaje original: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Establece un ID de destinatario preseleccionado
     */
    fun setReceiverId(receiverId: String) {
        viewModelScope.launch {
            try {
                val usuarioResult = usuarioRepository.getUsuarioById(receiverId)
                if (usuarioResult is Result.Success) {
                    val usuario = usuarioResult.data
                    addReceiver(usuario.dni, "${usuario.nombre} ${usuario.apellidos}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener destinatario: $receiverId")
            }
        }
    }
    
    /**
     * Alterna la visibilidad del menú de tipos de mensaje
     */
    fun toggleTypeMenu() {
        _uiState.update { it.copy(showTypeMenu = !it.showTypeMenu) }
    }
    
    /**
     * Limpia la información de respuesta
     */
    fun clearReplyTo() {
        _uiState.update { 
            it.copy(
                replyingTo = null,
                isReply = false
            )
        }
    }
    
    /**
     * Establece el mensaje al que se responde
     */
    fun setReplyTo(message: UnifiedMessage) {
        _uiState.update { currentState ->
            currentState.copy(
                replyingTo = message,
                subject = if (!message.title.startsWith("RE: ")) "RE: ${message.title}" else message.title,
                recipients = listOf(
                    RecipientItem(
                        id = message.senderId,
                        name = message.senderName,
                        email = "",
                        avatarUrl = null
                    )
                ),
                messageType = message.type,
                conversationId = message.conversationId
            )
        }
    }
    
    /**
     * Envía el mensaje
     */
    fun sendMessage() {
        val currentState = _uiState.value
        
        // Validar campos obligatorios
        if (currentState.subject.isBlank() || currentState.content.isBlank()) {
            _uiState.update {
                it.copy(
                    titleError = if (it.subject.isBlank()) "El título es obligatorio" else null,
                    contentError = if (it.content.isBlank()) "El contenido es obligatorio" else null
                )
            }
            return
        }
        
        // Validar destinatarios si no es una respuesta
        if (currentState.replyingTo == null && currentState.recipients.isEmpty()) {
            _uiState.update {
                it.copy(error = "Debe seleccionar al menos un destinatario")
            }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null, isSending = true) }
        
        viewModelScope.launch {
            try {
                // Obtener el usuario actual
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isSending = false,
                            error = "Error: Usuario no autenticado"
                        )
                    }
                    return@launch
                }
                
                // Crear el mensaje
                val message = UnifiedMessage(
                    id = UUID.randomUUID().toString(),
                    title = currentState.subject,
                    content = currentState.content,
                    type = currentState.messageType,
                    priority = currentState.priority,
                    senderId = currentUser.dni,
                    senderName = "${currentUser.nombre} ${currentUser.apellidos}".trim(),
                    conversationId = currentState.conversationId,
                    replyToId = currentState.replyingTo?.id ?: "",
                    // Si es una respuesta o hay un solo destinatario, usar receiverId
                    receiverId = if (currentState.replyingTo != null) 
                                    currentState.replyingTo.senderId 
                                 else 
                                    if (currentState.recipients.size == 1) 
                                        currentState.recipients.first().id 
                                    else "",
                    // Si hay múltiples destinatarios, usar una lista
                    receiversIds = if (currentState.recipients.size > 1) 
                                    currentState.recipients.map { it.id } 
                                   else emptyList(),
                    timestamp = Timestamp.now(),
                    status = MessageStatus.UNREAD
                )
                
                // Enviar el mensaje mediante el repositorio
                val result = messageRepository.sendMessage(message)
                
                when (result) {
                    is Result.Success -> {
                        // Mensaje enviado correctamente
                        Timber.d("Mensaje enviado correctamente con ID: ${result.data}")
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isSending = false,
                                messageSent = true,
                                subject = "",
                                content = "",
                                recipients = emptyList(),
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        // Error al enviar el mensaje
                        Timber.e(result.exception, "Error al enviar mensaje: ${result.message}")
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isSending = false,
                                error = "Error al enviar mensaje: ${result.message}"
                            )
                        }
                    }
                    else -> {
                        Timber.d("Resultado no manejado: $result")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al enviar mensaje")
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isSending = false,
                        error = "Error al enviar mensaje: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 