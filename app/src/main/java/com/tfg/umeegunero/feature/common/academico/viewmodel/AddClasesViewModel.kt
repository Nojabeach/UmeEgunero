package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Estado UI para la pantalla de añadir/editar clase
 */
data class AddClasesUiState(
    val id: String = "",
    val cursoId: String = "",
    val nombre: String = "",
    val horario: String = "",
    val aula: String = "",
    val profesorTitularId: String = "",
    val capacidadMaxima: String = "25",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isEditMode: Boolean = false,
    val centroId: String = "",
    val cursosDisponibles: List<Curso> = emptyList(),
    val profesoresDisponibles: List<Usuario> = emptyList(),
    val profesoresAuxiliaresIds: List<String> = emptyList(),
    val isLoadingCursos: Boolean = false,
    val isLoadingProfesores: Boolean = false,
    val cursoError: String? = null,
    val nombreError: String? = null,
    val profesorTitularError: String? = null,
    val aulaError: String? = null,
    val capacidadMaximaError: String? = null,
    val profesores: List<Usuario> = emptyList(),
    val isSuccess: Boolean = false,
    val successMessage: String? = null
) {
    val isFormValid: Boolean
        get() = nombre.isNotBlank() && 
                cursoId.isNotBlank() && 
                profesorTitularId.isNotBlank() &&
                aula.isNotBlank() &&
                capacidadMaxima.isNotBlank()
}

/**
 * ViewModel para añadir o editar una clase
 */
@HiltViewModel
class AddClasesViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClasesUiState())
    val uiState: StateFlow<AddClasesUiState> = _uiState.asStateFlow()

    /**
     * Establece el ID del centro
     */
    fun setCentroId(centroId: String) {
        _uiState.update { it.copy(centroId = centroId) }
        cargarCursos()
        cargarProfesores()
    }

    /**
     * Actualiza el ID del curso en el estado
     */
    fun updateCursoId(cursoId: String) {
        _uiState.update { it.copy(cursoId = cursoId, cursoError = null) }
    }

    /**
     * Actualiza el nombre en el estado
     */
    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre, nombreError = null) }
    }

    /**
     * Actualiza el horario en el estado
     */
    fun updateHorario(horario: String) {
        _uiState.update { it.copy(horario = horario) }
    }
    
    /**
     * Actualiza el aula en el estado
     */
    fun updateAula(aula: String) {
        val error = if (aula.isBlank()) "El aula es obligatoria" else null
        _uiState.update { it.copy(aula = aula, aulaError = error) }
    }

    /**
     * Actualiza el profesor titular en el estado
     */
    fun updateProfesorTitular(profesorId: String) {
        val error = if (profesorId.isBlank()) "El profesor titular es obligatorio" else null
        _uiState.update { it.copy(profesorTitularId = profesorId, profesorTitularError = error) }
    }

    /**
     * Actualiza la capacidad máxima en el estado
     */
    fun updateCapacidadMaxima(capacidad: String) {
        val error = when {
            capacidad.isBlank() -> "La capacidad máxima es obligatoria"
            !isNumeric(capacidad) -> "La capacidad debe ser un número"
            capacidad.toIntOrNull() ?: 0 <= 0 -> "La capacidad debe ser mayor que 0"
            else -> null
        }
        _uiState.update { it.copy(capacidadMaxima = capacidad, capacidadMaximaError = error) }
    }

    /**
     * Actualiza los profesores auxiliares en el estado
     */
    fun updateProfesoresAuxiliares(profesorId: String, isSelected: Boolean) {
        _uiState.update { currentState ->
            val newProfesoresAuxiliares = if (isSelected) {
                currentState.profesoresAuxiliaresIds + profesorId
            } else {
                currentState.profesoresAuxiliaresIds - profesorId
            }
            currentState.copy(profesoresAuxiliaresIds = newProfesoresAuxiliares)
        }
    }

    /**
     * Carga los cursos disponibles para el centro
     */
    private fun cargarCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCursos = true) }
            when (val result = cursoRepository.getCursosByCentro(_uiState.value.centroId)) {
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
                            error = result.exception.message,
                            isLoadingCursos = false
                        )
                    }
                }
                is Result.Loading -> {
                    // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                }
            }
        }
    }

    /**
     * Carga los profesores disponibles para el centro
     */
    private fun cargarProfesores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfesores = true) }
            when (val result = usuarioRepository.getProfesoresByCentro(_uiState.value.centroId)) {
                is Result.Success -> {
                    val profesores = result.data.filterIsInstance<Usuario>()
                    _uiState.update { 
                        it.copy(
                            profesoresDisponibles = profesores,
                            isLoadingProfesores = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.exception.message,
                            isLoadingProfesores = false
                        )
                    }
                }
                is Result.Loading -> {
                    // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                }
            }
        }
    }

    /**
     * Carga una clase existente para edición
     */
    fun loadClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = claseRepository.getClaseById(claseId)) {
                is Result.Success<*> -> {
                    val clase = result.data as Clase
                    _uiState.update { 
                        it.copy(
                            id = clase.id,
                            cursoId = clase.cursoId,
                            nombre = clase.nombre,
                            horario = clase.horario,
                            aula = clase.aula,
                            profesorTitularId = clase.profesorTitularId,
                            capacidadMaxima = clase.capacidadMaxima.toString(),
                            profesoresAuxiliaresIds = clase.profesoresAuxiliaresIds,
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.exception.message,
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                }
            }
        }
    }

    /**
     * Guarda la clase
     */
    fun saveClase() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val clase = Clase(
                id = if (_uiState.value.isEditMode) _uiState.value.id else UUID.randomUUID().toString(),
                cursoId = _uiState.value.cursoId,
                nombre = _uiState.value.nombre,
                horario = _uiState.value.horario,
                aula = _uiState.value.aula,
                profesorTitularId = _uiState.value.profesorTitularId,
                capacidadMaxima = _uiState.value.capacidadMaxima.toInt(),
                profesoresAuxiliaresIds = _uiState.value.profesoresAuxiliaresIds
            )

            when (val result = claseRepository.guardarClase(clase)) {
                is Result.Success<*> -> {
                    _uiState.update { it.copy(success = true, isLoading = false) }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = result.exception.message,
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                }
            }
        }
    }

    private fun isNumeric(str: String): Boolean {
        return str.matches("-?\\d+(\\.\\d+)?".toRegex())
    }
} 