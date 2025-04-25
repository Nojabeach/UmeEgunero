package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de gestión de cursos
 */
data class GestionCursosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cursos: List<Curso> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val centroSeleccionado: Centro? = null,
    val isAdminApp: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val selectedCurso: Curso? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)

/**
 * ViewModel para la gestión de cursos
 */
@HiltViewModel
class GestionCursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GestionCursosUiState())
    val uiState: StateFlow<GestionCursosUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActual()
            val isAdminApp = usuario?.perfiles?.any { it.tipo == TipoUsuario.ADMIN_APP } ?: false
            _uiState.update { it.copy(isAdminApp = isAdminApp) }
            
            if (isAdminApp) {
                cargarCentros()
            } else {
                // Si no es admin, cargar su centro asignado
                usuario?.perfiles?.firstOrNull()?.centroId?.let { centroId ->
                    seleccionarCentro(centroId)
                }
            }
        }
    }
    
    private fun cargarCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val centros = centroRepository.getCentros().first()
                _uiState.update { 
                    it.copy(
                        centros = centros,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar centros")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar centros: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun seleccionarCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val centroResult = centroRepository.getCentroById(centroId)
                val centro = if (centroResult is Result.Success) centroResult.data else null
                _uiState.update { 
                    it.copy(
                        centroSeleccionado = centro,
                        isLoading = false
                    )
                }
                cargarCursos(centroId)
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar centro")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar centro: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun cargarCursos(centroId: String) {
        viewModelScope.launch {
            try {
                val cursos = cursoRepository.obtenerCursosPorCentro(centroId)
                _uiState.update { 
                    it.copy(
                        cursos = cursos,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar cursos")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar cursos: ${e.message}",
                        isLoading = false,
                        cursos = emptyList()
                    )
                }
            }
        }
    }
    
    /**
     * Guarda un curso en Firestore
     */
    fun guardarCurso(curso: Curso) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null) 
            }
            try {
                val centroId = _uiState.value.centroSeleccionado?.id ?: return@launch
                val cursoCompleto = curso.copy(
                    centroId = centroId,
                    activo = true
                )
                val result = cursoRepository.saveCurso(cursoCompleto)
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isSuccess = true,
                                successMessage = "Curso guardado correctamente",
                                isLoading = false
                            ) 
                        }
                        cargarCursos(centroId)
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al guardar el curso: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al guardar curso en Firestore")
                    }
                    else -> {
                        // State Loading, ignoramos
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al guardar curso en Firestore")
            }
        }
    }
    
    /**
     * Elimina un curso de Firestore
     */
    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(isLoading = true, error = null) 
            }
            try {
                val result = cursoRepository.deleteCurso(cursoId)
                val centroId = _uiState.value.centroSeleccionado?.id ?: return@launch
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isSuccess = true,
                                successMessage = "Curso eliminado correctamente",
                                isLoading = false,
                                isDeleteDialogVisible = false,
                                selectedCurso = null
                            ) 
                        }
                        cargarCursos(centroId)
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar el curso: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar curso de Firestore")
                    }
                    else -> {
                        // State Loading, ignoramos
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al eliminar curso: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar curso")
            }
        }
    }
    
    /**
     * Muestra el diálogo de confirmación de eliminación
     */
    fun mostrarDialogoEliminar(curso: Curso) {
        _uiState.update { 
            it.copy(
                selectedCurso = curso,
                isDeleteDialogVisible = true
            ) 
        }
    }
    
    /**
     * Oculta el diálogo de confirmación de eliminación
     */
    fun ocultarDialogoEliminar() {
        _uiState.update { 
            it.copy(
                isDeleteDialogVisible = false,
                selectedCurso = null
            ) 
        }
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    fun clearSuccess() {
        _uiState.update { 
            it.copy(
                isSuccess = false,
                successMessage = null
            ) 
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 