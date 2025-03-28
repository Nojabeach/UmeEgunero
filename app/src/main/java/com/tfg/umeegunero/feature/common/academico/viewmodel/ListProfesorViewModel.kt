package com.tfg.umeegunero.feature.common.academico.viewmodel

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
 * Estado de UI para la pantalla de gestión de profesores
 */
data class ListProfesorUiState(
    val profesores: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la gestión de profesores
 */
@HiltViewModel
class ListProfesorViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListProfesorUiState(isLoading = true))
    val uiState: StateFlow<ListProfesorUiState> = _uiState.asStateFlow()

    /**
     * Carga todos los profesores desde el repositorio
     */
    fun loadProfesores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.getUsersByType(TipoUsuario.PROFESOR)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                profesores = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Profesores cargados: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar los profesores"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar los profesores")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los profesores"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar los profesores")
            }
        }
    }

    /**
     * Elimina un profesor por su DNI
     */
    fun deleteProfesor(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.borrarUsuarioByDni(dni)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista de profesores después de eliminar
                        loadProfesores()
                        Timber.d("Profesor eliminado con DNI: $dni")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al eliminar el profesor"
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar el profesor")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el profesor"
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar el profesor")
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