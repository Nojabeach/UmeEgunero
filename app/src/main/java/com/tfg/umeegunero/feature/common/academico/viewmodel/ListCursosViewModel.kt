package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.util.UsuarioUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de listado de cursos
 */
data class ListCursosUiState(
    val cursos: List<Curso> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val centroSeleccionado: Centro? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val centroId: String = "",
    val isAdminApp: Boolean = false // Indica si el usuario es administrador de aplicación
)

/**
 * ViewModel para la pantalla de listado de cursos
 * Gestiona la carga, actualización y eliminación de cursos
 */
@HiltViewModel
class ListCursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListCursosUiState())
    val uiState: StateFlow<ListCursosUiState> = _uiState.asStateFlow()
    
    init {
        // Verificar el tipo de usuario actual y obtener su centro
        viewModelScope.launch {
            // Obtener usuario actual
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                val userId = currentUser.documentId
                
                // Verificar si es administrador de aplicación
                val esAdminApp = verificarSiEsAdminApp(userId)
                _uiState.update { it.copy(isAdminApp = esAdminApp) }
                
                if (!esAdminApp) {
                    // Si no es admin de app, obtener su centro asignado
                    // Usar el método centralizado para obtener el centroId
                    val centroId = UsuarioUtils.obtenerCentroIdDelUsuarioActual(authRepository, usuarioRepository)
                    if (!centroId.isNullOrEmpty()) {
                        _uiState.update { it.copy(centroId = centroId) }
                        
                        // Cargar información del centro
                        val centro = obtenerCentroPorId(centroId)
                        if (centro != null) {
                            _uiState.update { it.copy(centroSeleccionado = centro) }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Verifica si el usuario es administrador de la aplicación
     */
    private suspend fun verificarSiEsAdminApp(usuarioId: String): Boolean {
        return try {
            // Obtener perfil completo del usuario
            val result = usuarioRepository.getUsuarioById(usuarioId)
            if (result is Result.Success) {
                // Verificar si tiene perfil de ADMIN_APP
                result.data.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP }
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar si es admin app: ${e.message}")
            false
        }
    }
    
    /**
     * Obtiene un centro por su ID
     */
    private suspend fun obtenerCentroPorId(centroId: String): Centro? {
        return try {
            when (val result = centroRepository.getCentroById(centroId)) {
                is Result.Success -> {
                    val centro = result.data
                    centro
                }
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centro: ${e.message}")
            null
        }
    }
    
    /**
     * Carga todos los centros disponibles (solo para admin app)
     */
    fun cargarCentros() {
        if (!_uiState.value.isAdminApp) return // Solo permitir a admin app
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = centroRepository.getAllCentros()) {
                    is Result.Success -> {
                        val centros = result.data
                        _uiState.update { 
                            it.copy(
                                centros = centros,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar centros")
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar centros: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Estado de carga ya establecido
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error general al cargar centros: ${e.message}")
                _uiState.update { 
                    it.copy(
                        error = "Error general al cargar centros: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Selecciona un centro (solo para admin app)
     */
    fun seleccionarCentro(centro: Centro) {
        if (!_uiState.value.isAdminApp) return // Solo permitir a admin app
        
        _uiState.update { 
            it.copy(
                centroSeleccionado = centro,
                centroId = centro.id
            )
        }
        
        cargarCursos()
    }
    
    /**
     * Carga la lista de cursos del centro
     */
    fun cargarCursos() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val centroId = _uiState.value.centroId
            if (centroId.isEmpty() && !_uiState.value.isAdminApp) {
                // Si no hay centro seleccionado y no es admin app, muestra error
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "No se ha seleccionado un centro"
                    )
                }
                return@launch
            }
            
            // Para admin app sin centro seleccionado, no cargamos cursos y dejamos lista vacía
            if (centroId.isEmpty() && _uiState.value.isAdminApp) {
                _uiState.update { it.copy(isLoading = false, cursos = emptyList()) }
                return@launch
            }
            
            // Obtener los cursos del centro
            when (val result = cursoRepository.obtenerCursosPorCentroResult(centroId, soloActivos = true)) {
                is Result.Success -> {
                    Timber.d("Cursos cargados: ${result.data.size}")
                    _uiState.update { 
                        it.copy(
                            cursos = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar cursos")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar cursos: ${result.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Elimina un curso
     * @param cursoId ID del curso a eliminar
     */
    fun eliminarCurso(cursoId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = cursoRepository.borrarCurso(cursoId)) {
                is Result.Success -> {
                    Timber.d("Curso eliminado: $cursoId")
                    // Actualizar la lista después de eliminar
                    cargarCursos()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al eliminar curso")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al eliminar curso: ${result.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 