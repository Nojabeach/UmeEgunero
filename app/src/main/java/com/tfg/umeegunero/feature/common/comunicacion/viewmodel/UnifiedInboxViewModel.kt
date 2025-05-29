package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.data.repository.AuthRepository
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
import kotlinx.coroutines.runBlocking

/**
 * Estado de la UI para la bandeja de entrada unificada
 */
data class UnifiedInboxUiState(
    val isLoading: Boolean = true,
    val messages: List<UnifiedMessage> = emptyList(),
    val filteredMessages: List<UnifiedMessage> = emptyList(),
    val selectedFilter: MessageType? = null,
    val error: String? = null,
    val searchQuery: String = "",
    val unreadCount: Int = 0
)

/**
 * ViewModel para la bandeja de entrada unificada
 */
@HiltViewModel
class UnifiedInboxViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UnifiedInboxUiState())
    val uiState: StateFlow<UnifiedInboxUiState> = _uiState.asStateFlow()
    
    /**
     * Obtiene el ID del usuario actualmente autenticado
     * @return ID del usuario actual o cadena vacía si no hay usuario
     */
    fun getCurrentUserId(): String {
        return runBlocking {
            authRepository.getCurrentUserId() ?: ""
        }
    }
    
    /**
     * Carga todos los mensajes del usuario actual
     */
    fun loadMessages() {
        // Solo mostrar loading si no hay mensajes cargados previamente
        if (_uiState.value.messages.isEmpty()) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        }
        
        viewModelScope.launch {
            Timber.d("⚠️ [INBOX] Iniciando carga de mensajes unificados...")
            
            messageRepository.getCurrentUserInbox().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val messages = result.data
                        Timber.d("⚠️ [INBOX] ✅ Cargados ${messages.size} mensajes unificados correctamente")
                        
                        // Contar mensajes no leídos
                        val unreadCount = messages.count { 
                            !it.isRead || it.status == com.tfg.umeegunero.data.model.MessageStatus.UNREAD 
                        }
                        Timber.d("⚠️ [INBOX] 📬 Mensajes sin leer: $unreadCount")
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                messages = messages,
                                unreadCount = unreadCount,
                                error = null
                            )
                        }
                        applyFilters()
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "⚠️ [INBOX] ❌ Error al cargar mensajes: ${result.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Solo actualizar a loading si es la primera carga
                        if (_uiState.value.messages.isEmpty()) {
                            Timber.d("⚠️ [INBOX] 🔄 Cargando mensajes...")
                            _uiState.update { it.copy(isLoading = true) }
                        }
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
                val result = messageRepository.markAsRead(messageId)
                
                if (result is Result.Success) {
                    // Actualizar el mensaje en la lista local para reflejar el estado leído
                    updateMessageReadState(messageId, true)
                    
                    // Verificar si necesitamos actualizar el contador
                    loadMessageCount()
                } else if (result is Result.Error) {
                    _uiState.update { it.copy(error = result.message ?: "Error al marcar como leído") }
                    Timber.e(result.exception, "Error al marcar mensaje como leído: $messageId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al marcar mensaje como leído: $messageId")
                _uiState.update { it.copy(error = "Error: ${e.message}") }
            }
        }
    }
    
    /**
     * Actualiza el estado de lectura de un mensaje en las listas locales
     */
    private fun updateMessageReadState(messageId: String, isRead: Boolean) {
        _uiState.update { currentState ->
            // Actualizar en la lista principal
            val updatedMessages = currentState.messages.map { message ->
                if (message.id == messageId) {
                    // Crear copia del mensaje con todos los campos actualizados relacionados con lectura
                    message.copy(
                        isRead = isRead,
                        status = if (isRead) com.tfg.umeegunero.data.model.MessageStatus.READ else com.tfg.umeegunero.data.model.MessageStatus.UNREAD
                    )
                } else {
                    message
                }
            }
            
            // Actualizar también en la lista filtrada si existe
            val updatedFilteredMessages = if (currentState.filteredMessages.isNotEmpty()) {
                currentState.filteredMessages.map { message ->
                    if (message.id == messageId) {
                        message.copy(
                            isRead = isRead,
                            status = if (isRead) com.tfg.umeegunero.data.model.MessageStatus.READ else com.tfg.umeegunero.data.model.MessageStatus.UNREAD
                        )
                    } else {
                        message
                    }
                }
            } else {
                currentState.filteredMessages
            }
            
            // Actualizar el estado
            currentState.copy(
                messages = updatedMessages,
                filteredMessages = updatedFilteredMessages
            )
        }
    }
    
    /**
     * Elimina un mensaje
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                // Encontrar el mensaje en la lista actual
                val message = _uiState.value.messages.find { it.id == messageId }
                
                if (message == null) {
                    _uiState.update { it.copy(error = "Mensaje no encontrado") }
                    return@launch
                }
                
                // Verificar si el usuario actual es el emisor del mensaje
                val currentUserId = authRepository.getCurrentUserId()
                val isCurrentUserSender = message.senderId == currentUserId
                
                // Verificar si el mensaje ha sido leído
                val isMessageRead = message.isRead || message.status == com.tfg.umeegunero.data.model.MessageStatus.READ
                
                // Solo permitir eliminar si es el emisor y el mensaje no ha sido leído
                if (!isCurrentUserSender) {
                    _uiState.update { it.copy(error = "Solo el emisor puede eliminar este mensaje") }
                    return@launch
                }
                
                if (isMessageRead) {
                    _uiState.update { it.copy(error = "No se puede eliminar un mensaje que ya ha sido leído") }
                    return@launch
                }
                
                // Proceder con la eliminación
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
     * Marca todos los mensajes como leídos
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            Timber.d("🔄 Iniciando proceso para marcar todos los mensajes como leídos")
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.w("⚠️ No se pueden marcar mensajes como leídos: usuario no autenticado")
                    return@launch
                }
                
                // Obtener mensajes no leídos
                val unreadMessages = _uiState.value.messages.filter { 
                    !it.isRead && it.status != com.tfg.umeegunero.data.model.MessageStatus.READ
                }
                
                if (unreadMessages.isEmpty()) {
                    Timber.d("✓ No hay mensajes sin leer para marcar")
                    return@launch
                }
                
                Timber.d("📩 Marcando ${unreadMessages.size} mensajes como leídos para usuario ${currentUser.dni}")
                
                // Marcar cada mensaje como leído en el repositorio
                unreadMessages.forEach { message ->
                    try {
                        Timber.d("🔖 Marcando mensaje ${message.id} como leído")
                        val result = messageRepository.markAsRead(message.id)
                        if (result is Result.Error) {
                            Timber.e(result.exception, "❌ Error al marcar mensaje ${message.id} como leído: ${result.message}")
                        } else if (result is Result.Success) {
                            Timber.d("✅ Mensaje ${message.id} marcado como leído correctamente")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Excepción al marcar mensaje ${message.id} como leído")
                    }
                }
                
                // Actualizar el estado local
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.map { message ->
                        if (!message.isRead && message.status != com.tfg.umeegunero.data.model.MessageStatus.READ) {
                            message.copy(
                                isRead = true, 
                                status = com.tfg.umeegunero.data.model.MessageStatus.READ
                            )
                        } else {
                            message
                        }
                    }
                    
                    val updatedFilteredMessages = if (currentState.filteredMessages.isNotEmpty()) {
                        currentState.filteredMessages.map { message ->
                            if (!message.isRead && message.status != com.tfg.umeegunero.data.model.MessageStatus.READ) {
                                message.copy(
                                    isRead = true, 
                                    status = com.tfg.umeegunero.data.model.MessageStatus.READ
                                )
                            } else {
                                message
                            }
                        }
                    } else {
                        currentState.filteredMessages
                    }
                    
                    currentState.copy(
                        messages = updatedMessages,
                        filteredMessages = updatedFilteredMessages,
                        unreadCount = 0 // Actualizar explícitamente el contador a cero
                    )
                }
                
                // Actualizar contador global
                Timber.d("🔄 Actualizando contador global después de marcar mensajes como leídos")
                loadMessageCount()
                
                // Poner un pequeño delay para asegurar que la BD se actualice
                delay(1000)
                Timber.d("🔄 Verificando contador global nuevamente")
                loadMessageCount() // Actualizar nuevamente para reflejar los cambios en la BD
            } catch (e: Exception) {
                Timber.e(e, "❌ Error general al marcar todos los mensajes como leídos")
            }
        }
    }

    /**
     * Carga el contador de mensajes no leídos
     */
    fun loadMessageCount() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser() ?: return@launch
                val count = messageRepository.getUnreadMessageCount(currentUser.dni)
                
                Timber.d("Contador de mensajes no leídos actualizado: $count")
                
                // Si hay eventos o callbacks registrados para notificar sobre cambios
                // en el contador, este sería el lugar para invocarlos
                
                // Este valor podría usarse para badges u otras indicaciones visuales
                _uiState.update { it.copy(unreadCount = count) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar contador de mensajes no leídos: ${e.message}")
            }
        }
    }

    init {
        setupPeriodicMessageCheck()
    }
} 