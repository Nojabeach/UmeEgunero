package com.tfg.umeegunero.feature.profesor.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.FirebaseFunctionsException
import timber.log.Timber
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Comidas
import com.tfg.umeegunero.data.model.Plato
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

// Definir las clases de enumeración que faltan
enum class Alimentacion {
    NORMAL, POCO, NADA, NO_SERVIDO
}

enum class Suenio {
    NORMAL, INQUIETO, NO_DUERME, MUCHO
}

enum class Evacuacion {
    NORMAL, DIARREA, ESTREÑIMIENTO
}

enum class Comportamiento {
    NORMAL, INQUIETO, AGRESIVO, TRISTE, ALEGRE
}

data class RegistroDiarioUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val registro: RegistroActividad = RegistroActividad(),
    
    // UI state para las comidas
    val primerPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val segundoPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val postre: EstadoComida = EstadoComida.NO_SERVIDO,
    val merienda: EstadoComida = EstadoComida.NO_SERVIDO,
    val observacionesComida: String = "",
    
    // UI state para la siesta
    val haSiestaSiNo: Boolean = false,
    val horaInicioSiesta: String = "",
    val horaFinSiesta: String = "",
    val observacionesSiesta: String = "",
    
    // UI state para los materiales
    val necesitaPanales: Boolean = false,
    val necesitaToallitas: Boolean = false,
    val necesitaRopaCambio: Boolean = false,
    val otroMaterialNecesario: String = "",
    
    // UI state para las observaciones
    val observacionesGenerales: String = "",
    val haHechoCaca: Boolean = false,
    val numeroCacas: Int = 0,
    val observacionesCaca: String = "",
    
    // UI de la pantalla
    val alumnoId: String = "",
    val claseId: String = "",
    val alumnoNombre: String? = null,
    val claseNombre: String? = null,
    val fechaSeleccionada: Date = Date(),
    val showSuccessDialog: Boolean = false
)

/**
 * ViewModel para la pantalla de registro diario
 */
@HiltViewModel
class RegistroDiarioViewModel @Inject constructor(
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository,
    private val notificationService: NotificationService,
    private val authRepository: AuthRepository,
    private val profesorRepository: ProfesorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegistroDiarioUiState())
    val uiState: StateFlow<RegistroDiarioUiState> = _uiState.asStateFlow()
    
    /**
     * Carga un registro diario existente o crea uno nuevo si no existe
     */
    fun cargarRegistroDiario(alumnoId: String, claseId: String, profesorId: String, fecha: Date = Date()) {
        _uiState.update { it.copy(isLoading = true, error = null, alumnoId = alumnoId, claseId = claseId, fechaSeleccionada = fecha) }
        Timber.d("RegistroDiarioViewModel: Iniciando cargarRegistroDiario. AlumnoID: '$alumnoId', ClaseID (inicial): '$claseId', ProfesorID: '$profesorId', Fecha: $fecha")
        
        viewModelScope.launch {
            try {
                // Obtener el usuario actual (profesor)
                var profesorIdActual = profesorId
                if (profesorIdActual.isEmpty()) {
                    val usuario = authRepository.getCurrentUser()
                    if (usuario == null) {
                        _uiState.update { it.copy(
                            error = "No se pudo identificar al profesor actual",
                            isLoading = false
                        ) }
                        return@launch
                    }
                    
                    // Intentar obtener el usuario con perfil de profesor desde la colección de usuarios
                    val usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni)
                    if (usuarioProfesor == null) {
                        Timber.e("No se encontró información de profesor para usuario con DNI: ${usuario.dni}")
                        
                        // Intento alternativo: buscar directamente por DNI
                        val usuarioProfesorPorDni = profesorRepository.getUsuarioProfesorByDni(usuario.dni)
                        if (usuarioProfesorPorDni == null) {
                            _uiState.update { it.copy(
                                error = "No se encontró información del profesor. Por favor, contacte al administrador.",
                                isLoading = false
                            ) }
                            return@launch
                        } else {
                            // Usar el DNI como ID del profesor
                            profesorIdActual = usuarioProfesorPorDni.dni
                            Timber.d("Se encontró profesor mediante búsqueda alternativa: ${usuarioProfesorPorDni.dni}")
                        }
                    } else {
                        profesorIdActual = usuarioProfesor.dni
                        Timber.d("Profesor identificado normalmente: $profesorIdActual (${usuarioProfesor.nombre} ${usuarioProfesor.apellidos})")
                    }
                }
                
                if (profesorIdActual.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No se pudo identificar al profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Cargar información del alumno
                val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                if (alumnoResult is Result.Success) {
                    val alumno = alumnoResult.data
                    // Log ANTES de actualizar el nombre del alumno en el estado
                    Timber.d("RegistroDiarioViewModel: Alumno recuperado: ID='${alumno.id}', Nombre='${alumno.nombre}', Apellidos='${alumno.apellidos}', Clase (del objeto Alumno)='${alumno.clase}', ClaseId (del objeto Alumno)='${alumno.claseId}'")
                    _uiState.update { it.copy(alumnoNombre = "${alumno.nombre} ${alumno.apellidos}") }
                    Timber.d("RegistroDiarioViewModel: AlumnoNombre actualizado en UIState: ${uiState.value.alumnoNombre}")
                } else {
                    Timber.e("RegistroDiarioViewModel: No se pudo cargar la información del alumno: $alumnoId. Error: ${(alumnoResult as? Result.Error)?.exception?.message ?: "Desconocido"}")
                    _uiState.update { it.copy(alumnoNombre = "?") } // Indicar que el nombre no se pudo cargar
                }
                
                // Determinar la clase (usar la proporcionada o buscar la del alumno)
                var claseIdAUsar = claseId
                if (claseIdAUsar.isEmpty() && alumnoResult is Result.Success) {
                    // Intentar obtener la clase del alumno
                    claseIdAUsar = alumnoResult.data.claseId
                    Timber.d("Usando claseId del alumno (campo claseId): $claseIdAUsar")
                }
                
                // Si aún no tenemos clase, intentar obtenerla del profesor
                if (claseIdAUsar.isEmpty()) {
                    // Aquí podríamos intentar obtener la clase del profesor, 
                    // pero no es esencial para la funcionalidad principal
                    Timber.d("No se pudo determinar la clase del profesor")
                }
                
                // Cargar información de la clase
                if (claseIdAUsar.isNotEmpty()) {
                    val claseResult = claseRepository.getClaseById(claseIdAUsar)
                    if (claseResult is Result.Success) {
                        val clase = claseResult.data
                        // Log ANTES de actualizar el nombre de la clase en el estado
                        Timber.d("RegistroDiarioViewModel: Clase recuperada: ID='${clase.id}', Nombre='${clase.nombre}'")
                        _uiState.update { it.copy(claseNombre = clase.nombre, claseId = claseIdAUsar) }
                        Timber.d("RegistroDiarioViewModel: ClaseNombre actualizado en UIState: ${uiState.value.claseNombre}")
                    } else {
                        Timber.e("RegistroDiarioViewModel: No se pudo cargar la información de la clase: $claseIdAUsar. Error: ${(claseResult as? Result.Error)?.exception?.message ?: "Desconocido"}")
                        _uiState.update { it.copy(claseNombre = "?") } // Indicar que el nombre de clase no se pudo cargar
                    }
                } else {
                    // Este bloque se añadió antes, si claseIdAUsar sigue vacío aquí, es un problema
                    Timber.w("RegistroDiarioViewModel: claseIdAUsar está vacío después de intentar obtenerla del alumno. No se cargará el nombre de la clase.")
                    _uiState.update { it.copy(claseNombre = "? (No encontrada)") }
                }
                
                // Obtener o crear el registro diario
                val fechaFormateada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)
                Timber.d("Buscando registro para alumno: $alumnoId, fecha: $fechaFormateada")
                
                val result = registroDiarioRepository.obtenerRegistroDiarioExistente(
                    alumnoId = alumnoId,
                    claseId = _uiState.value.claseId,
                    profesorId = profesorIdActual,
                    fecha = fecha
                )
                
                when (result) {
                    is Result.Success -> {
                        val registro = result.data
                        if (registro != null) {
                            // Si existe un registro, cargarlo
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    registro = registro,
                                    
                                    // Actualizar los estados de UI con los datos del registro
                                    primerPlato = registro.comidas.primerPlato.estadoComida,
                                    segundoPlato = registro.comidas.segundoPlato.estadoComida,
                                    postre = registro.comidas.postre.estadoComida,
                                    merienda = EstadoComida.NO_SERVIDO,
                                    observacionesComida = registro.observacionesComida ?: "",
                                    
                                    haSiestaSiNo = registro.haSiestaSiNo,
                                    horaInicioSiesta = registro.horaInicioSiesta ?: "",
                                    horaFinSiesta = registro.horaFinSiesta ?: "",
                                    observacionesSiesta = registro.observacionesSiesta ?: "",
                                    
                                    necesitaPanales = registro.necesitaPanales,
                                    necesitaToallitas = registro.necesitaToallitas,
                                    necesitaRopaCambio = registro.necesitaRopaCambio,
                                    otroMaterialNecesario = registro.otroMaterialNecesario ?: "",
                                    
                                    observacionesGenerales = registro.observacionesGenerales ?: "",
                                    haHechoCaca = registro.haHechoCaca,
                                    numeroCacas = registro.numeroCacas ?: 0,
                                    observacionesCaca = registro.observacionesCaca ?: ""
                                )
                            }
                            Timber.d("Registro existente cargado correctamente")
                        } else {
                            // Si no existe registro, solo actualizar el estado sin crear uno
                            _uiState.update { it.copy(
                                isLoading = false,
                                registro = RegistroActividad() // Registro vacío para el formulario
                            ) }
                            Timber.d("No existe registro previo para esta fecha")
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener registro diario")
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = result.exception?.message ?: "Error al cargar registro diario"
                        ) }
                    }
                    is Result.Loading -> {
                        // No hacer nada, ya estamos mostrando el indicador de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error en cargarRegistroDiario: ${e.message}")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Actualiza el estado de la comida
     */
    fun actualizarEstadoComida(tipo: String, estado: EstadoComida) {
        _uiState.update { state ->
            when (tipo) {
                "primerPlato" -> state.copy(primerPlato = estado)
                "segundoPlato" -> state.copy(segundoPlato = estado)
                "postre" -> state.copy(postre = estado)
                "merienda" -> state.copy(merienda = estado)
                else -> state
            }
        }
    }
    
    /**
     * Actualiza el campo de observaciones de comida
     */
    fun actualizarObservacionesComida(texto: String) {
        _uiState.update { it.copy(observacionesComida = texto) }
    }
    
    /**
     * Actualiza el estado de la siesta
     */
    fun toggleSiesta(haceSiesta: Boolean) {
        _uiState.update { it.copy(haSiestaSiNo = haceSiesta) }
    }
    
    /**
     * Establece la hora de inicio de la siesta
     */
    fun establecerHoraInicioSiesta(hora: String) {
        _uiState.update { it.copy(horaInicioSiesta = hora) }
    }
    
    /**
     * Establece la hora de fin de la siesta
     */
    fun establecerHoraFinSiesta(hora: String) {
        _uiState.update { it.copy(horaFinSiesta = hora) }
    }
    
    /**
     * Actualiza las observaciones de la siesta
     */
    fun actualizarObservacionesSiesta(texto: String) {
        _uiState.update { it.copy(observacionesSiesta = texto) }
    }
    
    /**
     * Actualiza si el alumno ha hecho caca
     */
    fun actualizarHaHechoCaca(value: Boolean) {
        _uiState.update { it.copy(
            haHechoCaca = value,
            // Si el alumno ha hecho caca y el número de cacas es 0, establecer 1 por defecto
            numeroCacas = if (value && it.numeroCacas <= 0) 1 else it.numeroCacas
        ) }
    }
    
    /**
     * Incrementa el contador de cacas
     */
    fun incrementarCacas() {
        _uiState.update { it.copy(numeroCacas = it.numeroCacas + 1) }
    }
    
    /**
     * Decrementa el contador de cacas
     */
    fun decrementarCacas() {
        _uiState.update { it.copy(numeroCacas = maxOf(0, it.numeroCacas - 1)) }
    }
    
    /**
     * Actualiza las observaciones de las deposiciones
     */
    fun actualizarObservacionesCaca(texto: String) {
        _uiState.update { it.copy(observacionesCaca = texto) }
    }
    
    /**
     * Actualiza el estado de los materiales necesarios
     */
    fun toggleMaterial(tipo: String, necesita: Boolean) {
        _uiState.update { state ->
            when (tipo) {
                "panales" -> state.copy(necesitaPanales = necesita)
                "toallitas" -> state.copy(necesitaToallitas = necesita)
                "ropa" -> state.copy(necesitaRopaCambio = necesita)
                else -> state
            }
        }
    }
    
    /**
     * Actualiza el campo de otros materiales necesarios
     */
    fun actualizarOtroMaterial(texto: String) {
        _uiState.update { it.copy(otroMaterialNecesario = texto) }
    }
    
    /**
     * Actualiza las observaciones generales
     */
    fun actualizarObservacionesGenerales(texto: String) {
        _uiState.update { it.copy(observacionesGenerales = texto) }
    }
    
    /**
     * Obtiene el ID del usuario actual (profesor)
     * @return ID del usuario o null si no se pudo obtener
     */
    suspend fun obtenerIdUsuarioActual(): String? {
        return try {
            val usuario = authRepository.getCurrentUser()
            if (usuario != null) {
                val profesorId = usuario.dni
                Timber.d("ID de usuario actual obtenido: $profesorId")
                profesorId
            } else {
                Timber.e("No se pudo obtener el usuario actual")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener ID del usuario actual")
            null
        }
    }
    
    /**
     * Versión asíncrona con callback para obtenerIdUsuarioActual (para mantener compatibilidad)
     */
    fun obtenerIdUsuarioActual(callback: (String?) -> Unit) {
        viewModelScope.launch {
            val resultado = obtenerIdUsuarioActual()
            callback(resultado)
        }
    }
    
    /**
     * Guarda el registro diario actual en Firestore
     */
    fun guardarRegistro(profesorId: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                var profesorIdActual = profesorId
                
                // Si no se provee un profesorId, intentar obtenerlo del usuario actual
                if (profesorIdActual.isEmpty()) {
                    val profesorResult = obtenerIdUsuarioActual()
                    if (profesorResult == null) {
                        _uiState.update { it.copy(
                            error = "No se pudo identificar al profesor actual",
                            isLoading = false
                        ) }
                        return@launch
                    }
                    profesorIdActual = profesorResult
                }
                
                val alumnoId = _uiState.value.alumnoId
                if (alumnoId.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No se ha seleccionado ningún alumno",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Aquí obtenemos la fecha actual
                val fecha = Timestamp(Date())
                
                // Generar el ID calculado para el registro
                val fechaStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(_uiState.value.fechaSeleccionada)
                val registroId = "registro_${fechaStr}_$alumnoId"
                
                // Obtener el nombre del alumno para incluirlo en el registro
                val nombreAlumno = _uiState.value.alumnoNombre ?: ""
                
                // Obtener el nombre del profesor
                var nombreProfesor = ""
                try {
                    val profesorData = profesorRepository.getUsuarioProfesorByDni(profesorIdActual)
                    if (profesorData != null) {
                        nombreProfesor = "${profesorData.nombre} ${profesorData.apellidos}".trim()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener nombre del profesor")
                }
                
                // Para evitar problemas con registros duplicados, verificamos si ya existe
                val registroExistente = _uiState.value.registro
                val registro = if (registroExistente.id.isEmpty() || !registroExistente.id.startsWith("registro_")) {
                    // Crear un nuevo registro con el formato correcto de ID
                    RegistroActividad(
                        id = registroId,
                        alumnoId = alumnoId,
                        alumnoNombre = nombreAlumno,
                        claseId = _uiState.value.claseId,
                        profesorId = profesorIdActual,
                        profesorNombre = nombreProfesor,
                        fecha = Timestamp(_uiState.value.fechaSeleccionada),
                        comidas = Comidas(
                            primerPlato = Plato("", _uiState.value.primerPlato),
                            segundoPlato = Plato("", _uiState.value.segundoPlato),
                            postre = Plato("", _uiState.value.postre)
                        ),
                        observacionesComida = _uiState.value.observacionesComida,
                        haSiestaSiNo = _uiState.value.haSiestaSiNo,
                        horaInicioSiesta = _uiState.value.horaInicioSiesta,
                        horaFinSiesta = _uiState.value.horaFinSiesta,
                        observacionesSiesta = _uiState.value.observacionesSiesta,
                        haHechoCaca = _uiState.value.haHechoCaca,
                        numeroCacas = _uiState.value.numeroCacas,
                        observacionesCaca = _uiState.value.observacionesCaca,
                        necesitaPanales = _uiState.value.necesitaPanales,
                        necesitaToallitas = _uiState.value.necesitaToallitas,
                        necesitaRopaCambio = _uiState.value.necesitaRopaCambio,
                        otroMaterialNecesario = _uiState.value.otroMaterialNecesario,
                        observacionesGenerales = _uiState.value.observacionesGenerales,
                        creadoPor = profesorIdActual,
                        modificadoPor = profesorIdActual,
                        ultimaModificacion = fecha
                    )
                } else {
                    // Actualizar registro existente manteniendo su ID
                    registroExistente.copy(
                        alumnoNombre = nombreAlumno, // Asegurar que el nombre del alumno esté actualizado
                        profesorNombre = nombreProfesor, // Asegurar que el nombre del profesor esté actualizado
                        comidas = Comidas(
                            primerPlato = Plato("", _uiState.value.primerPlato),
                            segundoPlato = Plato("", _uiState.value.segundoPlato),
                            postre = Plato("", _uiState.value.postre)
                        ),
                        observacionesComida = _uiState.value.observacionesComida,
                        haSiestaSiNo = _uiState.value.haSiestaSiNo,
                        horaInicioSiesta = _uiState.value.horaInicioSiesta,
                        horaFinSiesta = _uiState.value.horaFinSiesta,
                        observacionesSiesta = _uiState.value.observacionesSiesta,
                        haHechoCaca = _uiState.value.haHechoCaca,
                        numeroCacas = _uiState.value.numeroCacas,
                        observacionesCaca = _uiState.value.observacionesCaca,
                        necesitaPanales = _uiState.value.necesitaPanales,
                        necesitaToallitas = _uiState.value.necesitaToallitas,
                        necesitaRopaCambio = _uiState.value.necesitaRopaCambio,
                        otroMaterialNecesario = _uiState.value.otroMaterialNecesario,
                        observacionesGenerales = _uiState.value.observacionesGenerales,
                        modificadoPor = profesorIdActual,
                        ultimaModificacion = fecha
                    )
                }
                
                // Guardar el registro en Firestore
                val resultado = registroDiarioRepository.guardarRegistroDiario(registro)
                
                when (resultado) {
                    is Result.Success -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            registro = registro,
                            showSuccessDialog = true
                        ) }
                        
                        // Enviar notificación al familiar
                        viewModelScope.launch {
                            try {
                                val notificacionEnviada = registroDiarioRepository.notificarFamiliarSobreRegistro(registro.id)
                                if (notificacionEnviada) {
                                    Timber.d("Notificación enviada al familiar sobre el registro ${registro.id}")
                                } else {
                                    Timber.w("No se pudo enviar notificación al familiar sobre el registro ${registro.id}")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al enviar notificación al familiar")
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al guardar el registro: ${resultado.exception?.message ?: "Error desconocido"}"
                            )
                        }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar registro diario: ${e.message}")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al guardar el registro: ${e.message ?: "Error desconocido"}"
                ) }
            }
        }
    }
    
    /**
     * Convierte una hora en formato "HH:mm" a un Timestamp
     */
    private fun convertirHoraATimestamp(hora: String): Timestamp {
        try {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val parsedDate = formato.parse(hora)
            return if (parsedDate != null) {
                Timestamp(parsedDate)
            } else {
                Timestamp.now()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al convertir hora a Timestamp: $hora")
            return Timestamp.now()
        }
    }
    
    /**
     * Cierra el diálogo de éxito
     */
    fun ocultarDialogoExito() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }
    
    /**
     * Limpia el error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 