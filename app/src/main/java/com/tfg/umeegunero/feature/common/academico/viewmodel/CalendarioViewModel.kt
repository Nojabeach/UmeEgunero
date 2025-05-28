package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
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
import java.time.YearMonth
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de calendario
 */
data class CalendarioUiState(
    val eventos: List<Evento> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEventDialog: Boolean = false,
    val selectedEventType: TipoEvento? = null,
    val eventDescription: String = "",
    val eventTitle: String? = null,
    val eventLocation: String? = null,
    val eventTime: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val showTimePicker: Boolean = false,
    val selectedHour: Int = 0,
    val selectedMinute: Int = 0
)

/**
 * Información del usuario actual
 */
data class UserInfo(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val tipoUsuario: TipoUsuario,
    val centroId: String
)

/**
 * ViewModel para la pantalla de calendario
 */
@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val calendarioRepository: CalendarioRepository,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarioUiState())
    val uiState: StateFlow<CalendarioUiState> = _uiState.asStateFlow()

    init {
        loadEventos()
    }

    /**
     * Carga los eventos del calendario
     */
    fun loadEventos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val eventos = calendarioRepository.getEventos()
                _uiState.update { 
                    it.copy(
                        eventos = eventos,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar eventos: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error al cargar eventos")
            }
        }
    }

    /**
     * Actualiza la fecha seleccionada
     */
    fun updateSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /**
     * Actualiza el mes actual
     */
    fun updateCurrentMonth(month: YearMonth) {
        _uiState.update { it.copy(currentMonth = month) }
    }

    /**
     * Muestra el diálogo para añadir un evento
     */
    fun showEventDialog() {
        _uiState.update { 
            it.copy(
                showEventDialog = true,
                selectedEventType = null,
                eventDescription = "",
                eventTitle = "",
                eventLocation = "",
                eventTime = null
            ) 
        }
    }

    /**
     * Oculta el diálogo para añadir un evento
     */
    fun hideEventDialog() {
        _uiState.update { it.copy(showEventDialog = false) }
    }

    /**
     * Actualiza el tipo de evento seleccionado
     */
    fun updateSelectedEventType(tipo: TipoEvento) {
        _uiState.update { it.copy(selectedEventType = tipo) }
    }

    /**
     * Actualiza la descripción del evento
     */
    fun updateEventDescription(description: String) {
        _uiState.update { it.copy(eventDescription = description) }
    }
    
    /**
     * Actualiza el título del evento
     */
    fun updateEventTitle(title: String) {
        _uiState.update { it.copy(eventTitle = title) }
    }
    
    /**
     * Actualiza la ubicación del evento
     */
    fun updateEventLocation(location: String) {
        _uiState.update { it.copy(eventLocation = location) }
    }
    
    /**
     * Actualiza la hora del evento
     */
    fun updateEventTime(time: String) {
        _uiState.update { it.copy(eventTime = time) }
    }

    /**
     * Muestra el selector de hora
     */
    fun showTimePickerDialog() {
        _uiState.update { it.copy(showTimePicker = true) }
    }
    
    /**
     * Oculta el selector de hora
     */
    fun hideTimePickerDialog() {
        _uiState.update { it.copy(showTimePicker = false) }
    }
    
    /**
     * Actualiza la hora seleccionada
     */
    fun updateSelectedHour(hour: Int) {
        _uiState.update { it.copy(selectedHour = hour) }
    }
    
    /**
     * Actualiza el minuto seleccionado
     */
    fun updateSelectedMinute(minute: Int) {
        _uiState.update { it.copy(selectedMinute = minute) }
    }
    
    /**
     * Confirma la selección de tiempo
     */
    fun confirmTimeSelection() {
        val hour = _uiState.value.selectedHour
        val minute = _uiState.value.selectedMinute
        val formattedTime = String.format("%02d:%02d", hour, minute)
        
        _uiState.update { 
            it.copy(
                eventTime = formattedTime,
                showTimePicker = false
            ) 
        }
    }

    /**
     * Guarda un nuevo evento
     */
    fun saveEvento() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState.selectedEventType == null) {
                _uiState.update { it.copy(error = "Debes seleccionar un tipo de evento") }
                return@launch
            }
            
            if (currentState.eventDescription.isBlank() && (currentState.eventTitle == null || currentState.eventTitle.isBlank())) {
                _uiState.update { it.copy(error = "Debes proporcionar un título o descripción") }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener el usuario actual y su centro de manera adecuada
                val usuarioId = calendarioRepository.obtenerUsuarioId()
                val centroId = calendarioRepository.obtenerCentroId()
                
                // Construir título y descripción completos
                val title = currentState.eventTitle?.takeIf { it.isNotBlank() } 
                    ?: currentState.eventDescription.lines().firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: "Evento"
                
                // Construir descripción que incluya ubicación y hora si se especificaron
                var descripcionCompleta = currentState.eventDescription
                
                if (!currentState.eventLocation.isNullOrBlank()) {
                    descripcionCompleta += "\nLugar: ${currentState.eventLocation}"
                }
                
                if (!currentState.eventTime.isNullOrBlank()) {
                    descripcionCompleta += "\nHora: ${currentState.eventTime}"
                }
                
                val newEvent = Evento(
                    id = "", // Será generado por Firestore
                    titulo = title,
                    descripcion = descripcionCompleta,
                    fecha = currentState.selectedDate.atStartOfDay().toTimestamp(),
                    tipo = currentState.selectedEventType,
                    creadorId = usuarioId,
                    centroId = centroId
                )

                val result = calendarioRepository.saveEvento(newEvent)
                
                when (result) {
                    is Result.Success -> {
                        val eventoGuardado = result.data
                        _uiState.update { 
                            it.copy(
                                eventos = it.eventos + eventoGuardado,
                                isLoading = false,
                                showEventDialog = false,
                                selectedEventType = null,
                                eventDescription = "",
                                eventTitle = "",
                                eventLocation = "",
                                eventTime = null,
                                isSuccess = true,
                                successMessage = "Evento guardado correctamente"
                            ) 
                        }
                        Timber.d("Evento guardado: ${eventoGuardado.id}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al guardar el evento"
                            ) 
                        }
                        Timber.e(result.exception, "Error al guardar el evento")
                    }
                    is Result.Loading -> {
                        // Ya estamos en estado de carga
                    }
                    else -> {
                        // Caso por defecto para manejar todos los posibles valores
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Estado desconocido al guardar el evento"
                            ) 
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al guardar el evento: ${e.message}"
                    ) 
                }
                Timber.e(e, "Error al guardar el evento")
            }
        }
    }

    /**
     * Elimina un evento
     */
    fun deleteEvento(eventoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = calendarioRepository.deleteEvento(eventoId)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                eventos = it.eventos.filter { it.id != eventoId },
                                isLoading = false,
                                isSuccess = true,
                                successMessage = "Evento eliminado correctamente"
                            ) 
                        }
                        Timber.d("Evento eliminado: $eventoId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al eliminar el evento"
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar el evento")
                    }
                    is Result.Loading -> {
                        // Ya estamos en estado de carga
                    }
                    else -> {
                        // Caso por defecto para manejar todos los posibles valores
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Estado desconocido al eliminar el evento"
                            ) 
                        }
                        Timber.w("Estado desconocido al eliminar el evento")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el evento"
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar el evento")
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
    fun clearSuccessMessage() {
        _uiState.update { it.copy(isSuccess = false, successMessage = null) }
    }

    /**
     * Limpia el estado de éxito (alias para clearSuccessMessage)
     */
    fun clearSuccess() {
        clearSuccessMessage()
    }

    /**
     * Alias para loadEventos - necesario para compatibilidad
     */
    fun cargarEventos() {
        loadEventos()
    }

    /**
     * Limpia la lista de eventos
     */
    fun clearEvents() {
        _uiState.update { it.copy(eventos = emptyList()) }
    }
    
    /**
     * Obtiene la información del usuario actual
     * Este método debe usarse con precaución ya que no es asíncrono
     * @return Información del usuario
     */
    suspend fun getUserInfo(): UserInfo {
        val currentUser = authRepository.getCurrentUser()
        // Asumimos que usaremos el primer perfil que se encuentre
        val perfil = currentUser?.perfiles?.firstOrNull()
        return UserInfo(
            id = currentUser?.dni ?: "",
            nombre = currentUser?.nombre ?: "",
            apellidos = currentUser?.apellidos ?: "",
            email = currentUser?.email ?: "",
            tipoUsuario = perfil?.tipo ?: TipoUsuario.FAMILIAR,
            centroId = perfil?.centroId ?: ""
        )
    }
    
    /**
     * Carga eventos específicos de un centro educativo
     * @param centroId ID del centro educativo
     */
    fun loadEventosByCentro(centroId: String) {
        viewModelScope.launch {
            try {
                val eventos = calendarioRepository.getEventosByCentro(centroId)
                _uiState.update { it.copy(eventos = it.eventos + eventos) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos del centro $centroId")
            }
        }
    }
    
    /**
     * Carga eventos específicos de un usuario
     * @param usuarioId ID del usuario
     */
    fun loadEventosByUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                val eventos = calendarioRepository.getEventosByUsuarioId(usuarioId)
                _uiState.update { it.copy(eventos = it.eventos + eventos) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos del usuario $usuarioId")
            }
        }
    }
    
    /**
     * Carga eventos específicos para los hijos de un familiar
     * Útil para mostrar eventos específicos de los alumnos del familiar
     */
    fun loadEventosByHijos() {
        viewModelScope.launch {
            try {
                // Obtener IDs de los hijos del familiar actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    Timber.d("Usuario no encontrado")
                    return@launch
                }
                
                // Comprobar si el usuario tiene un perfil de tipo FAMILIAR
                val perfilFamiliar = usuario.perfiles.find { it.tipo == TipoUsuario.FAMILIAR }
                if (perfilFamiliar == null) {
                    Timber.d("No se encontró un perfil de tipo FAMILIAR")
                    return@launch
                }
                
                val familiarId = usuario.dni
                if (familiarId.isEmpty()) {
                    Timber.e("ID del familiar está vacío")
                    return@launch
                }
                
                try {
                    val hijos = usuarioRepository.getHijosByFamiliarId(familiarId)
                    
                    // Para cada hijo, cargar sus eventos específicos
                    for (hijo in hijos) {
                        val eventosHijo = calendarioRepository.getEventosByAlumnoId(hijo.id)
                        _uiState.update { it.copy(eventos = it.eventos + eventosHijo) }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener hijos del familiar: ${e.message}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar eventos de los hijos: ${e.message}")
            }
        }
    }
} 