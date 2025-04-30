package com.tfg.umeegunero.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tfg.umeegunero.util.Result

/**
 * ViewModel para la gestión del cambio de contraseña en UmeEgunero.
 * 
 * Implementa el patrón MVVM y gestiona el proceso de cambio de contraseña,
 * incluyendo validaciones y comunicación con el repositorio de usuarios.
 * 
 * ## Validaciones
 * - Coincidencia de contraseñas
 * - Longitud mínima (6 caracteres)
 * - Diferencia con contraseña actual
 * 
 * @property usuarioRepository Repositorio para operaciones de usuario
 * @see CambioContrasenaUiState
 */
@HiltViewModel
class CambioContrasenaViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    /**
     * Estado actual de la UI.
     * 
     * @property isLoading Estado de carga
     * @property isSuccess Éxito de la operación
     * @property error Mensaje de error
     */
    private val _uiState = MutableStateFlow(CambioContrasenaUiState())
    val uiState: StateFlow<CambioContrasenaUiState> = _uiState

    /**
     * Procesa el cambio de contraseña.
     * 
     * @param dni DNI del usuario
     * @param contrasenaActual Contraseña actual
     * @param nuevaContrasena Nueva contraseña
     * @param confirmarContrasena Confirmación de nueva contraseña
     */
    fun cambiarContrasena(dni: String, contrasenaActual: String, nuevaContrasena: String, confirmarContrasena: String) {
        if (nuevaContrasena != confirmarContrasena) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        if (nuevaContrasena.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        if (contrasenaActual == nuevaContrasena) {
            _uiState.update { it.copy(error = "La nueva contraseña debe ser diferente a la actual") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val resultado = usuarioRepository.resetearPassword(dni, nuevaContrasena)) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isSuccess = true, isLoading = false) }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = resultado.exception?.message ?: "Error al cambiar la contraseña",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Error inesperado",
                        isLoading = false
                    )
                }
            }
        }
    }
}

/**
 * Estado de la UI para la pantalla de cambio de contraseña.
 * 
 * @property isLoading Indica si hay operación en curso
 * @property isSuccess Indica si el cambio fue exitoso
 * @property error Mensaje de error si existe
 */
data class CambioContrasenaUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) 