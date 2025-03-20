package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de gestión de cursos
 */
data class CursosUiState(
    val cursos: List<Curso> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la gestión de cursos
 */
@HiltViewModel
class CursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CursosUiState(isLoading = true))
    val uiState: StateFlow<CursosUiState> = _uiState.asStateFlow()

    /**
     * Carga todos los cursos desde el repositorio
     */
    fun loadCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = cursoRepository.getAllCursos()
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                cursos = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Cursos cargados: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar los cursos"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar los cursos")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los cursos"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar los cursos")
            }
        }
    }

    /**
     * Elimina un curso por su ID
     */
    fun deleteCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = cursoRepository.deleteCurso(cursoId)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista de cursos después de eliminar
                        loadCursos()
                        Timber.d("Curso eliminado con ID: $cursoId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al eliminar el curso"
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar el curso")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el curso"
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar el curso")
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 