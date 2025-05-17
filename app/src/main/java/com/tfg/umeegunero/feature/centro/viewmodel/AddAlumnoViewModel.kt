package com.tfg.umeegunero.feature.centro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.UsuarioUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.google.android.gms.tasks.Tasks

/**
 * Estado UI para la pantalla de añadir alumno
 */
data class AddAlumnoUiState(
    // Datos personales
    val dni: String = "",
    val dniError: String? = null,
    val nombre: String = "",
    val nombreError: String? = null,
    val apellidos: String = "",
    val apellidosError: String? = null,
    val fechaNacimiento: String = "",
    val fechaNacimientoError: String? = null,
    
    // Datos académicos
    val cursos: List<Curso> = emptyList(),
    val cursoSeleccionado: Curso? = null,
    val isCursoDropdownExpanded: Boolean = false,
    
    val clases: List<Clase> = emptyList(),
    val claseSeleccionada: Clase? = null,
    val isClaseDropdownExpanded: Boolean = false,
    
    // Datos médicos
    val alergias: String = "",
    val medicacion: String = "",
    val necesidadesEspeciales: String = "",
    val observacionesMedicas: String = "",
    val numeroSS: String = "",
    val condicionesMedicas: String = "",
    
    // Observaciones
    val observaciones: String = "",
    
    // Estado UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    
    // Centro
    val centroId: String = ""
) {
    // Validación del formulario
    val isFormValid: Boolean
        get() = dni.isNotBlank() && dniError == null &&
                nombre.isNotBlank() && nombreError == null &&
                apellidos.isNotBlank() && apellidosError == null &&
                fechaNacimiento.isNotBlank() && fechaNacimientoError == null &&
                cursoSeleccionado != null &&
                claseSeleccionada != null
}

@HiltViewModel
class AddAlumnoViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cursoRepository: CursoRepository,
    private val claseRepository: ClaseRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    application: Application
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AddAlumnoUiState())
    val uiState: StateFlow<AddAlumnoUiState> = _uiState.asStateFlow()
    private val alumnosCollection = firestore.collection("alumnos")
    private val usuariosCollection = firestore.collection("usuarios")
    
    init {
        // Obtener el centro del administrador actual
        getCentroIdFromCurrentUser()
        // No cargamos cursos aquí, esperamos a tener el centroId
    }
    
    /**
     * Obtiene el ID del centro del usuario administrador actual y carga los cursos
     */
    private fun getCentroIdFromCurrentUser() {
        viewModelScope.launch {
            try {
                val centroId = UsuarioUtils.obtenerCentroIdDelUsuarioActual(authRepository, usuarioRepository)
                if (!centroId.isNullOrEmpty()) {
                    _uiState.update { state -> state.copy(centroId = centroId) }
                    // Cargar cursos una vez tenemos el centroId
                    cargarCursos(centroId)
                } else {
                    _uiState.update { it.copy(error = "No se pudo determinar el ID del centro.") }
                    Timber.e("No se pudo obtener el centroId del usuario actual")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al obtener datos iniciales: ${e.message}") }
                Timber.e(e, "Error al obtener centro del usuario actual")
            }
        }
    }
    
    /**
     * Carga los cursos disponibles en el centro
     */
    private fun cargarCursos(centroId: String) { // Ahora recibe centroId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cursosList = cursoRepository.obtenerCursosPorCentro(centroId)
                _uiState.update { it.copy(cursos = cursosList, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar cursos: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar cursos")
            } 
            // No ponemos finally isLoading = false aquí porque la carga puede continuar con clases
        }
    }
    
    /**
     * Carga las clases disponibles para un curso específico
     */
    private fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            // Mantenemos isLoading = true si ya estaba, o lo ponemos si no
            _uiState.update { it.copy(isLoading = true, clases = emptyList()) } // Limpiar clases anteriores
            try {
                // Obtener las clases del curso
                @Suppress("UNCHECKED_CAST") // Mantenemos el suppress si el repo no es seguro
                when (val result = cursoRepository.obtenerClasesPorCurso(cursoId)) {
                    is Result.Success<*> -> {
                        _uiState.update { 
                            it.copy(
                                clases = result.data as List<Clase>,
                                isLoading = false
                            ) 
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar las clases: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar clases")
                    }
                    else -> {
                       _uiState.update { it.copy(isLoading = false) } // Terminar carga si es Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar clases: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar clases")
            }
        }
    }
    
    // Funciones para actualizar campos de datos personales
    fun updateDni(dni: String) {
        val error = when {
            dni.isBlank() -> "El DNI es obligatorio"
            !isValidDni(dni) -> "El formato del DNI no es válido"
            else -> null
        }
        
        _uiState.update { it.copy(dni = dni, dniError = error) }
    }
    
    fun updateNombre(nombre: String) {
        val error = when {
            nombre.isBlank() -> "El nombre es obligatorio"
            else -> null
        }
        
        _uiState.update { it.copy(nombre = nombre, nombreError = error) }
    }
    
    fun updateApellidos(apellidos: String) {
        val error = when {
            apellidos.isBlank() -> "Los apellidos son obligatorios"
            else -> null
        }
        
        _uiState.update { it.copy(apellidos = apellidos, apellidosError = error) }
    }
    
    fun updateFechaNacimiento(fecha: String) {
        val error = when {
            fecha.isBlank() -> "La fecha de nacimiento es obligatoria"
            !isValidFecha(fecha) -> "El formato de fecha no es válido (DD/MM/AAAA)"
            else -> null
        }
        
        _uiState.update { it.copy(fechaNacimiento = fecha, fechaNacimientoError = error) }
    }
    
    // Funciones para manejar los dropdowns
    fun toggleCursoDropdown() {
        _uiState.update { it.copy(isCursoDropdownExpanded = !it.isCursoDropdownExpanded) }
    }
    
    fun toggleClaseDropdown() {
        _uiState.update { it.copy(isClaseDropdownExpanded = !it.isClaseDropdownExpanded) }
    }
    
    fun selectCurso(curso: Curso) {
        _uiState.update { 
            it.copy(
                cursoSeleccionado = curso, 
                claseSeleccionada = null, // Resetear clase seleccionada
                isCursoDropdownExpanded = false // Cerrar dropdown curso
            ) 
        }
        // Cargar las clases para el curso recién seleccionado
        cargarClasesPorCurso(curso.id)
    }
    
    fun selectClase(clase: Clase) {
        _uiState.update { 
            it.copy(
                claseSeleccionada = clase,
                isClaseDropdownExpanded = false // Cerrar dropdown clase
            )
        }
    }
    
    // Funciones para actualizar datos médicos
    fun updateAlergias(alergias: String) {
        _uiState.update { it.copy(alergias = alergias) }
    }
    
    fun updateMedicacion(medicacion: String) {
        _uiState.update { it.copy(medicacion = medicacion) }
    }
    
    fun updateNecesidadesEspeciales(necesidades: String) {
        _uiState.update { it.copy(necesidadesEspeciales = necesidades) }
    }
    
    fun updateObservacionesMedicas(observacionesMedicas: String) {
        _uiState.update { it.copy(observacionesMedicas = observacionesMedicas) }
    }
    
    fun updateNumeroSS(numeroSS: String) {
        _uiState.update { it.copy(numeroSS = numeroSS) }
    }
    
    fun updateCondicionesMedicas(condicionesMedicas: String) {
        _uiState.update { it.copy(condicionesMedicas = condicionesMedicas) }
    }
    
    // Función para actualizar observaciones
    fun updateObservaciones(observaciones: String) {
        _uiState.update { it.copy(observaciones = observaciones) }
    }
    
    /**
     * Guarda el alumno en la base de datos (colección 'alumnos')
     */
    fun guardarAlumno() {
        if (!_uiState.value.isFormValid) {
            _uiState.update { it.copy(error = "Por favor, complete todos los campos obligatorios.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 1. Primero obtener la URL del avatar para ALUMNO
                Timber.d("Obteniendo URL del avatar para ALUMNO")
                val avatarUrl = try {
                    val resourceName = "alumno.png"
                    
                    // Verificar si el avatar ya existe en Storage
                    val avatarPath = "avatares/${resourceName.lowercase()}"
                    val storageRef = FirebaseStorage.getInstance().reference.child(avatarPath)
                    
                    try {
                        // Intentar obtener URL si ya existe
                        val downloadTask = storageRef.downloadUrl
                        Tasks.await(downloadTask).toString()
                    } catch (e: Exception) {
                        // Si no existe, subir desde los assets
                        Timber.d("Avatar no encontrado en Storage, usando uno predeterminado")
                        "" // Devolvemos cadena vacía y dejamos que el repositorio maneje esto
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener avatar: ${e.message}")
                    "" // URL vacía en caso de error general
                }
                
                Timber.d("URL de avatar obtenida: $avatarUrl")
                
                // 2. Obtenemos el profesor asociado a la clase seleccionada (si existe)
                val claseId = _uiState.value.claseSeleccionada?.id ?: ""
                var profesorId = ""
                
                if (claseId.isNotEmpty()) {
                    try {
                        val claseResult = claseRepository.getClaseById(claseId)
                        if (claseResult is Result.Success) {
                            profesorId = claseResult.data.profesorId ?: ""
                            Timber.d("Obtenido profesorId: $profesorId para la clase: $claseId")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al obtener la información de la clase")
                    }
                }
                
                // 3. Crear objeto Alumno con los campos correctos
                val nuevoAlumno = Alumno(
                    // id se autogenera o usa dni? Usaremos dni como ID del documento
                    dni = _uiState.value.dni,
                    nombre = _uiState.value.nombre,
                    apellidos = _uiState.value.apellidos,
                    fechaNacimiento = _uiState.value.fechaNacimiento, // Guardar como String dd/MM/yyyy
                    centroId = _uiState.value.centroId,
                    curso = _uiState.value.cursoSeleccionado!!.nombre, // Guardar nombre del curso
                    clase = _uiState.value.claseSeleccionada!!.nombre,  // Guardar nombre de la clase
                    aulaId = _uiState.value.claseSeleccionada!!.id, // Usar id de clase como aulaId
                    alergias = _uiState.value.alergias.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    medicacion = _uiState.value.medicacion.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    necesidadesEspeciales = _uiState.value.necesidadesEspeciales,
                    observacionesMedicas = _uiState.value.observacionesMedicas,
                    numeroSS = _uiState.value.numeroSS,
                    condicionesMedicas = _uiState.value.condicionesMedicas,
                    observaciones = _uiState.value.observaciones,
                    activo = true, // Marcar como activo por defecto
                    profesorId = profesorId, // Asignamos el profesor vinculado a la clase
                    avatarUrl = avatarUrl // Incluimos la URL del avatar
                )
                
                // 4. Guardar el alumno usando el método del repositorio con el contexto
                val context = getApplication<Application>().applicationContext
                when (val result = usuarioRepository.guardarAlumno(nuevoAlumno, context)) {
                    is Result.Success -> {
                        // 5. La operación fue exitosa
                        _uiState.update { it.copy(isLoading = false, success = true) }
                        
                        // 6. Registrar el resultado en logs
                        if (profesorId.isNotEmpty()) {
                            Timber.d("Alumno ${nuevoAlumno.dni} asignado automáticamente al profesor: $profesorId")
                        } else {
                            Timber.d("Alumno guardado correctamente con DNI: ${nuevoAlumno.dni} (sin profesor asignado)")
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Error al guardar alumno: ${result.exception?.message}"
                            )
                        }
                        Timber.e(result.exception, "Error al guardar alumno en repositorio")
                    }
                    is Result.Loading -> {
                        // Mantener estado de carga
                        Timber.d("Guardando alumno... (estado en progreso)")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error inesperado al guardar: ${e.message}"
                    )
                }
                Timber.e(e, "Error inesperado al guardar alumno")
            }
        }
    }
    
    /**
     * Valida el formato de un DNI español
     */
    private fun isValidDni(dni: String): Boolean {
        // Patrón simple para DNI: 8 números y una letra
        val pattern = "^[0-9]{8}[A-Za-z]$".toRegex()
        return pattern.matches(dni)
    }
    
    /**
     * Valida el formato de una fecha (DD/MM/AAAA)
     */
    private fun isValidFecha(fecha: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.isLenient = false
            formatter.parse(fecha)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 