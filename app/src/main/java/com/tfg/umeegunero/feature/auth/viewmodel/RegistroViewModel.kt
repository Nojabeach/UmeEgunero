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
     * Validaciones de formato
     */
    private fun isDniValid(dni: String): Boolean {
        // DNI español: 8 números y 1 letra
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        if (!dniPattern.matches(dni.uppercase())) return false

        // Validación de letra de control
        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val numero = dni.substring(0, 8).toInt()
        val letra = dni[8]
        return letra == letras[numero % 23]
    }

    /**
     * Validación de email
     */
    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailPattern.matches(email)
    }

    /**
     * Validación de teléfono
     */
    private fun isTelefonoValid(telefono: String): Boolean {
        // Formato español: 9 dígitos, puede empezar por 6, 7, 8 o 9
        val telefonoPattern = Regex("^(\\+34|0034|34)?[6-9]\\d{8}$")
        return telefonoPattern.matches(telefono.replace("[\\s-]".toRegex(), ""))
    }

    /**
     * Validación de código postal
     */
    private fun isCodigoPostalValid(codigoPostal: String): Boolean {
        // Código postal español: 5 dígitos
        val cpPattern = Regex("^\\d{5}$")
        return cpPattern.matches(codigoPostal)
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

    /**
     * Avanza al siguiente paso del formulario
     */
    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        val form = _uiState.value.form
        val errors = mutableMapOf<String, String>()

        // Validaciones más flexibles por paso
        when (currentStep) {
            1 -> {
                // Validaciones de datos personales
                if (form.dni.isBlank()) errors["dni"] = "El DNI es obligatorio"
                if (form.email.isBlank()) errors["email"] = "El email es obligatorio"
                if (form.nombre.isBlank()) errors["nombre"] = "El nombre es obligatorio"
                if (form.apellidos.isBlank()) errors["apellidos"] = "Los apellidos son obligatorios"
                if (form.telefono.isBlank()) errors["telefono"] = "El teléfono es obligatorio"

                // Validaciones de formato adicionales
                if (form.dni.isNotBlank() && !isDniValid(form.dni))
                    errors["dni"] = "El DNI no tiene un formato válido"

                if (form.email.isNotBlank() && !isEmailValid(form.email))
                    errors["email"] = "El formato del email no es válido"

                // Si no hay errores críticos, avanzar
                if (errors.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            currentStep = it.currentStep + 1,
                            formErrors = emptyMap(),
                            error = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(formErrors = errors) }
                }
            }
            2 -> {
                // Validaciones de dirección
                if (form.direccion.calle.isBlank()) errors["calle"] = "La calle es obligatoria"
                if (form.direccion.numero.isBlank()) errors["numero"] = "El número es obligatorio"
                if (form.direccion.codigoPostal.isBlank()) errors["codigoPostal"] = "El código postal es obligatorio"
                if (form.direccion.ciudad.isBlank()) errors["ciudad"] = "La ciudad es obligatoria"
                if (form.direccion.provincia.isBlank()) errors["provincia"] = "La provincia es obligatoria"

                // Validación de formato de código postal
                if (form.direccion.codigoPostal.isNotBlank() && !isCodigoPostalValid(form.direccion.codigoPostal))
                    errors["codigoPostal"] = "El código postal debe tener 5 dígitos"

                // Si no hay errores críticos, avanzar
                if (errors.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            currentStep = it.currentStep + 1,
                            formErrors = emptyMap(),
                            error = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(formErrors = errors) }
                }
            }
            3 -> {
                // Validaciones de alumnos y centro
                if (form.alumnosDni.isEmpty()) errors["alumnos"] = "Debe indicar al menos un alumno"

                // Validar DNI de cada alumno
                form.alumnosDni.forEachIndexed { index, dni ->
                    if (!isDniValid(dni))
                        errors["alumno_$index"] = "El DNI del alumno ${index + 1} no es válido"
                }

                if (form.centroId.isBlank()) errors["centro"] = "Debe seleccionar un centro"

                // Si no hay errores críticos, avanzar
                if (errors.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            currentStep = it.currentStep + 1,
                            formErrors = emptyMap(),
                            error = null
                        )
                    }
                } else {
                    _uiState.update { it.copy(formErrors = errors) }
                }
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
     * Envía el formulario de registro
     */
    fun submitRegistration() {
        // Validar todos los pasos
        var isValid = true
        val allErrors = mutableMapOf<String, String>()

        // Validación completa de datos personales
        val form = _uiState.value.form
        if (form.dni.isBlank() || !isDniValid(form.dni))
            allErrors["dni"] = "DNI inválido"
        if (form.email.isBlank() || !isEmailValid(form.email))
            allErrors["email"] = "Email inválido"
        if (form.password.isBlank() || form.password.length < 6)
            allErrors["password"] = "Contraseña inválida"
        if (form.confirmPassword != form.password)
            allErrors["confirmPassword"] = "Las contraseñas no coinciden"
        if (form.nombre.isBlank())
            allErrors["nombre"] = "Nombre obligatorio"
        if (form.apellidos.isBlank())
            allErrors["apellidos"] = "Apellidos obligatorios"
        if (form.telefono.isBlank() || !isTelefonoValid(form.telefono))
            allErrors["telefono"] = "Teléfono inválido"

        // Validación de dirección
        if (form.direccion.calle.isBlank())
            allErrors["calle"] = "Calle obligatoria"
        if (form.direccion.numero.isBlank())
            allErrors["numero"] = "Número obligatorio"
        if (form.direccion.codigoPostal.isBlank() || !isCodigoPostalValid(form.direccion.codigoPostal))
            allErrors["codigoPostal"] = "Código postal inválido"
        if (form.direccion.ciudad.isBlank())
            allErrors["ciudad"] = "Ciudad obligatoria"
        if (form.direccion.provincia.isBlank())
            allErrors["provincia"] = "Provincia obligatoria"

        // Validación de alumnos y centro
        if (form.alumnosDni.isEmpty())
            allErrors["alumnos"] = "Debe indicar al menos un alumno"
        form.alumnosDni.forEachIndexed { index, dni ->
            if (!isDniValid(dni))
                allErrors["alumno_$index"] = "DNI del alumno ${index + 1} inválido"
        }
        if (form.centroId.isBlank())
            allErrors["centro"] = "Debe seleccionar un centro"

        // Si hay errores, actualizar estado
        if (allErrors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    formErrors = allErrors,
                    currentStep = 1  // Vuelve al primer paso para mostrar errores
                )
            }
            return
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
}