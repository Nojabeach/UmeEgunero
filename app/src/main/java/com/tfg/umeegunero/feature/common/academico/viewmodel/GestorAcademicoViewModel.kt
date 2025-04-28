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
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// Estado de la UI para el gestor académico
 data class GestorAcademicoUiState(
    val centros: List<Centro> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val selectedCentro: Centro? = null,
    val selectedCurso: Curso? = null,
    val isLoadingCentros: Boolean = false,
    val isLoadingCursos: Boolean = false,
    val isLoadingClases: Boolean = false,
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
            _uiState.update { it.copy(isLoadingCentros = true) }
            try {
                when (val centrosResult = centroRepository.getAllCentros()) {
                    is Result.Success -> {
                        _uiState.update { it.copy(centros = centrosResult.data, isLoadingCentros = false) }
                        centrosResult.data.firstOrNull()?.let { primerCentro ->
                            onCentroSelected(primerCentro) 
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al cargar centros: ${centrosResult.exception?.message}", isLoadingCentros = false) }
                    }
                    else -> { /* Loading state is handled */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al cargar centros")
                _uiState.update { it.copy(error = "Error inesperado al cargar centros: ${e.message}", isLoadingCentros = false) }
            }
        }
    }

    fun onCentroSelected(centro: Centro) {
        _uiState.update { it.copy(selectedCentro = centro, selectedCurso = null, cursos = emptyList(), clases = emptyList()) }
        observarCursos(centro.id)
    }

    private fun observarCursos(centroId: String) {
        _uiState.update { it.copy(isLoadingCursos = true, error = null) }
        
        cursoRepository.obtenerCursosPorCentroFlow(centroId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        Timber.d("Cursos actualizados para centro $centroId: ${result.data.size}")
                        _uiState.update { it.copy(cursos = result.data, isLoadingCursos = false) }
                        if (_uiState.value.selectedCurso == null && result.data.isNotEmpty()) {
                            onCursoSelected(result.data.first())
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al observar cursos del centro $centroId")
                        _uiState.update { it.copy(error = result.exception?.message ?: "Error al cargar cursos", isLoadingCursos = false) }
                    }
                    is Result.Loading -> {
                         _uiState.update { it.copy(isLoadingCursos = true) }
                    }
                }
            }
            .catch { e -> 
                Timber.e(e, "Excepción en el Flow de cursos del centro $centroId")
                _uiState.update { it.copy(error = e.message ?: "Error inesperado", isLoadingCursos = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onCursoSelected(curso: Curso) {
        _uiState.update { it.copy(selectedCurso = curso, clases = emptyList()) }
        observarClases(curso.id)
    }

    private fun observarClases(cursoId: String) {
        _uiState.update { it.copy(isLoadingClases = true, error = null) }
        
        claseRepository.obtenerClasesPorCursoFlow(cursoId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        Timber.d("Clases actualizadas para curso $cursoId: ${result.data.size}")
                        _uiState.update { it.copy(clases = result.data, isLoadingClases = false) }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al observar clases del curso $cursoId")
                        _uiState.update { it.copy(error = result.exception?.message ?: "Error al cargar clases", isLoadingClases = false) }
                    }
                    is Result.Loading -> {
                         _uiState.update { it.copy(isLoadingClases = true) }
                    }
                }
            }
            .catch { e -> 
                Timber.e(e, "Excepción en el Flow de clases del curso $cursoId")
                _uiState.update { it.copy(error = e.message ?: "Error inesperado", isLoadingClases = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onCentroMenuExpandedChanged(expanded: Boolean) {
        _uiState.update { it.copy(centroMenuExpanded = expanded) }
    }
    fun onCursoMenuExpandedChanged(expanded: Boolean) {
        _uiState.update { it.copy(cursoMenuExpanded = expanded) }
    }

    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCursos = true) }
            try {
                when (val result = cursoRepository.deleteCurso(cursoId)) {
                    is Result.Success -> {
                         _uiState.update { it.copy(isLoadingCursos = false) }
                        Timber.d("Curso $cursoId eliminado correctamente.")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al eliminar curso: ${result.exception?.message}", isLoadingCursos = false) }
                    }
                    else -> { _uiState.update { it.copy(isLoadingCursos = false) } }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al eliminar curso")
                _uiState.update { it.copy(error = "Error inesperado al eliminar curso: ${e.message}", isLoadingCursos = false) }
            }
        }
    }

    fun eliminarClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingClases = true) }
            try {
                when (val result = claseRepository.eliminarClase(claseId)) {
                    is Result.Success<*> -> {
                         _uiState.update { it.copy(isLoadingClases = false) }
                         Timber.d("Clase $claseId eliminada correctamente.")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al eliminar clase: ${result.exception?.message}", isLoadingClases = false) }
                    }
                    else -> { _uiState.update { it.copy(isLoadingClases = false) } }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al eliminar clase")
                _uiState.update { it.copy(error = "Error inesperado al eliminar clase: ${e.message}", isLoadingClases = false) }
            }
        }
    }
} 