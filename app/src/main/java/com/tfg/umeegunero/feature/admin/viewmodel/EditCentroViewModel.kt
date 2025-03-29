package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class EditCentroViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddCentroUiState(
        centroId = savedStateHandle.get<String>("centroId") ?: ""
    ))
    val uiState: StateFlow<AddCentroUiState> = _uiState.asStateFlow()
    
    private val centroId: String = savedStateHandle.get<String>("centroId") ?: ""
    
    // Patrón de validación para email
    private val emailPattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )
    
    /**
     * Carga los datos del centro desde el repositorio
     */
    fun loadCentro() {
        if (centroId.isEmpty()) {
            _uiState.update { it.copy(error = "ID de centro no proporcionado") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = centroRepository.getCentroById(centroId)) {
                is Result.Success -> {
                    val centro = result.data
                    _uiState.update { currentState ->
                        currentState.copy(
                            centroId = centro.id,
                            nombre = centro.nombre,
                            calle = centro.getDireccionCalle(),
                            numero = centro.getDireccionNumero(),
                            codigoPostal = centro.getDireccionCodigoPostal(),
                            ciudad = centro.getDireccionCiudad(),
                            provincia = centro.getDireccionProvincia(),
                            telefono = centro.obtenerTelefono(),
                            latitud = centro.latitud,
                            longitud = centro.longitud,
                            email = centro.obtenerEmail(),
                            isLoading = false,
                            centroLoaded = true
                        )
                    }
                    Timber.d("Centro cargado correctamente: ${centro.nombre}")
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        error = "Error al cargar el centro: ${result.exception.message}",
                        isLoading = false
                    )}
                    Timber.e(result.exception, "Error al cargar el centro")
                }
                is Result.Loading -> {
                    // Ya estamos en estado de carga, no hacemos nada adicional
                }
            }
        }
    }
    
    /**
     * Actualiza el centro con los datos del formulario
     */
    fun updateCentro() {
        if (!validarFormulario()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val currentState = _uiState.value
            val centro = Centro(
                id = centroId,
                nombre = currentState.nombre,
                direccion = "${currentState.calle}, ${currentState.numero}, ${currentState.codigoPostal}, ${currentState.ciudad}, ${currentState.provincia}",
                contacto = "${currentState.telefono}, ${currentState.adminCentro.firstOrNull()?.email ?: ""}",
                telefono = currentState.telefono,
                email = currentState.adminCentro.firstOrNull()?.email ?: "",
                latitud = currentState.latitud ?: 0.0,
                longitud = currentState.longitud ?: 0.0,
                direccionObj = Direccion(
                    calle = currentState.calle,
                    numero = currentState.numero,
                    codigoPostal = currentState.codigoPostal,
                    ciudad = currentState.ciudad,
                    provincia = currentState.provincia
                ),
                contactoObj = Contacto(
                    telefono = currentState.telefono,
                    email = currentState.adminCentro.firstOrNull()?.email ?: ""
                )
            )
            
            when (val result = centroRepository.updateCentro(centroId, centro)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )}
                    Timber.d("Centro actualizado correctamente: ${centro.nombre}")
                    
                    // Actualizar contraseñas de administradores si hay cambios
                    currentState.adminCentro.forEachIndexed { index, admin ->
                        if (admin.password.isNotBlank()) {
                            // En una implementación real, aquí iría la lógica para actualizar 
                            // la contraseña del administrador en el sistema de autenticación
                            Timber.d("Actualizando contraseña para administrador: ${admin.email}")
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        error = "Error al actualizar el centro: ${result.exception.message}",
                        isLoading = false
                    )}
                    Timber.e(result.exception, "Error al actualizar el centro")
                }
                is Result.Loading -> {
                    // Ya estamos en estado de carga, no hacemos nada adicional
                }
            }
        }
    }
    
    /**
     * Valida todos los campos del formulario
     */
    private fun validarFormulario(): Boolean {
        var isValid = true
        
        // Validar nombre
        if (_uiState.value.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre es obligatorio") }
            isValid = false
        } else {
            _uiState.update { it.copy(nombreError = null) }
        }
        
        // Validar calle
        if (_uiState.value.calle.isBlank()) {
            _uiState.update { it.copy(calleError = "La calle es obligatoria") }
            isValid = false
        } else {
            _uiState.update { it.copy(calleError = null) }
        }
        
        // Validar número
        if (_uiState.value.numero.isBlank()) {
            _uiState.update { it.copy(numeroError = "El número es obligatorio") }
            isValid = false
        } else {
            _uiState.update { it.copy(numeroError = null) }
        }
        
        // Validar código postal
        if (_uiState.value.codigoPostal.isBlank()) {
            _uiState.update { it.copy(codigoPostalError = "El código postal es obligatorio") }
            isValid = false
        } else if (_uiState.value.codigoPostal.length != 5 || !_uiState.value.codigoPostal.all { it.isDigit() }) {
            _uiState.update { it.copy(codigoPostalError = "El código postal debe tener 5 dígitos") }
            isValid = false
        } else {
            _uiState.update { it.copy(codigoPostalError = null) }
        }
        
        // Validar ciudad
        if (_uiState.value.ciudad.isBlank()) {
            _uiState.update { it.copy(ciudadError = "La ciudad es obligatoria") }
            isValid = false
        } else {
            _uiState.update { it.copy(ciudadError = null) }
        }
        
        // Validar provincia
        if (_uiState.value.provincia.isBlank()) {
            _uiState.update { it.copy(provinciaError = "La provincia es obligatoria") }
            isValid = false
        } else {
            _uiState.update { it.copy(provinciaError = null) }
        }
        
        // Validar teléfono si está presente
        if (_uiState.value.telefono.isNotBlank() && (_uiState.value.telefono.length < 9 || !_uiState.value.telefono.all { it.isDigit() })) {
            _uiState.update { it.copy(telefonoError = "El teléfono debe tener al menos 9 dígitos") }
            isValid = false
        } else {
            _uiState.update { it.copy(telefonoError = null) }
        }
        
        // Validar email de administradores
        val adminCentroUpdated = _uiState.value.adminCentro.mapIndexed { index, admin ->
            if (admin.email.isBlank()) {
                admin.copy(emailError = "El email es obligatorio")
            } else if (!emailPattern.matcher(admin.email).matches()) {
                admin.copy(emailError = "Email no válido")
            } else {
                admin.copy(emailError = null)
            }
        }
        
        _uiState.update { it.copy(adminCentro = adminCentroUpdated) }
        
        // Comprobar si hay algún error en los administradores
        if (adminCentroUpdated.any { it.emailError != null || it.passwordError != null }) {
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Actualiza el nombre del centro
     */
    fun updateNombre(nombre: String) {
        _uiState.update { currentState ->
            currentState.copy(
                nombre = nombre,
                nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null
            )
        }
    }
    
    /**
     * Actualiza la calle del centro
     */
    fun updateCalle(calle: String) {
        _uiState.update { currentState ->
            currentState.copy(
                calle = calle,
                calleError = if (calle.isBlank()) "La calle es obligatoria" else null
            )
        }
    }
    
    /**
     * Actualiza el número del centro
     */
    fun updateNumero(numero: String) {
        _uiState.update { currentState ->
            currentState.copy(
                numero = numero,
                numeroError = if (numero.isBlank()) "El número es obligatorio" else null
            )
        }
    }
    
    /**
     * Actualiza el código postal y busca ciudades según el código
     */
    fun updateCodigoPostal(codigoPostal: String) {
        if (codigoPostal.length > 5) return
        
        _uiState.update { currentState ->
            currentState.copy(
                codigoPostal = codigoPostal,
                codigoPostalError = when {
                    codigoPostal.isBlank() -> "El código postal es obligatorio"
                    codigoPostal.length != 5 || !codigoPostal.all { it.isDigit() } ->
                        "El código postal debe tener 5 dígitos"
                    else -> null
                },
                ciudadesSugeridas = emptyList()  // Limpiar sugerencias al cambiar el código postal
            )
        }
        
        // Si el código postal tiene 5 dígitos, buscar ciudades
        if (codigoPostal.length == 5 && codigoPostal.all { it.isDigit() }) {
            buscarCiudades(codigoPostal)
        }
    }
    
    /**
     * Busca ciudades por código postal
     */
    private fun buscarCiudades(codigoPostal: String) {
        // En una implementación real, aquí iría la lógica para buscar ciudades por código postal
        // Por simplicidad, solo simulamos la respuesta
        val ciudadesFicticias = listOf(
            Ciudad("Madrid", "Madrid", codigoPostal),
            Ciudad("Barcelona", "Barcelona", codigoPostal),
            Ciudad("Valencia", "Valencia", codigoPostal)
        )
        
        _uiState.update { it.copy(ciudadesSugeridas = ciudadesFicticias) }
    }
    
    /**
     * Actualiza la ciudad del centro
     */
    fun updateCiudad(ciudad: String) {
        _uiState.update { currentState ->
            currentState.copy(
                ciudad = ciudad,
                ciudadError = if (ciudad.isBlank()) "La ciudad es obligatoria" else null
            )
        }
    }
    
    /**
     * Actualiza la provincia del centro
     */
    fun updateProvincia(provincia: String) {
        _uiState.update { currentState ->
            currentState.copy(
                provincia = provincia,
                provinciaError = if (provincia.isBlank()) "La provincia es obligatoria" else null
            )
        }
    }
    
    /**
     * Actualiza el teléfono del centro
     */
    fun updateTelefono(telefono: String) {
        _uiState.update { currentState ->
            currentState.copy(
                telefono = telefono,
                telefonoError = if (telefono.isNotBlank() && (telefono.length < 9 || !telefono.all { it.isDigit() }))
                    "El teléfono debe tener al menos 9 dígitos"
                else null
            )
        }
    }
    
    /**
     * Actualiza el email de un administrador
     */
    fun updateAdminEmail(index: Int, email: String) {
        val adminCentro = _uiState.value.adminCentro.toMutableList()
        if (index < adminCentro.size) {
            adminCentro[index] = adminCentro[index].copy(
                email = email,
                emailError = when {
                    email.isBlank() -> "El email es obligatorio"
                    !emailPattern.matcher(email).matches() -> "Email no válido"
                    else -> null
                }
            )
            _uiState.update { it.copy(adminCentro = adminCentro) }
        }
    }
    
    /**
     * Actualiza la contraseña de un administrador
     */
    fun updateAdminPassword(index: Int, password: String) {
        val adminCentro = _uiState.value.adminCentro.toMutableList()
        if (index < adminCentro.size) {
            adminCentro[index] = adminCentro[index].copy(
                password = password,
                passwordError = if (password.isNotBlank() && password.length < 6)
                    "La contraseña debe tener al menos 6 caracteres"
                else null
            )
            _uiState.update { it.copy(adminCentro = adminCentro) }
        }
    }
    
    /**
     * Actualiza la latitud del centro
     */
    fun updateLatitud(latitud: Double?) {
        _uiState.update { it.copy(latitud = latitud) }
    }
    
    /**
     * Actualiza la longitud del centro
     */
    fun updateLongitud(longitud: Double?) {
        _uiState.update { it.copy(longitud = longitud) }
    }
    
    /**
     * Selecciona una ciudad de las sugerencias
     */
    fun seleccionarCiudad(ciudad: Ciudad) {
        _uiState.update { currentState ->
            currentState.copy(
                ciudad = ciudad.nombre,
                provincia = ciudad.provincia,
                ciudadesSugeridas = emptyList()
            )
        }
    }
    
    /**
     * Añade un nuevo administrador
     */
    fun addAdminCentro() {
        _uiState.update { currentState ->
            val currentAdmins = currentState.adminCentro.toMutableList()
            // Añadir un nuevo administrador con campos vacíos
            currentAdmins.add(
                com.tfg.umeegunero.feature.admin.viewmodel.AdminCentro(
                    email = "",
                    password = "",
                    emailError = null,
                    passwordError = null
                )
            )
            currentState.copy(adminCentro = currentAdmins)
        }
    }
    
    /**
     * Elimina un administrador secundario
     */
    fun removeAdminCentro(index: Int) {
        if (index > 0) { // No permitir eliminar el administrador principal
            _uiState.update { currentState ->
                val currentAdmins = currentState.adminCentro.toMutableList()
                currentAdmins.removeAt(index)
                currentState.copy(adminCentro = currentAdmins)
            }
        }
    }
} 