package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
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
    val cursos: List<Curso> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val centroId: String = ""
)

/**
 * ViewModel para la pantalla de listado de cursos
 * Gestiona la carga, actualización y eliminación de cursos
 */
@HiltViewModel
class ListCursosViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListCursosUiState())
    val uiState: StateFlow<ListCursosUiState> = _uiState.asStateFlow()
    
    init {
        // Obtener el centro del usuario actual
        viewModelScope.launch {
            val centroId = getCentroIdFromCurrentUser()
            centroId?.let {
                _uiState.update { state ->
                    state.copy(centroId = it)
                }
                cargarCursos()
            }
        }
    }
    
    /**
     * Obtiene el ID del centro del usuario actual
     */
    private suspend fun getCentroIdFromCurrentUser(): String? {
        // Obtener el usuario actual
        val currentUser = usuarioRepository.auth.currentUser
        if (currentUser == null) {
            Timber.d("No hay usuario autenticado")
            return null
        }
        
        // Buscar el usuario en Firestore usando uid en lugar de email
        val uid = currentUser.uid
        when (val result = usuarioRepository.getUsuarioById(uid)) {
            is Result.Success -> {
                val usuario = result.data
                // Obtener el primer centroId de sus perfiles
                val centroId = usuario.perfiles.firstOrNull()?.centroId
                Timber.d("Centro ID obtenido: $centroId")
                return centroId
            }
            is Result.Error -> {
                Timber.e(result.exception, "Error al obtener usuario")
                return null
            }
            is Result.Loading -> {
                Timber.d("Cargando usuario...")
                return null
            }
        }
    }
    
    /**
     * Carga la lista de cursos del centro
     */
    fun cargarCursos() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val centroId = _uiState.value.centroId
            if (centroId.isEmpty()) {
                // Si no hay centro seleccionado, muestra un error
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "No se ha seleccionado un centro"
                    )
                }
                return@launch
            }
            
            // Obtener los cursos del centro
            when (val result = cursoRepository.getCursosByCentro(centroId)) {
                is Result.Success -> {
                    Timber.d("Cursos cargados: ${result.data.size}")
                    _uiState.update { 
                        it.copy(
                            cursos = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar cursos")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar cursos: ${result.exception.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Elimina un curso
     * @param cursoId ID del curso a eliminar
     */
    fun eliminarCurso(cursoId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            when (val result = cursoRepository.borrarCurso(cursoId)) {
                is Result.Success -> {
                    Timber.d("Curso eliminado: $cursoId")
                    // Actualizar la lista después de eliminar
                    cargarCursos()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al eliminar curso")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al eliminar curso: ${result.exception.message}"
                        )
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
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