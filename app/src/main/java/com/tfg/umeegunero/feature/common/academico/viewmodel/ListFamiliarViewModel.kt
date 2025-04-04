package com.tfg.umeegunero.feature.common.academico.viewmodel

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
 * Estado de UI para la pantalla de listado de familiares
 */
data class ListFamiliarUiState(
    val familiares: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para el listado de familiares
 */
@HiltViewModel
class ListFamiliarViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListFamiliarUiState(isLoading = true))
    val uiState: StateFlow<ListFamiliarUiState> = _uiState.asStateFlow()

    /**
     * Carga todos los familiares desde el repositorio
     */
    fun loadFamiliares() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.getUsersByType(TipoUsuario.FAMILIAR)
                
                when (result) {
                    is Result.Success<List<Usuario>> -> {
                        _uiState.update { 
                            it.copy(
                                familiares = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Familiares cargados: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al cargar los familiares"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar los familiares")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los familiares"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar los familiares")
            }
        }
    }

    /**
     * Elimina un familiar por su DNI
     */
    fun deleteFamiliar(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.borrarUsuarioByDni(dni)
                
                when (result) {
                    is Result.Success<Unit> -> {
                        // Recargar la lista de familiares después de eliminar
                        loadFamiliares()
                        Timber.d("Familiar eliminado con DNI: $dni")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al eliminar el familiar"
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar el familiar")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el familiar"
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar el familiar")
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 