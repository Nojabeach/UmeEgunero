package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val centros: List<Centro> = emptyList(),
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingUsuarios: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadCentros()
        loadUsuarios()
    }

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
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar los centros"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los centros"
                    )
                }
            }
        }
    }
    
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
                            _uiState.update {
                                it.copy(
                                    isLoadingUsuarios = false,
                                    error = result.exception.message ?: "Error al cargar los usuarios de tipo $tipo"
                                )
                            }
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
                _uiState.update {
                    it.copy(
                        isLoadingUsuarios = false,
                        error = e.message ?: "Error inesperado al cargar los usuarios"
                    )
                }
            }
        }
    }

    fun deleteCentro(centroId: String) {
        viewModelScope.launch {
            try {
                val result = centroRepository.deleteCentro(centroId)

                if (result is Result.Success) {
                    // Recargar la lista después de borrar
                    loadCentros()
                } else if (result is Result.Error) {
                    _uiState.update {
                        it.copy(error = result.exception.message ?: "Error al eliminar el centro")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Error inesperado al eliminar el centro")
                }
            }
        }
    }
    
    fun deleteUsuario(usuarioDni: String) {
        viewModelScope.launch {
            try {
                // Aquí debería implementarse la lógica para eliminar el usuario
                // Por el momento solo recargamos los usuarios
                loadUsuarios()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Error inesperado al eliminar el usuario")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}