package com.tfg.umeegunero.feature.common.perfil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val latitud: String = "",
    val longitud: String = "",
    
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
    val latitudOriginal: String = "",
    val longitudOriginal: String = "",
    
    // Datos adicionales
    val usuario: Usuario? = null,
    val direccion: Direccion = Direccion(),
    val fotoPerfil: String? = null,
    val loadingCiudad: Boolean = false
)

/**
 * ViewModel para la gestión del perfil de usuario
 * 
 * Incluye funcionalidades para:
 * - Cargar y editar información de perfil
 * - Autocompletar ciudad basada en el código postal
 * - Obtener coordenadas geográficas basadas en la dirección
 * - Gestionar cambios de contraseña y cierre de sesión
 *
 * @author Maitane (Estudiante 2º DAM)
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
                                    fotoPerfil = null
                                ) }
                                actualizarPerfil(usuario)
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
                
                // Coordenadas
                latitud = usuario.direccion?.latitud ?: "",
                longitud = usuario.direccion?.longitud ?: "",
                
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
                latitudOriginal = usuario.direccion?.latitud ?: "",
                longitudOriginal = usuario.direccion?.longitud ?: "",
                
                // Actualizar datos adicionales
                usuario = usuario,
                direccion = usuario.direccion ?: Direccion(),
                fotoPerfil = null
            )
        }
    }
    
    /**
     * Obtiene el nombre de la ciudad basado en el código postal
     * Simula una llamada a la API
     */
    fun obtenerCiudadPorCP(cp: String) {
        viewModelScope.launch {
            if (cp.length != 5) return@launch
            
            _uiState.update { it.copy(loadingCiudad = true) }
            
            try {
                // Simulación de API con un delay
                delay(800)
                
                // Datos de ejemplo
                val cpData = mapOf(
                    "28001" to Pair("Madrid", "Madrid"),
                    "08001" to Pair("Barcelona", "Barcelona"),
                    "46001" to Pair("Valencia", "Valencia"),
                    "41001" to Pair("Sevilla", "Sevilla"),
                    "50001" to Pair("Zaragoza", "Zaragoza"),
                    "30001" to Pair("Murcia", "Murcia"),
                    "07001" to Pair("Palma de Mallorca", "Islas Baleares"),
                    "35001" to Pair("Las Palmas de Gran Canaria", "Las Palmas"),
                    "48001" to Pair("Bilbao", "Vizcaya"),
                    "03001" to Pair("Alicante", "Alicante")
                )
                
                val (ciudad, provincia) = cpData[cp] ?: Pair("", "")
                
                if (ciudad.isNotEmpty()) {
                    _uiState.update { it.copy(
                        direccionCiudad = ciudad,
                        direccionProvincia = provincia,
                        loadingCiudad = false
                    ) }
                    
                    // Auto-obtener coordenadas si la ciudad fue encontrada
                    obtenerCoordenadasDeDireccion()
                } else {
                    _uiState.update { it.copy(loadingCiudad = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    loadingCiudad = false,
                    error = "Error al buscar la ciudad: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Obtiene las coordenadas geográficas basado en la dirección actual
     * Simula una llamada a la API de geolocalización
     */
    fun obtenerCoordenadasDeDireccion() {
        viewModelScope.launch {
            // Verifica que haya suficiente información en la dirección
            val direccion = uiState.value
            if (direccion.direccionCalle.isEmpty() || 
                direccion.direccionCiudad.isEmpty() || 
                direccion.direccionCP.isEmpty()) {
                _uiState.update { it.copy(
                    error = "Completa la dirección para obtener las coordenadas"
                ) }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Simulación de API con un delay
                delay(1000)
                
                // Generación de coordenadas de ejemplo basadas en la ciudad
                val coordenadasPorCiudad = mapOf(
                    "Madrid" to Pair("40.4168", "-3.7038"),
                    "Barcelona" to Pair("41.3851", "2.1734"),
                    "Valencia" to Pair("39.4699", "-0.3763"),
                    "Sevilla" to Pair("37.3891", "-5.9845"),
                    "Zaragoza" to Pair("41.6488", "-0.8891"),
                    "Murcia" to Pair("37.9922", "-1.1307"),
                    "Palma de Mallorca" to Pair("39.5696", "2.6502"),
                    "Las Palmas de Gran Canaria" to Pair("28.1235", "-15.4366"),
                    "Bilbao" to Pair("43.2630", "-2.9350"),
                    "Alicante" to Pair("38.3452", "-0.4815")
                )
                
                val ciudadActual = direccion.direccionCiudad
                val (latitud, longitud) = coordenadasPorCiudad[ciudadActual] ?: Pair("40.4168", "-3.7038") // Default a Madrid
                
                _uiState.update { it.copy(
                    latitud = latitud,
                    longitud = longitud,
                    isLoading = false,
                    success = "Coordenadas obtenidas correctamente"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al obtener coordenadas: ${e.message}"
                ) }
            }
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
        // Limitar CP a 5 dígitos
        val cpLimitado = if (cp.length > 5) cp.substring(0, 5) else cp
        _uiState.update { it.copy(direccionCP = cpLimitado) }
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
     * Actualiza la latitud de la ubicación
     */
    fun actualizarLatitud(latitud: String) {
        _uiState.update { it.copy(latitud = latitud) }
    }
    
    /**
     * Actualiza la longitud de la ubicación
     */
    fun actualizarLongitud(longitud: String) {
        _uiState.update { it.copy(longitud = longitud) }
    }
    
    /**
     * Guarda los cambios realizados en el perfil
     */
    fun guardarCambios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            try {
                val usuarioActual = uiState.value.usuario
                if (usuarioActual == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo obtener el usuario actual") }
                    return@launch
                }
                val direccion = Direccion(
                    calle = uiState.value.direccionCalle,
                    numero = uiState.value.direccionNumero,
                    piso = uiState.value.direccionPiso,
                    codigoPostal = uiState.value.direccionCP,
                    ciudad = uiState.value.direccionCiudad,
                    provincia = uiState.value.direccionProvincia,
                    latitud = uiState.value.latitud,
                    longitud = uiState.value.longitud
                )
                val usuarioActualizado = usuarioActual.copy(
                    nombre = uiState.value.nombre,
                    apellidos = uiState.value.apellidos,
                    telefono = uiState.value.telefono,
                    direccion = direccion
                )
                val resultado = usuarioRepository.actualizarUsuario(usuarioActualizado)
                when (resultado) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                success = "Cambios guardados correctamente",
                                usuario = usuarioActualizado,
                                nombreOriginal = usuarioActualizado.nombre,
                                apellidosOriginal = usuarioActualizado.apellidos,
                                telefonoOriginal = usuarioActualizado.telefono ?: "",
                                direccionCalleOriginal = direccion.calle ?: "",
                                direccionNumeroOriginal = direccion.numero ?: "",
                                direccionPisoOriginal = direccion.piso ?: "",
                                direccionCPOriginal = direccion.codigoPostal ?: "",
                                direccionCiudadOriginal = direccion.ciudad ?: "",
                                direccionProvinciaOriginal = direccion.provincia ?: "",
                                latitudOriginal = direccion.latitud ?: "",
                                longitudOriginal = direccion.longitud ?: ""
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = resultado.exception?.message ?: "Error al guardar cambios") }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false, error = "Error desconocido al guardar cambios") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al guardar cambios: ${e.message}") }
            }
        }
    }
    
    /**
     * Cancela la edición del perfil
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
                direccionProvincia = it.direccionProvinciaOriginal,
                latitud = it.latitudOriginal,
                longitud = it.longitudOriginal
            ) 
        }
    }
    
    /**
     * Cambia la contraseña del usuario
     */
    fun cambiarContrasena(contraseniaActual: String, contraseniaNueva: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un entorno real, aquí haríamos la llamada al repositorio
                
                // Simulación de una llamada al servicio
                delay(1000)
                
                // Simulación de éxito
                _uiState.update { it.copy(
                    isLoading = false,
                    success = "Contraseña cambiada correctamente"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al cambiar la contraseña: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario
     */
    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                // En un entorno real, aquí haríamos la llamada al repositorio
                
                // Limpiar el estado
                _uiState.update { PerfilUiState() }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al cerrar sesión: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    fun clearMensaje() {
        _uiState.update { it.copy(success = null) }
    }
} 