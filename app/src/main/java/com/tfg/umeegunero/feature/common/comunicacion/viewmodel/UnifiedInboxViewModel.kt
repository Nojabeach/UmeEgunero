package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la bandeja de entrada unificada
 */
data class UnifiedInboxUiState(
    val isLoading: Boolean = false,
    val messages: List<UnifiedMessage> = emptyList(),
    val filteredMessages: List<UnifiedMessage> = emptyList(),
    val selectedFilter: MessageType? = null,
    val error: String? = null,
    val searchQuery: String = ""
)

/**
 * ViewModel para la bandeja de entrada unificada
 */
@HiltViewModel
class UnifiedInboxViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UnifiedInboxUiState())
    val uiState: StateFlow<UnifiedInboxUiState> = _uiState.asStateFlow()
    
    /**
     * Carga todos los mensajes del usuario actual
     */
    fun loadMessages() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            messageRepository.getCurrentUserInbox().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                messages = result.data,
                                error = null
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
    fun markAsRead(messageId: String) {
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
                
                // Reaplicar filtros si hay alguno activo
                applyFilters()
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensaje como leído: $messageId")
                _uiState.update { 
                    it.copy(error = "Error al marcar como leído: ${e.message}")
                }
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
                    
                    // Reaplicar filtros si hay alguno activo
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
     * Filtra los mensajes por tipo
     */
    fun filterByType(type: MessageType?) {
        _uiState.update { it.copy(selectedFilter = type) }
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
            // Filtrar por tipo si hay alguno seleccionado
            val matchesType = currentState.selectedFilter?.let { 
                message.type == it 
            } ?: true
            
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

    /**
     * Configura la verificación periódica de nuevos mensajes
     */
    fun setupPeriodicMessageCheck() {
        viewModelScope.launch {
            while (true) {
                // Verificar nuevos mensajes cada 60 segundos
                delay(60000)
                try {
                    // Usar un flag para evitar mostrar la UI de carga
                    refreshMessagesBackground()
                    Timber.d("Verificación periódica de mensajes completada")
                } catch (e: Exception) {
                    Timber.e(e, "Error al verificar periódicamente los mensajes")
                }
            }
        }
    }

    /**
     * Refresca los mensajes en segundo plano sin mostrar indicadores de carga
     */
    private suspend fun refreshMessagesBackground() {
        try {
            val currentFilter = _uiState.value.selectedFilter
            
            // Adaptar a la API real del repositorio
            messageRepository.getCurrentUserInbox().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val messages = result.data
                        // Aplicar filtro si es necesario
                        val filteredMessages = if (currentFilter != null) {
                            messages.filter { it.type == currentFilter }
                        } else {
                            messages
                        }
                        
                        _uiState.update { it.copy(
                            messages = messages,
                            filteredMessages = filteredMessages,
                            error = null
                        )}
                    }
                    is Result.Error -> {
                        // No actualizar el estado de error en verificaciones de fondo
                        Timber.e(result.exception, "Error en verificación de fondo")
                    }
                    is Result.Loading -> {
                        // No hacemos nada en segundo plano con los estados de carga
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error en refreshMessagesBackground")
        }
    }

    /**
     * Marca todos los mensajes no leídos como leídos
     * Esta función se llama al abrir la bandeja de entrada
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val unreadMessages = _uiState.value.messages.filter { !it.isRead }
                
                if (unreadMessages.isNotEmpty()) {
                    Timber.d("Marcando ${unreadMessages.size} mensajes como leídos")
                    
                    // Llamamos al repositorio para marcar todos como leídos
                    unreadMessages.forEach { message ->
                        messageRepository.markAsRead(message.id)
                    }
                    
                    // Actualizamos el estado local para reflejar los cambios
                    _uiState.update { currentState ->
                        val updatedMessages = currentState.messages.map { message ->
                            if (!message.isRead) {
                                message.copy(status = com.tfg.umeegunero.data.model.MessageStatus.READ)
                            } else {
                                message
                            }
                        }
                        
                        currentState.copy(messages = updatedMessages)
                    }
                    
                    // Reaplicamos los filtros
                    applyFilters()
                } else {
                    Timber.d("No hay mensajes sin leer para marcar")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar todos los mensajes como leídos")
                _uiState.update { 
                    it.copy(error = "Error al marcar mensajes como leídos: ${e.message}")
                }
            }
        }
    }

    init {
        setupPeriodicMessageCheck()
    }
} 