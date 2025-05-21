package com.tfg.umeegunero.feature.common.support.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber
import com.tfg.umeegunero.data.model.EmailSoporteConstants
import com.tfg.umeegunero.data.repository.ConfigRepository
import com.tfg.umeegunero.data.service.EmailNotificationService
import com.tfg.umeegunero.data.service.TipoPlantilla
import com.tfg.umeegunero.util.Constants

/**
 * ViewModel para la pantalla de soporte técnico.
 * 
 * Gestiona el proceso de envío de mensajes de soporte técnico a través del
 * servicio de notificaciones por correo electrónico. Mantiene el estado de la UI
 * durante el proceso de envío y gestiona los posibles errores.
 */
@HiltViewModel
class SupportViewModel @Inject constructor(
    private val emailService: EmailNotificationService,
    private val configRepository: ConfigRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SupportUiState())
    
    /**
     * Estado de la UI de soporte técnico que puede ser observado por la pantalla.
     */
    val uiState: StateFlow<SupportUiState> = _uiState
    
    /**
     * Envía un email de soporte técnico al equipo de UmeEgunero.
     * 
     * @param emailUsuario Email del usuario que envía el mensaje de soporte
     * @param nombre Nombre del usuario que envía el mensaje
     * @param asunto Asunto del mensaje de soporte
     * @param mensaje Contenido del mensaje de soporte
     */
    fun sendEmailSoporte(emailUsuario: String, nombre: String, asunto: String, mensaje: String) {
        _uiState.update { it.copy(isLoading = true, error = null, success = false) }
        
        viewModelScope.launch {
            try {
                Timber.d("Obteniendo configuración de email de soporte")
                val config = configRepository.getEmailSoporteConfig()
                val emailDestino = config.emailDestino
                
                Timber.d("Enviando email de soporte a $emailDestino") 
                
                // Usar el nuevo método específico para envío de soporte técnico
                val resultado = emailService.enviarEmailSoporte(
                    destinatario = emailDestino,
                    nombreUsuario = nombre,
                    emailUsuario = emailUsuario,
                    asunto = asunto,
                    mensaje = mensaje
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        success = resultado,
                        error = if (!resultado) "Error al enviar el mensaje de soporte. Por favor, inténtelo de nuevo más tarde." else null
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error en sendEmailSoporte: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        success = false, 
                        error = "Error inesperado al enviar el mensaje: ${e.message ?: "Desconocido"}"
                    )
                }
            }
        }
    }

    /**
     * Restablece el estado de la UI a su valor inicial.
     * 
     * Útil después de mostrar un mensaje de éxito o error para permitir
     * al usuario realizar un nuevo intento de envío.
     */
    fun clearState() {
        _uiState.update { SupportUiState() }
    }
}

/**
 * Estado de UI para la pantalla de soporte técnico.
 * 
 * @property isLoading Indica si se está procesando el envío del mensaje
 * @property success Indica si el último envío fue exitoso
 * @property error Mensaje de error en caso de fallo, o null si no hay error
 */
data class SupportUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
) 