package com.tfg.umeegunero.feature.profesor.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Asistencia
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.util.Date
import javax.inject.Inject
import com.tfg.umeegunero.data.model.RegistroActividad

/**
 * Enumeración para los posibles estados de asistencia de un alumno
 */
enum class Asistencia {
    PRESENTE,
    FALTA,
    RETRASO,
    AUSENCIA_JUSTIFICADA
}

/**
 * Estado de la UI para la pantalla de listado pre-registro diario
 *
 * @property alumnos Lista completa de alumnos de la clase
 * @property alumnosFiltrados Lista de alumnos filtrados según criterios actuales
 * @property alumnosSeleccionados Lista de alumnos seleccionados para registro
 * @property alumnosConRegistro IDs de alumnos que ya tienen registro para la fecha seleccionada
 * @property fechaSeleccionada Fecha seleccionada para el registro
 * @property esFestivo Indica si la fecha seleccionada es festiva
 * @property nombreClase Nombre de la clase actual
 * @property mostrarSoloPresentes Filtro para mostrar sólo alumnos presentes
 * @property totalAlumnos Total de alumnos en la clase
 * @property alumnosPresentes Total de alumnos presentes hoy
 * @property error Mensaje de error (si existe)
 * @property mensajeExito Mensaje de éxito (si existe)
 * @property isLoading Indica si está cargando datos
 * @property navegarARegistroDiario Indica si debe navegar a la pantalla de registro
 */
data class ListadoPreRegistroDiarioUiState(
    val alumnos: List<Alumno> = emptyList(),
    val alumnosFiltrados: List<Alumno> = emptyList(),
    val alumnosSeleccionados: List<Alumno> = emptyList(),
    val alumnosConRegistro: Set<String> = emptySet(),
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val esFestivo: Boolean = false,
    val nombreClase: String = "",
    val mostrarSoloPresentes: Boolean = false,
    val totalAlumnos: Int = 0,
    val alumnosPresentes: Int = 0,
    val error: String? = null,
    val mensajeExito: String? = null,
    val isLoading: Boolean = true,
    val navegarARegistroDiario: Boolean = false
)

/**
 * ViewModel para la pantalla de listado pre-registro diario
 */
@HiltViewModel
class ListadoPreRegistroDiarioViewModel @Inject constructor(
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository, 
    private val profesorRepository: ProfesorRepository,
    private val asistenciaRepository: AsistenciaRepository,
    private val calendarioRepository: CalendarioRepository,
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListadoPreRegistroDiarioUiState())
    val uiState: StateFlow<ListadoPreRegistroDiarioUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga los datos iniciales: alumnos, asistencia, y estado de festivos
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener usuario actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener el usuario actual",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener datos del profesor
                val profesor = profesorRepository.getProfesorPorUsuarioId(usuario.dni)
                if (profesor == null) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener clases asignadas al profesor
                val clasesResult = claseRepository.getClasesByProfesor(profesor.id)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No hay clases asignadas a este profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Por simplicidad, trabajamos con la primera clase
                val clase = clasesResult.data.first()
                
                // Obtener alumnos de la clase
                val alumnosResult = alumnoRepository.getAlumnosByClaseId(clase.id)
                val alumnos = if (alumnosResult is Result.Success) alumnosResult.data else emptyList()
                
                // Obtener registros existentes para la fecha actual
                val fechaActual = _uiState.value.fechaSeleccionada
                val registros = getRegistrosDiariosPorFechaYClase(
                    LocalDate.now().toString(),
                    clase.id
                )
                
                // Crear set con IDs de alumnos que ya tienen registro
                val alumnosConRegistro = registros.map { it.alumnoId }.toSet()
                
                // Verificar si la fecha actual es festiva
                val esFestivo = esDiaFestivo(fechaActual)
                
                // Obtener datos de asistencia
                val fechaJava = java.util.Date.from(
                    fechaActual.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
                )
                
                val asistenciaResult = asistenciaRepository.obtenerAsistencia(clase.id, fechaJava)
                val asistencias = if (asistenciaResult is Result.Success) asistenciaResult.data else emptyMap()
                
                // Actualizar presentes en los alumnos basado en asistencia
                val alumnosConPresencia = alumnos.map { alumno ->
                    val asistencia = asistencias[alumno.id]
                    alumno.copy(presente = asistencia == Asistencia.PRESENTE)
                }
                
                // Actualizar el estado
                val alumnosFiltrados = if (_uiState.value.mostrarSoloPresentes) {
                    alumnosConPresencia.filter { it.presente }
                } else {
                    alumnosConPresencia
                }
                
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        alumnos = alumnosConPresencia,
                        alumnosFiltrados = alumnosFiltrados,
                        nombreClase = clase.nombre,
                        totalAlumnos = alumnosConPresencia.size,
                        alumnosPresentes = alumnosConPresencia.count { it.presente },
                        esFestivo = esFestivo,
                        alumnosConRegistro = alumnosConRegistro
                    )
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos")
                _uiState.update { it.copy(
                    error = "Error al cargar datos: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Verifica si una fecha es festiva consultando al repositorio
     */
    private suspend fun esDiaFestivo(fecha: LocalDate): Boolean {
        try {
            val resultado = calendarioRepository.esDiaFestivo(fecha)
            return when (resultado) {
                is Result.Success -> resultado.data
                else -> fecha.dayOfWeek.value > 5 // Considerar fin de semana como festivo
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar día festivo")
            return fecha.dayOfWeek.value > 5 // Valor por defecto - fin de semana
        }
    }

    /**
     * Selecciona una fecha para el registro
     */
    fun seleccionarFecha(fecha: LocalDate) {
        if (fecha == _uiState.value.fechaSeleccionada) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(fechaSeleccionada = fecha, isLoading = true) }
            
            try {
                // Verificar si la fecha es festiva
                val esFestivo = esDiaFestivo(fecha)
                _uiState.update { it.copy(esFestivo = esFestivo) }
                
                // Obtener usuario y profesor
                val usuario = authRepository.getCurrentUser() ?: return@launch
                val profesor = profesorRepository.getProfesorPorUsuarioId(usuario.dni) ?: return@launch
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(profesor.id)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) return@launch
                
                val clase = clasesResult.data.first()
                
                // Obtener registros para esta fecha
                val registros = getRegistrosDiariosPorFechaYClase(
                    fecha.toString(),
                    clase.id
                )
                
                // Actualizar IDs de alumnos con registro
                val alumnosConRegistro = registros.map { it.alumnoId }.toSet()
                
                // Obtener asistencia para la fecha
                val fechaJava = java.util.Date.from(
                    fecha.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
                )
                
                val asistenciaResult = asistenciaRepository.obtenerAsistencia(clase.id, fechaJava)
                val asistencias = if (asistenciaResult is Result.Success) asistenciaResult.data else emptyMap()
                
                // Actualizar presencia en alumnos
                val alumnosActualizados = _uiState.value.alumnos.map { alumno ->
                    val asistencia = asistencias[alumno.id]
                    alumno.copy(presente = asistencia == Asistencia.PRESENTE)
                }
                
                // Actualizar lista filtrada
                val alumnosFiltrados = if (_uiState.value.mostrarSoloPresentes) {
                    alumnosActualizados.filter { it.presente }
                } else {
                    alumnosActualizados
                }
                
                _uiState.update { it.copy(
                    alumnos = alumnosActualizados,
                    alumnosFiltrados = alumnosFiltrados,
                    alumnosConRegistro = alumnosConRegistro,
                    alumnosPresentes = alumnosActualizados.count { it.presente },
                    isLoading = false
                ) }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cambiar la fecha")
                _uiState.update { it.copy(
                    error = "Error al cambiar fecha: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Alterna la selección de un alumno
     */
    fun toggleSeleccionAlumno(alumno: Alumno) {
        _uiState.update { currentState ->
            val alumnosSeleccionados = currentState.alumnosSeleccionados.toMutableList()
            
            if (alumnosSeleccionados.contains(alumno)) {
                alumnosSeleccionados.remove(alumno)
            } else {
                alumnosSeleccionados.add(alumno)
            }
            
            currentState.copy(alumnosSeleccionados = alumnosSeleccionados)
        }
    }

    /**
     * Selecciona todos los alumnos filtrados
     */
    fun seleccionarTodosLosAlumnos() {
        _uiState.update { currentState ->
            val alumnosVisibles = currentState.alumnosFiltrados
            val yaSeleccionados = currentState.alumnosSeleccionados
            
            // Si todos los visibles ya están seleccionados, desseleccionamos todos
            val nuevaSeleccion = if (alumnosVisibles.all { yaSeleccionados.contains(it) }) {
                yaSeleccionados.filter { !alumnosVisibles.contains(it) }
            } else {
                // Si no, seleccionamos todos los visibles
                (yaSeleccionados + alumnosVisibles).distinct()
            }
            
            currentState.copy(alumnosSeleccionados = nuevaSeleccion)
        }
    }

    /**
     * Alterna el filtro para mostrar solo alumnos presentes
     */
    fun toggleFiltroPresentes() {
        _uiState.update { currentState ->
            val mostrarSoloPresentes = !currentState.mostrarSoloPresentes
            val alumnosFiltrados = if (mostrarSoloPresentes) {
                currentState.alumnos.filter { it.presente }
            } else {
                currentState.alumnos
            }
            
            currentState.copy(
                mostrarSoloPresentes = mostrarSoloPresentes,
                alumnosFiltrados = alumnosFiltrados
            )
        }
    }

    /**
     * Completa automáticamente registros para todos los alumnos presentes
     */
    fun completarAutomaticamente() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val alumnosPresentes = _uiState.value.alumnos.filter { it.presente }
                if (alumnosPresentes.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No hay alumnos presentes para registrar",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener usuario y profesor
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener el usuario actual",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                val profesor = profesorRepository.getProfesorPorUsuarioId(usuario.dni)
                if (profesor == null) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener clase
                val clasesResult = claseRepository.getClasesByProfesor(profesor.id)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No hay clases asignadas a este profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                val clase = clasesResult.data.first()
                
                // Crear registros automáticos para cada alumno presente
                val fecha = _uiState.value.fechaSeleccionada.toString()
                var contadorExitos = 0
                
                for (alumno in alumnosPresentes) {
                    // Verificar si ya tiene registro
                    if (_uiState.value.alumnosConRegistro.contains(alumno.id)) {
                        continue
                    }
                    
                    val resultado = crearRegistroAutomatico(
                        alumnoId = alumno.id,
                        claseId = clase.id,
                        profesorId = profesor.id,
                        fecha = fecha
                    )
                    
                    if (resultado) contadorExitos++
                }
                
                // Actualizar la lista de alumnos con registro
                val nuevosRegistros = getRegistrosDiariosPorFechaYClase(
                    fecha,
                    clase.id
                )
                
                val alumnosConRegistro = nuevosRegistros.map { it.alumnoId }.toSet()
                
                _uiState.update { it.copy(
                    alumnosConRegistro = alumnosConRegistro,
                    mensajeExito = "Se han completado automáticamente $contadorExitos registros",
                    isLoading = false
                ) }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al completar registros automáticamente")
                _uiState.update { it.copy(
                    error = "Error al crear registros: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Método provisional para crear un registro automático
     */
    private suspend fun crearRegistroAutomatico(
        alumnoId: String,
        claseId: String,
        profesorId: String,
        fecha: String
    ): Boolean {
        return try {
            // Implementación temporal - en producción se usaría:
            // registroDiarioRepository.crearRegistroAutomatico(alumnoId, claseId, profesorId, fecha)
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al crear registro automático")
            false
        }
    }

    /**
     * Inicia la navegación a la pantalla de registro diario
     */
    fun iniciarRegistroDiario() {
        if (_uiState.value.alumnosSeleccionados.isEmpty()) {
            _uiState.update { it.copy(error = "Selecciona al menos un alumno") }
            return
        }
        
        _uiState.update { it.copy(navegarARegistroDiario = true) }
    }

    /**
     * Resetea el estado de navegación
     */
    fun resetearNavegacion() {
        _uiState.update { it.copy(navegarARegistroDiario = false) }
    }

    /**
     * Limpia mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia mensaje de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    /**
     * Obtiene los registros diarios por fecha y clase
     * Método provisional mientras se completa la implementación en el repositorio
     */
    private suspend fun getRegistrosDiariosPorFechaYClase(fecha: String, claseId: String): List<RegistroActividad> {
        return try {
            // Implementación temporal - en producción se usaría:
            // registroDiarioRepository.obtenerRegistrosDiariosPorFechaYClase(fecha, claseId)
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por fecha y clase")
            emptyList()
        }
    }
} 