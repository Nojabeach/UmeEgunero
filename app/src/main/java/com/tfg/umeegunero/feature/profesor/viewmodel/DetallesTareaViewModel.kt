package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.TareaRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.ResultadoUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de entrega de una tarea
 */
enum class EstadoEntrega {
    PENDIENTE,
    ENTREGADO,
    CALIFICADO,
    RETRASADO
}

/**
 * Modelo de datos para la información de un alumno con su entrega
 */
data class AlumnoEntrega(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val entrega: EntregaTarea?,
    val fotoUrl: String?
) {
    val nombreCompleto: String get() = "$nombre $apellidos"
}

/**
 * Modelo de datos para un alumno con su estado de entrega
 */
data class AlumnoConEstadoEntrega(
    val alumno: Alumno,
    val estadoEntrega: EstadoEntrega,
    val entrega: EntregaTarea?
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
    val alumnosConEstado: List<AlumnoConEstadoEntrega> = emptyList(),
    val totalAlumnos: Int = 0,
    val alumnosEntregados: Int = 0,
    val alumnoSeleccionado: AlumnoEntrega? = null,
    val entregaSeleccionada: EntregaTarea? = null,
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
     * Carga la tarea y sus entregas
     */
    fun cargarDetalleTarea(tareaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val resultado = tareaRepository.obtenerTarea(tareaId)) {
                is Result.Success -> {
                    val tarea = resultado.data
                    if (tarea != null) {
                        _uiState.update { 
                            it.copy(
                                tarea = tarea,
                                isLoading = false
                            )
                        }
                        
                        if (tarea.claseId.isNotEmpty()) {
                            cargarEntregas(tareaId, tarea.claseId)
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
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
                    Timber.e(resultado.exception, "Error al cargar la tarea")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar la tarea: ${resultado.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                else -> {
                    // No hacer nada para Result.Loading
                }
            }
        }
    }
    
    /**
     * Carga las entregas de la tarea
     */
    private fun cargarEntregas(tareaId: String, claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val resultado = tareaRepository.obtenerEntregasPorTarea(tareaId)) {
                is Result.Success -> {
                    val entregas = resultado.data
                    _uiState.update { 
                        it.copy(
                            entregas = entregas,
                            alumnosEntregados = entregas.size
                        )
                    }
                    
                    // Cargar los alumnos de la clase para mostrar quien no ha entregado
                    cargarAlumnos(claseId, entregas)
                }
                is Result.Error -> {
                    Timber.e(resultado.exception, "Error al cargar las entregas")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar las entregas: ${resultado.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                else -> {
                    // No hacer nada para Result.Loading
                }
            }
        }
    }
    
    /**
     * Carga los alumnos de la clase
     */
    private fun cargarAlumnos(claseId: String, entregas: List<EntregaTarea>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Convertimos Resultado a Result para mantener coherencia
            val resultadoAlumnos = alumnoRepository.obtenerAlumnosPorClase(claseId)
            val alumnosResult = ResultadoUtil.convertirResultadoAResult(resultadoAlumnos)
            
            when (alumnosResult) {
                is Result.Success -> {
                    val alumnos = alumnosResult.data
                    val entregasPorAlumno = entregas.associateBy { it.alumnoId }
                    
                    // Lista de AlumnoEntrega para compatibilidad con la UI actual
                    val alumnosEntregas = alumnos.map { alumno ->
                        val entrega = entregasPorAlumno[alumno.id]
                        AlumnoEntrega(
                            id = alumno.id,
                            nombre = alumno.nombre,
                            apellidos = alumno.apellidos ?: "",
                            entrega = entrega,
                            fotoUrl = null
                        )
                    }
                    
                    // Lista con estados para mejor manejo de la UI
                    val alumnosConEstado = alumnos.map { alumno ->
                        val entrega = entregasPorAlumno[alumno.id]
                        val estado = when {
                            entrega == null -> EstadoEntrega.PENDIENTE
                            entrega.calificacion != null -> EstadoEntrega.CALIFICADO
                            entrega.fechaEntrega.toDate().after(_uiState.value.tarea?.fechaEntrega?.toDate()) -> EstadoEntrega.RETRASADO
                            else -> EstadoEntrega.ENTREGADO
                        }
                        
                        AlumnoConEstadoEntrega(
                            alumno = alumno,
                            estadoEntrega = estado,
                            entrega = entrega
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(
                            alumnosEntregas = alumnosEntregas,
                            alumnosConEstado = alumnosConEstado,
                            totalAlumnos = alumnos.size,
                            isLoading = false
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(alumnosResult.exception, "Error al cargar alumnos")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar alumnos: ${alumnosResult.exception?.message}"
                        ) 
                    }
                }
                else -> {
                    // No hacer nada para Result.Loading
                }
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
                // Primero obtenemos el resultado directamente del repositorio que devuelve Result<Boolean>
                val resultado = tareaRepository.calificarEntrega(entregaId, calificacion, feedback)
                
                when (resultado) {
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
                                error = "Error al calificar entrega: ${resultado.exception?.message}"
                            ) 
                        }
                    }
                    else -> {
                        // No hacer nada para Result.Loading
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