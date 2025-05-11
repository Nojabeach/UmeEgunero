package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de listado de profesores
 */
data class ListProfesoresUiState(
    val profesores: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val soloActivos: Boolean = true,
    val profesoresCompletos: List<Usuario> = emptyList(), // Lista completa sin filtros
    val eliminacionExitosa: Boolean = false
)

/**
 * ViewModel para la gestión de la pantalla de listado de profesores
 */
@HiltViewModel
class ListProfesoresViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val profesorRepository: ProfesorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListProfesoresUiState())
    val uiState: StateFlow<ListProfesoresUiState> = _uiState.asStateFlow()

    /**
     * Carga la lista de profesores desde el repositorio para un centro específico
     * @param centroId El ID del centro cuyos profesores se cargarán
     */
    fun cargarProfesores(centroId: String) {
        if (centroId.isBlank()) {
            Timber.e("Error: Se intentó cargar profesores con centroId vacío.")
            _uiState.update { it.copy(error = "ID de centro inválido.", isLoading = false) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, eliminacionExitosa = false) }
            Timber.d("Iniciando carga de profesores para centro: $centroId")
            
            try {
                // Usar el método específico del repositorio
                when (val result = usuarioRepository.getProfesoresByCentro(centroId)) {
                    is Result.Success -> {
                        val profesores = result.data
                        _uiState.update { 
                            it.copy(
                                profesoresCompletos = profesores,
                                profesores = if (it.soloActivos) profesores.filter { profesor -> profesor.activo } else profesores,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Profesores cargados para centro $centroId: ${profesores.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar profesores del centro: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar profesores para centro $centroId")
                    }
                    else -> {
                        // Este estado lo manejamos al inicio
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar profesores para centro $centroId")
            }
        }
    }

    /**
     * Aplica filtros a la lista de profesores
     * @param soloActivos Si es true, muestra solo profesores activos
     */
    fun aplicarFiltros(soloActivos: Boolean) {
        _uiState.update { currentState ->
            val profesoresToShow = if (soloActivos) {
                currentState.profesoresCompletos.filter { it.activo }
            } else {
                currentState.profesoresCompletos
            }
            
            currentState.copy(
                profesores = profesoresToShow,
                soloActivos = soloActivos
            )
        }
    }

    /**
     * Elimina un profesor completamente del sistema
     * 
     * Este método realiza la eliminación del usuario con rol de profesor:
     * 1. Elimina el usuario asociado al profesor de la colección 'usuarios'
     *    y de Firebase Authentication.
     * 
     * @param profesorId ID del profesor a eliminar (DNI)
     */
    fun eliminarProfesor(profesorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, eliminacionExitosa = false) }
            
            try {
                Timber.d("Iniciando proceso de eliminación del profesor: $profesorId")
                
                // Eliminar el usuario asociado al profesor
                when (val result = usuarioRepository.borrarUsuarioByDni(profesorId)) {
                    is Result.Success -> {
                        // Actualización local de la lista después de eliminar
                        _uiState.update { currentState ->
                            val profesoresActualizados = currentState.profesoresCompletos.filter { it.dni != profesorId }
                            val profesoresFiltrados = if (currentState.soloActivos) {
                                profesoresActualizados.filter { it.activo }
                            } else {
                                profesoresActualizados
                            }
                            
                            currentState.copy(
                                profesores = profesoresFiltrados,
                                profesoresCompletos = profesoresActualizados,
                                isLoading = false,
                                eliminacionExitosa = true
                            )
                        }
                        
                        Timber.d("Profesor eliminado completamente del sistema: $profesorId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar profesor: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar profesor $profesorId")
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al eliminar profesor: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar profesor $profesorId")
            }
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Restablece el estado de eliminación exitosa
     */
    fun resetEliminacionExitosa() {
        _uiState.update { it.copy(eliminacionExitosa = false) }
    }

    // La función obtenerCentroIdUsuarioActual ya no es necesaria aquí
    /*
    fun obtenerCentroIdUsuarioActual() {
        viewModelScope.launch {
            try {
                // Obtener el usuario actual
                val usuario = usuarioRepository.obtenerUsuarioActual()
                
                if (usuario != null) {
                    // Buscar el perfil de tipo ADMIN_CENTRO para obtener el centroId
                    val perfilAdminCentro = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                    val centroId = perfilAdminCentro?.centroId ?: ""
                    
                    if (centroId.isNotEmpty()) {
                        Timber.d("Centro ID del administrador obtenido: $centroId")
                        _uiState.update { it.copy(centroId = centroId) }
                    } else {
                        Timber.w("No se encontró centro ID para el administrador actual")
                    }
                } else {
                    Timber.w("No se pudo obtener el usuario actual")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener el centro ID del administrador actual")
            }
        }
    }
    */
} 