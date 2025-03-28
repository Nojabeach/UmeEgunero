package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.repository.CalendarioRepository
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
import kotlin.Result

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
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue
)

/**
 * ViewModel para la pantalla de calendario
 */
@HiltViewModel
class CalendarioViewModel @Inject constructor(
    private val calendarioRepository: CalendarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarioUiState())
    val uiState: StateFlow<CalendarioUiState> = _uiState.asStateFlow()

    init {
        loadEventos()
    }

    /**
     * Carga los eventos del mes actual
     */
    fun loadEventos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val eventos = calendarioRepository.getEventosByMonth(
                    _uiState.value.currentMonth.year,
                    _uiState.value.currentMonth.monthValue
                )
                
                _uiState.update { 
                    it.copy(
                        eventos = eventos,
                        isLoading = false
                    ) 
                }
                Timber.d("Eventos cargados: ${eventos.size}")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los eventos"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar los eventos")
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
        loadEventos()
    }

    /**
     * Muestra el diálogo para añadir evento
     */
    fun showEventDialog() {
        _uiState.update { it.copy(showEventDialog = true) }
    }

    /**
     * Oculta el diálogo para añadir evento
     */
    fun hideEventDialog() {
        _uiState.update { 
            it.copy(
                showEventDialog = false,
                selectedEventType = null,
                eventDescription = ""
            ) 
        }
    }

    /**
     * Actualiza el tipo de evento seleccionado
     */
    fun updateSelectedEventType(tipo: TipoEvento?) {
        _uiState.update { it.copy(selectedEventType = tipo) }
    }

    /**
     * Actualiza la descripción del evento
     */
    fun updateEventDescription(description: String) {
        _uiState.update { it.copy(eventDescription = description) }
    }

    /**
     * Guarda un nuevo evento
     */
    fun saveEvento() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.selectedEventType == null || currentState.eventDescription.isBlank()) {
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val newEvent = Evento(
                    id = "", // Será generado por Firestore
                    titulo = currentState.eventDescription.lines().firstOrNull() ?: "Evento",
                    descripcion = currentState.eventDescription,
                    fecha = currentState.selectedDate.atStartOfDay(),
                    tipo = currentState.selectedEventType,
                    creadorId = calendarioRepository.obtenerUsuarioId(),
                    centroId = calendarioRepository.obtenerCentroId()
                )

                val result = calendarioRepository.saveEvento(newEvent)
                
                when {
                    result.isSuccess -> {
                        val eventoGuardado = result.getOrNull()
                        if (eventoGuardado != null) {
                            _uiState.update { 
                                it.copy(
                                    eventos = it.eventos + eventoGuardado,
                                    isLoading = false,
                                    showEventDialog = false,
                                    selectedEventType = null,
                                    eventDescription = "",
                                    isSuccess = true,
                                    successMessage = "Evento guardado correctamente"
                                ) 
                            }
                            Timber.d("Evento guardado: ${eventoGuardado.id}")
                        }
                    }
                    result.isFailure -> {
                        val exception = result.exceptionOrNull()
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception?.message ?: "Error al guardar el evento"
                            ) 
                        }
                        Timber.e(exception, "Error al guardar el evento")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al guardar el evento"
                    ) 
                }
                Timber.e(e, "Error inesperado al guardar el evento")
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
                
                when {
                    result.isSuccess -> {
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
                    result.isFailure -> {
                        val exception = result.exceptionOrNull()
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception?.message ?: "Error al eliminar el evento"
                            ) 
                        }
                        Timber.e(exception, "Error al eliminar el evento")
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
    fun clearSuccess() {
        _uiState.update { it.copy(isSuccess = false, successMessage = null) }
    }
} 