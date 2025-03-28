package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.ClaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de edición de clases
 * Gestiona el estado y la lógica de negocio relacionada con la edición de clases
 */
@HiltViewModel
class EditClaseViewModel @Inject constructor(
    private val claseRepository: ClaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditClaseUiState())
    val uiState: StateFlow<EditClaseUiState> = _uiState.asStateFlow()

    /**
     * Carga los datos de una clase existente
     * @param claseId ID de la clase a cargar
     */
    fun cargarClase(claseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = claseRepository.getClaseById(claseId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        clase = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al cargar la clase: ${result.exception.message}",
                        isLoading = false
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }
            }
        }
    }

    /**
     * Guarda una clase nueva o actualiza una existente
     * @param clase Clase a guardar
     */
    fun guardarClase(clase: Clase) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = claseRepository.guardarClase(clase)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al guardar la clase: ${result.exception.message}",
                        isLoading = false
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }
            }
        }
    }

    /**
     * Elimina una clase
     * @param claseId ID de la clase a eliminar
     */
    fun eliminarClase(claseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = claseRepository.eliminarClase(claseId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al eliminar la clase: ${result.exception.message}",
                        isLoading = false
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }
            }
        }
    }
}

/**
 * Estado de la UI para la pantalla de edición de clases
 */
data class EditClaseUiState(
    val clase: Clase? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) 