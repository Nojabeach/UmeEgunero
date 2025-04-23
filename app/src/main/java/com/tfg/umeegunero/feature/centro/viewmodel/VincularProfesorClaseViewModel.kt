package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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
    val clasesAsignadas: Map<String, List<Clase>> = emptyMap(),
    val profesorSeleccionado: Usuario? = null,
    val claseSeleccionada: Clase? = null,
    val cursoSeleccionado: Curso? = null,
    val centroId: String = "",
    val showAsignarClasesDialog: Boolean = false,
    val showConfirmarDesasignacionDialog: Boolean = false,
    val showSuccessMessage: Boolean = false
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
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VincularProfesorClaseUiState())
    val uiState: StateFlow<VincularProfesorClaseUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Obtenemos el ID del centro del usuario actual
            val usuarioActual = authRepository.getCurrentUser()
            
            usuarioActual?.let { usuario ->
                // TODO: Obtener el centroId correspondiente según el perfil actual del usuario
                // Por ahora asumimos que es el primer centro asociado al usuario
                val centroId = obtenerCentroIdDelUsuarioActual(usuario.documentId)
                
                if (centroId != null) {
                    _uiState.update { it.copy(centroId = centroId) }
                    cargarCursos(centroId)
                } else {
                    _uiState.update { 
                        it.copy(error = "No se pudo determinar el centro educativo") 
                    }
                }
            } ?: run {
                _uiState.update { 
                    it.copy(error = "Usuario no autenticado") 
                }
            }
        }
    }
    
    /**
     * Obtiene el ID del centro asociado al usuario actual
     */
    private suspend fun obtenerCentroIdDelUsuarioActual(usuarioId: String): String? {
        return when (val result = usuarioRepository.getUsuarioById(usuarioId)) {
            is Result.Success -> {
                val usuario = result.data
                // Buscamos el primer perfil que tenga un centroId asignado
                val centroId = usuario.perfiles.firstOrNull()?.centroId
                centroId
            }
            is Result.Error -> {
                _uiState.update { 
                    it.copy(error = "Error al obtener el usuario: ${result.exception?.message}") 
                }
                null
            }
            is Result.Loading -> {
                // Manejo del estado de carga si es necesario
                null
            }
        }
    }
    
    /**
     * Carga los cursos disponibles para el centro
     */
    fun cargarCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = cursoRepository.getCursosByCentro(centroId)) {
                is Result.Success -> {
                    val cursos = result.data
                    _uiState.update { 
                        it.copy(
                            cursos = cursos,
                            isLoading = false
                        )
                    }
                    
                    // Si hay cursos, cargamos los profesores
                    if (cursos.isNotEmpty()) {
                        cargarProfesores(centroId)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar cursos")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar cursos: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Carga los profesores del centro
     */
    fun cargarProfesores(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = centroRepository.getProfesoresByCentro(centroId)) {
                is Result.Success -> {
                    // Ordenamos los profesores: primero los activos y luego los inactivos
                    val profesoresOrdenados = result.data.sortedWith(
                        compareByDescending<Usuario> { it.activo }
                            .thenBy { it.nombre }
                            .thenBy { it.apellidos }
                    )
                    
                    _uiState.update { 
                        it.copy(
                            profesores = profesoresOrdenados,
                            isLoading = false
                        )
                    }
                    
                    // Si no hay profesores, mostrar mensaje de error
                    if (profesoresOrdenados.isEmpty()) {
                        _uiState.update { 
                            it.copy(
                                error = "No hay profesores disponibles para este centro"
                            )
                        }
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar profesores")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar profesores: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Selecciona un curso y carga sus clases
     */
    fun seleccionarCurso(curso: Curso) {
        _uiState.update { it.copy(cursoSeleccionado = curso) }
        cargarClases(curso.id)
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            clases = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar clases: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Selecciona un profesor y carga sus clases asignadas
     */
    fun seleccionarProfesor(profesor: Usuario) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    profesorSeleccionado = profesor,
                    isLoading = true,
                    error = null
                ) 
            }
            
            when (val result = claseRepository.getClasesByProfesor(profesor.documentId)) {
                is Result.Success -> {
                    val clasesAsignadas = result.data
                    _uiState.update { state ->
                        val clasesMap = mutableMapOf<String, List<Clase>>()
                        clasesMap[profesor.documentId] = clasesAsignadas
                        
                        state.copy(
                            clasesAsignadas = state.clasesAsignadas + clasesMap,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases del profesor")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar clases del profesor: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Selecciona una clase para asignar o desasignar
     */
    fun seleccionarClase(clase: Clase) {
        _uiState.update { it.copy(claseSeleccionada = clase) }
    }
    
    /**
     * Asigna un profesor a una clase
     */
    fun asignarProfesorAClase() {
        val profesor = _uiState.value.profesorSeleccionado
        val clase = _uiState.value.claseSeleccionada
        
        if (profesor == null || clase == null) {
            _uiState.update { 
                it.copy(error = "Selecciona un profesor y una clase") 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = claseRepository.asignarProfesorAClase(profesor.documentId, clase.id)) {
                is Result.Success -> {
                    // Actualizamos la lista de clases asignadas
                    seleccionarProfesor(profesor)
                    
                    _uiState.update { 
                        it.copy(
                            mensaje = "Profesor asignado correctamente",
                            showSuccessMessage = true,
                            isLoading = false,
                            showAsignarClasesDialog = false
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al asignar profesor a clase")
                    _uiState.update { 
                        it.copy(
                            error = "Error al asignar profesor: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Desasigna un profesor de una clase
     */
    fun desasignarProfesorDeClase() {
        val profesor = _uiState.value.profesorSeleccionado
        val clase = _uiState.value.claseSeleccionada
        
        if (profesor == null || clase == null) {
            _uiState.update { 
                it.copy(error = "Selecciona un profesor y una clase") 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = claseRepository.desasignarProfesorDeClase(profesor.documentId, clase.id)) {
                is Result.Success -> {
                    // Actualizamos la lista de clases asignadas
                    seleccionarProfesor(profesor)
                    
                    _uiState.update { 
                        it.copy(
                            mensaje = "Profesor desasignado correctamente",
                            showSuccessMessage = true,
                            isLoading = false,
                            showConfirmarDesasignacionDialog = false
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al desasignar profesor de clase")
                    _uiState.update { 
                        it.copy(
                            error = "Error al desasignar profesor: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Verifica si un profesor está asignado a una clase específica
     */
    fun isProfesorAsignadoAClase(profesorId: String, claseId: String): Boolean {
        val clasesAsignadas = _uiState.value.clasesAsignadas[profesorId] ?: emptyList()
        return clasesAsignadas.any { it.id == claseId }
    }
    
    /**
     * Muestra el diálogo de asignación de clases
     */
    fun mostrarDialogoAsignarClases(mostrar: Boolean) {
        _uiState.update { it.copy(showAsignarClasesDialog = mostrar) }
    }
    
    /**
     * Muestra el diálogo de confirmación de desasignación
     */
    fun mostrarDialogoConfirmarDesasignacion(mostrar: Boolean) {
        _uiState.update { it.copy(showConfirmarDesasignacionDialog = mostrar) }
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.update { 
            it.copy(
                mensaje = null,
                showSuccessMessage = false
            ) 
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 