package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.model.Result
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
 * Estado UI para la pantalla de dashboard del centro educativo
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
 *
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.1
 */
data class CentroDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: Usuario? = null,
    val cursos: List<Curso> = emptyList(),
    val navigateToWelcome: Boolean = false
)

/**
 * ViewModel para la gestión del dashboard de centro educativo
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
 * @property cursoRepository Repositorio para acceder a los datos de cursos
 * @property usuarioRepository Repositorio para acceder a los datos de usuarios
 * @property authRepository Repositorio para gestionar la autenticación
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.1
 */
@HiltViewModel
class CentroDashboardViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
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
     * Inicialización del ViewModel
     * 
     * Carga automáticamente los datos necesarios para el dashboard al crearse
     * la instancia, evitando que la UI tenga que solicitar esta carga explícitamente.
     */
    init {
        loadCurrentUser()
        loadCursos()
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
            try {
                // Intentar obtener el usuario directamente del repositorio de usuario
                // usando el ID del usuario autenticado actualmente
                val currentFirebaseUser = authRepository.getCurrentUser()
                
                if (currentFirebaseUser != null) {
                    // Aquí podríamos necesitar buscar el perfil completo del usuario
                    // usando algún campo identificador como el email o ID
                    when (val userResult = usuarioRepository.getUsuarioByEmail(currentFirebaseUser.email)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(currentUser = userResult.data) }
                        }
                        is Result.Error -> {
                            Timber.e(userResult.exception, "Error al cargar perfil de usuario")
                        }
                        else -> { /* Ignorar estado loading */ }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar el usuario actual")
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
     */
    private fun loadCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En producción, se debería filtrar por centro del usuario
                when (val result = cursoRepository.getAllCursos()) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                cursos = result.data,
                                isLoading = false
                            ) 
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar los cursos"
                            )
                        }
                        Timber.e(result.exception, "Error al cargar los cursos")
                    }
                    else -> { /* Ignorar estado loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los cursos"
                    )
                }
                Timber.e(e, "Error inesperado al cargar los cursos")
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