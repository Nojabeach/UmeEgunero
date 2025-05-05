package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de comunicados del profesor.
 */
data class ComunicadosProfesorUiState(
    val isLoading: Boolean = false,
    val comunicados: List<Comunicado> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla de gestión de comunicados del profesor.
 *
 * Gestiona la carga, creación y estado de los comunicados.
 *
 * @param comunicadoRepository Repositorio para acceder a los datos de comunicados.
 * @param authRepository Repositorio para obtener información del usuario actual.
 */
@HiltViewModel
class ComunicadosProfesorViewModel @Inject constructor(
    private val comunicadoRepository: ComunicadoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComunicadosProfesorUiState())
    val uiState: StateFlow<ComunicadosProfesorUiState> = _uiState.asStateFlow()

    init {
        cargarComunicados()
    }

    /**
     * Carga la lista de comunicados relevantes para el profesor.
     */
    private fun cargarComunicados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado.") }
                return@launch
            }

            when (val result = comunicadoRepository.getComunicadosByTipoUsuario(com.tfg.umeegunero.data.model.TipoUsuario.PROFESOR)) { 
                is Result.Success -> {
                     _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            comunicados = result.data.sortedByDescending { com -> com.fechaCreacion },
                            error = null
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar comunicados para profesor $userId")
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar comunicados: ${result.exception?.localizedMessage}") }
                }
                else -> { _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }
} 