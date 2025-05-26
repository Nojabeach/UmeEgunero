package com.tfg.umeegunero.feature.common.config.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.notification.AppNotificationManager
import com.tfg.umeegunero.util.NotificationDiagnostic
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
    private val notificationManager: AppNotificationManager,
    private val notificationDiagnostic: NotificationDiagnostic
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()
    
    init {
        cargarPreferencias()
        ejecutarDiagnostico()
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
    
    /**
     * Ejecuta un diagnóstico completo de las notificaciones push
     */
    private fun ejecutarDiagnostico() {
        viewModelScope.launch {
            try {
                Timber.d("🔍 Iniciando diagnóstico de notificaciones push...")
                val result = notificationDiagnostic.runDiagnostic()
                notificationDiagnostic.printDiagnosticReport(result)
                
                // Si hay problemas críticos, mostrar mensaje en la UI
                if (result.issues.isNotEmpty()) {
                    val problemasGraves = result.issues.filter { 
                        it.contains("❌") && !it.contains("Token FCM local y de Firestore no coinciden")
                    }
                    
                    if (problemasGraves.isNotEmpty()) {
                        _uiState.update { 
                            it.copy(mensaje = "⚠️ Problemas detectados en notificaciones. Revisa los logs para más detalles.")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al ejecutar diagnóstico de notificaciones")
            }
        }
    }
    
    /**
     * Ejecuta el diagnóstico manualmente (para botón de diagnóstico)
     */
    fun ejecutarDiagnosticoManual() {
        viewModelScope.launch {
            try {
                val result = notificationDiagnostic.runDiagnostic()
                notificationDiagnostic.printDiagnosticReport(result)
                
                val mensaje = if (result.issues.isEmpty()) {
                    "✅ Diagnóstico completado: No se encontraron problemas"
                } else {
                    "⚠️ Diagnóstico completado: ${result.issues.size} problemas detectados. Revisa los logs para más detalles."
                }
                
                _uiState.update { it.copy(mensaje = mensaje) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(mensaje = "❌ Error ejecutando diagnóstico: ${e.message}") 
                }
            }
        }
    }
} 