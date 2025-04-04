package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
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
 * Estado de UI para la pantalla de gestión de alumnos
 */
data class ListAlumnoUiState(
    val alumnos: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la gestión de alumnos
 */
@HiltViewModel
class ListAlumnoViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListAlumnoUiState(isLoading = true))
    val uiState: StateFlow<ListAlumnoUiState> = _uiState.asStateFlow()

    /**
     * Carga todos los alumnos desde el repositorio
     */
    fun loadAlumnos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.getUsersByType(TipoUsuario.ALUMNO)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                alumnos = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Alumnos cargados: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al cargar los alumnos"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar los alumnos")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los alumnos"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar los alumnos")
            }
        }
    }

    /**
     * Elimina un alumno por su DNI
     */
    fun deleteAlumno(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.borrarUsuarioByDni(dni)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista de alumnos después de eliminar
                        loadAlumnos()
                        Timber.d("Alumno eliminado con DNI: $dni")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al eliminar el alumno"
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar el alumno")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el alumno"
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar el alumno")
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