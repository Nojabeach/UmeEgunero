package com.tfg.umeegunero.feature.common.academico.screen.detallediaevento.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.model.TipoUsuario
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
    val eventoParaEliminar: Evento? = null,
    val eventosConCreador: Map<String, UsuarioCreador> = emptyMap()
)

/**
 * Datos del usuario creador del evento
 */
data class UsuarioCreador(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val tipo: TipoUsuario
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
                
                // Obtener información de los creadores
                val eventosConCreador = mutableMapOf<String, UsuarioCreador>()
                
                // Procesar cada evento para obtener los datos de su creador
                eventosFecha.forEach { evento ->
                    if (evento.creadorId.isNotEmpty()) {
                        obtenerInfoCreador(evento.creadorId)?.let { creador ->
                            eventosConCreador[evento.id] = creador
                        }
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        eventos = eventosFecha,
                        cargando = false,
                        error = null,
                        eventosConCreador = eventosConCreador
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
     * Obtiene la información del usuario creador
     */
    private suspend fun obtenerInfoCreador(creadorId: String): UsuarioCreador? {
        return try {
            when (val result = usuarioRepository.getUsuarioByDni(creadorId)) {
                is Result.Success -> {
                    val usuario = result.data
                    if (usuario != null) {
                        val tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.OTRO
                        UsuarioCreador(
                            id = usuario.dni,
                            nombre = usuario.nombre,
                            apellidos = usuario.apellidos,
                            email = usuario.email ?: "",
                            tipo = tipoUsuario
                        )
                    } else null
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al obtener usuario creador: ${result.exception?.message ?: "Error desconocido"}")
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuario creador: ${e.message ?: "Error desconocido"}")
            null
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
                Timber.d("Iniciando eliminación del evento: ${evento.id}, título: ${evento.titulo}")
                
                // Eliminar del repositorio
                val resultado = eventoRepository.eliminarEvento(evento.id)
                
                when (resultado) {
                    is Result.Success -> {
                        Timber.d("Evento eliminado correctamente del repositorio: ${evento.id}")
                        
                        // Recargar eventos
                        val fechaEvento = evento.fecha.toDate().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        Timber.d("Recargando eventos para la fecha: $fechaEvento")
                        cargarEventosPorFecha(fechaEvento)
                        
                        _uiState.update { 
                            it.copy(
                                cargando = false,
                                mensaje = "Evento eliminado correctamente"
                            ) 
                        }
                    }
                    
                    is Result.Error -> {
                        val errorMsg = resultado.exception?.message ?: "Error desconocido"
                        Timber.e(resultado.exception, "Error al eliminar evento: ${evento.id}, $errorMsg")
                        _uiState.update { 
                            it.copy(
                                cargando = false,
                                error = "Error al eliminar evento: $errorMsg"
                            ) 
                        }
                    }
                    
                    is Result.Loading -> {
                        // No hacer nada, ya estamos en estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al eliminar evento: ${evento.id}, ${e.message}")
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = "Error al eliminar evento: ${e.message ?: "Error desconocido"}"
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