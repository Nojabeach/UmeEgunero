package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.ClaseRepository
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
 * Estado UI para la pantalla de listado de clases
 */
data class ListClasesUiState(
    val clases: List<Clase> = emptyList(),
    val nombreCurso: String = "",
    val cursoId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de listado de clases
 * Gestiona la carga y eliminación de clases
 */
@HiltViewModel
class ListClasesViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    private val cursoRepository: CursoRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListClasesUiState())
    val uiState: StateFlow<ListClasesUiState> = _uiState.asStateFlow()
    
    init {
        // Intentar obtener el cursoId de los argumentos de navegación
        val cursoId = savedStateHandle.get<String>("cursoId")
        if (!cursoId.isNullOrEmpty()) {
            _uiState.update { it.copy(cursoId = cursoId) }
            cargarClases()
            cargarDatosCurso(cursoId)
        }
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClases() {
        val cursoId = _uiState.value.cursoId
        if (cursoId.isEmpty()) {
            _uiState.update { it.copy(
                error = "No se pudo determinar el curso para cargar las clases",
                isLoading = false
            ) }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                is Result.Success -> {
                    Timber.d("Clases cargadas: ${result.data.size}")
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
                            error = "Error al cargar clases: ${result.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // El estado de carga ya fue establecido
                }
            }
        }
    }
    
    /**
     * Carga los datos del curso para mostrar su nombre en la UI
     * @param cursoId ID del curso
     */
    private fun cargarDatosCurso(cursoId: String) {
        viewModelScope.launch {
            when (val result = cursoRepository.getCursoById(cursoId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(nombreCurso = result.data.nombre)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar datos del curso")
                }
                is Result.Loading -> {
                    // No es necesario hacer nada aquí
                }
            }
        }
    }
    
    /**
     * Elimina una clase
     * @param claseId ID de la clase a eliminar
     */
    fun eliminarClase(claseId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = claseRepository.eliminarClase(claseId)) {
                is Result.Success -> {
                    Timber.d("Clase eliminada: $claseId")
                    // Recargar la lista de clases después de eliminar
                    cargarClases()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al eliminar clase")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al eliminar clase: ${result.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // El estado de carga ya fue establecido
                }
            }
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 