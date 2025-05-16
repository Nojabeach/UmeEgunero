package com.tfg.umeegunero.feature.common.perfil.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.StorageRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val avatarUrl: String? = null,
    val loadingCiudad: Boolean = false,
    
    // Estado para forzar el tipo de usuario como administrador de aplicación
    val forzarRolAdminApp: Boolean? = null
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
    private val usuarioRepository: UsuarioRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    /**
     * Establece manualmente si el usuario es administrador de aplicación
     * Se usa para preservar el rol independientemente de lo que se obtenga de la BD
     */
    fun setIsAdminApp(isAdminApp: Boolean) {
        Timber.d("PerfilViewModel - Forzando rol admin app: $isAdminApp")
        _uiState.update { it.copy(forzarRolAdminApp = isAdminApp) }
    }

    /**
     * Determina si el usuario es administrador de aplicación
     * Usa el valor forzado si está disponible, o lo determina a partir de los perfiles
     */
    fun esAdminApp(): Boolean {
        return _uiState.value.forzarRolAdminApp ?: (_uiState.value.usuario?.perfiles?.any { it.tipo == TipoUsuario.ADMIN_APP } ?: false)
    }

    /**
     * Inicia la carga del perfil del usuario
     */
    fun cargarUsuario() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener usuario actual directamente desde AuthRepository
                val usuarioActual = authRepository.getUsuarioActual()
                
                if (usuarioActual != null) {
                    Timber.d("Usuario actual obtenido: ${usuarioActual.nombre} ${usuarioActual.apellidos}, avatar: ${usuarioActual.avatarUrl}")
                    
                    // Actualizar directamente el perfil con los datos que tenemos
                    actualizarPerfil(usuarioActual)
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No se pudo obtener el usuario actual") }
                    Timber.e("No se pudo obtener el usuario actual")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar usuario: ${e.message}") }
                Timber.e(e, "Excepción al cargar usuario actual")
            }
        }
    }

    /**
     * Actualiza el estado según los datos del usuario
     */
    private fun actualizarPerfil(usuario: Usuario) {
        // Usar la URL de avatar que viene directamente de Firestore
        val avatarUrl = usuario.avatarUrl
        
        Timber.d("Avatar URL desde Firestore: $avatarUrl")
        
        _uiState.update {
            it.copy(
                isLoading = false,
                usuario = usuario,
                avatarUrl = avatarUrl,
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
                latitud = usuario.direccion?.latitud ?: "",
                longitud = usuario.direccion?.longitud ?: "",
                
                // Datos para detectar cambios
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
                longitudOriginal = usuario.direccion?.longitud ?: ""
            )
        }
        
        // Registrar información actualizada para depuración
        Timber.d("actualizarPerfil - avatarUrl actualizado en uiState: ${_uiState.value.avatarUrl}")
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
                // Obtenemos el contexto de la aplicación
                val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                
                // Limpiar el flag de admin en SharedPreferences
                sharedPreferences.edit()
                    .remove("is_admin_app")
                    .remove("user_email")
                    .remove("remember_user")
                    .apply()
                
                // Llamar al repositorio para cerrar la sesión en Firebase
                usuarioRepository.cerrarSesion()
                
                // También desregistrar el token FCM (es buena práctica al cerrar sesión)
                try {
                    // Obtener colección de usuarios para actualizar tokens FCM
                    val usuarioActual = _uiState.value.usuario
                    if (usuarioActual != null) {
                        FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(usuarioActual.dni)
                            .update("preferencias.notificaciones.fcmTokens", mapOf<String, String>())
                            .addOnSuccessListener {
                                Timber.d("Tokens FCM limpiados correctamente al cerrar sesión")
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Error al limpiar tokens FCM al cerrar sesión")
                            }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al limpiar tokens FCM: ${e.message}")
                }
                
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
    
    /**
     * Obtiene la ciudad correspondiente al código postal proporcionado
     * @param codigoPostal Código postal a consultar
     */
    fun obtenerCiudadPorCP(codigoPostal: String) {
        // Solo procesar si el CP tiene 5 dígitos
        if (codigoPostal.length == 5) {
            viewModelScope.launch {
                _uiState.update { it.copy(loadingCiudad = true) }
                
                try {
                    // Simulación: En producción esto consultaría una API real
                    delay(500) // Simula el tiempo de respuesta de una API
                    
                    // Códigos postales de ejemplo:
                    val ciudades = mapOf(
                        "48015" to "Bilbao",
                        "48950" to "Erandio",
                        "48080" to "Bilbao",
                        "28001" to "Madrid",
                        "08001" to "Barcelona"
                    )
                    
                    // Buscar en el mapa o devolver vacío
                    val ciudad = ciudades[codigoPostal] ?: ""
                    
                    if (ciudad.isNotEmpty()) {
                        _uiState.update { 
                            it.copy(
                                direccionCiudad = ciudad,
                                loadingCiudad = false
                            ) 
                        }
                    } else {
                        _uiState.update { it.copy(loadingCiudad = false) }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener ciudad por CP: ${e.message}")
                    _uiState.update { it.copy(loadingCiudad = false) }
                }
            }
        }
    }
    
    /**
     * Obtiene las coordenadas geográficas para una dirección completa
     */
    fun obtenerCoordenadasDeDireccion() {
        viewModelScope.launch {
            val state = _uiState.value
            val direccionCompleta = "${state.direccionCalle} ${state.direccionNumero}, ${state.direccionCP} ${state.direccionCiudad}, ${state.direccionProvincia}"
            
            if (direccionCompleta.isBlank()) {
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Simulación: En producción consultaría una API de geocodificación
                delay(1000) // Simula tiempo de respuesta
                
                // Coordenadas ficticias para propósitos de demostración
                val latitud = "43.2630" // Coordenadas aproximadas para Bilbao
                val longitud = "-2.9350"
                
                _uiState.update { 
                    it.copy(
                        latitud = latitud,
                        longitud = longitud,
                        isLoading = false,
                        success = "Coordenadas obtenidas correctamente"
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener coordenadas: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al obtener coordenadas: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Sube una nueva imagen de perfil para el usuario
     * @param uri URI de la imagen a subir
     */
    fun subirAvatar(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val usuario = uiState.value.usuario ?: throw Exception("No hay usuario activo")
                
                // Ruta donde se guardará el avatar en Firebase Storage
                val rutaAlmacenamiento = "avatares"
                val nombreArchivo = "${usuario.dni}.jpg"
                
                storageRepository.subirArchivo(uri, rutaAlmacenamiento, nombreArchivo).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is Result.Success -> {
                            val urlAvatar = result.data as String
                            
                            // Actualizar el usuario en Firestore con la nueva URL
                            val usuarioActualizado = usuario.copy(avatarUrl = urlAvatar)
                            val resultadoActualizacion = usuarioRepository.actualizarUsuario(usuarioActualizado)
                            
                            when (resultadoActualizacion) {
                                is Result.Success -> {
                                    _uiState.update { 
                                        it.copy(
                                            isLoading = false,
                                            avatarUrl = urlAvatar,
                                            usuario = usuarioActualizado,
                                            success = "Imagen de perfil actualizada correctamente"
                                        ) 
                                    }
                                }
                                is Result.Error -> {
                                    _uiState.update { 
                                        it.copy(
                                            isLoading = false,
                                            error = resultadoActualizacion.exception?.message ?: "Error al actualizar imagen de perfil"
                                        ) 
                                    }
                                }
                                else -> {}
                            }
                        }
                        is Result.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = result.exception?.message ?: "Error al subir imagen de perfil"
                                ) 
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al subir imagen de perfil: ${e.message}"
                    ) 
                }
            }
        }
    }
} 