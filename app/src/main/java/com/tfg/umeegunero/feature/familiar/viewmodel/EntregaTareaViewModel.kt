package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.TareaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de entrega de tarea
 */
data class EntregaTareaUiState(
    val tarea: Tarea? = null,
    val alumnoId: String = "",
    val isLoading: Boolean = false,
    val archivosCargando: Boolean = false,
    val progresoCarga: Float = 0f,
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para manejar la entrega de tareas por parte de un familiar
 */
@HiltViewModel
class EntregaTareaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EntregaTareaUiState())
    val uiState: StateFlow<EntregaTareaUiState> = _uiState.asStateFlow()
    
    /**
     * Inicializa el ViewModel cargando la información de la tarea
     */
    fun inicializar(tareaId: String, alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, alumnoId = alumnoId) }
            
            try {
                val result = tareaRepository.obtenerTarea(tareaId)
                
                if (result is Result.Success) {
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
                } else if (result is Result.Error) {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar la tarea: ${result.exception?.message}",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar información de la tarea")
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
     * Envía la entrega de la tarea
     * @param comentario Comentario opcional del alumno/familiar
     * @param archivos Lista de URIs de los archivos adjuntos
     */
    fun enviarEntrega(comentario: String, archivos: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val tareaId = _uiState.value.tarea?.id ?: ""
                val alumnoId = _uiState.value.alumnoId
                
                if (tareaId.isEmpty() || alumnoId.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo identificar la tarea o el alumno",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // En una aplicación real, aquí cargaríamos los archivos a Firebase Storage
                // y obtendríamos las URLs para guardarlas en la entrega
                
                // Por ahora, simularemos que tenemos las URLs de los archivos
                val urlsArchivos = archivos.mapIndexed { index, uri ->
                    "https://storage.example.com/entregas/$tareaId/${alumnoId}_archivo$index.pdf"
                }
                
                // Crear objeto de entrega
                val entrega = EntregaTarea(
                    tareaId = tareaId,
                    alumnoId = alumnoId,
                    fechaEntrega = Timestamp.now(),
                    archivos = urlsArchivos,
                    comentario = comentario
                )
                
                // Guardar la entrega
                val result = tareaRepository.guardarEntrega(entrega)
                
                // Si la entrega se guarda correctamente, actualizamos el estado de la tarea
                if (result is Result.Success) {
                    // Actualizar estado de la tarea a EN_PROGRESO o COMPLETADA según la política
                    val nuevoEstado = EstadoTarea.COMPLETADA.name
                    tareaRepository.actualizarEstadoTarea(tareaId, nuevoEstado)
                    
                    _uiState.update { 
                        it.copy(
                            mensaje = "Tarea entregada exitosamente",
                            isLoading = false
                        )
                    }
                } else if (result is Result.Error) {
                    _uiState.update { 
                        it.copy(
                            error = "Error al enviar la entrega: ${result.exception?.message}",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al enviar entrega de tarea")
                _uiState.update { 
                    it.copy(
                        error = "Error al enviar entrega: ${e.message}",
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
        _uiState.update { 
            it.copy(
                error = null,
                mensaje = null
            ) 
        }
    }
} 