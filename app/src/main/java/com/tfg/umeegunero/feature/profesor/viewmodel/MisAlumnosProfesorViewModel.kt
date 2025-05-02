package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI para la pantalla "Mis Alumnos".
 */
data class MisAlumnosUiState(
    val isLoading: Boolean = false,
    val alumnos: List<Alumno> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla "Mis Alumnos" del profesor.
 *
 * TODO: Implementar la carga de la lista de alumnos asignados al profesor
 *       desde el repositorio correspondiente.
 *
 * @author Maitane Ibáñez (2º DAM)
 */
@HiltViewModel
class MisAlumnosProfesorViewModel @Inject constructor(
    // TODO: Inyectar UsuarioRepository u otro repositorio necesario
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisAlumnosUiState())
    val uiState: StateFlow<MisAlumnosUiState> = _uiState.asStateFlow()

    init {
        cargarAlumnos()
    }

    private fun cargarAlumnos() {
        viewModelScope.launch {
            _uiState.value = MisAlumnosUiState(isLoading = true)
            // TODO: Llamar al repositorio para obtener la lista de alumnos
            // val result = usuarioRepository.getAlumnosDelProfesorActual(...)
            // Actualizar _uiState con el resultado (Success o Error)
             _uiState.value = MisAlumnosUiState(isLoading = false, alumnos = emptyList()) // Placeholder
        }
    }

    // TODO: Añadir funciones si se necesita filtrar o buscar alumnos
} 