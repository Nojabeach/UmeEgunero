package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

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
    val message: UnifiedMessage? = null,
    val originalMessage: UnifiedMessage? = null, // Mensaje original en caso de ser una respuesta
    val isLoading: Boolean = false,
    val error: String? = null,
    val messageDeleted: Boolean = false
)

/**
 * ViewModel para la pantalla de detalle de mensaje
 */
@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MessageDetailUiState())
    val uiState: StateFlow<MessageDetailUiState> = _uiState.asStateFlow()
    
    private var messageId: String = ""
    
    /**
     * Carga un mensaje específico por su ID
     */
    fun loadMessage(id: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        messageId = id
        
        viewModelScope.launch {
            try {
                when (val result = messageRepository.getMessageById(id)) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                message = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                        
                        // Si el mensaje no está leído, marcarlo como leído automáticamente
                        if (result.data.isRead.not()) {
                            markAsRead()
                        }
                        
                        // Si es una respuesta, cargar el mensaje original
                        if (!result.data.replyToId.isNullOrEmpty()) {
                            loadOriginalMessage(result.data.replyToId!!)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Nada que hacer aquí, ya actualizamos isLoading arriba
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar mensaje: $id")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar mensaje: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Carga el mensaje original al que se responde
     */
    private suspend fun loadOriginalMessage(originalMessageId: String) {
        try {
            when (val result = messageRepository.getMessageById(originalMessageId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(originalMessage = result.data)
                    }
                }
                is Result.Error -> {
                    Timber.e("Error al cargar mensaje original: ${result.message}")
                }
                is Result.Loading -> {
                    // No es necesario manejar este estado aquí
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar mensaje original: ${e.message}")
        }
    }
    
    /**
     * Marca el mensaje actual como leído
     */
    fun markAsRead() {
        viewModelScope.launch {
            try {
                val result = messageRepository.markAsRead(messageId)
                
                if (result is Result.Success) {
                    // Actualizar el estado localmente
                    _uiState.update { currentState ->
                        currentState.message?.let { message ->
                            currentState.copy(
                                message = message.copy(
                                    status = com.tfg.umeegunero.data.model.MessageStatus.READ
                                )
                            )
                        } ?: currentState
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensaje como leído: $messageId")
                _uiState.update { 
                    it.copy(error = "Error al marcar mensaje como leído: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Elimina el mensaje actual
     */
    fun deleteMessage() {
        viewModelScope.launch {
            try {
                when (val result = messageRepository.deleteMessage(messageId)) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(messageDeleted = true)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(error = "Error al eliminar mensaje: ${result.message}")
                        }
                    }
                    is Result.Loading -> {
                        // No es necesario manejar este estado aquí
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar mensaje: $messageId")
                _uiState.update { 
                    it.copy(error = "Error al eliminar mensaje: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Limpia cualquier error actual
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
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
} 