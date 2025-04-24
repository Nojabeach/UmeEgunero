package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.UsuarioUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de gestión de cursos y clases
 */
data class GestionCursosYClasesUiState(
    // Datos
    val cursos: List<Curso> = emptyList(),
    val clases: List<Clase> = emptyList(),
    
    // Estado UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    
    // Centro
    val centroId: String = ""
)

@HiltViewModel
class GestionCursosYClasesViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GestionCursosYClasesUiState())
    val uiState: StateFlow<GestionCursosYClasesUiState> = _uiState.asStateFlow()
    
    init {
        // Obtener el ID del centro del administrador actual
        getCentroIdFromCurrentUser()
    }
    
    /**
     * Obtiene el ID del centro del usuario administrador actual
     */
    private fun getCentroIdFromCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                
                if (currentUser != null) {
                    // Utilizar la utilidad centralizada para obtener el centroId
                    val centroId = UsuarioUtils.obtenerCentroIdDelUsuarioActual(authRepository, usuarioRepository)
                    
                    if (!centroId.isNullOrEmpty()) {
                        _uiState.update { state -> state.copy(centroId = centroId) }
                        cargarCursos()
                    } else {
                        Timber.e("No se pudo obtener el centroId del usuario actual")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener centro del usuario actual")
            }
        }
    }
    
    /**
     * Carga todos los cursos del centro
     */
    fun cargarCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val centroId = _uiState.value.centroId
                
                if (centroId.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo determinar el centro del administrador",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Cargamos los cursos del centro
                when (val cursosResult = cursoRepository.obtenerCursosPorCentroResult(centroId)) {
                    is Result.Success -> {
                        _uiState.update { it.copy(cursos = cursosResult.data) }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar cursos: ${cursosResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(cursosResult.exception, "Error al cargar cursos")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
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
     * Carga todas las clases de un curso específico
     */
    fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener clases del curso
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
                                clases = emptyList(),
                                error = "Error al cargar clases: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al cargar clases")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        clases = emptyList(),
                        error = "Error inesperado al cargar clases: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar clases")
            }
        }
    }
    
    /**
     * Crea un nuevo curso
     */
    fun crearCurso(
        nombre: String,
        anioAcademico: String,
        edadMinima: Int,
        edadMaxima: Int,
        descripcion: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val centroId = _uiState.value.centroId
                
                if (centroId.isBlank()) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo determinar el centro del administrador",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Crear el curso
                val curso = Curso(
                    nombre = nombre,
                    anioAcademico = anioAcademico,
                    edadMinima = edadMinima,
                    edadMaxima = edadMaxima,
                    descripcion = descripcion,
                    centroId = centroId,
                    activo = true
                )
                
                val result = cursoRepository.agregarCurso(curso)
                
                when (result) {
                    is Result.Success<*> -> {
                        // Recargar los cursos para reflejar el cambio
                        cargarCursos()
                        
                        _uiState.update { 
                            it.copy(
                                mensaje = "Curso creado correctamente",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al crear curso: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al crear curso")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al crear curso: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al crear curso")
            }
        }
    }
    
    /**
     * Actualiza un curso existente
     */
    fun actualizarCurso(
        cursoId: String,
        nombre: String,
        anioAcademico: String,
        edadMinima: Int,
        edadMaxima: Int,
        descripcion: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Primero obtener el curso actual para mantener sus otros datos
                val cursoActual = _uiState.value.cursos.find { it.id == cursoId }
                
                if (cursoActual == null) {
                    _uiState.update { 
                        it.copy(
                            error = "No se encontró el curso a actualizar",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Actualizar el curso con los nuevos datos
                val cursoActualizado = cursoActual.copy(
                    nombre = nombre,
                    anioAcademico = anioAcademico,
                    edadMinima = edadMinima,
                    edadMaxima = edadMaxima,
                    descripcion = descripcion
                )
                
                val result = cursoRepository.modificarCurso(cursoActualizado)
                
                when (result) {
                    is Result.Success<*> -> {
                        // Recargar los cursos para reflejar el cambio
                        cargarCursos()
                        
                        _uiState.update { 
                            it.copy(
                                mensaje = "Curso actualizado correctamente",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al actualizar curso: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al actualizar curso")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al actualizar curso: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al actualizar curso")
            }
        }
    }
    
    /**
     * Elimina un curso y todas sus clases asociadas
     */
    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Eliminar el curso
                val result = cursoRepository.borrarCurso(cursoId)
                
                when (result) {
                    is Result.Success<*> -> {
                        // Recargar los cursos para reflejar el cambio
                        cargarCursos()
                        
                        _uiState.update { 
                            it.copy(
                                mensaje = "Curso eliminado correctamente",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar curso: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al eliminar curso")
                    }
                    else -> { /* No hacer nada para el estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al eliminar curso: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al eliminar curso")
            }
        }
    }
    
    // Funciones para manejar mensajes
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 