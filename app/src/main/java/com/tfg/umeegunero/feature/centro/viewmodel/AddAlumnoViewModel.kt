package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddAlumnoUiState())
    val uiState: StateFlow<AddAlumnoUiState> = _uiState.asStateFlow()
    
    init {
        // Obtener el centro del administrador actual
        getCentroIdFromCurrentUser()
        // Cargar los cursos disponibles
        cargarCursos()
    }
    
    /**
     * Obtiene el ID del centro del usuario administrador actual
     */
    private fun getCentroIdFromCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                
                if (currentUser != null) {
                    // Obtener el perfil completo del usuario
                    val usuarioResult = usuarioRepository.getUsuarioByEmail(currentUser.email)
                    
                    if (usuarioResult is Result.Success) {
                        val usuario = usuarioResult.data
                        
                        // Obtener el centroId del primer perfil de tipo ADMIN_CENTRO
                        val centroId = usuario?.perfiles
                            ?.find { it.tipo == com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_CENTRO }
                            ?.centroId
                        
                        centroId?.let {
                            _uiState.update { state -> state.copy(centroId = it) }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener centro del usuario actual")
            }
        }
    }
    
    /**
     * Carga los cursos disponibles en el centro
     */
    private fun cargarCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener cursos
                val centroId = _uiState.value.centroId
                
                if (!centroId.isNullOrEmpty()) {
                    when (val cursoResult = cursoRepository.obtenerCursosPorCentroResult(centroId)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(cursos = cursoResult.data) }
                        }
                        is Result.Error -> {
                            _uiState.update { 
                                it.copy(
                                    error = "Error al cargar los cursos: ${cursoResult.exception?.message}",
                                    isLoading = false
                                ) 
                            }
                            Timber.e(cursoResult.exception, "Error al cargar cursos")
                        }
                        else -> {
                            // No hacer nada para el estado Loading
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar cursos: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar cursos")
            }
        }
    }
    
    /**
     * Carga las clases disponibles para un curso específico
     */
    private fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener las clases del curso
                val result = cursoRepository.obtenerClasesPorCurso(cursoId)
                
                when (result) {
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
                        // No hacer nada para el estado Loading
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
                claseSeleccionada = null // Resetear la clase seleccionada al cambiar de curso
            ) 
        }
        
        // Cargar las clases del curso seleccionado
        cargarClasesPorCurso(curso.id)
    }
    
    fun selectClase(clase: Clase) {
        _uiState.update { it.copy(claseSeleccionada = clase) }
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
    
    // Función para actualizar observaciones
    fun updateObservaciones(observaciones: String) {
        _uiState.update { it.copy(observaciones = observaciones) }
    }
    
    /**
     * Guarda el alumno en la base de datos
     */
    fun guardarAlumno() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Verificar que el formulario sea válido
            if (!state.isFormValid) {
                _uiState.update { it.copy(error = "Por favor, complete todos los campos obligatorios") }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Convertir las cadenas de alergias y medicación a listas
                val alergiasList = if (state.alergias.isBlank()) {
                    emptyList()
                } else {
                    state.alergias.split(",").map { it.trim() }
                }
                
                val medicacionList = if (state.medicacion.isBlank()) {
                    emptyList()
                } else {
                    state.medicacion.split(",").map { it.trim() }
                }
                
                // Crear el objeto Alumno
                val alumno = Alumno(
                    dni = state.dni,
                    nombre = state.nombre,
                    apellidos = state.apellidos,
                    fechaNacimiento = state.fechaNacimiento,
                    centroId = state.centroId,
                    aulaId = state.claseSeleccionada?.id ?: "",
                    curso = state.cursoSeleccionado?.nombre ?: "",
                    clase = state.claseSeleccionada?.nombre ?: "",
                    profesorIds = state.claseSeleccionada?.profesorTitularId?.let { listOf(it) } ?: emptyList(),
                    alergias = alergiasList,
                    medicacion = medicacionList,
                    necesidadesEspeciales = state.necesidadesEspeciales,
                    observaciones = state.observaciones,
                    activo = true
                )
                
                // Guardar el alumno en la base de datos
                val result = usuarioRepository.registrarAlumno(alumno)
                
                when (result) {
                    is Result.Success<*> -> {
                        // También asignamos el alumno a la clase seleccionada
                        val asignacionResult = cursoRepository.asignarAlumnoAClase(
                            alumnoId = alumno.dni,
                            claseId = state.claseSeleccionada?.id ?: ""
                        )
                        
                        if (asignacionResult is Result.Error) {
                            Timber.e(asignacionResult.exception, "Error al asignar alumno a clase")
                            // No bloqueamos el éxito por este error
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                success = true,
                                error = null
                            ) 
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al guardar el alumno: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al guardar alumno")
                    }
                    else -> {
                        // No hacer nada para el estado Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al guardar el alumno: ${e.message}",
                        isLoading = false
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