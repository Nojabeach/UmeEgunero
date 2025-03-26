package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.repository.Result
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
 * Estado de UI para la pantalla de añadir/editar curso
 */
data class AddCursosUiState(
    val nombre: String = "",
    val anioAcademico: String = "",
    val edadMinima: String = "",
    val edadMaxima: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)

/**
 * ViewModel para la pantalla de añadir/editar curso
 */
@HiltViewModel
class AddCursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCursosUiState())
    val uiState: StateFlow<AddCursosUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el nombre del curso
     */
    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    /**
     * Actualiza el año académico del curso
     */
    fun updateAnioAcademico(anio: String) {
        _uiState.update { it.copy(anioAcademico = anio) }
    }

    /**
     * Actualiza la edad mínima del curso
     */
    fun updateEdadMinima(edad: String) {
        _uiState.update { it.copy(edadMinima = edad) }
    }

    /**
     * Actualiza la edad máxima del curso
     */
    fun updateEdadMaxima(edad: String) {
        _uiState.update { it.copy(edadMaxima = edad) }
    }

    /**
     * Carga los datos de un curso existente
     */
    fun loadCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = cursoRepository.getCursoById(cursoId)
                
                when (result) {
                    is Result.Success<*> -> {
                        val curso = result.data as Curso
                        _uiState.update { 
                            it.copy(
                                nombre = curso.nombre,
                                anioAcademico = curso.anioAcademico,
                                edadMinima = curso.edadMinima.toString(),
                                edadMaxima = curso.edadMaxima.toString(),
                                isLoading = false
                            ) 
                        }
                        Timber.d("Curso cargado: ${curso.nombre}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al cargar el curso"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar el curso")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar el curso"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar el curso")
            }
        }
    }

    /**
     * Guarda un curso
     */
    fun guardarCurso(curso: Curso) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = cursoRepository.saveCurso(curso)
                
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                successMessage = "Curso guardado correctamente"
                            ) 
                        }
                        Timber.d("Curso guardado: ${curso.nombre}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Error al guardar el curso"
                            ) 
                        }
                        Timber.e(result.exception, "Error al guardar el curso")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al guardar el curso"
                    ) 
                }
                Timber.e(e, "Error inesperado al guardar el curso")
            }
        }
    }

    /**
     * Limpia el error actual
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia el mensaje de éxito
     */
    fun limpiarExito() {
        _uiState.update { it.copy(isSuccess = false, successMessage = null) }
    }
} 