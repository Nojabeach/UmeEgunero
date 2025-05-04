package com.tfg.umeegunero.feature.auth.viewmodel

import android.content.SharedPreferences
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.SolicitudRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de login.
 * 
 * Esta clase encapsula todos los estados posibles de la interfaz de usuario
 * para la pantalla de inicio de sesión, siguiendo el patrón de State Hoisting
 * recomendado para Jetpack Compose.
 * 
 * El state hoisting es un patrón de diseño en el que el estado se eleva (o "hoists")
 * a un nivel superior en la jerarquía de componentes, de forma que podamos
 * separar la lógica de negocio de la interfaz de usuario.
 * 
 * @property email Email introducido por el usuario
 * @property password Contraseña introducida por el usuario
 * @property emailError Error de validación del email (null si es válido)
 * @property passwordError Error de validación de la contraseña (null si es válida)
 * @property isLoading Indica si se está procesando la solicitud de inicio de sesión
 * @property error Mensaje de error general (null si no hay error)
 * @property success Indica si el inicio de sesión fue exitoso
 * @property userType Tipo de usuario que ha iniciado sesión
 * @property solicitudPendiente Indica si el familiar tiene una solicitud pendiente
 * @property solicitudId ID de la solicitud pendiente (si existe)
 * @property necesitaPermisoNotificaciones Indica si el familiar necesita configurar permisos
 * 
 * @author Estudiante 2º DAM
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val userType: TipoUsuario? = null,
    val solicitudPendiente: Boolean = false,
    val solicitudId: String = "",
    val necesitaPermisoNotificaciones: Boolean = false
) {
    /**
     * Propiedad calculada que indica si el botón de login debe estar habilitado.
     * 
     * El botón de login estará habilitado solo si ambos campos tienen contenido
     * y no hay errores de validación.
     */
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() &&
                emailError == null && passwordError == null
}

/**
 * ViewModel para la pantalla de inicio de sesión en la aplicación UmeEgunero.
 * 
 * Este ViewModel implementa la lógica de autenticación para el sistema UmeEgunero,
 * gestionando todo el proceso de inicio de sesión para los diferentes tipos de usuarios
 * (administradores, centros educativos, profesores y familiares).
 * 
 * Responsabilidades principales:
 * - Validación de credenciales en tiempo real (email y contraseña)
 * - Autenticación contra Firebase Authentication
 * - Verificación de permisos según tipo de usuario
 * - Manejo de errores de autenticación
 * - Persistencia de credenciales para inicio de sesión automático
 * 
 * El ViewModel expone un [StateFlow] inmutable [uiState] que contiene el estado actual
 * de la interfaz de usuario, siguiendo el patrón de Unidirectional Data Flow (UDF).
 * 
 * Se integra con Hilt para la inyección de dependencias, recibiendo el repositorio
 * de usuarios y las preferencias compartidas necesarias para su funcionamiento.
 * 
 * ## Seguridad
 * 
 * Implementa las siguientes medidas de seguridad:
 * - Validación de credenciales en tiempo real
 * - Persistencia segura de credenciales
 * - Manejo de sesiones
 * - Verificación de tipos de usuario
 * 
 * ## Gestión de Estado
 * 
 * Utiliza un estado inmutable que contiene:
 * - Credenciales del usuario
 * - Errores de validación
 * - Estado de carga
 * - Tipo de usuario autenticado
 * 
 * @property usuarioRepository Repositorio para operaciones de autenticación
 * @property solicitudRepository Repositorio para operaciones de solicitudes
 * @property sharedPreferences Preferencias para persistencia de credenciales
 * @property uiState Estado actual de la UI
 * 
 * @see LoginUiState
 * @see UsuarioRepository
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val solicitudRepository: SolicitudRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    // Constantes para las preferencias
    companion object {
        private const val PREF_SAVED_EMAIL = "saved_email"
        private const val PREF_REMEMBER_USER = "remember_user"
        private const val PREF_IS_ADMIN_APP = "is_admin_app"
    }

    // Estado de la UI expuesto como StateFlow inmutable
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Al inicializar el ViewModel, comprobamos si hay credenciales guardadas
        val savedEmail = sharedPreferences.getString(PREF_SAVED_EMAIL, "")
        if (!savedEmail.isNullOrEmpty()) {
            updateEmail(savedEmail)
        }
    }

    /**
     * Actualiza el email en el estado y valida su formato.
     * 
     * Esta función implementa validación en tiempo real mientras el usuario escribe,
     * utilizando un patrón regex estándar para verificar que el formato del email
     * sea correcto. El resultado de la validación se almacena en el estado UI.
     * 
     * @param email Nuevo valor del email introducido por el usuario
     */
    fun updateEmail(email: String) {
        val emailError = if (email.isNotBlank() && !isValidEmail(email)) {
            "Email inválido"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                email = email,
                emailError = emailError
            )
        }
    }

    /**
     * Actualiza la contraseña en el estado y valida su longitud.
     * 
     * Implementa validación en tiempo real para asegurar que la contraseña
     * cumple con los requisitos mínimos de seguridad (6 caracteres).
     * El resultado de la validación se almacena en el estado UI.
     * 
     * @param password Nuevo valor de la contraseña introducida por el usuario
     */
    fun updatePassword(password: String) {
        val passwordError = if (password.isNotBlank() && password.length < 6) {
            "La contraseña debe tener al menos 6 caracteres"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                password = password,
                passwordError = passwordError
            )
        }
    }

    /**
     * Valida el formato del email utilizando el patrón estándar de Android.
     * 
     * @param email Email a validar
     * @return true si el email tiene un formato válido, false en caso contrario
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Comprueba si hay credenciales guardadas y las carga
     */
    fun checkSavedCredentials() {
        val savedEmail = sharedPreferences.getString(PREF_SAVED_EMAIL, "")
        val rememberUser = sharedPreferences.getBoolean(PREF_REMEMBER_USER, false)
        
        if (!savedEmail.isNullOrEmpty() && rememberUser) {
            updateEmail(savedEmail)
            _uiState.update {
                it.copy(
                    email = savedEmail
                )
            }
        }
    }

    /**
     * Obtiene el estado actual de la preferencia de recordar usuario
     * 
     * @return true si el usuario ha marcado la opción de recordar credenciales, false en caso contrario
     */
    fun getRememberUserPreference(): Boolean {
        return sharedPreferences.getBoolean(PREF_REMEMBER_USER, false)
    }

    /**
     * Actualiza la preferencia de recordar usuario
     */
    fun updateRememberUser(remember: Boolean) {
        sharedPreferences.edit()
            .putBoolean(PREF_REMEMBER_USER, remember)
            .apply()
    }

    /**
     * Valida las credenciales para habilitar el botón de login
     */
    fun validateCredentials() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        
        if (email.isNotBlank() && password.isNotBlank() && 
            isValidEmail(email) && password.length >= 6) {
            _uiState.update {
                it.copy(
                    emailError = null,
                    passwordError = null
                )
            }
        }
    }

    /**
     * Persiste las credenciales del usuario en las preferencias compartidas.
     * 
     * Utilizado cuando el usuario marca la opción "Recordarme" para facilitar
     * futuros inicios de sesión sin tener que volver a introducir las credenciales.
     * 
     * @param email Email del usuario a guardar
     */
    private fun saveUserCredentials(email: String) {
        sharedPreferences.edit()
            .putString(PREF_SAVED_EMAIL, email)
            .putBoolean(PREF_REMEMBER_USER, true)
            .apply()
    }

    /**
     * Elimina las credenciales guardadas de las preferencias compartidas.
     * 
     * Utilizado cuando el usuario desmarca la opción "Recordarme" o
     * cuando se cierra sesión explícitamente.
     */
    private fun clearSavedCredentials() {
        sharedPreferences.edit()
            .remove(PREF_SAVED_EMAIL)
            .putBoolean(PREF_REMEMBER_USER, false)
            .apply()
    }

    /**
     * Inicia el proceso de autenticación con las credenciales proporcionadas.
     * 
     * Este método ejecuta el flujo completo de autenticación:
     * 1. Valida los campos de entrada (email y contraseña)
     * 2. Actualiza el estado a "cargando" (isLoading)
     * 3. Invoca al repositorio para realizar la autenticación con Firebase
     * 4. Verifica que el usuario tenga el perfil correcto para el tipo seleccionado
     * 5. Actualiza el estado según el resultado:
     *    - En caso de éxito: marca success=true y guarda credenciales si corresponde
     *    - En caso de error: almacena el mensaje de error para mostrarlo en la UI
     * 
     * La operación se ejecuta en una corrutina dentro del viewModelScope para 
     * no bloquear el hilo principal de la aplicación.
     * 
     * @param userType Tipo de usuario que intenta iniciar sesión (ADMIN_APP, ADMIN_CENTRO, PROFESOR, FAMILIAR)
     * @param rememberUser Si se debe recordar al usuario para futuros inicios de sesión
     */
    fun login(userType: TipoUsuario, rememberUser: Boolean = false) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        // Validar campos antes de enviar
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

        if (email.isBlank()) {
            emailError = "El email es obligatorio"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = "Email inválido"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "La contraseña es obligatoria"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (!isValid) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        // Iniciar proceso de login
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                Timber.d("Iniciando proceso de login para email: $email y tipo de usuario: $userType")
                val result = usuarioRepository.iniciarSesion(email, password)

                when (result) {
                    is Result.Success -> {
                        val usuarioId = result.data
                        Timber.d("Login exitoso en Firebase Auth, obteniendo datos de usuario con ID: $usuarioId")

                        // Obtener datos completos del usuario
                        val usuarioResult = usuarioRepository.getUsuarioPorDni(usuarioId)
                        if (usuarioResult is Result.Success) {
                            val usuario = usuarioResult.data
                            Timber.d("Datos de usuario obtenidos: ${usuario.email}, nombre: ${usuario.nombre}")

                            // Verificar que el usuario tiene el tipo adecuado de perfil
                            val tipoUsuarioFirebase = userType

                            val perfiles = usuario.perfiles
                            Timber.d("Perfiles del usuario (${perfiles.size}): ${perfiles.map { it.tipo }}")
                            
                            val isAdminApp = usuario.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP }
                            sharedPreferences.edit()
                                .putBoolean(PREF_IS_ADMIN_APP, isAdminApp)
                                .apply()

                            val tienePerfil = perfiles.any { it.tipo == tipoUsuarioFirebase }
                            Timber.d("¿Tiene perfil $tipoUsuarioFirebase? $tienePerfil")

                            if (tienePerfil) {
                                // Si es familiar, verificamos si tiene solicitudes pendientes
                                if (tipoUsuarioFirebase == TipoUsuario.FAMILIAR) {
                                    // Verificar si necesita aceptar permisos de notificación
                                    val tienePermisoPendiente = usuario.preferenciasNotificacion?.permisoPendiente ?: false
                                    
                                    if (tienePermisoPendiente) {
                                        // Si necesita configurar notificaciones, guardamos credenciales si es necesario
                                        // pero redirigimos a la pantalla de permisos
                                        if (rememberUser) {
                                            saveUserCredentials(email)
                                        } else {
                                            clearSavedCredentials()
                                        }
                                        
                                        _uiState.update {
                                            it.copy(
                                                isLoading = false,
                                                necesitaPermisoNotificaciones = true,
                                                success = false,
                                                userType = userType
                                            )
                                        }
                                        
                                        Timber.d("Familiar necesita configurar permisos de notificación")
                                        return@launch
                                    }
                                    
                                    when (val solicitudResult = solicitudRepository.tieneSolicitudesPendientes(usuario.dni)) {
                                        is Result.Success -> {
                                            val tieneSolicitudesPendientes = solicitudResult.data
                                            
                                            if (tieneSolicitudesPendientes) {
                                                // Obtener las solicitudes para mostrar el ID
                                                val solicitudesResult = solicitudRepository.getSolicitudesByFamiliarId(usuario.dni)
                                                var solicitudId = ""
                                                
                                                if (solicitudesResult is Result.Success) {
                                                    // Buscar una solicitud pendiente
                                                    val solicitudPendiente = solicitudesResult.data.find { 
                                                        it.estado == EstadoSolicitud.PENDIENTE 
                                                    }
                                                    solicitudId = solicitudPendiente?.id ?: ""
                                                }
                                                
                                                // Si el usuario seleccionó recordar usuario, guardamos el email
                                                if (rememberUser) {
                                                    saveUserCredentials(email)
                                                } else {
                                                    // Si no quiere recordar, borramos credenciales guardadas
                                                    clearSavedCredentials()
                                                }
                                                
                                                // Marcar que tiene solicitud pendiente para mostrar mensaje
                                                _uiState.update {
                                                    it.copy(
                                                        isLoading = false,
                                                        solicitudPendiente = true,
                                                        solicitudId = solicitudId,
                                                        userType = userType,
                                                        success = false
                                                    )
                                                }
                                                
                                                Timber.d("Familiar con solicitud pendiente, ID: $solicitudId")
                                                return@launch
                                            }
                                        }
                                        is Result.Error -> {
                                            Timber.e(solicitudResult.exception, "Error al verificar solicitudes pendientes")
                                        }
                                        is Result.Loading -> {
                                            // En caso de que estemos cargando, aunque no debería ocurrir en este caso
                                            Timber.d("Cargando información de solicitudes")
                                        }
                                    }
                                }
                                
                                // Si el usuario seleccionó recordar usuario, guardamos el email
                                if (rememberUser) {
                                    saveUserCredentials(email)
                                } else {
                                    // Si no quiere recordar, borramos credenciales guardadas
                                    clearSavedCredentials()
                                }
                                
                                // Login exitoso
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        success = true,
                                        userType = userType
                                    )
                                }
                                Timber.d("Login exitoso para $email como $userType")
                            } else {
                                // No tiene el perfil adecuado
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "No tienes permisos para acceder como $userType. Por favor, verifica tus credenciales."
                                    )
                                }
                                // Cerrar sesión ya que no tiene los permisos correctos
                                usuarioRepository.cerrarSesion()
                                Timber.d("Usuario sin perfil $userType: $email")
                            }
                        } else if (usuarioResult is Result.Error) {
                            Timber.e(usuarioResult.exception, "Error al obtener datos de usuario")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Error al obtener datos de usuario. Por favor, intenta de nuevo."
                                )
                            }
                            // Cerrar sesión ya que no pudimos obtener los datos
                            usuarioRepository.cerrarSesion()
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error en el login")
                        val errorMessage = result.exception?.message ?: "Error al iniciar sesión. Por favor, verifica tus credenciales."
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                    }
                    else -> {
                        Timber.e("Estado inesperado en el login")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error inesperado al iniciar sesión. Por favor, intenta de nuevo."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error general en el proceso de login")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado. Por favor, intenta de nuevo."
                    )
                }
            }
        }
    }

    /**
     * Limpia los errores
     */
    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    /**
     * Reinicia el estado
     */
    fun resetState() {
        _uiState.update {
            LoginUiState()
        }
    }

    /**
     * Permite consultar si el usuario autenticado es admin app (persistente en sesión)
     */
    fun isAdminApp(): Boolean {
        return sharedPreferences.getBoolean(PREF_IS_ADMIN_APP, false)
    }
}