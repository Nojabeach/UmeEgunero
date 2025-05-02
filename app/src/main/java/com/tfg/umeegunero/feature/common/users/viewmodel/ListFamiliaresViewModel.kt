package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
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
 * Estado de la UI para la pantalla de listado de familiares
 */
data class ListFamiliaresUiState(
    val familiares: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val soloActivos: Boolean = true,
    val familiaresCompletos: List<Usuario> = emptyList() // Lista completa sin filtros
)

/**
 * ViewModel para la gestión de la pantalla de listado de familiares
 */
@HiltViewModel
class ListFamiliaresViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListFamiliaresUiState())
    val uiState: StateFlow<ListFamiliaresUiState> = _uiState.asStateFlow()

    /**
     * Carga la lista de familiares desde el repositorio
     */
    fun cargarFamiliares() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = usuarioRepository.getUsersByType(TipoUsuario.FAMILIAR)) {
                    is Result.Success -> {
                        val familiares = result.data
                        _uiState.update { 
                            it.copy(
                                familiaresCompletos = familiares,
                                familiares = if (it.soloActivos) familiares.filter { familiar -> familiar.activo } else familiares,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Familiares cargados: ${familiares.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar familiares: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar familiares")
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
                Timber.e(e, "Error inesperado al cargar familiares")
            }
        }
    }

    /**
     * Aplica filtros a la lista de familiares
     * @param soloActivos Si es true, muestra solo familiares activos
     */
    fun aplicarFiltros(soloActivos: Boolean) {
        _uiState.update { currentState ->
            val familiaresToShow = if (soloActivos) {
                currentState.familiaresCompletos.filter { it.activo }
            } else {
                currentState.familiaresCompletos
            }
            
            currentState.copy(
                familiares = familiaresToShow,
                soloActivos = soloActivos
            )
        }
    }

    /**
     * Elimina un familiar por su ID (DNI)
     * @param familiarId ID del familiar a eliminar
     */
    fun eliminarFamiliar(familiarId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = usuarioRepository.borrarUsuarioByDni(familiarId)) {
                    is Result.Success -> {
                        // Actualización local de la lista después de eliminar
                        _uiState.update { currentState ->
                            val familiaresActualizados = currentState.familiaresCompletos.filter { it.dni != familiarId }
                            val familiaresFiltrados = if (currentState.soloActivos) {
                                familiaresActualizados.filter { it.activo }
                            } else {
                                familiaresActualizados
                            }
                            
                            currentState.copy(
                                familiares = familiaresFiltrados,
                                familiaresCompletos = familiaresActualizados,
                                isLoading = false
                            )
                        }
                        
                        Timber.d("Familiar eliminado correctamente: $familiarId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar familiar: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar familiar $familiarId")
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al eliminar familiar: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar familiar $familiarId")
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