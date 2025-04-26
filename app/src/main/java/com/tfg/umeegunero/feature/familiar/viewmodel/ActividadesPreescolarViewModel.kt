package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoActividad
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.ActividadPreescolarRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
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
 * Filtros para actividades preescolares en la vista de los familiares
 */
enum class FiltroActividad {
    TODAS,
    PENDIENTES,
    REALIZADAS,
    RECIENTES
}

/**
 * Información básica del alumno para la interfaz
 */
data class AlumnoPreescolarInfo(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val edad: Int = 0,
    val aula: String = ""
)

/**
 * Estado de la UI para la pantalla de actividades preescolares para familiares
 */
data class ActividadesPreescolarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val familiarId: String = "",
    val alumnos: List<AlumnoPreescolarInfo> = emptyList(),
    val alumnoSeleccionadoId: String = "",
    val actividades: List<ActividadPreescolar> = emptyList(),
    val actividadesFiltradas: List<ActividadPreescolar> = emptyList(),
    val filtroSeleccionado: FiltroActividad = FiltroActividad.TODAS,
    val categoriaSeleccionada: CategoriaActividad? = null,
    val actividadSeleccionada: ActividadPreescolar? = null,
    val mostrarDetalleActividad: Boolean = false
)

/**
 * ViewModel para visualizar las actividades preescolares desde la perspectiva del familiar
 */
@HiltViewModel
class ActividadesPreescolarViewModel @Inject constructor(
    private val actividadRepository: ActividadPreescolarRepository,
    private val alumnoRepository: AlumnoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActividadesPreescolarUiState())
    val uiState: StateFlow<ActividadesPreescolarUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con los datos del familiar
     */
    fun inicializar(familiarId: String) {
        if (familiarId.isEmpty()) {
            _uiState.update { it.copy(error = "No se pudo identificar al familiar") }
            return
        }

        _uiState.update { 
            it.copy(
                familiarId = familiarId,
                isLoading = true
            ) 
        }
        
        cargarAlumnosDelFamiliar(familiarId)
    }

    /**
     * Carga la información de los alumnos asociados al familiar
     */
    private fun cargarAlumnosDelFamiliar(familiarId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val alumnosResult: Result<List<Alumno>> = alumnoRepository.obtenerAlumnosPorFamiliar(familiarId)

                when (alumnosResult) {
                    is Result.Success<List<Alumno>> -> {
                        val alumnosData = alumnosResult.data
                        val alumnosInfo = alumnosData.map { alumno: Alumno ->
                            AlumnoPreescolarInfo(
                                id = alumno.id,
                                nombre = alumno.nombre,
                                apellidos = alumno.apellidos,
                                edad = calcularEdad(parseFechaNacimiento(alumno.fechaNacimiento)),
                                aula = alumno.clase.ifEmpty { "Clase desconocida" }
                            )
                        }

                        val alumnoIdSeleccionado = if (alumnosInfo.isNotEmpty()) alumnosInfo[0].id else ""

                        _uiState.update { state ->
                            state.copy(
                                alumnos = alumnosInfo,
                                alumnoSeleccionadoId = alumnoIdSeleccionado,
                                isLoading = false
                            )
                        }

                        if (alumnoIdSeleccionado.isNotEmpty()) {
                            cargarActividadesDelAlumno(alumnoIdSeleccionado)
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al cargar niños: ${alumnosResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(alumnosResult.exception, "Error al cargar niños del familiar $familiarId")
                    }
                    is Result.Loading -> {
                       // Ya estamos manejando isLoading al inicio de la corrutina
                    }
                }

            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al cargar niños: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar niños del familiar $familiarId")
            }
        }
    }

    /**
     * Calcula la edad en años a partir de la fecha de nacimiento
     */
    private fun calcularEdad(fechaNacimiento: Date?): Int {
        if (fechaNacimiento == null) return 0
        
        val hoy = Calendar.getInstance()
        val nacimiento = Calendar.getInstance().apply { time = fechaNacimiento }
        
        var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
        
        // Ajustar si todavía no ha cumplido años este año
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        
        return edad
    }

    /**
     * Parsea la fecha de nacimiento de String a Date.
     * Asume formato YYYY-MM-DD u otros formatos comunes.
     */
    private fun parseFechaNacimiento(fechaString: String?): Date? {
        if (fechaString.isNullOrEmpty()) return null
        // Intentar con varios formatos comunes
        val formatos = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        )
        for (formato in formatos) {
            try {
                return formato.parse(fechaString)
            } catch (e: Exception) { 
                // Ignorar y probar el siguiente formato
            }
        }
        Timber.w("No se pudo parsear la fecha de nacimiento: $fechaString")
        return null // Devolver null si ningún formato funciona
    }

    /**
     * Carga las actividades asociadas a un alumno específico
     */
    fun cargarActividadesDelAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { state -> 
                state.copy(
                    isLoading = true,
                    alumnoSeleccionadoId = alumnoId
                ) 
            }

            try {
                val actividadesResult = actividadRepository.obtenerActividadesPorAlumno(alumnoId)
                
                when (actividadesResult) {
                    is Result.Success -> {
                        val actividades = actividadesResult.data
                        
                        _uiState.update { state ->
                            state.copy(
                                actividades = actividades,
                                actividadesFiltradas = filtrarActividades(
                                    actividades, 
                                    state.filtroSeleccionado, 
                                    state.categoriaSeleccionada
                                ),
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al cargar actividades: ${actividadesResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(actividadesResult.exception, "Error al cargar actividades")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al cargar actividades: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar actividades")
            }
        }
    }

    /**
     * Aplica un filtro a las actividades
     */
    fun aplicarFiltro(filtro: FiltroActividad) {
        val actividadesFiltradas = filtrarActividades(
            _uiState.value.actividades, 
            filtro, 
            _uiState.value.categoriaSeleccionada
        )
        
        _uiState.update { state -> 
            state.copy(
                filtroSeleccionado = filtro,
                actividadesFiltradas = actividadesFiltradas
            )
        }
    }

    /**
     * Aplica un filtro por categoría de actividad
     */
    fun aplicarFiltroCategoria(categoria: CategoriaActividad?) {
        val actividadesFiltradas = filtrarActividades(
            _uiState.value.actividades, 
            _uiState.value.filtroSeleccionado,
            categoria
        )
        
        _uiState.update { state -> 
            state.copy(
                categoriaSeleccionada = categoria,
                actividadesFiltradas = actividadesFiltradas
            )
        }
    }

    /**
     * Filtra las actividades según los criterios seleccionados
     */
    private fun filtrarActividades(
        actividades: List<ActividadPreescolar>,
        filtro: FiltroActividad,
        categoria: CategoriaActividad?
    ): List<ActividadPreescolar> {
        // Primero filtrar por estado
        val actividadesPorEstado = when (filtro) {
            FiltroActividad.TODAS -> actividades
            
            FiltroActividad.PENDIENTES -> actividades.filter { 
                it.estado == EstadoActividad.PENDIENTE
            }
            
            FiltroActividad.REALIZADAS -> actividades.filter { 
                it.estado == EstadoActividad.REALIZADA
            }
            
            FiltroActividad.RECIENTES -> {
                // Filtrar las actividades de los últimos 7 días
                val hace7dias = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
                
                actividades.filter { actividad ->
                    actividad.fechaCreacion.toDate().after(hace7dias)
                }
            }
        }
        
        // Luego filtrar por categoría si está seleccionada
        return if (categoria != null) {
            actividadesPorEstado.filter { it.categoria == categoria }
        } else {
            actividadesPorEstado
        }
    }

    /**
     * Selecciona una actividad para ver sus detalles
     */
    fun seleccionarActividad(actividadId: String) {
        val actividad = _uiState.value.actividades.find { it.id == actividadId }
        
        if (actividad != null) {
            _uiState.update { state ->
                state.copy(
                    actividadSeleccionada = actividad,
                    mostrarDetalleActividad = true
                )
            }
        }
    }

    /**
     * Cierra la vista de detalles de actividad
     */
    fun cerrarDetalleActividad() {
        _uiState.update { state ->
            state.copy(
                mostrarDetalleActividad = false
            )
        }
    }

    /**
     * Marca una actividad como revisada por el familiar
     */
    fun marcarComoRevisada(actividadId: String, comentario: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val actividadesActualizadas = _uiState.value.actividades.map { actividad ->
                    if (actividad.id == actividadId) {
                        actividad.copy(
                            revisadaPorFamiliar = true,
                            fechaRevision = Timestamp.now(),
                            comentariosFamiliar = comentario
                        )
                    } else {
                        actividad
                    }
                }

                when (val resultado: Result<Boolean> = actividadRepository.marcarComoRevisada(actividadId, comentario)) {
                    is Result.Success<Boolean> -> {
                        _uiState.update { state ->
                            state.copy(
                                actividades = actividadesActualizadas,
                                actividadesFiltradas = filtrarActividades(
                                    actividadesActualizadas,
                                    state.filtroSeleccionado,
                                    state.categoriaSeleccionada
                                ),
                                mensaje = "Actividad marcada como revisada",
                                isLoading = false,
                                actividadSeleccionada = if (state.actividadSeleccionada?.id == actividadId) {
                                    actividadesActualizadas.find { it.id == actividadId }
                                } else {
                                    state.actividadSeleccionada
                                },
                                mostrarDetalleActividad = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al marcar actividad: ${resultado.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al marcar actividad como revisada")
                    }
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al marcar actividad: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al marcar actividad como revisada")
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