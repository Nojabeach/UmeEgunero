package com.tfg.umeegunero.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.RegistroUsuarioForm
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.DebugUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.regex.Pattern
import android.util.Log

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
    val formErrors: Map<String, String> = emptyMap(),
    // Errores de validación en tiempo real
    val dniError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nombreError: String? = null,
    val apellidosError: String? = null,
    val telefonoError: String? = null,
    val direccionError: String? = null
)

/**
 * ViewModel para el proceso de registro de usuarios en la aplicación UmeEgunero.
 * 
 * Este ViewModel implementa el patrón MVVM (Model-View-ViewModel) y utiliza las mejores prácticas
 * de desarrollo Android moderno, incluyendo:
 * - Jetpack Compose para la UI
 * - Coroutines para operaciones asíncronas
 * - StateFlow para la gestión del estado
 * - Hilt para la inyección de dependencias
 * 
 * @property authRepository Repositorio para operaciones de autenticación
 * @property uiState Estado actual de la UI
 * @property currentStep Paso actual del formulario
 * @property totalSteps Total de pasos del formulario
 * 
 * @see RegistroUiState
 * @see RegistroUsuarioForm
 */
@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val debugUtils: DebugUtils
) : ViewModel() {

    /**
     * Estado actual de la UI del registro.
     * 
     * Este estado es inmutable y contiene toda la información necesaria para renderizar la UI.
     * Se actualiza mediante el patrón de diseño StateFlow para garantizar la reactividad.
     * 
     * @property form Datos actuales del formulario
     * @property isLoading Indica si hay operaciones en curso
     * @property error Mensaje de error, si existe
     * @property success Indica si el registro fue exitoso
     * @property emailError Error de validación del email
     * @property dniError Error de validación del DNI
     * @property passwordError Error de validación de la contraseña
     * @property nombreError Error de validación del nombre
     * @property apellidosError Error de validación de los apellidos
     * @property telefonoError Error de validación del teléfono
     */
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
                    // Comprobar si el error es de deserialización
                    val errorMessage = if (result.exception?.message?.contains("deserialize") == true || 
                                         result.exception?.message?.contains("HashMap") == true) {
                        "Error de compatibilidad en los datos de los centros. Por favor, contacte con soporte."
                    } else {
                        "Error al cargar centros: ${result.exception?.message}"
                    }
                    
                    _uiState.update {
                        it.copy(
                            error = errorMessage,
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
        when (field) {
            "dni" -> _uiState.update { it.copy(form = it.form.copy(dni = value)) }
            "email" -> _uiState.update { it.copy(form = it.form.copy(email = value)) }
            "password" -> _uiState.update { it.copy(form = it.form.copy(password = value)) }
            "confirmPassword" -> _uiState.update { it.copy(form = it.form.copy(confirmPassword = value)) }
            "nombre" -> _uiState.update { it.copy(form = it.form.copy(nombre = value)) }
            "apellidos" -> _uiState.update { it.copy(form = it.form.copy(apellidos = value)) }
            "telefono" -> _uiState.update { it.copy(form = it.form.copy(telefono = value)) }
            "calle" -> {
                val nuevaDireccion = _uiState.value.form.direccion.copy(calle = value)
                _uiState.update { it.copy(form = it.form.copy(direccion = nuevaDireccion)) }
            }
            "numero" -> {
                val nuevaDireccion = _uiState.value.form.direccion.copy(numero = value)
                _uiState.update { it.copy(form = it.form.copy(direccion = nuevaDireccion)) }
            }
            "piso" -> {
                val nuevaDireccion = _uiState.value.form.direccion.copy(piso = value)
                _uiState.update { it.copy(form = it.form.copy(direccion = nuevaDireccion)) }
            }
            "codigoPostal" -> {
                val nuevaDireccion = _uiState.value.form.direccion.copy(codigoPostal = value)
                _uiState.update { it.copy(form = it.form.copy(direccion = nuevaDireccion)) }
            }
            "ciudad" -> {
                val nuevaDireccion = _uiState.value.form.direccion.copy(ciudad = value)
                _uiState.update { it.copy(form = it.form.copy(direccion = nuevaDireccion)) }
            }
            "provincia" -> {
                val nuevaDireccion = _uiState.value.form.direccion.copy(provincia = value)
                _uiState.update { it.copy(form = it.form.copy(direccion = nuevaDireccion)) }
            }
            "centroId" -> _uiState.update { it.copy(form = it.form.copy(centroId = value)) }
        }
        validateFormFields() // Llamar a la validación en tiempo real
    }
    
    /**
     * Método que actualiza el formulario y valida los campos
     * (método para usar desde la UI ya que validateFormFields es privado)
     */
    fun updateForm() {
        validateFormFields()
    }
    
    /**
     * Validar campos del formulario
     */
    private fun validateFormFields() {
        val errors = mutableMapOf<String, String>()
        val form = _uiState.value.form

        // Validaciones de ejemplo
        if (form.email.isNotEmpty() && !isEmailValid(form.email)) {
            errors["email"] = "Email inválido."
        }
        // Agregar más validaciones según sea necesario

        _uiState.update { it.copy(formErrors = errors) }
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
     * Añade un nuevo campo para DNI de alumno
     */
    fun addAlumnoDni() {
        val currentAlumnos = _uiState.value.form.alumnosDni.toMutableList()
        currentAlumnos.add("")

        _uiState.update {
            it.copy(
                form = it.form.copy(alumnosDni = currentAlumnos)
            )
        }
    }

    /**
     * Actualiza el DNI de un alumno específico
     */
    fun updateAlumnoDni(index: Int, dni: String) {
        val currentAlumnos = _uiState.value.form.alumnosDni.toMutableList()
        if (index in currentAlumnos.indices) {
            currentAlumnos[index] = dni
            _uiState.update {
                it.copy(
                    form = it.form.copy(alumnosDni = currentAlumnos)
                )
            }
        }
    }

    /**
     * Elimina el DNI de un alumno específico
     */
    fun removeAlumnoDni(index: Int) {
        val currentAlumnos = _uiState.value.form.alumnosDni.toMutableList()
        if (index in currentAlumnos.indices) {
            currentAlumnos.removeAt(index)
            _uiState.update {
                it.copy(
                    form = it.form.copy(alumnosDni = currentAlumnos)
                )
            }
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
     * Envía la solicitud de registro
     */
    fun submitRegistration() {
        // Validar todos los campos antes de proceder
        val dniError = validateDni(_uiState.value.form.dni)
        val emailError = validateEmail(_uiState.value.form.email)
        val nombreError = validateNombre(_uiState.value.form.nombre)
        val apellidosError = validateApellidos(_uiState.value.form.apellidos)
        val telefonoError = validateTelefono(_uiState.value.form.telefono)
        val passwordError = validatePassword(_uiState.value.form.password)
        val confirmPasswordError = validateConfirmPassword(_uiState.value.form.password, _uiState.value.form.confirmPassword)
        
        // Actualizar errores
        _uiState.update { 
            it.copy(
                dniError = dniError,
                emailError = emailError,
                nombreError = nombreError,
                apellidosError = apellidosError,
                telefonoError = telefonoError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
        }
        
        // Si hay errores, no continuar con el registro
        if (dniError != null || emailError != null || nombreError != null || 
            apellidosError != null || telefonoError != null || 
            passwordError != null || confirmPasswordError != null) {
            // Mostramos un mensaje de error general
            _uiState.update { it.copy(error = "Por favor, corrige los errores antes de continuar.") }
            return
        }
        
        // Si no hay errores, proceder con el registro
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = usuarioRepository.registrarUsuario(_uiState.value.form)) {
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
                            error = "Error al registrar usuario: ${result.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // No hacer nada, ya hemos establecido isLoading a true
                }
            }
        }
    }

    /**
     * Enviar código de verificación al email
     */
    fun sendVerificationEmail() {
        viewModelScope.launch {
            val email = _uiState.value.form.email
            if (isEmailValid(email)) {
                // Lógica para enviar el código de verificación
                // En una implementación real, esto llamaría a un servicio de autenticación
                // Por ahora, solo actualizamos el estado para mostrar que se envió
                _uiState.update { 
                    it.copy(
                        // Aquí podríamos actualizar algún estado para mostrar feedback al usuario
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        formErrors = it.formErrors + ("email" to "Email inválido, no se puede enviar verificación.")
                    ) 
                }
            }
        }
    }

    /**
     * Detección automática de ubicación
     */
    fun detectUserLocation() {
        viewModelScope.launch {
            // En una implementación real, esto usaría servicios de ubicación
            // Por ahora, simularemos que obtuvimos la ubicación
            _uiState.update { 
                it.copy(
                    // Actualizar la dirección con datos de ubicación simulada
                    form = it.form.copy(
                        direccion = it.form.direccion.copy(
                            ciudad = "Bilbao",
                            provincia = "Vizcaya"
                        )
                    )
                ) 
            }
        }
    }

    /**
     * Escaneo de código QR
     */
    fun scanQRCode() {
        // Esta función en una implementación real iniciaría el escáner de QR
        // y procesaría el resultado
    }

    /**
     * Actualiza el valor del DNI y valida en tiempo real
     */
    fun updateDni(dni: String) {
        _uiState.update { 
            it.copy(
                form = it.form.copy(dni = dni), 
                dniError = validateDni(dni)
            ) 
        }
    }

    /**
     * Valida formato del DNI
     */
    private fun validateDni(dni: String): String? {
        return when {
            dni.isBlank() -> "El DNI es obligatorio"
            !isDniValid(dni) -> "El DNI no tiene un formato válido"
            else -> null
        }
    }

    /**
     * Actualiza el valor del email y valida en tiempo real
     */
    fun updateEmail(email: String) {
        _uiState.update { 
            it.copy(
                form = it.form.copy(email = email), 
                emailError = validateEmail(email)
            ) 
        }
    }

    /**
     * Valida formato del email
     */
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !Pattern.matches(EMAIL_PATTERN, email) -> "El formato del email no es válido"
            else -> null
        }
    }

    /**
     * Actualiza el valor del nombre y valida en tiempo real
     */
    fun updateNombre(nombre: String) {
        _uiState.update { 
            it.copy(
                form = it.form.copy(nombre = nombre), 
                nombreError = validateNombre(nombre)
            ) 
        }
    }

    /**
     * Valida formato del nombre
     */
    private fun validateNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "El nombre es obligatorio"
            nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
    }

    /**
     * Actualiza el valor de los apellidos y valida en tiempo real
     */
    fun updateApellidos(apellidos: String) {
        _uiState.update { 
            it.copy(
                form = it.form.copy(apellidos = apellidos), 
                apellidosError = validateApellidos(apellidos)
            ) 
        }
    }

    /**
     * Valida formato de los apellidos
     */
    private fun validateApellidos(apellidos: String): String? {
        return when {
            apellidos.isBlank() -> "Los apellidos son obligatorios"
            apellidos.length < 2 -> "Los apellidos deben tener al menos 2 caracteres"
            else -> null
        }
    }

    /**
     * Actualiza el valor del teléfono y valida en tiempo real
     */
    fun updateTelefono(telefono: String) {
        _uiState.update { 
            it.copy(
                form = it.form.copy(telefono = telefono), 
                telefonoError = validateTelefono(telefono)
            ) 
        }
    }

    /**
     * Valida formato del teléfono
     */
    private fun validateTelefono(telefono: String): String? {
        return when {
            telefono.isBlank() -> null // El teléfono no es obligatorio
            !Pattern.matches(PHONE_PATTERN, telefono) -> "El formato del teléfono no es válido"
            else -> null
        }
    }

    /**
     * Actualiza el valor de la contraseña y valida en tiempo real
     */
    fun updatePassword(password: String) {
        val passwordError = validatePassword(password)
        val confirmPasswordError = if (_uiState.value.form.confirmPassword.isNotEmpty()) {
            validateConfirmPassword(password, _uiState.value.form.confirmPassword)
        } else {
            _uiState.value.confirmPasswordError
        }

        _uiState.update { 
            it.copy(
                form = it.form.copy(password = password), 
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            ) 
        }
    }

    /**
     * Valida formato de la contraseña
     */
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            !password.any { it.isDigit() } -> "La contraseña debe contener al menos un número"
            !password.any { it.isLetter() } -> "La contraseña debe contener al menos una letra"
            else -> null
        }
    }

    /**
     * Actualiza el valor de la confirmación de contraseña y valida en tiempo real
     */
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { 
            it.copy(
                form = it.form.copy(confirmPassword = confirmPassword), 
                confirmPasswordError = validateConfirmPassword(it.form.password, confirmPassword)
            ) 
        }
    }

    /**
     * Valida que la confirmación de contraseña coincida con la contraseña
     */
    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "La confirmación de contraseña es obligatoria"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    /**
     * Método para purgar los centros de Firestore desde la pantalla de debug
     */
    fun purgarCentrosFirestore() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val (success, message) = debugUtils.purgarCentrosEducativos()
                
                if (success) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Operación completada: $message"
                        ) 
                    }
                    
                    // Recargar centros después de purgar
                    cargarCentros()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error: $message"
                        ) 
                    }
                }
            } catch (e: Exception) {
                Log.e("RegistroViewModel", "Error al purgar centros", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    ) 
                }
            }
        }
    }

    companion object {
        private const val DNI_PATTERN = "^[0-9]{8}[A-Za-z]$"
        private const val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        private const val PHONE_PATTERN = "^[6-9][0-9]{8}$"
    }
}