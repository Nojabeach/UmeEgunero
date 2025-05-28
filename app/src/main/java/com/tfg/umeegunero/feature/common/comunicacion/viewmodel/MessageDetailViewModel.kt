package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de detalle de mensaje
 */
data class MessageDetailUiState(
    val isLoading: Boolean = false,
    val message: UnifiedMessage? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

/**
 * ViewModel para la pantalla de detalle de mensaje
 */
@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MessageDetailUiState())
    val uiState: StateFlow<MessageDetailUiState> = _uiState.asStateFlow()
    
    // Obtener el ID del mensaje de los argumentos de navegación
    private val messageId: String = savedStateHandle["messageId"] ?: ""
    
    init {
        if (messageId.isNotEmpty()) {
            loadMessage(messageId)
        }
    }
    
    /**
     * Carga los detalles del mensaje
     */
    fun loadMessage(id: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = messageRepository.getMessageById(id)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                message = result.data,
                                error = null
                            )
                        }
                        
                        // Marcar como leído automáticamente si no es un comunicado que requiere confirmación explícita
                        val message = result.data
                        if (!message.isRead && 
                            (message.type != com.tfg.umeegunero.data.model.MessageType.ANNOUNCEMENT || 
                             message.metadata["requireConfirmation"] != "true")) {
                            
                            markAsRead()
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Error al cargar el mensaje"
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar mensaje: $id")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message ?: "Error desconocido"}"
                    )
                }
            }
        }
    }
    
    /**
     * Marca el mensaje como leído
     */
    fun markAsRead() {
        val currentMessage = _uiState.value.message ?: return
        
        viewModelScope.launch {
            try {
                val result = messageRepository.markAsRead(currentMessage.id)
                
                when (result) {
                    is Result.Success -> {
                        // Actualizar el estado local
                        _uiState.update { state ->
                            state.copy(
                                message = state.message?.copy(
                                    status = com.tfg.umeegunero.data.model.MessageStatus.READ
                                )
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al marcar como leído: ${result.message}") }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensaje como leído: ${currentMessage.id}")
                _uiState.update { 
                    it.copy(error = "Error al marcar como leído: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Elimina el mensaje
     */
    fun deleteMessage() {
        val currentMessage = _uiState.value.message ?: return
        
        viewModelScope.launch {
            try {
                val result = messageRepository.deleteMessage(currentMessage.id)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isDeleted = true) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al eliminar mensaje: ${result.message}") }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar mensaje: ${currentMessage.id}")
                _uiState.update { 
                    it.copy(error = "Error al eliminar mensaje: ${e.message}")
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

    /**
     * Marca el mensaje como leído
     */
    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                val result = messageRepository.markAsRead(messageId)
                if (result is Result.Success && _uiState.value.message != null) {
                    // Actualizar el estado local del mensaje
                    _uiState.update { 
                        it.copy(
                            message = it.message?.copy(
                                status = com.tfg.umeegunero.data.model.MessageStatus.READ
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensaje como leído: $messageId")
            }
        }
    }

    /**
     * Inicia una nueva conversación con el remitente del mensaje
     */
    fun startNewConversation(remitentId: String) {
        viewModelScope.launch {
            try {
                // Implementar la navegación a la pantalla de nuevo mensaje
                // con el remitente preseleccionado
                Timber.d("Iniciando nueva conversación con remitente: $remitentId")
                
                // Esta funcionalidad se completará en la capa de UI
                // ya que requiere navegación
            } catch (e: Exception) {
                Timber.e(e, "Error al iniciar nueva conversación")
                _uiState.update {
                    it.copy(error = "Error al iniciar conversación: ${e.message}")
                }
            }
        }
    }

    /**
     * Abre un archivo adjunto
     */
    fun openAttachment(attachment: Map<String, Any>) {
        viewModelScope.launch {
            try {
                val url = attachment["url"] as? String
                if (url.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(error = "URL de archivo no válida")
                    }
                    return@launch
                }
                
                Timber.d("Abriendo adjunto: $url")
                
                // La apertura real del adjunto se hará desde la capa de UI
                // ya que requiere abrir una aplicación externa o mostrar un visualizador
                
            } catch (e: Exception) {
                Timber.e(e, "Error al abrir adjunto")
                _uiState.update {
                    it.copy(error = "Error al abrir adjunto: ${e.message}")
                }
            }
        }
    }

    /**
     * Formatea el tamaño de un archivo a una cadena legible
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
    
    /**
     * Obtiene el ID del participante para una conversación
     * @param conversationId ID de la conversación
     * @return ID del participante (profesor o familiar según corresponda)
     */
    suspend fun getParticipantId(conversationId: String): String {
        try {
            Timber.d("Obteniendo ID del participante para conversación: $conversationId")
            
            // Si el mensaje actual tiene el participante, usar directamente
            val currentMessage = _uiState.value.message
            if (currentMessage != null) {
                if (currentMessage.senderId.isNotEmpty()) {
                    Timber.d("Usando el senderId como participante: ${currentMessage.senderId}")
                    return currentMessage.senderId
                }
            }
            
            // Intentar obtener el participante a través del repositorio de mensajes
            val result = messageRepository.getConversationParticipants(conversationId)
            if (result is Result.Success && result.data.isNotEmpty()) {
                val participantId = result.data.first()
                Timber.d("Participante encontrado en la conversación: $participantId")
                return participantId
            }
            
            // Si no se pudo obtener, devolver un valor por defecto (vacío)
            Timber.w("No se pudo obtener el participante, devolviendo valor vacío")
            return ""
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener el participante: ${e.message}")
            return ""
        }
    }
} 