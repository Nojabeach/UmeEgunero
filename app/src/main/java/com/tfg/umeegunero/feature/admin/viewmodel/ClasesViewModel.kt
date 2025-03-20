package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.repository.ClaseRepository
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
 * Estado de UI para la pantalla de gestión de clases
 */
data class ClasesUiState(
    val clases: List<Clase> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la gestión de clases
 */
@HiltViewModel
class ClasesViewModel @Inject constructor(
    private val claseRepository: ClaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClasesUiState(isLoading = true))
    val uiState: StateFlow<ClasesUiState> = _uiState.asStateFlow()

    /**
     * Carga todas las clases desde el repositorio
     */
    fun loadClases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = claseRepository.getAllClases()
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                clases = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Clases cargadas: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar las clases"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar las clases")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar las clases"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar las clases")
            }
        }
    }

    /**
     * Elimina una clase por su ID
     */
    fun deleteClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = claseRepository.deleteClase(claseId)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista de clases después de eliminar
                        loadClases()
                        Timber.d("Clase eliminada con ID: $claseId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al eliminar la clase"
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar la clase")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar la clase"
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar la clase")
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