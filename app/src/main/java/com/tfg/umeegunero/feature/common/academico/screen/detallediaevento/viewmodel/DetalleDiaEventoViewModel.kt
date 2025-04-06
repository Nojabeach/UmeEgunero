package com.tfg.umeegunero.feature.common.academico.screen.detallediaevento.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.toTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Estado UI para la pantalla de detalle de día con eventos
 */
data class DetalleDiaEventoUiState(
    val eventos: List<Evento> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val mostrarDialogoCrearEvento: Boolean = false,
    val eventoParaEditar: Evento? = null,
    val eventoParaEliminar: Evento? = null
)

/**
 * ViewModel para la pantalla de detalle de día con eventos
 */
@HiltViewModel
class DetalleDiaEventoViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetalleDiaEventoUiState())
    val uiState: StateFlow<DetalleDiaEventoUiState> = _uiState.asStateFlow()
    
    private var userId: String? = null
    private var centroId: String? = null
    
    init {
        viewModelScope.launch {
            // Obtener ID del usuario actual
            val currentUser = usuarioRepository.obtenerUsuarioActual()
            userId = currentUser?.dni
            centroId = currentUser?.perfiles?.firstOrNull()?.centroId
        }
    }
    
    /**
     * Carga los eventos para una fecha específica
     */
    fun cargarEventosPorFecha(fecha: LocalDate) {
        if (centroId == null) {
            _uiState.update { it.copy(error = "No se ha podido determinar el centro educativo") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            
            try {
                // Obtener todos los eventos del centro
                val eventos = eventoRepository.obtenerEventosPorCentro(centroId!!)
                
                // Filtrar por fecha
                val eventosFecha = eventos.filter { evento ->
                    val fechaEvento = evento.fecha.toDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                    fechaEvento == fecha
                }
                
                _uiState.update { 
                    it.copy(
                        eventos = eventosFecha,
                        cargando = false,
                        error = null
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos por fecha")
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = "Error al cargar eventos: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Crea un nuevo evento
     */
    fun crearEvento(
        titulo: String,
        descripcion: String,
        tipo: TipoEvento,
        fecha: LocalDate,
        ubicacion: String,
        recordatorio: Boolean,
        tiempoRecordatorioMinutos: Int
    ) {
        if (userId == null || centroId == null) {
            _uiState.update { it.copy(error = "No se ha podido determinar el usuario o centro") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    cargando = true,
                    mostrarDialogoCrearEvento = false
                ) 
            }
            
            try {
                // Convertir LocalDate a Timestamp (por defecto a las 8:00)
                val fechaHora = LocalDateTime.of(fecha, LocalTime.of(8, 0))
                val timestamp = fechaHora.toTimestamp()
                
                // Crear objeto evento
                val nuevoEvento = Evento(
                    titulo = titulo,
                    descripcion = descripcion,
                    fecha = timestamp,
                    tipo = tipo,
                    creadorId = userId!!,
                    centroId = centroId!!,
                    recordatorio = recordatorio,
                    tiempoRecordatorioMinutos = tiempoRecordatorioMinutos,
                    ubicacion = ubicacion
                )
                
                // Guardar evento
                eventoRepository.crearEvento(nuevoEvento)
                
                // Recargar eventos
                cargarEventosPorFecha(fecha)
                
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        mensaje = "Evento creado correctamente"
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al crear evento")
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = "Error al crear evento: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Actualiza un evento existente
     */
    fun actualizarEvento(evento: Evento) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    cargando = true,
                    eventoParaEditar = null
                ) 
            }
            
            try {
                // Actualizar en repositorio
                eventoRepository.actualizarEvento(evento)
                
                // Recargar eventos
                val fechaEvento = evento.fecha.toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                cargarEventosPorFecha(fechaEvento)
                
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        mensaje = "Evento actualizado correctamente"
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar evento")
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = "Error al actualizar evento: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Elimina un evento
     */
    fun eliminarEvento(evento: Evento) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    cargando = true,
                    eventoParaEliminar = null
                ) 
            }
            
            try {
                // Eliminar del repositorio
                eventoRepository.eliminarEvento(evento.id)
                
                // Recargar eventos
                val fechaEvento = evento.fecha.toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                cargarEventosPorFecha(fechaEvento)
                
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        mensaje = "Evento eliminado correctamente"
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar evento")
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = "Error al eliminar evento: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    // Métodos para gestionar diálogos
    
    fun mostrarDialogoCrearEvento() {
        _uiState.update { it.copy(mostrarDialogoCrearEvento = true) }
    }
    
    fun ocultarDialogoCrearEvento() {
        _uiState.update { it.copy(mostrarDialogoCrearEvento = false) }
    }
    
    fun mostrarDialogoEditarEvento(evento: Evento) {
        _uiState.update { it.copy(eventoParaEditar = evento) }
    }
    
    fun ocultarDialogoEditarEvento() {
        _uiState.update { it.copy(eventoParaEditar = null) }
    }
    
    fun mostrarDialogoConfirmarEliminar(evento: Evento) {
        _uiState.update { it.copy(eventoParaEliminar = evento) }
    }
    
    fun ocultarDialogoConfirmarEliminar() {
        _uiState.update { it.copy(eventoParaEliminar = null) }
    }
    
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 