package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * Estado de UI para la pantalla de detalles de usuario
 */
data class UserDetailUiState(
    val usuario: Usuario? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false
)

/**
 * ViewModel para la gestión de detalles de usuario
 */
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDetailUiState(isLoading = true))
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    /**
     * Carga los datos del usuario por su DNI
     */
    fun loadUsuario(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.getUsuarioPorDni(dni)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                usuario = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Usuario cargado: ${result.data.nombre} ${result.data.apellidos}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar el usuario"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar el usuario")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar el usuario"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar el usuario")
            }
        }
    }

    /**
     * Elimina un usuario por su DNI
     */
    fun deleteUsuario(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.borrarUsuarioByDni(dni)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                usuario = null,
                                isLoading = false,
                                showDeleteConfirmation = false
                            ) 
                        }
                        Timber.d("Usuario eliminado con DNI: $dni")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al eliminar el usuario",
                                showDeleteConfirmation = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar el usuario")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el usuario",
                        showDeleteConfirmation = false
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar el usuario")
            }
        }
    }

    /**
     * Muestra el diálogo de confirmación para eliminar
     */
    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    /**
     * Oculta el diálogo de confirmación para eliminar
     */
    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 