package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.TareaRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Datos básicos de un alumno para las entregas
 */
data class AlumnoEntrega(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val fotoUrl: String? = null,
    val entrega: EntregaTarea? = null
)

/**
 * Estado de la UI para la pantalla de detalle de tarea
 */
data class DetallesTareaUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val tarea: Tarea? = null,
    val entregas: List<EntregaTarea> = emptyList(),
    val alumnosEntregas: List<AlumnoEntrega> = emptyList(),
    val totalAlumnos: Int = 0,
    val alumnosEntregados: Int = 0,
    val entregaSeleccionada: EntregaTarea? = null,
    val alumnoSeleccionado: AlumnoEntrega? = null,
    val mostrarDialogoCalificacion: Boolean = false
)

/**
 * ViewModel para la pantalla de detalle de tarea
 */
@HiltViewModel
class DetallesTareaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val alumnoRepository: AlumnoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetallesTareaUiState())
    val uiState: StateFlow<DetallesTareaUiState> = _uiState.asStateFlow()
    
    /**
     * Inicializa el ViewModel cargando la tarea y sus entregas
     */
    fun cargarDetalleTarea(tareaId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                // Cargar la tarea
                when (val resultado = tareaRepository.obtenerTarea(tareaId)) {
                    is Result.Success -> {
                        val tarea = resultado.data
                        
                        if (tarea != null) {
                            _uiState.update { it.copy(tarea = tarea) }
                            
                            // Obtener entregas de la tarea
                            cargarEntregas(tareaId, tarea.claseId)
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "La tarea no existe o ha sido eliminada"
                                ) 
                            }
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al cargar la tarea")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar la tarea: ${resultado.exception.message}"
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada, ya estamos mostrando el indicador de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar la tarea")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar la tarea: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Carga las entregas de la tarea y los alumnos asociados
     */
    private suspend fun cargarEntregas(tareaId: String, claseId: String) {
        try {
            // Cargar entregas
            when (val resultadoEntregas = tareaRepository.obtenerEntregasPorTarea(tareaId)) {
                is Result.Success -> {
                    val entregas = resultadoEntregas.data
                    _uiState.update { it.copy(entregas = entregas) }
                    
                    // Cargar alumnos de la clase
                    val resultadoAlumnos = alumnoRepository.obtenerAlumnosPorClase(claseId)
                    when (resultadoAlumnos) {
                        is Result.Success -> {
                            val alumnos = resultadoAlumnos.data
                            
                            // Crear lista de alumnos con sus entregas
                            val alumnosEntregas = alumnos.map { alumno ->
                                val entrega = entregas.find { it.alumnoId == alumno.id }
                                AlumnoEntrega(
                                    id = alumno.dni,
                                    nombre = alumno.nombre,
                                    apellidos = alumno.apellidos,
                                    entrega = entrega,
                                    fotoUrl = null // Por ahora no tenemos fotos de alumnos
                                )
                            }
                            
                            _uiState.update { 
                                it.copy(
                                    alumnosEntregas = alumnosEntregas,
                                    totalAlumnos = alumnos.size,
                                    alumnosEntregados = entregas.size,
                                    isLoading = false
                                ) 
                            }
                        }
                        is Result.Error -> {
                            Timber.e(resultadoAlumnos.exception, "Error al cargar alumnos")
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "Error al cargar alumnos: ${resultadoAlumnos.exception.message}"
                                ) 
                            }
                        }
                        is Result.Loading -> {
                            // No hacer nada, ya estamos mostrando el indicador de carga
                        }
                    }
                }
                is Result.Error -> {
                    Timber.e(resultadoEntregas.exception, "Error al cargar entregas")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar entregas: ${resultadoEntregas.exception.message}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // No hacer nada, ya estamos mostrando el indicador de carga
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar entregas y alumnos")
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "Error al cargar entregas y alumnos: ${e.message}"
                ) 
            }
        }
    }
    
    /**
     * Selecciona una entrega para calificar
     */
    fun seleccionarEntrega(alumnoEntrega: AlumnoEntrega) {
        _uiState.update { 
            it.copy(
                alumnoSeleccionado = alumnoEntrega,
                entregaSeleccionada = alumnoEntrega.entrega,
                mostrarDialogoCalificacion = true
            ) 
        }
    }
    
    /**
     * Oculta el diálogo de calificación
     */
    fun ocultarDialogoCalificacion() {
        _uiState.update { it.copy(mostrarDialogoCalificacion = false) }
    }
    
    /**
     * Califica una entrega
     */
    fun calificarEntrega(entregaId: String, calificacion: Double, feedback: String) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                when (val resultado = tareaRepository.calificarEntrega(entregaId, calificacion, feedback)) {
                    is Result.Success -> {
                        // Recargar los datos
                        _uiState.value.tarea?.let { tarea ->
                            cargarEntregas(tarea.id, tarea.claseId)
                        }
                        
                        _uiState.update { 
                            it.copy(
                                mensaje = "Entrega calificada correctamente",
                                mostrarDialogoCalificacion = false
                            ) 
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al calificar entrega")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al calificar entrega: ${resultado.exception.message}"
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada, ya estamos mostrando el indicador de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al calificar entrega")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al calificar entrega: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 