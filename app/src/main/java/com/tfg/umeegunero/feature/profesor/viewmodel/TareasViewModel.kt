package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.model.Result
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
 * Estado de la UI para la pantalla de tareas
 */
data class TareasUiState(
    val profesorId: String = "",
    val tareas: List<Tarea> = emptyList(),
    val tareasFiltradas: List<Tarea> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val claseSeleccionada: Clase? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val ordenAscendente: Boolean = false,
    val mostrarFiltroClasesDialog: Boolean = false
)

/**
 * ViewModel para la gestión de tareas
 */
@HiltViewModel
class TareasViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TareasUiState())
    val uiState: StateFlow<TareasUiState> = _uiState.asStateFlow()

    init {
        // Obtener el ID del profesor autenticado
        viewModelScope.launch {
            try {
                val usuarioActual = authRepository.getCurrentUser()
                val uid = usuarioActual?.documentId ?: ""
                if (uid.isNotEmpty()) {
                    _uiState.update { it.copy(profesorId = uid) }
                    cargarClasesDelProfesor(uid)
                    cargarTareas(uid)
                } else {
                    _uiState.update { it.copy(error = "No se pudo obtener el ID del profesor") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener usuario actual")
                _uiState.update { it.copy(error = "Error al obtener usuario: ${e.message}") }
            }
        }
    }

    /**
     * Carga las clases asignadas al profesor
     */
    private fun cargarClasesDelProfesor(profesorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val clasesResult = usuarioRepository.getClasesByProfesor(profesorId)

                when (clasesResult) {
                    is Result.Success -> {
                        val clases = clasesResult.data
                        _uiState.update {
                            it.copy(
                                clases = clases,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar clases: ${clasesResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(clasesResult.exception, "Error al cargar clases")
                    }
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al cargar clases: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar clases")
            }
        }
    }

    /**
     * Carga las tareas del profesor
     */
    private fun cargarTareas(profesorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val tareasResult = tareaRepository.obtenerTareasPorProfesor(profesorId)

                when (tareasResult) {
                    is Result.Success -> {
                        val tareas = tareasResult.data
                        val tareasOrdenadas = ordenarTareas(tareas, _uiState.value.ordenAscendente)
                        
                        _uiState.update {
                            it.copy(
                                tareas = tareasOrdenadas,
                                tareasFiltradas = filtrarTareas(tareasOrdenadas, it.claseSeleccionada),
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
                        Timber.e(tareasResult.exception, "Error al cargar tareas")
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
                Timber.e(e, "Error inesperado al cargar tareas")
            }
        }
    }

    /**
     * Crea una nueva tarea
     */
    fun crearTarea(tarea: Tarea) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val profesorId = _uiState.value.profesorId
                if (profesorId.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo identificar al profesor",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                val nuevaTarea = tarea.copy(profesorId = profesorId)
                val resultado = tareaRepository.crearTarea(nuevaTarea)

                when (resultado) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                mensaje = "Tarea creada correctamente",
                                isLoading = false
                            )
                        }
                        // Recargar las tareas
                        cargarTareas(profesorId)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al crear tarea: ${resultado.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al crear tarea")
                    }
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al crear tarea: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al crear tarea")
            }
        }
    }

    /**
     * Ordena las tareas por fecha
     */
    private fun ordenarTareas(tareas: List<Tarea>, ascendente: Boolean): List<Tarea> {
        return if (ascendente) {
            tareas.sortedBy { it.fechaEntrega }
        } else {
            tareas.sortedByDescending { it.fechaEntrega }
        }
    }

    /**
     * Filtra las tareas por clase
     */
    private fun filtrarTareas(tareas: List<Tarea>, clase: Clase?): List<Tarea> {
        return if (clase != null) {
            tareas.filter { it.claseId == clase.id }
        } else {
            tareas
        }
    }

    /**
     * Cambia el orden de las tareas
     */
    fun cambiarOrden() {
        val nuevoOrden = !_uiState.value.ordenAscendente
        val tareasOrdenadas = ordenarTareas(_uiState.value.tareas, nuevoOrden)
        
        _uiState.update { 
            it.copy(
                ordenAscendente = nuevoOrden,
                tareas = tareasOrdenadas,
                tareasFiltradas = filtrarTareas(tareasOrdenadas, it.claseSeleccionada)
            )
        }
    }

    /**
     * Muestra el diálogo para filtrar por clases
     */
    fun mostrarFiltroClases() {
        _uiState.update { it.copy(mostrarFiltroClasesDialog = true) }
    }

    /**
     * Oculta el diálogo para filtrar por clases
     */
    fun ocultarFiltroClases() {
        _uiState.update { it.copy(mostrarFiltroClasesDialog = false) }
    }

    /**
     * Aplica el filtro por clase
     */
    fun aplicarFiltroClase(clase: Clase?) {
        val tareasOrdenadas = ordenarTareas(_uiState.value.tareas, _uiState.value.ordenAscendente)
        val tareasFiltradas = filtrarTareas(tareasOrdenadas, clase)
        
        _uiState.update { 
            it.copy(
                claseSeleccionada = clase,
                tareasFiltradas = tareasFiltradas,
                mostrarFiltroClasesDialog = false
            )
        }
    }

    /**
     * Elimina el filtro actual
     */
    fun clearFiltro() {
        _uiState.update { 
            it.copy(
                claseSeleccionada = null,
                tareasFiltradas = it.tareas
            )
        }
    }

    /**
     * Elimina una tarea
     */
    fun eliminarTarea(tareaId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val resultado = tareaRepository.eliminarTarea(tareaId)

                when (resultado) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                mensaje = "Tarea eliminada correctamente",
                                isLoading = false
                            )
                        }
                        // Recargar las tareas
                        cargarTareas(_uiState.value.profesorId)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al eliminar tarea: ${resultado.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al eliminar tarea")
                    }
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al eliminar tarea: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al eliminar tarea")
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia el mensaje de éxito
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    /**
     * Actualiza una tarea existente
     */
    fun actualizarTarea(tarea: Tarea) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val resultado = tareaRepository.actualizarTarea(tarea)

                when (resultado) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                mensaje = "Tarea actualizada correctamente",
                                isLoading = false
                            )
                        }
                        // Recargar las tareas
                        cargarTareas(_uiState.value.profesorId)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al actualizar tarea: ${resultado.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al actualizar tarea")
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
                Timber.e(e, "Error inesperado al actualizar tarea")
            }
        }
    }
} 