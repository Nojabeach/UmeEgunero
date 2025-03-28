package com.tfg.umeegunero.feature.common.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Notificacion
import com.tfg.umeegunero.data.model.PrioridadNotificacion
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de notificaciones
 */
data class NotificacionesUiState(
    val notificaciones: List<Notificacion> = emptyList(),
    val notificacionesOriginales: List<Notificacion> = emptyList(), // Lista completa sin filtros
    val isLoading: Boolean = false,
    val error: String? = null,
    val filtroTipo: TipoNotificacion? = null // Filtro actual
)

/**
 * ViewModel para la gestión de notificaciones
 */
@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val notificacionRepository: NotificacionRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionesUiState())
    val uiState: StateFlow<NotificacionesUiState> = _uiState.asStateFlow()

    /**
     * Carga las notificaciones del usuario actual
     */
    fun cargarNotificaciones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un entorno real, obtendríamos el ID del usuario actual
                val usuarioId = usuarioRepository.getUsuarioActualId()
                
                if (usuarioId.isNotEmpty()) {
                    // Cargamos notificaciones del usuario, simulando en desarrollo
                    // notificacionRepository.getNotificacionesUsuario(usuarioId).collectLatest { result ->
                    // Simulación de datos
                    val notificacionesSimuladas = generarNotificacionesSimuladas()
                    _uiState.update { 
                        it.copy(
                            notificaciones = notificacionesSimuladas,
                            notificacionesOriginales = notificacionesSimuladas,
                            isLoading = false
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo identificar al usuario actual",
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar notificaciones: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error al cargar notificaciones")
            }
        }
    }

    /**
     * Marca una notificación como leída
     */
    fun marcarComoLeida(notificacionId: String) {
        viewModelScope.launch {
            try {
                // En un entorno real, llamaríamos al repositorio
                // notificacionRepository.marcarComoLeida(notificacionId)
                
                // Simulación
                _uiState.update { currentState ->
                    val notificacionesActualizadas = currentState.notificacionesOriginales.map { 
                        if (it.id == notificacionId) it.copy(leida = true) else it 
                    }
                    
                    // Aplicamos el filtro actual
                    val notificacionesFiltradas = aplicarFiltroInterno(
                        notificacionesActualizadas, 
                        currentState.filtroTipo
                    )
                    
                    currentState.copy(
                        notificaciones = notificacionesFiltradas,
                        notificacionesOriginales = notificacionesActualizadas
                    )
                }
                
                Timber.d("Notificación marcada como leída: $notificacionId")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al marcar notificación como leída: ${e.message}") 
                }
                Timber.e(e, "Error al marcar notificación como leída: $notificacionId")
            }
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            try {
                // En un entorno real llamaríamos al repositorio
                // val usuarioId = usuarioRepository.getUsuarioActualId()
                // notificacionRepository.marcarTodasComoLeidas(usuarioId)
                
                // Simulación
                _uiState.update { currentState ->
                    val notificacionesActualizadas = currentState.notificacionesOriginales.map { 
                        it.copy(leida = true) 
                    }
                    
                    // Aplicamos el filtro actual
                    val notificacionesFiltradas = aplicarFiltroInterno(
                        notificacionesActualizadas, 
                        currentState.filtroTipo
                    )
                    
                    currentState.copy(
                        notificaciones = notificacionesFiltradas,
                        notificacionesOriginales = notificacionesActualizadas
                    )
                }
                
                Timber.d("Todas las notificaciones marcadas como leídas")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al marcar todas las notificaciones como leídas: ${e.message}") 
                }
                Timber.e(e, "Error al marcar todas las notificaciones como leídas")
            }
        }
    }

    /**
     * Elimina una notificación
     */
    fun eliminarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            try {
                // En un entorno real llamaríamos al repositorio
                // notificacionRepository.eliminarNotificacion(notificacionId)
                
                // Simulación
                _uiState.update { currentState ->
                    val notificacionesActualizadas = currentState.notificacionesOriginales.filter { 
                        it.id != notificacionId 
                    }
                    
                    // Aplicamos el filtro actual
                    val notificacionesFiltradas = aplicarFiltroInterno(
                        notificacionesActualizadas, 
                        currentState.filtroTipo
                    )
                    
                    currentState.copy(
                        notificaciones = notificacionesFiltradas,
                        notificacionesOriginales = notificacionesActualizadas
                    )
                }
                
                Timber.d("Notificación eliminada: $notificacionId")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Error al eliminar notificación: ${e.message}") 
                }
                Timber.e(e, "Error al eliminar notificación: $notificacionId")
            }
        }
    }

    /**
     * Aplica un filtro por tipo de notificación
     */
    fun aplicarFiltro(tipo: TipoNotificacion?) {
        _uiState.update { currentState ->
            val notificacionesFiltradas = aplicarFiltroInterno(
                currentState.notificacionesOriginales, 
                tipo
            )
            
            currentState.copy(
                notificaciones = notificacionesFiltradas,
                filtroTipo = tipo
            )
        }
    }
    
    /**
     * Función auxiliar que aplica un filtro a una lista de notificaciones
     */
    private fun aplicarFiltroInterno(
        notificaciones: List<Notificacion>,
        tipo: TipoNotificacion?
    ): List<Notificacion> {
        return if (tipo == null) {
            notificaciones
        } else {
            notificaciones.filter { it.tipo == tipo }
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Genera notificaciones simuladas para desarrollo
     * En un entorno real, estas vendrían de Firestore
     */
    private fun generarNotificacionesSimuladas(): List<Notificacion> {
        val ahora = Timestamp.now()
        val ayer = Timestamp(Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)))
        val anteayer = Timestamp(Date(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)))
        val haceUnaSemana = Timestamp(Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)))
        
        return listOf(
            Notificacion(
                id = "1",
                titulo = "Actualización del sistema",
                mensaje = "Se ha completado la actualización programada del sistema. La aplicación ahora ofrece un mejor rendimiento y nuevas funcionalidades.",
                fecha = ahora,
                leida = true,
                tipo = TipoNotificacion.SISTEMA,
                remitente = "Sistema UmeEgunero",
                prioridad = PrioridadNotificacion.NORMAL
            ),
            Notificacion(
                id = "2",
                titulo = "Recordatorio de reunión",
                mensaje = "Mañana tendrá lugar la reunión de coordinación a las 16:00h en la sala principal.",
                fecha = ahora,
                leida = false,
                tipo = TipoNotificacion.EVENTO,
                remitente = "Coordinación",
                prioridad = PrioridadNotificacion.ALTA
            ),
            Notificacion(
                id = "3",
                titulo = "Nuevo mensaje de Ana Gómez",
                mensaje = "Hola, ¿podrías enviarme el informe de progreso de Luis? Necesito revisarlo antes de la reunión con los padres.",
                fecha = ayer,
                leida = false,
                tipo = TipoNotificacion.MENSAJE,
                remitente = "Ana Gómez",
                prioridad = PrioridadNotificacion.NORMAL
            ),
            Notificacion(
                id = "4",
                titulo = "Cierre por mantenimiento",
                mensaje = "El sistema estará en mantenimiento el próximo sábado de 2:00 a 4:00 AM. Durante este período la aplicación no estará disponible.",
                fecha = anteayer,
                leida = true,
                tipo = TipoNotificacion.SISTEMA,
                remitente = "Administración",
                prioridad = PrioridadNotificacion.NORMAL
            ),
            Notificacion(
                id = "5",
                titulo = "Alerta de seguridad",
                mensaje = "Hemos detectado un intento de acceso inusual a tu cuenta. Por favor, verifica tu contraseña y activa la verificación en dos pasos si es necesario.",
                fecha = anteayer,
                leida = false,
                tipo = TipoNotificacion.ALERTA,
                remitente = "Seguridad UmeEgunero",
                prioridad = PrioridadNotificacion.URGENTE
            ),
            Notificacion(
                id = "6",
                titulo = "Evaluación pendiente",
                mensaje = "Tienes pendiente completar la evaluación del segundo trimestre para el grupo 3B antes del viernes.",
                fecha = haceUnaSemana,
                leida = true,
                tipo = TipoNotificacion.ACADEMICO,
                remitente = "Sistema Académico",
                prioridad = PrioridadNotificacion.ALTA
            ),
            Notificacion(
                id = "7",
                titulo = "Actualización de permisos",
                mensaje = "Se han actualizado tus permisos en el sistema. Ahora puedes acceder a los informes de evaluación de todos los grupos asignados.",
                fecha = haceUnaSemana,
                leida = true,
                tipo = TipoNotificacion.SISTEMA,
                remitente = "Administración",
                prioridad = PrioridadNotificacion.BAJA
            )
        )
    }
} 