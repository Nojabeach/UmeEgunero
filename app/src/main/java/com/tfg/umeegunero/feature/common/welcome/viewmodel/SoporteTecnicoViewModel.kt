package com.tfg.umeegunero.feature.common.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.ConfigRepository
import com.tfg.umeegunero.util.EmailSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de soporte técnico
 */
data class SoporteTecnicoUiState(
    val nombre: String = "",
    val email: String = "",
    val asunto: String = "",
    val mensaje: String = "",
    val isLoading: Boolean = false,
    val enviado: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla de soporte técnico
 */
@HiltViewModel
class SoporteTecnicoViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val emailSender: EmailSender
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SoporteTecnicoUiState())
    val uiState: StateFlow<SoporteTecnicoUiState> = _uiState
    
    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }
    
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }
    
    fun updateAsunto(asunto: String) {
        _uiState.update { it.copy(asunto = asunto) }
    }
    
    fun updateMensaje(mensaje: String) {
        _uiState.update { it.copy(mensaje = mensaje) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Valida los campos del formulario
     * @return true si todos los campos son válidos
     */
    private fun validarCampos(): Boolean {
        val nuevosErrores = mutableMapOf<String, String>()
        val state = _uiState.value
        
        if (state.nombre.isBlank()) {
            nuevosErrores["nombre"] = "El nombre es obligatorio"
        }
        
        if (state.email.isBlank()) {
            nuevosErrores["email"] = "El email es obligatorio"
        } else if (!state.email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))) {
            nuevosErrores["email"] = "Ingresa un email válido"
        }
        
        if (state.asunto.isBlank()) {
            nuevosErrores["asunto"] = "El asunto es obligatorio"
        }
        
        if (state.mensaje.isBlank()) {
            nuevosErrores["mensaje"] = "El mensaje es obligatorio"
        }
        
        _uiState.update { it.copy(errores = nuevosErrores) }
        return nuevosErrores.isEmpty()
    }
    
    /**
     * Envía el correo electrónico
     */
    fun enviarEmail() {
        if (!validarCampos()) return
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener la configuración de email desde Firestore
                val config = configRepository.getEmailSoporteConfig()
                
                val state = _uiState.value
                
                val cuerpoMensaje = """
                    De: ${state.nombre}
                    Email: ${state.email}
                    
                    ${state.mensaje}
                    
                    --- Enviado desde la app UmeEgunero ---
                """.trimIndent()
                
                val resultado = emailSender.sendEmail(
                    from = state.email, // Usar el email del usuario como remitente
                    to = "maitanepruebas1@gmail.com", // Destinatario fijo
                    subject = state.asunto,
                    messageBody = cuerpoMensaje,
                    senderName = state.nombre
                )
                
                if (resultado) {
                    _uiState.update { it.copy(enviado = true, isLoading = false) }
                    Timber.d("Email enviado correctamente a ${config.emailDestino}")
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo enviar el email. Inténtalo más tarde.",
                            isLoading = false
                        ) 
                    }
                    Timber.e("Error al enviar email")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al enviar email: ${e.message}")
                _uiState.update { 
                    it.copy(
                        error = "Error: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }
} 