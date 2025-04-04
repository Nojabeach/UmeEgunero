package com.tfg.umeegunero.feature.common.config.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.notification.AppNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de configuración de notificaciones
 */
data class NotificacionesUiState(
    val notificacionesTareasHabilitadas: Boolean = true,
    val notificacionesGeneralHabilitadas: Boolean = true,
    val fcmToken: String = "",
    val isLoading: Boolean = false,
    val mensaje: String? = null
)

/**
 * ViewModel para la pantalla de configuración de notificaciones
 */
@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val notificationManager: AppNotificationManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()
    
    init {
        cargarPreferencias()
    }
    
    /**
     * Carga las preferencias de notificaciones del repositorio
     */
    private fun cargarPreferencias() {
        viewModelScope.launch {
            try {
                // Recoger el token FCM actual
                preferenciasRepository.fcmToken.collectLatest { token ->
                    _uiState.update { it.copy(fcmToken = token) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar token FCM")
            }
        }
        
        viewModelScope.launch {
            try {
                // Recoger preferencias de notificaciones de tareas
                preferenciasRepository.notificacionesTareasHabilitadas.collectLatest { habilitadas ->
                    _uiState.update { it.copy(notificacionesTareasHabilitadas = habilitadas) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar preferencias de notificaciones de tareas")
            }
        }
        
        viewModelScope.launch {
            try {
                // Recoger preferencias de notificaciones generales
                preferenciasRepository.notificacionesGeneralHabilitadas.collectLatest { habilitadas ->
                    _uiState.update { it.copy(notificacionesGeneralHabilitadas = habilitadas) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar preferencias de notificaciones generales")
            }
        }
    }
    
    /**
     * Actualiza el token FCM
     */
    fun actualizarToken() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val token = notificationManager.registerDeviceToken()
                
                if (token.isNotEmpty()) {
                    preferenciasRepository.guardarFcmToken(token)
                    _uiState.update { 
                        it.copy(
                            fcmToken = token,
                            mensaje = "Dispositivo registrado correctamente",
                            isLoading = false
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            mensaje = "No se pudo registrar el dispositivo",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar token FCM")
                _uiState.update { 
                    it.copy(
                        mensaje = "Error al registrar dispositivo: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Configura las notificaciones de tareas
     * @param habilitadas true para habilitar, false para deshabilitar
     */
    fun setNotificacionesTareas(habilitadas: Boolean) {
        viewModelScope.launch {
            try {
                preferenciasRepository.setNotificacionesTareas(habilitadas)
                _uiState.update { 
                    it.copy(
                        notificacionesTareasHabilitadas = habilitadas,
                        mensaje = if (habilitadas) 
                            "Notificaciones de tareas habilitadas" 
                        else 
                            "Notificaciones de tareas deshabilitadas"
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al configurar notificaciones de tareas")
                _uiState.update { 
                    it.copy(mensaje = "Error al guardar preferencia: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Configura las notificaciones generales
     * @param habilitadas true para habilitar, false para deshabilitar
     */
    fun setNotificacionesGeneral(habilitadas: Boolean) {
        viewModelScope.launch {
            try {
                preferenciasRepository.setNotificacionesGeneral(habilitadas)
                _uiState.update { 
                    it.copy(
                        notificacionesGeneralHabilitadas = habilitadas,
                        mensaje = if (habilitadas) 
                            "Notificaciones generales habilitadas" 
                        else 
                            "Notificaciones generales deshabilitadas"
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al configurar notificaciones generales")
                _uiState.update { 
                    it.copy(mensaje = "Error al guardar preferencia: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Limpia el mensaje de la UI
     */
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 