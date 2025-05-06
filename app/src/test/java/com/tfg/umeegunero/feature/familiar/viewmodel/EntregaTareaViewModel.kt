package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.TareaRepository
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para la entrega de tareas
 */
data class EntregaTareaUiState(
    val tarea: Tarea? = null,
    val alumnoId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para la funcionalidad de entrega de tareas
 */
class EntregaTareaViewModel(
    private val tareaRepository: TareaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntregaTareaUiState())
    val uiState: StateFlow<EntregaTareaUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con una tarea específica
     */
    fun inicializar(tareaId: String, alumnoId: String) {
        _uiState.update { it.copy(isLoading = true, alumnoId = alumnoId) }
        
        viewModelScope.launch {
            when (val result = tareaRepository.obtenerTarea(tareaId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            tarea = result.data,
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar la tarea: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                else -> {
                    _uiState.update { 
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }

    /**
     * Envía la entrega de una tarea
     */
    fun enviarEntrega(comentario: String, archivos: List<String>) {
        val currentState = _uiState.value
        val tareaId = currentState.tarea?.id ?: return
        val alumnoId = currentState.alumnoId
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            // Crear el objeto de entrega
            val entrega = EntregaTarea(
                tareaId = tareaId,
                alumnoId = alumnoId,
                comentario = comentario,
                archivos = archivos
            )
            
            // Guardar la entrega
            when (val result = tareaRepository.guardarEntrega(entrega)) {
                is Result.Success -> {
                    // Actualizar el estado de la tarea
                    tareaRepository.actualizarEstadoTarea(tareaId, EstadoTarea.COMPLETADA.name)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            mensaje = "Tarea entregada exitosamente",
                            error = null
                        ) 
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al enviar la entrega: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                else -> {
                    _uiState.update { 
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }

    /**
     * Limpia los mensajes y errores del estado
     */
    fun limpiarMensajes() {
        _uiState.update { 
            it.copy(error = null, mensaje = null)
        }
    }
} 