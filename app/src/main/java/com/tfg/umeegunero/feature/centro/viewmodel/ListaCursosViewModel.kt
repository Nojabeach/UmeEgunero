package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Estado UI para la pantalla de lista de cursos.
 * 
 * Esta clase representa el estado completo de la interfaz de usuario para
 * la gestión de cursos, conteniendo toda la información necesaria para
 * renderizar correctamente la pantalla.
 * 
 * @property cursos Lista de cursos del centro seleccionado
 * @property centros Lista de centros disponibles
 * @property centroSeleccionado Centro actualmente seleccionado
 * @property isLoading Indica si hay operaciones en progreso
 * @property error Mensaje de error a mostrar, null si no hay errores
 * 
 * @see ListaCursosViewModel
 * @see ListaCursosScreen
 */
data class ListaCursosUiState(
    val cursos: List<Curso> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val centroSeleccionado: Centro? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la gestión de cursos en un centro educativo.
 * 
 * Este ViewModel implementa la lógica de negocio relacionada con la gestión
 * de cursos, proporcionando funcionalidades para:
 * - Listar cursos por centro
 * - Crear nuevos cursos
 * - Actualizar cursos existentes
 * - Eliminar cursos
 * - Gestionar la selección de centros
 * 
 * ## Características principales
 * - Gestión del estado de la UI mediante [StateFlow]
 * - Carga automática de datos al inicializar
 * - Manejo de errores y estados de carga
 * - Integración con repositorios de cursos y centros
 * 
 * ## Flujo de datos
 * 1. Carga inicial de centros
 * 2. Selección de centro por defecto
 * 3. Carga de cursos del centro seleccionado
 * 4. Actualización del estado UI
 * 
 * @constructor Crea una instancia del ViewModel con las dependencias necesarias
 * @param cursoRepository Repositorio para acceder a los datos de cursos
 * @param centroRepository Repositorio para acceder a los datos de centros
 * 
 * @see ListaCursosUiState
 * @see ListaCursosScreen
 */
@HiltViewModel
class ListaCursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val centroRepository: CentroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaCursosUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Carga todos los centros activos y selecciona el primero por defecto
     */
    fun cargarCentrosYSeleccionar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val centrosResult = centroRepository.getActiveCentros()
            val centros = when (centrosResult) {
                is Result.Success -> centrosResult.data
                else -> emptyList()
            }
            val centroSeleccionado = centros.firstOrNull()
            _uiState.update { it.copy(centros = centros, centroSeleccionado = centroSeleccionado, isLoading = false) }
            if (centroSeleccionado != null) {
                cargarCursos(centroSeleccionado.id)
            }
        }
    }

    /**
     * Cambia el centro seleccionado y recarga los cursos
     */
    fun seleccionarCentro(centro: Centro) {
        _uiState.update { it.copy(centroSeleccionado = centro) }
        cargarCursos(centro.id)
    }

    /**
     * Carga los cursos del centro seleccionado
     */
    fun cargarCursos(centroId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val idCentro = centroId ?: _uiState.value.centroSeleccionado?.id
                if (idCentro == null) {
                    _uiState.update { it.copy(cursos = emptyList(), isLoading = false) }
                    return@launch
                }
                val cursosResult = cursoRepository.obtenerCursosPorCentro(idCentro)
                _uiState.update { it.copy(cursos = cursosResult, isLoading = false) }
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
                val centroId = _uiState.value.centroSeleccionado?.id
                if (centroId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Selecciona un centro antes de crear un curso") }
                    return@launch
                }
                val nuevoCurso = Curso(
                    id = UUID.randomUUID().toString(),
                    nombre = nombre,
                    anioAcademico = anioAcademico,
                    descripcion = descripcion,
                    edadMinima = edadMinima,
                    edadMaxima = edadMaxima,
                    centroId = centroId
                )
                cursoRepository.agregarCurso(nuevoCurso)
                cargarCursos(centroId)
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
                val centroId = _uiState.value.centroSeleccionado?.id
                if (centroId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Selecciona un centro antes de actualizar un curso") }
                    return@launch
                }
                val cursoActualizado = Curso(
                    id = id,
                    nombre = nombre,
                    anioAcademico = anioAcademico,
                    descripcion = descripcion,
                    edadMinima = edadMinima,
                    edadMaxima = edadMaxima,
                    centroId = centroId
                )
                cursoRepository.modificarCurso(cursoActualizado)
                cargarCursos(centroId)
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
                val centroId = _uiState.value.centroSeleccionado?.id
                if (centroId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Selecciona un centro antes de eliminar un curso") }
                    return@launch
                }
                cursoRepository.borrarCurso(id)
                cargarCursos(centroId)
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