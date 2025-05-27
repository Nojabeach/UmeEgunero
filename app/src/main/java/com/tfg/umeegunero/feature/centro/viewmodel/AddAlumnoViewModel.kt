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
import com.tfg.umeegunero.data.repository.AlumnoRepository
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
    private val alumnoRepository: AlumnoRepository,
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
                // 1. Crear el alumno básico usando el método del AlumnoRepository
                val result = alumnoRepository.crearAlumno(
                    nombre = _uiState.value.nombre,
                    apellidos = _uiState.value.apellidos,
                    dni = _uiState.value.dni,
                    fechaNacimiento = _uiState.value.fechaNacimiento,
                    cursoId = _uiState.value.cursoSeleccionado?.id ?: "",
                    claseId = _uiState.value.claseSeleccionada?.id ?: ""
                )
                
                when (result) {
                    is Result.Success -> {
                        // 2. Obtener la URL del avatar para ALUMNO
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
                        
                        // 3. Actualizar los datos médicos y observaciones si es necesario
                        if (_uiState.value.alergias.isNotEmpty() || 
                            _uiState.value.medicacion.isNotEmpty() || 
                            _uiState.value.necesidadesEspeciales.isNotEmpty() ||
                            _uiState.value.observacionesMedicas.isNotEmpty() ||
                            _uiState.value.numeroSS.isNotEmpty() ||
                            _uiState.value.condicionesMedicas.isNotEmpty() ||
                            _uiState.value.observaciones.isNotEmpty() ||
                            avatarUrl.isNotEmpty()) {
                            
                            // Actualizar el alumno con los datos médicos usando Firestore directamente
                            val alumnoRef = firestore.collection("alumnos").document(_uiState.value.dni)
                            val updates = mutableMapOf<String, Any>()
                            
                            if (_uiState.value.alergias.isNotEmpty()) {
                                updates["alergias"] = _uiState.value.alergias.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            }
                            if (_uiState.value.medicacion.isNotEmpty()) {
                                updates["medicacion"] = _uiState.value.medicacion.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            }
                            if (_uiState.value.necesidadesEspeciales.isNotEmpty()) {
                                updates["necesidadesEspeciales"] = _uiState.value.necesidadesEspeciales
                            }
                            if (_uiState.value.observacionesMedicas.isNotEmpty()) {
                                updates["observacionesMedicas"] = _uiState.value.observacionesMedicas
                            }
                            if (_uiState.value.numeroSS.isNotEmpty()) {
                                updates["numeroSS"] = _uiState.value.numeroSS
                            }
                            if (_uiState.value.condicionesMedicas.isNotEmpty()) {
                                updates["condicionesMedicas"] = _uiState.value.condicionesMedicas
                            }
                            if (_uiState.value.observaciones.isNotEmpty()) {
                                updates["observaciones"] = _uiState.value.observaciones
                            }
                            if (avatarUrl.isNotEmpty()) {
                                updates["avatarUrl"] = avatarUrl
                            }
                            
                            if (updates.isNotEmpty()) {
                                alumnoRef.update(updates).await()
                                Timber.d("Datos médicos y observaciones actualizados para el alumno ${_uiState.value.dni}")
                            }
                        }
                        
                        // 4. Actualizar estado de éxito
                        _uiState.update { it.copy(isLoading = false, success = true) }
                        Timber.d("Alumno guardado correctamente con DNI: ${_uiState.value.dni}")
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