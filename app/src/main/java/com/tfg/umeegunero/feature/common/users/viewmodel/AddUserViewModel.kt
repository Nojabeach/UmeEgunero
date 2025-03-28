package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddUserUiState(
    val dni: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val tipoUsuario: TipoUsuario = TipoUsuario.FAMILIAR,
    val centroId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val dniError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nombreError: String? = null,
    val apellidosError: String? = null,
    val telefonoError: String? = null
) {
    val isFormValid: Boolean
        get() = dni.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                nombre.isNotBlank() &&
                apellidos.isNotBlank() &&
                telefono.isNotBlank() &&
                password == confirmPassword &&
                dniError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                nombreError == null &&
                apellidosError == null &&
                telefonoError == null
}

@HiltViewModel
class AddUserViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUserUiState())
    val uiState: StateFlow<AddUserUiState> = _uiState.asStateFlow()

    fun updateDni(dni: String) {
        val error = when {
            dni.isBlank() -> "DNI es obligatorio"
            !isDniValid(dni) -> "DNI no válido"
            else -> null
        }
        _uiState.update { it.copy(dni = dni, dniError = error) }
    }

    fun updateEmail(email: String) {
        val error = when {
            email.isBlank() -> "Email es obligatorio"
            !isEmailValid(email) -> "Email no válido"
            else -> null
        }
        _uiState.update { it.copy(email = email, emailError = error) }
    }

    fun updatePassword(password: String) {
        val error = when {
            password.isBlank() -> "Contraseña es obligatoria"
            password.length < 6 -> "Contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        _uiState.update {
            it.copy(
                password = password,
                passwordError = error,
                confirmPasswordError = if (password != it.confirmPassword) "Las contraseñas no coinciden" else null
            )
        }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        val error = when {
            confirmPassword.isBlank() -> "Confirmar contraseña es obligatorio"
            confirmPassword != _uiState.value.password -> "Las contraseñas no coinciden"
            else -> null
        }
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = error) }
    }

    fun updateNombre(nombre: String) {
        val error = if (nombre.isBlank()) "Nombre es obligatorio" else null
        _uiState.update { it.copy(nombre = nombre, nombreError = error) }
    }

    fun updateApellidos(apellidos: String) {
        val error = if (apellidos.isBlank()) "Apellidos son obligatorios" else null
        _uiState.update { it.copy(apellidos = apellidos, apellidosError = error) }
    }

    fun updateTelefono(telefono: String) {
        val error = when {
            telefono.isBlank() -> "Teléfono es obligatorio"
            !isTelefonoValid(telefono) -> "Teléfono no válido"
            else -> null
        }
        _uiState.update { it.copy(telefono = telefono, telefonoError = error) }
    }

    fun updateTipoUsuario(tipoUsuario: TipoUsuario) {
        _uiState.update { it.copy(tipoUsuario = tipoUsuario) }
    }

    fun updateCentroSeleccionado(centroId: String) {
        _uiState.update { it.copy(centroId = centroId) }
    }

    fun saveUser() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Implementar la lógica de guardado de usuario
                // Esto dependerá de tu implementación específica de repositorio
                val result = usuarioRepository.guardarUsuario(
                    // Convertir el estado UI a un objeto Usuario
                    // Deberás implementar esta conversión
                    Usuario(
                        dni = _uiState.value.dni,
                        email = _uiState.value.email,
                        nombre = _uiState.value.nombre,
                        apellidos = _uiState.value.apellidos,
                        telefono = _uiState.value.telefono,
                        perfiles = listOf(
                            Perfil(
                                tipo = _uiState.value.tipoUsuario,
                                centroId = _uiState.value.centroId
                            )
                        )
                    )
                )

                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                success = true,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                success = false,
                                error = result.exception.message ?: "Error al guardar usuario"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        error = e.message ?: "Error inesperado"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Métodos de validación
    private fun isDniValid(dni: String): Boolean {
        // Implementar validación de DNI
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        return dniPattern.matches(dni.uppercase())
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailPattern.matches(email)
    }

    private fun isTelefonoValid(telefono: String): Boolean {
        val telefonoPattern = Regex("^(\\+34|0034|34)?[6-9]\\d{8}$")
        return telefonoPattern.matches(telefono.replace("[\\s-]".toRegex(), ""))
    }
}