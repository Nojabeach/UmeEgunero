package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.TareaRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
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
 * Estado de la UI para la pantalla de tareas del profesor.
 */
data class TareasProfesorUiState(
    val isLoading: Boolean = false,
    val tareas: List<Tarea> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para la pantalla de gestión de tareas del profesor.
 *
 * Gestiona la carga, creación, actualización y eliminación de tareas asignadas.
 *
 * @param tareaRepository Repositorio para acceder a los datos de tareas.
 * @param authRepository Repositorio para obtener el ID del profesor.
 * @param claseRepository Repositorio para obtener las clases del profesor.
 */
@HiltViewModel
class TareasProfesorViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val authRepository: AuthRepository,
    private val claseRepository: ClaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TareasProfesorUiState())
    val uiState: StateFlow<TareasProfesorUiState> = _uiState.asStateFlow()

    private var profesorId: String? = null

    init {
        viewModelScope.launch {
            profesorId = authRepository.getCurrentUserId()
            profesorId?.let {
                cargarClases(it)
                cargarTareas(it)
            }
        }
    }

    /**
     * Carga la lista de clases del profesor.
     */
    private fun cargarClases(profesorId: String) {
        viewModelScope.launch {
            when (val result = claseRepository.getClasesByProfesor(profesorId)) { 
                is Result.Success -> _uiState.update { it.copy(clases = result.data) }
                is Result.Error -> {
                     _uiState.update { it.copy(error = "Error al cargar clases: ${result.exception?.localizedMessage}") }
                     Timber.e(result.exception, "Error cargando clases para profesor $profesorId")
                }
                else -> {}
            }
        }
    }

    /**
     * Carga la lista de tareas creadas por el profesor.
     */
    fun cargarTareas(profesorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = tareaRepository.obtenerTareasPorProfesor(profesorId)) { 
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            tareas = result.data.sortedByDescending { t -> t.fechaCreacion }, 
                            error = null
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar tareas para profesor $profesorId")
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar tareas: ${result.exception?.localizedMessage}") }
                }
                else -> { _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }

    /**
     * Crea una nueva tarea.
     */
    fun crearTarea(tarea: Tarea) {
        viewModelScope.launch {
             _uiState.update { it.copy(isLoading = true) } 
             val tareaConProfesor = tarea.copy(profesorId = profesorId ?: "")
             when (val result = tareaRepository.crearTarea(tareaConProfesor)) { 
                 is Result.Success -> {
                     _uiState.update { it.copy(isLoading = false, mensaje = "Tarea creada con éxito") }
                     profesorId?.let { cargarTareas(it) } 
                 }
                 is Result.Error -> {
                     Timber.e(result.exception, "Error al crear tarea")
                     _uiState.update { it.copy(isLoading = false, error = "Error al crear tarea: ${result.exception?.localizedMessage}") }
                 }
                 else -> { _uiState.update { it.copy(isLoading = false) } }
             }
        }
    }

    /**
     * Actualiza una tarea existente.
     */
    fun actualizarTarea(tarea: Tarea) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = tareaRepository.actualizarTarea(tarea)) { 
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, mensaje = "Tarea actualizada con éxito") }
                     profesorId?.let { cargarTareas(it) } 
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al actualizar tarea")
                    _uiState.update { it.copy(isLoading = false, error = "Error al actualizar tarea: ${result.exception?.localizedMessage}") }
                }
                 else -> { _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }

    /**
     * Elimina una tarea.
     */
    fun eliminarTarea(tareaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = tareaRepository.eliminarTarea(tareaId)) { 
                 is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, mensaje = "Tarea eliminada con éxito") }
                     profesorId?.let { cargarTareas(it) } 
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al eliminar tarea")
                    _uiState.update { it.copy(isLoading = false, error = "Error al eliminar tarea: ${result.exception?.localizedMessage}") }
                }
                 else -> { _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }
    
    /**
     * Limpia el mensaje de error de la UI.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Limpia el mensaje de éxito de la UI.
     */
    fun clearMensaje() {
         _uiState.update { it.copy(mensaje = null) }
    }
} 