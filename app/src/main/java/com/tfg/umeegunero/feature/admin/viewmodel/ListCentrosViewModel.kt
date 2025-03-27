package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

/**
 * Estado UI para la pantalla de listado de centros
 */
data class ListCentrosUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de listado de centros educativos
 */
@HiltViewModel
class ListCentrosViewModel @Inject constructor(
    private val centroRepository: CentroRepository
) : ViewModel() {
    
    // Estado UI
    private val _uiState = MutableStateFlow(ListCentrosUiState())
    val uiState: StateFlow<ListCentrosUiState> = _uiState.asStateFlow()
    
    // Lista de centros
    private val _centros = MutableStateFlow<List<Centro>>(emptyList())
    val centros: StateFlow<List<Centro>> = _centros.asStateFlow()
    
    /**
     * Carga la lista de centros desde el repositorio
     */
    fun loadCentros() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = centroRepository.getAllCentros()
                when (result) {
                    is Result.Success -> {
                        _centros.value = result.data
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al cargar los centros: ${result.exception.message}"
                        )
                        Timber.e(result.exception, "Error al cargar centros")
                    }
                    else -> {
                        // Estado Loading, no hacemos nada adicional
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado al cargar los centros: ${e.message}"
                )
                Timber.e(e, "Error inesperado al cargar centros")
            }
        }
    }
    
    /**
     * Elimina un centro por su ID
     */
    fun deleteCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = centroRepository.deleteCentro(centroId)
                when (result) {
                    is Result.Success -> {
                        // Actualizar la lista después de eliminar
                        val updatedList = _centros.value.filter { it.id != centroId }
                        _centros.value = updatedList
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Centro eliminado correctamente"
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al eliminar el centro: ${result.exception.message}"
                        )
                        Timber.e(result.exception, "Error al eliminar centro")
                    }
                    else -> {
                        // Estado Loading, no hacemos nada adicional
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado al eliminar el centro: ${e.message}"
                )
                Timber.e(e, "Error inesperado al eliminar centro")
            }
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 