package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalle del alumno
 * Gestiona el estado de UI y las operaciones relacionadas con el alumno
 */
@HiltViewModel
open class StudentDetailViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    // Constructor secundario sin parámetros para uso en previews
    constructor() : this(UsuarioRepository.createMock())
    
    // Constructor secundario que acepta un CoroutineScope para testing
    constructor(viewModelScope: CoroutineScope) : this(UsuarioRepository.createMock())
    
    // Estado de UI expuesto como StateFlow inmutable
    protected val _uiState = MutableStateFlow(StudentDetailUiState())
    val uiState: StateFlow<StudentDetailUiState> = _uiState.asStateFlow()
    
    /**
     * Carga la información de un alumno por su ID
     */
    open fun loadAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = usuarioRepository.getAlumnoPorId(alumnoId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            alumno = result.data
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Error al cargar el alumno"
                        )
                    }
                }
                Result.Loading -> { /* No hacemos nada, ya estamos en estado de carga */ }
            }
        }
    }
    
    /**
     * Establece directamente el estado para vistas previas
     */
    fun setStateForPreview(state: StudentDetailUiState) {
        _uiState.value = state
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Estado de UI para la pantalla de detalle del alumno
 */
data class StudentDetailUiState(
    val isLoading: Boolean = false,
    val alumno: Alumno? = null,
    val error: String? = null
) 