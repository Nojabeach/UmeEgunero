package com.tfg.umeegunero.feature.common.perfil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de perfil
 */
data class PerfilUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    
    // Datos personales
    val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaRegistro: Timestamp? = null,
    val ultimoAcceso: Timestamp? = null,
    val tipoUsuario: TipoUsuario = TipoUsuario.FAMILIAR,
    
    // Dirección
    val direccionCalle: String = "",
    val direccionNumero: String = "",
    val direccionPiso: String = "",
    val direccionCP: String = "",
    val direccionCiudad: String = "",
    val direccionProvincia: String = "",
    
    // Datos originales para cancelar edición
    val nombreOriginal: String = "",
    val apellidosOriginal: String = "",
    val telefonoOriginal: String = "",
    val direccionCalleOriginal: String = "",
    val direccionNumeroOriginal: String = "",
    val direccionPisoOriginal: String = "",
    val direccionCPOriginal: String = "",
    val direccionCiudadOriginal: String = "",
    val direccionProvinciaOriginal: String = ""
)

/**
 * ViewModel para la gestión del perfil de usuario
 */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    /**
     * Carga los datos del perfil del usuario actual
     */
    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                usuarioRepository.getUsuarioActual().collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            result.data?.let { usuario ->
                                actualizarPerfil(usuario)
                            } ?: run {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "No se encontró información del usuario"
                                    )
                                }
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Error al cargar el perfil: ${result.exception.message}"
                                )
                            }
                            Timber.e(result.exception, "Error al cargar perfil")
                        }
                        is Result.Loading -> {
                            // Este estado ya se maneja al iniciar
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
                Timber.e(e, "Error inesperado al cargar perfil")
            }
        }
    }
    
    /**
     * Actualiza el estado UI con los datos del usuario
     */
    private fun actualizarPerfil(usuario: Usuario) {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                dni = usuario.dni,
                nombre = usuario.nombre,
                apellidos = usuario.apellidos,
                email = usuario.email,
                telefono = usuario.telefono,
                fechaRegistro = usuario.fechaRegistro,
                ultimoAcceso = usuario.ultimoAcceso,
                tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.FAMILIAR,
                
                // Dirección
                direccionCalle = usuario.direccion?.calle ?: "",
                direccionNumero = usuario.direccion?.numero ?: "",
                direccionPiso = usuario.direccion?.piso ?: "",
                direccionCP = usuario.direccion?.codigoPostal ?: "",
                direccionCiudad = usuario.direccion?.ciudad ?: "",
                direccionProvincia = usuario.direccion?.provincia ?: "",
                
                // Guardar valores originales
                nombreOriginal = usuario.nombre,
                apellidosOriginal = usuario.apellidos,
                telefonoOriginal = usuario.telefono,
                direccionCalleOriginal = usuario.direccion?.calle ?: "",
                direccionNumeroOriginal = usuario.direccion?.numero ?: "",
                direccionPisoOriginal = usuario.direccion?.piso ?: "",
                direccionCPOriginal = usuario.direccion?.codigoPostal ?: "",
                direccionCiudadOriginal = usuario.direccion?.ciudad ?: "",
                direccionProvinciaOriginal = usuario.direccion?.provincia ?: ""
            )
        }
    }
    
    /**
     * Actualiza el nombre del usuario
     */
    fun actualizarNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }
    
    /**
     * Actualiza los apellidos del usuario
     */
    fun actualizarApellidos(apellidos: String) {
        _uiState.update { it.copy(apellidos = apellidos) }
    }
    
    /**
     * Actualiza el teléfono del usuario
     */
    fun actualizarTelefono(telefono: String) {
        _uiState.update { it.copy(telefono = telefono) }
    }
    
    /**
     * Actualiza la calle de la dirección
     */
    fun actualizarDireccionCalle(calle: String) {
        _uiState.update { it.copy(direccionCalle = calle) }
    }
    
    /**
     * Actualiza el número de la dirección
     */
    fun actualizarDireccionNumero(numero: String) {
        _uiState.update { it.copy(direccionNumero = numero) }
    }
    
    /**
     * Actualiza el piso de la dirección
     */
    fun actualizarDireccionPiso(piso: String) {
        _uiState.update { it.copy(direccionPiso = piso) }
    }
    
    /**
     * Actualiza el código postal de la dirección
     */
    fun actualizarDireccionCP(cp: String) {
        _uiState.update { it.copy(direccionCP = cp) }
    }
    
    /**
     * Actualiza la ciudad de la dirección
     */
    fun actualizarDireccionCiudad(ciudad: String) {
        _uiState.update { it.copy(direccionCiudad = ciudad) }
    }
    
    /**
     * Actualiza la provincia de la dirección
     */
    fun actualizarDireccionProvincia(provincia: String) {
        _uiState.update { it.copy(direccionProvincia = provincia) }
    }
    
    /**
     * Guarda los cambios realizados en el perfil
     */
    fun guardarCambios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un entorno real, aquí haríamos la llamada al repositorio
                // Crear el objeto direccion
                val direccion = Direccion(
                    calle = uiState.value.direccionCalle,
                    numero = uiState.value.direccionNumero,
                    piso = uiState.value.direccionPiso,
                    codigoPostal = uiState.value.direccionCP,
                    ciudad = uiState.value.direccionCiudad,
                    provincia = uiState.value.direccionProvincia
                )
                
                // Simular guardado exitoso
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Cambios guardados correctamente",
                        
                        // Actualizar valores originales
                        nombreOriginal = it.nombre,
                        apellidosOriginal = it.apellidos,
                        telefonoOriginal = it.telefono,
                        direccionCalleOriginal = it.direccionCalle,
                        direccionNumeroOriginal = it.direccionNumero,
                        direccionPisoOriginal = it.direccionPiso,
                        direccionCPOriginal = it.direccionCP,
                        direccionCiudadOriginal = it.direccionCiudad,
                        direccionProvinciaOriginal = it.direccionProvincia
                    ) 
                }
                
                Timber.d("Perfil actualizado correctamente")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al guardar cambios: ${e.message}"
                    ) 
                }
                Timber.e(e, "Error al guardar cambios en el perfil")
            }
        }
    }
    
    /**
     * Cancela la edición y restaura los valores originales
     */
    fun cancelarEdicion() {
        _uiState.update { 
            it.copy(
                nombre = it.nombreOriginal,
                apellidos = it.apellidosOriginal,
                telefono = it.telefonoOriginal,
                direccionCalle = it.direccionCalleOriginal,
                direccionNumero = it.direccionNumeroOriginal,
                direccionPiso = it.direccionPisoOriginal,
                direccionCP = it.direccionCPOriginal,
                direccionCiudad = it.direccionCiudadOriginal,
                direccionProvincia = it.direccionProvinciaOriginal
            ) 
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Limpia el mensaje actual
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 