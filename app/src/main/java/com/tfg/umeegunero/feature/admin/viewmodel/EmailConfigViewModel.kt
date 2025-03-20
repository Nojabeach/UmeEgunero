package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.EmailSoporteConfig
import com.tfg.umeegunero.data.repository.ConfigRepository
import com.tfg.umeegunero.data.service.RemoteConfigService
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
    val emailRemitente: String = "",
    val passwordTemporal: String = "", // Contraseña temporal solo para UI
    val nombreRemitente: String = "",
    val usarEmailUsuarioComoRemitente: Boolean = false,
    val mostrarPassword: Boolean = false,
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
    private val configRepository: ConfigRepository,
    private val remoteConfigService: RemoteConfigService
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
                
                // Obtenemos la contraseña de Remote Config
                val password = remoteConfigService.getSMTPPassword()
                
                _uiState.update { 
                    it.copy(
                        emailDestino = config.emailDestino,
                        emailRemitente = config.emailRemitente,
                        passwordTemporal = password, // Cargamos la contraseña para la UI
                        nombreRemitente = config.nombreRemitente,
                        usarEmailUsuarioComoRemitente = config.usarEmailUsuarioComoRemitente,
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
     * Actualiza el email del remitente
     */
    fun updateEmailRemitente(email: String) {
        _uiState.update { 
            it.copy(
                emailRemitente = email,
                cambiosPendientes = true,
                errores = it.errores - "emailRemitente"
            ) 
        }
    }
    
    /**
     * Actualiza la contraseña del remitente (solo temporal para la UI)
     */
    fun updatePasswordRemitente(password: String) {
        _uiState.update { 
            it.copy(
                passwordTemporal = password,
                cambiosPendientes = true,
                errores = it.errores - "passwordRemitente"
            ) 
        }
    }
    
    /**
     * Actualiza el nombre del remitente
     */
    fun updateNombreRemitente(nombre: String) {
        _uiState.update { 
            it.copy(
                nombreRemitente = nombre,
                cambiosPendientes = true
            ) 
        }
    }
    
    /**
     * Actualiza la opción de usar el email del usuario como remitente
     */
    fun updateUsarEmailUsuarioComoRemitente(usar: Boolean) {
        _uiState.update { 
            it.copy(
                usarEmailUsuarioComoRemitente = usar,
                cambiosPendientes = true
            ) 
        }
    }
    
    /**
     * Cambia la visibilidad de la contraseña
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(mostrarPassword = !it.mostrarPassword) }
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
        
        // Validar email remitente
        if (state.emailRemitente.isBlank()) {
            errores["emailRemitente"] = "El email del remitente es obligatorio"
        } else if (!state.emailRemitente.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))) {
            errores["emailRemitente"] = "Ingresa un email válido"
        }
        
        // Validar contraseña
        if (state.passwordTemporal.isBlank()) {
            errores["passwordRemitente"] = "La contraseña es obligatoria"
        } else if (state.passwordTemporal.length < 8) {
            errores["passwordRemitente"] = "La contraseña debe tener al menos 8 caracteres"
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
                    emailRemitente = state.emailRemitente,
                    nombreRemitente = state.nombreRemitente,
                    usarEmailUsuarioComoRemitente = state.usarEmailUsuarioComoRemitente,
                    ultimaActualizacion = Timestamp.now()
                )
                
                // Guardamos el estado del modelo en Firestore
                val resultado = configRepository.saveEmailSoporteConfig(config)
                
                // Para la contraseña, necesitaríamos un método diferente para Firebase Remote Config
                // Nota: La contraseña no se guarda aquí, se debe configurar manualmente desde la consola de Firebase
                // Se muestra un mensaje informativo al usuario
                
                if (resultado) {
                    _uiState.update { 
                        it.copy(
                            isSaving = false,
                            cambiosPendientes = false,
                            mensaje = "Configuración guardada correctamente.\nLa contraseña se debe modificar desde la consola de Firebase.",
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
    
    /**
     * Recarga la configuración descartando los cambios
     */
    fun descartarCambios() {
        cargarConfiguracion()
    }
} 