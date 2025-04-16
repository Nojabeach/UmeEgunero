package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
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
 * Estado UI para la pantalla de vinculación de profesores a clases
 */
data class VincularProfesorClaseUiState(
    // Datos
    val profesores: List<Usuario> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val profesoresSeleccionados: Map<String, List<String>> = emptyMap(), // Map<claseId, List<profesorId>>
    
    // Estado UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    
    // Filtros y selección
    val cursoSeleccionadoId: String = "",
    val cursoNombre: String = "",
    val claseSeleccionadaId: String = "",
    val profesorSeleccionadoId: String = "",
    val busquedaProfesor: String = "",
    
    // Listado de IDs para facilitar asignaciones
    val profesorIds: List<String> = emptyList(),
    
    // Para manejo del diálogo
    val showAsignarClasesDialog: Boolean = false,
    val selectedProfesor: Usuario? = null,
    val clasesAsignadas: List<Clase> = emptyList()
)

/**
 * ViewModel para la vinculación de profesores a clases
 */
@HiltViewModel
class VincularProfesorClaseViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val claseRepository: ClaseRepository,
    private val cursoRepository: CursoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VincularProfesorClaseUiState())
    val uiState: StateFlow<VincularProfesorClaseUiState> = _uiState.asStateFlow()
    
    init {
        cargarCursos()
        cargarProfesores()
    }
    
    /**
     * Carga todas las clases disponibles
     */
    fun cargarClases() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            // Obtener ID del centro actual
            val centroId = authRepository.getCurrentCentroId()
            
            if (centroId.isEmpty()) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "No se ha podido obtener el centro actual"
                    ) 
                }
                return@launch
            }
            
            when (val result = claseRepository.getClasesByCentro(centroId)) {
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
                            isLoading = false,
                            error = "Error al cargar clases: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga todos los cursos disponibles
     */
    fun cargarCursos() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = cursoRepository.getCursos()) {
                is Result.Success<List<Curso>> -> {
                    _uiState.update { 
                        it.copy(
                            cursos = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar cursos")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar cursos: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga todos los profesores disponibles
     */
    fun cargarProfesores() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = usuarioRepository.getProfesores()) {
                is Result.Success<List<Usuario>> -> {
                    _uiState.update { 
                        it.copy(
                            profesores = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar profesores")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar profesores: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClasesPorCurso(cursoId: String) {
        if (cursoId.isEmpty()) return
        
        _uiState.update { it.copy(
            isLoading = true,
            cursoSeleccionadoId = cursoId
        )}
        
        viewModelScope.launch {
            // Obtener nombre del curso
            val cursoNombre = _uiState.value.cursos.find { it.id == cursoId }?.nombre ?: ""
            
            when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            clases = result.data,
                            isLoading = false,
                            cursoNombre = cursoNombre
                        )
                    }
                    
                    // Para cada clase, cargamos los profesores asignados
                    result.data.forEach { clase ->
                        cargarProfesoresDeClase(clase.id)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar clases: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga los profesores asignados a una clase específica
     */
    private fun cargarProfesoresDeClase(claseId: String) {
        if (claseId.isEmpty()) return
        
        viewModelScope.launch {
            when (val result = claseRepository.getProfesoresByClaseId(claseId)) {
                is Result.Success -> {
                    _uiState.update { currentState ->
                        val updatedMap = currentState.profesoresSeleccionados.toMutableMap()
                        updatedMap[claseId] = result.data
                        currentState.copy(
                            profesoresSeleccionados = updatedMap
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar profesores de clase $claseId")
                    // No actualizamos el error en UI para no interrumpir la experiencia
                }
                is Result.Loading -> {
                    // No necesitamos mostrar indicador de carga para cada clase
                }
            }
        }
    }
    
    /**
     * Selecciona una clase para editar sus profesores
     */
    fun seleccionarClase(claseId: String) {
        _uiState.update { it.copy(claseSeleccionadaId = claseId) }
    }
    
    /**
     * Asigna un profesor a una clase
     */
    fun asignarProfesorAClase(profesorId: String, claseId: String) {
        if (profesorId.isEmpty() || claseId.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = claseRepository.asignarProfesorAClase(profesorId, claseId)) {
                is Result.Success -> {
                    // Actualizar la lista de profesores asignados a esta clase
                    val profesoresActuales = _uiState.value.profesoresSeleccionados[claseId] ?: emptyList()
                    val nuevaLista = if (profesoresActuales.contains(profesorId)) {
                        profesoresActuales
                    } else {
                        profesoresActuales + profesorId
                    }
                    
                    val mapaActualizado = _uiState.value.profesoresSeleccionados.toMutableMap()
                    mapaActualizado[claseId] = nuevaLista
                    
                    _uiState.update { 
                        it.copy(
                            profesoresSeleccionados = mapaActualizado,
                            isLoading = false,
                            mensaje = "Profesor asignado correctamente"
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al asignar profesor a clase")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al asignar profesor: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Desasigna un profesor de una clase
     */
    fun desasignarProfesorDeClase(profesorId: String, claseId: String) {
        if (profesorId.isEmpty() || claseId.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = claseRepository.desasignarProfesorDeClase(profesorId, claseId)) {
                is Result.Success -> {
                    // Actualizar la lista de profesores asignados a esta clase
                    val profesoresActuales = _uiState.value.profesoresSeleccionados[claseId] ?: emptyList()
                    val nuevaLista = profesoresActuales.filter { it != profesorId }
                    
                    val mapaActualizado = _uiState.value.profesoresSeleccionados.toMutableMap()
                    mapaActualizado[claseId] = nuevaLista
                    
                    _uiState.update { 
                        it.copy(
                            profesoresSeleccionados = mapaActualizado,
                            isLoading = false,
                            mensaje = "Profesor desasignado correctamente"
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al desasignar profesor de clase")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al desasignar profesor: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Filtra profesores según texto de búsqueda
     */
    fun actualizarBusquedaProfesor(busqueda: String) {
        _uiState.update { it.copy(busquedaProfesor = busqueda) }
    }
    
    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
    
    /**
     * Selecciona un profesor para asignarle clases
     */
    fun seleccionarProfesor(profesor: Usuario) {
        _uiState.update { it.copy(selectedProfesor = profesor) }
    }
    
    /**
     * Carga las clases asignadas a un profesor
     */
    fun cargarClasesAsignadas(profesorId: String) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = claseRepository.getClasesByProfesor(profesorId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            clasesAsignadas = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases asignadas")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar clases asignadas: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Muestra el diálogo para asignar clases
     */
    fun mostrarDialogoAsignarClases() {
        _uiState.update { it.copy(showAsignarClasesDialog = true) }
    }
    
    /**
     * Oculta el diálogo para asignar clases
     */
    fun ocultarDialogoAsignarClases() {
        _uiState.update { it.copy(showAsignarClasesDialog = false) }
    }
    
    /**
     * Asigna múltiples clases a un profesor
     */
    fun asignarClasesAProfesor(profesorId: String, clasesIds: List<String>) {
        if (profesorId.isEmpty() || clasesIds.isEmpty()) {
            _uiState.update { 
                it.copy(
                    mensaje = "No se han seleccionado clases para asignar",
                    showAsignarClasesDialog = false
                ) 
            }
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            // Obtener clases actuales del profesor
            val clasesActuales = when (val result = claseRepository.getClasesByProfesor(profesorId)) {
                is Result.Success -> result.data.map { it.id }
                else -> emptyList()
            }
            
            // Clases a asignar (nuevas)
            val clasesNuevas = clasesIds.filter { !clasesActuales.contains(it) }
            
            // Clases a desasignar (ya no seleccionadas)
            val clasesDesasignar = clasesActuales.filter { !clasesIds.contains(it) }
            
            var errores = false
            
            // Asignar nuevas clases
            for (claseId in clasesNuevas) {
                when (val result = claseRepository.asignarProfesorAClase(profesorId, claseId)) {
                    is Result.Error -> {
                        errores = true
                        Timber.e(result.exception, "Error al asignar clase $claseId")
                    }
                    else -> {}
                }
            }
            
            // Desasignar clases
            for (claseId in clasesDesasignar) {
                when (val result = claseRepository.desasignarProfesorDeClase(profesorId, claseId)) {
                    is Result.Error -> {
                        errores = true
                        Timber.e(result.exception, "Error al desasignar clase $claseId")
                    }
                    else -> {}
                }
            }
            
            // Actualizar estado
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    showAsignarClasesDialog = false,
                    mensaje = if (errores) 
                        "Se completó la operación con algunos errores" 
                    else 
                        "Clases asignadas correctamente"
                )
            }
            
            // Recargar datos
            cargarClasesAsignadas(profesorId)
        }
    }
} 