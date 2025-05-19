package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.UsuarioUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

/**
 * Estado de la UI para la pantalla de vinculación de profesores a clases
 */
data class VincularProfesorClaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val profesores: List<Usuario> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val clasesAsignadas: Map<String, Map<Clase, Boolean>> = emptyMap(),
    val profesorSeleccionado: Usuario? = null,
    val claseSeleccionada: Clase? = null,
    val cursoSeleccionado: Curso? = null,
    val centroSeleccionado: Centro? = null,
    val centroId: String = "",
    val showAsignarClasesDialog: Boolean = false,
    val showConfirmarDesasignacionDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val isAdminApp: Boolean = false
)

/**
 * ViewModel para la pantalla de vinculación de profesores a clases
 */
@HiltViewModel
class VincularProfesorClaseViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val claseRepository: ClaseRepository,
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository,
    private val authRepository: AuthRepository,
    private val profesorRepository: ProfesorRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VincularProfesorClaseUiState())
    val uiState: StateFlow<VincularProfesorClaseUiState> = _uiState.asStateFlow()
    
    private lateinit var firestore: FirebaseFirestore
    
    init {
        cargarDatosIniciales()
    }
    
    /**
     * Carga los datos iniciales necesarios para la pantalla
     */
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Verificar si es admin de app
                val currentUser = authRepository.getCurrentUser()
                val isAdminApp = currentUser?.perfiles?.any { 
                    it.tipo == TipoUsuario.ADMIN_APP 
                } ?: false

                _uiState.update { it.copy(isAdminApp = isAdminApp) }

                // Cargar centros disponibles
                centroRepository.getCentros().collect { centros ->
                    _uiState.update { it.copy(centros = centros) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos iniciales")
                _uiState.update { it.copy(error = "Error al cargar datos: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Selecciona un centro y carga sus cursos
     */
    fun seleccionarCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null,
                centroId = centroId
            ) }
            
            try {
                // Obtener el centro seleccionado
                val centroResult = centroRepository.getCentroById(centroId)
                if (centroResult is Result.Success<*>) {
                    @Suppress("UNCHECKED_CAST")
                    _uiState.update { it.copy(centroSeleccionado = centroResult.data as Centro) }
                }

                // Cargar cursos del centro
                val cursos = cursoRepository.obtenerCursosPorCentro(centroId, soloActivos = false)
                _uiState.update { it.copy(cursos = cursos) }
                
                // Cargar profesores del centro
                cargarProfesores(centroId)
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar centro")
                _uiState.update { it.copy(error = "Error al cargar datos del centro: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Carga los profesores de un centro específico
     */
    fun cargarProfesores(centroId: String) {
        viewModelScope.launch {
            try {
                val profesoresResult = usuarioRepository.getProfesoresByCentro(centroId)
                if (profesoresResult is Result.Success<*>) {
                    @Suppress("UNCHECKED_CAST")
                    _uiState.update { it.copy(profesores = profesoresResult.data as List<Usuario>) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar profesores")
                _uiState.update { it.copy(error = "Error al cargar profesores: ${e.message}") }
            }
        }
    }
    
    /**
     * Selecciona un profesor y carga sus clases asignadas
     */
    fun seleccionarProfesor(profesor: Usuario) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null,
                profesorSeleccionado = profesor
            ) }
            
            try {
                // Cargar clases asignadas al profesor
                when (val result = profesorRepository.getClasesAsignadas(profesor.dni)) {
                    is Result.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val clasesAsignadas = result.data as List<Clase>
                        _uiState.update { it.copy(clasesAsignadas = mapOf(profesor.dni to clasesAsignadas.associateWith { true })) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al cargar clases asignadas: ${result.exception?.message}") }
                    }
                    is Result.Loading<*> -> {
                        // Manejar estado de carga si es necesario
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar profesor")
                _uiState.update { it.copy(error = "Error al cargar datos del profesor: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Asigna un profesor a una clase
     */
    fun asignarProfesorAClase(profesorId: String, claseId: String) {
        if (profesorId.isEmpty() || claseId.isEmpty()) {
            _uiState.update { it.copy(
                error = "No se puede asignar: faltan datos del profesor o de la clase",
                showAsignarClasesDialog = false
            ) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = usuarioRepository.actualizarClasesProfesor(profesorId, claseId, true)
                if (result is Result.Success) {
                    // Actualizar el estado de la UI
                    _uiState.update { it.copy(
                        mensaje = "Profesor asignado correctamente a la clase",
                        showAsignarClasesDialog = false,
                        showSuccessMessage = true
                    ) }
                    
                    // Recargar los datos
                    val profesor = _uiState.value.profesorSeleccionado
                    if (profesor != null) {
                        cargarClasesProfesor(profesor)
                    }
                } else if (result is Result.Error) {
                    _uiState.update { it.copy(
                        error = result.message ?: "Error al asignar profesor a la clase",
                        showAsignarClasesDialog = false
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al asignar profesor a clase")
                _uiState.update { it.copy(
                    error = "Error al asignar profesor: ${e.message}",
                    showAsignarClasesDialog = false
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Desasigna un profesor de una clase
     */
    fun desasignarProfesorDeClase(profesorId: String, claseId: String) {
        if (profesorId.isEmpty() || claseId.isEmpty()) {
            _uiState.update { it.copy(
                error = "No se puede desasignar: faltan datos del profesor o de la clase",
                showConfirmarDesasignacionDialog = false
            ) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = usuarioRepository.actualizarClasesProfesor(profesorId, claseId, false)
                if (result is Result.Success) {
                    // Actualizar el estado de la UI
                    _uiState.update { it.copy(
                        mensaje = "Profesor desasignado correctamente de la clase",
                        showConfirmarDesasignacionDialog = false,
                        showSuccessMessage = true
                    ) }
                    
                    // Recargar los datos
                    val profesor = _uiState.value.profesorSeleccionado
                    if (profesor != null) {
                        cargarClasesProfesor(profesor)
                    }
                } else if (result is Result.Error) {
                    _uiState.update { it.copy(
                        error = result.message ?: "Error al desasignar profesor de la clase",
                        showConfirmarDesasignacionDialog = false
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al desasignar profesor de clase")
                _uiState.update { it.copy(
                    error = "Error al desasignar profesor: ${e.message}",
                    showConfirmarDesasignacionDialog = false
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
            when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                    is Result.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        _uiState.update { it.copy(clases = result.data as List<Clase>) }
                    
                    // Si hay un profesor seleccionado, recargamos sus clases
                    val profesorSeleccionado = _uiState.value.profesorSeleccionado
                    if (profesorSeleccionado != null) {
                        seleccionarProfesor(profesorSeleccionado)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        error = "Error al cargar clases: ${result.exception?.message}",
                        clases = emptyList()
                    ) }
                }
                    is Result.Loading<*> -> {
                    // Manejar estado de carga si es necesario
                }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar clases del curso")
                _uiState.update { it.copy(
                    error = "Error al cargar clases: ${e.message}",
                    clases = emptyList()
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.update { it.copy(
            mensaje = null,
            showSuccessMessage = false
        ) }
    }
    
    /**
     * Limpia los errores
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Inicializa el ViewModel con una clase específica
     */
    fun inicializarConClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Cargar la información de la clase
                val claseResult = claseRepository.getClaseById(claseId)
                if (claseResult is Result.Success<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val clase = claseResult.data as Clase
                    _uiState.update { it.copy(claseSeleccionada = clase) }
                    
                    // Cargar el curso al que pertenece la clase
                    val cursoResult = cursoRepository.getCursoById(clase.cursoId)
                    if (cursoResult is Result.Success<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val curso = cursoResult.data as Curso
                        _uiState.update { it.copy(cursoSeleccionado = curso) }
                        
                        // Cargar el centro al que pertenece el curso
                        if (curso.centroId.isNotEmpty()) {
                            val centroResult = centroRepository.getCentroById(curso.centroId)
                            if (centroResult is Result.Success<*>) {
                                @Suppress("UNCHECKED_CAST")
                                val centro = centroResult.data as Centro
                                _uiState.update { it.copy(
                                    centroId = centro.id,
                                    centroSeleccionado = centro
                                ) }
                                
                                // Cargar cursos del centro
                                val cursos = cursoRepository.obtenerCursosPorCentro(centro.id, soloActivos = false)
                                _uiState.update { it.copy(cursos = cursos) }
                                
                                // Cargar clases del curso
                                cargarClasesPorCurso(curso.id)
                                
                                // Cargar profesores del centro
                                cargarProfesores(centro.id)
                    }
                }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al inicializar con clase")
                _uiState.update { it.copy(error = "Error al cargar la clase: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Obtiene un centro por su ID
     */
    suspend fun obtenerCentroPorId(centroId: String): Centro? {
        return try {
            val result = centroRepository.getCentroById(centroId)
            if (result is Result.Success<*>) {
                @Suppress("UNCHECKED_CAST")
                result.data as Centro
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centro por ID")
            null
        }
    }
    
    /**
     * Muestra un mensaje de error en la UI
     */
    fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
    }
    
    /**
     * Carga los cursos de un centro
     */
    fun cargarCursos(centroId: String) {
        viewModelScope.launch {
            try {
                val cursos = cursoRepository.obtenerCursosPorCentro(centroId, soloActivos = false)
                _uiState.update { it.copy(cursos = cursos) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar cursos")
                _uiState.update { it.copy(error = "Error al cargar cursos: ${e.message}") }
            }
        }
    }
    
    /**
     * Carga todos los centros disponibles
     */
    fun cargarTodosCentros() {
        viewModelScope.launch {
            try {
                centroRepository.getCentros().collect { centros ->
                    _uiState.update { it.copy(centros = centros) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar todos los centros")
                _uiState.update { it.copy(error = "Error al cargar centros: ${e.message}") }
            }
        }
    }
    
    /**
     * Selecciona un curso
     */
    fun seleccionarCurso(curso: Curso) {
        _uiState.update { it.copy(
            cursoSeleccionado = curso,
            claseSeleccionada = null
        ) }
        cargarClasesPorCurso(curso.id)
    }
    
    /**
     * Selecciona una clase
     */
    fun seleccionarClase(clase: Clase) {
        _uiState.update { it.copy(claseSeleccionada = clase) }
    }
    
    /**
     * Verifica si un profesor está asignado a una clase
     */
    fun isProfesorAsignadoAClase(profesorId: String, claseId: String): Boolean {
        val profesor = _uiState.value.profesorSeleccionado ?: return false
        val clasesAsignadas = _uiState.value.clasesAsignadas[profesor.dni] ?: return false
        
        return clasesAsignadas.entries.find { it.key.id == claseId }?.value ?: false
    }
    
    /**
     * Muestra el diálogo de confirmación para desasignar un profesor de una clase
     */
    fun mostrarDialogoConfirmarDesasignacion() {
        _uiState.update { it.copy(showConfirmarDesasignacionDialog = true) }
    }
    
    /**
     * Muestra el diálogo para asignar clases a un profesor
     */
    fun mostrarDialogoAsignarClases() {
        _uiState.update { it.copy(showAsignarClasesDialog = true) }
    }
    
    /**
     * Oculta el diálogo para asignar clases a un profesor
     */
    fun ocultarDialogoAsignarClases() {
        _uiState.update { it.copy(showAsignarClasesDialog = false) }
    }
    
    /**
     * Oculta el diálogo de confirmación para desasignar un profesor de una clase
     */
    fun ocultarDialogoConfirmarDesasignacion() {
        _uiState.update { it.copy(showConfirmarDesasignacionDialog = false) }
    }
    
    /**
     * Carga las clases asignadas a un profesor
     */
    private fun cargarClasesProfesor(profesor: Usuario) {
        viewModelScope.launch {
            try {
                val clasesResult = claseRepository.getClasesByProfesor(profesor.dni)
                if (clasesResult is Result.Success<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val clases = clasesResult.data as List<Clase>
                    
                    // Crear mapa de clases asignadas
                    val clasesMap = mutableMapOf<Clase, Boolean>()
                    clases.forEach { clase ->
                        clasesMap[clase] = true
                                }
                                
                    // Añadir clases no asignadas del curso seleccionado
                    val cursoId = _uiState.value.cursoSeleccionado?.id
                    if (cursoId != null && cursoId.isNotEmpty()) {
                        val todasLasClasesResult = claseRepository.getClasesByCursoId(cursoId)
                        if (todasLasClasesResult is Result.Success<*>) {
                            @Suppress("UNCHECKED_CAST")
                            val todasLasClases = todasLasClasesResult.data as List<Clase>
                            
                            todasLasClases.forEach { clase ->
                                if (!clasesMap.containsKey(clase)) {
                                    clasesMap[clase] = false
                                }
    }
                        }
                    }
                    
                    // Actualizar el estado de la UI
                    val clasesAsignadas = _uiState.value.clasesAsignadas.toMutableMap()
                    clasesAsignadas[profesor.dni] = clasesMap
                    
                    _uiState.update { it.copy(
                        clasesAsignadas = clasesAsignadas
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar clases del profesor")
                _uiState.update { it.copy(error = "Error al cargar clases: ${e.message}") }
            }
        }
    }
} 