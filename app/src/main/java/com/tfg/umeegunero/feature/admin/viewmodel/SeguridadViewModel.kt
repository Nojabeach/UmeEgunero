package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tfg.umeegunero.data.repository.SeguridadRepository
import com.tfg.umeegunero.data.repository.SeguridadRepository.ConfiguracionSeguridad

/**
 * ViewModel para la gestión de la configuración de seguridad.
 * 
 * Este ViewModel maneja la lógica de negocio y el estado de la
 * pantalla de configuración de seguridad del sistema, permitiendo
 * gestionar todas las políticas y parámetros de seguridad.
 * 
 * ## Características
 * - Gestión de políticas de contraseñas
 * - Control de sesiones
 * - Configuración de acceso
 * - Monitoreo y alertas
 * 
 * ## Estados
 * - Complejidad de contraseñas
 * - Tiempo de sesión
 * - Intentos máximos de acceso
 * - Verificación en dos pasos
 * 
 * ## Eventos
 * - Actualización de configuración
 * - Guardado de cambios
 * - Notificación de errores
 * 
 * @property uiState Estado actual de la interfaz
 * @property repository Repositorio para la persistencia de la configuración
 */
@HiltViewModel
class SeguridadViewModel @Inject constructor(
    private val repository: SeguridadRepository
) : ViewModel() {
    
    /**
     * Estado de la interfaz de usuario para la configuración de seguridad.
     * 
     * @property complejidadPassword Nivel de complejidad requerido para contraseñas (1-4)
     * @property tiempoSesion Tiempo máximo de inactividad en minutos
     * @property maxIntentos Número máximo de intentos de acceso fallidos
     * @property verificacionDosFactores Si está habilitada la verificación en dos pasos
     * @property notificacionesActividad Si están habilitadas las notificaciones de actividad
     * @property registroCompleto Si está habilitado el registro completo de actividad
     * @property bloqueoIP Si está habilitado el bloqueo automático de IPs
     * @property isLoading Si se está cargando o guardando la configuración
     * @property error Mensaje de error actual, si existe
     * @property success Mensaje de éxito actual, si existe
     */
    data class SecurityUiState(
        val complejidadPassword: Int = 2,
        val tiempoSesion: Int = 30,
        val maxIntentos: Int = 3,
        val verificacionDosFactores: Boolean = false,
        val notificacionesActividad: Boolean = true,
        val registroCompleto: Boolean = true,
        val bloqueoIP: Boolean = true,
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: String? = null
    )

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        // Cargar configuración inicial
        viewModelScope.launch {
            repository.configuracion
                .catch { e ->
                    _uiState.update { it.copy(
                        error = "Error al cargar la configuración: ${e.message}"
                    ) }
                }
                .collect { config ->
                    _uiState.update { it.copy(
                        complejidadPassword = config.complejidadPassword,
                        tiempoSesion = config.tiempoSesion,
                        maxIntentos = config.maxIntentos,
                        verificacionDosFactores = config.verificacionDosFactores,
                        notificacionesActividad = config.notificacionesActividad,
                        registroCompleto = config.registroCompleto,
                        bloqueoIP = config.bloqueoIP
                    ) }
                }
        }
    }

    /**
     * Actualiza la complejidad requerida para las contraseñas.
     * 
     * @param nivel Nuevo nivel de complejidad (1-4)
     */
    fun updateComplejidadPassword(nivel: Int) {
        _uiState.update { it.copy(complejidadPassword = nivel) }
    }

    /**
     * Actualiza el tiempo máximo de inactividad de sesión.
     * 
     * @param minutos Tiempo en minutos
     */
    fun updateTiempoSesion(minutos: Int) {
        _uiState.update { it.copy(tiempoSesion = minutos) }
    }

    /**
     * Actualiza el número máximo de intentos de acceso fallidos.
     * 
     * @param intentos Número de intentos
     */
    fun updateMaxIntentos(intentos: Int) {
        _uiState.update { it.copy(maxIntentos = intentos) }
    }

    /**
     * Actualiza el estado de la verificación en dos pasos.
     * 
     * @param enabled Estado de la verificación
     */
    fun updateVerificacionDosFactores(enabled: Boolean) {
        _uiState.update { it.copy(verificacionDosFactores = enabled) }
    }

    /**
     * Actualiza el estado de las notificaciones de actividad.
     * 
     * @param enabled Estado de las notificaciones
     */
    fun updateNotificacionesActividad(enabled: Boolean) {
        _uiState.update { it.copy(notificacionesActividad = enabled) }
    }

    /**
     * Actualiza el estado del registro completo.
     * 
     * @param enabled Estado del registro
     */
    fun updateRegistroCompleto(enabled: Boolean) {
        _uiState.update { it.copy(registroCompleto = enabled) }
    }

    /**
     * Actualiza el estado del bloqueo de IP.
     * 
     * @param enabled Estado del bloqueo
     */
    fun updateBloqueoIP(enabled: Boolean) {
        _uiState.update { it.copy(bloqueoIP = enabled) }
    }

    /**
     * Guarda la configuración de seguridad actual.
     * 
     * Este método persiste los cambios realizados en la configuración
     * de seguridad y actualiza el estado de la UI en consecuencia.
     */
    fun guardarConfiguracion() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null,
                success = null
            ) }

            try {
                val config = ConfiguracionSeguridad(
                    complejidadPassword = _uiState.value.complejidadPassword,
                    tiempoSesion = _uiState.value.tiempoSesion,
                    maxIntentos = _uiState.value.maxIntentos,
                    verificacionDosFactores = _uiState.value.verificacionDosFactores,
                    notificacionesActividad = _uiState.value.notificacionesActividad,
                    registroCompleto = _uiState.value.registroCompleto,
                    bloqueoIP = _uiState.value.bloqueoIP
                )
                
                repository.actualizarConfiguracion(config)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    success = "Configuración guardada correctamente"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al guardar la configuración: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia el mensaje de éxito actual.
     */
    fun clearSuccess() {
        _uiState.update { it.copy(success = null) }
    }
} 