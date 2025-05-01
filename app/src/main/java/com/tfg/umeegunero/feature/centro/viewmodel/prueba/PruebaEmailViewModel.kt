package com.tfg.umeegunero.feature.centro.viewmodel.prueba

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.util.EmailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de prueba de emails
 */
data class PruebaEmailUiState(
    val enviando: Boolean = false,
    val mensaje: String? = null
)

/**
 * ViewModel para la pantalla de prueba de envío de emails
 */
@HiltViewModel
class PruebaEmailViewModel @Inject constructor(
    private val emailService: EmailService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PruebaEmailUiState())
    val uiState: StateFlow<PruebaEmailUiState> = _uiState.asStateFlow()
    
    /**
     * Envía un email de prueba de solicitud aprobada
     */
    fun enviarEmailAprobado(destinatario: String) {
        if (destinatario.isBlank()) {
            _uiState.update { it.copy(mensaje = "Por favor, introduce un correo electrónico") }
            return
        }
        
        enviarEmail(destinatario, true)
    }
    
    /**
     * Envía un email de prueba de solicitud rechazada
     */
    fun enviarEmailRechazado(destinatario: String) {
        if (destinatario.isBlank()) {
            _uiState.update { it.copy(mensaje = "Por favor, introduce un correo electrónico") }
            return
        }
        
        enviarEmail(destinatario, false)
    }
    
    /**
     * Envía un email de prueba
     */
    private fun enviarEmail(destinatario: String, aprobado: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(enviando = true, mensaje = "Enviando email a $destinatario...") }
                
                // Configurar datos para el envío del email
                val alumnoNombre = "Alumno de Prueba"
                val centroNombre = "Centro Educativo UmeEgunero (Prueba)"
                
                // Log para depuración
                Timber.d("Iniciando envío de email ${if (aprobado) "de aprobación" else "de rechazo"} a $destinatario")
                
                // Usar el servicio de email para enviar la notificación
                when (val result = emailService.sendVinculacionNotification(
                    to = destinatario,
                    isApproved = aprobado,
                    alumnoNombre = alumnoNombre,
                    centroNombre = centroNombre
                )) {
                    is com.tfg.umeegunero.util.Result.Success -> {
                        val response = result.data
                        
                        // Información detallada sobre el resultado exitoso
                        val statusCode = response.statusCode
                        val tipoEmail = if (aprobado) "aprobación" else "rechazo"
                        
                        val mensajeExito = """
                            ¡Email de $tipoEmail enviado correctamente!
                            Destinatario: $destinatario
                            Código de estado: $statusCode
                        """.trimIndent()
                        
                        Timber.d("Email enviado exitosamente. StatusCode: $statusCode")
                        
                        _uiState.update { 
                            it.copy(
                                enviando = false,
                                mensaje = mensajeExito
                            )
                        }
                    }
                    is com.tfg.umeegunero.util.Result.Error -> {
                        val error = result.exception
                        
                        // Información detallada sobre el error
                        val mensajeError = """
                            Error al enviar email: ${error?.message ?: "Error desconocido"}
                            Destinatario: $destinatario
                            Tipo: ${if (aprobado) "Aprobación" else "Rechazo"}
                        """.trimIndent()
                        
                        Timber.e(error, "Error al enviar email de prueba a $destinatario")
                        _uiState.update { 
                            it.copy(
                                enviando = false,
                                mensaje = mensajeError
                            )
                        }
                    }
                    else -> {
                        _uiState.update { 
                            it.copy(
                                enviando = false,
                                mensaje = "Estado inesperado en el envío de email"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Capturar cualquier excepción no manejada
                val mensajeExcepcion = """
                    Excepción al enviar email:
                    ${e.localizedMessage ?: e.message ?: "Error desconocido"}
                    Tipo: ${e.javaClass.simpleName}
                """.trimIndent()
                
                Timber.e(e, "Excepción al enviar email de prueba a $destinatario")
                _uiState.update { 
                    it.copy(
                        enviando = false, 
                        mensaje = mensajeExcepcion
                    ) 
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