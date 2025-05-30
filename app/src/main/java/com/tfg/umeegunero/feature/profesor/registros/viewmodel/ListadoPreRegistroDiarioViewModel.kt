package com.tfg.umeegunero.feature.profesor.registros.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.EstadoAsistencia
import com.tfg.umeegunero.data.model.EstadoNotificacionAusencia
import com.tfg.umeegunero.data.model.NotificacionAusencia
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.RegistroDiario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.NotificacionAusenciaRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.format.DateTimeFormatter

/**
 * Extensión para convertir LocalDate a Date
 */
fun LocalDate.toDate(): Date {
    return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

/**
 * Enumeración para los posibles estados de asistencia de un alumno
 */
enum class EstadoAsistencia {
    PRESENTE,
    FALTA,
    RETRASO,
    AUSENCIA_JUSTIFICADA
}

/**
 * Datos del informe de asistencia para mostrar en el diálogo
 */
data class InformeAsistencia(
    val totalAlumnos: Int = 0,
    val alumnosPresentes: Int = 0,
    val alumnosAusentes: Int = 0,
    val porcentajeAsistencia: Float = 0f,
    val alumnosConRegistro: Int = 0,
    val alumnosSinRegistro: Int = 0,
    val fecha: LocalDate = LocalDate.now()
)

/**
 * Estado de la UI para la pantalla de listado pre-registro diario
 *
 * @property alumnos Lista completa de alumnos de la clase
 * @property alumnosFiltrados Lista de alumnos filtrados según criterios actuales
 * @property alumnosSeleccionados Lista de alumnos seleccionados para registro
 * @property alumnosConRegistro IDs de alumnos que ya tienen registro para la fecha seleccionada
 * @property alumnosConAusenciaJustificada IDs de alumnos con ausencia justificada para la fecha seleccionada
 * @property fechaSeleccionada Fecha seleccionada para el registro
 * @property esFestivo Indica si la fecha seleccionada es festiva
 * @property nombreClase Nombre de la clase actual
 * @property claseId ID de la clase actual
 * @property mostrarSoloPresentes Filtro para mostrar sólo alumnos presentes
 * @property totalAlumnos Total de alumnos en la clase
 * @property alumnosPresentes Total de alumnos presentes hoy
 * @property error Mensaje de error (si existe)
 * @property mensajeExito Mensaje de éxito (si existe)
 * @property isLoading Indica si está cargando datos
 * @property navegarARegistroDiario Indica si debe navegar a la pantalla de registro
 * @property profesorId ID del profesor (cuando se recibe desde otra pantalla)
 * @property mostrarDialogoInforme Indica si debe mostrarse el diálogo de informe
 * @property datosInforme Datos del informe de asistencia
 * @property ausenciasNotificadas Lista de notificaciones de ausencia para la clase actual
 * @property mostrarDialogoAusencia Indica si debe mostrarse el diálogo de detalle de ausencia
 * @property ausenciaSeleccionada Notificación de ausencia seleccionada para mostrar detalles
 * @property hayNuevaNotificacionAusencia Indica si hay una nueva notificación de ausencia
 */
data class ListadoPreRegistroDiarioUiState(
    val alumnos: List<Alumno> = emptyList(),
    val alumnosFiltrados: List<Alumno> = emptyList(),
    val alumnosSeleccionados: List<Alumno> = emptyList(),
    val alumnosConRegistro: Set<String> = emptySet(),
    val alumnosConAusenciaJustificada: Set<String> = emptySet(),
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val esFestivo: Boolean = false,
    val nombreClase: String = "",
    val claseId: String = "",
    val mostrarSoloPresentes: Boolean = false,
    val totalAlumnos: Int = 0,
    val alumnosPresentes: Int = 0,
    val error: String? = null,
    val mensajeExito: String? = null,
    val isLoading: Boolean = true,
    val navegarARegistroDiario: Boolean = false,
    val profesorId: String = "",
    val mostrarDialogoInforme: Boolean = false,
    val datosInforme: InformeAsistencia = InformeAsistencia(),
    val ausenciasNotificadas: List<NotificacionAusencia> = emptyList(),
    val mostrarDialogoAusencia: Boolean = false,
    val ausenciaSeleccionada: NotificacionAusencia? = null,
    val hayNuevaNotificacionAusencia: Boolean = false
)

/**
 * ViewModel para la pantalla de listado pre-registro diario
 */
@HiltViewModel
class ListadoPreRegistroDiarioViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val asistenciaRepository: AsistenciaRepository,
    private val alumnoRepository: AlumnoRepository,
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val claseRepository: ClaseRepository,
    private val calendarioRepository: CalendarioRepository,
    private val notificacionAusenciaRepository: NotificacionAusenciaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListadoPreRegistroDiarioUiState())
    val uiState: StateFlow<ListadoPreRegistroDiarioUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga los datos iniciales: profesor, clase y alumnos asociados.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Obtener usuario actual
                val usuarioActual = authRepository.getCurrentUser()
                if (usuarioActual == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener el usuario actual",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Si tenemos un ID de profesor pasado como parámetro, lo usamos
                if (_uiState.value.profesorId.isNotEmpty()) {
                    setProfesorId(_uiState.value.profesorId)
                    return@launch
                }
                
                // Obtener el profesor por el ID de usuario
                val usuarioProfesor = usuarioRepository.getUsuarioById(usuarioActual.dni)
                if (usuarioProfesor !is Result.Success) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener las clases asignadas al profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.data.dni)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No hay clases asignadas a este profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Por simplicidad, trabajamos con la primera clase
                val clase = clasesResult.data.first()
                Timber.d("Llamando a getAlumnosByClase con clase.id: ${clase.id} (nombre: ${clase.nombre})")
                
                val alumnosResult = usuarioRepository.getAlumnosByClase(clase.id)
                val alumnos = if (alumnosResult is Result.Success) alumnosResult.data else emptyList()
                
                // Registrar los valores de registroDiarioLeido para cada alumno
                Timber.d("ListadoPreRegistroDiarioViewModel (cargarDatosPreRegistro): Alumnos recibidos: ${alumnos.joinToString { 
                    it.nombre + " (ID: " + it.id + ", DNI: " + it.dni + ", Presente: " + it.presente + 
                    ", RegistroLeido: " + it.registroDiarioLeido + ")" 
                }}")
                
                // Obtener registros existentes para la fecha actual
                val fechaActual = _uiState.value.fechaSeleccionada
                val registros = getRegistrosDiariosPorFechaYClase(
                    fechaActual.toString(),
                    clase.id
                )
                
                // Crear set con IDs de alumnos que ya tienen registro
                val alumnosConRegistro = registros.map { it.alumnoId }.toSet()
                
                // Marcar los registros leídos para depuración
                val estadoLecturaRegistros = registros.joinToString("\n") { 
                    "Registro: ${it.id}, AlumnoId: ${it.alumnoId}, Visto: ${it.vistoPorFamiliar}, Lecturas: ${it.lecturasPorFamiliar.keys}" 
                }
                Timber.d("Estado de lectura de registros:\n$estadoLecturaRegistros")
                
                Timber.d("cargarDatos - Fecha seleccionada: $fechaActual")
                Timber.d("cargarDatos - Alumnos con registro: $alumnosConRegistro")
                
                // Verificar si la fecha actual es festiva
                val esFestivo = esDiaFestivo(fechaActual)
                
                // Obtener datos de asistencia
                val fechaJava = java.util.Date.from(
                    fechaActual.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
                )
                
                val asistenciaResult = asistenciaRepository.obtenerRegistroAsistencia(clase.id, fechaJava)
                val estadosAsistencia = asistenciaResult?.estadosAsistencia ?: emptyMap()
                
                // Actualizar presentes en los alumnos basado en asistencia
                val alumnosConPresencia = alumnos.map { alumno ->
                    val estado = estadosAsistencia[alumno.id]
                    alumno.copy(presente = estado == EstadoAsistencia.PRESENTE)
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
                        claseId = clase.id,
                        totalAlumnos = alumnosConPresencia.size,
                        alumnosPresentes = alumnosConPresencia.count { it.presente },
                        esFestivo = esFestivo,
                        alumnosConRegistro = alumnosConRegistro
                    )
                }
                
                // Cargar ausencias notificadas
                cargarAusenciasNotificadas()
                
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
                is Result.Success<Boolean> -> resultado.data
                else -> fecha.dayOfWeek.value > 5 // Considerar fin de semana como festivo
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar día festivo")
            return fecha.dayOfWeek.value > 5 // Valor por defecto - fin de semana
        }
    }

    /**
     * Selecciona una fecha para el registro diario
     *
     * @param fecha Nueva fecha seleccionada
     */
    fun seleccionarFecha(fecha: LocalDate) {
        viewModelScope.launch {
            try {
                // Verificar si la fecha es festiva
                val esFestivo = esDiaFestivo(fecha)
                _uiState.update { it.copy(esFestivo = esFestivo) }
                
                // Obtener usuario y profesor
                val usuario = authRepository.getCurrentUser() ?: return@launch
                val usuarioProfesor = usuarioRepository.getUsuarioById(usuario.dni)
                if (usuarioProfesor !is Result.Success) return@launch
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.data.dni)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) return@launch
                
                val clase = clasesResult.data.first()
                Timber.d("Llamando a getAlumnosByClase en seleccionarFecha con clase.id: ${clase.id} (nombre: ${clase.nombre})")
                
                // Obtener registros para esta fecha
                val registros = getRegistrosDiariosPorFechaYClase(
                    fecha.toString(),
                    clase.id
                )
                
                // Actualizar IDs de alumnos con registro
                val alumnosConRegistro = registros.map { it.alumnoId }.toSet()
                
                Timber.d("seleccionarFecha - Nueva fecha: $fecha")
                Timber.d("seleccionarFecha - Registros encontrados: ${registros.size}")
                Timber.d("seleccionarFecha - Alumnos con registro: $alumnosConRegistro")
                
                // Obtener asistencia para la fecha
                val fechaJava = java.util.Date.from(
                    fecha.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
                )
                
                val asistenciaResult = asistenciaRepository.obtenerRegistroAsistencia(clase.id, fechaJava)
                val estadosAsistencia = asistenciaResult?.estadosAsistencia ?: emptyMap()
                
                // Actualizar estados de asistencia para los alumnos
                val alumnosActualizados = _uiState.value.alumnos.map { alumno ->
                    val estado = estadosAsistencia[alumno.id]
                    alumno.copy(presente = estado == EstadoAsistencia.PRESENTE)
                }
                
                // Actualizar UI state
                _uiState.update { it.copy(
                    fechaSeleccionada = fecha,
                    alumnosConRegistro = alumnosConRegistro,
                    alumnos = alumnosActualizados,
                    alumnosFiltrados = if (it.mostrarSoloPresentes) 
                        alumnosActualizados.filter { a -> a.presente } 
                    else 
                        alumnosActualizados,
                    alumnosPresentes = alumnosActualizados.count { a -> a.presente },
                    alumnosSeleccionados = it.alumnosSeleccionados.filter { alumno -> !alumnosConRegistro.contains(alumno.id) }
                )}
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar fecha: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error al seleccionar fecha: ${e.message}"
                )}
            }
        }
    }

    /**
     * Selecciona un alumno para el registro diario
     */
    fun seleccionarAlumno(alumno: Alumno) {
        val currentState = _uiState.value
        
        // Verificar que el alumno no tenga registro previo
        if (!currentState.alumnosConRegistro.contains(alumno.id) && 
            !currentState.alumnosSeleccionados.contains(alumno)) {
            Timber.d("Seleccionando alumno: ${alumno.nombre} (ID: ${alumno.id})")
            _uiState.update {
                it.copy(
                    alumnosSeleccionados = it.alumnosSeleccionados + alumno
                )
            }
        } else {
            // Si ya tiene registro, mostrar error
            if (currentState.alumnosConRegistro.contains(alumno.id)) {
                Timber.d("No se puede seleccionar alumno ${alumno.nombre}: ya tiene registro")
                mostrarErrorTemporal("El alumno ${alumno.nombre} ya tiene un registro para hoy")
            }
            // Si ya estaba seleccionado, no hacer nada
        }
    }

    /**
     * Deselecciona un alumno del registro diario
     */
    fun deseleccionarAlumno(alumno: Alumno) {
        _uiState.update { currentState ->
            currentState.copy(
                alumnosSeleccionados = currentState.alumnosSeleccionados.filter { it.id != alumno.id }
            )
        }
    }

    /**
     * Selecciona todos los alumnos para el registro diario
     */
    fun seleccionarTodosLosAlumnos() {
        val currentState = _uiState.value
        
        // Filtrar alumnos que no tengan registro previo
        val alumnosSinRegistro = currentState.alumnos.filter { alumno -> 
            !currentState.alumnosConRegistro.contains(alumno.id) 
        }
        
        if (alumnosSinRegistro.isEmpty()) {
            Timber.d("No hay alumnos sin registro para seleccionar")
            mostrarErrorTemporal("Todos los alumnos ya tienen registro para hoy")
        } else {
            Timber.d("Seleccionando todos los alumnos sin registro: ${alumnosSinRegistro.size}")
            _uiState.update {
                it.copy(
                    alumnosSeleccionados = alumnosSinRegistro,
                    error = null
                )
            }
        }
    }

    /**
     * Deselecciona todos los alumnos
     */
    fun deseleccionarTodosLosAlumnos() {
        _uiState.update { currentState ->
            currentState.copy(
                alumnosSeleccionados = emptyList()
            )
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
                
                val usuarioProfesor = usuarioRepository.getUsuarioById(usuario.dni)
                if (usuarioProfesor !is Result.Success) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener clase
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.data.dni)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No hay clases asignadas a este profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                val clase = clasesResult.data.first()
                Timber.d("Llamando a getAlumnosByClase en completarAutomaticamente con clase.id: ${clase.id} (nombre: ${clase.nombre})")
                
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
                        profesorId = usuarioProfesor.data.dni,
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
        @Suppress("UNUSED_PARAMETER") alumnoId: String,
        @Suppress("UNUSED_PARAMETER") claseId: String,
        @Suppress("UNUSED_PARAMETER") profesorId: String,
        @Suppress("UNUSED_PARAMETER") fecha: String
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
     * Inicia el proceso de registro diario para los alumnos seleccionados
     * Registra la asistencia de los alumnos seleccionados y luego navega a la pantalla de registro diario
     */
    fun iniciarRegistroDiario() {
        val alumnosSeleccionados = _uiState.value.alumnosSeleccionados
        val alumnosConRegistro = _uiState.value.alumnosConRegistro
        
        // Verificar si hay alumnos seleccionados con registro previo
        val alumnosYaRegistrados = alumnosSeleccionados.filter { alumno -> 
            alumnosConRegistro.contains(alumno.id) 
        }
        
        if (alumnosYaRegistrados.isNotEmpty()) {
            _uiState.update { it.copy(
                error = "Hay ${alumnosYaRegistrados.size} alumno(s) seleccionado(s) que ya tienen registro para hoy"
            ) }
            return
        }
        
        if (alumnosSeleccionados.isEmpty()) {
            _uiState.update { it.copy(
                error = "Debes seleccionar al menos un alumno"
            ) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener fecha actual y clase
                val fecha = _uiState.value.fechaSeleccionada.toDate()
                
                // Obtener usuario actual y clase
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener el usuario actual",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                val usuarioProfesor = usuarioRepository.getUsuarioById(usuario.dni)
                if (usuarioProfesor !is Result.Success) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.data.dni)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No hay clases asignadas a este profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                val clase = clasesResult.data.first()
                
                // Crear mapa de asistencia para los alumnos seleccionados
                val estadosAsistencia = mutableMapOf<String, EstadoAsistencia>()
                
                // Todos los alumnos seleccionados se marcan como PRESENTE
                _uiState.value.alumnosSeleccionados.forEach { alumno ->
                    estadosAsistencia[alumno.id] = EstadoAsistencia.PRESENTE
                }
                
                // Registrar la asistencia en Firestore
                val registroAsistencia = com.tfg.umeegunero.data.model.RegistroAsistencia(
                    claseId = clase.id,
                    profesorId = usuarioProfesor.data.dni,
                    fecha = com.google.firebase.Timestamp(fecha),
                    estadosAsistencia = estadosAsistencia,
                    observaciones = ""
                )
                
                val resultado = asistenciaRepository.guardarRegistroAsistencia(registroAsistencia)
                
                if (resultado) {
                    Timber.d("Asistencia registrada correctamente para ${estadosAsistencia.size} alumnos")
                    // Navegamos a la pantalla de registro diario
                    _uiState.update { it.copy(
                        isLoading = false,
                        mensajeExito = "Asistencia registrada correctamente",
                        navegarARegistroDiario = true
                    ) }
                } else {
                    // Si hay error, mostramos mensaje pero seguimos adelante con la navegación
                    Timber.e("Error al registrar asistencia")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al registrar asistencia, pero puedes continuar con el registro diario",
                        navegarARegistroDiario = true
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al iniciar registro diario")
                _uiState.update { it.copy(
                    error = "Error al iniciar registro diario: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Resetea el estado de navegación
     */
    fun resetearNavegacion() {
        _uiState.update { it.copy(navegarARegistroDiario = false) }
    }

    /**
     * Limpiar mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpiar mensaje de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    /**
     * Mostrar error temporal y luego limpiarlo automáticamente
     */
    fun mostrarErrorTemporal(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000) // Mostrar el error por 3 segundos
            limpiarError()
        }
    }

    /**
     * Muestra un mensaje de error en la UI.
     */
    fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
    }

    /**
     * Obtiene los registros diarios para una fecha y clase específicas
     * 
     * @param fecha Fecha en formato string (yyyy-MM-dd)
     * @param claseId ID de la clase
     * @return Lista de registros diarios
     */
    private suspend fun getRegistrosDiariosPorFechaYClase(
        fecha: String,
        claseId: String
    ): List<RegistroActividad> {
        return try {
            // Convertir el string de fecha a objeto Date
            val fechaCalendar = Calendar.getInstance()
            try {
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                fechaCalendar.time = formatoFecha.parse(fecha) ?: Date()
            } catch (e: Exception) {
                // Si hay error en el formato, usar la fecha actual
                Timber.e(e, "Error al parsear fecha: $fecha")
            }
            
            // Obtener registros de la colección registrosActividad 
            val resultActividad = registroDiarioRepository.obtenerRegistrosActividadPorFechaYClase(
                claseId,
                fechaCalendar.time
            )
            
            val registros = mutableListOf<RegistroActividad>()
            
            // Procesar los registros de la colección registrosActividad
            if (resultActividad is Result.Success) {
                // Filtrar los registros no eliminados
                val registrosActividadNoEliminados = resultActividad.data
                    .filter { !it.eliminado }
                
                registros.addAll(registrosActividadNoEliminados)
                
                Timber.d("Registros obtenidos de registrosActividad para clase $claseId en fecha $fecha:")
                Timber.d("- Total registros: ${resultActividad.data.size}")
                Timber.d("- Registros no eliminados: ${registrosActividadNoEliminados.size}")
            } else {
                Timber.e("Error al obtener registros para clase $claseId en fecha $fecha")
            }
            
            registros.forEach { registro ->
                Timber.d("  - Alumno: ${registro.alumnoId}, ID: ${registro.id}, Eliminado: ${registro.eliminado}")
            }
            
            return registros
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por fecha y clase")
            emptyList()
        }
    }

    /**
     * Establece el ID del profesor actual y actualiza la lista de alumnos con registro
     * 
     * @param profesorId ID del profesor
     */
    fun setProfesorId(profesorId: String) {
        _uiState.update { it.copy(profesorId = profesorId) }
        
        viewModelScope.launch {
            try {
                // Obtener la clase asignada al profesor
                val profesorResult = usuarioRepository.getUsuarioById(profesorId)
                if (profesorResult is Result.Success) {
                    // Buscar la clase asignada al profesor en los clasesIds
                    val claseAsignada = if (profesorResult.data.clasesIds.isNotEmpty()) {
                        profesorResult.data.clasesIds.first()
                    } else {
                        // Intentar obtener de otra forma si no tiene clasesIds
                        ""
                    }
                    
                    if (claseAsignada.isNotBlank()) {
                        _uiState.update { it.copy(claseId = claseAsignada) }
                        cargarDatos()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener la clase asignada al profesor")
            }
        }
    }

    /**
     * Verifica y actualiza los registros existentes para la fecha actual
     */
    private fun verificarRegistrosExistentes() {
        viewModelScope.launch {
            try {
                // Obtener usuario actual y clase
                val usuario = authRepository.getCurrentUser() ?: return@launch
                val usuarioProfesor = usuarioRepository.getUsuarioById(usuario.dni)
                if (usuarioProfesor !is Result.Success) return@launch
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.data.dni)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) return@launch
                
                val clase = clasesResult.data.first()
                Timber.d("Verificando registros para clase ${clase.nombre} (${clase.id}) en fecha ${_uiState.value.fechaSeleccionada}")
                
                // Obtener registros para esta fecha desde ambas colecciones
                val registros = getRegistrosDiariosPorFechaYClase(
                    _uiState.value.fechaSeleccionada.toString(),
                    clase.id
                )
                
                // Actualizar alumnos con registro
                val alumnosConRegistro = registros
                    .filter { !it.eliminado } // Filtrar registros no eliminados
                    .map { it.alumnoId }
                    .toSet()
                
                Timber.d("Alumnos con registro actualizados: $alumnosConRegistro")
                
                // Actualizar el estado de asistencia 
                val fechaJava = _uiState.value.fechaSeleccionada.toDate()
                val asistenciaResult = asistenciaRepository.obtenerRegistroAsistencia(clase.id, fechaJava)
                val estadosAsistencia = asistenciaResult?.estadosAsistencia ?: emptyMap()
                
                // Actualizar la presencia de los alumnos basado en los estados de asistencia
                val alumnosActualizados = _uiState.value.alumnos.map { alumno ->
                    val estado = estadosAsistencia[alumno.id]
                    alumno.copy(presente = estado == EstadoAsistencia.PRESENTE)
                }
                
                _uiState.update { it.copy(
                    alumnosConRegistro = alumnosConRegistro,
                    alumnos = alumnosActualizados,
                    alumnosFiltrados = if (it.mostrarSoloPresentes) 
                        alumnosActualizados.filter { a -> a.presente } 
                    else 
                        alumnosActualizados,
                    alumnosPresentes = alumnosActualizados.count { a -> a.presente },
                    isLoading = false
                )}
                
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar registros existentes")
                _uiState.update { it.copy(
                    error = "Error al verificar registros: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    /**
     * Elimina el registro de un alumno para la fecha seleccionada
     */
    fun eliminarRegistro(alumnoId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val fecha = _uiState.value.fechaSeleccionada
                val formattedDate = fecha.format(DateTimeFormatter.BASIC_ISO_DATE) // Formato: YYYYMMDD
                val registroDocId = "registro_${formattedDate}_${alumnoId}"
                
                Timber.d("Intentando eliminar registro: $registroDocId")
                
                val resultado = registroDiarioRepository.eliminarRegistro(registroDocId)
                if (resultado) {
                    // Actualizar la lista de alumnos con registro
                    val alumnosConRegistroActualizado = _uiState.value.alumnosConRegistro.toMutableSet()
                    alumnosConRegistroActualizado.remove(alumnoId)
                    
                    _uiState.update { it.copy(
                        alumnosConRegistro = alumnosConRegistroActualizado,
                        mensajeExito = "Registro eliminado correctamente",
                        isLoading = false
                    ) }
                    
                    // Recargar registros para actualizar la UI y el estado de asistencia
                    verificarRegistrosExistentes()
                    
                    Timber.d("Registro eliminado correctamente: $registroDocId")
                } else {
                    _uiState.update { it.copy(
                        error = "No se pudo eliminar el registro",
                        isLoading = false
                    ) }
                    Timber.e("Error al eliminar registro: $registroDocId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar registro para alumnoId: $alumnoId")
                _uiState.update { it.copy(
                    error = "Error al eliminar registro: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Genera un informe de asistencia para la fecha seleccionada
     */
    fun generarInforme() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val alumnosPresentes = _uiState.value.alumnos.count { it.presente }
                val alumnosAusentes = _uiState.value.alumnos.size - alumnosPresentes
                val porcentajeAsistencia = if (_uiState.value.alumnos.isNotEmpty()) {
                    (alumnosPresentes.toFloat() / _uiState.value.alumnos.size) * 100
                } else {
                    0f
                }
                
                val informe = InformeAsistencia(
                    totalAlumnos = _uiState.value.alumnos.size,
                    alumnosPresentes = alumnosPresentes,
                    alumnosAusentes = alumnosAusentes,
                    porcentajeAsistencia = porcentajeAsistencia,
                    alumnosConRegistro = _uiState.value.alumnosConRegistro.size,
                    alumnosSinRegistro = _uiState.value.alumnos.size - _uiState.value.alumnosConRegistro.size,
                    fecha = _uiState.value.fechaSeleccionada
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        mostrarDialogoInforme = true,
                        datosInforme = informe
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al generar informe de asistencia")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al generar informe: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Genera el texto del informe para compartir
     * @return Texto formateado del informe
     */
    fun generarTextoInforme(): String {
        val informe = _uiState.value.datosInforme
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "ES"))
        val fechaFormateada = informe.fecha.format(formatter)
        val nombreClase = _uiState.value.nombreClase
        
        return """
            INFORME DE ASISTENCIA
            
            Clase: $nombreClase
            Fecha: $fechaFormateada
            
            RESUMEN:
            • Total alumnos: ${informe.totalAlumnos}
            • Alumnos presentes: ${informe.alumnosPresentes}
            • Alumnos ausentes: ${informe.alumnosAusentes}
            • Porcentaje de asistencia: ${String.format("%.1f", informe.porcentajeAsistencia)}%
            
            REGISTROS:
            • Alumnos con registro completado: ${informe.alumnosConRegistro}
            • Alumnos sin registro: ${informe.alumnosSinRegistro}
            
            Informe generado desde la aplicación UmeEgunero.
        """.trimIndent()
    }

    /**
     * Cierra el diálogo de informe
     */
    fun cerrarDialogoInforme() {
        _uiState.update { it.copy(mostrarDialogoInforme = false) }
    }
    
    /**
     * Exporta el informe de asistencia como PDF
     * @param context Contexto de la aplicación necesario para acceder al almacenamiento
     * @return Uri del archivo PDF generado o null si hubo un error
     */
    suspend fun exportarInformeAsistenciaPDF(context: Context): Uri? {
        return try {
            Timber.d("Iniciando exportación de informe de asistencia a PDF")
            
            val informe = _uiState.value.datosInforme
            val formatter = DateTimeFormatter.ofPattern("d_MMMM_yyyy", Locale("es", "ES"))
            val fechaArchivo = informe.fecha.format(formatter)
            val nombreClase = _uiState.value.nombreClase.replace(" ", "_")
            
            // Nombre del archivo
            val nombreArchivo = "Informe_Asistencia_${nombreClase}_${fechaArchivo}.pdf"
            
            // Generar contenido del informe detallado con alumnos
            val contenidoInforme = generarContenidoInformeDetallado()
            
            // Usar PdfExporter para generar el PDF
            val pdfExporter = com.tfg.umeegunero.util.PdfExporter(context)
            val success = pdfExporter.createPdfFromText(
                contenido = contenidoInforme,
                fileName = nombreArchivo,
                title = "INFORME DE ASISTENCIA ESCOLAR"
            )
            
            if (success) {
                // Obtener Uri del archivo generado para compartir
                val downloadsDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    null // En Android 10+ se gestiona por MediaStore
                } else {
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                
                val pdfFile = java.io.File(downloadsDir, nombreArchivo)
                if (pdfFile.exists()) {
                    Timber.d("Archivo PDF generado exitosamente: ${pdfFile.absolutePath}")
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        pdfFile
                    )
                } else {
                    // En Android 10+ el archivo estará en MediaStore
                    // Devolvemos null pero asumimos que se ha guardado correctamente
                    Timber.d("PDF generado en Android 10+ (no se obtiene Uri directa)")
                    null
                }
            } else {
                Timber.e("Error al crear el archivo PDF")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al exportar informe como PDF")
            null
        }
    }
    
    /**
     * Genera el contenido detallado del informe de asistencia incluyendo la lista de alumnos
     * @return Texto formateado del informe con detalles completos
     */
    private fun generarContenidoInformeDetallado(): String {
        val informe = _uiState.value.datosInforme
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val fechaFormateada = informe.fecha.format(formatter)
        val nombreClase = _uiState.value.nombreClase
        val fechaCorta = informe.fecha.format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val alumnosPresentes = _uiState.value.alumnos.filter { it.presente }
        val alumnosAusentes = _uiState.value.alumnos.filter { !it.presente }
        
        // Crear un StringBuilder para ir construyendo el contenido
        val sb = StringBuilder()
        
        // Encabezado
        sb.append("INFORME DE ASISTENCIA ESCOLAR\n\n")
        sb.append("Clase: $nombreClase\n")
        sb.append("Fecha: $fechaFormateada\n")
        sb.append("-".repeat(50)).append("\n\n")
        
        // Resumen
        sb.append("RESUMEN DE ASISTENCIA:\n")
        sb.append("Total alumnos: ${informe.totalAlumnos}\n")
        sb.append("Alumnos presentes: ${informe.alumnosPresentes}\n")
        sb.append("Alumnos ausentes: ${informe.alumnosAusentes}\n")
        sb.append("Porcentaje de asistencia: ${String.format("%.1f", informe.porcentajeAsistencia)}%\n\n")
        
        // Detalle de registros
        sb.append("ESTADO DE REGISTROS:\n")
        sb.append("Alumnos con registro completado: ${informe.alumnosConRegistro}\n")
        sb.append("Alumnos sin registro: ${informe.alumnosSinRegistro}\n\n")
        
        // Lista de alumnos presentes
        sb.append("ALUMNOS PRESENTES ($fechaCorta):\n")
        sb.append("-".repeat(50)).append("\n")
        
        if (alumnosPresentes.isEmpty()) {
            sb.append("No hay alumnos presentes registrados.\n")
        } else {
            sb.append("N° | DNI/ID | NOMBRE COMPLETO\n")
            sb.append("-".repeat(50)).append("\n")
            
            alumnosPresentes.forEachIndexed { index, alumno ->
                val numeroFormateado = String.format("%02d", index + 1)
                val dni = alumno.dni.ifEmpty { alumno.id }
                sb.append("$numeroFormateado | $dni | ${alumno.nombre} ${alumno.apellidos}\n")
            }
        }
        
        sb.append("\n")
        
        // Lista de alumnos ausentes
        sb.append("ALUMNOS AUSENTES:\n")
        sb.append("-".repeat(50)).append("\n")
        
        if (alumnosAusentes.isEmpty()) {
            sb.append("No hay alumnos ausentes registrados.\n")
        } else {
            sb.append("N° | DNI/ID | NOMBRE COMPLETO\n")
            sb.append("-".repeat(50)).append("\n")
            
            alumnosAusentes.forEachIndexed { index, alumno ->
                val numeroFormateado = String.format("%02d", index + 1)
                val dni = alumno.dni.ifEmpty { alumno.id }
                sb.append("$numeroFormateado | $dni | ${alumno.nombre} ${alumno.apellidos}\n")
            }
        }
        
        sb.append("\n")
        sb.append("=".repeat(50)).append("\n\n")
        sb.append("Documento generado por la aplicación UmeEgunero\n")
        sb.append("Fecha de emisión: ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}")
        
        return sb.toString()
    }

    /**
     * Carga las notificaciones de ausencia para la clase actual
     * y configura un listener en tiempo real para actualizaciones
     */
    fun cargarAusenciasNotificadas() {
        viewModelScope.launch {
            try {
                val claseActual = _uiState.value.claseId
                if (claseActual.isNotBlank()) {
                    Timber.d("Configurando listener de ausencias para clase: $claseActual")
                    
                    // Usar un listener en tiempo real en lugar de una consulta única
                    notificacionAusenciaRepository.observarAusenciasPorClase(claseActual)
                        .collect { resultado ->
                            if (resultado is Result.Success) {
                                val ausencias = resultado.data
                                
                                // Filtrar las ausencias aceptadas para la fecha seleccionada
                                val fechaSeleccionada = _uiState.value.fechaSeleccionada
                                val alumnosConAusenciaJustificada = ausencias
                                    .filter { ausencia ->
                                        ausencia.estado == EstadoNotificacionAusencia.ACEPTADA.name &&
                                        ausencia.fechaAusencia.toDate().toInstant()
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toLocalDate() == fechaSeleccionada
                                    }
                                    .map { it.alumnoId }
                                    .toSet()
                                
                                // Verificar si hay nuevas ausencias pendientes comparando con el estado anterior
                                val ausenciasPendientesAnteriores = _uiState.value.ausenciasNotificadas
                                    .filter { it.estado == EstadoNotificacionAusencia.PENDIENTE.name }
                                    .map { it.id }
                                    .toSet()
                                    
                                val ausenciasPendientesNuevas = ausencias
                                    .filter { it.estado == EstadoNotificacionAusencia.PENDIENTE.name }
                                    .map { it.id }
                                    .toSet()
                                
                                // Si hay nuevas ausencias pendientes, mostrar notificación
                                val hayNuevasAusencias = ausenciasPendientesNuevas.minus(ausenciasPendientesAnteriores).isNotEmpty()
                                
                                _uiState.update { it.copy(
                                    ausenciasNotificadas = ausencias,
                                    alumnosConAusenciaJustificada = alumnosConAusenciaJustificada,
                                    hayNuevaNotificacionAusencia = hayNuevasAusencias
                                ) }
                                
                                if (hayNuevasAusencias) {
                                    // Después de 5 segundos, ocultar la notificación
                                    viewModelScope.launch {
                                        delay(5000)
                                        _uiState.update { it.copy(hayNuevaNotificacionAusencia = false) }
                                    }
                                }
                                
                                Timber.d("Ausencias actualizadas en tiempo real: ${ausencias.size}, justificadas para hoy: ${alumnosConAusenciaJustificada.size}")
                            } else if (resultado is Result.Error) {
                                Timber.e(resultado.exception, "Error al observar ausencias para la clase")
                            }
                        }
                } else {
                    Timber.d("No hay claseId definido para cargar ausencias")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al configurar listener de ausencias notificadas")
                // No actualizamos el estado de error para no interrumpir la experiencia
            }
        }
    }

    /**
     * Muestra el diálogo de detalle de ausencia
     */
    fun mostrarDetalleAusencia(ausencia: NotificacionAusencia) {
        _uiState.update { it.copy(
            mostrarDialogoAusencia = true,
            ausenciaSeleccionada = ausencia
        ) }
    }

    /**
     * Cierra el diálogo de detalle de ausencia
     */
    fun cerrarDetalleAusencia() {
        _uiState.update { it.copy(
            mostrarDialogoAusencia = false,
            ausenciaSeleccionada = null
        ) }
    }

    /**
     * Procesa una ausencia notificada (aceptar o rechazar)
     */
    fun procesarAusencia(ausencia: NotificacionAusencia, aceptar: Boolean) {
        viewModelScope.launch {
            try {
                val profesorId = authRepository.getCurrentUserId() ?: return@launch
                
                val estado = if (aceptar) 
                    EstadoNotificacionAusencia.ACEPTADA 
                else 
                    EstadoNotificacionAusencia.RECHAZADA
                
                val resultado = notificacionAusenciaRepository.actualizarEstadoAusencia(
                    notificacionId = ausencia.id,
                    estado = estado,
                    profesorId = profesorId
                )
                
                if (resultado is Result.Success) {
                    // Recargar ausencias
                    cargarAusenciasNotificadas()
                    
                    // Cerrar diálogo
                    cerrarDetalleAusencia()
                    
                    // Mostrar mensaje de éxito
                    _uiState.update { it.copy(
                        mensajeExito = if (aceptar) 
                            "Ausencia aceptada correctamente" 
                        else 
                            "Ausencia rechazada"
                    ) }
                    
                    // Si se aceptó, actualizar el estado de asistencia del alumno
                    if (aceptar) {
                        // Buscar el alumno en la lista
                        val alumno = _uiState.value.alumnos.find { it.id == ausencia.alumnoId }
                        
                        if (alumno != null) {
                            // Actualizar en el repositorio de asistencia
                            viewModelScope.launch {
                                try {
                                    // Obtener el registro de asistencia actual o crear uno nuevo
                                    val registroExistente = asistenciaRepository.obtenerRegistroAsistencia(
                                        _uiState.value.claseId,
                                        java.util.Date.from(_uiState.value.fechaSeleccionada.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant())
                                    )
                                    
                                    val estadosActualizados = if (registroExistente != null) {
                                        // Actualizar el estado existente
                                        registroExistente.estadosAsistencia.toMutableMap().apply {
                                            this[alumno.id] = EstadoAsistencia.AUSENTE_JUSTIFICADO
                                        }
                                    } else {
                                        // Crear nuevo mapa con el estado
                                        mutableMapOf(alumno.id to EstadoAsistencia.AUSENTE_JUSTIFICADO)
                                    }
                                    
                                    // Crear o actualizar el registro
                                    val registroAsistencia = com.tfg.umeegunero.data.model.RegistroAsistencia(
                                        id = registroExistente?.id ?: "",
                                        claseId = _uiState.value.claseId,
                                        profesorId = registroExistente?.profesorId ?: profesorId,
                                        fecha = com.google.firebase.Timestamp(java.util.Date.from(_uiState.value.fechaSeleccionada.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant())),
                                        estadosAsistencia = estadosActualizados,
                                        observaciones = registroExistente?.observaciones ?: ""
                                    )
                                    
                                    val resultado = asistenciaRepository.guardarRegistroAsistencia(registroAsistencia)
                                    
                                    if (resultado) {
                                        Timber.d("Estado de asistencia actualizado a AUSENTE_JUSTIFICADO para alumno ${alumno.id}")
                                    } else {
                                        Timber.e("Error al actualizar estado de asistencia")
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al actualizar estado de asistencia")
                                }
                            }
                            
                            // Marcar como ausente en los datos de UI (para actualizar la vista)
                            val alumnosActualizados = _uiState.value.alumnos.map {
                                if (it.id == alumno.id) it.copy(presente = false) else it
                            }
                            
                            _uiState.update { it.copy(
                                alumnos = alumnosActualizados,
                                alumnosPresentes = alumnosActualizados.count { alumno -> alumno.presente },
                                alumnosConAusenciaJustificada = it.alumnosConAusenciaJustificada + ausencia.alumnoId
                            ) }
                        }
                    }
                } else if (resultado is Result.Error) {
                    _uiState.update { it.copy(
                        error = "Error al procesar la ausencia: ${resultado.exception?.message ?: "Error desconocido"}"
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al procesar ausencia")
                _uiState.update { it.copy(
                    error = "Error al procesar la ausencia: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Actualiza manualmente el estado de lectura del registro diario para un alumno
     * 
     * @param alumnoId ID del alumno
     * @param leido Estado de lectura (true = leído, false = no leído)
     */
    fun actualizarEstadoLecturaAlumno(alumnoId: String, leido: Boolean = true) {
        viewModelScope.launch {
            try {
                Timber.d("Solicitando actualización manual del estado de lectura para alumno: $alumnoId a $leido")
                
                val resultado = registroDiarioRepository.actualizarEstadoLecturaRegistroDiario(alumnoId, leido)
                
                if (resultado is Result.Success) {
                    Timber.d("Estado de lectura actualizado correctamente para alumno: $alumnoId")
                    
                    // Actualizar la lista de alumnos en memoria
                    val alumnosActualizados = _uiState.value.alumnos.map { alumno ->
                        if (alumno.id == alumnoId) {
                            alumno.copy(registroDiarioLeido = leido)
                        } else {
                            alumno
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            alumnos = alumnosActualizados,
                            alumnosFiltrados = alumnosActualizados,
                            mensajeExito = "Estado de lectura actualizado"
                        )
                    }
                } else {
                    Timber.e("Error al actualizar estado de lectura: ${(resultado as Result.Error).message}")
                    _uiState.update { it.copy(error = "Error al actualizar estado de lectura") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar estado de lectura: ${e.message}")
                _uiState.update { it.copy(error = "Error al actualizar estado de lectura: ${e.message}") }
            }
        }
    }
} 