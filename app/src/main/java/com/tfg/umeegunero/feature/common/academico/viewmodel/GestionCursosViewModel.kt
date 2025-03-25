package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.domain.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de cursos
 * Maneja la lógica de negocio relacionada con los cursos
 */
class GestionCursosViewModel : ViewModel() {
    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Carga los cursos del centro especificado
     * @param centroId ID del centro del que se quieren cargar los cursos
     */
    fun cargarCursos(centroId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para cargar los cursos
                // _cursos.value = cursoRepository.getCursosByCentroId(centroId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Añade un nuevo curso
     * @param curso Curso a añadir
     */
    fun añadirCurso(curso: Curso) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para añadir el curso
                // cursoRepository.addCurso(curso)
                // Recargar la lista de cursos
                cargarCursos(curso.centroId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un curso existente
     * @param curso Curso a actualizar
     */
    fun actualizarCurso(curso: Curso) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para actualizar el curso
                // cursoRepository.updateCurso(curso)
                // Recargar la lista de cursos
                cargarCursos(curso.centroId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un curso
     * @param cursoId ID del curso a eliminar
     * @param centroId ID del centro al que pertenece el curso
     */
    fun eliminarCurso(cursoId: String, centroId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // TODO: Implementar la llamada al repositorio para eliminar el curso
                // cursoRepository.deleteCurso(cursoId)
                // Recargar la lista de cursos
                cargarCursos(centroId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 