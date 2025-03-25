package com.tfg.umeegunero.feature.centro.viewmodel

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
import javax.inject.Inject

/**
 * Estado UI para la pantalla de añadir/editar clase
 */
data class AddClaseUiState(
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
    val capacidadMaximaError: String? = null
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
class AddClaseViewModel @Inject constructor(
    private val claseRepository: ClaseRepository,
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClaseUiState())
    val uiState: StateFlow<AddClaseUiState> = _uiState.asStateFlow()

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
        _uiState.update { it.copy(aula = aula, aulaError = null) }
    }

    /**
     * Actualiza el ID del profesor titular en el estado
     */
    fun updateProfesorTitular(profesorTitularId: String) {
        _uiState.update { it.copy(profesorTitularId = profesorTitularId, profesorTitularError = null) }
    }
    
    /**
     * Actualiza la capacidad máxima en el estado
     */
    fun updateCapacidadMaxima(capacidadMaxima: String) {
        _uiState.update { it.copy(capacidadMaxima = capacidadMaxima, capacidadMaximaError = null) }
    }

    /**
     * Añade un profesor auxiliar
     */
    fun addProfesorAuxiliar(profesorId: String) {
        _uiState.update { currentState ->
            if (!currentState.profesoresAuxiliaresIds.contains(profesorId)) {
                currentState.copy(
                    profesoresAuxiliaresIds = currentState.profesoresAuxiliaresIds + profesorId
                )
            } else {
                currentState
            }
        }
    }

    /**
     * Elimina un profesor auxiliar
     */
    fun removeProfesorAuxiliar(profesorId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                profesoresAuxiliaresIds = currentState.profesoresAuxiliaresIds.filter { it != profesorId }
            )
        }
    }

    /**
     * Carga los cursos disponibles
     */
    private fun cargarCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCursos = true) }
            try {
                val result = cursoRepository.getCursosByCentro(_uiState.value.centroId)
                when (result) {
                    is Result.Success<List<Curso>> -> {
                        _uiState.update { it.copy(cursosDisponibles = result.data, isLoadingCursos = false) }
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

    /**
     * Carga los profesores disponibles
     */
    private fun cargarProfesores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfesores = true) }
            try {
                val result = usuarioRepository.getUsersByType(TipoUsuario.PROFESOR)
                when (result) {
                    is Result.Success<List<Usuario>> -> {
                        _uiState.update { it.copy(profesoresDisponibles = result.data, isLoadingProfesores = false) }
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

    /**
     * Carga los datos de una clase existente
     */
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
                                cursoId = clase.cursoId,
                                nombre = clase.nombre,
                                horario = clase.horario,
                                aula = clase.aula,
                                profesorTitularId = clase.profesorTitularId,
                                capacidadMaxima = clase.capacidadMaxima.toString(),
                                profesoresAuxiliaresIds = clase.profesoresAuxiliaresIds,
                                isLoading = false,
                                isEditMode = true
                            )
                        }
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

    /**
     * Guarda una clase nueva o actualiza una existente
     */
    fun saveClase() {
        if (!_uiState.value.isFormValid) {
            validarFormulario()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val clase = Clase(
                    id = if (_uiState.value.isEditMode) _uiState.value.id else "",
                    cursoId = _uiState.value.cursoId,
                    centroId = _uiState.value.centroId,
                    nombre = _uiState.value.nombre,
                    horario = _uiState.value.horario,
                    aula = _uiState.value.aula,
                    profesorTitularId = _uiState.value.profesorTitularId,
                    capacidadMaxima = _uiState.value.capacidadMaxima.toIntOrNull() ?: 25,
                    activo = true,
                    profesoresAuxiliaresIds = _uiState.value.profesoresAuxiliaresIds,
                    alumnosIds = emptyList()
                )
                
                val result = claseRepository.guardarClase(clase)
                
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

    /**
     * Valida el formulario y establece los errores correspondientes
     */
    private fun validarFormulario() {
        val currentState = _uiState.value
        var hasErrors = false

        if (currentState.cursoId.isBlank()) {
            _uiState.update { it.copy(cursoError = "Debe seleccionar un curso") }
            hasErrors = true
        }

        if (currentState.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre no puede estar vacío") }
            hasErrors = true
        }

        if (currentState.profesorTitularId.isBlank()) {
            _uiState.update { it.copy(profesorTitularError = "Debe seleccionar un profesor titular") }
            hasErrors = true
        }

        if (currentState.aula.isBlank()) {
            _uiState.update { it.copy(aulaError = "El aula no puede estar vacía") }
            hasErrors = true
        }

        if (currentState.capacidadMaxima.isBlank() || currentState.capacidadMaxima.toIntOrNull() == null) {
            _uiState.update { it.copy(capacidadMaximaError = "La capacidad máxima debe ser un número válido") }
            hasErrors = true
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Resetea el estado de éxito
     */
    fun resetSuccess() {
        _uiState.update { it.copy(success = false) }
    }
} 