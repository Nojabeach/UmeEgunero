package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoActividad
import com.tfg.umeegunero.data.model.Result
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
            try {
                // Simulación temporal de datos hasta que tengamos el método en el repositorio
                val alumnosInfo = listOf(
                    AlumnoPreescolarInfo(
                        id = "alumno1",
                        nombre = "Ana",
                        apellidos = "García",
                        edad = 3,
                        aula = "Clase A"
                    ),
                    AlumnoPreescolarInfo(
                        id = "alumno2",
                        nombre = "Pablo",
                        apellidos = "Rodríguez",
                        edad = 2,
                        aula = "Clase B"
                    )
                )
                
                _uiState.update { state -> 
                    state.copy(
                        alumnos = alumnosInfo,
                        alumnoSeleccionadoId = if (alumnosInfo.isNotEmpty()) alumnosInfo[0].id else "",
                        isLoading = false
                    )
                }
                
                // Cargar actividades del primer alumno
                if (alumnosInfo.isNotEmpty()) {
                    cargarActividadesDelAlumno(alumnosInfo[0].id)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al cargar niños: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar niños")
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
                                error = "Error al cargar actividades: ${actividadesResult.exception.message}",
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
                val resultado = actividadRepository.marcarComoRevisada(actividadId, comentario)
                
                when (resultado) {
                    is Result.Success -> {
                        // Actualizar localmente
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
                                // Actualizar también la actividad seleccionada si es la que se modificó
                                actividadSeleccionada = if (state.actividadSeleccionada?.id == actividadId) {
                                    actividadesActualizadas.find { it.id == actividadId }
                                } else {
                                    state.actividadSeleccionada
                                }
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al marcar actividad: ${resultado.exception.message}",
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