package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
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
 * Estado de la UI para la pantalla de listado de alumnos
 */
data class ListAlumnosUiState(
    val alumnos: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val soloActivos: Boolean = true,
    val alumnosCompletos: List<Usuario> = emptyList(), // Lista completa sin filtros
    val cursosDisponibles: List<String> = emptyList(),
    val clasesDisponibles: List<String> = emptyList(),
    val cursoSeleccionado: String? = null,
    val claseSeleccionada: String? = null
)

/**
 * ViewModel para la gestión de la pantalla de listado de alumnos
 */
@HiltViewModel
class ListAlumnosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListAlumnosUiState())
    val uiState: StateFlow<ListAlumnosUiState> = _uiState.asStateFlow()

    /**
     * Carga la lista de alumnos desde el repositorio
     */
    fun cargarAlumnos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.getUsersByType(TipoUsuario.ALUMNO)
                
                when (result) {
                    is Result.Success<List<Usuario>> -> {
                        val alumnos = result.data
                        val cursos = alumnos.mapNotNull { (it as? Alumno)?.curso }.distinct().filter { it.isNotBlank() }
                        val clases = alumnos.mapNotNull { (it as? Alumno)?.clase }.distinct().filter { it.isNotBlank() }
                        _uiState.update { 
                            it.copy(
                                alumnosCompletos = alumnos,
                                alumnos = if (it.soloActivos) alumnos.filter { alumno -> alumno.activo } else alumnos,
                                isLoading = false,
                                cursosDisponibles = cursos,
                                clasesDisponibles = clases
                            ) 
                        }
                        Timber.d("Alumnos cargados: ${alumnos.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar alumnos: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar alumnos")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar alumnos")
            }
        }
    }

    /**
     * Aplica filtros a la lista de alumnos
     * @param soloActivos Si es true, muestra solo alumnos activos
     * @param curso Si no es null, filtra por curso
     * @param clase Si no es null, filtra por clase
     */
    fun aplicarFiltros(soloActivos: Boolean, curso: String? = null, clase: String? = null) {
        _uiState.update { currentState ->
            var alumnosToShow = currentState.alumnosCompletos
            if (soloActivos) {
                alumnosToShow = alumnosToShow.filter { it.activo }
            }
            if (!curso.isNullOrBlank()) {
                alumnosToShow = alumnosToShow.filter { (it as? Alumno)?.curso == curso }
            }
            if (!clase.isNullOrBlank()) {
                alumnosToShow = alumnosToShow.filter { (it as? Alumno)?.clase == clase }
            }
            currentState.copy(
                alumnos = alumnosToShow,
                soloActivos = soloActivos,
                cursoSeleccionado = curso,
                claseSeleccionada = clase
            )
        }
    }

    /**
     * Elimina un alumno por su ID (DNI)
     * @param alumnoId ID del alumno a eliminar
     */
    fun eliminarAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un caso real, aquí llamaríamos al repositorio para eliminar
                // Por ahora, hacemos una eliminación simulada para demostración
                _uiState.update { currentState ->
                    val alumnosActualizados = currentState.alumnosCompletos.filter { it.dni != alumnoId }
                    val alumnosFiltrados = if (currentState.soloActivos) {
                        alumnosActualizados.filter { it.activo }
                    } else {
                        alumnosActualizados
                    }
                    
                    currentState.copy(
                        alumnos = alumnosFiltrados,
                        alumnosCompletos = alumnosActualizados,
                        isLoading = false
                    )
                }
                
                Timber.d("Alumno eliminado: $alumnoId")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar alumno: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error al eliminar alumno $alumnoId")
            }
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 