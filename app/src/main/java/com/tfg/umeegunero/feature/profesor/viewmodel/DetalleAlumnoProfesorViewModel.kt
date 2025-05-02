package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
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
 * ViewModel para la pantalla de detalle del alumno del profesor.
 * Gestiona el estado de UI y las operaciones relacionadas con el alumno.
 */
@HiltViewModel
open class DetalleAlumnoProfesorViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Constructor secundario sin parámetros para uso en previews
    // Nota: El mock puede necesitar ajustarse a la implementación real de UsuarioRepository
    constructor() : this(UsuarioRepository.createMock())

    // Constructor secundario que acepta un CoroutineScope para testing
    constructor(viewModelScope: CoroutineScope) : this(UsuarioRepository.createMock())

    // Estado de UI expuesto como StateFlow inmutable
    protected val _uiState = MutableStateFlow(DetalleAlumnoProfesorUiState())
    val uiState: StateFlow<DetalleAlumnoProfesorUiState> = _uiState.asStateFlow()

    /**
     * Carga la información de un alumno por su ID.
     * Se llama desde la pantalla cuando se recibe el alumnoId.
     */
    open fun loadAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Asumiendo que getAlumnoPorId existe en UsuarioRepository
                when (val result = usuarioRepository.getAlumnoPorId(alumnoId)) {
                    is Result.Success<Alumno> -> {
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
                                error = "Error al cargar alumno: ${result.exception?.message ?: "Error desconocido"}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Ya estamos en isLoading = true
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar el alumno"
                    )
                }
            }
        }
    }

    /**
     * Establece directamente el estado para vistas previas o testing.
     */
    fun setStateForPreview(state: DetalleAlumnoProfesorUiState) {
        _uiState.value = state
    }

    /**
     * Limpia el mensaje de error del estado de la UI.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Estado de UI para la pantalla de detalle del alumno del profesor.
 *
 * @property isLoading Indica si los datos del alumno se están cargando.
 * @property alumno Los datos del alumno cargados, o null si no se han cargado o hubo error.
 * @property error Mensaje de error si la carga falló, o null si no hay error.
 */
data class DetalleAlumnoProfesorUiState(
    val isLoading: Boolean = false,
    val alumno: Alumno? = null,
    val error: String? = null
) 