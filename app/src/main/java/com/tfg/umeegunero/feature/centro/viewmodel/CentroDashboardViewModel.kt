package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de dashboard del centro educativo.
 * 
 * Esta clase representa el estado completo de la interfaz de usuario para
 * la gestión a nivel de centro educativo, conteniendo toda la información
 * necesaria para renderizar correctamente la pantalla del dashboard.
 * 
 * Sigue el patrón de UI State en Jetpack Compose, donde el estado se mantiene
 * de forma inmutable y cada cambio genera un nuevo objeto de estado.
 * 
 * @property isLoading Indica si hay operaciones de carga en progreso
 * @property error Mensaje de error a mostrar, null si no hay errores
 * @property currentUser Usuario administrador del centro actual
 * @property cursos Lista de cursos pertenecientes al centro educativo
 * @property navigateToWelcome Flag para controlar la navegación a la pantalla de bienvenida
 * @property nombreCentro Nombre del centro educativo a mostrar en el dashboard
 * @property centroId ID del centro educativo asociado al usuario actual
 *
 * @see CentroDashboardViewModel
 * @see CentroDashboardScreen
 */
data class CentroDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: Usuario? = null,
    val cursos: List<Curso> = emptyList(),
    val navigateToWelcome: Boolean = false,
    val nombreCentro: String = "Centro Educativo",
    val centroId: String = ""
)

/**
 * ViewModel para la gestión del dashboard de centro educativo.
 * 
 * Este ViewModel implementa la lógica de negocio relacionada con la administración
 * a nivel de centro educativo, proporcionando datos y funcionalidades para:
 * - Visualización de información global del centro
 * - Gestión de cursos y clases
 * - Configuración general del centro
 * - Monitorización de actividades y estadísticas
 * 
 * Utiliza el patrón MVVM junto con Flows para exponer el estado de forma reactiva
 * y corrutinas para manejar operaciones asíncronas.
 * 
 * ## Características principales
 * - Gestión del estado de la UI mediante [StateFlow]
 * - Carga automática de datos al inicializar
 * - Manejo de errores y estados de carga
 * - Integración con múltiples repositorios
 * 
 * ## Flujo de datos
 * 1. Inicialización y carga del usuario actual
 * 2. Obtención del centro asociado
 * 3. Carga de cursos y datos relacionados
 * 4. Actualización del estado UI
 * 
 * @constructor Crea una instancia del ViewModel con las dependencias necesarias
 * @param cursoRepository Repositorio para acceder a los datos de cursos
 * @param usuarioRepository Repositorio para acceder a los datos de usuarios
 * @param authRepository Repositorio para gestionar la autenticación
 * @param centroRepository Repositorio para acceder a los datos de centros
 * 
 * @see CentroDashboardUiState
 * @see CentroDashboardScreen
 */
@HiltViewModel
class CentroDashboardViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val centroRepository: CentroRepository
) : ViewModel() {
    // Estado mutable internamente para modificaciones dentro del ViewModel
    private val _uiState = MutableStateFlow(CentroDashboardUiState())
    
    // Estado inmutable expuesto a la UI siguiendo el principio de encapsulamiento
    val uiState: StateFlow<CentroDashboardUiState> = _uiState.asStateFlow()
    
    /**
     * Propiedad derivada que expone directamente los cursos para facilitar
     * su uso en la UI sin necesidad de acceder a todo el estado
     */
    val cursos = _uiState.asStateFlow().map { it.cursos }
    
    /**
     * Propiedad derivada que expone el nombre del centro educativo
     * para facilitar su uso en la UI
     */
    val nombreCentro = _uiState.asStateFlow().map { it.nombreCentro }
    
    /**
     * Inicialización del ViewModel
     * 
     * Carga automáticamente los datos necesarios para el dashboard al crearse
     * la instancia, evitando que la UI tenga que solicitar esta carga explícitamente.
     */
    init {
        loadCurrentUser()
    }
    
    /**
     * Carga los datos del usuario actual (administrador del centro)
     * 
     * Este método obtiene la información completa del administrador de centro
     * logueado actualmente a través del repositorio de usuarios, utilizando
     * el email como identificador.
     * 
     * La información del usuario es fundamental para:
     * - Personalizar el dashboard
     * - Filtrar acciones según sus permisos
     * - Proporcionar contexto sobre qué centro se está gestionando
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Intentar obtener el usuario directamente del repositorio de usuario
                // usando el ID del usuario autenticado actualmente
                val currentFirebaseUser = authRepository.getCurrentUser()
                
                if (currentFirebaseUser != null) {
                    // Aquí podríamos necesitar buscar el perfil completo del usuario
                    // usando algún campo identificador como el email o ID
                    when (val userResult = usuarioRepository.getUsuarioByEmail(currentFirebaseUser.email)) {
                        is Result.Success -> {
                            val usuario = userResult.data
                            // Buscar el perfil ADMIN_CENTRO para obtener el centroId
                            val perfilCentro = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                            val centroId = perfilCentro?.centroId ?: ""
                            
                            if (centroId.isNotEmpty()) {
                                // Cargar los datos del centro
                                when (val centroResult = centroRepository.getCentroById(centroId)) {
                                    is Result.Success -> {
                                        val centro = centroResult.data
                                        _uiState.update { it.copy(
                                            currentUser = usuario,
                                            nombreCentro = centro.nombre,
                                            centroId = centroId,
                                            isLoading = false
                                        ) }
                                        
                                        // Una vez que tenemos el centroId, cargamos los cursos
                                        loadCursos(centroId)
                                    }
                                    is Result.Error -> {
                                        Timber.e(centroResult.exception, "Error al cargar datos del centro")
                                        _uiState.update { it.copy(
                                            error = "No se pudo cargar la información del centro",
                                            isLoading = false
                                        ) }
                                    }
                                    else -> { /* Ignorar estado loading */ }
                                }
                            } else {
                                // No hay perfil de centro o no tiene centroId
                                _uiState.update { it.copy(
                                    currentUser = usuario,
                                    error = "El usuario no tiene un centro asignado",
                                    isLoading = false
                                ) }
                            }
                        }
                        is Result.Error -> {
                            Timber.e(userResult.exception, "Error al cargar perfil de usuario")
                            _uiState.update { it.copy(
                                error = "Error al cargar el perfil de usuario",
                                isLoading = false
                            ) }
                        }
                        else -> { /* Ignorar estado loading */ }
                    }
                } else {
                    _uiState.update { it.copy(
                        error = "No hay usuario autenticado",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar el usuario actual")
                _uiState.update { it.copy(
                    error = e.message ?: "Error al cargar datos del usuario",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga todos los cursos del centro educativo
     * 
     * Este método recupera la lista completa de cursos disponibles en el
     * centro educativo, lo que proporciona una visión general de la estructura
     * académica del centro y permite acceder a información más detallada.
     * 
     * Los cursos son la base para:
     * - Organizar clases y grupos
     * - Asignar profesores y alumnos
     * - Estructurar el contenido académico
     * - Generar informes y estadísticas
     * 
     * @param centroId ID del centro del cual cargar los cursos
     */
    private fun loadCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                if (centroId.isNotEmpty()) {
                    // Usar la función suspendida que devuelve directamente la lista
                    val cursosList = cursoRepository.obtenerCursosPorCentro(centroId)
                    Timber.d("Cursos cargados: ${cursosList.size}")
                    _uiState.update { it.copy(cursos = cursosList, isLoading = false) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No hay centroId disponible"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los cursos"
                    )
                }
                Timber.e(e, "Error inesperado al cargar los cursos")
            } finally {
                 _uiState.update { it.copy(isLoading = false) } // Asegurar que isLoading se ponga a false
            }
        }
    }
    
    /**
     * Limpia el mensaje de error actual
     * 
     * Este método restablece el estado de error a null, generalmente
     * después de que el error ha sido mostrado al usuario o cuando
     * se inicia una nueva operación.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Cierra la sesión del usuario actual
     * 
     * Este método ejecuta el proceso de logout mediante el repositorio
     * de autenticación y actualiza el estado para redirigir al usuario
     * a la pantalla de bienvenida/login.
     * 
     * Se asegura de:
     * - Eliminar tokens de autenticación
     * - Limpiar el estado actual
     * - Configurar la navegación hacia la pantalla inicial
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.update { it.copy(navigateToWelcome = true, currentUser = null) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cerrar sesión")
                _uiState.update { 
                    it.copy(error = e.message ?: "Error al cerrar sesión")
                }
            }
        }
    }
} 