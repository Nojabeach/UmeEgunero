package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.SavedStateHandle
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
 * Estado de la UI para la pantalla de detalle de comunicado
 */
data class ComunicadoDetailUiState(
    val isLoading: Boolean = false,
    val comunicado: UnifiedMessage? = null,
    val error: String? = null,
    val confirmationSent: Boolean = false
)

/**
 * ViewModel para la pantalla de detalle de un comunicado
 */
@HiltViewModel
class ComunicadoDetailViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ComunicadoDetailUiState())
    val uiState: StateFlow<ComunicadoDetailUiState> = _uiState.asStateFlow()
    
    // Obtener el ID del comunicado de los argumentos de navegación
    private val comunicadoId: String = savedStateHandle["comunicadoId"] ?: ""
    
    init {
        if (comunicadoId.isNotEmpty()) {
            loadComunicado(comunicadoId)
        }
    }
    
    /**
     * Carga los detalles del comunicado
     */
    fun loadComunicado(id: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = messageRepository.getMessageById(id)
                
                when (result) {
                    is Result.Success -> {
                        val message = result.data
                        
                        // Verificar que es un comunicado
                        if (message.type == MessageType.ANNOUNCEMENT) {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    comunicado = message,
                                    error = null
                                )
                            }
                            
                            // Marcar como leído automáticamente, a menos que requiera confirmación explícita
                            if (message.metadata["requireConfirmation"] != "true" && !message.isRead) {
                                messageRepository.markAsRead(id)
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "El mensaje no es un comunicado"
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Error al cargar el comunicado"
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar comunicado: $id")
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
     * Confirma la lectura del comunicado
     */
    fun confirmRead() {
        val currentComunicado = _uiState.value.comunicado ?: return
        
        viewModelScope.launch {
            try {
                val result = messageRepository.markAsRead(currentComunicado.id)
                
                when (result) {
                    is Result.Success -> {
                        // Actualizar el estado local
                        _uiState.update { state ->
                            state.copy(
                                comunicado = state.comunicado?.copy(
                                    status = com.tfg.umeegunero.data.model.MessageStatus.READ
                                ),
                                confirmationSent = true
                            )
                        }
                        
                        // Intentar cargar el comunicado nuevamente para obtener el estado actualizado
                        loadComunicado(currentComunicado.id)
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al confirmar lectura: ${result.message}") }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al confirmar lectura del comunicado: ${currentComunicado.id}")
                _uiState.update { 
                    it.copy(error = "Error al confirmar lectura: ${e.message}")
                }
            }
        }
    }
} 