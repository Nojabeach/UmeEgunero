package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.model.TipoUsuario
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
import java.util.Date
import javax.inject.Inject

/**
 * Enumeración para los filtros de tareas disponibles para los familiares
 */
enum class FiltroTarea {
    TODAS, 
    PENDIENTES, 
    EN_PROGRESO, 
    COMPLETADAS, 
    RETRASADAS
}

/**
 * Modelo básico para representar un alumno en la lista de selección
 */
data class AlumnoInfo(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val cursoNombre: String,
    val claseNombre: String
)

/**
 * Estado de la UI para la pantalla de tareas del familiar
 */
data class TareasFamiliaUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val familiarId: String = "",
    val alumnoSeleccionadoId: String = "",
    val alumnos: List<AlumnoInfo> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    val tareasFiltradas: List<Tarea> = emptyList(),
    val filtroSeleccionado: FiltroTarea = FiltroTarea.TODAS
)

/**
 * ViewModel para la gestión de tareas desde la perspectiva del familiar
 */
@HiltViewModel
class TareasFamiliaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val alumnoRepository: AlumnoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TareasFamiliaUiState())
    val uiState: StateFlow<TareasFamiliaUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel cargando los datos del familiar actual
     */
    fun inicializar(familiarId: String) {
        if (familiarId.isEmpty()) {
            _uiState.update { it.copy(error = "No se pudo identificar al familiar") }
            return
        }

        _uiState.update { it.copy(familiarId = familiarId, isLoading = true) }
        cargarAlumnosDelFamiliar(familiarId)
    }

    /**
     * Carga los alumnos asociados al familiar actual
     */
    private fun cargarAlumnosDelFamiliar(familiarId: String) {
        viewModelScope.launch {
            try {
                val usuarioResult = usuarioRepository.obtenerUsuarioPorId(familiarId)
                
                when (usuarioResult) {
                    is Result.Success -> {
                        val usuario = usuarioResult.data
                        
                        // Obtener los perfiles de familiar del usuario
                        val perfilesFamiliar = usuario.perfiles.filter { 
                            it.tipo == TipoUsuario.FAMILIAR 
                        }
                        
                        // Obtener las IDs de los alumnos de todos los perfiles
                        val alumnosIds = perfilesFamiliar.flatMap { it.alumnos }.distinct()
                        
                        if (alumnosIds.isEmpty()) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No hay alumnos asociados a este familiar"
                                )
                            }
                            return@launch
                        }
                        
                        // Cargar información detallada de cada alumno
                        val alumnosInfo = mutableListOf<AlumnoInfo>()
                        
                        for (alumnoId in alumnosIds) {
                            val alumnoResult = alumnoRepository.obtenerAlumnoPorId(alumnoId)
                            
                            if (alumnoResult is Result.Success && alumnoResult.data != null) {
                                val alumno = alumnoResult.data
                                alumnosInfo.add(
                                    AlumnoInfo(
                                        id = alumno.id,
                                        nombre = alumno.nombre,
                                        apellidos = alumno.apellidos,
                                        cursoNombre = alumno.curso ?: "",
                                        claseNombre = alumno.clase ?: ""
                                    )
                                )
                            }
                        }
                        
                        _uiState.update { 
                            it.copy(
                                alumnos = alumnosInfo,
                                alumnoSeleccionadoId = if (alumnosInfo.isNotEmpty()) alumnosInfo[0].id else "",
                                isLoading = false
                            )
                        }
                        
                        // Cargar tareas del primer alumno
                        if (alumnosInfo.isNotEmpty()) {
                            cargarTareasPorAlumno(alumnosInfo[0].id)
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar información del usuario: ${usuarioResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(usuarioResult.exception, "Error al cargar información del usuario")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al cargar alumnos: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumnos")
            }
        }
    }

    /**
     * Carga las tareas de un alumno específico
     */
    fun cargarTareasPorAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    alumnoSeleccionadoId = alumnoId
                ) 
            }

            try {
                val tareasResult = tareaRepository.obtenerTareasPorAlumno(alumnoId)
                
                when (tareasResult) {
                    is Result.Success -> {
                        val tareas = tareasResult.data
                        
                        _uiState.update {
                            it.copy(
                                tareas = tareas,
                                tareasFiltradas = aplicarFiltro(tareas, it.filtroSeleccionado),
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar tareas: ${tareasResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(tareasResult.exception, "Error al cargar tareas del alumno")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al cargar tareas: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar tareas del alumno")
            }
        }
    }

    /**
     * Selecciona un alumno para mostrar sus tareas
     */
    fun seleccionarAlumno(alumnoId: String) {
        if (alumnoId != _uiState.value.alumnoSeleccionadoId) {
            cargarTareasPorAlumno(alumnoId)
        }
    }

    /**
     * Aplica un filtro a las tareas
     */
    fun aplicarFiltro(filtro: FiltroTarea) {
        val tareasFiltradas = aplicarFiltro(_uiState.value.tareas, filtro)
        _uiState.update { 
            it.copy(
                filtroSeleccionado = filtro,
                tareasFiltradas = tareasFiltradas
            )
        }
    }

    /**
     * Aplica un filtro a la lista de tareas
     */
    private fun aplicarFiltro(tareas: List<Tarea>, filtro: FiltroTarea): List<Tarea> {
        val fechaActual = Date()
        
        return when (filtro) {
            FiltroTarea.TODAS -> tareas
            
            FiltroTarea.PENDIENTES -> tareas.filter { 
                it.estado == EstadoTarea.PENDIENTE
            }
            
            FiltroTarea.EN_PROGRESO -> tareas.filter { 
                it.estado == EstadoTarea.EN_PROGRESO
            }
            
            FiltroTarea.COMPLETADAS -> tareas.filter { 
                it.estado == EstadoTarea.COMPLETADA
            }
            
            FiltroTarea.RETRASADAS -> tareas.filter {
                val fechaEntrega = it.fechaEntrega?.toDate()
                fechaEntrega != null && fechaEntrega < fechaActual && 
                it.estado != EstadoTarea.COMPLETADA
            }
        }
    }

    /**
     * Marcar una tarea como completada
     */
    fun marcarTareaComoCompletada(tareaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val resultado = tareaRepository.actualizarEstadoTarea(tareaId, EstadoTarea.COMPLETADA.name)
                
                when (resultado) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                mensaje = "Tarea marcada como completada",
                                isLoading = false
                            )
                        }
                        // Recargar tareas para reflejar el cambio
                        cargarTareasPorAlumno(_uiState.value.alumnoSeleccionadoId)
                    }
                    
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al actualizar tarea: ${resultado.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al marcar tarea como completada")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al actualizar tarea: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al marcar tarea como completada")
            }
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpiar mensaje de éxito
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 