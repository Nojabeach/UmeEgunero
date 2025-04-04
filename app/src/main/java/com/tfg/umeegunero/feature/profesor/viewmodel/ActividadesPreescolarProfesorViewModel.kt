package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoActividad
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.ActividadPreescolarRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * Filtros para actividades preescolares en la vista del profesor
 */
enum class FiltroActividadProfesor {
    TODAS,
    PENDIENTES,
    REALIZADAS,
    RECIENTES,
    MIS_ACTIVIDADES
}

/**
 * Información básica de una clase para la interfaz
 */
data class ClasePreescolarInfo(
    val id: String,
    val nombre: String,
    val curso: String = "",
    val numAlumnos: Int = 0
)

/**
 * Información básica de un alumno para la interfaz
 */
data class AlumnoPreescolarProfesorInfo(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val edad: Int = 0
)

/**
 * Estado de la UI para la pantalla de gestión de actividades preescolares
 */
data class ActividadesPreescolarProfesorUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val profesorId: String = "",
    val profesorNombre: String = "",
    val clases: List<ClasePreescolarInfo> = emptyList(),
    val claseSeleccionadaId: String = "",
    val alumnos: List<AlumnoPreescolarProfesorInfo> = emptyList(),
    val alumnoSeleccionadoId: String = "",
    val actividades: List<ActividadPreescolar> = emptyList(),
    val actividadesFiltradas: List<ActividadPreescolar> = emptyList(),
    val filtroSeleccionado: FiltroActividadProfesor = FiltroActividadProfesor.TODAS,
    val categoriaSeleccionada: CategoriaActividad? = null,
    
    // Variables para crear/editar actividad
    val actividadEnEdicion: ActividadPreescolar? = null,
    val modoEdicion: Boolean = false,
    val mostrarDialogoNuevaActividad: Boolean = false
)

/**
 * ViewModel para gestionar las actividades preescolares desde la perspectiva del profesor
 */
@HiltViewModel
class ActividadesPreescolarProfesorViewModel @Inject constructor(
    private val actividadRepository: ActividadPreescolarRepository,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActividadesPreescolarProfesorUiState())
    val uiState: StateFlow<ActividadesPreescolarProfesorUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con los datos del profesor
     */
    fun inicializar(profesorId: String, profesorNombre: String) {
        if (profesorId.isEmpty()) {
            _uiState.update { it.copy(error = "No se pudo identificar al profesor") }
            return
        }

        _uiState.update { 
            it.copy(
                profesorId = profesorId,
                profesorNombre = profesorNombre,
                isLoading = true
            ) 
        }
        
        cargarClasesDelProfesor(profesorId)
    }

    /**
     * Carga las clases asignadas al profesor
     */
    private fun cargarClasesDelProfesor(profesorId: String) {
        viewModelScope.launch {
            try {
                val clasesResult = claseRepository.getClasesByProfesor(profesorId)
                
                when (clasesResult) {
                    is Result.Success -> {
                        val clases = clasesResult.data.map { clase ->
                            ClasePreescolarInfo(
                                id = clase.id,
                                nombre = clase.nombre,
                                curso = "",
                                numAlumnos = clase.alumnosIds?.size ?: 0
                            )
                        }
                        
                        _uiState.update { state ->
                            state.copy(
                                clases = clases,
                                claseSeleccionadaId = if (clases.isNotEmpty()) clases[0].id else "",
                                isLoading = false
                            )
                        }
                        
                        // Cargar datos de la primera clase si hay alguna
                        if (clases.isNotEmpty()) {
                            seleccionarClase(clases[0].id)
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al cargar clases: ${clasesResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(clasesResult.exception, "Error al cargar clases del profesor")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al cargar clases: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar clases del profesor")
            }
        }
    }

    /**
     * Selecciona una clase y carga sus alumnos y actividades
     */
    fun seleccionarClase(claseId: String) {
        if (claseId.isEmpty() || claseId == _uiState.value.claseSeleccionadaId) return
        
        _uiState.update { 
            it.copy(
                claseSeleccionadaId = claseId,
                isLoading = true
            ) 
        }
        
        cargarAlumnosPorClase(claseId)
        cargarActividadesPorClase(claseId)
    }

    /**
     * Carga los alumnos de una clase específica
     */
    private fun cargarAlumnosPorClase(claseId: String) {
        viewModelScope.launch {
            try {
                val alumnosResult = alumnoRepository.getAlumnosByClase(claseId)
                
                when (alumnosResult) {
                    is Result.Success -> {
                        val alumnos = alumnosResult.data.map { alumno ->
                            // Calcular edad (implementación simplificada)
                            val edad = try {
                                val fechaNacimientoStr = alumno.fechaNacimiento
                                val añoNacimiento = fechaNacimientoStr.split("-")[0].toInt()
                                val añoActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                añoActual - añoNacimiento
                            } catch (e: Exception) {
                                0
                            }
                            
                            AlumnoPreescolarProfesorInfo(
                                id = alumno.id,
                                nombre = alumno.nombre,
                                apellidos = alumno.apellidos,
                                edad = edad
                            )
                        }
                        
                        _uiState.update { state ->
                            state.copy(
                                alumnos = alumnos,
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al cargar alumnos: ${alumnosResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(alumnosResult.exception, "Error al cargar alumnos de la clase")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al cargar alumnos: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumnos de la clase")
            }
        }
    }

    /**
     * Carga las actividades de una clase específica
     */
    private fun cargarActividadesPorClase(claseId: String) {
        viewModelScope.launch {
            try {
                val actividadesResult = actividadRepository.obtenerActividadesPorClase(claseId)
                
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
                        Timber.e(actividadesResult.exception, "Error al cargar actividades de la clase")
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
                Timber.e(e, "Error inesperado al cargar actividades de la clase")
            }
        }
    }

    /**
     * Aplica un filtro a las actividades
     */
    fun aplicarFiltro(filtro: FiltroActividadProfesor) {
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
        filtro: FiltroActividadProfesor,
        categoria: CategoriaActividad?
    ): List<ActividadPreescolar> {
        // Primero filtrar por estado
        val actividadesPorEstado = when (filtro) {
            FiltroActividadProfesor.TODAS -> actividades
            
            FiltroActividadProfesor.PENDIENTES -> actividades.filter { 
                it.estado == EstadoActividad.PENDIENTE
            }
            
            FiltroActividadProfesor.REALIZADAS -> actividades.filter { 
                it.estado == EstadoActividad.REALIZADA
            }
            
            FiltroActividadProfesor.RECIENTES -> {
                // Filtrar las actividades de los últimos 7 días
                val hace7dias = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -7)
                }.time
                
                actividades.filter { actividad ->
                    actividad.fechaCreacion.toDate().after(hace7dias)
                }
            }
            
            FiltroActividadProfesor.MIS_ACTIVIDADES -> actividades.filter {
                it.profesorId == _uiState.value.profesorId
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
     * Inicia el modo de creación de una nueva actividad
     */
    fun iniciarCreacionActividad() {
        val nuevaActividad = ActividadPreescolar(
            profesorId = _uiState.value.profesorId,
            profesorNombre = _uiState.value.profesorNombre,
            claseId = _uiState.value.claseSeleccionadaId,
            fechaCreacion = Timestamp.now()
        )
        
        _uiState.update { state ->
            state.copy(
                actividadEnEdicion = nuevaActividad,
                modoEdicion = true
            )
        }
    }

    /**
     * Inicia el modo de edición de una actividad existente
     */
    fun editarActividad(actividadId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val actividadResult = actividadRepository.obtenerActividad(actividadId)
                
                when (actividadResult) {
                    is Result.Success -> {
                        val actividad = actividadResult.data
                        
                        if (actividad != null) {
                            _uiState.update { state ->
                                state.copy(
                                    actividadEnEdicion = actividad,
                                    modoEdicion = true,
                                    isLoading = false
                                )
                            }
                        } else {
                            _uiState.update { state ->
                                state.copy(
                                    error = "No se encontró la actividad",
                                    isLoading = false
                                )
                            }
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al cargar actividad: ${actividadResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(actividadResult.exception, "Error al cargar actividad para editar")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al cargar actividad: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar actividad para editar")
            }
        }
    }

    /**
     * Cancela el modo de edición
     */
    fun cancelarEdicion() {
        _uiState.update { state ->
            state.copy(
                actividadEnEdicion = null,
                modoEdicion = false
            )
        }
    }

    /**
     * Guarda una actividad (nueva o editada)
     */
    fun guardarActividad(
        titulo: String,
        descripcion: String,
        categoria: CategoriaActividad,
        alumnoId: String?,
        comentarios: String,
        fechaProgramada: Date?
    ) {
        val actividadActual = _uiState.value.actividadEnEdicion ?: return
        
        if (titulo.isBlank()) {
            _uiState.update { it.copy(error = "El título no puede estar vacío") }
            return
        }
        
        val actividadActualizada = actividadActual.copy(
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            alumnoId = alumnoId.takeIf { !it.isNullOrBlank() },
            comentariosProfesor = comentarios,
            fechaProgramada = fechaProgramada?.let { Timestamp(it) }
        )
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val resultado = if (actividadActual.id.isEmpty()) {
                    // Crear nueva actividad
                    actividadRepository.crearActividad(actividadActualizada)
                } else {
                    // Actualizar actividad existente
                    actividadRepository.actualizarActividad(actividadActualizada).let {
                        when (it) {
                            is Result.Success -> Result.Success(actividadActualizada.id)
                            is Result.Error -> it
                            is Result.Loading -> Result.Success(actividadActualizada.id)
                        }
                    }
                }
                
                when (resultado) {
                    is Result.Success -> {
                        // Recargar actividades para mostrar los cambios
                        cargarActividadesPorClase(_uiState.value.claseSeleccionadaId)
                        
                        _uiState.update { state ->
                            state.copy(
                                actividadEnEdicion = null,
                                modoEdicion = false,
                                mensaje = "Actividad guardada correctamente",
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al guardar actividad: ${resultado.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al guardar actividad")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al guardar actividad: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al guardar actividad")
            }
        }
    }

    /**
     * Elimina una actividad
     */
    fun eliminarActividad(actividadId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val resultado = actividadRepository.eliminarActividad(actividadId)
                
                when (resultado) {
                    is Result.Success -> {
                        // Recargar actividades para mostrar los cambios
                        cargarActividadesPorClase(_uiState.value.claseSeleccionadaId)
                        
                        _uiState.update { state ->
                            state.copy(
                                mensaje = "Actividad eliminada correctamente",
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al eliminar actividad: ${resultado.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al eliminar actividad")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al eliminar actividad: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al eliminar actividad")
            }
        }
    }

    /**
     * Marca una actividad como realizada
     */
    fun marcarComoRealizada(actividadId: String, comentario: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val resultado = actividadRepository.marcarComoRealizada(actividadId, comentario)
                
                when (resultado) {
                    is Result.Success -> {
                        // Actualizar localmente la lista de actividades
                        val actividadesActualizadas = _uiState.value.actividades.map { actividad ->
                            if (actividad.id == actividadId) {
                                actividad.copy(
                                    estado = EstadoActividad.REALIZADA,
                                    comentariosProfesor = if (comentario.isNotEmpty()) comentario else actividad.comentariosProfesor
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
                                mensaje = "Actividad marcada como realizada",
                                isLoading = false
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
                        Timber.e(resultado.exception, "Error al marcar actividad como realizada")
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
                Timber.e(e, "Error inesperado al marcar actividad como realizada")
            }
        }
    }

    /**
     * Muestra el diálogo para crear una nueva actividad
     */
    fun mostrarDialogoCrearActividad() {
        val nuevaActividad = ActividadPreescolar(
            profesorId = _uiState.value.profesorId,
            profesorNombre = _uiState.value.profesorNombre,
            claseId = _uiState.value.claseSeleccionadaId,
            fechaCreacion = Timestamp.now()
        )
        
        _uiState.update { state ->
            state.copy(
                actividadEnEdicion = nuevaActividad,
                modoEdicion = false,
                mostrarDialogoNuevaActividad = true
            )
        }
    }
    
    /**
     * Oculta el diálogo de nueva actividad
     */
    fun ocultarDialogoNuevaActividad() {
        _uiState.update { state ->
            state.copy(
                mostrarDialogoNuevaActividad = false,
                actividadEnEdicion = null,
                modoEdicion = false
            )
        }
    }

    /**
     * Crea una nueva actividad
     */
    fun crearActividad(actividad: ActividadPreescolar) {
        val actividadCompleta = actividad.copy(
            profesorId = _uiState.value.profesorId,
            profesorNombre = _uiState.value.profesorNombre,
            claseId = _uiState.value.claseSeleccionadaId,
            fechaCreacion = Timestamp.now()
        )
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val resultado = actividadRepository.crearActividad(actividadCompleta)
                
                when (resultado) {
                    is Result.Success -> {
                        // Recargar actividades para mostrar los cambios
                        cargarActividadesPorClase(_uiState.value.claseSeleccionadaId)
                        
                        _uiState.update { state ->
                            state.copy(
                                mensaje = "Actividad creada correctamente",
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al crear actividad: ${resultado.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al crear actividad")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al crear actividad: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al crear actividad")
            }
        }
    }
    
    /**
     * Actualiza una actividad existente
     */
    fun actualizarActividad(actividad: ActividadPreescolar) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val resultado = actividadRepository.actualizarActividad(actividad)
                
                when (resultado) {
                    is Result.Success -> {
                        // Recargar actividades para mostrar los cambios
                        cargarActividadesPorClase(_uiState.value.claseSeleccionadaId)
                        
                        _uiState.update { state ->
                            state.copy(
                                mensaje = "Actividad actualizada correctamente",
                                isLoading = false
                            )
                        }
                    }
                    
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = "Error al actualizar actividad: ${resultado.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultado.exception, "Error al actualizar actividad")
                    }
                    
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = "Error inesperado al actualizar actividad: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al actualizar actividad")
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