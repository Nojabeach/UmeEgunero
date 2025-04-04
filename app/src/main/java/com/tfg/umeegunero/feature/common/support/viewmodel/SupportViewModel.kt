package com.tfg.umeegunero.feature.common.support.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.util.EmailSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para la pantalla de soporte técnico
 */
@HiltViewModel
class SupportViewModel @Inject constructor(
    private val emailSender: EmailSender
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()
    
    /**
     * Envía un email utilizando el servicio de EmailSender
     * 
     * @param from Correo del remitente
     * @param to Correo del destinatario
     * @param subject Asunto del mensaje
     * @param messageBody Cuerpo del mensaje
     * @param senderName Nombre del remitente (opcional)
     * @return true si el correo se envió correctamente, false en caso contrario
     */
    suspend fun sendEmail(
        from: String,
        to: String,
        subject: String,
        messageBody: String,
        senderName: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Intentando enviar email de $from a $to")
            val result = emailSender.sendEmail(
                from = from,
                to = to,
                subject = subject,
                messageBody = messageBody,
                senderName = senderName
            )
            
            if (result) {
                Timber.d("Email enviado correctamente")
            } else {
                Timber.e("No se pudo enviar el email")
            }
            
            return@withContext result
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar email: ${e.message}")
            return@withContext false
        }
    }
}

/**
 * Estado de UI para la pantalla de soporte
 */
data class SupportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmailSent: Boolean = false
) 