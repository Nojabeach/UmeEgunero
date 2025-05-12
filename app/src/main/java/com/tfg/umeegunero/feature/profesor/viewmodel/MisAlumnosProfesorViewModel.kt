package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.util.FileSaverUtil
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import java.util.Date
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.update

/**
 * Estado UI para la pantalla "Mis Alumnos".
 */
data class MisAlumnosUiState(
    val isLoading: Boolean = false,
    val alumnos: List<Alumno> = emptyList(),
    val error: String? = null,
    val clase: String = "",
    val mensajeExito: String? = null
)

/**
 * ViewModel para la pantalla "Mis Alumnos" del profesor.
 *
 * Gestiona la carga y filtrado de alumnos asignados al profesor.
 * Proporciona acceso a la información detallada de cada alumno,
 * incluyendo sus vinculaciones familiares.
 */
@HiltViewModel
class MisAlumnosProfesorViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val profesorRepository: ProfesorRepository,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository,
    private val calendarioRepository: CalendarioRepository,
    private val authRepository: AuthRepository,
    private val eventoRepository: EventoRepository,
    private val firestore: FirebaseFirestore,
    private val fileSaverUtil: FileSaverUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisAlumnosUiState())
    val uiState: StateFlow<MisAlumnosUiState> = _uiState.asStateFlow()

    init {
        cargarAlumnos()
    }

    /**
     * Carga los alumnos asignados al profesor actual
     */
    fun cargarAlumnos() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener ID del usuario actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No se pudo obtener el usuario actual"
                    ) }
                    return@launch
                }
                
                Timber.d("Cargando alumnos para usuario con DNI: ${usuario.dni}")
                
                // Obtener el perfil de profesor del usuario
                val perfilProfesor = usuario.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
                if (perfilProfesor == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "El usuario no tiene perfil de profesor"
                    ) }
                    return@launch
                }
                
                val centroId = perfilProfesor.centroId
                val profesorId = usuario.dni
                
                Timber.d("Profesor identificado. DNI: $profesorId, Centro: $centroId")
                
                // Lista para almacenar todos los alumnos
                val todosLosAlumnos = mutableListOf<Alumno>()
                var nombreClase = ""
                
                // 1. Obtener alumnos donde este profesor es el asignado directamente
                Timber.d("Buscando alumnos con profesor asignado: $profesorId")
                val alumnosDirectos = alumnoRepository.getAlumnosForProfesor(profesorId)
                todosLosAlumnos.addAll(alumnosDirectos)
                Timber.d("Encontrados ${alumnosDirectos.size} alumnos asignados directamente")
                
                // 2. Obtener clases asignadas al profesor
                val clasesResult = claseRepository.getClasesByProfesor(profesorId)
                if (clasesResult is Result.Success && clasesResult.data.isNotEmpty()) {
                    Timber.d("Encontradas ${clasesResult.data.size} clases para el profesor")
                    
                    // Para cada clase, obtener sus alumnos
                    for (clase in clasesResult.data) {
                        // Guardamos el nombre de la primera clase (para simplificar)
                        if (nombreClase.isEmpty()) {
                            nombreClase = clase.nombre
                        }
                        
                        Timber.d("Procesando clase: ${clase.id} - ${clase.nombre}")
                        
                        // Usar múltiples métodos para obtener alumnos
                        
                        // Método 1: Obtener por ID de clase
                        val alumnosResult = alumnoRepository.getAlumnosByClaseId(clase.id)
                        if (alumnosResult is Result.Success) {
                            Timber.d("Método 1 - Encontrados ${alumnosResult.data.size} alumnos en clase ${clase.nombre}")
                            todosLosAlumnos.addAll(alumnosResult.data)
                        }
                        
                        // Método 2: Usar alumnosIds de la clase
                        clase.alumnosIds?.let { ids ->
                            if (ids.isNotEmpty()) {
                                Timber.d("Método 2 - La clase tiene ${ids.size} IDs de alumnos")
                                for (alumnoId in ids) {
                                    try {
                                        val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                                        if (alumnoResult is Result.Success) {
                                            todosLosAlumnos.add(alumnoResult.data)
                                            Timber.d("Método 2 - Añadido alumno: ${alumnoResult.data.nombre}")
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error al cargar alumno por ID: $alumnoId")
                                    }
                                }
                            }
                        }
                        
                        // Método 3: Obtener alumnos por documento
                        val alumnosPorClase = alumnoRepository.getAlumnosPorClase(clase.id)
                        if (alumnosPorClase.isNotEmpty()) {
                            Timber.d("Método 3 - Encontrados ${alumnosPorClase.size} alumnos por clase")
                            todosLosAlumnos.addAll(alumnosPorClase)
                        }
                    }
                } else {
                    // Intentamos buscar clases asignadas al profesor directamente en la base de datos
                    Timber.d("No se encontraron clases asignadas a este profesor en la búsqueda principal")
                    
                    // Intentar buscar por el ID del profesor en caso de que no esté correctamente mapeado
                    val clasesResultDirecto = claseRepository.getClasesByProfesorId(profesorId)
                    if (clasesResultDirecto is Result.Success && clasesResultDirecto.data.isNotEmpty()) {
                        val clases = clasesResultDirecto.data
                        Timber.d("Encontradas ${clases.size} clases mediante búsqueda directa por ID")
                        
                        for (clase in clases) {
                            // Guardamos el nombre de la primera clase
                            if (nombreClase.isEmpty()) {
                                nombreClase = clase.nombre
                            }
                            
                            // Obtener alumnos de la clase
                            val alumnosResult = alumnoRepository.getAlumnosByClaseId(clase.id)
                            if (alumnosResult is Result.Success) {
                                todosLosAlumnos.addAll(alumnosResult.data)
                                Timber.d("Añadidos ${alumnosResult.data.size} alumnos de clase ${clase.nombre}")
                            }
                        }
                    } else {
                        Timber.d("No hay clases asignadas al profesor $profesorId")
                    }
                }
                
                // 4. Buscar por centro educativo si no se encontraron alumnos
                if (centroId.isNotEmpty() && todosLosAlumnos.isEmpty()) {
                    Timber.d("Buscando alumnos por centroId: $centroId")
                    try {
                        val alumnosDelCentro = cargarAlumnosPorCentro(centroId)
                        if (alumnosDelCentro.isNotEmpty()) {
                            Timber.d("Encontrados ${alumnosDelCentro.size} alumnos del centro")
                            todosLosAlumnos.addAll(alumnosDelCentro)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al cargar alumnos por centro: ${e.message}")
                    }
                }
                
                // Eliminar duplicados
                val alumnosSinDuplicados = todosLosAlumnos.distinctBy { it.dni }
                Timber.d("Total de alumnos sin duplicados: ${alumnosSinDuplicados.size}")
                
                // Si no hay alumnos, mostrar mensaje específico
                if (alumnosSinDuplicados.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No hay alumnos asignados a este profesor",
                        alumnos = emptyList(),
                        clase = nombreClase
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        alumnos = alumnosSinDuplicados,
                        clase = nombreClase
                    ) }
                    Timber.d("Cargados ${alumnosSinDuplicados.size} alumnos para el profesor")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar alumnos del profesor: ${e.message}")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al cargar alumnos: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Carga alumnos por centro educativo
     */
    private suspend fun cargarAlumnosPorCentro(centroId: String): List<Alumno> {
        return try {
            // Intentar con el repositorio 
            val result = alumnoRepository.getAlumnosByCentroId(centroId)
            Timber.d("Resultado de carga por centro: ${result.size} alumnos")
            
            // Si no hay alumnos por el método directo, intentar con método alternativo
            if (result.isEmpty()) {
                Timber.d("Intentando método alternativo de carga por centro")
                val resultAlternativo = alumnoRepository.getAlumnosByCentro(centroId)
                if (resultAlternativo is Result.Success) {
                    Timber.d("Método alternativo: ${resultAlternativo.data.size} alumnos")
                    return resultAlternativo.data
                }
            }
            
            return result
        } catch (e: Exception) {
            Timber.e(e, "Error en cargarAlumnosPorCentro: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Refresca la lista de alumnos
     */
    fun refrescarAlumnos() {
        cargarAlumnos()
    }

    /**
     * Programa una reunión con el familiar de un alumno
     */
    fun programarReunion(alumnoId: String, titulo: String, fecha: String, hora: String, descripcion: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Obtener datos del usuario actual (profesor)
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Usuario no autenticado"
                    ) }
                    return@launch
                }
                
                // Obtener datos del profesor
                val profesorId = currentUser.dni
                
                try {
                    // Manejar el resultado sin depender de tipos específicos
                    val profesorResult = profesorRepository.getProfesorById(profesorId)
                    
                    // Variable para almacenar el centroId
                    var centroId = ""
                    
                    // Obtener centroId de diferentes fuentes
                    centroId = when {
                        // Intento 1: Obtener del perfil del usuario actual (modo más seguro)
                        currentUser.perfiles.any { it.tipo == TipoUsuario.PROFESOR && it.centroId.isNotEmpty() } -> 
                            currentUser.perfiles.first { it.tipo == TipoUsuario.PROFESOR }.centroId
                        
                        // Intento 2: Obtener un valor por defecto si todo falla
                        else -> ""
                    }
                    
                    Timber.d("Programando reunión con centroId: $centroId")
                    
                    // Usar el modelo de Evento existente con los parámetros que acepta
                    val evento = Evento(
                        id = "",
                        titulo = titulo,
                        descripcion = "$descripcion\\n\\nFecha: $fecha\\nHora: $hora",
                        fecha = Timestamp.now(), // Usamos la fecha actual
                        tipo = TipoEvento.REUNION,
                        creadorId = profesorId,
                        centroId = centroId,
                        recordatorio = true,
                        tiempoRecordatorioMinutos = 30,
                        publico = false,
                        destinatarios = listOf(alumnoId),
                        ubicacion = "Centro educativo - Sala de reuniones"
                    )
                    
                    // Guardar evento en Firestore
                    val resultado = eventoRepository.crearEvento(evento)
                    when (resultado) {
                        is Result.Success -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                mensajeExito = "Reunión programada correctamente",
                                error = null
                            ) }
                            
                            // Limpiar mensaje de éxito después de unos segundos
                            delay(3000)
                            _uiState.update { it.copy(mensajeExito = null) }
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                error = "Error al programar reunión: ${resultado.exception?.message}"
                            ) }
                        }
                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener profesor: ${e.message}")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al obtener datos del profesor: ${e.message}"
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al programar reunión")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al programar reunión: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Genera un informe con el listado de alumnos y lo guarda localmente y en Firestore.
     */
    fun generarInformeAlumnos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val alumnos = uiState.value.alumnos
                if (alumnos.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No hay alumnos para generar el informe.") }
                    return@launch
                }
                
                // Obtener ID del profesor actual
                val profesorId = authRepository.getCurrentUserId() ?: "Desconocido"

                // Generar contenido del informe (ejemplo simple)
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                val nombreArchivoBase = "Informe_Alumnos_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}"
                val nombreArchivoLocal = "$nombreArchivoBase.txt"
                var contenido = "Informe de Alumnos Generado el $timestamp\\n"
                contenido += "Clase: ${uiState.value.clase.ifEmpty { "No especificada" }}\\n"
                contenido += "Profesor ID: $profesorId\\n"
                contenido += "=============================================\\n\\n"
                alumnos.forEachIndexed { index, alumno ->
                    contenido += "${index + 1}. ${alumno.nombre} ${alumno.apellidos} (DNI: ${alumno.dni})\\n"
                    // Aquí podrías añadir más detalles si los tuvieras en el modelo Alumno
                }

                // Crear el Map para Firestore (sin el contenido completo)
                val informeFirestore = mapOf(
                    "nombre" to nombreArchivoBase,
                    "fechaGeneracion" to Timestamp.now(),
                    "tipo" to "listado_alumnos_profesor",
                    "generadoPorId" to profesorId,
                    "contenido" to contenido // Guardamos el contenido aquí también por consistencia con el ejemplo del usuario
                    // "urlDescarga" // No aplica para guardado local directo
                )

                // 1. Guardar en Firestore
                var firestoreError: String? = null
                try {
                    firestore.collection("informes")
                             .document(nombreArchivoBase) // Usar nombre base como ID
                             .set(informeFirestore)
                             .await()
                    Timber.i("Registro del informe guardado en Firestore con ID: $nombreArchivoBase")
                } catch (e: Exception) {
                    Timber.e(e, "Error al guardar registro del informe en Firestore")
                    firestoreError = "Error al guardar registro del informe: ${e.message}"
                }

                // 2. Guardar archivo localmente
                val fileSaveResult = fileSaverUtil.saveTextToFile(nombreArchivoLocal, contenido)
                if (fileSaveResult is Result.Success) {
                    // Éxito al guardar localmente, comprobar si hubo error en Firestore
                    if (firestoreError != null) {
                        _uiState.update { it.copy(isLoading = false, error = "$firestoreError. El archivo se guardó localmente como '${fileSaveResult.data}'") }
                    } else {
                        _uiState.update { it.copy(isLoading = false, mensajeExito = "Informe guardado como '${fileSaveResult.data}'") }
                    }
                } else if (fileSaveResult is Result.Error) {
                    // Error al guardar localmente, combinar con error de Firestore si existe
                    val errorMsg = "Error al guardar el archivo local: ${fileSaveResult.exception?.message}" + 
                                   if (firestoreError != null) " ($firestoreError)" else ""
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }

                 // Limpiar mensaje después de unos segundos
                 delay(4000)
                 _uiState.update { it.copy(mensajeExito = null, error = null) }

            } catch (e: Exception) {
                Timber.e(e, "Error al generar el informe de alumnos")
                _uiState.update { it.copy(isLoading = false, error = "Error inesperado al generar el informe: ${e.message}") }
            }
        }
    }
} 