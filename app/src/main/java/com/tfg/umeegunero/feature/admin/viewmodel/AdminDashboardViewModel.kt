package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.model.Result
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
 * Estado UI para la pantalla de dashboard del administrador
 * 
 * Esta clase define un estado inmutable que representa toda la información
 * necesaria para mostrar la interfaz de usuario del panel de control del
 * administrador de la aplicación. Contiene datos de centros, usuarios,
 * estados de carga, errores y navegación.
 * 
 * Siguiendo el patrón de UI State en Jetpack Compose, cada cambio en el estado
 * genera un nuevo objeto inmutable, facilitando el seguimiento de cambios
 * y la predictibilidad del estado de la UI.
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
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.3
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
 * ViewModel para la pantalla de dashboard del administrador
 * 
 * Este ViewModel implementa el patrón MVVM y es responsable de toda la lógica
 * de negocio relacionada con la administración global del sistema:
 * - Gestión completa de centros educativos (CRUD)
 * - Administración de usuarios de todos los tipos
 * - Monitorización del sistema completo
 * - Generación de estadísticas y reportes
 * 
 * Actúa como intermediario entre los repositorios (acceso a datos) y la UI,
 * exponiendo el estado a través de Flows reactivos y procesando eventos
 * mediante corrutinas.
 * 
 * @property centroRepository Repositorio para acceder a datos de centros educativos
 * @property usuarioRepository Repositorio para acceder a datos de usuarios
 * @property authRepository Repositorio para gestionar la autenticación
 * @property errorHandler Utilidad para procesar y formatear errores de forma consistente
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.3
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
     * Propiedad derivada que expone directamente los centros
     * 
     * Facilita a la UI el acceso a la lista de centros sin necesidad
     * de procesar todo el objeto de estado.
     */
    val centros = _uiState.asStateFlow().map { it.centros }

    /**
     * Inicialización del ViewModel
     * 
     * Ejecuta las cargas iniciales necesarias para poblar el dashboard
     * del administrador con los datos relevantes del sistema.
     */
    init {
        loadCentros()
        loadUsuarios()
        loadCurrentUser()
    }

    /**
     * Carga todos los centros educativos registrados
     * 
     * Este método recupera la lista completa de centros educativos 
     * desde el repositorio y actualiza el estado de la UI.
     * 
     * Los centros son una entidad fundamental para:
     * - Mostrar la distribución geográfica de la plataforma
     * - Acceder a detalles específicos de cada centro
     * - Ejecutar operaciones administrativas por centro
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
                        val errorMsg = errorHandler.procesarError(result.exception)
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
                val errorMsg = errorHandler.procesarError(e)
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
     * Carga todos los usuarios del sistema por tipo
     * 
     * Este método obtiene todos los usuarios registrados en el sistema,
     * categorizados por su tipo (administrador, profesor, familiar, etc.)
     * para permitir una gestión centralizada.
     * 
     * Esta funcionalidad es crítica para:
     * - Monitorizar el crecimiento de la plataforma
     * - Gestionar permisos y accesos globales
     * - Identificar patrones de uso por tipo de usuario
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
                            val errorMsg = errorHandler.procesarError(result.exception)
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
                val errorMsg = errorHandler.procesarError(e)
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
                            val errorMsg = errorHandler.procesarError(userResult.exception)
                            Timber.e(userResult.exception, "Error al cargar perfil de usuario: $errorMsg")
                        }
                        else -> { /* Ignorar estado loading */ }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = errorHandler.procesarError(e)
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
                        val errorMsg = errorHandler.procesarError(result.exception)
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
                val errorMsg = errorHandler.procesarError(e)
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
                        val errorMsg = errorHandler.procesarError(result.exception)
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
                val errorMsg = errorHandler.procesarError(e)
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
                val errorMsg = errorHandler.procesarError(e)
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
                        error = "Error al resetear contraseña: ${result.exception.message}"
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
                        error = "Error al actualizar estado de usuario: ${result.exception.message}"
                    ) }
                }
                is Result.Loading -> {
                    // Ignorar estado loading
                }
            }
        }
    }
}