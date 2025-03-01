package com.tfg.umeegunero.feature.centro.viewmodel

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
import java.util.UUID
import javax.inject.Inject

/**
 * Estado UI para la pantalla de añadir/editar curso
 */
data class AddCursoUiState(
    // Información del curso
    val id: String = "",
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
    val aniosNacimiento: List<Int> = emptyList(),
    val nuevoAnioNacimiento: String = "",
    val nuevoAnioNacimientoError: String? = null,
    
    // Estado de la UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isEditMode: Boolean = false
) {
    val isFormValid: Boolean
        get() = nombre.isNotBlank() && nombreError == null &&
                descripcion.isNotBlank() && descripcionError == null &&
                edadMinima.isNotBlank() && edadMinimaError == null &&
                edadMaxima.isNotBlank() && edadMaximaError == null &&
                anioAcademico.isNotBlank() && anioAcademicoError == null &&
                aniosNacimiento.isNotEmpty()
}

@HiltViewModel
class AddCursoViewModel @Inject constructor(
    private val cursoRepository: CursoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCursoUiState())
    val uiState: StateFlow<AddCursoUiState> = _uiState.asStateFlow()

    fun updateNombre(nombre: String) {
        val error = if (nombre.isBlank()) "El nombre es obligatorio" else null
        _uiState.update { it.copy(nombre = nombre, nombreError = error) }
    }

    fun updateDescripcion(descripcion: String) {
        val error = if (descripcion.isBlank()) "La descripción es obligatoria" else null
        _uiState.update { it.copy(descripcion = descripcion, descripcionError = error) }
    }

    fun updateEdadMinima(edadMinima: String) {
        val error = when {
            edadMinima.isBlank() -> "La edad mínima es obligatoria"
            !isNumeric(edadMinima) -> "La edad debe ser un número"
            edadMinima.toIntOrNull() ?: 0 < 0 -> "La edad no puede ser negativa"
            _uiState.value.edadMaxima.isNotBlank() && 
            isNumeric(_uiState.value.edadMaxima) && 
            (edadMinima.toInt() > _uiState.value.edadMaxima.toInt()) -> 
                "La edad mínima no puede ser mayor que la máxima"
            else -> null
        }
        _uiState.update { it.copy(edadMinima = edadMinima, edadMinimaError = error) }
        
        // Actualizar también el error de edad máxima si es necesario
        if (_uiState.value.edadMaxima.isNotBlank() && isNumeric(_uiState.value.edadMaxima) && 
            isNumeric(edadMinima) && edadMinima.toInt() > _uiState.value.edadMaxima.toInt()) {
            _uiState.update { 
                it.copy(edadMaximaError = "La edad máxima debe ser mayor o igual que la mínima") 
            }
        } else if (_uiState.value.edadMaximaError != null && 
                  _uiState.value.edadMaxima.isNotBlank() && 
                  isNumeric(_uiState.value.edadMaxima) && 
                  isNumeric(edadMinima) && 
                  edadMinima.toInt() <= _uiState.value.edadMaxima.toInt()) {
            _uiState.update { it.copy(edadMaximaError = null) }
        }
        
        // Si tenemos edades mínima y máxima válidas, y año académico válido, calculamos los años de nacimiento
        if (error == null && 
            _uiState.value.edadMaxima.isNotBlank() && _uiState.value.edadMaximaError == null &&
            _uiState.value.anioAcademico.isNotBlank() && _uiState.value.anioAcademicoError == null) {
            calcularAniosNacimiento()
        }
    }

    fun updateEdadMaxima(edadMaxima: String) {
        val error = when {
            edadMaxima.isBlank() -> "La edad máxima es obligatoria"
            !isNumeric(edadMaxima) -> "La edad debe ser un número"
            edadMaxima.toIntOrNull() ?: 0 < 0 -> "La edad no puede ser negativa"
            _uiState.value.edadMinima.isNotBlank() && 
            isNumeric(_uiState.value.edadMinima) && 
            (edadMaxima.toInt() < _uiState.value.edadMinima.toInt()) -> 
                "La edad máxima no puede ser menor que la mínima"
            else -> null
        }
        _uiState.update { it.copy(edadMaxima = edadMaxima, edadMaximaError = error) }
        
        // Actualizar también el error de edad mínima si es necesario
        if (_uiState.value.edadMinima.isNotBlank() && isNumeric(_uiState.value.edadMinima) && 
            isNumeric(edadMaxima) && _uiState.value.edadMinima.toInt() > edadMaxima.toInt()) {
            _uiState.update { 
                it.copy(edadMinimaError = "La edad mínima debe ser menor o igual que la máxima") 
            }
        } else if (_uiState.value.edadMinimaError != null && 
                  _uiState.value.edadMinima.isNotBlank() && 
                  isNumeric(_uiState.value.edadMinima) && 
                  isNumeric(edadMaxima) && 
                  _uiState.value.edadMinima.toInt() <= edadMaxima.toInt()) {
            _uiState.update { it.copy(edadMinimaError = null) }
        }
        
        // Si tenemos edades mínima y máxima válidas, y año académico válido, calculamos los años de nacimiento
        if (error == null && 
            _uiState.value.edadMinima.isNotBlank() && _uiState.value.edadMinimaError == null &&
            _uiState.value.anioAcademico.isNotBlank() && _uiState.value.anioAcademicoError == null) {
            calcularAniosNacimiento()
        }
    }

    fun updateAnioAcademico(anioAcademico: String) {
        val error = when {
            anioAcademico.isBlank() -> "El año académico es obligatorio"
            !isValidAcademicYear(anioAcademico) -> "Formato inválido. Debe ser YYYY-YYYY"
            else -> null
        }
        _uiState.update { it.copy(anioAcademico = anioAcademico, anioAcademicoError = error) }
        
        // Si tenemos edades mínima y máxima válidas, y año académico válido, calculamos los años de nacimiento
        if (error == null && 
            _uiState.value.edadMinima.isNotBlank() && _uiState.value.edadMinimaError == null &&
            _uiState.value.edadMaxima.isNotBlank() && _uiState.value.edadMaximaError == null) {
            calcularAniosNacimiento()
        }
    }

    fun updateNuevoAnioNacimiento(anio: String) {
        val error = when {
            anio.isBlank() -> null // No es obligatorio hasta que se quiera añadir
            !isNumeric(anio) -> "El año debe ser un número"
            anio.length != 4 -> "El año debe tener 4 dígitos"
            anio.toIntOrNull() ?: 0 < 1900 || anio.toIntOrNull() ?: 0 > 2100 -> "Año fuera de rango válido"
            _uiState.value.aniosNacimiento.contains(anio.toIntOrNull() ?: 0) -> "Este año ya está en la lista"
            else -> null
        }
        _uiState.update { it.copy(nuevoAnioNacimiento = anio, nuevoAnioNacimientoError = error) }
    }

    fun addAnioNacimiento() {
        val anio = _uiState.value.nuevoAnioNacimiento
        if (anio.isBlank() || _uiState.value.nuevoAnioNacimientoError != null) return
        
        val anioInt = anio.toIntOrNull() ?: return
        if (_uiState.value.aniosNacimiento.contains(anioInt)) return
        
        val updatedList = _uiState.value.aniosNacimiento + anioInt
        _uiState.update { 
            it.copy(
                aniosNacimiento = updatedList.sorted(),
                nuevoAnioNacimiento = ""
            ) 
        }
    }

    fun removeAnioNacimiento(anio: Int) {
        val updatedList = _uiState.value.aniosNacimiento.filter { it != anio }
        _uiState.update { it.copy(aniosNacimiento = updatedList) }
    }

    fun setCentroId(centroId: String) {
        _uiState.update { it.copy(centroId = centroId) }
    }

    fun loadCurso(cursoId: String) {
        if (cursoId.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = cursoRepository.getCursoById(cursoId)
                
                when (result) {
                    is Result.Success -> {
                        val curso = result.data
                        _uiState.update {
                            it.copy(
                                id = curso.id,
                                centroId = curso.centroId,
                                nombre = curso.nombre,
                                descripcion = curso.descripcion,
                                edadMinima = curso.edadMinima.toString(),
                                edadMaxima = curso.edadMaxima.toString(),
                                aniosNacimiento = curso.aniosNacimiento,
                                anioAcademico = curso.anioAcademico,
                                isLoading = false,
                                isEditMode = true
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar el curso"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar el curso"
                    )
                }
            }
        }
    }

    fun saveCurso() {
        if (!_uiState.value.isFormValid) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val curso = Curso(
                    id = if (_uiState.value.isEditMode) _uiState.value.id else UUID.randomUUID().toString(),
                    centroId = _uiState.value.centroId,
                    nombre = _uiState.value.nombre,
                    descripcion = _uiState.value.descripcion,
                    edadMinima = _uiState.value.edadMinima.toIntOrNull() ?: 0,
                    edadMaxima = _uiState.value.edadMaxima.toIntOrNull() ?: 0,
                    aniosNacimiento = _uiState.value.aniosNacimiento,
                    anioAcademico = _uiState.value.anioAcademico,
                    activo = true
                )
                
                val result = if (_uiState.value.isEditMode) {
                    cursoRepository.updateCurso(curso)
                } else {
                    cursoRepository.createCurso(curso)
                }
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                success = true,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                success = false,
                                error = result.exception.message ?: "Error al guardar el curso"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = false,
                        error = e.message ?: "Error inesperado al guardar el curso"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(success = false) }
    }

    // Métodos de validación
    private fun isNumeric(text: String): Boolean {
        return text.toIntOrNull() != null
    }
    
    private fun isValidAcademicYear(year: String): Boolean {
        // Formato esperado: "2023-2024"
        val pattern = Regex("^\\d{4}-\\d{4}$")
        if (!pattern.matches(year)) return false
        
        val years = year.split("-")
        val firstYear = years[0].toIntOrNull() ?: return false
        val secondYear = years[1].toIntOrNull() ?: return false
        
        // El segundo año debe ser el siguiente al primero
        return secondYear == firstYear + 1
    }
    
    /**
     * Calcula automáticamente los años de nacimiento basados en el rango de edad y el año académico
     */
    private fun calcularAniosNacimiento() {
        val edadMinima = _uiState.value.edadMinima.toIntOrNull() ?: return
        val edadMaxima = _uiState.value.edadMaxima.toIntOrNull() ?: return
        val anioAcademico = _uiState.value.anioAcademico
        
        if (!isValidAcademicYear(anioAcademico)) return
        
        val anioInicio = anioAcademico.split("-")[0].toIntOrNull() ?: return
        
        // Calculamos los años de nacimiento correspondientes al rango de edad
        val aniosCalculados = mutableListOf<Int>()
        for (edad in edadMinima..edadMaxima) {
            val anioNacimiento = anioInicio - edad
            aniosCalculados.add(anioNacimiento)
        }
        
        // Actualizamos la lista de años de nacimiento
        _uiState.update { it.copy(aniosNacimiento = aniosCalculados.sorted()) }
    }
    
    /**
     * Botón para calcular automáticamente los años de nacimiento
     */
    fun calcularAniosNacimientoAutomaticamente() {
        calcularAniosNacimiento()
    }
} 