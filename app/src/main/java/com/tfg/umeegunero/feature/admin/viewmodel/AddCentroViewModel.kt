package com.tfg.umeegunero.feature.admin.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.CiudadRepository
import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.network.NominatimRetrofitClient
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class AddCentroViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val ciudadRepository: CiudadRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCentroState())
    val uiState: StateFlow<AddCentroState> = _uiState.asStateFlow()
    
    // Lista de provincias disponibles
    private val _provincias = MutableStateFlow<List<String>>(emptyList())
    val provincias: StateFlow<List<String>> = _provincias.asStateFlow()
    
    init {
        // Cargar la lista de provincias al inicializar el ViewModel
        cargarProvincias()
    }
    
    /**
     * Carga la lista de provincias disponibles desde el repositorio
     */
    private fun cargarProvincias() {
        viewModelScope.launch {
            try {
                val listaProvincias = ciudadRepository.obtenerProvincias()
                _provincias.value = listaProvincias
                Timber.d("Provincias cargadas: ${listaProvincias.size}")
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar provincias")
                _provincias.value = emptyList()
            }
        }
    }

    fun updateNombre(nombre: String) {
        val error = if (nombre.isBlank()) "El nombre es obligatorio" else null
        _uiState.update { it.copy(nombre = nombre, nombreError = error) }
    }

    fun updateCalle(calle: String) {
        val error = if (calle.isBlank()) "La calle es obligatoria" else null
        _uiState.update { it.copy(calle = calle, calleError = error) }
        
        // Intentar geocodificar la dirección cuando se actualiza la calle
        actualizarCoordenadas()
    }

    fun updateNumero(numero: String) {
        val error = if (numero.isBlank()) "El número es obligatorio" else null
        _uiState.update { it.copy(numero = numero, numeroError = error) }
        
        // Intentar geocodificar la dirección cuando se actualiza el número
        actualizarCoordenadas()
    }

    fun updateCodigoPostal(codigoPostal: String) {
        val error = when {
            codigoPostal.isBlank() -> "El código postal es obligatorio"
            !isValidCodigoPostal(codigoPostal) -> "El código postal debe tener 5 dígitos"
            else -> null
        }
        _uiState.update { it.copy(codigoPostal = codigoPostal, codigoPostalError = error) }
        
        // Si el código postal es válido, buscar ciudades
        if (codigoPostal.length == 5 && error == null) {
            buscarCiudadesPorCodigoPostal(codigoPostal)
            // También actualizamos las coordenadas
            actualizarCoordenadas()
        } else {
            // Limpiar las ciudades sugeridas si el código postal no es válido
            _uiState.update { it.copy(ciudadesSugeridas = emptyList(), errorBusquedaCiudades = null) }
        }
    }

    private fun buscarCiudadesPorCodigoPostal(codigoPostal: String) {
        _uiState.update { it.copy(isBuscandoCiudades = true, errorBusquedaCiudades = null) }
        
        viewModelScope.launch {
            try {
                ciudadRepository.buscarCiudadesPorCodigoPostal(codigoPostal) { ciudades, error ->
                    viewModelScope.launch {
                        if (ciudades != null && ciudades.isNotEmpty()) {
                            _uiState.update { 
                                it.copy(
                                    ciudadesSugeridas = ciudades,
                                    isBuscandoCiudades = false,
                                    errorBusquedaCiudades = error // Puede contener un mensaje informativo aunque haya resultados
                                ) 
                            }
                            
                            // Seleccionar la primera ciudad y actualizar la provincia
                            val primeraCiudad = ciudades.first()
                            updateCiudad(primeraCiudad.nombre)
                            updateProvincia(primeraCiudad.provincia)
                            
                            Timber.d("Ciudades encontradas para CP $codigoPostal: ${ciudades.size}")
                        } else {
                            // No se encontraron ciudades o hubo un error
                            _uiState.update { 
                                it.copy(
                                    ciudadesSugeridas = emptyList(),
                                    isBuscandoCiudades = false,
                                    errorBusquedaCiudades = error ?: "No se encontraron ciudades para este código postal"
                                ) 
                            }
                            
                            Timber.d("No se encontraron ciudades para CP $codigoPostal: ${error ?: "Sin detalles"}")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al buscar ciudades para CP $codigoPostal")
                _uiState.update { 
                    it.copy(
                        ciudadesSugeridas = emptyList(),
                        isBuscandoCiudades = false,
                        errorBusquedaCiudades = "Error al buscar ciudades: ${e.message ?: "Error desconocido"}"
                    ) 
                }
            }
        }
    }

    fun seleccionarCiudad(ciudad: Ciudad) {
        _uiState.update { 
            it.copy(
                ciudad = ciudad.nombre,
                ciudadError = null,
                provincia = ciudad.provincia,
                provinciaError = null
            ) 
        }
        
        // Forzar la actualización de coordenadas inmediatamente
        viewModelScope.launch {
            // Pequeño retraso para permitir que el estado se actualice
            kotlinx.coroutines.delay(100)
            actualizarCoordenadas()
        }
    }

    fun updateCiudad(ciudad: String) {
        val error = if (ciudad.isBlank()) "La ciudad es obligatoria" else null
        _uiState.update { it.copy(ciudad = ciudad, ciudadError = error) }
        
        // Actualizar coordenadas cuando se cambia la ciudad
        actualizarCoordenadas()
    }

    fun updateProvincia(provincia: String) {
        val error = if (provincia.isBlank()) "La provincia es obligatoria" else null
        _uiState.update { it.copy(provincia = provincia, provinciaError = error) }
        
        // Actualizar coordenadas cuando se cambia la provincia
        actualizarCoordenadas()
    }

    fun updateTelefono(telefono: String) {
        val error = when {
            telefono.isNotBlank() && !isValidTelefono(telefono) -> "El formato del teléfono no es válido"
            else -> null
        }
        _uiState.update { it.copy(telefono = telefono, telefonoError = error) }
    }

    // Añadir estos métodos para gestionar los usuarios administradores de centro

    /**
     * Actualiza las coordenadas geográficas basadas en la dirección actual
     */
    private fun actualizarCoordenadas() {
        val state = _uiState.value
        
        // Solo intentamos geocodificar si tenemos información completa de la dirección
        if (state.calle.isBlank() || state.numero.isBlank() || state.ciudad.isBlank() || state.codigoPostal.isBlank() || state.provincia.isBlank()) {
            return
        }
        
        // Construir la dirección completa para geocodificar
        val direccion = buildString {
            append(state.calle)
            if (state.numero.isNotBlank()) {
                append(" ")
                append(state.numero)
            }
            append(", ")
            if (state.codigoPostal.isNotBlank()) {
                append(state.codigoPostal)
                append(" ")
            }
            append(state.ciudad)
            if (state.provincia.isNotBlank()) {
                append(", ")
                append(state.provincia)
            }
            append(", España")
        }
        
        // Evitar búsquedas innecesarias si la dirección no ha cambiado
        if (direccion == state.direccionCompleta && state.tieneUbicacionValida) {
            return
        }
        
        // Marcamos el estado como si estuviera cargando la ubicación
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                // Buscar las coordenadas de la dirección
                val response = NominatimRetrofitClient.nominatimApiService.search(
                    query = direccion,
                    limit = 1
                )
                
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val place = response.body()!!.first()
                    
                    // Actualizar el estado con las coordenadas
                    _uiState.update { 
                        it.copy(
                            latitud = place.lat.toDoubleOrNull(),
                            longitud = place.lon.toDoubleOrNull(),
                            direccionCompleta = direccion,
                            mostrarMapa = true,
                            isLoading = false
                        ) 
                    }
                    
                    Timber.d("Coordenadas actualizadas: ${place.lat}, ${place.lon} para $direccion")
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    Timber.d("No se encontraron coordenadas para la dirección: $direccion")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                Timber.e(e, "Error al geocodificar dirección: $direccion")
            }
        }
    }
    
    /**
     * Alterna la visibilidad del mapa
     */
    fun toggleMapa() {
        _uiState.update { it.copy(mostrarMapa = !it.mostrarMapa) }
    }

    // Función auxiliar para validar complejidad de contraseña
    private fun containsLetterAndNumber(password: String): Boolean {
        return password.matches(".*[0-9].*".toRegex()) &&
                password.matches(".*[a-zA-Z].*".toRegex())
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

    fun saveCentro(centro: Centro) {
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Verificar que tenemos al menos un administrador
                if (_uiState.value.adminCentro.isEmpty()) {
                    throw Exception("Debe existir al menos un administrador de centro")
                }
                
                // Obtener el administrador principal (el primero)
                val adminPrincipal = _uiState.value.adminCentro.first()
                
                // Verificar si el centro ya existe por nombre (solo para nuevos centros)
                if (centro.id.isBlank()) {
                    val centroExistente = centroRepository.getCentroByNombre(centro.nombre)
                    if (centroExistente is Result.Success && centroExistente.data != null) {
                        throw Exception("Ya existe un centro con este nombre. Por favor, utilice otro nombre.")
                    }
                }
                
                // Verificar si el administrador ya existe por DNI o correo electrónico
                for (admin in _uiState.value.adminCentro) {
                    val usuarioExistente = usuarioRepository.getUsuarioByEmail(admin.email)
                    if (usuarioExistente is Result.Success && usuarioExistente.data != null) {
                        throw Exception("Ya existe un usuario con el correo electrónico ${admin.email}. Por favor, utilice otro correo.")
                    }
                    
                    val usuarioExistenteDni = usuarioRepository.getUsuarioByDni(admin.dni)
                    if (usuarioExistenteDni is Result.Success && usuarioExistenteDni.data != null) {
                        throw Exception("Ya existe un usuario con el DNI ${admin.dni}. Por favor, utilice otro DNI.")
                    }
                }
                
                var resultId: String? = null

                // Determinamos si es una actualización o un nuevo centro
                if (centro.id.isBlank()) {
                    // Si es un nuevo centro, primero creamos la cuenta en Firebase Auth
                    val authResult = centroRepository.createUserWithEmailAndPassword(
                        adminPrincipal.email,
                        adminPrincipal.password
                    )

                    when (authResult) {
                        is Result.Success -> {
                            // Hemos creado la cuenta de usuario correctamente, ahora añadimos el centro
                            val dbResult = centroRepository.addCentro(centro)
                            
                            when (dbResult) {
                                is Result.Success -> {
                                    resultId = dbResult.data
                                    
                                    // Crear los usuarios administradores de centro
                                    val adminIds = crearAdministradoresCentro(resultId)
                                    
                                    // Actualizar el centro con los IDs de los administradores
                                    if (adminIds.isNotEmpty()) {
                                        val centroConAdmins = centro.copy(adminIds = adminIds)
                                        centroRepository.updateCentro(centroConAdmins)
                                    }
                                }
                                is Result.Error -> {
                                    // Si falla la creación del centro, intentamos eliminar la cuenta de usuario
                                    centroRepository.deleteUser(authResult.data)
                                    throw dbResult.exception
                                }
                                is Result.Loading -> { /* No debería ocurrir */ }
                            }
                        }
                        is Result.Error -> {
                            throw authResult.exception
                        }
                        is Result.Loading -> { /* No debería ocurrir */ }
                    }
                } else {
                    // Si es una actualización, simplemente actualizamos el centro
                    val updateResult = centroRepository.updateCentro(centro)
                    
                    when (updateResult) {
                        is Result.Success -> {
                            resultId = centro.id
                        }
                        is Result.Error -> {
                            throw updateResult.exception
                        }
                        is Result.Loading -> { /* No debería ocurrir */ }
                    }
                }

                // Si todo ha ido bien, actualizamos el estado
                if (resultId != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            success = true
                        )
                    }
                    
                    Timber.d("Centro guardado correctamente con ID: $resultId")
                } else {
                    throw Exception("No se ha obtenido un ID para el centro")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al guardar el centro: ${e.message}"
                    )
                }
                
                Timber.e(e, "Error al guardar centro")
            }
        }
    }

    /**
     * Borra un centro educativo y todos sus datos asociados
     */
    fun deleteCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                Timber.d("Iniciando eliminación del centro: $centroId")
                
                // Usamos el nuevo método que elimina todo en un proceso transaccional
                val deleteResult = centroRepository.deleteCentroCompleto(centroId)
                
                if (deleteResult is Result.Success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            success = true
                        )
                    }
                    Timber.d("Centro eliminado correctamente junto con todos sus datos asociados: $centroId")
                } else if (deleteResult is Result.Error) {
                    throw deleteResult.exception
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar el centro: ${e.message}"
                    )
                }
                Timber.e(e, "Error al eliminar centro $centroId")
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        // Validar nombre del centro
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

        // Validar teléfono general del centro (opcional)
        if (currentState.telefono.isNotBlank() && !isValidTelefono(currentState.telefono)) {
            _uiState.update { it.copy(telefonoError = "El formato del teléfono no es válido") }
            isValid = false
        }

        // Validar administradores
        if (currentState.adminCentro.isEmpty()) {
            _uiState.update { 
                it.copy(adminCentroError = "Debe existir al menos un administrador de centro") 
            }
            isValid = false
        } else {
            // Validar los datos del administrador principal (index 0)
            val adminPrincipal = currentState.adminCentro.firstOrNull()
            if (adminPrincipal != null) {
                if (adminPrincipal.dni.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(dniError = "El DNI es obligatorio") 
                    }
                    isValid = false
                } else if (!isDniValid(adminPrincipal.dni)) {
                    updateAdminCentroField(0) { 
                        it.copy(dniError = "El DNI no es válido") 
                    }
                    isValid = false
                }
                
                if (adminPrincipal.nombre.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(nombreError = "El nombre es obligatorio") 
                    }
                    isValid = false
                }
                
                if (adminPrincipal.apellidos.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(apellidosError = "Los apellidos son obligatorios") 
                    }
                    isValid = false
                }
                
                if (adminPrincipal.email.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(emailError = "El email es obligatorio") 
                    }
                    isValid = false
                } else if (!isValidEmail(adminPrincipal.email)) {
                    updateAdminCentroField(0) { 
                        it.copy(emailError = "El formato del email no es válido") 
                    }
                    isValid = false
                }
                
                if (adminPrincipal.telefono.isNotBlank() && !isValidTelefono(adminPrincipal.telefono)) {
                    updateAdminCentroField(0) { 
                        it.copy(telefonoError = "El formato del teléfono no es válido") 
                    }
                    isValid = false
                }
                
                if (adminPrincipal.password.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(passwordError = "La contraseña es obligatoria") 
                    }
                    isValid = false
                } else if (adminPrincipal.password.length < 8) {
                    updateAdminCentroField(0) { 
                        it.copy(passwordError = "La contraseña debe tener al menos 8 caracteres") 
                    }
                    isValid = false
                } else if (!isPasswordComplex(adminPrincipal.password)) {
                    updateAdminCentroField(0) { 
                        it.copy(passwordError = "La contraseña debe incluir letras y números") 
                    }
                    isValid = false
                }
            }
            
            // Validar el resto de administradores
            currentState.adminCentro.forEachIndexed { index, admin ->
                if (index > 0) { // Saltamos el principal que ya se validó
                    val indexFinal = index // Para capturar el índice correcto en las lambdas
                    
                    if (admin.dni.isBlank()) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(dniError = "El DNI es obligatorio") 
                        }
                        isValid = false
                    } else if (!isDniValid(admin.dni)) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(dniError = "El DNI no es válido") 
                        }
                        isValid = false
                    }
                    
                    if (admin.nombre.isBlank()) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(nombreError = "El nombre es obligatorio") 
                        }
                        isValid = false
                    }
                    
                    if (admin.apellidos.isBlank()) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(apellidosError = "Los apellidos son obligatorios") 
                        }
                        isValid = false
                    }
                    
                    if (admin.email.isBlank()) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(emailError = "El email es obligatorio") 
                        }
                        isValid = false
                    } else if (!isValidEmail(admin.email)) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(emailError = "El formato del email no es válido") 
                        }
                        isValid = false
                    }
                    
                    if (admin.telefono.isNotBlank() && !isValidTelefono(admin.telefono)) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(telefonoError = "El formato del teléfono no es válido") 
                        }
                        isValid = false
                    }
                    
                    if (admin.password.isBlank()) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(passwordError = "La contraseña es obligatoria") 
                        }
                        isValid = false
                    } else if (admin.password.length < 8) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(passwordError = "La contraseña debe tener al menos 8 caracteres") 
                        }
                        isValid = false
                    } else if (!isPasswordComplex(admin.password)) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(passwordError = "La contraseña debe incluir letras y números") 
                        }
                        isValid = false
                    }
                }
            }
        }

        return isValid
    }

    // Definición de AddCentroState
    data class AddCentroState(
        val id: String = "",
        val nombre: String = "",
        val calle: String = "",
        val numero: String = "",
        val codigoPostal: String = "",
        val ciudad: String = "",
        val provincia: String = "",
        val telefono: String = "",
        val adminCentro: List<AdminCentroUsuario> = emptyList(),
        val latitud: Double? = null,
        val longitud: Double? = null,
        val direccionCompleta: String = "",
        val mostrarMapa: Boolean = false,
        val ciudadesSugeridas: List<Ciudad> = emptyList(),
        val isBuscandoCiudades: Boolean = false,
        val errorBusquedaCiudades: String? = null,
        val nombreError: String? = null,
        val calleError: String? = null,
        val numeroError: String? = null,
        val codigoPostalError: String? = null,
        val ciudadError: String? = null,
        val provinciaError: String? = null,
        val telefonoError: String? = null,
        val adminCentroError: String? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    ) {
        val tieneUbicacionValida: Boolean
            get() = latitud != null && longitud != null
    }

    private fun createCentroFromState(state: AddCentroState): Centro {
        val direccion = Direccion(
            calle = state.calle,
            numero = state.numero,
            codigoPostal = state.codigoPostal,
            ciudad = state.ciudad,
            provincia = state.provincia
        )

        val contacto = Contacto(
            telefono = state.telefono,
            email = state.adminCentro.firstOrNull()?.email ?: ""
        )

        return Centro(
            id = state.id.ifBlank { UUID.randomUUID().toString() },
            nombre = state.nombre,
            direccion = direccion,
            contacto = contacto,
            latitud = state.latitud ?: 0.0,
            longitud = state.longitud ?: 0.0,
            adminIds = emptyList() // Se llenará después de crear los administradores
        )
    }

    // Funciones de validación
    private fun isValidCodigoPostal(codigoPostal: String): Boolean {
        return codigoPostal.matches(Regex("^\\d{5}$"))
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

    /**
     * Añade un nuevo administrador de centro a la lista
     */
    fun addAdminCentro() {
        val currentAdmins = _uiState.value.adminCentro
        _uiState.update { 
            it.copy(adminCentro = currentAdmins + AdminCentroUsuario()) 
        }
    }
    
    /**
     * Elimina un administrador de centro de la lista
     */
    fun removeAdminCentro(index: Int) {
        val currentAdmins = _uiState.value.adminCentro.toMutableList()
        if (currentAdmins.size > 1 && index >= 0 && index < currentAdmins.size) {
            currentAdmins.removeAt(index)
            _uiState.update { 
                it.copy(adminCentro = currentAdmins) 
            }
        } else if (currentAdmins.size <= 1) {
            // No permitimos eliminar si es el único administrador
            _uiState.update {
                it.copy(adminCentroError = "Debe haber al menos un administrador de centro")
            }
        }
    }
    
    /**
     * Actualiza los datos de un administrador de centro
     */
    fun updateAdminCentroDni(index: Int, dni: String) {
        updateAdminCentroField(index) { it.copy(
            dni = dni,
            dniError = if (dni.isBlank()) "El DNI es obligatorio" else if (!isDniValid(dni)) "DNI no válido" else null
        )}
    }
    
    fun updateAdminCentroNombre(index: Int, nombre: String) {
        updateAdminCentroField(index) { it.copy(
            nombre = nombre,
            nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null
        )}
    }
    
    fun updateAdminCentroApellidos(index: Int, apellidos: String) {
        updateAdminCentroField(index) { it.copy(
            apellidos = apellidos,
            apellidosError = if (apellidos.isBlank()) "Los apellidos son obligatorios" else null
        )}
    }
    
    fun updateAdminCentroEmail(index: Int, email: String) {
        updateAdminCentroField(index) { it.copy(
            email = email,
            emailError = if (email.isBlank()) "El email es obligatorio" else if (!isValidEmail(email)) "Email no válido" else null
        )}
    }
    
    fun updateAdminCentroTelefono(index: Int, telefono: String) {
        updateAdminCentroField(index) { it.copy(
            telefono = telefono,
            telefonoError = if (telefono.isBlank()) "El teléfono es obligatorio" else if (!isValidTelefono(telefono)) "Teléfono no válido" else null
        )}
    }
    
    fun updateAdminCentroPassword(index: Int, password: String) {
        updateAdminCentroField(index) { it.copy(
            password = password,
            passwordError = if (password.isBlank()) "La contraseña es obligatoria" else if (password.length < 8) "La contraseña debe tener al menos 8 caracteres" else if (!isPasswordComplex(password)) "La contraseña debe incluir letras y números" else null
        )}
    }
    
    /**
     * Función auxiliar para actualizar un campo específico de un admin de centro
     */
    private fun updateAdminCentroField(index: Int, update: (AdminCentroUsuario) -> AdminCentroUsuario) {
        if (index < 0 || index >= _uiState.value.adminCentro.size) return
        
        val adminList = _uiState.value.adminCentro.toMutableList()
        adminList[index] = update(adminList[index])
        
        _uiState.update { it.copy(adminCentro = adminList) }
    }

    /**
     * Verifica si un DNI es válido
     */
    private fun isDniValid(dni: String): Boolean {
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        return dniPattern.matches(dni.uppercase())
    }

    /**
     * Crea los usuarios administradores de centro
     */
    private suspend fun crearAdministradoresCentro(centroId: String): List<String> {
        val adminIds = mutableListOf<String>()
        
        for (admin in _uiState.value.adminCentro) {
            try {
                // Crear usuario en Firebase Auth
                val authResult = usuarioRepository.crearUsuarioConEmailYPassword(
                    admin.email, 
                    admin.password
                )
                
                when (authResult) {
                    is Result.Success -> {
                        // Crear perfil de administrador de centro
                        val perfil = Perfil(
                            tipo = TipoUsuario.ADMIN_CENTRO,
                            centroId = centroId,
                            verificado = true
                        )
                        
                        // Crear usuario en Firestore
                        val usuario = Usuario(
                            dni = admin.dni,
                            email = admin.email,
                            nombre = admin.nombre,
                            apellidos = admin.apellidos,
                            telefono = admin.telefono,
                            perfiles = listOf(perfil)
                        )
                        
                        // Guardar usuario en Firestore
                        val saveResult = usuarioRepository.guardarUsuario(usuario)
                        
                        if (saveResult is Result.Success) {
                            adminIds.add(admin.dni)
                            Timber.d("Administrador de centro creado: ${admin.dni}")
                        } else if (saveResult is Result.Error) {
                            // Si falla guardar el usuario, intentamos eliminar la cuenta de Firebase Auth
                            usuarioRepository.borrarUsuario(authResult.data)
                            Timber.e(saveResult.exception, "Error al guardar administrador: ${admin.dni}")
                        }
                    }
                    is Result.Error -> {
                        Timber.e(authResult.exception, "Error al crear cuenta para administrador: ${admin.dni}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al crear administrador: ${admin.dni}")
            }
        }
        
        return adminIds
    }
}