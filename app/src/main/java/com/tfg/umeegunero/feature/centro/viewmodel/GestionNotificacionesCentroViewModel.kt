package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Notificacion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Estado UI para la gestión de notificaciones del centro
 */
data class GestionNotificacionesCentroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val notificaciones: List<Notificacion> = emptyList(),
    val centroId: String = "",
    
    // Estados para crear nueva notificación
    val tituloNueva: String = "",
    val mensajeNueva: String = "",
    val perfilesSeleccionados: Set<TipoUsuario> = emptySet(),
    val gruposSeleccionados: Set<String> = emptySet()
)

/**
 * ViewModel para gestionar las notificaciones del centro
 */
@HiltViewModel
class GestionNotificacionesCentroViewModel @Inject constructor(
    private val notificacionRepository: NotificacionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GestionNotificacionesCentroUiState())
    val uiState: StateFlow<GestionNotificacionesCentroUiState> = _uiState.asStateFlow()
    
    init {
        obtenerCentroActual()
    }
    
    /**
     * Obtiene el ID del centro del usuario actual
     */
    private fun obtenerCentroActual() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    val usuarioResult = usuarioRepository.getUsuarioByEmail(currentUser.email)
                    
                    if (usuarioResult is Result.Success<*>) {
                        val user = usuarioResult.data as Usuario
                        // Obtener el centroId del primer perfil de tipo ADMIN_CENTRO
                        val userCentroId = user.perfiles
                            ?.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                            ?.centroId
                        
                        if (!userCentroId.isNullOrBlank()) {
                            _uiState.update { state -> 
                                state.copy(centroId = userCentroId)
                            }
                            cargarNotificaciones()
                        } else {
                            _uiState.update { state -> 
                                state.copy(error = "El usuario no está asociado a ningún centro.") 
                            }
                        }
                    } else {
                        _uiState.update { state -> 
                            state.copy(error = "No se ha podido obtener la información del usuario.") 
                        }
                    }
                } else {
                    _uiState.update { state -> 
                        state.copy(error = "No hay ningún usuario autenticado.") 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener el centro del usuario")
                _uiState.update { state -> 
                    state.copy(error = "Error al obtener información del centro: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Carga las notificaciones del centro
     */
    fun cargarNotificaciones() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                if (_uiState.value.centroId.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se ha podido determinar el centro al que pertenece."
                        ) 
                    }
                    return@launch
                }
                
                val notificaciones = notificacionRepository.getNotificacionesByCentro(_uiState.value.centroId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        notificaciones = notificaciones.sortedByDescending { notif -> notif.fecha }
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar las notificaciones")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar las notificaciones: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Actualiza el título de la nueva notificación
     */
    fun updateTituloNueva(titulo: String) {
        _uiState.update { it.copy(tituloNueva = titulo) }
    }
    
    /**
     * Actualiza el mensaje de la nueva notificación
     */
    fun updateMensajeNueva(mensaje: String) {
        _uiState.update { it.copy(mensajeNueva = mensaje) }
    }
    
    /**
     * Alterna la selección de un tipo de perfil
     */
    fun togglePerfilSeleccionado(tipo: TipoUsuario) {
        val perfiles = _uiState.value.perfilesSeleccionados.toMutableSet()
        if (perfiles.contains(tipo)) {
            perfiles.remove(tipo)
        } else {
            perfiles.add(tipo)
        }
        _uiState.update { it.copy(perfilesSeleccionados = perfiles) }
    }
    
    /**
     * Alterna la selección de un grupo
     */
    fun toggleGrupoSeleccionado(grupo: String) {
        val grupos = _uiState.value.gruposSeleccionados.toMutableSet()
        if (grupos.contains(grupo)) {
            grupos.remove(grupo)
        } else {
            grupos.add(grupo)
        }
        _uiState.update { it.copy(gruposSeleccionados = grupos) }
    }
    
    /**
     * Crea y envía una nueva notificación
     */
    fun enviarNotificacion() {
        viewModelScope.launch {
            try {
                val titulo = _uiState.value.tituloNueva.trim()
                val mensaje = _uiState.value.mensajeNueva.trim()
                val perfiles = _uiState.value.perfilesSeleccionados
                val grupos = _uiState.value.gruposSeleccionados
                val centroId = _uiState.value.centroId
                
                // Validaciones
                if (titulo.isBlank()) {
                    _uiState.update { it.copy(error = "El título no puede estar vacío.") }
                    return@launch
                }
                
                if (mensaje.isBlank()) {
                    _uiState.update { it.copy(error = "El mensaje no puede estar vacío.") }
                    return@launch
                }
                
                if (perfiles.isEmpty()) {
                    _uiState.update { it.copy(error = "Debes seleccionar al menos un tipo de destinatario.") }
                    return@launch
                }
                
                if (centroId.isBlank()) {
                    _uiState.update { it.copy(error = "No se ha podido determinar el centro al que pertenece.") }
                    return@launch
                }
                
                _uiState.update { it.copy(isLoading = true) }
                
                // Crear la notificación
                val notificacion = Notificacion(
                    id = UUID.randomUUID().toString(),
                    titulo = titulo,
                    mensaje = mensaje,
                    fecha = Timestamp.now(),
                    centroId = centroId,
                    tipoDestinatarios = perfiles.toList(),
                    gruposDestinatarios = if (grupos.isEmpty()) null else grupos.toList(),
                    leida = false,
                    metadata = mapOf("origen" to "CENTRO")
                )
                
                // Guardar en la base de datos
                notificacionRepository.createNotificacion(notificacion)
                
                // Actualizar estado
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Notificación enviada correctamente.",
                        tituloNueva = "",
                        mensajeNueva = "",
                        perfilesSeleccionados = emptySet(),
                        gruposSeleccionados = emptySet()
                    ) 
                }
                
                // Recargar notificaciones
                cargarNotificaciones()
                
            } catch (e: Exception) {
                Timber.e(e, "Error al enviar notificación")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al enviar la notificación: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Elimina una notificación
     */
    fun eliminarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Eliminar de la base de datos
                notificacionRepository.eliminarNotificacion(notificacionId)
                
                // Actualizar estado
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Notificación eliminada correctamente."
                    ) 
                }
                
                // Recargar notificaciones
                cargarNotificaciones()
                
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar notificación")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar la notificación: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Limpia el mensaje actual
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
    
    /**
     * Resetea los campos de la nueva notificación
     */
    fun resetNuevaNotificacion() {
        _uiState.update { 
            it.copy(
                tituloNueva = "",
                mensajeNueva = "",
                perfilesSeleccionados = emptySet(),
                gruposSeleccionados = emptySet()
            ) 
        }
    }
} 