package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Comidas
import com.tfg.umeegunero.data.model.CacaControl
import com.tfg.umeegunero.data.model.Comida
import com.tfg.umeegunero.data.model.NecesidadesFisiologicas
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.data.model.Observacion
import com.tfg.umeegunero.data.model.Plato
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Siesta
import com.tfg.umeegunero.data.model.TipoObservacion
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    val necesidadesFisiologicas: CacaControl = CacaControl(
        caca = false, 
        pipi = false, 
        observaciones = ""
    ),
    val observaciones: List<Observacion> = emptyList(),
    val registroGuardado: Boolean = false,
    val registroId: String = "",
    val nuevoObservacionMensaje: String = "",
    val nuevoObservacionTipo: TipoObservacion = TipoObservacion.OTRO
)

// ViewModel para la pantalla donde los profes registran actividades de los niños
@HiltViewModel
class RegistroActividadViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroActividadUiState())
    val uiState: StateFlow<RegistroActividadUiState> = _uiState.asStateFlow()

    // Sacamos los IDs de los args de navegación
    private val alumnoId: String = savedStateHandle.get<String>("alumnoId") ?: ""
    private val registroId: String = savedStateHandle.get<String>("registroId") ?: ""

    init {
        if (alumnoId.isNotBlank()) {
            cargarDatosAlumno(alumnoId)
        }

        if (registroId.isNotBlank()) {
            _uiState.update { it.copy(registroId = registroId) }
            // TODO: Aquí debería cargar el registro existente, pero no tengo el método
            // getRegistroById implementado todavía
        }
    }

    // Carga los datos del alumno
    fun cargarDatosAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Esto no está implementado en el repo todavía
                // Tengo que añadirlo cuando termine la parte de UI
                // Por ahora meto un alumno de ejemplo para que se vea algo
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

    fun updateComidas(comidas: Comidas) {
        _uiState.update { it.copy(comidas = comidas) }
    }

    /**
     * Actualiza información del primer plato
     */
    fun updatePrimerPlato(descripcion: String, nivelConsumo: NivelConsumo) {
        val plato = _uiState.value.comidas.primerPlato?.copy(
            descripcion = descripcion,
            nivelConsumo = nivelConsumo,
            consumo = nivelConsumo
        ) ?: Plato(descripcion, nivelConsumo, nivelConsumo)

        _uiState.update {
            it.copy(comidas = it.comidas.copy(primerPlato = plato))
        }
    }

    /**
     * Actualiza información del segundo plato
     */
    fun updateSegundoPlato(descripcion: String, nivelConsumo: NivelConsumo) {
        val plato = _uiState.value.comidas.segundoPlato?.copy(
            descripcion = descripcion,
            nivelConsumo = nivelConsumo,
            consumo = nivelConsumo
        ) ?: Plato(descripcion, nivelConsumo, nivelConsumo)

        _uiState.update {
            it.copy(comidas = it.comidas.copy(segundoPlato = plato))
        }
    }

    /**
     * Actualiza información del postre
     */
    fun updatePostre(descripcion: String, nivelConsumo: NivelConsumo) {
        val plato = _uiState.value.comidas.postre?.copy(
            descripcion = descripcion,
            nivelConsumo = nivelConsumo,
            consumo = nivelConsumo
        ) ?: Plato(descripcion, nivelConsumo, nivelConsumo)

        _uiState.update {
            it.copy(comidas = it.comidas.copy(postre = plato))
        }
    }

    fun updateSiesta(siesta: Siesta?) {
        _uiState.update { it.copy(siesta = siesta) }
    }

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

    // Calcula cuántos minutos ha dormido el niño
    private fun calcularDuracionSiesta(inicio: Timestamp?, fin: Timestamp?): Int {
        if (inicio == null || fin == null) return 0

        val diferenciaMillis = fin.toDate().time - inicio.toDate().time
        return (diferenciaMillis / (1000 * 60)).toInt() // Convertir a minutos
    }

    /**
     * Actualiza información sobre necesidades fisiológicas
     */
    fun updateNecesidadesFisiologicas(caca: Boolean, pipi: Boolean, observaciones: String = "") {
        _uiState.update {
            it.copy(necesidadesFisiologicas = it.necesidadesFisiologicas.copy(
                caca = caca,
                pipi = pipi,
                observaciones = observaciones
            ))
        }
    }

    fun updateNuevoObservacionMensaje(mensaje: String) {
        _uiState.update { it.copy(nuevoObservacionMensaje = mensaje) }
    }

    fun updateNuevoObservacionTipo(tipo: TipoObservacion) {
        _uiState.update { it.copy(nuevoObservacionTipo = tipo) }
    }

    // Añade una observación a la lista
    fun addObservacion() {
        val mensaje = _uiState.value.nuevoObservacionMensaje
        if (mensaje.isBlank()) return

        val observacion = Observacion(
            tipo = _uiState.value.nuevoObservacionTipo,
            mensaje = mensaje,
            timestamp = Timestamp.now()
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

    // Quita una observación de la lista
    fun removeObservacion(index: Int) {
        val observacionesActualizadas = _uiState.value.observaciones.toMutableList()
        if (index in observacionesActualizadas.indices) {
            observacionesActualizadas.removeAt(index)
            _uiState.update { it.copy(observaciones = observacionesActualizadas) }
        }
    }

    // Guarda el registro en Firebase
    fun guardarRegistro() {
        val alumno = _uiState.value.alumno ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Obtener el ID del profesor actual
                val profesorId = usuarioRepository.getUsuarioActualId()
                
                if (profesorId.isNotBlank()) {
                    guardarRegistroEnFirestore(profesorId)
                } else {
                    throw Exception("No se pudo obtener el ID del profesor")
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

    private fun guardarRegistroEnFirestore(profesorId: String) {
        viewModelScope.launch {
            try {
                val alumnoId = _uiState.value.alumno?.dni ?: throw Exception("No se ha seleccionado ningún alumno")
                val alumnoNombre = "${_uiState.value.alumno?.nombre} ${_uiState.value.alumno?.apellidos}"
                
                // Convertir los datos del UI a objetos del modelo
                val comida = Comida(
                    consumoPrimero = _uiState.value.comidas.primerPlato?.descripcion,
                    descripcionPrimero = "Nivel: ${_uiState.value.comidas.primerPlato?.nivelConsumo?.name ?: "No especificado"}",
                    consumoSegundo = _uiState.value.comidas.segundoPlato?.descripcion,
                    descripcionSegundo = "Nivel: ${_uiState.value.comidas.segundoPlato?.nivelConsumo?.name ?: "No especificado"}",
                    consumoPostre = _uiState.value.comidas.postre?.descripcion,
                    descripcionPostre = "Nivel: ${_uiState.value.comidas.postre?.nivelConsumo?.name ?: "No especificado"}"
                )
                
                val cacaControl = _uiState.value.necesidadesFisiologicas.copy()
                
                // Convertir las observaciones a texto para ser compatible con el nuevo modelo
                val observacionesTexto = if (_uiState.value.observaciones.isNotEmpty()) {
                    _uiState.value.observaciones.joinToString("\n") { 
                        "[${it.tipo}] ${it.mensaje} (${formatTime(it.timestamp)})" 
                    }
                } else {
                    null
                }
                
                val registro = RegistroActividad(
                    id = _uiState.value.registroId,  // Si es "" se creará uno nuevo
                    alumnoId = alumnoId,
                    alumnoNombre = alumnoNombre,
                    fecha = Timestamp.now(),
                    profesorId = profesorId,
                    comida = comida,
                    siesta = _uiState.value.siesta,
                    cacaControl = cacaControl,
                    observaciones = observacionesTexto,
                    comidas = _uiState.value.comidas,
                    necesidadesFisiologicas = cacaControl
                )

                // TODO: Simular guardado exitoso mientras se implementa el método en el repositorio
                // En una implementación real, aquí llamaríamos al repositorio:
                // val result = usuarioRepository.guardarRegistroActividad(registro)
                
                // Simulación:
                _uiState.update {
                    it.copy(
                        registroGuardado = true,
                        registroId = "registro_simulado_id",
                        isLoading = false
                    )
                }
                
                /* TODO:Código para cuando el método esté implementado:
                val result = usuarioRepository.guardarRegistroActividad(registro)
                when (result) {
                    is Result.Success<*> -> {
                        val registroId = result.data?.toString() ?: ""
                        _uiState.update {
                            it.copy(
                                registroGuardado = true,
                                registroId = registroId,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al guardar el registro: ${result.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al guardar registro")
                    }
                    is Result.Loading -> { }
                }
                */
                
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

    // Función auxiliar para formatear la hora
    private fun formatTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}