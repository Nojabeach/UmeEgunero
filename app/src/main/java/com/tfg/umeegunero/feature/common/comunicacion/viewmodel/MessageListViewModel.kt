package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.MessageType
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
 * Estado de la UI para la lista de mensajes
 */
data class MessageListUiState(
    val isLoading: Boolean = false,
    val messages: List<UnifiedMessage> = emptyList(),
    val filteredMessages: List<UnifiedMessage> = emptyList(),
    val selectedTabIndex: Int = 0,
    val error: String? = null,
    val searchQuery: String = ""
)

/**
 * ViewModel para la pantalla de lista de mensajes
 */
@HiltViewModel
class MessageListViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MessageListUiState())
    val uiState: StateFlow<MessageListUiState> = _uiState.asStateFlow()
    
    /**
     * Carga todos los mensajes del usuario actual
     */
    fun loadMessages() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            messageRepository.getCurrentUserInbox().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val messages = result.data.sortedByDescending { it.timestamp.seconds }
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                messages = messages,
                                filteredMessages = messages
                            )
                        }
                        applyFilters()
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
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
    
    /**
     * Marca un mensaje como leído
     */
    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                messageRepository.markAsRead(messageId)
                
                // Actualizar localmente el estado de los mensajes
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.map { message ->
                        if (message.id == messageId) {
                            message.copy(status = com.tfg.umeegunero.data.model.MessageStatus.READ)
                        } else {
                            message
                        }
                    }
                    
                    currentState.copy(messages = updatedMessages)
                }
                
                applyFilters()
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensaje como leído: $messageId")
            }
        }
    }
    
    /**
     * Elimina un mensaje
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                val result = messageRepository.deleteMessage(messageId)
                
                if (result is Result.Success) {
                    // Actualizar la lista local de mensajes
                    _uiState.update { currentState ->
                        val updatedMessages = currentState.messages.filter { it.id != messageId }
                        currentState.copy(messages = updatedMessages)
                    }
                    
                    // Reaplicar filtros
                    applyFilters()
                } else if (result is Result.Error) {
                    _uiState.update { 
                        it.copy(error = "Error al eliminar mensaje: ${result.message}")
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
     * Selecciona una pestaña para filtrar los mensajes
     */
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
        applyFilters()
    }
    
    /**
     * Establece una consulta de búsqueda
     */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }
    
    /**
     * Aplica los filtros actuales a la lista de mensajes
     */
    private fun applyFilters() {
        val currentState = _uiState.value
        
        val filtered = currentState.messages.filter { message ->
            // Filtrar por tipo según la pestaña seleccionada
            val matchesType = when (currentState.selectedTabIndex) {
                0 -> true // Todos
                1 -> message.type == MessageType.NOTIFICATION // Notificaciones
                2 -> message.type == MessageType.CHAT // Chats
                3 -> message.type == MessageType.ANNOUNCEMENT // Comunicados
                4 -> message.type == MessageType.INCIDENT // Incidencias
                else -> true
            }
            
            // Filtrar por búsqueda si hay texto de búsqueda
            val matchesSearch = if (currentState.searchQuery.isNotBlank()) {
                message.title.contains(currentState.searchQuery, ignoreCase = true) ||
                message.content.contains(currentState.searchQuery, ignoreCase = true) ||
                message.senderName.contains(currentState.searchQuery, ignoreCase = true)
            } else {
                true
            }
            
            matchesType && matchesSearch
        }
        
        _uiState.update { it.copy(filteredMessages = filtered) }
    }
    
    /**
     * Borra cualquier error mostrado actualmente
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 