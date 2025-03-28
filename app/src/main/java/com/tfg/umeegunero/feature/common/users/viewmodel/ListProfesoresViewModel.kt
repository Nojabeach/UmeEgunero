package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Result
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
 * Estado de la UI para la pantalla de listado de profesores
 */
data class ListProfesoresUiState(
    val profesores: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val soloActivos: Boolean = true,
    val profesoresCompletos: List<Usuario> = emptyList() // Lista completa sin filtros
)

/**
 * ViewModel para la gestión de la pantalla de listado de profesores
 */
@HiltViewModel
class ListProfesoresViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListProfesoresUiState())
    val uiState: StateFlow<ListProfesoresUiState> = _uiState.asStateFlow()

    /**
     * Carga la lista de profesores desde el repositorio
     */
    fun cargarProfesores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = usuarioRepository.getUsersByType(TipoUsuario.PROFESOR)) {
                    is Result.Success -> {
                        val profesores = result.data
                        _uiState.update { 
                            it.copy(
                                profesoresCompletos = profesores,
                                profesores = if (it.soloActivos) profesores.filter { profesor -> profesor.activo } else profesores,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Profesores cargados: ${profesores.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar profesores: ${result.exception.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar profesores")
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
                Timber.e(e, "Error inesperado al cargar profesores")
            }
        }
    }

    /**
     * Aplica filtros a la lista de profesores
     * @param soloActivos Si es true, muestra solo profesores activos
     */
    fun aplicarFiltros(soloActivos: Boolean) {
        _uiState.update { currentState ->
            val profesoresToShow = if (soloActivos) {
                currentState.profesoresCompletos.filter { it.activo }
            } else {
                currentState.profesoresCompletos
            }
            
            currentState.copy(
                profesores = profesoresToShow,
                soloActivos = soloActivos
            )
        }
    }

    /**
     * Elimina un profesor por su ID (DNI)
     * @param profesorId ID del profesor a eliminar
     */
    fun eliminarProfesor(profesorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un caso real, aquí llamaríamos al repositorio para eliminar
                // Por ahora, hacemos una eliminación simulada para demostración
                _uiState.update { currentState ->
                    val profesoresActualizados = currentState.profesoresCompletos.filter { it.dni != profesorId }
                    val profesoresFiltrados = if (currentState.soloActivos) {
                        profesoresActualizados.filter { it.activo }
                    } else {
                        profesoresActualizados
                    }
                    
                    currentState.copy(
                        profesores = profesoresFiltrados,
                        profesoresCompletos = profesoresActualizados,
                        isLoading = false
                    )
                }
                
                Timber.d("Profesor eliminado: $profesorId")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar profesor: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error al eliminar profesor $profesorId")
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