package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de listado de cursos
 */
data class ListCursosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cursos: List<Curso> = emptyList(),
    val centros: List<Centro> = emptyList(),
    val centroId: String = "",
    val centroSeleccionado: Centro? = null,
    val isAdminApp: Boolean = false
)

/**
 * ViewModel para la pantalla de listado de cursos
 * Gestiona la carga, actualización y eliminación de cursos
 */
@HiltViewModel
class ListCursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListCursosUiState())
    val uiState: StateFlow<ListCursosUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            val usuario = usuarioRepository.obtenerUsuarioActual()
            val isAdminApp = usuario?.perfiles?.any { it.tipo == TipoUsuario.ADMIN_APP } ?: false
            _uiState.update { it.copy(isAdminApp = isAdminApp) }

            if (!isAdminApp) {
                // Si no es admin de app, obtener su centro
                val centroId = usuario?.perfiles?.firstOrNull()?.centroId
                if (!centroId.isNullOrEmpty()) {
                    _uiState.update { it.copy(centroId = centroId) }
                    cargarCentroSeleccionado(centroId)
                }
            }
        }
    }
    
    /**
     * Carga todos los centros disponibles (solo para admin app)
     */
    fun cargarCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                centroRepository.getCentros().collect { centros ->
                    _uiState.update { 
                        it.copy(
                            centros = centros,
                            isLoading = false
                        )
                    }
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
    
    /**
     * Carga la lista de cursos del centro
     */
    fun cargarCursos() {
        viewModelScope.launch {
            if (_uiState.value.centroId.isEmpty()) {
                _uiState.update { 
                    it.copy(
                        cursos = emptyList(),
                        isLoading = false
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            try {
                val cursos = cursoRepository.obtenerCursosPorCentro(_uiState.value.centroId)
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
                        cursos = emptyList(),
                        error = "Error al cargar cursos: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Selecciona un centro (solo para admin app)
     */
    fun seleccionarCentro(centro: Centro) {
        _uiState.update { 
            it.copy(
                centroId = centro.id,
                centroSeleccionado = centro
            )
        }
        cargarCursos()
    }
    
    private suspend fun cargarCentroSeleccionado(centroId: String) {
        try {
            when (val resultado = centroRepository.getCentroById(centroId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(centroSeleccionado = resultado.data) }
                }
                is Result.Error -> {
                    Timber.e(resultado.exception, "Error al cargar centro seleccionado")
                    _uiState.update { 
                        it.copy(
                            centroSeleccionado = null,
                            error = "Error al cargar información del centro: ${resultado.exception?.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar centro seleccionado")
            _uiState.update { 
                it.copy(
                    centroSeleccionado = null,
                    error = "Error al cargar información del centro: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Elimina un curso
     * @param cursoId ID del curso a eliminar
     */
    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                cursoRepository.deleteCurso(cursoId)
                cargarCursos()
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar curso")
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar curso: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 