package com.tfg.umeegunero.feature.admin.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddCentroUiState(
    val id: String = "",
    // Información del centro
    val nombre: String = "",
    val nombreError: String? = null,

    // Dirección
    val calle: String = "",
    val calleError: String? = null,
    val numero: String = "",
    val numeroError: String? = null,
    val codigoPostal: String = "",
    val codigoPostalError: String? = null,
    val ciudad: String = "",
    val ciudadError: String? = null,
    val provincia: String = "",
    val provinciaError: String? = null,

    // Contacto
    val telefono: String = "",
    val telefonoError: String? = null,
    val email: String = "",
    val emailError: String? = null,

    // Contraseñas para crear cuenta de acceso (solo para nuevos centros)
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,

    // Estado de la UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
) {
    val isFormValid: Boolean get() =
        nombre.isNotBlank() && nombreError == null &&
                calle.isNotBlank() && calleError == null &&
                numero.isNotBlank() && numeroError == null &&
                codigoPostal.isNotBlank() && codigoPostalError == null &&
                ciudad.isNotBlank() && ciudadError == null &&
                provincia.isNotBlank() && provinciaError == null &&
                telefono.isNotBlank() && telefonoError == null &&
                email.isNotBlank() && emailError == null &&
                (id.isNotBlank() || (password.isNotBlank() && passwordError == null &&
                        confirmPassword.isNotBlank() && confirmPasswordError == null))
}

@HiltViewModel
class AddCentroViewModel @Inject constructor(
    private val centroRepository: CentroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCentroUiState())
    val uiState: StateFlow<AddCentroUiState> = _uiState.asStateFlow()

    fun updateNombre(nombre: String) {
        val error = if (nombre.isBlank()) "El nombre es obligatorio" else null
        _uiState.update { it.copy(nombre = nombre, nombreError = error) }
    }

    fun updateCalle(calle: String) {
        val error = if (calle.isBlank()) "La calle es obligatoria" else null
        _uiState.update { it.copy(calle = calle, calleError = error) }
    }

    fun updateNumero(numero: String) {
        val error = if (numero.isBlank()) "El número es obligatorio" else null
        _uiState.update { it.copy(numero = numero, numeroError = error) }
    }

    fun updateCodigoPostal(codigoPostal: String) {
        val error = when {
            codigoPostal.isBlank() -> "El código postal es obligatorio"
            !isValidCodigoPostal(codigoPostal) -> "El código postal debe tener 5 dígitos"
            else -> null
        }
        _uiState.update { it.copy(codigoPostal = codigoPostal, codigoPostalError = error) }
    }

    fun updateCiudad(ciudad: String) {
        val error = if (ciudad.isBlank()) "La ciudad es obligatoria" else null
        _uiState.update { it.copy(ciudad = ciudad, ciudadError = error) }
    }

    fun updateProvincia(provincia: String) {
        val error = if (provincia.isBlank()) "La provincia es obligatoria" else null
        _uiState.update { it.copy(provincia = provincia, provinciaError = error) }
    }

    fun updateTelefono(telefono: String) {
        val error = when {
            telefono.isBlank() -> "El teléfono es obligatorio"
            !isValidTelefono(telefono) -> "El formato del teléfono no es válido"
            else -> null
        }
        _uiState.update { it.copy(telefono = telefono, telefonoError = error) }
    }

    fun updateEmail(email: String) {
        val error = when {
            email.isBlank() -> "El email es obligatorio"
            !isValidEmail(email) -> "El formato del email no es válido"
            else -> null
        }
        _uiState.update { it.copy(email = email, emailError = error) }
    }

    fun updatePassword(password: String) {
        val error = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            !isPasswordComplex(password) -> "La contraseña debe incluir letras y números"
            else -> null
        }

        // También validamos confirmPassword si ya existe
        val confirmPasswordError = if (_uiState.value.confirmPassword.isNotBlank() &&
            _uiState.value.confirmPassword != password) {
            "Las contraseñas no coinciden"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                password = password,
                passwordError = error,
                confirmPasswordError = confirmPasswordError
            )
        }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        val error = when {
            confirmPassword.isBlank() -> "Debe confirmar la contraseña"
            confirmPassword != _uiState.value.password -> "Las contraseñas no coinciden"
            else -> null
        }
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = error) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Carga los datos de un centro existente para edición
     */
    fun loadCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = centroRepository.getCentroById(centroId)

                when (result) {
                    is Result.Success -> {
                        val centro = result.data

                        // Actualizar todos los campos del state con los datos del centro
                        _uiState.update { currentState ->
                            currentState.copy(
                                id = centro.id,
                                nombre = centro.nombre,
                                calle = centro.direccion.calle,
                                numero = centro.direccion.numero,
                                codigoPostal = centro.direccion.codigoPostal,
                                ciudad = centro.direccion.ciudad,
                                provincia = centro.direccion.provincia,
                                telefono = centro.contacto.telefono,
                                email = centro.contacto.email,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar el centro: ${result.exception.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // No deberíamos llegar aquí si usamos withContext en el repositorio
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    fun saveCentro() {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val centro = createCentroFromState()

                // Determinamos si es una actualización o un nuevo centro
                val result = if (_uiState.value.id.isBlank()) {
                    centroRepository.addCentro(centro)
                } else {
                    centroRepository.updateCentro(centro).let {
                        if (it is Result.Success) Result.Success(centro.id) else it
                    }
                }

                when (result) {
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
                                error = "Error al guardar el centro: ${result.exception.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // No deberíamos llegar aquí
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        // Validar nombre
        if (currentState.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre es obligatorio") }
            isValid = false
        }

        // Validar dirección
        if (currentState.calle.isBlank()) {
            _uiState.update { it.copy(calleError = "La calle es obligatoria") }
            isValid = false
        }

        if (currentState.numero.isBlank()) {
            _uiState.update { it.copy(numeroError = "El número es obligatorio") }
            isValid = false
        }

        if (currentState.codigoPostal.isBlank()) {
            _uiState.update { it.copy(codigoPostalError = "El código postal es obligatorio") }
            isValid = false
        } else if (!isValidCodigoPostal(currentState.codigoPostal)) {
            _uiState.update { it.copy(codigoPostalError = "El código postal debe tener 5 dígitos") }
            isValid = false
        }

        if (currentState.ciudad.isBlank()) {
            _uiState.update { it.copy(ciudadError = "La ciudad es obligatoria") }
            isValid = false
        }

        if (currentState.provincia.isBlank()) {
            _uiState.update { it.copy(provinciaError = "La provincia es obligatoria") }
            isValid = false
        }

        // Validar contacto
        if (currentState.telefono.isBlank()) {
            _uiState.update { it.copy(telefonoError = "El teléfono es obligatorio") }
            isValid = false
        } else if (!isValidTelefono(currentState.telefono)) {
            _uiState.update { it.copy(telefonoError = "El formato del teléfono no es válido") }
            isValid = false
        }

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(emailError = "El email es obligatorio") }
            isValid = false
        } else if (!isValidEmail(currentState.email)) {
            _uiState.update { it.copy(emailError = "El formato del email no es válido") }
            isValid = false
        }

        // Validar contraseñas solo para centros nuevos
        if (currentState.id.isBlank()) {
            if (currentState.password.isBlank()) {
                _uiState.update { it.copy(passwordError = "La contraseña es obligatoria") }
                isValid = false
            } else if (currentState.password.length < 8) {
                _uiState.update { it.copy(passwordError = "La contraseña debe tener al menos 8 caracteres") }
                isValid = false
            } else if (!isPasswordComplex(currentState.password)) {
                _uiState.update { it.copy(passwordError = "La contraseña debe incluir letras y números") }
                isValid = false
            }

            if (currentState.confirmPassword.isBlank()) {
                _uiState.update { it.copy(confirmPasswordError = "Debe confirmar la contraseña") }
                isValid = false
            } else if (currentState.password != currentState.confirmPassword) {
                _uiState.update { it.copy(confirmPasswordError = "Las contraseñas no coinciden") }
                isValid = false
            }
        }

        return isValid
    }

    private fun createCentroFromState(): Centro {
        val state = _uiState.value

        val direccion = Direccion(
            calle = state.calle,
            numero = state.numero,
            codigoPostal = state.codigoPostal,
            ciudad = state.ciudad,
            provincia = state.provincia
        )

        val contacto = Contacto(
            telefono = state.telefono,
            email = state.email
        )

        return Centro(
            id = state.id.ifBlank { UUID.randomUUID().toString() },
            nombre = state.nombre,
            direccion = direccion,
            contacto = contacto,
            activo = true
        )
    }

    // Funciones de validación
    private fun isValidCodigoPostal(codigoPostal: String): Boolean {
        val cpPattern = Regex("^\\d{5}$")
        return cpPattern.matches(codigoPostal)
    }

    private fun isValidTelefono(telefono: String): Boolean {
        val telefonoPattern = Regex("^(\\+34|0034|34)?[6-9]\\d{8}$")
        return telefonoPattern.matches(telefono.replace("[\\s-]".toRegex(), ""))
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordComplex(password: String): Boolean {
        return password.matches(".*[0-9].*".toRegex()) &&
                password.matches(".*[a-zA-Z].*".toRegex())
    }
}