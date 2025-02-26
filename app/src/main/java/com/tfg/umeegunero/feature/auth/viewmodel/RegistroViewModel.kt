package com.tfg.umeegunero.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.RegistroUsuarioForm
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI para la pantalla de registro
 */
data class RegistroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val form: RegistroUsuarioForm = RegistroUsuarioForm(),
    val currentStep: Int = 1, // 1: Datos personales, 2: Dirección, 3: Datos de alumnos y centro
    val totalSteps: Int = 3,
    val centros: List<Centro> = emptyList(),
    val isLoadingCentros: Boolean = false,
    val formErrors: Map<String, String> = emptyMap()
)

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    init {
        cargarCentros()
    }

    private fun cargarCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCentros = true) }

            when (val result = usuarioRepository.getCentrosEducativos()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            centros = result.data,
                            isLoadingCentros = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = "Error al cargar centros: ${result.exception.message}",
                            isLoadingCentros = false
                        )
                    }
                }
                else -> { /* Ignorar estado Loading */ }
            }
        }
    }

    /**
     * Actualiza el formulario de registro
     */
    fun updateFormField(field: String, value: String) {
        val currentForm = _uiState.value.form
        val formErrors = _uiState.value.formErrors.toMutableMap()

        // Limpiar error si existe
        formErrors.remove(field)

        val updatedForm = when (field) {
            "dni" -> currentForm.copy(dni = value)
            "email" -> currentForm.copy(email = value)
            "password" -> currentForm.copy(password = value)
            "confirmPassword" -> currentForm.copy(confirmPassword = value)
            "nombre" -> currentForm.copy(nombre = value)
            "apellidos" -> currentForm.copy(apellidos = value)
            "telefono" -> currentForm.copy(telefono = value)
            "calle" -> {
                val newDireccion = currentForm.direccion.copy(calle = value)
                currentForm.copy(direccion = newDireccion)
            }
            "numero" -> {
                val newDireccion = currentForm.direccion.copy(numero = value)
                currentForm.copy(direccion = newDireccion)
            }
            "piso" -> {
                val newDireccion = currentForm.direccion.copy(piso = value)
                currentForm.copy(direccion = newDireccion)
            }
            "codigoPostal" -> {
                val newDireccion = currentForm.direccion.copy(codigoPostal = value)
                currentForm.copy(direccion = newDireccion)
            }
            "ciudad" -> {
                val newDireccion = currentForm.direccion.copy(ciudad = value)
                currentForm.copy(direccion = newDireccion)
            }
            "provincia" -> {
                val newDireccion = currentForm.direccion.copy(provincia = value)
                currentForm.copy(direccion = newDireccion)
            }
            "centroId" -> currentForm.copy(centroId = value)
            else -> currentForm
        }

        _uiState.update { it.copy(form = updatedForm, formErrors = formErrors) }
    }

    /**
     * Actualiza el tipo de familiar
     */
    fun updateSubtipoFamiliar(subtipo: SubtipoFamiliar) {
        _uiState.update {
            it.copy(
                form = it.form.copy(subtipo = subtipo)
            )
        }
    }

    /**
     * Añade un DNI de alumno al formulario
     */
    fun addAlumnoDni(dni: String) {
        if (dni.isNotBlank() && !_uiState.value.form.alumnosDni.contains(dni)) {
            val currentAlumnos = _uiState.value.form.alumnosDni.toMutableList()
            currentAlumnos.add(dni)

            _uiState.update {
                it.copy(
                    form = it.form.copy(alumnosDni = currentAlumnos)
                )
            }
        }
    }

    /**
     * Elimina un DNI de alumno del formulario
     */
    fun removeAlumnoDni(dni: String) {
        val currentAlumnos = _uiState.value.form.alumnosDni.toMutableList()
        currentAlumnos.remove(dni)

        _uiState.update {
            it.copy(
                form = it.form.copy(alumnosDni = currentAlumnos)
            )
        }
    }

    /**
     * Avanza al siguiente paso del formulario
     */
    fun nextStep() {
        // Validar el paso actual antes de avanzar
        if (validateCurrentStep()) {
            _uiState.update {
                it.copy(
                    currentStep = it.currentStep + 1,
                    error = null
                )
            }
        }
    }

    /**
     * Retrocede al paso anterior del formulario
     */
    fun previousStep() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update {
                it.copy(
                    currentStep = it.currentStep - 1,
                    error = null
                )
            }
        }
    }

    /**
     * Valida el paso actual del formulario
     */
    private fun validateCurrentStep(): Boolean {
        val currentStep = _uiState.value.currentStep
        val form = _uiState.value.form
        val errors = mutableMapOf<String, String>()

        when (currentStep) {
            1 -> {
                // Validación de datos personales
                if (form.dni.isBlank()) errors["dni"] = "El DNI es obligatorio"
                else if (!isDniValid(form.dni)) errors["dni"] = "El formato del DNI no es válido"

                if (form.email.isBlank()) errors["email"] = "El email es obligatorio"
                else if (!isEmailValid(form.email)) errors["email"] = "El formato del email no es válido"

                if (form.password.isBlank()) errors["password"] = "La contraseña es obligatoria"
                else if (form.password.length < 6) errors["password"] = "La contraseña debe tener al menos 6 caracteres"

                if (form.confirmPassword != form.password) errors["confirmPassword"] = "Las contraseñas no coinciden"

                if (form.nombre.isBlank()) errors["nombre"] = "El nombre es obligatorio"
                if (form.apellidos.isBlank()) errors["apellidos"] = "Los apellidos son obligatorios"
                if (form.telefono.isBlank()) errors["telefono"] = "El teléfono es obligatorio"
                else if (!isTelefonoValid(form.telefono)) errors["telefono"] = "El formato del teléfono no es válido"
            }
            2 -> {
                // Validación de dirección
                if (form.direccion.calle.isBlank()) errors["calle"] = "La calle es obligatoria"
                if (form.direccion.numero.isBlank()) errors["numero"] = "El número es obligatorio"
                if (form.direccion.codigoPostal.isBlank()) errors["codigoPostal"] = "El código postal es obligatorio"
                else if (!isCodigoPostalValid(form.direccion.codigoPostal)) errors["codigoPostal"] = "El formato del código postal no es válido"

                if (form.direccion.ciudad.isBlank()) errors["ciudad"] = "La ciudad es obligatoria"
                if (form.direccion.provincia.isBlank()) errors["provincia"] = "La provincia es obligatoria"
            }
            3 -> {
                // Validación de alumnos y centro
                if (form.alumnosDni.isEmpty()) errors["alumnos"] = "Debe indicar al menos un alumno"

                // Validar que cada DNI de alumno tenga formato correcto
                form.alumnosDni.forEachIndexed { index, dni ->
                    if (!isDniValid(dni)) errors["alumno_$index"] = "El DNI del alumno ${index + 1} no tiene un formato válido"
                }

                if (form.centroId.isBlank()) errors["centro"] = "Debe seleccionar un centro educativo"
            }
        }

        _uiState.update { it.copy(formErrors = errors) }
        return errors.isEmpty()
    }

    /**
     * Validaciones de formato
     */
    private fun isDniValid(dni: String): Boolean {
        // DNI español: 8 números y 1 letra (puede tener espacios o guiones que ignoramos)
        val dniPattern = Regex("^\\d{8}[A-Z]$")
        return dniPattern.matches(dni.replace("[\\s-]".toRegex(), "").uppercase())
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailPattern.matches(email)
    }

    private fun isTelefonoValid(telefono: String): Boolean {
        // Formato español: +34 seguido de 9 dígitos (puede tener espacios o guiones que ignoramos)
        val telefonoPattern = Regex("^(\\+34|0034|34)?[6789]\\d{8}$")
        return telefonoPattern.matches(telefono.replace("[\\s-]".toRegex(), ""))
    }

    private fun isCodigoPostalValid(codigoPostal: String): Boolean {
        // Código postal español: 5 dígitos
        val cpPattern = Regex("^\\d{5}$")
        return cpPattern.matches(codigoPostal)
    }

    /**
     * Envía el formulario de registro
     */
    fun submitRegistration() {
        // Validar todos los pasos
        for (step in 1.._uiState.value.totalSteps) {
            _uiState.update { it.copy(currentStep = step) }
            if (!validateCurrentStep()) {
                return
            }
        }

        // Enviar formulario
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = usuarioRepository.registrarUsuario(_uiState.value.form)) {
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
                            error = "Error al registrar: ${result.exception.message}"
                        )
                    }
                }
                else -> { /* Ignorar estado Loading */ }
            }
        }
    }

    /**
     * Limpia los errores
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Reinicia el estado
     */
    fun resetState() {
        _uiState.update {
            RegistroUiState(centros = it.centros)
        }
    }
}