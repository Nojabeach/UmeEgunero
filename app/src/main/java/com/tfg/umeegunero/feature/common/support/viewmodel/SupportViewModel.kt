package com.tfg.umeegunero.feature.common.support.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber
import com.tfg.umeegunero.data.model.EmailSoporteConstants

/**
 * ViewModel para la pantalla de soporte técnico
 */
@HiltViewModel
class SupportViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState
    
    fun sendEmailSoporte(destinatario: String, nombre: String, asunto: String, mensaje: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                // TODO: Implementar envío con Gmail API u otra solución
                Timber.d("Simulando envío de email de soporte a $destinatario...") 
                // MailjetEmailSender.sendEmail(
                //     destinatario = destinatario,
                //     nombre = nombre,
                //     subject = asunto,
                //     htmlBody = mensaje
                // ) { success ->
                //     _uiState.value = _uiState.value.copy(
                //         isLoading = false,
                //         success = success,
                //         error = if (!success) "Error al enviar el correo" else null
                //     )
                // }
                kotlinx.coroutines.delay(1500) // Simular delay de red
                 _uiState.value = _uiState.value.copy(
                     isLoading = false,
                     success = true, // Simular éxito por ahora
                     error = null
                 )

            } catch (e: Exception) {
                Timber.e(e, "Error en sendEmailSoporte")
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    success = false, 
                    error = "Error inesperado al enviar el correo: ${e.message}"
                )
            }
        }
    }

    fun clearState() {
        _uiState.value = SupportUiState()
    }
}

/**
 * Estado de UI para la pantalla de soporte
 */
data class SupportUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
) 