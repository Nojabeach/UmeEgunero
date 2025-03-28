package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.Result
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
    val alumnosCompletos: List<Usuario> = emptyList() // Lista completa sin filtros
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
                when (val result = usuarioRepository.getUsersByType(TipoUsuario.ALUMNO)) {
                    is Result.Success -> {
                        val alumnos = result.data
                        _uiState.update { 
                            it.copy(
                                alumnosCompletos = alumnos,
                                alumnos = if (it.soloActivos) alumnos.filter { alumno -> alumno.activo } else alumnos,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Alumnos cargados: ${alumnos.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar alumnos: ${result.exception.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar alumnos")
                    }
                    is Result.Loading -> {
                        // Este estado lo manejamos al inicio
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
     */
    fun aplicarFiltros(soloActivos: Boolean) {
        _uiState.update { currentState ->
            val alumnosToShow = if (soloActivos) {
                currentState.alumnosCompletos.filter { it.activo }
            } else {
                currentState.alumnosCompletos
            }
            
            currentState.copy(
                alumnos = alumnosToShow,
                soloActivos = soloActivos
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