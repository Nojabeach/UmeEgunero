package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.TareaRepository
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
 * Estado UI para la pantalla de detalle de tarea
 */
data class DetalleTareaUiState(
    val tarea: Tarea? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para la pantalla de detalle de tarea
 */
@HiltViewModel
class DetalleTareaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetalleTareaUiState())
    val uiState: StateFlow<DetalleTareaUiState> = _uiState.asStateFlow()
    
    /**
     * Carga la información completa de una tarea
     */
    fun cargarTarea(tareaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = tareaRepository.obtenerTarea(tareaId)
                
                when (result) {
                    is Result.Success -> {
                        val tarea = result.data
                        
                        if (tarea != null) {
                            _uiState.update { 
                                it.copy(
                                    tarea = tarea,
                                    isLoading = false
                                ) 
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    error = "No se encontró la tarea",
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar la tarea: ${result.exception?.message ?: "Error desconocido"}",
                                isLoading = false
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada, ya estamos en estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar detalle de tarea")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar detalles: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Marca una tarea como revisada por el familiar
     */
    fun marcarTareaComoRevisada(tareaId: String, comentario: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Utilizamos un ID fijo temporal - en una aplicación real obtendríamos el ID del usuario logueado
                val familiarId = "familiar_temp_id"
                
                val result = tareaRepository.marcarTareaComoRevisadaPorFamiliar(
                    tareaId = tareaId,
                    familiarId = familiarId,
                    comentario = comentario
                )
                
                when (result) {
                    is Result.Success -> {
                        // Actualizar la tarea en el estado
                        val tareaActualizada = _uiState.value.tarea?.copy(
                            revisadaPorFamiliar = true,
                            fechaRevision = Timestamp.now(),
                            comentariosFamiliar = comentario
                        )
                        
                        _uiState.update { 
                            it.copy(
                                tarea = tareaActualizada,
                                mensaje = "Tarea marcada como revisada",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al marcar la tarea como revisada: ${result.exception?.message ?: "Error desconocido"}",
                                isLoading = false
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada, ya estamos en estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar tarea como revisada")
                _uiState.update { 
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Limpia los mensajes de error o éxito
     */
    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }
    
    /**
     * Obtiene el ID del alumno asociado a la tarea actual
     * Si la tarea es específica para un alumno, devuelve ese ID
     * Si no, devuelve el primer alumno del familiar conectado
     */
    fun getAlumnoIdParaTarea(): String {
        val tarea = _uiState.value.tarea
        
        // Si la tarea tiene un alumno específico asignado
        if (tarea != null && tarea.alumnoId.isNotEmpty()) {
            return tarea.alumnoId
        }
        
        // Aquí en una aplicación real, obtendríamos el primer hijo/alumno del familiar
        // Por ahora, usamos un valor temporal
        return "alumno_temp_id"
    }
} 