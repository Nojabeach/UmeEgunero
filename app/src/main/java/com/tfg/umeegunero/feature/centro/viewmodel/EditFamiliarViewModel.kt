package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de edición de familiares
 */
data class EditFamiliarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false,
    val isEditMode: Boolean = false,
    
    // Datos personales
    val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val subtipo: SubtipoFamiliar = SubtipoFamiliar.PADRE,
    
    // Dirección
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = "",
    
    // Centro
    val centroId: String = "",
    
    // Errores de validación
    val dniError: String? = null,
    val nombreError: String? = null,
    val apellidosError: String? = null,
    val emailError: String? = null,
    val telefonoError: String? = null
)

/**
 * ViewModel para la gestión de la pantalla de edición de familiares
 */
@HiltViewModel
class EditFamiliarViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditFamiliarUiState())
    val uiState: StateFlow<EditFamiliarUiState> = _uiState.asStateFlow()
    
    /**
     * Carga los datos de un familiar existente
     */
    fun cargarFamiliar(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = usuarioRepository.getUsuarioByDni(dni)) {
                    is Result.Success -> {
                        val usuario = result.data
                        
                        if (usuario == null || usuario.perfiles.none { it.tipo == TipoUsuario.FAMILIAR }) {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "Usuario no encontrado o no es un familiar"
                                )
                            }
                            return@launch
                        }
                        
                        // Obtener el subtipo de familiar del primer perfil que lo tenga
                        val subtipo = usuario.perfiles
                            .firstOrNull { it.subtipo != null }
                            ?.subtipo ?: SubtipoFamiliar.PADRE
                        
                        // Obtener el centroId del primer perfil
                        val centroId = usuario.perfiles
                            .firstOrNull()
                            ?.centroId ?: ""
                        
                        // Actualizar el estado con los datos del usuario
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isEditMode = true,
                                dni = usuario.dni,
                                nombre = usuario.nombre,
                                apellidos = usuario.apellidos,
                                email = usuario.email ?: "",
                                telefono = usuario.telefono ?: "",
                                subtipo = subtipo,
                                centroId = centroId,
                                
                                // Datos de dirección
                                calle = usuario.direccion?.calle ?: "",
                                numero = usuario.direccion?.numero ?: "",
                                piso = usuario.direccion?.piso ?: "",
                                codigoPostal = usuario.direccion?.codigoPostal ?: "",
                                ciudad = usuario.direccion?.ciudad ?: "",
                                provincia = usuario.direccion?.provincia ?: ""
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar el familiar: ${result.exception?.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Ya estamos en estado de carga
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
                Timber.e(e, "Error al cargar familiar")
            }
        }
    }
    
    /**
     * Guarda o actualiza los datos del familiar
     */
    fun guardarFamiliar() {
        // Validar los datos antes de guardar
        if (!validarDatos()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val direccion = Direccion(
                    calle = uiState.value.calle,
                    numero = uiState.value.numero,
                    piso = uiState.value.piso,
                    codigoPostal = uiState.value.codigoPostal,
                    ciudad = uiState.value.ciudad,
                    provincia = uiState.value.provincia
                )
                
                val perfil = Perfil(
                    tipo = TipoUsuario.FAMILIAR,
                    subtipo = uiState.value.subtipo,
                    centroId = uiState.value.centroId.ifEmpty { "default_centro" },
                    verificado = true,
                    alumnos = emptyList()
                )
                
                val usuario = Usuario(
                    dni = uiState.value.dni,
                    nombre = uiState.value.nombre,
                    apellidos = uiState.value.apellidos,
                    email = uiState.value.email,
                    telefono = uiState.value.telefono,
                    direccion = direccion,
                    perfiles = listOf(perfil),
                    activo = true
                )
                
                val result = if (uiState.value.isEditMode) {
                    usuarioRepository.updateUsuario(usuario)
                } else {
                    usuarioRepository.saveUsuarioSinAuth(usuario)
                }
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                guardadoExitoso = true
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al guardar el familiar: ${result.exception?.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Ya estamos en estado de carga
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
                Timber.e(e, "Error al guardar familiar")
            }
        }
    }
    
    /**
     * Valida los datos del formulario
     */
    private fun validarDatos(): Boolean {
        var isValid = true
        
        // Validar DNI
        if (uiState.value.dni.isBlank()) {
            _uiState.update { it.copy(dniError = "El DNI es obligatorio") }
            isValid = false
        } else if (!validateDni(uiState.value.dni)) {
            _uiState.update { it.copy(dniError = "El formato del DNI no es válido") }
            isValid = false
        } else {
            _uiState.update { it.copy(dniError = null) }
        }
        
        // Validar nombre
        if (uiState.value.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre es obligatorio") }
            isValid = false
        } else {
            _uiState.update { it.copy(nombreError = null) }
        }
        
        // Validar apellidos
        if (uiState.value.apellidos.isBlank()) {
            _uiState.update { it.copy(apellidosError = "Los apellidos son obligatorios") }
            isValid = false
        } else {
            _uiState.update { it.copy(apellidosError = null) }
        }
        
        // Validar email
        if (uiState.value.email.isBlank()) {
            _uiState.update { it.copy(emailError = "El email es obligatorio") }
            isValid = false
        } else if (!validateEmail(uiState.value.email)) {
            _uiState.update { it.copy(emailError = "El formato del email no es válido") }
            isValid = false
        } else {
            _uiState.update { it.copy(emailError = null) }
        }
        
        // Validar teléfono
        if (uiState.value.telefono.isBlank()) {
            _uiState.update { it.copy(telefonoError = "El teléfono es obligatorio") }
            isValid = false
        } else if (!validateTelefono(uiState.value.telefono)) {
            _uiState.update { it.copy(telefonoError = "El formato del teléfono no es válido") }
            isValid = false
        } else {
            _uiState.update { it.copy(telefonoError = null) }
        }
        
        return isValid
    }
    
    /**
     * Valida el formato de un DNI español
     */
    private fun validateDni(dni: String): Boolean {
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        if (!dniPattern.matches(dni.uppercase())) return false
        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val numero = dni.substring(0, 8).toIntOrNull() ?: return false
        return dni.uppercase().getOrNull(8) == letras.getOrNull(numero % 23)
    }
    
    /**
     * Valida el formato de un email
     */
    private fun validateEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailPattern.matches(email)
    }
    
    /**
     * Valida el formato de un teléfono español
     */
    private fun validateTelefono(telefono: String): Boolean {
        val telefonoPattern = Regex("^[6-9]\\d{8}$")
        return telefonoPattern.matches(telefono)
    }
    
    /**
     * Actualiza el DNI en el estado
     */
    fun updateDni(dni: String) {
        _uiState.update { 
            it.copy(
                dni = dni,
                dniError = if (dni.isBlank()) "El DNI es obligatorio" else null
            )
        }
    }
    
    /**
     * Actualiza el nombre en el estado
     */
    fun updateNombre(nombre: String) {
        _uiState.update { 
            it.copy(
                nombre = nombre,
                nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null
            )
        }
    }
    
    /**
     * Actualiza los apellidos en el estado
     */
    fun updateApellidos(apellidos: String) {
        _uiState.update { 
            it.copy(
                apellidos = apellidos,
                apellidosError = if (apellidos.isBlank()) "Los apellidos son obligatorios" else null
            )
        }
    }
    
    /**
     * Actualiza el email en el estado
     */
    fun updateEmail(email: String) {
        _uiState.update { 
            it.copy(
                email = email,
                emailError = if (email.isBlank()) 
                    "El email es obligatorio" 
                else if (!validateEmail(email)) 
                    "El formato del email no es válido" 
                else 
                    null
            )
        }
    }
    
    /**
     * Actualiza el teléfono en el estado
     */
    fun updateTelefono(telefono: String) {
        _uiState.update { 
            it.copy(
                telefono = telefono,
                telefonoError = if (telefono.isBlank()) 
                    "El teléfono es obligatorio" 
                else if (!validateTelefono(telefono)) 
                    "El formato del teléfono no es válido" 
                else 
                    null
            )
        }
    }
    
    /**
     * Actualiza el subtipo de familiar en el estado
     */
    fun updateSubtipoFamiliar(subtipo: SubtipoFamiliar) {
        _uiState.update { it.copy(subtipo = subtipo) }
    }
    
    /**
     * Actualiza la calle en el estado
     */
    fun updateCalle(calle: String) {
        _uiState.update { it.copy(calle = calle) }
    }
    
    /**
     * Actualiza el número en el estado
     */
    fun updateNumero(numero: String) {
        _uiState.update { it.copy(numero = numero) }
    }
    
    /**
     * Actualiza el piso en el estado
     */
    fun updatePiso(piso: String) {
        _uiState.update { it.copy(piso = piso) }
    }
    
    /**
     * Actualiza el código postal en el estado
     */
    fun updateCodigoPostal(codigoPostal: String) {
        _uiState.update { it.copy(codigoPostal = codigoPostal) }
    }
    
    /**
     * Actualiza la ciudad en el estado
     */
    fun updateCiudad(ciudad: String) {
        _uiState.update { it.copy(ciudad = ciudad) }
    }
    
    /**
     * Actualiza la provincia en el estado
     */
    fun updateProvincia(provincia: String) {
        _uiState.update { it.copy(provincia = provincia) }
    }
    
    /**
     * Limpia el error en el estado
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 