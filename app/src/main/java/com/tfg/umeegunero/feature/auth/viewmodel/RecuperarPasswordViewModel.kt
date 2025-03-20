package com.tfg.umeegunero.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.Result
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

// TODO: Mejoras pendientes para el ViewModel de recuperación de contraseña
// - Implementar sistema de caché para evitar múltiples peticiones
// - Añadir soporte para diferentes métodos de recuperación (SMS, preguntas)
// - Implementar registro de intentos para limitar peticiones
// - Mejorar la gestión de estados durante el proceso de recuperación
// - Añadir analíticas de uso para detectar patrones sospechosos
// - Implementar validación adicional del dispositivo
// - Añadir soporte para notificaciones push sobre el estado del proceso
// - Desarrollar sistema de seguimiento del proceso de recuperación

/**
 * Estado UI para la pantalla de recuperación de contraseña
 */
data class RecuperarPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && emailError == null && !isLoading
}

/**
 * ViewModel para la pantalla de recuperación de contraseña
 */
@HiltViewModel
class RecuperarPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarPasswordUiState())
    val uiState: StateFlow<RecuperarPasswordUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el email y valida en tiempo real
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
     * Valida el formato del email
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
     * Envía la solicitud de recuperación de contraseña
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
                                error = result.exception.message ?: "Error al enviar el correo de recuperación"
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
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    }
} 