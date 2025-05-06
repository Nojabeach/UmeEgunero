package com.tfg.umeegunero.feature.familiar.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.notification.AppNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de solicitud de permisos de notificaciones
 */
data class PermisoNotificacionesUiState(
    val isLoading: Boolean = false,
    val mensaje: String? = null,
    val continuarSiguientePantalla: Boolean = false
)

/**
 * ViewModel para la pantalla de solicitud de permisos de notificaciones
 * 
 * Se encarga de gestionar la solicitud de permisos y el registro del token
 * FCM en Firebase para habilitar las notificaciones push.
 */
@HiltViewModel
class PermisoNotificacionesViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository,
    private val notificationManager: AppNotificationManager,
    private val functions: FirebaseFunctions
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermisoNotificacionesUiState())
    val uiState: StateFlow<PermisoNotificacionesUiState> = _uiState.asStateFlow()
    
    /**
     * Procesa la respuesta del usuario al permiso de notificaciones
     * 
     * @param aceptado True si el usuario aceptó los permisos, false en caso contrario
     */
    fun onPermisoRespuesta(aceptado: Boolean) {
        if (aceptado) {
            aceptarNotificaciones()
        } else {
            rechazarNotificaciones()
        }
    }
    
    /**
     * Registra la aceptación de notificaciones y el token FCM
     */
    fun aceptarNotificaciones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener token FCM
                val token = notificationManager.registerDeviceToken()
                
                // Guardar preferencia local
                preferenciasRepository.setNotificacionesGeneral(true)
                
                if (token.isNotEmpty()) {
                    preferenciasRepository.guardarFcmToken(token)
                    
                    // Registrar en Firestore a través de Cloud Function
                    registrarPreferenciaEnFirebase(true, token)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mensaje = "No se pudo obtener el token para notificaciones",
                            continuarSiguientePantalla = true
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al activar notificaciones")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensaje = "Error al activar notificaciones: ${e.message}",
                        continuarSiguientePantalla = true
                    )
                }
            }
        }
    }
    
    /**
     * Registra el rechazo a recibir notificaciones
     */
    fun rechazarNotificaciones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Guardar preferencia local
                preferenciasRepository.setNotificacionesGeneral(false)
                
                // Registrar en Firestore a través de Cloud Function
                registrarPreferenciaEnFirebase(false, "")
            } catch (e: Exception) {
                Timber.e(e, "Error al desactivar notificaciones")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensaje = "Error al desactivar notificaciones: ${e.message}",
                        continuarSiguientePantalla = true
                    )
                }
            }
        }
    }
    
    /**
     * Registra la preferencia de notificaciones en Firebase
     */
    private fun registrarPreferenciaEnFirebase(permitir: Boolean, token: String) {
        val data = hashMapOf(
            "permitir" to permitir,
            "token" to token
        )
        
        functions
            .getHttpsCallable("registrarPermisoNotificaciones")
            .call(data)
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensaje = if (permitir) 
                            "Notificaciones activadas correctamente" 
                        else 
                            "Notificaciones desactivadas",
                        continuarSiguientePantalla = true
                    )
                }
                
                Timber.d("Preferencia de notificaciones registrada: permitir=$permitir")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Error al registrar preferencia de notificaciones")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensaje = "Error al registrar preferencia: ${e.message}",
                        continuarSiguientePantalla = true
                    )
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