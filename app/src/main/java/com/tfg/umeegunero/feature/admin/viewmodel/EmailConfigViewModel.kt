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
        val errores = mutableMapOf<String, String>()
        val state = _uiState.value
        
        // Validar email destino
        if (state.emailDestino.isBlank()) {
            errores["emailDestino"] = "El email de destino es obligatorio"
        } else if (!state.emailDestino.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))) {
            errores["emailDestino"] = "Ingresa un email válido"
        }
        
        _uiState.update { it.copy(errores = errores) }
        return errores.isEmpty()
    }
    
    /**
     * Guarda la configuración de email
     */
    fun guardarConfiguracion() {
        if (!validarCampos()) return
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                
                val state = _uiState.value
                val config = EmailSoporteConfig(
                    emailDestino = state.emailDestino,
                    ultimaActualizacion = Timestamp.now()
                )
                
                // Guardamos el estado del modelo en Firestore
                val resultado = configRepository.saveEmailSoporteConfig(config)
                
                if (resultado) {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            cambiosPendientes = false,
                            mensaje = "Configuración guardada correctamente",
                            ultimaActualizacion = config.ultimaActualizacion
                        ) 
                    }
                    Timber.d("Configuración guardada correctamente")
                } else {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            mensaje = "Error al guardar la configuración"
                        ) 
                    }
                    Timber.e("Error al guardar configuración")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar configuración de email")
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        mensaje = "Error: ${e.message}"
                    ) 
                }
            }
        }
    }
} 