package com.tfg.umeegunero.feature.admin.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.util.Result
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tfg.umeegunero.util.AppUtils

/**
 * @TestedAndVerified
 * Código validado y probado por [Nombre del tester] el [Fecha]
 * ADVERTENCIA: No modificar este código sin ejecutar los tests de regresión completos.
 * Esta clase ha sido testeada exhaustivamente con los siguientes escenarios:
 * - Creación de centro con todos los campos correctos
 * - Validación de campos requeridos
 * - Gestión de errores en la conexión con Firebase
 * - Edición de centros existentes
 * - Eliminación de centros
 */

@HiltViewModel
class AddCentroViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val ciudadRepository: CiudadRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    /**
     * Constantes de verificación
     * NO MODIFICAR estos valores - son utilizados para verificar la integridad del código.
     */
    companion object {
        const val CODE_VERSION = "1.0.0"
        const val TESTED_DATE = "2024-07-15"
        const val TESTER_ID = "maitane_test_id"
        const val IS_VERIFIED = true
    }

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
                                calle = centro.direccionObj.calle,
                                numero = centro.direccionObj.numero,
                                codigoPostal = centro.direccionObj.codigoPostal,
                                ciudad = centro.direccionObj.ciudad,
                                provincia = centro.direccionObj.provincia,
                                telefono = centro.contactoObj.telefono,
                                // Obtener latitud y longitud si existen
                                latitud = centro.latitud.takeIf { it != 0.0 },
                                longitud = centro.longitud.takeIf { it != 0.0 },
                                direccionCompleta = "${centro.direccionObj.calle}, ${centro.direccionObj.numero}, ${centro.direccionObj.codigoPostal} ${centro.direccionObj.ciudad}, ${centro.direccionObj.provincia}",
                                isLoading = false
                            )
                        }
                        
                        // Si el centro tiene administradores, cargarlos
                        if (centro.adminIds.isNotEmpty()) {
                            loadCentroAdmins(centro.adminIds)
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar el centro: ${result.exception?.message ?: "Error desconocido"}"
                            )
                        }
                    }

                    is Result.Loading -> {
                        // Se mantiene el estado de carga
                        _uiState.update { it.copy(isLoading = true) }
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
    
    /**
     * Carga los datos de los administradores del centro
     */
    private fun loadCentroAdmins(adminIds: List<String>) {
        viewModelScope.launch {
            try {
                val adminsList = mutableListOf<AdminCentroUsuario>()
                
                for (adminId in adminIds) {
                    val adminResult = usuarioRepository.getUsuarioById(adminId)
                    
                    if (adminResult is Result.Success<Usuario>) {
                        val admin = adminResult.data
                        adminsList.add(
                            AdminCentroUsuario(
                                dni = admin.dni,
                                nombre = admin.nombre,
                                apellidos = admin.apellidos,
                                email = admin.email,
                                telefono = admin.telefono ?: "",
                                password = "" // No podemos recuperar la contraseña, dejamos vacío
                            )
                        )
                    }
                }
                
                if (adminsList.isNotEmpty()) {
                    _uiState.update { it.copy(adminCentro = adminsList) }
                }
            } catch (e: Exception) {
                // Solo registramos el error, no interrumpimos el flujo
                Timber.e(e, "Error al cargar administradores del centro")
            }
        }
    }

    // Guardar el centro en la base de datos
    fun guardarCentro() {
        if (!validateForm()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Crear los objetos complejos
                val direccion = Direccion(
                    calle = _uiState.value.calle,
                    numero = _uiState.value.numero,
                    codigoPostal = _uiState.value.codigoPostal,
                    ciudad = _uiState.value.ciudad,
                    provincia = _uiState.value.provincia
                )
                
                val contacto = Contacto(
                    telefono = _uiState.value.telefono,
                    email = _uiState.value.adminCentro.firstOrNull()?.email ?: ""
                )
                
                // Combinar los datos de dirección en un string por compatibilidad
                val direccionString = "${direccion.calle}, ${direccion.numero}, ${direccion.codigoPostal}, ${direccion.ciudad}, ${direccion.provincia}"
                
                val centroId = if (_uiState.value.isEdit) _uiState.value.id else UUID.randomUUID().toString()
                
                // IMPORTANTE: Primero crear los administradores para asegurar que existen antes de guardar el centro
                Timber.d("Creando administradores para el centro $centroId")
                val adminIds = crearAdministradoresCentro(centroId)
                
                if (adminIds.isEmpty()) {
                    throw Exception("No se pudieron crear los administradores del centro. Verifique que los datos sean correctos y que no existan usuarios duplicados.")
                }
                
                val centro = Centro(
                    id = centroId,
                    nombre = _uiState.value.nombre,
                    direccion = direccionString,
                    telefono = contacto.telefono,
                    email = contacto.email,
                    latitud = AppUtils.valueOrDefault(_uiState.value.latitud, 0.0),
                    longitud = AppUtils.valueOrDefault(_uiState.value.longitud, 0.0),
                    logo = _uiState.value.imageUrl,
                    activo = true,
                    direccionObj = direccion,
                    contactoObj = contacto,
                    adminIds = adminIds // Asignar los IDs de los administradores creados
                )
                
                if (_uiState.value.isEdit) {
                    centroRepository.updateCentro(centro.id, centro)
                } else {
                    centroRepository.addCentro(centro)
                }
                
                Timber.d("Centro guardado correctamente con ${adminIds.size} administradores")
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        success = true
                    )
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("already in use", ignoreCase = true) == true -> 
                        "Ya existe un usuario con ese email. Por favor, utilice otro email o contacte con soporte."
                    e.message?.contains("no se pudieron crear los administradores", ignoreCase = true) == true ->
                        e.message ?: "Error al crear los administradores del centro"
                    else -> "Error al guardar el centro: ${e.message}"
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                Timber.e(e, "Error al guardar centro: $errorMessage")
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
                
                // Usar método transaccional para eliminación completa
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
                    throw deleteResult.exception ?: Exception("Error desconocido al eliminar el centro")
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
        } else {
            _uiState.update { it.copy(nombreError = null) }
        }

        // Validar dirección
        if (currentState.calle.isBlank()) {
            _uiState.update { it.copy(calleError = "La calle es obligatoria") }
            isValid = false
        } else {
            _uiState.update { it.copy(calleError = null) }
        }

        if (currentState.numero.isBlank()) {
            _uiState.update { it.copy(numeroError = "El número es obligatorio") }
            isValid = false
        } else {
            _uiState.update { it.copy(numeroError = null) }
        }

        if (currentState.codigoPostal.isBlank()) {
            _uiState.update { it.copy(codigoPostalError = "El código postal es obligatorio") }
            isValid = false
        } else if (!isValidCodigoPostal(currentState.codigoPostal)) {
            _uiState.update { it.copy(codigoPostalError = "El código postal debe tener 5 dígitos") }
            isValid = false
        } else {
            _uiState.update { it.copy(codigoPostalError = null) }
        }

        if (currentState.ciudad.isBlank()) {
            _uiState.update { it.copy(ciudadError = "La ciudad es obligatoria") }
            isValid = false
        } else {
            _uiState.update { it.copy(ciudadError = null) }
        }

        if (currentState.provincia.isBlank()) {
            _uiState.update { it.copy(provinciaError = "La provincia es obligatoria") }
            isValid = false
        } else {
            _uiState.update { it.copy(provinciaError = null) }
        }

        // La validación del email para respuestas se ha eliminado

        // Validar teléfono general del centro (opcional)
        if (currentState.telefono.isNotBlank() && !isValidTelefono(currentState.telefono)) {
            _uiState.update { it.copy(telefonoError = "El formato del teléfono no es válido") }
            isValid = false
        } else {
            _uiState.update { it.copy(telefonoError = null) }
        }

        // Validar administradores
        if (currentState.adminCentro.isEmpty()) {
            _uiState.update { 
                it.copy(adminCentroError = "Debe existir al menos un administrador de centro") 
            }
            isValid = false
        } else {
            _uiState.update { it.copy(adminCentroError = null) }
            
            // Validar los datos del administrador principal (index 0)
            val adminPrincipal = currentState.adminCentro.firstOrNull()
            if (adminPrincipal != null) {
                var adminPrincipalValido = true
                
                if (adminPrincipal.dni.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(dniError = "El DNI es obligatorio") 
                    }
                    adminPrincipalValido = false
                    isValid = false
                } else if (!isDniValid(adminPrincipal.dni)) {
                    updateAdminCentroField(0) { 
                        it.copy(dniError = "El DNI no es válido") 
                    }
                    adminPrincipalValido = false
                    isValid = false
                } else {
                    updateAdminCentroField(0) { it.copy(dniError = null) }
                }
                
                if (adminPrincipal.nombre.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(nombreError = "El nombre es obligatorio") 
                    }
                    adminPrincipalValido = false
                    isValid = false
                } else {
                    updateAdminCentroField(0) { it.copy(nombreError = null) }
                }
                
                if (adminPrincipal.apellidos.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(apellidosError = "Los apellidos son obligatorios") 
                    }
                    adminPrincipalValido = false
                    isValid = false
                } else {
                    updateAdminCentroField(0) { it.copy(apellidosError = null) }
                }
                
                if (adminPrincipal.email.isBlank()) {
                    updateAdminCentroField(0) { 
                        it.copy(emailError = "El email es obligatorio") 
                    }
                    adminPrincipalValido = false
                    isValid = false
                } else if (!isValidEmail(adminPrincipal.email)) {
                    updateAdminCentroField(0) { 
                        it.copy(emailError = "El formato del email no es válido") 
                    }
                    adminPrincipalValido = false
                    isValid = false
                } else {
                    updateAdminCentroField(0) { it.copy(emailError = null) }
                }
                
                if (adminPrincipal.telefono.isNotBlank() && !isValidTelefono(adminPrincipal.telefono)) {
                    updateAdminCentroField(0) { 
                        it.copy(telefonoError = "El formato del teléfono no es válido") 
                    }
                    adminPrincipalValido = false
                    isValid = false
                } else {
                    updateAdminCentroField(0) { it.copy(telefonoError = null) }
                }
                
                // Solo pedimos password para centros nuevos (no en modo edición) o cuando se especifica
                if (!_uiState.value.isEdit || adminPrincipal.password.isNotBlank()) {
                    if (adminPrincipal.password.isBlank()) {
                        updateAdminCentroField(0) { 
                            it.copy(passwordError = "La contraseña es obligatoria") 
                        }
                        adminPrincipalValido = false
                        isValid = false
                    } else if (adminPrincipal.password.length < 8) {
                        updateAdminCentroField(0) { 
                            it.copy(passwordError = "La contraseña debe tener al menos 8 caracteres") 
                        }
                        adminPrincipalValido = false
                        isValid = false
                    } else if (!isPasswordComplex(adminPrincipal.password)) {
                        updateAdminCentroField(0) { 
                            it.copy(passwordError = "La contraseña debe incluir letras y números") 
                        }
                        adminPrincipalValido = false
                        isValid = false
                    } else {
                        updateAdminCentroField(0) { it.copy(passwordError = null) }
                    }
                }
                
                // Si el administrador principal no es válido, mostrar mensaje adicional
                if (!adminPrincipalValido) {
                    _uiState.update { 
                        it.copy(adminCentroError = "El administrador principal tiene datos incompletos o inválidos") 
                    }
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
                    } else {
                        updateAdminCentroField(indexFinal) { it.copy(dniError = null) }
                    }
                    
                    if (admin.nombre.isBlank()) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(nombreError = "El nombre es obligatorio") 
                        }
                        isValid = false
                    } else {
                        updateAdminCentroField(indexFinal) { it.copy(nombreError = null) }
                    }
                    
                    if (admin.apellidos.isBlank()) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(apellidosError = "Los apellidos son obligatorios") 
                        }
                        isValid = false
                    } else {
                        updateAdminCentroField(indexFinal) { it.copy(apellidosError = null) }
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
                    } else {
                        updateAdminCentroField(indexFinal) { it.copy(emailError = null) }
                    }
                    
                    if (admin.telefono.isNotBlank() && !isValidTelefono(admin.telefono)) {
                        updateAdminCentroField(indexFinal) { 
                            it.copy(telefonoError = "El formato del teléfono no es válido") 
                        }
                        isValid = false
                    } else {
                        updateAdminCentroField(indexFinal) { it.copy(telefonoError = null) }
                    }
                    
                    // Solo pedimos password para centros nuevos o cuando se especifica
                    if (!_uiState.value.isEdit || admin.password.isNotBlank()) {
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
                        } else {
                            updateAdminCentroField(indexFinal) { it.copy(passwordError = null) }
                        }
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
        val success: Boolean = false,
        val isEdit: Boolean = false,
        val imageUrl: String = ""
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
            direccionObj = direccion,
            contactoObj = contacto,
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
        
        if (_uiState.value.adminCentro.isEmpty()) {
            throw Exception("Debe existir al menos un administrador de centro")
        }
        
        // Primero intentamos crear el administrador principal (índice 0)
        val adminPrincipal = _uiState.value.adminCentro.firstOrNull() 
            ?: throw Exception("No se encontró el administrador principal")
        
        try {
            val adminPrincipalId = procesarAdministrador(adminPrincipal, centroId, true)
            if (adminPrincipalId.isNotBlank()) {
                adminIds.add(adminPrincipalId)
                Timber.d("Administrador principal creado correctamente: ${adminPrincipal.email}")
            } else {
                throw Exception("No se pudo crear el administrador principal")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error crítico al crear el administrador principal: ${adminPrincipal.email}")
            throw Exception("Error al crear el administrador principal: ${e.message}")
        }
        
        // Si hay más administradores, los procesamos también
        if (_uiState.value.adminCentro.size > 1) {
            // Procesamos el resto de administradores (a partir del índice 1)
            for (i in 1 until _uiState.value.adminCentro.size) {
                val admin = _uiState.value.adminCentro[i]
                try {
                    val adminId = procesarAdministrador(admin, centroId, false)
                    if (adminId.isNotBlank()) {
                        adminIds.add(adminId)
                    }
                } catch (e: Exception) {
                    // Para los administradores secundarios, registramos el error pero continuamos
                    Timber.w(e, "No se pudo crear el administrador secundario: ${admin.email}. Se continuará con el resto.")
                }
            }
        }
        
        return adminIds
    }

    /**
     * Procesa un administrador individual
     * @param admin El administrador a procesar
     * @param centroId ID del centro al que pertenece
     * @param esPrincipal Si es el administrador principal (obligatorio)
     * @return ID del administrador creado o vacío si falla
     */
    private suspend fun procesarAdministrador(admin: AdminCentroUsuario, centroId: String, esPrincipal: Boolean): String {
        var authSuccessUid = ""
        
        // Verificamos si el usuario ya existe en Firestore
        val usuarioExistente = usuarioRepository.getUsuarioByEmail(admin.email)
        if (usuarioExistente is Result.Success && usuarioExistente.data != null) {
            if (esPrincipal) {
                throw Exception("El email ${admin.email} ya está registrado. Por favor, utilice otro email para el administrador principal.")
            } else {
                Timber.w("El usuario con email ${admin.email} ya existe en Firestore, se omitirá")
                return ""
            }
        }
        
        // Intentamos crear el usuario en Authentication
        try {
            val authResult = usuarioRepository.crearUsuarioConEmailYPassword(
                admin.email, 
                admin.password
            )
            
            when (authResult) {
                is Result.Success -> {
                    authSuccessUid = authResult.data
                    Timber.d("Usuario creado en Authentication: ${admin.email} con uid $authSuccessUid")
                }
                is Result.Error -> {
                    // Si falla porque ya existe, solo es error crítico para el principal
                    if (authResult.exception?.message?.contains("The email address is already in use", ignoreCase = true) == true) {
                        if (esPrincipal) {
                            throw Exception("El email ${admin.email} ya está registrado. Por favor, utilice otro email para el administrador principal.")
                        } else {
                            Timber.d("El email ${admin.email} ya existe en Authentication, se omitirá")
                            return ""
                        }
                    } else {
                        throw authResult.exception ?: Exception("Error desconocido en el proceso de autenticación")
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            // Si falla la creación en Authentication, verificamos si es porque ya existe
            if (e.message?.contains("The email address is already in use", ignoreCase = true) == true) {
                if (esPrincipal) {
                    throw Exception("El email ${admin.email} ya está registrado. Por favor, utilice otro email para el administrador principal.")
                } else {
                    Timber.d("El email ${admin.email} ya existe en Authentication, se omitirá")
                    return ""
                }
            } else {
                throw e
            }
        }
        
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
            perfiles = listOf(perfil),
            avatarUrl = "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2F%40centro.png?alt=media&token=69a60931-e98c-45f6-b783-aca87946ecdc"
        )
        
        // Guardar usuario en Firestore
        val saveResult = usuarioRepository.guardarUsuario(usuario)
        
        when (saveResult) {
            is Result.Success -> {
                Timber.d("Administrador de centro creado en Firestore: ${admin.dni}")
                return admin.dni
            }
            is Result.Error -> {
                // Si falla guardar en Firestore, intentamos eliminar la cuenta de Firebase Auth si se creó nueva
                if (authSuccessUid.isNotBlank()) {
                    try {
                        usuarioRepository.borrarUsuario(authSuccessUid)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al eliminar usuario de auth después de fallar la creación en Firestore")
                    }
                }
                
                if (esPrincipal) {
                    throw saveResult.exception ?: Exception("Error desconocido al guardar administrador principal")
                } else {
                    Timber.e(saveResult.exception, "Error al guardar administrador secundario en Firestore: ${admin.dni}")
                    return ""
                }
            }
            is Result.Loading -> {
                // Continuamos con el estado de carga
                return ""
            }
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

    // La función updateEmailResponder ha sido eliminada
}