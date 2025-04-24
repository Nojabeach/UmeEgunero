package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.CursoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de gestión de cursos
 */
data class GestionCursosUiState(
    val cursos: List<Curso> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleteDialogVisible: Boolean = false,
    val selectedCurso: Curso? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)

/**
 * ViewModel para la gestión de cursos
 */
@HiltViewModel
class GestionCursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val centroId: String = savedStateHandle.get<String>("centroId") ?: ""
    
    private val _uiState = MutableStateFlow(GestionCursosUiState())
    val uiState: StateFlow<GestionCursosUiState> = _uiState.asStateFlow()
    
    init {
        Timber.d("Inicializando GestionCursosViewModel con centroId: $centroId")
        if (centroId.isNotEmpty()) {
            cargarCursos()
        } else {
            _uiState.update { 
                it.copy(error = "ID de centro no válido") 
            }
        }
    }
    
    /**
     * Carga todos los cursos asociados al centro desde Firestore
     */
    private fun cargarCursos() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null) 
            }
            
            try {
                // Cargar cursos por centro
                when (val cursosResult = cursoRepository.obtenerCursosPorCentroResult(centroId)) {
                    is Result.Success -> {
                        _uiState.update { it.copy(cursos = cursosResult.data) }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar los cursos: ${cursosResult.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(cursosResult.exception, "Error al cargar cursos desde Firestore")
                    }
                    else -> {
                        // State Loading, ignoramos
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar cursos desde Firestore")
            }
        }
    }
    
    /**
     * Guarda un curso en Firestore
     */
    fun guardarCurso(curso: Curso) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null) 
            }
            
            try {
                // Asegurarse de que los campos mínimos estén establecidos
                val cursoCompleto = curso.copy(
                    centroId = centroId,
                    activo = true
                )
                
                val result = cursoRepository.saveCurso(cursoCompleto)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isSuccess = true,
                                successMessage = "Curso guardado correctamente",
                                isLoading = false
                            ) 
                        }
                        cargarCursos() // Recargar la lista
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al guardar el curso: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al guardar curso en Firestore")
                    }
                    else -> {
                        // State Loading, ignoramos
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al guardar curso en Firestore")
            }
        }
    }
    
    /**
     * Elimina un curso de Firestore
     */
    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null) 
            }
            
            try {
                val result = cursoRepository.deleteCurso(cursoId)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isSuccess = true,
                                successMessage = "Curso eliminado correctamente",
                                isLoading = false,
                                isDeleteDialogVisible = false,
                                selectedCurso = null
                            ) 
                        }
                        cargarCursos() // Recargar la lista
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar el curso: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar curso de Firestore")
                    }
                    else -> {
                        // State Loading, ignoramos
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar curso de Firestore")
            }
        }
    }
    
    /**
     * Muestra el diálogo de confirmación de eliminación
     */
    fun mostrarDialogoEliminar(curso: Curso) {
        _uiState.update { 
            it.copy(
                selectedCurso = curso,
                isDeleteDialogVisible = true
            ) 
        }
    }
    
    /**
     * Oculta el diálogo de confirmación de eliminación
     */
    fun ocultarDialogoEliminar() {
        _uiState.update { 
            it.copy(
                isDeleteDialogVisible = false,
                selectedCurso = null
            ) 
        }
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    fun clearSuccess() {
        _uiState.update { 
            it.copy(
                isSuccess = false,
                successMessage = null
            ) 
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { 
            it.copy(error = null) 
        }
    }
} 