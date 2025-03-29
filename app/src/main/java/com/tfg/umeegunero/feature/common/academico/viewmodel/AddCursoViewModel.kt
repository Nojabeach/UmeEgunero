package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.repository.CursoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de añadir curso
 */
data class AddCursoUiState(
    val centroId: String = "",
    val nombre: String = "",
    val nombreError: String? = null,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val edadMinima: String = "",
    val edadMinimaError: String? = null,
    val edadMaxima: String = "",
    val edadMaximaError: String? = null,
    val anioAcademico: String = "",
    val anioAcademicoError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de añadir curso
 */
@HiltViewModel
class AddCursoViewModel @Inject constructor(
    private val cursoRepository: CursoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddCursoUiState())
    val uiState: StateFlow<AddCursoUiState> = _uiState.asStateFlow()
    
    // Métodos para actualizar los campos del formulario
    fun setCentroId(centroId: String) {
        _uiState.update { it.copy(centroId = centroId) }
    }
    
    fun updateNombre(nombre: String) {
        _uiState.update { 
            it.copy(
                nombre = nombre,
                nombreError = null
            )
        }
    }
    
    fun updateDescripcion(descripcion: String) {
        _uiState.update { 
            it.copy(
                descripcion = descripcion,
                descripcionError = null
            )
        }
    }
    
    fun updateEdadMinima(edadMinima: String) {
        _uiState.update { 
            it.copy(
                edadMinima = edadMinima,
                edadMinimaError = null
            )
        }
    }
    
    fun updateEdadMaxima(edadMaxima: String) {
        _uiState.update { 
            it.copy(
                edadMaxima = edadMaxima,
                edadMaximaError = null
            )
        }
    }
    
    fun updateAnioAcademico(anioAcademico: String) {
        _uiState.update { 
            it.copy(
                anioAcademico = anioAcademico,
                anioAcademicoError = null
            )
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // Método para validar el formulario
    private fun validarFormulario(): Boolean {
        var isValid = true
        
        // Validar nombre
        if (_uiState.value.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre del curso es obligatorio") }
            isValid = false
        }
        
        // Validar descripción
        if (_uiState.value.descripcion.isBlank()) {
            _uiState.update { it.copy(descripcionError = "La descripción es obligatoria") }
            isValid = false
        }
        
        // Validar edad mínima
        val edadMinima = _uiState.value.edadMinima.toIntOrNull()
        if (edadMinima == null) {
            _uiState.update { it.copy(edadMinimaError = "La edad mínima debe ser un número") }
            isValid = false
        } else if (edadMinima < 0 || edadMinima > 99) {
            _uiState.update { it.copy(edadMinimaError = "La edad mínima debe estar entre 0 y 99") }
            isValid = false
        }
        
        // Validar edad máxima
        val edadMaxima = _uiState.value.edadMaxima.toIntOrNull()
        if (edadMaxima == null) {
            _uiState.update { it.copy(edadMaximaError = "La edad máxima debe ser un número") }
            isValid = false
        } else if (edadMaxima < 0 || edadMaxima > 99) {
            _uiState.update { it.copy(edadMaximaError = "La edad máxima debe estar entre 0 y 99") }
            isValid = false
        }
        
        // Validar que edad máxima sea mayor o igual que edad mínima
        if (edadMinima != null && edadMaxima != null && edadMaxima < edadMinima) {
            _uiState.update { it.copy(edadMaximaError = "La edad máxima debe ser mayor o igual que la edad mínima") }
            isValid = false
        }
        
        // Validar año académico
        if (_uiState.value.anioAcademico.isBlank()) {
            _uiState.update { it.copy(anioAcademicoError = "El año académico es obligatorio") }
            isValid = false
        } else if (!_uiState.value.anioAcademico.matches(Regex("^\\d{4}-\\d{4}$"))) {
            _uiState.update { it.copy(anioAcademicoError = "El formato debe ser AAAA-AAAA (ej: 2023-2024)") }
            isValid = false
        }
        
        return isValid
    }
    
    // Método para guardar el curso
    fun guardarCurso() {
        if (!validarFormulario()) {
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val curso = Curso(
                    id = UUID.randomUUID().toString(),
                    nombre = _uiState.value.nombre,
                    descripcion = _uiState.value.descripcion,
                    edadMinima = _uiState.value.edadMinima.toInt(),
                    edadMaxima = _uiState.value.edadMaxima.toInt(),
                    anioAcademico = _uiState.value.anioAcademico,
                    centroId = _uiState.value.centroId,
                    fechaCreacion = Timestamp.now(),
                    activo = true,
                    clases = emptyList()
                )
                
                cursoRepository.guardarCurso(curso)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al guardar el curso: ${e.message}"
                    )
                }
            }
        }
    }
} 