package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.repository.CursosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ListaCursosUiState(
    val cursos: List<Curso> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ListaCursosViewModel @Inject constructor(
    private val cursosRepository: CursosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaCursosUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Carga todos los cursos disponibles desde el repositorio
     */
    fun cargarCursos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val cursos = cursosRepository.getAllCursos()
                _uiState.update { it.copy(cursos = cursos, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al cargar los cursos: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Crea un nuevo curso
     */
    fun crearCurso(
        nombre: String,
        anioAcademico: String,
        descripcion: String,
        edadMinima: Int,
        edadMaxima: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val nuevoCurso = Curso(
                    id = UUID.randomUUID().toString(),
                    nombre = nombre,
                    anioAcademico = anioAcademico,
                    descripcion = descripcion,
                    edadMinima = edadMinima,
                    edadMaxima = edadMaxima
                )
                
                cursosRepository.crearCurso(nuevoCurso)
                
                // Recargar la lista de cursos
                cargarCursos()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al crear el curso: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Actualiza un curso existente
     */
    fun actualizarCurso(
        id: String,
        nombre: String,
        anioAcademico: String,
        descripcion: String,
        edadMinima: Int,
        edadMaxima: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val cursoActualizado = Curso(
                    id = id,
                    nombre = nombre,
                    anioAcademico = anioAcademico,
                    descripcion = descripcion,
                    edadMinima = edadMinima,
                    edadMaxima = edadMaxima
                )
                
                cursosRepository.actualizarCurso(cursoActualizado)
                
                // Recargar la lista de cursos
                cargarCursos()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al actualizar el curso: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Elimina un curso por su ID
     */
    fun eliminarCurso(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                cursosRepository.eliminarCurso(id)
                
                // Recargar la lista de cursos
                cargarCursos()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al eliminar el curso: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 