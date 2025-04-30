package com.tfg.umeegunero.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject
import android.util.Patterns

/**
 * Estado de la UI para la recuperación de contraseña.
 * 
 * @property email Email introducido
 * @property emailError Error de validación
 * @property isLoading Estado de carga
 * @property success Éxito de la operación
 * @property error Mensaje de error
 */
data class RecuperarPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
) {
    /**
     * Indica si se puede enviar la solicitud.
     */
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && emailError == null && !isLoading
}

/**
 * ViewModel para la recuperación de contraseña en UmeEgunero.
 * 
 * Gestiona el proceso de recuperación mediante email, incluyendo validaciones
 * y comunicación con el servicio de autenticación.
 * 
 * ## Validaciones
 * - Formato de email
 * - Campos obligatorios
 * 
 * @property authRepository Repositorio de autenticación
 * @see RecuperarPasswordUiState
 */
@HiltViewModel
class RecuperarPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Estado actual de la UI.
     */
    private val _uiState = MutableStateFlow(RecuperarPasswordUiState())
    val uiState: StateFlow<RecuperarPasswordUiState> = _uiState.asStateFlow()

    /**
     * Actualiza y valida el email en tiempo real.
     * 
     * @param email Nuevo valor del email
     */
    fun updateEmail(email: String) {
        _uiState.update { 
            it.copy(
                email = email,
                emailError = validateEmail(email),
                error = null
            )
        }
    }

    /**
     * Valida el formato del email.
     * 
     * @param email Email a validar
     * @return Mensaje de error o null si es válido
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es obligatorio"
            !Pattern.compile(EMAIL_PATTERN).matcher(email).matches() -> 
                "El formato del correo electrónico no es válido"
            else -> null
        }
    }

    /**
     * Procesa la solicitud de recuperación de contraseña.
     * 
     * Envía un email con instrucciones para resetear la contraseña.
     */
    fun recuperarPassword() {
        val state = _uiState.value
        
        // Validar email nuevamente
        val emailError = validateEmail(state.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                when (val result = authRepository.sendPasswordResetEmail(state.email)) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                success = true
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al enviar el correo de recuperación"
                            )
                        }
                        Timber.e(result.exception, "Error al enviar email de recuperación")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado"
                    )
                }
                Timber.e(e, "Error inesperado al enviar email de recuperación")
            }
        }
    }

    /**
     * Limpia el mensaje de error del estado.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    }
} 