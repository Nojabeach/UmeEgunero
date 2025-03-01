package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Estado UI para la pantalla de añadir/editar clase
 */
data class AddClaseUiState(
    // Información de la clase
    val id: String = "",
    val centroId: String = "",
    val cursoId: String = "",
    val cursoError: String? = null,
    val nombre: String = "",
    val nombreError: String? = null,
    val profesorTitularId: String = "",
    val profesorTitularError: String? = null,
    val profesoresAuxiliaresIds: List<String> = emptyList(),
    val capacidadMaxima: String = "25",
    val capacidadMaximaError: String? = null,
    val horario: String = "",
    val aula: String = "",
    val aulaError: String? = null,
    
    // Listas para selección
    val cursosDisponibles: List<Curso> = emptyList(),
    val profesoresDisponibles: List<Usuario> = emptyList(),
    val profesoresSeleccionados: List<Usuario> = emptyList(),
    
    // Estado de la UI
    val isLoadingCursos: Boolean = false,
    val isLoadingProfesores: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isEditMode: Boolean = false
) {
    val isFormValid: Boolean
        get() = cursoId.isNotBlank() && cursoError == null &&
                nombre.isNotBlank() && nombreError == null &&
                profesorTitularId.isNotBlank() && profesorTitularError == null &&
                capacidadMaxima.isNotBlank() && capacidadMaximaError == null &&
                aula.isNotBlank() && aulaError == null
}

@HiltViewModel
class AddClaseViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClaseUiState())
    val uiState: StateFlow<AddClaseUiState> = _uiState.asStateFlow()

    fun setCentroId(centroId: String) {
        _uiState.update { it.copy(centroId = centroId) }
        loadCursos(centroId)
        loadProfesores(centroId)
    }

    fun updateNombre(nombre: String) {
        val error = if (nombre.isBlank()) "El nombre es obligatorio" else null
        _uiState.update { it.copy(nombre = nombre, nombreError = error) }
    }

    fun updateCursoId(cursoId: String) {
        val error = if (cursoId.isBlank()) "Debe seleccionar un curso" else null
        _uiState.update { it.copy(cursoId = cursoId, cursoError = error) }
    }

    fun updateProfesorTitular(profesorId: String) {
        val error = if (profesorId.isBlank()) "Debe seleccionar un profesor titular" else null
        _uiState.update { it.copy(profesorTitularId = profesorId, profesorTitularError = error) }
    }

    fun updateProfesoresAuxiliares(profesoresIds: List<String>) {
        _uiState.update { it.copy(profesoresAuxiliaresIds = profesoresIds) }
    }

    fun addProfesorAuxiliar(profesorId: String) {
        if (profesorId.isBlank() || _uiState.value.profesoresAuxiliaresIds.contains(profesorId)) return
        
        val updatedList = _uiState.value.profesoresAuxiliaresIds + profesorId
        _uiState.update { it.copy(profesoresAuxiliaresIds = updatedList) }
    }

    fun removeProfesorAuxiliar(profesorId: String) {
        val updatedList = _uiState.value.profesoresAuxiliaresIds.filter { it != profesorId }
        _uiState.update { it.copy(profesoresAuxiliaresIds = updatedList) }
    }

    fun updateCapacidadMaxima(capacidad: String) {
        val error = when {
            capacidad.isBlank() -> "La capacidad máxima es obligatoria"
            !isNumeric(capacidad) -> "La capacidad debe ser un número"
            capacidad.toIntOrNull() ?: 0 <= 0 -> "La capacidad debe ser mayor que cero"
            else -> null
        }
        _uiState.update { it.copy(capacidadMaxima = capacidad, capacidadMaximaError = error) }
    }

    fun updateHorario(horario: String) {
        _uiState.update { it.copy(horario = horario) }
    }

    fun updateAula(aula: String) {
        val error = if (aula.isBlank()) "El aula es obligatoria" else null
        _uiState.update { it.copy(aula = aula, aulaError = error) }
    }

    fun loadCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCursos = true) }
            
            try {
                val result = cursoRepository.getCursosByCentro(centroId)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                cursosDisponibles = result.data,
                                isLoadingCursos = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingCursos = false,
                                error = result.exception.message ?: "Error al cargar los cursos"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingCursos = false,
                        error = e.message ?: "Error inesperado al cargar los cursos"
                    )
                }
            }
        }
    }

    fun loadProfesores(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfesores = true) }
            
            try {
                val result = usuarioRepository.getProfesoresByCentro(centroId)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                profesoresDisponibles = result.data,
                                isLoadingProfesores = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingProfesores = false,
                                error = result.exception.message ?: "Error al cargar los profesores"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingProfesores = false,
                        error = e.message ?: "Error inesperado al cargar los profesores"
                    )
                }
            }
        }
    }

    fun loadClase(claseId: String) {
        if (claseId.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = claseRepository.getClaseById(claseId)
                
                when (result) {
                    is Result.Success -> {
                        val clase = result.data
                        _uiState.update {
                            it.copy(
                                id = clase.id,
                                centroId = clase.centroId,
                                cursoId = clase.cursoId,
                                nombre = clase.nombre,
                                profesorTitularId = clase.profesorTitularId,
                                profesoresAuxiliaresIds = clase.profesoresAuxiliaresIds,
                                capacidadMaxima = clase.capacidadMaxima.toString(),
                                horario = clase.horario,
                                aula = clase.aula,
                                isLoading = false,
                                isEditMode = true
                            )
                        }
                        
                        // Cargar datos relacionados
                        loadCursos(clase.centroId)
                        loadProfesores(clase.centroId)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar la clase"
                            )
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar la clase"
                    )
                }
            }
        }
    }

    fun saveClase() {
        if (!_uiState.value.isFormValid) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val clase = Clase(
                    id = if (_uiState.value.isEditMode) _uiState.value.id else UUID.randomUUID().toString(),
                    cursoId = _uiState.value.cursoId,
                    centroId = _uiState.value.centroId,
                    nombre = _uiState.value.nombre,
                    profesorTitularId = _uiState.value.profesorTitularId,
                    profesoresAuxiliaresIds = _uiState.value.profesoresAuxiliaresIds,
                    capacidadMaxima = _uiState.value.capacidadMaxima.toIntOrNull() ?: 25,
                    horario = _uiState.value.horario,
                    aula = _uiState.value.aula,
                    activo = true
                )
                
                val result = if (_uiState.value.isEditMode) {
                    claseRepository.updateClase(clase)
                } else {
                    claseRepository.createClase(clase)
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
                                error = result.exception.message ?: "Error al guardar la clase"
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
                        error = e.message ?: "Error inesperado al guardar la clase"
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
} 