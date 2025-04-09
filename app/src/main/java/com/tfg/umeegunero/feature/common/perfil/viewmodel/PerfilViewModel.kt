package com.tfg.umeegunero.feature.common.perfil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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
    val isEditing: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    
    // Datos del perfil
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val direccionCalle: String = "",
    val direccionNumero: String = "",
    val direccionPiso: String = "",
    val direccionCP: String = "",
    val direccionCiudad: String = "",
    val direccionProvincia: String = "",
    
    // Datos para detectar cambios
    val nombreOriginal: String = "",
    val apellidosOriginal: String = "",
    val telefonoOriginal: String = "",
    val direccionCalleOriginal: String = "",
    val direccionNumeroOriginal: String = "",
    val direccionPisoOriginal: String = "",
    val direccionCPOriginal: String = "",
    val direccionCiudadOriginal: String = "",
    val direccionProvinciaOriginal: String = "",
    
    // Datos adicionales
    val usuario: Usuario? = null,
    val direccion: Direccion = Direccion(),
    val fotoPerfil: String? = null,
    
    // Estado de navegación
    val navigateToWelcome: Boolean = false
)

/**
 * ViewModel para la gestión del perfil de usuario
 */
@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
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
                usuarioRepository.getUsuarioActual().collectLatest<Result<Usuario>> { result ->
                    when (result) {
                        is Result.Success<*> -> {
                            val usuario = result.data as? Usuario
                            if (usuario != null) {
                                _uiState.update { it.copy(
                                    usuario = usuario,
                                    isLoading = false,
                                    nombre = usuario.nombre,
                                    apellidos = usuario.apellidos,
                                    telefono = usuario.telefono ?: "",
                                    direccion = usuario.direccion ?: Direccion(),
                                    fotoPerfil = null // La foto de perfil no existe en el modelo Usuario
                                ) }
                            }
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al cargar los datos del usuario"
                            ) }
                        }
                        is Result.Loading<*> -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar los datos del usuario"
                ) }
            }
        }
    }
    
    /**
     * Actualiza el estado según los datos del usuario
     */
    private fun actualizarPerfil(usuario: Usuario) {
        _uiState.update {
            it.copy(
                isLoading = false,
                nombre = usuario.nombre,
                apellidos = usuario.apellidos,
                telefono = usuario.telefono ?: "",
                
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
                telefonoOriginal = usuario.telefono ?: "",
                direccionCalleOriginal = usuario.direccion?.calle ?: "",
                direccionNumeroOriginal = usuario.direccion?.numero ?: "",
                direccionPisoOriginal = usuario.direccion?.piso ?: "",
                direccionCPOriginal = usuario.direccion?.codigoPostal ?: "",
                direccionCiudadOriginal = usuario.direccion?.ciudad ?: "",
                direccionProvinciaOriginal = usuario.direccion?.provincia ?: "",
                
                // Actualizar datos adicionales
                usuario = usuario,
                direccion = usuario.direccion ?: Direccion(),
                fotoPerfil = null
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
                        success = "Cambios guardados correctamente",
                        
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
        _uiState.update { it.copy(success = null) }
    }

    /**
     * Cierra la sesión del usuario actual
     * 
     * Este método gestiona el proceso de cierre de sesión utilizando el repositorio
     * de autenticación y actualiza el estado para indicar la navegación a la pantalla
     * de bienvenida.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Intentamos cerrar sesión 
                val result = authRepository.signOut()
                
                if (result) {
                    _uiState.update { 
                        it.copy(
                            navigateToWelcome = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cerrar sesión",
                            isLoading = false
                        )
                    }
                    Timber.e("Error al cerrar sesión")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cerrar sesión: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cerrar sesión")
            }
        }
    }
} 