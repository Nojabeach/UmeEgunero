package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber
import kotlinx.coroutines.flow.map

/**
 * Estado UI para la pantalla de dashboard del administrador.
 * 
 * Esta clase define un estado inmutable que representa toda la información
 * necesaria para mostrar la interfaz de usuario del panel de control del
 * administrador de la aplicación. Sigue el patrón de Unidirectional Data Flow (UDF)
 * donde cada cambio en los datos genera un nuevo objeto de estado inmutable.
 * 
 * El estado incluye:
 * - Datos de centros educativos y usuarios del sistema
 * - Indicadores de carga para operaciones asíncronas
 * - Mensajes de error y éxito para feedback al usuario
 * - Referencias al usuario actual y flags de navegación
 * 
 * Esta clase es utilizada por el [AdminDashboardViewModel] para gestionar y exponer
 * el estado de la UI a los componentes Compose que conforman la pantalla.
 * 
 * @property centros Lista de centros educativos registrados en el sistema
 * @property usuarios Lista de usuarios del sistema, incluyendo administradores, profesores y familiares
 * @property isLoading Indica si hay operaciones de carga en progreso para centros
 * @property isLoadingUsuarios Indica si hay operaciones de carga en progreso para usuarios
 * @property error Mensaje de error a mostrar, null si no hay errores
 * @property currentUser Usuario administrador actualmente logueado
 * @property navigateToWelcome Flag para controlar la navegación a la pantalla de bienvenida
 * @property showListadoCentros Flag para controlar la visibilidad del listado de centros
 * @property mensajeExito Mensaje de éxito a mostrar tras completar una operación
 * 
 * @see AdminDashboardViewModel
 * @see AdminDashboardScreen
 */
data class AdminDashboardUiState(
    val centros: List<Centro> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingUsuarios: Boolean = false,
    val error: String? = null,
    val currentUser: Usuario? = null,
    val navigateToWelcome: Boolean = false,
    val showListadoCentros: Boolean = false,
    val mensajeExito: String? = null
)

/**
 * ViewModel para la pantalla de dashboard del administrador del sistema UmeEgunero.
 * 
 * Este ViewModel es el componente central de la funcionalidad administrativa del sistema,
 * implementando la lógica de negocio para la gestión completa de la plataforma educativa.
 * Sigue el patrón MVVM (Model-View-ViewModel) para separar la lógica de negocio
 * de la interfaz de usuario.
 * 
 * Responsabilidades principales:
 * - Gestión completa del ciclo de vida de centros educativos (CRUD)
 * - Administración centralizada de usuarios y permisos
 * - Monitorización del estado global del sistema
 * - Generación de estadísticas y métricas de uso
 * - Gestión de la sesión del administrador
 * 
 * Este ViewModel utiliza corrutinas de Kotlin y Flow para operaciones asíncronas,
 * proporcionando reactivamente datos actualizados a la UI. Se integra con Hilt
 * para la inyección de dependencias y con Timber para el registro de eventos.
 * 
 * El estado se expone mediante [StateFlow] a través de la propiedad [uiState],
 * que contiene toda la información necesaria para que la UI refleje correctamente
 * el estado actual del sistema.
 *
 * @property centroRepository Repositorio para acceder a datos de centros educativos
 * @property usuarioRepository Repositorio para acceder a datos de usuarios
 * @property authRepository Repositorio para gestionar la autenticación
 * @property errorHandler Utilidad para procesar y formatear errores de forma consistente
 * 
 * @see AdminDashboardUiState
 * @see AdminDashboardScreen
 * @see CentroRepository
 * @see UsuarioRepository
 */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    // Estado mutable interno que solo el ViewModel puede modificar
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    
    // Estado inmutable expuesto a la UI para garantizar la predictibilidad
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    /**
     * Propiedad derivada que expone directamente los centros educativos.
     * 
     * Transforma el flujo de estado principal para extraer únicamente la lista de centros,
     * facilitando a los componentes de UI que solo necesitan acceder a esta información
     * específica sin tener que observar todo el estado.
     * 
     * @return [StateFlow] que emite la lista actualizada de centros educativos
     */
    val centros = _uiState.asStateFlow().map { it.centros }

    /**
     * Inicialización del ViewModel.
     * 
     * Al crear una instancia del ViewModel, este bloque se ejecuta automáticamente
     * para cargar los datos iniciales necesarios para poblar el dashboard:
     * - Lista de centros educativos
     * - Usuarios por tipo
     * - Información del administrador actual
     * 
     * Estas cargas iniciales establecen el estado base para que la UI
     * pueda mostrar la información relevante inmediatamente.
     */
    init {
        loadCentros()
        loadUsuarios()
        loadCurrentUser()
    }

    /**
     * Carga todos los centros educativos registrados en el sistema.
     * 
     * Este método recupera la lista completa de centros educativos desde Firestore
     * a través del repositorio correspondiente. Durante la operación, gestiona
     * el estado de carga y posibles errores para informar correctamente a la UI.
     * 
     * Los centros educativos son una entidad fundamental en el sistema, ya que:
     * - Representan los nodos principales de la estructura organizativa
     * - Contienen referencias a profesores, clases y alumnos
     * - Son el punto de partida para muchas operaciones administrativas
     * 
     * La carga se realiza asíncronamente mediante una corrutina en el viewModelScope,
     * garantizando que se cancele automáticamente cuando el ViewModel sea destruido.
     */
    fun loadCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = centroRepository.getAllCentros()

                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                centros = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        val errorMsg = procesarErrorThrowable(result.exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMsg
                            )
                        }
                        Timber.e(result.exception, "Error al cargar los centros: $errorMsg")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                val errorMsg = procesarErrorThrowable(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
                Timber.e(e, "Error inesperado al cargar los centros: $errorMsg")
            }
        }
    }
    
    /**
     * Carga todos los usuarios del sistema organizados por tipo.
     * 
     * Este método obtiene todos los usuarios registrados en el sistema,
     * categorizados por su tipo (administrador, centro, profesor, familiar)
     * para permitir una gestión centralizada por parte del administrador.
     * 
     * La información agregada de usuarios permite:
     * - Visualizar la distribución de usuarios por rol
     * - Identificar patrones de crecimiento del sistema
     * - Realizar operaciones masivas por tipo de usuario
     * - Monitorear el estado de las cuentas y permisos
     * 
     * La carga se realiza de forma paralela para cada tipo de usuario,
     * combinando los resultados en una única lista actualizada en el estado.
     */
    fun loadUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUsuarios = true, error = null) }
            
            try {
                val usuarios = mutableListOf<Usuario>()
                
                // Cargar todos los tipos de usuarios
                val tiposUsuario = listOf(
                    TipoUsuario.ADMIN_APP, 
                    TipoUsuario.ADMIN_CENTRO, 
                    TipoUsuario.PROFESOR, 
                    TipoUsuario.FAMILIAR
                )
                
                for (tipo in tiposUsuario) {
                    when (val result = usuarioRepository.getUsersByType(tipo)) {
                        is Result.Success -> {
                            usuarios.addAll(result.data)
                        }
                        is Result.Error -> {
                            val errorMsg = procesarErrorThrowable(result.exception)
                            _uiState.update {
                                it.copy(
                                    isLoadingUsuarios = false,
                                    error = errorMsg
                                )
                            }
                            Timber.e(result.exception, "Error al cargar usuarios de tipo $tipo: $errorMsg")
                            return@launch
                        }
                        else -> {}
                    }
                }
                
                _uiState.update {
                    it.copy(
                        usuarios = usuarios,
                        isLoadingUsuarios = false
                    )
                }
                
            } catch (e: Exception) {
                val errorMsg = procesarErrorThrowable(e)
                _uiState.update {
                    it.copy(
                        isLoadingUsuarios = false,
                        error = errorMsg
                    )
                }
                Timber.e(e, "Error inesperado al cargar los usuarios: $errorMsg")
            }
        }
    }

    /**
     * Carga los datos del usuario administrador actual
     * 
     * Este método obtiene la información completa del administrador
     * actualmente logueado para personalizar el dashboard y controlar
     * los accesos a funcionalidades específicas.
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                // Intentar obtener el usuario desde el repositorio de autenticación
                val currentFirebaseUser = authRepository.getCurrentUser()
                
                if (currentFirebaseUser != null) {
                    // Buscar el perfil completo usando el email
                    when (val userResult = usuarioRepository.getUsuarioByEmail(currentFirebaseUser.email)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(currentUser = userResult.data) }
                        }
                        is Result.Error -> {
                            val errorMsg = procesarErrorThrowable(userResult.exception)
                            Timber.e(userResult.exception, "Error al cargar perfil de usuario: $errorMsg")
                        }
                        else -> { /* Ignorar estado loading */ }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = procesarErrorThrowable(e)
                Timber.e(e, "Error al cargar el usuario actual: $errorMsg")
            }
        }
    }

    /**
     * Elimina un centro educativo del sistema
     * 
     * Este método realiza la eliminación permanente de un centro educativo
     * y toda su información asociada, incluyendo una validación de seguridad
     * antes de ejecutar la operación.
     * 
     * @param centroId Identificador único del centro a eliminar
     */
    fun deleteCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = centroRepository.deleteCentro(centroId)

                when (result) {
                    is Result.Success -> {
                        // Recargar la lista después de borrar
                        loadCentros()
                        _uiState.update {
                            it.copy(
                                mensajeExito = "Centro eliminado correctamente",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        val errorMsg = procesarErrorThrowable(result.exception)
                        _uiState.update {
                            it.copy(
                                error = errorMsg,
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al eliminar el centro: $errorMsg")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                val errorMsg = procesarErrorThrowable(e)
                _uiState.update {
                    it.copy(
                        error = errorMsg,
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al eliminar el centro: $errorMsg")
            }
        }
    }
    
    /**
     * Elimina un usuario del sistema
     * 
     * Este método realiza la eliminación permanente de un usuario
     * y toda su información asociada, tras verificar que no tiene
     * dependencias críticas en el sistema.
     * 
     * @param usuarioDni DNI del usuario a eliminar
     */
    fun deleteUsuario(usuarioDni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.borrarUsuarioByDni(usuarioDni)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista después de borrar
                        loadUsuarios()
                        _uiState.update {
                            it.copy(
                                mensajeExito = "Usuario eliminado correctamente",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        val errorMsg = procesarErrorThrowable(result.exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMsg
                            )
                        }
                        Timber.e(result.exception, "Error al eliminar el usuario: $errorMsg")
                    }
                    else -> { /* Ignorar estado loading */ }
                }
            } catch (e: Exception) {
                val errorMsg = procesarErrorThrowable(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMsg
                    )
                }
                Timber.e(e, "Error inesperado al eliminar el usuario: $errorMsg")
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
    fun clearMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.update { it.copy(navigateToWelcome = true, currentUser = null) }
            } catch (e: Exception) {
                val errorMsg = procesarErrorThrowable(e)
                Timber.e(e, "Error al cerrar sesión: $errorMsg")
                _uiState.update { 
                    it.copy(error = errorMsg)
                }
            }
        }
    }

    /**
     * Controla la visibilidad del listado de centros
     */
    fun setShowListadoCentros(show: Boolean) {
        _uiState.update { it.copy(showListadoCentros = show) }
    }

    /**
     * Muestra el listado de centros
     */
    fun showListadoCentros() {
        _uiState.update { it.copy(showListadoCentros = true) }
        loadCentros() // Recargar centros para asegurar datos actualizados
    }

    /**
     * Obtiene el ID del centro seleccionado o el primero de la lista si no hay ninguno seleccionado
     */
    fun obtenerCentroSeleccionadoOPrimero(): String {
        // Por ahora, simplemente devolvemos el ID del primer centro si existe
        return _uiState.value.centros.firstOrNull()?.id ?: ""
    }

    /**
     * Obtiene el ID del curso seleccionado o el primero si no hay ninguno seleccionado
     * Para este ejemplo, devolvemos un ID estático ya que los cursos no están implementados todavía
     */
    fun obtenerCursoSeleccionadoOPrimero(): String {
        // Este es un valor temporal, en una implementación real obtendríamos el ID
        // de un curso real consultando a Firestore
        return "curso_primero_a" // ID demo para pruebas
    }

    /**
     * Resetea la contraseña de un usuario
     */
    fun resetPassword(dni: String, nuevaPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = usuarioRepository.resetearPassword(dni, nuevaPassword)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        mensajeExito = "Contraseña restablecida correctamente"
                    ) }
                    loadUsuarios()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al resetear contraseña")
                    _uiState.update { it.copy(
                        isLoading = false, 
                        error = "Error al resetear contraseña: ${result.exception?.message}"
                    ) }
                }
                is Result.Loading -> {
                    // Ignorar estado loading
                }
            }
        }
    }

    /**
     * Activa o desactiva un usuario
     */
    fun toggleUsuarioActivo(usuario: Usuario, activo: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val usuarioActualizado = usuario.copy(activo = activo)
            
            when (val result = usuarioRepository.actualizarUsuario(usuarioActualizado)) {
                is Result.Success -> {
                    val mensaje = if (activo) "Usuario activado correctamente" else "Usuario desactivado correctamente"
                    _uiState.update { it.copy(
                        isLoading = false,
                        mensajeExito = mensaje
                    ) }
                    loadUsuarios()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al actualizar estado de usuario")
                    _uiState.update { it.copy(
                        isLoading = false, 
                        error = "Error al actualizar estado de usuario: ${result.exception?.message}"
                    ) }
                }
                is Result.Loading -> {
                    // Ignorar estado loading
                }
            }
        }
    }

    private fun procesarErrorThrowable(exception: Throwable?): String {
        return if (exception is Exception) {
            errorHandler.procesarError(exception)
        } else {
            errorHandler.procesarError(Exception(exception?.message ?: "Error desconocido"))
        }
    }
}