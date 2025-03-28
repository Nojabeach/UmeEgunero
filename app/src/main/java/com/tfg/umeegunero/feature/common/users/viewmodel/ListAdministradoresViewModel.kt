package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de listado de administradores
 */
data class ListAdministradoresUiState(
    val administradores: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val soloActivos: Boolean = true,
    val administradoresCompletos: List<Usuario> = emptyList() // Lista completa sin filtros
)

/**
 * ViewModel para la gestión de la pantalla de listado de administradores
 */
@HiltViewModel
class ListAdministradoresViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListAdministradoresUiState())
    val uiState: StateFlow<ListAdministradoresUiState> = _uiState.asStateFlow()

    /**
     * Carga la lista de administradores desde el repositorio
     */
    fun cargarAdministradores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = usuarioRepository.getUsersByType(TipoUsuario.ADMIN_APP)) {
                    is Result.Success -> {
                        val admins = result.data
                        _uiState.update { 
                            it.copy(
                                administradoresCompletos = admins,
                                administradores = if (it.soloActivos) admins.filter { admin -> admin.activo } else admins,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Administradores cargados: ${admins.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar administradores: ${result.exception.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar administradores")
                    }
                    is Result.Loading -> {
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
                Timber.e(e, "Error inesperado al cargar administradores")
            }
        }
    }

    /**
     * Aplica filtros a la lista de administradores
     * @param soloActivos Si es true, muestra solo administradores activos
     */
    fun aplicarFiltros(soloActivos: Boolean) {
        _uiState.update { currentState ->
            val administradoresToShow = if (soloActivos) {
                currentState.administradoresCompletos.filter { it.activo }
            } else {
                currentState.administradoresCompletos
            }
            
            currentState.copy(
                administradores = administradoresToShow,
                soloActivos = soloActivos
            )
        }
    }

    /**
     * Elimina un administrador por su ID (DNI)
     * @param administradorId ID del administrador a eliminar
     */
    fun eliminarAdministrador(administradorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un caso real, aquí llamaríamos al repositorio para eliminar
                // Por ahora, hacemos una eliminación simulada para demostración
                _uiState.update { currentState ->
                    val administradoresActualizados = currentState.administradoresCompletos.filter { it.dni != administradorId }
                    val adminsFiltrados = if (currentState.soloActivos) {
                        administradoresActualizados.filter { it.activo }
                    } else {
                        administradoresActualizados
                    }
                    
                    currentState.copy(
                        administradores = adminsFiltrados,
                        administradoresCompletos = administradoresActualizados,
                        isLoading = false
                    )
                }
                
                Timber.d("Administrador eliminado: $administradorId")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar administrador: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error al eliminar administrador $administradorId")
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