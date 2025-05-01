package com.tfg.umeegunero.feature.common.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tfg.umeegunero.data.repository.ConfigRepository
import com.tfg.umeegunero.util.Constants
import timber.log.Timber

/**
 * ViewModel para la pantalla de Soporte Técnico.
 * Gestiona el estado de la UI y la lógica de envío de mensajes de soporte.
 */
@HiltViewModel
class SoporteTecnicoViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoporteUiState())
    val uiState: StateFlow<SoporteUiState> = _uiState.asStateFlow()

    init {
        loadSupportEmail()
    }

    private fun loadSupportEmail() {
        viewModelScope.launch {
            try {
                val emailSoporte = Constants.EMAIL_SOPORTE
                _uiState.value = _uiState.value.copy(emailDestino = emailSoporte)
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar email de soporte")
                _uiState.value = _uiState.value.copy(error = "No se pudo obtener la dirección de soporte.")
            }
        }
    }

    fun updateNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateAsunto(asunto: String) {
        _uiState.value = _uiState.value.copy(asunto = asunto)
    }

    fun updateMensaje(mensaje: String) {
        _uiState.value = _uiState.value.copy(mensaje = mensaje)
    }

    fun enviarEmail() {
        val currentState = _uiState.value
        
        if (currentState.nombre.isBlank() || currentState.email.isBlank() || 
            currentState.asunto.isBlank() || currentState.mensaje.isBlank()) {
            _uiState.value = currentState.copy(
                errores = mapOf(
                    "nombre" to if (currentState.nombre.isBlank()) "El nombre es obligatorio" else "",
                    "email" to if (currentState.email.isBlank()) "El email es obligatorio" else "",
                    "asunto" to if (currentState.asunto.isBlank()) "El asunto es obligatorio" else "",
                    "mensaje" to if (currentState.mensaje.isBlank()) "El mensaje es obligatorio" else ""
                ).filterValues { it.isNotEmpty() }
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true, enviado = false, errores = emptyMap())

        viewModelScope.launch {
            try {
                // Simulamos el envío exitoso por ahora
                kotlinx.coroutines.delay(1500)
                _uiState.value = currentState.copy(
                    isLoading = false,
                    enviado = true,
                    errores = emptyMap()
                )
            } catch (e: Exception) {
                Timber.e(e, "Error al enviar mensaje de soporte")
                _uiState.value = currentState.copy(
                    isLoading = false,
                    enviado = false,
                    errores = mapOf("general" to "Error al enviar el mensaje: ${e.message}")
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errores = emptyMap())
    }
}

/**
 * Estado de la UI para la pantalla de Soporte Técnico.
 */
data class SoporteUiState(
    val isLoading: Boolean = false,
    val enviado: Boolean = false,
    val error: String? = null,
    val emailDestino: String? = null,
    val nombre: String = "",
    val email: String = "",
    val asunto: String = "",
    val mensaje: String = "",
    val errores: Map<String, String> = emptyMap()
) 