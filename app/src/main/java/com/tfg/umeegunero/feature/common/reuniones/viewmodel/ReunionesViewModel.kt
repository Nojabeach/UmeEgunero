package com.tfg.umeegunero.feature.common.reuniones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Reunion
import com.tfg.umeegunero.data.model.TipoReunion
import com.tfg.umeegunero.data.model.Recordatorio
import com.tfg.umeegunero.data.repository.ReunionRepository
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
import java.util.Date
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
    val horaFin: LocalTime = LocalTime.now(),
    val tipo: TipoReunion = TipoReunion.GENERAL,
    val ubicacion: String = "",
    val enlaceVirtual: String = "",
    val notas: String = "",
    val participantes: List<String> = emptyList(),
    val recordatorios: List<Recordatorio> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val reunionToDelete: Reunion? = null
)

/**
 * ViewModel para la gestión de reuniones
 */
@HiltViewModel
class ReunionesViewModel @Inject constructor(
    private val reunionRepository: ReunionRepository
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
                horaFin = LocalTime.now(),
                tipo = TipoReunion.GENERAL,
                ubicacion = "",
                enlaceVirtual = "",
                notas = "",
                participantes = emptyList(),
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
    fun addRecordatorio(recordatorio: Recordatorio) {
        _uiState.update { 
            it.copy(recordatorios = it.recordatorios + recordatorio)
        }
    }

    /**
     * Elimina un recordatorio
     */
    fun removeRecordatorio(recordatorio: Recordatorio) {
        _uiState.update { 
            it.copy(recordatorios = it.recordatorios - recordatorio)
        }
    }

    /**
     * Crea una nueva reunión
     */
    fun crearReunion() {
        viewModelScope.launch {
            val fechaInicioDate = Date.from(
                _uiState.value.fechaInicio
                    .atTime(_uiState.value.horaInicio)
                    .toInstant(ZoneOffset.UTC)
            )
            val fechaFinDate = Date.from(
                _uiState.value.fechaFin
                    .atTime(_uiState.value.horaFin)
                    .toInstant(ZoneOffset.UTC)
            )

            val reunion = Reunion(
                titulo = _uiState.value.titulo,
                descripcion = _uiState.value.descripcion,
                fechaInicio = Timestamp(fechaInicioDate),
                fechaFin = Timestamp(fechaFinDate),
                tipo = _uiState.value.tipo,
                ubicacion = _uiState.value.ubicacion,
                enlaceVirtual = _uiState.value.enlaceVirtual,
                notas = _uiState.value.notas,
                participantesIds = _uiState.value.participantes,
                recordatorios = _uiState.value.recordatorios
            )

            when (val result = reunionRepository.crearReunion(reunion)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            success = "Reunión creada correctamente",
                            showNuevaReunion = false
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
     * Confirma la asistencia a una reunión
     */
    fun confirmarAsistencia(reunionId: String, usuarioId: String) {
        viewModelScope.launch {
            when (val result = reunionRepository.confirmarAsistencia(reunionId, usuarioId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(success = "Asistencia confirmada correctamente")
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
} 