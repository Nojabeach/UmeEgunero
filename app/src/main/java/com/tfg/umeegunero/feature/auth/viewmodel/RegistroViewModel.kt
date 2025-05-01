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
import com.tfg.umeegunero.data.network.NominatimApiService
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
import com.tfg.umeegunero.data.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException

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
    val direccionError: String? = null,
    val centroIdError: String? = null,
    val alumnosDniError: String? = null,
    // Estado de búsqueda de dirección
    val isLoadingDireccion: Boolean = false,
    val coordenadasLatitud: Double? = null,
    val coordenadasLongitud: Double? = null,
    val mapaUrl: String? = null
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
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val nominatimApiService: NominatimApiService,
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
                        it.copy(centros = result.data, isLoadingCentros = false)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = "Error al cargar centros: ${result.exception?.message}",
                            isLoadingCentros = false
                        )
                    }
                }
                else -> { /* Ignorar Loading */ }
            }
        }
    }

    /**
     * Actualiza un campo del formulario y dispara validaciones en tiempo real.
     *
     * @param field Nombre del campo (ej. "email", "calle", "centroId").
     * @param value Nuevo valor del campo.
     */
    fun updateFormField(field: String, value: String) {
        _uiState.update { currentState ->
            val newForm = when (field) {
                "dni" -> currentState.form.copy(dni = value)
                "email" -> currentState.form.copy(email = value)
                "password" -> currentState.form.copy(password = value)
                "confirmPassword" -> currentState.form.copy(confirmPassword = value)
                "nombre" -> currentState.form.copy(nombre = value)
                "apellidos" -> currentState.form.copy(apellidos = value)
                "telefono" -> currentState.form.copy(telefono = value)
                // Campos de Dirección
                "calle" -> currentState.form.copy(direccion = currentState.form.direccion.copy(calle = value))
                "numero" -> currentState.form.copy(direccion = currentState.form.direccion.copy(numero = value))
                "piso" -> currentState.form.copy(direccion = currentState.form.direccion.copy(piso = value))
                "codigoPostal" -> {
                    val formUpdated = currentState.form.copy(direccion = currentState.form.direccion.copy(codigoPostal = value))
                    // Si el código postal tiene 5 dígitos, buscar automáticamente
                    if (value.length == 5 && value.all { it.isDigit() }) {
                        // Lanzar búsqueda en segundo plano
                        buscarDireccionPorCP(value)
                    }
                    formUpdated
                }
                "ciudad" -> currentState.form.copy(direccion = currentState.form.direccion.copy(ciudad = value))
                "provincia" -> currentState.form.copy(direccion = currentState.form.direccion.copy(provincia = value))
                // Campo de Centro
                "centroId" -> currentState.form.copy(centroId = value)
                else -> currentState.form // No hacer nada si el campo no se reconoce
            }
            // Llamar a validación después de actualizar el formulario
            validateField(field, newForm, currentState.copy(form = newForm))
        }
    }

    /**
     * Busca la dirección completa basada en un código postal usando la API de Nominatim.
     * Actualiza automáticamente los campos de ciudad y provincia.
     * 
     * @param codigoPostal El código postal a buscar (5 dígitos para España)
     */
    private fun buscarDireccionPorCP(codigoPostal: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingDireccion = true) }
                
                val response = nominatimApiService.searchByPostalCode(
                    postalCode = codigoPostal,
                    limit = 1
                )
                
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val place = response.body()!!.first()
                    
                    // Extraer ciudad y provincia
                    val ciudad = place.address?.getCityName() ?: ""
                    val provincia = place.address?.province ?: place.address?.state ?: ""
                    
                    // Extraer coordenadas
                    val latitud = place.lat.toDoubleOrNull()
                    val longitud = place.lon.toDoubleOrNull()
                    
                    // Generar URL de mapa estático (usar OpenStreetMap en lugar de Google Maps)
                    val mapaUrl = if (latitud != null && longitud != null) {
                        // URL para OpenStreetMap
                        "https://staticmap.openstreetmap.de/staticmap.php?center=$latitud,$longitud&zoom=14&size=400x200&markers=$latitud,$longitud"
                    } else null
                    
                    // Actualizar el formulario con los datos obtenidos
                    _uiState.update { state ->
                        state.copy(
                            form = state.form.copy(
                                direccion = state.form.direccion.copy(
                                    ciudad = ciudad,
                                    provincia = provincia
                                )
                            ),
                            coordenadasLatitud = latitud,
                            coordenadasLongitud = longitud,
                            mapaUrl = mapaUrl,
                            isLoadingDireccion = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingDireccion = false) }
                }
            } catch (e: IOException) {
                Log.e("RegistroViewModel", "Error de red al buscar dirección: ${e.message}")
                _uiState.update { it.copy(isLoadingDireccion = false) }
            } catch (e: HttpException) {
                Log.e("RegistroViewModel", "Error HTTP al buscar dirección: ${e.message}")
                _uiState.update { it.copy(isLoadingDireccion = false) }
            } catch (e: Exception) {
                Log.e("RegistroViewModel", "Error al buscar dirección: ${e.message}")
                _uiState.update { it.copy(isLoadingDireccion = false) }
            }
        }
    }

    /**
     * Valida un campo específico y actualiza el estado de error correspondiente.
     *
     * @param field Nombre del campo validado.
     * @param updatedForm El formulario con el valor ya actualizado.
     * @param currentState El estado actual (antes de aplicar errores de validación).
     * @return El nuevo estado con los errores de validación actualizados.
     */
    private fun validateField(field: String, updatedForm: RegistroUsuarioForm, currentState: RegistroUiState): RegistroUiState {
        var newState = currentState.copy(form = updatedForm) // Empezar con el formulario actualizado

        when (field) {
            "email" -> {
                val emailError = if (updatedForm.email.isNotBlank() && !isEmailValid(updatedForm.email)) {
                    "Formato de email inválido."
                } else null
                newState = newState.copy(emailError = emailError)
            }
            "dni" -> {
                val dniError = if (updatedForm.dni.isNotBlank() && !validateDni(updatedForm.dni)) {
                    "Formato de DNI/NIE inválido."
                } else null
                newState = newState.copy(dniError = dniError)
            }
            "password" -> {
                var passwordError: String? = null
                if (updatedForm.password.isNotBlank() && !validatePassword(updatedForm.password)) {
                    passwordError = "La contraseña no cumple los requisitos."
                }
                newState = newState.copy(passwordError = passwordError)
                // Revalidar confirmación si la contraseña cambia
                val confirmError = if (updatedForm.confirmPassword.isNotBlank() && updatedForm.password != updatedForm.confirmPassword) {
                    "Las contraseñas no coinciden."
                } else null
                newState = newState.copy(confirmPasswordError = confirmError)
            }
            "confirmPassword" -> {
                val confirmError = if (updatedForm.confirmPassword.isNotBlank() && updatedForm.password != updatedForm.confirmPassword) {
                    "Las contraseñas no coinciden."
                } else null
                newState = newState.copy(confirmPasswordError = confirmError)
            }
             "telefono" -> {
                 val telefonoError = if (updatedForm.telefono.isNotBlank() && !isPhoneValid(updatedForm.telefono)) { // Crear isPhoneValid si se necesita
                     "Formato de teléfono inválido."
                 } else null
                 newState = newState.copy(telefonoError = telefonoError)
             }
             "centroId" -> {
                 val centroError = if (updatedForm.centroId.isBlank()) {
                     "Debes seleccionar un centro."
                 } else null
                 newState = newState.copy(centroIdError = centroError)
             }
             "codigoPostal" -> {
                 val cpError = if (updatedForm.direccion.codigoPostal.isNotBlank() && 
                                   (updatedForm.direccion.codigoPostal.length != 5 || 
                                    !updatedForm.direccion.codigoPostal.all { it.isDigit() })) {
                     "Código postal debe tener 5 dígitos"
                 } else null
                 newState = newState.copy(direccionError = cpError)
             }
            // Añadir validaciones para otros campos si es necesario (nombre, apellidos, dirección)
        }
        return newState
    }

    /**
     * Actualiza el tipo de familiar seleccionado.
     */
    fun updateSubtipoFamiliar(subtipo: SubtipoFamiliar) {
        _uiState.update { it.copy(form = it.form.copy(subtipo = subtipo)) }
    }

    /**
     * Añade un nuevo campo vacío para DNI de alumno.
     */
    fun addAlumnoDni() {
        _uiState.update { currentState ->
            val currentAlumnos = currentState.form.alumnosDni.toMutableList()
            currentAlumnos.add("")
            val newForm = currentState.form.copy(alumnosDni = currentAlumnos)
            // Revalidar si al menos un DNI es requerido
            validateAlumnosDniList(newForm, currentState.copy(form = newForm))
        }
    }

    /**
     * Actualiza el DNI de un alumno específico y valida la lista.
     */
    fun updateAlumnoDni(index: Int, dni: String) {
        _uiState.update { currentState ->
            val currentAlumnos = currentState.form.alumnosDni.toMutableList()
            if (index in currentAlumnos.indices) {
                currentAlumnos[index] = dni
                val newForm = currentState.form.copy(alumnosDni = currentAlumnos)
                 // Revalidar la lista completa (p.ej., si se requiere al menos uno)
                 validateAlumnosDniList(newForm, currentState.copy(form = newForm))
            } else {
                currentState // No hacer cambios si el índice es inválido
            }
        }
    }

    /**
     * Elimina el DNI de un alumno específico y valida la lista.
     */
    fun removeAlumnoDni(index: Int) {
        _uiState.update { currentState ->
            val currentAlumnos = currentState.form.alumnosDni.toMutableList()
            if (index in currentAlumnos.indices) {
                currentAlumnos.removeAt(index)
                val newForm = currentState.form.copy(alumnosDni = currentAlumnos)
                 // Revalidar la lista completa
                 validateAlumnosDniList(newForm, currentState.copy(form = newForm))
            } else {
                 currentState
            }
        }
    }

    /**
      * Valida la lista de DNIs de alumnos (ej. si se requiere al menos uno).
      */
    private fun validateAlumnosDniList(form: RegistroUsuarioForm, currentState: RegistroUiState): RegistroUiState {
        // Ejemplo: Validar que al menos un DNI no esté vacío si estamos en el paso 3 o más allá
        val error = if (currentState.currentStep >= 3 && form.alumnosDni.all { it.isBlank() }) {
            "Debes añadir al menos un DNI de alumno."
        } else {
            null
        }
        return currentState.copy(form = form, alumnosDniError = error)
    }

    /**
     * Avanza al siguiente paso del formulario.
     * La validación ahora se hace en la UI antes de llamar a esta función.
     */
    fun nextStep() {
        _uiState.update { currentState ->
            if (currentState.currentStep < currentState.totalSteps) {
                currentState.copy(currentStep = currentState.currentStep + 1, error = null) // Limpiar error al avanzar
            } else {
                currentState // No hacer nada si ya está en el último paso
            }
        }
    }

    /**
     * Retrocede al paso anterior del formulario.
     */
    fun previousStep() {
        _uiState.update { currentState ->
            if (currentState.currentStep > 1) {
                currentState.copy(currentStep = currentState.currentStep - 1, error = null) // Limpiar error al retroceder
            } else {
                currentState // No hacer nada si ya está en el primer paso
            }
        }
    }

    /**
     * Intenta registrar al usuario con los datos del formulario.
     * Llamado desde la UI después de validar el último paso.
     */
    fun registrarUsuario() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = false) }

            val formToRegister = _uiState.value.form

            try {
                // Llamar al método de registro del UsuarioRepository
                when (val result = usuarioRepository.registrarUsuario(formToRegister)) { 
                    is Result.Success -> { 
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                success = true
                            )
                        }
                        // Registrar éxito en log
                        Log.i("RegistroViewModel", "Usuario registrado exitosamente: ${formToRegister.email}")
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error desconocido durante el registro.",
                                success = false
                            )
                        }
                        // Registrar error en log
                        Log.e("RegistroViewModel", "Error en registro", result.exception)
                    }
                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Estado de registro no reconocido.",
                                success = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false, 
                        error = "Error en el registro: ${e.message ?: "Desconocido"}",
                        success = false
                    )
                }
                // Registrar excepción en log
                Log.e("RegistroViewModel", "Excepción en el registro", e)
            }
        }
    }

    // --- Funciones de Validación Auxiliares (Mantenidas o movidas a Utils) ---
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validateDni(dni: String): Boolean {
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        if (!dniPattern.matches(dni.uppercase())) return false
        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val numero = dni.substring(0, 8).toIntOrNull() ?: return false
        return dni.uppercase()[8] == letras[numero % 23]
    }

    private fun validatePassword(password: String): Boolean {
        return password.length >= 6 && password.any { it.isLetter() } && password.any { it.isDigit() }
    }

     private fun isPhoneValid(phone: String): Boolean {
         // Implementar validación de formato de teléfono si es necesario
         return phone.all { it.isDigit() } && phone.length >= 9 // Ejemplo básico
     }
}