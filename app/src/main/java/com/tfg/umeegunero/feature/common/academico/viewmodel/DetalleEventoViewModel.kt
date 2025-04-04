package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.toLocalDate
import com.tfg.umeegunero.util.toLocalTime
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
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Estado UI para la pantalla de detalle de evento
 */
data class DetalleEventoUiState(
    val evento: Evento? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEditing: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    
    // Campos para edición
    val titulo: String = "",
    val descripcion: String = "",
    val tipoEvento: TipoEvento = TipoEvento.OTRO,
    val fechaTexto: String = "",
    val horaTexto: String = "",
    val ubicacion: String = "",
    val recordatorio: Boolean = false,
    val tiempoRecordatorioMinutos: Int = 30,
    val publico: Boolean = true
)

/**
 * ViewModel para la pantalla de detalle de evento
 */
@HiltViewModel
class DetalleEventoViewModel @Inject constructor(
    private val calendarioRepository: CalendarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetalleEventoUiState())
    val uiState: StateFlow<DetalleEventoUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    /**
     * Carga un evento por su ID
     */
    fun loadEvento(eventoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Este método es simulado, en una implementación real
                // buscaría el evento en Firestore por su ID
                val eventosList = calendarioRepository.getEventosByMonth(
                    LocalDate.now().year,
                    LocalDate.now().monthValue
                )
                
                val evento = eventosList.find { it.id == eventoId }
                
                if (evento != null) {
                    _uiState.update { 
                        it.copy(
                            evento = evento,
                            isLoading = false,
                            titulo = evento.titulo,
                            descripcion = evento.descripcion,
                            tipoEvento = evento.tipo,
                            fechaTexto = evento.fecha.toLocalDate().format(dateFormatter),
                            horaTexto = evento.fecha.toLocalTime().format(timeFormatter),
                            ubicacion = evento.ubicacion,
                            recordatorio = evento.recordatorio,
                            tiempoRecordatorioMinutos = evento.tiempoRecordatorioMinutos,
                            publico = evento.publico
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se encontró el evento"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar el evento"
                    ) 
                }
                Timber.e(e, "Error al cargar el evento")
            }
        }
    }
    
    /**
     * Inicia el modo de edición
     */
    fun startEditing() {
        _uiState.update { it.copy(isEditing = true) }
    }
    
    /**
     * Cancela el modo de edición
     */
    fun cancelEditing() {
        // Restaurar valores originales del evento
        _uiState.value.evento?.let { evento ->
            _uiState.update { 
                it.copy(
                    isEditing = false,
                    titulo = evento.titulo,
                    descripcion = evento.descripcion,
                    tipoEvento = evento.tipo,
                    fechaTexto = evento.fecha.toLocalDate().format(dateFormatter),
                    horaTexto = evento.fecha.toLocalTime().format(timeFormatter),
                    ubicacion = evento.ubicacion,
                    recordatorio = evento.recordatorio,
                    tiempoRecordatorioMinutos = evento.tiempoRecordatorioMinutos,
                    publico = evento.publico
                ) 
            }
        }
    }
    
    /**
     * Actualiza el título del evento
     */
    fun updateTitulo(titulo: String) {
        _uiState.update { it.copy(titulo = titulo) }
    }
    
    /**
     * Actualiza la descripción del evento
     */
    fun updateDescripcion(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }
    
    /**
     * Actualiza el tipo de evento
     */
    fun updateTipo(tipo: TipoEvento) {
        _uiState.update { it.copy(tipoEvento = tipo) }
    }
    
    /**
     * Actualiza la fecha del evento
     */
    fun updateFecha(fecha: String) {
        _uiState.update { it.copy(fechaTexto = fecha) }
    }
    
    /**
     * Actualiza la hora del evento
     */
    fun updateHora(hora: String) {
        _uiState.update { it.copy(horaTexto = hora) }
    }
    
    /**
     * Actualiza la ubicación del evento
     */
    fun updateUbicacion(ubicacion: String) {
        _uiState.update { it.copy(ubicacion = ubicacion) }
    }
    
    /**
     * Actualiza si el evento tiene recordatorio
     */
    fun updateRecordatorio(recordatorio: Boolean) {
        _uiState.update { it.copy(recordatorio = recordatorio) }
    }
    
    /**
     * Actualiza el tiempo de recordatorio en minutos
     */
    fun updateTiempoRecordatorio(minutos: Int) {
        _uiState.update { it.copy(tiempoRecordatorioMinutos = minutos) }
    }
    
    /**
     * Actualiza si el evento es público
     */
    fun updatePublico(publico: Boolean) {
        _uiState.update { it.copy(publico = publico) }
    }
    
    /**
     * Guarda los cambios del evento
     */
    fun saveEvento() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState.titulo.isBlank()) {
                _uiState.update { it.copy(error = "El título no puede estar vacío") }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Parsear fecha y hora
                val fecha = try {
                    LocalDate.parse(currentState.fechaTexto, dateFormatter)
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Formato de fecha inválido. Use dd/mm/yyyy"
                        ) 
                    }
                    return@launch
                }
                
                val hora = try {
                    LocalTime.parse(currentState.horaTexto, timeFormatter)
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Formato de hora inválido. Use hh:mm"
                        ) 
                    }
                    return@launch
                }
                
                val fechaHora = LocalDateTime.of(fecha, hora)
                
                // Crear evento actualizado
                val eventoActualizado = currentState.evento?.copy(
                    titulo = currentState.titulo,
                    descripcion = currentState.descripcion,
                    fecha = fechaHora.toTimestamp(),
                    tipo = currentState.tipoEvento,
                    ubicacion = currentState.ubicacion,
                    recordatorio = currentState.recordatorio,
                    tiempoRecordatorioMinutos = currentState.tiempoRecordatorioMinutos,
                    publico = currentState.publico
                )
                
                if (eventoActualizado != null) {
                    val result = calendarioRepository.updateEvento(eventoActualizado)
                    
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    evento = eventoActualizado,
                                    isLoading = false,
                                    isEditing = false,
                                    isSuccess = true
                                ) 
                            }
                            Timber.d("Evento actualizado: ${eventoActualizado.id}")
                        }
                        is Result.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = result.exception?.message ?: "Error al actualizar el evento"
                                ) 
                            }
                            Timber.e(result.exception, "Error al actualizar el evento")
                        }
                        is Result.Loading -> {
                            // Ya estamos en estado de carga
                        }
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
     * Muestra el diálogo de confirmación para eliminar evento
     */
    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }
    
    /**
     * Oculta el diálogo de confirmación para eliminar evento
     */
    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }
    
    /**
     * Elimina el evento
     */
    fun deleteEvento() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val evento = currentState.evento ?: return@launch
            
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    error = null,
                    showDeleteConfirmation = false
                ) 
            }
            
            try {
                val result = calendarioRepository.deleteEvento(evento.id)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isSuccess = true
                            ) 
                        }
                        Timber.d("Evento eliminado: ${evento.id}")
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
     * Limpia el estado de éxito
     */
    fun clearSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
} 