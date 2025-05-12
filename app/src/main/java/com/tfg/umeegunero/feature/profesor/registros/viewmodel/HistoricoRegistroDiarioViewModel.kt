package com.tfg.umeegunero.feature.profesor.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.Comidas
import com.google.firebase.Timestamp

/**
 * Estado de UI para la pantalla de histórico de registros diarios
 */
data class HistoricoRegistroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profesorId: String = "",
    val clases: List<Clase> = emptyList(),
    val claseSeleccionada: Clase? = null,
    val alumnos: List<Alumno> = emptyList(),
    val alumnoSeleccionado: Alumno? = null,
    val registros: List<RegistroActividad> = emptyList(),
    val fechaSeleccionada: Date = Date(),
    val mensajeExito: String? = null
)

/**
 * ViewModel para la pantalla de histórico de registros diarios
 */
@HiltViewModel
class HistoricoRegistroDiarioViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profesorRepository: ProfesorRepository,
    private val claseRepository: ClaseRepository,
    private val alumnoRepository: AlumnoRepository,
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoRegistroUiState())
    val uiState: StateFlow<HistoricoRegistroUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }
    
    /**
     * Carga los datos iniciales del profesor: sus clases
     */
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener usuario actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    Timber.e("No se pudo obtener el usuario actual")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se pudo identificar al usuario actual"
                        )
                    }
                    return@launch
                }
                
                Timber.d("Usuario autenticado: ${usuario.dni}, nombre: ${usuario.nombre}")
                
                // Buscar el profesor - intentamos varias estrategias
                var usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni)
                if (usuarioProfesor == null) {
                    Timber.d("No se encontró profesor por usuarioId, intentando buscar por DNI")
                    usuarioProfesor = profesorRepository.getUsuarioProfesorByDni(usuario.dni)
                }
                
                if (usuarioProfesor == null) {
                    Timber.e("No se pudo encontrar información del profesor para usuario: ${usuario.dni}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se encontró información del profesor"
                        )
                    }
                    return@launch
                }
                
                // Verificar que el usuario tiene perfil de PROFESOR
                val perfilProfesor = usuarioProfesor.perfiles.firstOrNull { it.tipo == TipoUsuario.PROFESOR }
                if (perfilProfesor == null) {
                    Timber.e("El usuario no tiene perfil de PROFESOR")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "El usuario no tiene perfil de profesor"
                        )
                    }
                    return@launch
                }
                
                // Usar el DNI como profesorId
                val profesorId = usuarioProfesor.dni
                Timber.d("Profesor identificado: $profesorId, nombre: ${usuarioProfesor.nombre}")
                
                // Cargar clases del profesor
                var clasesResult = claseRepository.getClasesByProfesor(profesorId)
                
                // Si no se obtuvieron clases, intentar con el ID de usuario directamente
                if (clasesResult is Result.Success && clasesResult.data.isEmpty()) {
                    Timber.d("No se encontraron clases con profesorId: $profesorId, intentando con usuarioId: ${usuario.dni}")
                    clasesResult = claseRepository.getClasesByProfesor(usuario.dni)
                }
                
                if (clasesResult is Result.Success && clasesResult.data.isNotEmpty()) {
                    val clases = clasesResult.data
                    val claseSeleccionada = clases.firstOrNull()
                    
                    Timber.d("Se encontraron ${clases.size} clases para el profesor")
                    clases.forEach { clase ->
                        Timber.d("Clase: ${clase.id} - ${clase.nombre}")
                    }
                    
                    _uiState.update { 
                        it.copy(
                            clases = clases,
                            claseSeleccionada = claseSeleccionada,
                            profesorId = profesorId,
                            isLoading = false
                        )
                    }
                    
                    // Si hay una clase seleccionada, cargar sus alumnos
                    claseSeleccionada?.let { clase ->
                        Timber.d("Cargando alumnos para la clase: ${clase.id} - ${clase.nombre}")
                        cargarAlumnosPorClase(clase.id)
                    }
                } else {
                    Timber.e("No se encontraron clases para el profesor: $profesorId")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se encontraron clases asignadas a este profesor"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos iniciales: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Carga los alumnos de una clase
     */
    private fun cargarAlumnosPorClase(claseId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, alumnos = emptyList(), alumnoSeleccionado = null) }
                
                val alumnosResult = usuarioRepository.getAlumnosByClase(claseId)
                
                if (alumnosResult is Result.Success && alumnosResult.data.isNotEmpty()) {
                    val alumnos = alumnosResult.data
                    val alumnoSeleccionado = alumnos.firstOrNull()
                    
                    _uiState.update { 
                        it.copy(
                            alumnos = alumnos,
                            alumnoSeleccionado = alumnoSeleccionado,
                            isLoading = false
                        )
                    }
                    
                    // Si hay un alumno seleccionado, cargar sus registros
                    alumnoSeleccionado?.let { alumno ->
                        cargarRegistrosAlumno(alumno.id)
                    }
                } else {
                    Timber.d("No se encontraron alumnos para la clase: $claseId")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            alumnos = emptyList(),
                            registros = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar alumnos por clase: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar alumnos: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Carga los registros de un alumno
     */
    fun cargarRegistrosAlumno(alumnoId: String, limite: Long = 30) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, registros = emptyList()) }
                
                Timber.d("Cargando registros para alumno ID: $alumnoId")
                
                registroDiarioRepository.obtenerRegistrosAlumno(alumnoId)
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                // Procesamos los registros para asegurar la consistencia de datos
                                val registros = result.data.map { registro ->
                                    // Verificar si el ID tiene formato temporal y corregirlo si es necesario
                                    val registroProcesado = if (registro.id.startsWith("local_")) {
                                        val idCorregido = generarIdConsistente(registro)
                                        registro.copy(id = idCorregido)
                                    } else {
                                        registro
                                    }
                                    
                                    // Procesar el registro para garantizar la consistencia de los datos
                                    procesarRegistroParaHistorico(registroProcesado)
                                }.sortedByDescending { it.fecha }
                                
                                _uiState.update { 
                                    it.copy(
                                        registros = registros,
                                        isLoading = false
                                    )
                                }
                                
                                if (registros.isEmpty()) {
                                    Timber.d("No se encontraron registros para el alumno $alumnoId")
                                } else {
                                    Timber.d("Registros cargados: ${registros.size}")
                                    registros.forEach { reg ->
                                        Timber.d("Registro: fecha=${reg.fecha}, comidas=${reg.comidas}, alumnoId=${reg.alumnoId}")
                                    }
                                }
                            }
                            is Result.Error -> {
                                Timber.e(result.exception, "Error al obtener registros")
                                _uiState.update { 
                                    it.copy(
                                        error = "Error al cargar registros: ${result.exception?.message ?: "Error desconocido"}",
                                        isLoading = false
                                    )
                                }
                            }
                            is Result.Loading -> {
                                // Este estado se maneja durante la inicialización
                                _uiState.update { it.copy(isLoading = true) }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros del alumno: ${e.message}")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar registros: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Genera un ID consistente para un registro basado en la fecha y el ID del alumno
     */
    private fun generarIdConsistente(registro: RegistroActividad): String {
        val fechaStr = if (registro.fecha != null) {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            sdf.format(registro.fecha)
        } else {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            sdf.format(Date())
        }
        
        return "registro_${fechaStr}_${registro.alumnoId}"
    }
    
    /**
     * Procesa un registro para asegurar la consistencia de los datos en el histórico
     * 
     * @param registro Registro a procesar
     * @return Registro procesado
     */
    private fun procesarRegistroParaHistorico(registro: RegistroActividad): RegistroActividad {
        try {
            Timber.d("Procesando registro: ID=${registro.id}, AlumnoID=${registro.alumnoId}, Fecha=${registro.fecha}")
            
            // Verificar y procesar el objeto comidas
            val comidas = registro.comidas ?: Comidas()
            
            // Verificar cada campo del objeto comidas y convertir estados si es necesario
            val primerPlato = comidas.primerPlato.copy(
                estadoComida = convertirStringAEstadoComida(comidas.primerPlato.estadoComida.toString())
            )
            
            val segundoPlato = comidas.segundoPlato.copy(
                estadoComida = convertirStringAEstadoComida(comidas.segundoPlato.estadoComida.toString())
            )
            
            val postre = comidas.postre.copy(
                estadoComida = convertirStringAEstadoComida(comidas.postre.estadoComida.toString())
            )
            
            val comidasProcesadas = comidas.copy(
                primerPlato = primerPlato,
                segundoPlato = segundoPlato,
                postre = postre
            )
            
            // Asegurar que tengamos horaInicio y horaFin para siesta en formato correcto
            val horaInicioSiesta = formatearHora(registro.horaInicioSiesta)
            val horaFinSiesta = formatearHora(registro.horaFinSiesta)
            
            Timber.d("Comidas procesadas: primer=${primerPlato.estadoComida}, segundo=${segundoPlato.estadoComida}, postre=${postre.estadoComida}")
            
            return registro.copy(
                comidas = comidasProcesadas,
                horaInicioSiesta = horaInicioSiesta,
                horaFinSiesta = horaFinSiesta
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al procesar registro para histórico: ${e.message}")
            return registro
        }
    }
    
    /**
     * Convierte una cadena de texto a un valor del enum EstadoComida
     */
    private fun convertirStringAEstadoComida(estadoStr: String): EstadoComida {
        return when (estadoStr.uppercase()) {
            "COMPLETO" -> EstadoComida.COMPLETO
            "PARCIAL" -> EstadoComida.PARCIAL
            "RECHAZADO" -> EstadoComida.RECHAZADO
            "NO_SERVIDO" -> EstadoComida.NO_SERVIDO
            "SIN_DATOS" -> EstadoComida.SIN_DATOS
            "NO_APLICABLE" -> EstadoComida.NO_APLICABLE
            else -> {
                // También manejar casos de strings que podrían venir de la base de datos
                when (estadoStr.uppercase()) {
                    "BIEN", "BUENO", "GOOD" -> EstadoComida.COMPLETO
                    "REGULAR", "MEDIO" -> EstadoComida.PARCIAL
                    "MAL", "NADA" -> EstadoComida.RECHAZADO
                    else -> EstadoComida.NO_SERVIDO
                }
            }
        }
    }
    
    /**
     * Formatea una hora en formato HH:mm a partir de un Timestamp o string
     */
    private fun formatearHora(horaStr: String): String {
        return try {
            if (horaStr.isEmpty()) return ""
            
            // Si ya tiene formato HH:mm, devolverlo directamente
            if (horaStr.matches(Regex("\\d{1,2}:\\d{2}"))) {
                return horaStr
            }
            
            val partes = horaStr.split(":")
            return if (partes.size >= 2) {
                String.format("%02d:%02d", partes[0].toInt(), partes[1].toInt())
            } else {
                ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al formatear hora: $horaStr")
            ""
        }
    }
    
    /**
     * Selecciona una clase
     */
    fun seleccionarClase(claseId: String) {
        val clase = _uiState.value.clases.find { it.id == claseId }
        if (clase != null) {
            _uiState.update { it.copy(claseSeleccionada = clase) }
            cargarAlumnosPorClase(claseId)
        }
    }
    
    /**
     * Selecciona un alumno
     */
    fun seleccionarAlumno(alumnoId: String) {
        val alumno = _uiState.value.alumnos.find { it.id == alumnoId }
        if (alumno != null) {
            _uiState.update { it.copy(alumnoSeleccionado = alumno) }
            cargarRegistrosAlumno(alumnoId)
        }
    }
    
    /**
     * Selecciona una fecha para ver registros específicos
     */
    fun seleccionarFecha(fecha: Date) {
        _uiState.update { it.copy(fechaSeleccionada = fecha) }
        
        // Recargar los registros del alumno actual con la nueva fecha
        _uiState.value.alumnoSeleccionado?.let { alumno ->
            cargarRegistrosPorFecha(alumno.id, fecha)
        }
    }
    
    /**
     * Carga los registros por fecha específica
     */
    private fun cargarRegistrosPorFecha(alumnoId: String, fecha: Date) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, registros = emptyList()) }
                
                // Obtener los límites del día
                val inicio = Calendar.getInstance().apply {
                    time = fecha
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val fin = Calendar.getInstance().apply {
                    time = fecha
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                Timber.d("Buscando registros para alumno $alumnoId entre $inicio y $fin")
                
                // Usar el repositorio con Timestamp para consultas más precisas
                val inicioTimestamp = Timestamp(inicio)
                val finTimestamp = Timestamp(fin)
                
                val result = registroDiarioRepository.obtenerRegistrosPorFechaYAlumno(alumnoId, inicioTimestamp, finTimestamp)
                
                when (result) {
                    is Result.Success -> {
                        val registrosCrudos = result.data
                        
                        // Procesar los registros para asegurar la consistencia
                        val registros = registrosCrudos.map { procesarRegistroParaHistorico(it) }
                            .sortedByDescending { it.fecha }
                        
                        _uiState.update { 
                            it.copy(
                                registros = registros,
                                isLoading = false
                            )
                        }
                        
                        if (registros.isEmpty()) {
                            Timber.d("No se encontraron registros para la fecha seleccionada")
                        } else {
                            Timber.d("Se encontraron ${registros.size} registros para la fecha")
                            registros.forEach { reg -> 
                                Timber.d("Registro: fecha=${reg.fecha}, comidas=${reg.comidas}")
                            }
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener registros por fecha: ${result.exception?.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar registros: ${result.exception?.message ?: "Error desconocido"}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada, ya estamos mostrando la carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros por fecha: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar registros: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Refrescar todos los datos
     */
    fun refrescarDatos() {
        cargarDatosIniciales()
    }
    
    /**
     * Limpiar mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Método para convertir un registro diario a un formato unificado
     * 
     * @param registro Registro diario a normalizar
     * @return Registro diario con formato normalizado
     */
    private fun normalizarRegistro(registro: RegistroActividad): RegistroActividad {
        // Valores por defecto para campos que podrían estar nulos
        val primerPlato = registro.comidas.primerPlato.estadoComida
        val segundoPlato = registro.comidas.segundoPlato.estadoComida
        val postre = registro.comidas.postre.estadoComida
        
        // Componer observaciones para evitar duplicidad
        val obsComida = registro.observacionesComida.ifEmpty { "" }
        
        // Unificar las observaciones
        val obsCaca = registro.observacionesCaca
        val obsGenerales = registro.observacionesGenerales
        
        return registro.copy(
            observacionesComida = obsComida,
            observacionesCaca = obsCaca,
            observacionesGenerales = obsGenerales
        )
    }
    
    /**
     * Convierte un nivel de consumo a un estado de comida correspondiente
     */
    private fun convertirNivelConsumoAEstadoComida(nivelConsumo: String): EstadoComida {
        return when (nivelConsumo.uppercase()) {
            "BIEN", "COMPLETO" -> EstadoComida.COMPLETO
            "REGULAR", "PARCIAL" -> EstadoComida.PARCIAL
            "MAL", "NADA", "RECHAZADO" -> EstadoComida.RECHAZADO
            else -> EstadoComida.NO_SERVIDO
        }
    }

    /**
     * Carga los registros para la fecha seleccionada
     */
    private fun cargarRegistros() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                // Solo cargar si tenemos un alumno seleccionado
                val alumnoId = _uiState.value.alumnoSeleccionado?.id ?: return@launch
                val fecha = _uiState.value.fechaSeleccionada
                
                // Convertir fecha a formato adecuado para la consulta
                val calendar = Calendar.getInstance()
                calendar.time = fecha
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val fechaInicio = calendar.time
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val fechaFin = calendar.time
                
                val result = obtenerRegistrosPorAlumnoYFecha(alumnoId, fechaInicio, fechaFin)
                
                if (result is Result.Success<List<RegistroActividad>>) {
                    val registrosNormalizados = result.data.map { normalizarRegistro(it) }
                    _uiState.update { it.copy(
                        registros = registrosNormalizados,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(
                        error = "Error al cargar los registros: ${(result as? Result.Error)?.exception?.message ?: "Desconocido"}",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al cargar registros: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Obtiene registros por alumno y fecha
     */
    private suspend fun obtenerRegistrosPorAlumnoYFecha(
        alumnoId: String, 
        fechaInicio: Date, 
        fechaFin: Date
    ): Result<List<RegistroActividad>> {
        return try {
            val startTimestamp = Timestamp(fechaInicio)
            val endTimestamp = Timestamp(fechaFin)
            
            val result = registroDiarioRepository.obtenerRegistrosPorFechaYAlumno(alumnoId, startTimestamp, endTimestamp)
            if (result is Result.Success) {
                Timber.d("Registros obtenidos para alumno $alumnoId: ${result.data.size}")
                Result.Success(result.data)
            } else {
                Timber.e("Error al obtener registros para alumno $alumnoId: ${(result as? Result.Error)?.exception?.message}")
                Result.Error((result as? Result.Error)?.exception ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por alumno y fecha")
            Result.Error(e)
        }
    }

    /**
     * Obtiene un resumen textual del estado de las comidas
     */
    private fun obtenerResumenComidas(registro: RegistroActividad): String {
        val comidas = mutableListOf<String>()
        
        // Verificar si el campo comidas está inicializado
        if (registro.comidas != null) {
            if (registro.comidas.primerPlato.estadoComida != EstadoComida.NO_SERVIDO) {
                comidas.add("Primer plato: ${obtenerTextoEstadoComida(registro.comidas.primerPlato.estadoComida)}")
            }
            
            if (registro.comidas.segundoPlato.estadoComida != EstadoComida.NO_SERVIDO) {
                comidas.add("Segundo plato: ${obtenerTextoEstadoComida(registro.comidas.segundoPlato.estadoComida)}")
            }
            
            if (registro.comidas.postre.estadoComida != EstadoComida.NO_SERVIDO) {
                comidas.add("Postre: ${obtenerTextoEstadoComida(registro.comidas.postre.estadoComida)}")
            }
        }
        
        return if (comidas.isEmpty()) {
            "No se ha servido ninguna comida"
        } else {
            comidas.joinToString(", ")
        }
    }

    /**
     * Convierte el estado de comida a texto legible
     */
    private fun obtenerTextoEstadoComida(estado: EstadoComida): String {
        return when (estado) {
            EstadoComida.COMPLETO -> "Completo"
            EstadoComida.PARCIAL -> "Parcial"
            EstadoComida.RECHAZADO -> "Rechazado"
            EstadoComida.NO_SERVIDO -> "No servido"
            EstadoComida.SIN_DATOS -> "Sin datos"
            EstadoComida.NO_APLICABLE -> "No aplicable"
        }
    }

    /**
     * Limpiar mensaje de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }
} 