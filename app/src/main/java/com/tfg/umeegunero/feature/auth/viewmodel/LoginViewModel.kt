package com.tfg.umeegunero.feature.auth.viewmodel

import android.content.SharedPreferences
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.UserType
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
 * Estado UI para la pantalla de login
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val userType: UserType? = null
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() &&
                emailError == null && passwordError == null
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Comprobar si hay credenciales guardadas
        val savedEmail = sharedPreferences.getString(PREF_SAVED_EMAIL, "")
        if (!savedEmail.isNullOrEmpty()) {
            updateEmail(savedEmail)
        }
    }

    /**
     * Actualiza el email y valida su formato
     */
    fun updateEmail(email: String) {
        val emailError = if (email.isNotBlank() && !isValidEmail(email)) {
            "Email inválido"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                email = email,
                emailError = emailError
            )
        }
    }

    /**
     * Actualiza la contraseña y valida su longitud
     */
    fun updatePassword(password: String) {
        val passwordError = if (password.isNotBlank() && password.length < 6) {
            "La contraseña debe tener al menos 6 caracteres"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                password = password,
                passwordError = passwordError
            )
        }
    }

    /**
     * Valida el formato del email
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Realiza el inicio de sesión
     */
    fun login(userType: UserType, rememberUser: Boolean = false) {
        val email = _uiState.value.email
        val password = _uiState.value.password

        // Validar campos antes de enviar
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

        if (email.isBlank()) {
            emailError = "El email es obligatorio"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = "Email inválido"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (!isValid) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        // Iniciar proceso de login
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                Timber.d("Iniciando proceso de login para email: $email")
                val result = usuarioRepository.iniciarSesion(email, password)

                when (result) {
                    is Result.Success -> {
                        val usuarioId = result.data
                        Timber.d("Login exitoso en Firebase Auth, obteniendo datos de usuario...")

                        // Obtener datos completos del usuario
                        val usuarioResult = usuarioRepository.getUsuarioPorDni(usuarioId)
                        if (usuarioResult is Result.Success) {
                            val usuario = usuarioResult.data
                            Timber.d("Datos de usuario obtenidos: ${usuario.email}")

                            // Verificar que el usuario tiene el tipo adecuado de perfil
                            val tipoUsuarioFirebase = when (userType) {
                                UserType.ADMIN_APP -> com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_APP
                                UserType.ADMIN_CENTRO -> com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_CENTRO
                                UserType.PROFESOR -> com.tfg.umeegunero.data.model.TipoUsuario.PROFESOR
                                UserType.FAMILIAR -> com.tfg.umeegunero.data.model.TipoUsuario.FAMILIAR
                            }

                            val perfiles = usuario.perfiles ?: emptyList()
                            Timber.d("Perfiles del usuario: ${perfiles.map { it.tipo }}")
                            
                            val tienePerfil = perfiles.any { it.tipo == tipoUsuarioFirebase }

                            if (tienePerfil) {
                                // Si el usuario seleccionó recordar usuario, guardamos el email
                                if (rememberUser) {
                                    saveUserCredentials(email)
                                } else {
                                    // Si no quiere recordar, borramos credenciales guardadas
                                    clearSavedCredentials()
                                }
                                
                                // Login exitoso
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        success = true,
                                        userType = userType
                                    )
                                }
                                Timber.d("Login exitoso para $email como $userType")
                            } else {
                                // No tiene el perfil adecuado
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "No tienes permisos para acceder como $userType"
                                    )
                                }
                                // Cerrar sesión ya que no tiene los permisos correctos
                                usuarioRepository.cerrarSesion()
                                Timber.d("Usuario sin perfil $userType: $email")
                            }
                        } else if (usuarioResult is Result.Error) {
                            Timber.e(usuarioResult.exception, "Error al obtener datos de usuario")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Error al obtener datos de usuario: ${usuarioResult.exception.message}"
                                )
                            }
                            // Cerrar sesión ya que no pudimos obtener los datos
                            usuarioRepository.cerrarSesion()
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error en el login")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al iniciar sesión: ${result.exception.message}"
                            )
                        }
                    }
                    else -> {
                        Timber.e("Estado inesperado en el login")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error inesperado al iniciar sesión"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error general en el proceso de login")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Guarda las credenciales del usuario para recordarlas
     */
    private fun saveUserCredentials(email: String) {
        sharedPreferences.edit()
            .putString(PREF_SAVED_EMAIL, email)
            .apply()
        Timber.d("Credenciales guardadas para: $email")
    }

    /**
     * Limpia las credenciales guardadas
     */
    private fun clearSavedCredentials() {
        sharedPreferences.edit()
            .remove(PREF_SAVED_EMAIL)
            .apply()
        Timber.d("Credenciales eliminadas")
    }

    /**
     * Limpia los errores
     */
    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    /**
     * Reinicia el estado
     */
    fun resetState() {
        _uiState.update {
            LoginUiState()
        }
    }
    
    companion object {
        private const val PREF_SAVED_EMAIL = "savedEmail"
    }
}