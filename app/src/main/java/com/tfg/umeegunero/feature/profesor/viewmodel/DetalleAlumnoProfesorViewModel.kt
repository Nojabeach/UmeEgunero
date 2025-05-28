package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import timber.log.Timber

/**
 * ViewModel para la pantalla de detalle del alumno del profesor.
 * Gestiona el estado de UI y las operaciones relacionadas con el alumno.
 */
@HiltViewModel
class DetalleAlumnoProfesorViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val calendarioRepository: CalendarioRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    // Estado de UI expuesto como StateFlow inmutable
    protected val _uiState = MutableStateFlow(DetalleAlumnoProfesorUiState())
    val uiState: StateFlow<DetalleAlumnoProfesorUiState> = _uiState.asStateFlow()

    /**
     * Carga la información de un alumno por su ID.
     * Se llama desde la pantalla cuando se recibe el alumnoId.
     */
    fun loadAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Asumiendo que getAlumnoPorId existe en UsuarioRepository
                when (val result = usuarioRepository.getAlumnoPorId(alumnoId)) {
                    is Result.Success<Alumno> -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                alumno = result.data
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar alumno: ${result.exception?.message ?: "Error desconocido"}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Ya estamos en isLoading = true
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar el alumno"
                    )
                }
            }
        }
    }

    /**
     * Establece directamente el estado para vistas previas o testing.
     */
    fun setStateForPreview(state: DetalleAlumnoProfesorUiState) {
        _uiState.value = state
    }

    /**
     * Limpia el mensaje de error del estado de la UI.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Muestra el selector de fecha y hora
     * @param initialDate Fecha inicial seleccionada
     * @param onDateSelected Callback con la fecha seleccionada
     */
    fun showDatePicker(initialDate: LocalDateTime, onDateSelected: (LocalDateTime) -> Unit) {
        // En una implementación real, esto mostraría un DatePicker nativo de Android
        // Por ahora, simplemente devolveremos la fecha inicial
        onDateSelected(initialDate)
    }
    
    /**
     * Programa una reunión con un familiar
     * @param fecha Fecha y hora de la reunión
     * @param duracionMinutos Duración en minutos
     * @param tipo Tipo de reunión
     * @param notas Notas adicionales
     * @param alumnoId ID del alumno
     * @param alumnoNombre Nombre del alumno
     */
    fun programarReunion(
        fecha: LocalDateTime,
        duracionMinutos: Int,
        tipo: String,
        notas: String,
        alumnoId: String,
        alumnoNombre: String
    ) {
        viewModelScope.launch {
            try {
                // Obtener el ID y nombre del profesor
                val profesor = authRepository.getCurrentUser()
                if (profesor == null) {
                    Timber.e("No se pudo obtener el usuario actual para programar la reunión")
                    return@launch
                }
                
                // Obtener los familiares del alumno
                val alumno = _uiState.value.alumno ?: return@launch
                val familiaresIds = alumno.familiarIds
                
                if (familiaresIds.isEmpty()) {
                    Timber.e("El alumno no tiene familiares asociados")
                    return@launch
                }
                
                // Crear el evento con los campos mínimos necesarios
                val evento = Evento(
                    id = "", // Se generará automáticamente
                    titulo = "Reunión: $tipo - $alumnoNombre",
                    descripcion = notas,
                    fecha = Timestamp.now(), // Usar timestamp actual como placeholder
                    tipo = "REUNION",
                    ubicacion = "Centro educativo"
                )
                
                // Guardar el evento en el calendario
                val resultado = calendarioRepository.createEvento(evento)
                
                if (resultado is Result.Success<*>) {
                    Timber.d("Reunión programada correctamente con ID: ${resultado.data}")
                    
                    // Notificar a los familiares
                    for (familiarId in familiaresIds) {
                        try {
                            val familiar = usuarioRepository.getUsuarioById(familiarId)
                            if (familiar != null) {
                                notificacionRepository.enviarNotificacion(
                                    familiarId,
                                    "Nueva reunión programada",
                                    "El profesor ${profesor.nombre} ${profesor.apellidos} " +
                                            "ha programado una reunión de $tipo para $alumnoNombre " +
                                            "el día ${fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} " +
                                            "a las ${fecha.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                                )
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al notificar al familiar $familiarId sobre la reunión")
                        }
                    }
                } else if (resultado is Result.Error) {
                    Timber.e(resultado.exception, "Error al programar la reunión")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al programar la reunión")
            }
        }
    }
}

/**
 * Estado de UI para la pantalla de detalle del alumno del profesor.
 *
 * @property isLoading Indica si los datos del alumno se están cargando.
 * @property alumno Los datos del alumno cargados, o null si no se han cargado o hubo error.
 * @property error Mensaje de error si la carga falló, o null si no hay error.
 */
data class DetalleAlumnoProfesorUiState(
    val isLoading: Boolean = false,
    val alumno: Alumno? = null,
    val error: String? = null
) 