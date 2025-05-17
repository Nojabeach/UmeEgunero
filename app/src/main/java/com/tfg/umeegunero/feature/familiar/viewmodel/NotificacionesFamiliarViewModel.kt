package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.feature.familiar.screen.Notificacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de notificaciones de familiar
 */
data class NotificacionesFamiliarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val notificaciones: List<Notificacion> = emptyList()
)

/**
 * ViewModel para gestionar las notificaciones de familiares en la aplicación UmeEgunero.
 * 
 * Se encarga de cargar, filtrar y gestionar las notificaciones para el familiar,
 * incluyendo alertas, comunicados y mensajes.
 * 
 * @property usuarioRepository Repositorio para operaciones relacionadas con el usuario
 */
@HiltViewModel
class NotificacionesFamiliarViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificacionesFamiliarUiState())
    val uiState: StateFlow<NotificacionesFamiliarUiState> = _uiState.asStateFlow()

    init {
        cargarNotificaciones()
    }

    /**
     * Carga las notificaciones del familiar actual
     */
    fun cargarNotificaciones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Obtener el usuario actual
                val usuarioActual = usuarioRepository.getCurrentUser()
                
                if (usuarioActual != null) {
                    // Aquí se implementaría la lógica real para obtener notificaciones
                    // Por ahora, creamos datos de ejemplo
                    val notificacionesDemo = obtenerNotificacionesDemo()
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            notificaciones = notificacionesDemo,
                            error = null
                        )
                    }
                    
                    Timber.d("Cargadas ${notificacionesDemo.size} notificaciones para familiar")
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se ha encontrado el usuario actual"
                        )
                    }
                    Timber.e("No se pudo obtener el usuario actual al cargar notificaciones")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar notificaciones: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar notificaciones: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Marca una notificación como leída
     */
    fun marcarComoLeida(notificacionId: String) {
        viewModelScope.launch {
            try {
                // Aquí implementar la lógica real para marcar como leída
                Timber.d("Notificación $notificacionId marcada como leída")
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar notificación como leída: ${e.message}")
            }
        }
    }

    /**
     * Elimina una notificación
     */
    fun eliminarNotificacion(notificacionId: String) {
        viewModelScope.launch {
            try {
                // Filtrar la notificación eliminada
                val notificacionesActualizadas = _uiState.value.notificaciones.filter { 
                    it.id != notificacionId 
                }
                
                _uiState.update { 
                    it.copy(notificaciones = notificacionesActualizadas) 
                }
                
                // Aquí implementar la lógica real para eliminar de la base de datos
                Timber.d("Notificación $notificacionId eliminada")
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar notificación: ${e.message}")
            }
        }
    }

    /**
     * Crea notificaciones de ejemplo para demostración
     */
    private fun obtenerNotificacionesDemo(): List<Notificacion> {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return listOf(
            Notificacion(
                id = UUID.randomUUID().toString(),
                titulo = "Calificación trimestral disponible",
                contenido = "La calificación del primer trimestre de Matemáticas ya está disponible.",
                fecha = formatter.format(Date(System.currentTimeMillis() - 86400000)) // Ayer
            ),
            Notificacion(
                id = UUID.randomUUID().toString(),
                titulo = "Reunión de padres programada",
                contenido = "Se ha programado una reunión para el próximo viernes a las 17:00.",
                fecha = formatter.format(Date(System.currentTimeMillis() - 172800000)) // Hace 2 días
            ),
            Notificacion(
                id = UUID.randomUUID().toString(),
                titulo = "Tarea pendiente de revisar",
                contenido = "Hay una nueva tarea de Inglés pendiente de revisar.",
                fecha = formatter.format(Date(System.currentTimeMillis() - 259200000)) // Hace 3 días
            )
        )
    }
} 