package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditCursoViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCursoUiState())
    val uiState: StateFlow<EditCursoUiState> = _uiState.asStateFlow()

    private val cursoId = savedStateHandle.get<String>("cursoId") ?: ""
    private val centroId = savedStateHandle.get<String>("centroId") ?: ""

    init {
        if (cursoId.isNotBlank()) {
            loadCurso()
        }
    }

    private fun loadCurso() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                when (val result = cursoRepository.getCursoById(cursoId)) {
                    is Result.Success -> {
                        val curso = result.data
                        _uiState.update { state ->
                            state.copy(
                                nombre = curso.nombre,
                                descripcion = curso.descripcion,
                                edadMinima = curso.edadMinima,
                                edadMaxima = curso.edadMaxima,
                                anioAcademico = curso.anioAcademico,
                                activo = curso.activo,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = result.exception.message ?: "Error al cargar el curso",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar el curso")
                _uiState.update { state ->
                    state.copy(
                        error = "Error al cargar el curso: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun updateDescripcion(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }

    fun updateEdadMinima(edadMinima: Int) {
        _uiState.update { it.copy(edadMinima = edadMinima) }
    }

    fun updateEdadMaxima(edadMaxima: Int) {
        _uiState.update { it.copy(edadMaxima = edadMaxima) }
    }

    fun updateAnioAcademico(anioAcademico: String) {
        _uiState.update { it.copy(anioAcademico = anioAcademico) }
    }

    fun updateActivo(activo: Boolean) {
        _uiState.update { it.copy(activo = activo) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(success = null) }
    }

    fun saveCurso() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val curso = Curso(
                    id = cursoId,
                    centroId = centroId,
                    nombre = _uiState.value.nombre,
                    descripcion = _uiState.value.descripcion,
                    edadMinima = _uiState.value.edadMinima,
                    edadMaxima = _uiState.value.edadMaxima,
                    anioAcademico = _uiState.value.anioAcademico,
                    activo = _uiState.value.activo
                )

                when (val result = cursoRepository.modificarCurso(curso)) {
                    is Result.Success<*> -> {
                        _uiState.update { state ->
                            state.copy(
                                success = "Curso actualizado correctamente",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                error = result.exception.message ?: "Error al actualizar el curso",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar el curso")
                _uiState.update { state ->
                    state.copy(
                        error = "Error al actualizar el curso: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class EditCursoUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val edadMinima: Int = 0,
    val edadMaxima: Int = 0,
    val anioAcademico: String = "",
    val activo: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
) 