package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI para el gestor académico
 data class GestorAcademicoUiState(
    val centros: List<Centro> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val selectedCentro: Centro? = null,
    val selectedCurso: Curso? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val centroMenuExpanded: Boolean = false,
    val cursoMenuExpanded: Boolean = false
)

@HiltViewModel
class GestorAcademicoViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository,
    private val claseRepository: ClaseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GestorAcademicoUiState())
    val uiState: StateFlow<GestorAcademicoUiState> = _uiState.asStateFlow()

    init {
        cargarCentros()
    }

    fun cargarCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val centrosResult = centroRepository.getAllCentros()
                val centros = if (centrosResult is com.tfg.umeegunero.util.Result.Success) centrosResult.data else emptyList()
                _uiState.update { it.copy(centros = centros, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar centros: ${e.message}", isLoading = false) }
            }
        }
    }

    fun onCentroSelected(centro: Centro) {
        _uiState.update { it.copy(selectedCentro = centro, selectedCurso = null) }
        cargarCursos(centro.id)
    }

    fun cargarCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cursos = cursoRepository.obtenerCursosPorCentro(centroId)
                _uiState.update { it.copy(cursos = cursos, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar cursos: ${e.message}", isLoading = false) }
            }
        }
    }

    fun onCursoSelected(curso: Curso) {
        _uiState.update { it.copy(selectedCurso = curso) }
        cargarClases(curso.id)
    }

    fun cargarClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val clasesResult = claseRepository.getClasesByCursoId(cursoId)
                val clases = if (clasesResult is com.tfg.umeegunero.util.Result.Success) clasesResult.data else emptyList()
                _uiState.update { it.copy(clases = clases, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cargar clases: ${e.message}", isLoading = false) }
            }
        }
    }

    fun onCentroMenuExpandedChanged(expanded: Boolean) {
        _uiState.update { it.copy(centroMenuExpanded = expanded) }
    }
    fun onCursoMenuExpandedChanged(expanded: Boolean) {
        _uiState.update { it.copy(cursoMenuExpanded = expanded) }
    }
} 