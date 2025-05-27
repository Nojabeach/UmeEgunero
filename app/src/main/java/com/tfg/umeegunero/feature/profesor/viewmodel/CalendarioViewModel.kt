package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.toLocalDate
import com.tfg.umeegunero.util.toTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * Estado UI para la pantalla de Calendario
 */
data class CalendarioUiState(
    val mesSeleccionado: YearMonth = YearMonth.now(),
    val diaSeleccionado: LocalDate? = null,
    val eventos: List<Evento> = emptyList(),
    val eventosDiaSeleccionado: List<Evento> = emptyList(),
    val mostrarEventos: Boolean = false,
    val mostrarDialogoEvento: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para gestionar el calendario y eventos del profesor
 */
@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarioUiState())
    val uiState: StateFlow<CalendarioUiState> = _uiState.asStateFlow()
    
    private var profesorId: String? = null
    private var centroId: String? = null

    init {
        // Obtener ID del profesor actual
        viewModelScope.launch {
            try {
                usuarioRepository.getUsuarioActual().collectLatest<Result<Usuario>> { result ->
                    when (result) {
                        is Result.Success<*> -> {
                            (result.data as? Usuario)?.let { user ->
                                profesorId = user.dni
                                user.perfiles.find { it.tipo == TipoUsuario.PROFESOR }?.let { perfil ->
                                    centroId = perfil.centroId
                                    cargarEventos()
                                }
                            }
                        }
                        is Result.Error -> {
                            Timber.e(result.exception, "Error al obtener usuario actual")
                            _uiState.update { 
                                it.copy(error = "Error al cargar datos de usuario: ${result.exception?.message}")
                            }
                        }
                        is Result.Loading<*> -> {
                            // Esperando datos
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener usuario actual")
                _uiState.update { 
                    it.copy(error = "Error al cargar datos de usuario: ${e.message}")
                }
            }
        }
    }

    /**
     * Carga los eventos del mes seleccionado
     */
    private fun cargarEventos() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val eventos = if (centroId != null) {
                    eventoRepository.obtenerEventosPorCentro(centroId!!)
                } else {
                    emptyList()
                }
                
                _uiState.update { 
                    it.copy(
                        eventos = eventos,
                        isLoading = false
                    )
                }
                
                actualizarEventosDiaSeleccionado()
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar eventos: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Selecciona un día del calendario
     */
    fun seleccionarDia(dia: LocalDate) {
        _uiState.update { 
            it.copy(
                diaSeleccionado = dia,
                mostrarEventos = true
            )
        }
        actualizarEventosDiaSeleccionado()
    }

    /**
     * Actualiza la lista de eventos para el día seleccionado
     */
    private fun actualizarEventosDiaSeleccionado() {
        val diaSeleccionado = _uiState.value.diaSeleccionado ?: return
        
        val eventosDelDia = _uiState.value.eventos.filter { evento ->
            val fechaEvento = evento.fecha.toLocalDate()
            fechaEvento.isEqual(diaSeleccionado)
        }
        
        _uiState.update { 
            it.copy(eventosDiaSeleccionado = eventosDelDia)
        }
    }

    /**
     * Avanza al mes siguiente
     */
    fun mesSiguiente() {
        _uiState.update { 
            it.copy(
                mesSeleccionado = it.mesSeleccionado.plusMonths(1),
                diaSeleccionado = null,
                mostrarEventos = false
            )
        }
    }

    /**
     * Retrocede al mes anterior
     */
    fun mesAnterior() {
        _uiState.update { 
            it.copy(
                mesSeleccionado = it.mesSeleccionado.minusMonths(1),
                diaSeleccionado = null,
                mostrarEventos = false
            )
        }
    }

    /**
     * Muestra el diálogo para crear un evento
     */
    fun mostrarDialogoCrearEvento() {
        if (_uiState.value.diaSeleccionado == null) {
            _uiState.update { it.copy(error = "Selecciona un día primero") }
            return
        }
        
        _uiState.update { it.copy(mostrarDialogoEvento = true) }
    }

    /**
     * Oculta el diálogo para crear un evento
     */
    fun ocultarDialogoCrearEvento() {
        _uiState.update { it.copy(mostrarDialogoEvento = false) }
    }

    /**
     * Crea un nuevo evento
     */
    fun crearEvento(titulo: String, descripcion: String, tipoEvento: TipoEvento) {
        val diaSeleccionado = _uiState.value.diaSeleccionado
        val profesorId = this.profesorId
        val centroId = this.centroId
        
        if (diaSeleccionado == null) {
            _uiState.update { it.copy(error = "No se puede crear el evento: no hay día seleccionado") }
            return
        }
        
        // Validar que tenemos el ID del usuario y del centro
        if (profesorId.isNullOrEmpty() || centroId.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    // Intentar obtener los datos del usuario actual
                    val currentUser = authRepository.getCurrentUser()
                    if (currentUser != null) {
                        this@CalendarioViewModel.profesorId = currentUser.dni
                        
                        // Buscar perfil de profesor para obtener centroId
                        val perfilProfesor = currentUser.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
                        if (perfilProfesor != null && perfilProfesor.centroId.isNotEmpty()) {
                            this@CalendarioViewModel.centroId = perfilProfesor.centroId
                            // Intentar crear el evento de nuevo con los datos obtenidos
                            crearEvento(titulo, descripcion, tipoEvento)
                            return@launch
                        }
                    }
                    
                    // Si llegamos aquí, no se pudieron obtener los datos necesarios
                    _uiState.update { it.copy(error = "No se puede crear el evento: no se pudo identificar al usuario o su centro") }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener datos de usuario para crear evento")
                    _uiState.update { it.copy(error = "Error al crear evento: ${e.message}") }
                }
            }
            return
        }
        
        // Crear objeto evento
        val fechaHora = LocalDateTime.of(diaSeleccionado, java.time.LocalTime.of(8, 0))
        val timestamp = fechaHora.toTimestamp()
        val nuevoEvento = Evento(
            id = "",  // Se asignará en el repositorio
            titulo = titulo,
            descripcion = descripcion,
            fecha = timestamp,
            tipo = tipoEvento,
            creadorId = profesorId,
            centroId = centroId
        )
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, mostrarDialogoEvento = false) }
                
                eventoRepository.crearEvento(nuevoEvento)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Evento creado correctamente"
                    )
                }
                
                cargarEventos()
            } catch (e: Exception) {
                Timber.e(e, "Error al crear evento")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al crear evento: ${e.message}"
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
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                eventoRepository.eliminarEvento(evento.id)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mensaje = "Evento eliminado correctamente"
                    )
                }
                
                cargarEventos()
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar evento")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al eliminar evento: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia el mensaje de éxito
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 