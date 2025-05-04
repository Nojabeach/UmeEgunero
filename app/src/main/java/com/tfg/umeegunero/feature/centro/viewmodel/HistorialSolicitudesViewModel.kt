package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.SolicitudRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
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
 * Estado UI para la pantalla de historial de solicitudes de vinculación.
 */
data class HistorialSolicitudesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val solicitudes: List<SolicitudVinculacion> = emptyList(),
    val centroId: String = ""
)

/**
 * ViewModel para la pantalla de historial de solicitudes de vinculación.
 * 
 * Este ViewModel se encarga de cargar y gestionar el historial completo
 * de solicitudes de vinculación procesadas por los administradores del centro.
 * 
 * @property solicitudRepository Repositorio para acceder a las solicitudes
 * @property usuarioRepository Repositorio para acceder a los usuarios
 * @property authRepository Repositorio para la autenticación
 */
@HiltViewModel
class HistorialSolicitudesViewModel @Inject constructor(
    private val solicitudRepository: SolicitudRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistorialSolicitudesUiState())
    val uiState: StateFlow<HistorialSolicitudesUiState> = _uiState.asStateFlow()
    
    init {
        obtenerCentroId()
    }
    
    /**
     * Obtiene el ID del centro asociado al administrador actual
     */
    private fun obtenerCentroId() {
        viewModelScope.launch {
            try {
                val firebaseUser = authRepository.getCurrentUser()
                if (firebaseUser != null) {
                    val userResult = usuarioRepository.getUsuarioByEmail(firebaseUser.email)
                    if (userResult is Result.Success) {
                        val usuario = userResult.data
                        val perfilCentro = usuario.perfiles.find { it.tipo == com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_CENTRO }
                        val centroId = perfilCentro?.centroId ?: ""
                        
                        if (centroId.isNotEmpty()) {
                            _uiState.update { it.copy(centroId = centroId) }
                            cargarHistorialSolicitudes()
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = "No se encontró un centro asociado al administrador"
                                )
                            }
                        }
                    } else if (userResult is Result.Error) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar el usuario: ${userResult.exception?.message}"
                            )
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Usuario no autenticado"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener centroId")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Carga el historial completo de solicitudes de vinculación
     */
    fun cargarHistorialSolicitudes() {
        val centroId = _uiState.value.centroId
        if (centroId.isEmpty()) {
            obtenerCentroId()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = solicitudRepository.getHistorialSolicitudesByCentroId(centroId)
                
                if (result is Result.Success) {
                    _uiState.update { 
                        it.copy(
                            solicitudes = result.data,
                            isLoading = false
                        )
                    }
                } else if (result is Result.Error) {
                    Timber.e(result.exception, "Error al cargar historial")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar historial: ${result.exception?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar historial")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }
} 