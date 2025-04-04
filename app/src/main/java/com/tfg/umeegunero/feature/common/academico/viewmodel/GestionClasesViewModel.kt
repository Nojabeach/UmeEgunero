package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.ClaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estados posibles para la UI de gestión de clases
 */
data class GestionClasesUiState(
    val clases: List<Clase> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeleteDialogVisible: Boolean = false,
    val selectedClase: Clase? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)

/**
 * ViewModel para la gestión de clases
 * Trabaja con Firestore para operaciones CRUD
 */
@HiltViewModel
class GestionClasesViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val cursoId: String = savedStateHandle.get<String>("cursoId") ?: ""
    
    private val _uiState = MutableStateFlow(GestionClasesUiState())
    val uiState: StateFlow<GestionClasesUiState> = _uiState.asStateFlow()
    
    init {
        Timber.d("Inicializando GestionClasesViewModel con cursoId: $cursoId")
        if (cursoId.isNotEmpty()) {
            cargarClasesPorCurso(cursoId)
        } else {
            _uiState.update { 
                it.copy(error = "ID de curso no válido") 
            }
        }
    }
    
    /**
     * Carga todas las clases asociadas a un curso desde Firestore
     */
    fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null) 
            }
            
            try {
                val result = claseRepository.getClasesByCursoId(cursoId)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                clases = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Clases cargadas desde Firestore: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar las clases: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar clases desde Firestore")
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
                Timber.e(e, "Error inesperado al cargar clases desde Firestore")
            }
        }
    }
    
    /**
     * Guarda una clase en Firestore
     */
    fun guardarClase(clase: Clase) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null) 
            }
            
            try {
                // Asegurarse de que los campos mínimos estén establecidos
                val claseCompleta = clase.copy(
                    cursoId = cursoId,
                    centroId = clase.centroId.ifBlank { obtenerCentroIdDelCurso() },
                    activo = true
                )
                
                val result = claseRepository.guardarClase(claseCompleta)
                
                when (result) {
                    is Result.Success -> {
                        // Actualizar el estado UI después de guardar
                        cargarClasesPorCurso(cursoId)
                        _uiState.update { 
                            it.copy(
                                isSuccess = true,
                                successMessage = "Clase guardada correctamente en Firestore"
                            ) 
                        }
                        Timber.d("Clase guardada en Firestore con ID: ${result.data}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al guardar la clase: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al guardar clase en Firestore")
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
                Timber.e(e, "Error inesperado al guardar clase en Firestore")
            }
        }
    }
    
    // Función auxiliar para obtener el centroId asociado al curso
    private fun obtenerCentroIdDelCurso(): String {
        // En una implementación real, consultaríamos el CursoRepository para obtener el centroId
        // Como simplificación, usamos un valor por defecto
        return "centro_default"
    }
    
    /**
     * Elimina una clase de Firestore
     */
    fun eliminarClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null, isDeleteDialogVisible = false) 
            }
            
            try {
                val result = claseRepository.eliminarClase(claseId)
                
                when (result) {
                    is Result.Success -> {
                        // Actualizar el estado UI después de eliminar
                        cargarClasesPorCurso(cursoId)
                        _uiState.update { 
                            it.copy(
                                isSuccess = true,
                                successMessage = "Clase eliminada correctamente de Firestore"
                            ) 
                        }
                        Timber.d("Clase eliminada de Firestore con ID: $claseId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar la clase: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar clase de Firestore")
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
                Timber.e(e, "Error inesperado al eliminar clase de Firestore")
            }
        }
    }
    
    /**
     * Muestra el diálogo de confirmación para eliminar una clase
     */
    fun mostrarDialogoEliminar(clase: Clase) {
        _uiState.update { 
            it.copy(
                isDeleteDialogVisible = true,
                selectedClase = clase
            ) 
        }
    }
    
    /**
     * Oculta el diálogo de confirmación para eliminar una clase
     */
    fun ocultarDialogoEliminar() {
        _uiState.update { 
            it.copy(
                isDeleteDialogVisible = false,
                selectedClase = null
            ) 
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun limpiarError() {
        _uiState.update { 
            it.copy(error = null) 
        }
    }
    
    /**
     * Limpia el estado de éxito
     */
    fun limpiarExito() {
        _uiState.update { 
            it.copy(
                isSuccess = false,
                successMessage = null
            ) 
        }
    }
} 