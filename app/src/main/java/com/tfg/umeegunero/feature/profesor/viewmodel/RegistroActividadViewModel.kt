package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Comidas
import com.tfg.umeegunero.data.model.NecesidadesFisiologicas
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.data.model.Observacion
import com.tfg.umeegunero.data.model.Plato
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Siesta
import com.tfg.umeegunero.data.model.TipoObservacion
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * Estado UI para la pantalla de registro de actividad
 */
data class RegistroActividadUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val alumno: Alumno? = null,
    val comidas: Comidas = Comidas(
        primerPlato = Plato("", NivelConsumo.BIEN),
        segundoPlato = Plato("", NivelConsumo.BIEN),
        postre = Plato("", NivelConsumo.BIEN)
    ),
    val siesta: Siesta? = null,
    val necesidadesFisiologicas: NecesidadesFisiologicas = NecesidadesFisiologicas(false, false),
    val observaciones: List<Observacion> = emptyList(),
    val registroGuardado: Boolean = false,
    val registroId: String = "",
    val nuevoObservacionMensaje: String = "",
    val nuevoObservacionTipo: TipoObservacion = TipoObservacion.OTRO
)

/**
 * ViewModel para la pantalla de registro de actividad
 *
 * Gestiona la lógica de negocio y el estado de la UI para la pantalla de registro de actividad,
 * incluyendo la carga de datos del alumno y la creación/edición del registro.
 */
@HiltViewModel
class RegistroActividadViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroActividadUiState())
    val uiState: StateFlow<RegistroActividadUiState> = _uiState.asStateFlow()

    // Obtener el ID del alumno de los argumentos de navegación
    private val alumnoId: String = savedStateHandle.get<String>("alumnoId") ?: ""
    private val registroId: String = savedStateHandle.get<String>("registroId") ?: ""

    init {
        if (alumnoId.isNotBlank()) {
            cargarDatosAlumno(alumnoId)
        }

        if (registroId.isNotBlank()) {
            _uiState.update { it.copy(registroId = registroId) }
            // Aquí deberíamos cargar el registro existente, pero parece que no existe
            // el método getRegistroById en el repositorio
        }
    }

    /**
     * Carga los datos del alumno
     */
    fun cargarDatosAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Este método aún no existe en el repositorio proporcionado
                // Habría que implementarlo o usar otro existente
                // Por ahora, simulamos un alumno para que la UI funcione
                val alumno = Alumno(
                    dni = alumnoId,
                    nombre = "Alumno",
                    apellidos = "Ejemplo"
                )

                _uiState.update {
                    it.copy(
                        alumno = alumno,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumno")
            }
        }
    }

    /**
     * Actualiza los datos de comidas
     */
    fun updateComidas(comidas: Comidas) {
        _uiState.update { it.copy(comidas = comidas) }
    }

    /**
     * Actualiza el primer plato
     */
    fun updatePrimerPlato(descripcion: String, consumo: NivelConsumo) {
        val currentComidas = _uiState.value.comidas
        val updatedComidas = currentComidas.copy(
            primerPlato = currentComidas.primerPlato.copy(
                descripcion = descripcion,
                consumo = consumo
            )
        )
        _uiState.update { it.copy(comidas = updatedComidas) }
    }

    /**
     * Actualiza el segundo plato
     */
    fun updateSegundoPlato(descripcion: String, consumo: NivelConsumo) {
        val currentComidas = _uiState.value.comidas
        val updatedComidas = currentComidas.copy(
            segundoPlato = currentComidas.segundoPlato.copy(
                descripcion = descripcion,
                consumo = consumo
            )
        )
        _uiState.update { it.copy(comidas = updatedComidas) }
    }

    /**
     * Actualiza el postre
     */
    fun updatePostre(descripcion: String, consumo: NivelConsumo) {
        val currentComidas = _uiState.value.comidas
        val updatedComidas = currentComidas.copy(
            postre = currentComidas.postre.copy(
                descripcion = descripcion,
                consumo = consumo
            )
        )
        _uiState.update { it.copy(comidas = updatedComidas) }
    }

    /**
     * Actualiza los datos de siesta
     */
    fun updateSiesta(siesta: Siesta?) {
        _uiState.update { it.copy(siesta = siesta) }
    }

    /**
     * Actualiza la hora de inicio de la siesta
     */
    fun updateSiestaInicio(hora: Int, minuto: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hora)
        calendar.set(Calendar.MINUTE, minuto)

        val timestamp = Timestamp(calendar.time)
        val siesta = _uiState.value.siesta ?: Siesta()

        _uiState.update {
            it.copy(
                siesta = siesta.copy(
                    inicio = timestamp,
                    duracion = calcularDuracionSiesta(timestamp, siesta.fin)
                )
            )
        }
    }

    /**
     * Actualiza la hora de fin de la siesta
     */
    fun updateSiestaFin(hora: Int, minuto: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hora)
        calendar.set(Calendar.MINUTE, minuto)

        val timestamp = Timestamp(calendar.time)
        val siesta = _uiState.value.siesta ?: Siesta()

        _uiState.update {
            it.copy(
                siesta = siesta.copy(
                    fin = timestamp,
                    duracion = calcularDuracionSiesta(siesta.inicio, timestamp)
                )
            )
        }
    }

    /**
     * Calcula la duración de la siesta en minutos
     */
    private fun calcularDuracionSiesta(inicio: Timestamp?, fin: Timestamp?): Int {
        if (inicio == null || fin == null) return 0

        val diferenciaMillis = fin.toDate().time - inicio.toDate().time
        return (diferenciaMillis / (1000 * 60)).toInt() // Convertir a minutos
    }

    /**
     * Actualiza los datos de necesidades fisiológicas
     */
    fun updateNecesidadesFisiologicas(pipi: Boolean, caca: Boolean) {
        _uiState.update {
            it.copy(
                necesidadesFisiologicas = NecesidadesFisiologicas(
                    caca = caca,
                    pipi = pipi
                )
            )
        }
    }

    /**
     * Actualiza el mensaje para una nueva observación
     */
    fun updateNuevoObservacionMensaje(mensaje: String) {
        _uiState.update { it.copy(nuevoObservacionMensaje = mensaje) }
    }

    /**
     * Actualiza el tipo para una nueva observación
     */
    fun updateNuevoObservacionTipo(tipo: TipoObservacion) {
        _uiState.update { it.copy(nuevoObservacionTipo = tipo) }
    }

    /**
     * Añade una observación a la lista
     */
    fun addObservacion() {
        val mensaje = _uiState.value.nuevoObservacionMensaje
        if (mensaje.isBlank()) return

        val observacion = Observacion(
            tipo = _uiState.value.nuevoObservacionTipo,
            mensaje = mensaje,
            hora = Timestamp.now()
        )

        val observacionesActualizadas = _uiState.value.observaciones.toMutableList()
        observacionesActualizadas.add(observacion)
        _uiState.update {
            it.copy(
                observaciones = observacionesActualizadas,
                nuevoObservacionMensaje = "" // Limpia el campo después de añadir
            )
        }
    }

    /**
     * Elimina una observación de la lista
     */
    fun removeObservacion(index: Int) {
        val observacionesActualizadas = _uiState.value.observaciones.toMutableList()
        if (index in observacionesActualizadas.indices) {
            observacionesActualizadas.removeAt(index)
            _uiState.update { it.copy(observaciones = observacionesActualizadas) }
        }
    }

    /**
     * Guarda el registro de actividad
     */
    fun guardarRegistro() {
        val alumno = _uiState.value.alumno ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Supongamos que el usuario actual es el profesor
                val profesorId = "profesor_id" // En una implementación real, obtendríamos el ID del usuario actual

                val registro = RegistroActividad(
                    id = _uiState.value.registroId, // Si es "" se creará uno nuevo
                    alumnoId = alumno.dni,
                    fecha = Timestamp.now(),
                    profesorId = profesorId,
                    comidas = _uiState.value.comidas,
                    siesta = _uiState.value.siesta,
                    necesidadesFisiologicas = _uiState.value.necesidadesFisiologicas,
                    observaciones = _uiState.value.observaciones
                )

                // En una implementación real, aquí llamaríamos al repositorio para guardar el registro
                // Por ahora simulamos que se ha guardado con éxito

                _uiState.update {
                    it.copy(
                        registroGuardado = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al guardar registro")
            }
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}