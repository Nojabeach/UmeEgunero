package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.EmailSoporteConfig
import com.tfg.umeegunero.data.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de configuración de email
 */
data class EmailConfigUiState(
    val emailDestino: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val cambiosPendientes: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val mensaje: String? = null,
    val ultimaActualizacion: Timestamp? = null
)

/**
 * ViewModel para la pantalla de configuración de email
 */
@HiltViewModel
class EmailConfigViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EmailConfigUiState(isLoading = true))
    val uiState: StateFlow<EmailConfigUiState> = _uiState
    
    init {
        cargarConfiguracion()
    }
    
    /**
     * Carga la configuración actual de email
     */
    private fun cargarConfiguracion() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val config = configRepository.getEmailSoporteConfig()
                
                _uiState.update { 
                    it.copy(
                        emailDestino = config.emailDestino,
                        isLoading = false,
                        cambiosPendientes = false,
                        ultimaActualizacion = config.ultimaActualizacion
                    )
                }
                Timber.d("Configuración cargada: $config")
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar configuración de email")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Error al cargar configuración: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Actualiza el email de destino
     */
    fun updateEmailDestino(email: String) {
        _uiState.update { 
            it.copy(
                emailDestino = email,
                cambiosPendientes = true,
                errores = it.errores - "emailDestino"
            )
        }
    }
    
    /**
     * Limpia el mensaje de estado
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
    
    /**
     * Valida los campos antes de guardar
     */
    private fun validarCampos(): Boolean {
        var errores = mutableMapOf<String, String>()
        
        // Validar email
        if (uiState.value.emailDestino.isBlank()) {
            errores["emailDestino"] = "El email no puede estar vacío"
        } else if (!isValidEmail(uiState.value.emailDestino)) {
            errores["emailDestino"] = "El formato del email no es válido"
        }
        
        _uiState.update { it.copy(errores = errores) }
        return errores.isEmpty()
    }
    
    /**
     * Valida el formato de un email
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailRegex.toRegex())
    }
    
    /**
     * Guarda la configuración actual
     */
    fun guardarConfiguracion() {
        if (!validarCampos()) {
            _uiState.update { 
                it.copy(mensaje = "Por favor, corrige los errores antes de guardar") 
            }
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                
                val config = EmailSoporteConfig(
                    emailDestino = uiState.value.emailDestino,
                    ultimaActualizacion = Timestamp.now()
                )
                
                configRepository.saveEmailSoporteConfig(config)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        cambiosPendientes = false,
                        mensaje = "Configuración guardada correctamente",
                        ultimaActualizacion = config.ultimaActualizacion
                    ) 
                }
                
                Timber.d("Configuración guardada: $config")
                
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar configuración")
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        mensaje = "Error al guardar: ${e.message}"
                    ) 
                }
            }
        }
    }
} 