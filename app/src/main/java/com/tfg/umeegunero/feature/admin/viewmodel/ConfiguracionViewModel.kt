package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.service.AvatarService
import com.tfg.umeegunero.util.DefaultAvatarsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de configuración de la aplicación.
 */
data class ConfiguracionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val avataresPredeterminados: Map<TipoUsuario, String> = emptyMap(),
    val resultadoAsignacionAvatares: Map<String, String> = emptyMap()
)

/**
 * ViewModel para la configuración general de la aplicación.
 *
 * Este ViewModel proporciona funcionalidades para la administración de configuraciones
 * generales de la aplicación UmeEgunero, incluyendo la inicialización y verificación
 * de recursos predeterminados como avatares.
 *
 * @property avatarService Servicio para operaciones relacionadas con avatares
 * @property defaultAvatarsManager Gestor de avatares predeterminados
 */
@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val avatarService: AvatarService,
    private val defaultAvatarsManager: DefaultAvatarsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    /**
     * Verifica y sube todos los avatares predeterminados al Storage de Firebase.
     * Este método comprueba la existencia de los avatares para cada tipo de usuario,
     * y si no existen, los sube desde los recursos de la aplicación.
     */
    fun inicializarAvataresPredeterminados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                Timber.d("Iniciando verificación y subida de avatares predeterminados")
                val avatares = defaultAvatarsManager.verificarYSubirTodosLosAvatares()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        avataresPredeterminados = avatares,
                        error = if (avatares.isEmpty()) "No se pudieron inicializar los avatares" else null
                    )
                }
                
                Timber.d("Avatares predeterminados inicializados: ${avatares.size}")
            } catch (e: Exception) {
                Timber.e(e, "Error al inicializar avatares predeterminados: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al inicializar avatares: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
    
    /**
     * Asigna avatares predeterminados a todos los usuarios que no tienen avatar.
     */
    fun asignarAvataresPredeterminadosATodosLosUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                Timber.d("Iniciando asignación de avatares a usuarios sin avatar")
                val resultados = avatarService.asignarAvataresPredeterminados()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        resultadoAsignacionAvatares = resultados,
                        error = null
                    )
                }
                
                Timber.d("Avatares asignados a ${resultados.size} usuarios")
            } catch (e: Exception) {
                Timber.e(e, "Error al asignar avatares predeterminados: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al asignar avatares: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
} 