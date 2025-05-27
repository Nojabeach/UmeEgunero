package com.tfg.umeegunero.feature.profesor.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.EstadoAsistencia
import com.tfg.umeegunero.data.model.RegistroDiario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
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
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.TipoUsuario
import java.util.Calendar
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
 * @property profesorId ID del profesor (cuando se recibe desde otra pantalla)
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
    val navegarARegistroDiario: Boolean = false,
    val profesorId: String = ""
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
     * Carga los datos iniciales: profesor, clase y alumnos asociados.
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Obtener usuario actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener el usuario actual",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener datos del profesor (ahora es un objeto Usuario con perfil de PROFESOR)
                val usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni)
                if (usuarioProfesor == null) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener el perfil de profesor y el ID del centro
                val perfilProfesor = usuarioProfesor.perfiles.firstOrNull { it.tipo == TipoUsuario.PROFESOR }
                if (perfilProfesor == null) {
                    _uiState.update { it.copy(
                        error = "El usuario no tiene perfil de profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // El centroId está disponible pero no lo necesitamos aquí
                // val centroId = perfilProfesor.centroId
                
                // Obtener clases asignadas al profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.dni)
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
                Timber.d("ListadoPreRegistroDiarioViewModel (cargarDatosPreRegistro): Alumnos recibidos: ${alumnos.joinToString { it.nombre + " (ID: " + it.id + ", DNI: " + it.dni + ", Presente: " + it.presente + ")" }}")
                
                // Obtener registros existentes para la fecha actual
                val fechaActual = _uiState.value.fechaSeleccionada
                val registros = getRegistrosDiariosPorFechaYClase(
                    fechaActual.toString(),
                    clase.id
                )
                
                // Crear set con IDs de alumnos que ya tienen registro
                val alumnosConRegistro = registros.map { it.alumnoId }.toSet()
                
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
                val usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni) ?: return@launch
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.dni)
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
                
                // Actualizar presencia en alumnos
                val alumnosActualizados = _uiState.value.alumnos.map { alumno ->
                    val estado = estadosAsistencia[alumno.id]
                    alumno.copy(presente = estado == EstadoAsistencia.PRESENTE)
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
                
                val usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni)
                if (usuarioProfesor == null) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener clase
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.dni)
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
                        profesorId = usuarioProfesor.dni,
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
                
                val usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni)
                if (usuarioProfesor == null) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.dni)
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
                    profesorId = usuarioProfesor.dni,
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
     * Obtiene los registros diarios por fecha y clase
     * Método provisional mientras se completa la implementación en el repositorio
     */
    private suspend fun getRegistrosDiariosPorFechaYClase(
        fecha: String, 
        claseId: String
    ): List<RegistroActividad> {
        return try {
            // En lugar de usar una implementación temporal, llamamos directamente al repositorio
            val fechaCalendar = Calendar.getInstance()
            try {
                // Convertir el string de fecha a objeto Date
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                fechaCalendar.time = formatoFecha.parse(fecha) ?: Date()
            } catch (e: Exception) {
                // Si hay error en el formato, usar la fecha actual
                Timber.e(e, "Error al parsear fecha: $fecha")
            }
            
            val result = registroDiarioRepository.obtenerRegistrosDiariosPorFechaYClase(
                fechaCalendar.time,
                claseId
            )
            
            if (result is Result.Success) {
                // Filtrar registros no eliminados
                val registrosNoEliminados = result.data.filter { registro ->
                    !registro.eliminado
                }
                
                Timber.d("Registros obtenidos para clase $claseId en fecha $fecha:")
                Timber.d("- Total registros: ${result.data.size}")
                Timber.d("- Registros no eliminados: ${registrosNoEliminados.size}")
                registrosNoEliminados.forEach { registro ->
                    Timber.d("  - Alumno: ${registro.alumnoId}, ID: ${registro.id}, Eliminado: ${registro.eliminado}")
                }
                
                registrosNoEliminados
            } else {
                Timber.e("Error al obtener registros para clase $claseId en fecha $fecha")
                emptyList()
            }
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
        Timber.d("Estableciendo profesorId: $profesorId")
        _uiState.update { it.copy(profesorId = profesorId) }
        
        // Cargar datos con el nuevo profesorId
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener usuario profesor
                val usuario = usuarioRepository.getUsuarioPorDni(profesorId)
                if (usuario !is Result.Success) {
                    _uiState.update { it.copy(
                        error = "No se encontró información del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                val usuarioProfesor = usuario.data
                
                // Verificar que tiene perfil de profesor
                val perfilProfesor = usuarioProfesor.perfiles.find { perfil -> perfil.tipo == TipoUsuario.PROFESOR }
                if (perfilProfesor == null) {
                    _uiState.update { it.copy(
                        error = "El usuario no tiene perfil de profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(profesorId)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No hay clases asignadas a este profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                val clase = clasesResult.data.first()
                Timber.d("Llamando a getAlumnosByClase en setProfesorId con clase.id: ${clase.id} (nombre: ${clase.nombre})")
                
                val alumnosResult = usuarioRepository.getAlumnosByClase(clase.id)
                val alumnos = if (alumnosResult is Result.Success) alumnosResult.data else emptyList()
                Timber.d("ListadoPreRegistroDiarioViewModel (setProfesorId): Alumnos recibidos: ${alumnos.joinToString { it.nombre + " (ID: " + it.id + ", DNI: " + it.dni + ", Presente: " + it.presente + ")" }}")
                
                // Obtener registros existentes para la fecha actual
                val registrosResult = registroDiarioRepository.obtenerRegistrosDiariosPorFechaYClase(
                    _uiState.value.fechaSeleccionada.toDate(),
                    clase.id
                )
                
                // Actualizar alumnos con registro
                val alumnosConRegistro = if (registrosResult is Result.Success) {
                    registrosResult.data.map { it.alumnoId }.toSet()
                } else {
                    emptySet()
                }
                
                Timber.d("Alumnos con registro para la fecha ${_uiState.value.fechaSeleccionada}: $alumnosConRegistro")
                
                _uiState.update { 
                    it.copy(
                        nombreClase = clase.nombre,
                        alumnos = alumnos,
                        alumnosFiltrados = if (it.mostrarSoloPresentes) alumnos.filter { a -> a.presente } else alumnos,
                        totalAlumnos = alumnos.size,
                        alumnosPresentes = alumnos.count { a -> a.presente },
                        alumnosConRegistro = alumnosConRegistro,
                        alumnosSeleccionados = it.alumnosSeleccionados.filter { alumno -> !alumnosConRegistro.contains(alumno.id) },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al establecer profesor: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error al cargar los datos: ${e.message}",
                    isLoading = false
                ) }
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
                    
                    // Recargar registros para actualizar la UI
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
     * Verifica y actualiza los registros existentes para la fecha actual
     */
    private fun verificarRegistrosExistentes() {
        viewModelScope.launch {
            try {
                // Obtener usuario actual y clase
                val usuario = authRepository.getCurrentUser() ?: return@launch
                val usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni) ?: return@launch
                
                // Obtener clases del profesor
                val clasesResult = claseRepository.getClasesByProfesor(usuarioProfesor.dni)
                if (clasesResult !is Result.Success || clasesResult.data.isEmpty()) return@launch
                
                val clase = clasesResult.data.first()
                
                // Obtener registros para esta fecha
                val registrosResult = registroDiarioRepository.obtenerRegistrosDiariosPorFechaYClase(
                    _uiState.value.fechaSeleccionada.toDate(),
                    clase.id
                )
                
                // Actualizar alumnos con registro
                val alumnosConRegistro = if (registrosResult is Result.Success) {
                    registrosResult.data.map { it.alumnoId }.toSet()
                } else {
                    emptySet()
                }
                
                Timber.d("Alumnos con registro actualizados: $alumnosConRegistro")
                
                _uiState.update { it.copy(
                    alumnosConRegistro = alumnosConRegistro,
                    isLoading = false
                )}
                
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar registros existentes")
                _uiState.update { it.copy(
                    error = "Error al actualizar registros: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }
} 