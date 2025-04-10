package com.tfg.umeegunero.feature.common.reuniones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Reunion
import com.tfg.umeegunero.data.model.TipoReunion
import com.tfg.umeegunero.data.model.Recordatorio
import com.tfg.umeegunero.data.model.TipoRecordatorio
import com.tfg.umeegunero.data.model.EstadoRecordatorio
import com.tfg.umeegunero.data.model.EventoCalendario
import com.tfg.umeegunero.data.model.EstadoReunion
import com.tfg.umeegunero.data.repository.ReunionRepository
import com.tfg.umeegunero.data.repository.RecordatoriosRepository
import com.tfg.umeegunero.data.repository.CalendarRepository
import com.tfg.umeegunero.data.repository.UserRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.time.LocalDateTime
import java.time.Instant
import javax.inject.Inject

/**
 * Estado UI para la pantalla de reuniones
 */
data class ReunionesUiState(
    val reuniones: List<Reunion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val showNuevaReunion: Boolean = false,
    val titulo: String = "",
    val descripcion: String = "",
    val fechaInicio: LocalDate = LocalDate.now(),
    val horaInicio: LocalTime = LocalTime.now(),
    val fechaFin: LocalDate = LocalDate.now(),
    val horaFin: LocalTime = LocalTime.now().plusHours(1),
    val tipo: TipoReunion = TipoReunion.OTRA,
    val ubicacion: String = "",
    val enlaceVirtual: String = "",
    val notas: String = "",
    val participantes: List<String> = emptyList(),
    val participantesIds: List<String> = emptyList(),
    val participantesNombres: List<String> = emptyList(),
    val recordatorios: List<Recordatorio> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val reunionToDelete: Reunion? = null
)

/**
 * ViewModel para la gestión de reuniones
 */
@HiltViewModel
class ReunionesViewModel @Inject constructor(
    private val reunionRepository: ReunionRepository,
    private val recordatoriosRepository: RecordatoriosRepository,
    private val calendarRepository: CalendarRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReunionesUiState())
    val uiState: StateFlow<ReunionesUiState> = _uiState.asStateFlow()

    init {
        cargarReuniones()
    }

    /**
     * Carga todas las reuniones
     */
    private fun cargarReuniones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = reunionRepository.getReuniones()) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            reuniones = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.exception?.message ?: "Error desconocido",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Muestra/oculta el formulario de nueva reunión
     */
    fun toggleNuevaReunion() {
        _uiState.update { 
            it.copy(
                showNuevaReunion = !it.showNuevaReunion,
                titulo = "",
                descripcion = "",
                fechaInicio = LocalDate.now(),
                horaInicio = LocalTime.now(),
                fechaFin = LocalDate.now(),
                horaFin = LocalTime.now().plusHours(1),
                tipo = TipoReunion.OTRA,
                ubicacion = "",
                enlaceVirtual = "",
                notas = "",
                participantes = emptyList(),
                participantesIds = emptyList(),
                participantesNombres = emptyList(),
                recordatorios = emptyList()
            )
        }
    }

    /**
     * Actualiza el título de la reunión
     */
    fun updateTitulo(titulo: String) {
        _uiState.update { it.copy(titulo = titulo) }
    }

    /**
     * Actualiza la descripción de la reunión
     */
    fun updateDescripcion(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }

    /**
     * Actualiza la fecha de inicio
     */
    fun updateFechaInicio(fecha: LocalDate) {
        _uiState.update { it.copy(fechaInicio = fecha) }
    }

    /**
     * Actualiza la hora de inicio
     */
    fun updateHoraInicio(hora: LocalTime) {
        _uiState.update { it.copy(horaInicio = hora) }
    }

    /**
     * Actualiza la fecha de fin
     */
    fun updateFechaFin(fecha: LocalDate) {
        _uiState.update { it.copy(fechaFin = fecha) }
    }

    /**
     * Actualiza la hora de fin
     */
    fun updateHoraFin(hora: LocalTime) {
        _uiState.update { it.copy(horaFin = hora) }
    }

    /**
     * Actualiza el tipo de reunión
     */
    fun updateTipo(tipo: TipoReunion) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    /**
     * Actualiza la ubicación
     */
    fun updateUbicacion(ubicacion: String) {
        _uiState.update { it.copy(ubicacion = ubicacion) }
    }

    /**
     * Actualiza el enlace virtual
     */
    fun updateEnlaceVirtual(enlace: String) {
        _uiState.update { it.copy(enlaceVirtual = enlace) }
    }

    /**
     * Actualiza las notas
     */
    fun updateNotas(notas: String) {
        _uiState.update { it.copy(notas = notas) }
    }

    /**
     * Añade un participante a la reunión
     */
    fun addParticipante(participante: String) {
        _uiState.update { 
            it.copy(participantes = it.participantes + participante)
        }
    }

    /**
     * Elimina un participante de la reunión
     */
    fun removeParticipante(participante: String) {
        _uiState.update { 
            it.copy(participantes = it.participantes - participante)
        }
    }

    /**
     * Añade un recordatorio
     */
    fun addRecordatorio(tipo: TipoRecordatorio, tiempoAntes: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                recordatorios = currentState.recordatorios + Recordatorio(
                    tipo = tipo,
                    tiempoAntes = tiempoAntes
                )
            )
        }
    }

    /**
     * Elimina un recordatorio
     */
    fun removeRecordatorio(recordatorio: Recordatorio) {
        _uiState.update { currentState ->
            currentState.copy(
                recordatorios = currentState.recordatorios - recordatorio
            )
        }
    }

    /**
     * Programa los recordatorios para una reunión
     */
    private suspend fun programarRecordatorios(reunion: Reunion) {
        reunion.recordatorios.forEach { recordatorio ->
            val fechaInicio = reunion.fechaInicio.toDate().toInstant()
            val fechaRecordatorio = fechaInicio.minusMillis(recordatorio.tiempoAntes * 60 * 1000)
            
            recordatoriosRepository.programarRecordatorio(
                recordatorio = recordatorio.copy(
                    estado = EstadoRecordatorio.PENDIENTE
                ),
                fechaRecordatorio = fechaRecordatorio
            )
        }
    }

    /**
     * Confirma la asistencia a una reunión
     */
    fun confirmarAsistencia(reunion: Reunion) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Obtener el ID del usuario actual
                val currentUserId = userRepository.getCurrentUserId() ?: ""
                if (currentUserId.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se ha identificado un usuario activo"
                        )
                    }
                    return@launch
                }
                
                reunionRepository.confirmarAsistencia(reunion.id, currentUserId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        success = "Asistencia confirmada correctamente",
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al confirmar asistencia: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Muestra el diálogo de confirmación para eliminar una reunión
     */
    fun showDeleteDialog(reunion: Reunion) {
        _uiState.update { 
            it.copy(
                showDeleteDialog = true,
                reunionToDelete = reunion
            )
        }
    }

    /**
     * Oculta el diálogo de confirmación
     */
    fun hideDeleteDialog() {
        _uiState.update { 
            it.copy(
                showDeleteDialog = false,
                reunionToDelete = null
            )
        }
    }

    /**
     * Elimina una reunión
     */
    fun deleteReunion() {
        val reunion = _uiState.value.reunionToDelete ?: return
        viewModelScope.launch {
            when (val result = reunionRepository.eliminarReunion(reunion.id)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            success = "Reunión eliminada correctamente",
                            showDeleteDialog = false,
                            reunionToDelete = null
                        )
                    }
                    cargarReuniones()
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(error = result.exception?.message ?: "Error desconocido")
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia los mensajes de éxito
     */
    fun clearSuccess() {
        _uiState.update { it.copy(success = null) }
    }

    /**
     * Añade una reunión al calendario del dispositivo
     */
    fun addReunionToCalendar(reunion: Reunion) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val eventoCalendario = EventoCalendario(
                    titulo = reunion.titulo,
                    descripcion = reunion.descripcion,
                    fechaInicio = reunion.fechaInicio.toDate(),
                    fechaFin = reunion.fechaFin.toDate(),
                    ubicacion = reunion.ubicacion,
                    recordatorio = reunion.recordatorios.firstOrNull()?.tiempoAntes ?: 30
                )
                
                calendarRepository.addEventoCalendario(eventoCalendario)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        success = "Reunión añadida al calendario correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al añadir la reunión al calendario: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Muestra un mensaje de error
     */
    fun showError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    /**
     * Crea una nueva reunión
     */
    fun crearReunion() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val fechaInicio = _uiState.value.fechaInicio.atTime(_uiState.value.horaInicio)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                
                val fechaFin = _uiState.value.fechaFin.atTime(_uiState.value.horaFin)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                
                val reunion = Reunion(
                    titulo = _uiState.value.titulo,
                    descripcion = _uiState.value.descripcion,
                    fechaInicio = Timestamp(Date.from(fechaInicio)),
                    fechaFin = Timestamp(Date.from(fechaFin)),
                    tipo = _uiState.value.tipo,
                    ubicacion = _uiState.value.ubicacion,
                    enlaceVirtual = _uiState.value.enlaceVirtual,
                    notas = _uiState.value.notas,
                    participantesIds = _uiState.value.participantesIds,
                    participantesNombres = _uiState.value.participantesNombres,
                    recordatorios = _uiState.value.recordatorios,
                    estado = EstadoReunion.PROGRAMADA
                )
                
                when (val result = reunionRepository.crearReunion(reunion)) {
                    is Result.Success -> {
                        val reunionId = result.data
                        val reunionCreada = reunionRepository.getReunion(reunionId)
                        if (reunionCreada is Result.Success) {
                            programarRecordatorios(reunionCreada.data)
                        }
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                showNuevaReunion = false,
                                success = "Reunión creada correctamente"
                            )
                        }
                        cargarReuniones()
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al crear la reunión: ${result.exception?.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al crear la reunión: ${e.message}"
                    )
                }
            }
        }
    }
} 