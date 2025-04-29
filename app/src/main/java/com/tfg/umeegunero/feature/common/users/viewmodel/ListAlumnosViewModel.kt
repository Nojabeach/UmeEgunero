package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// Estado de UI actualizado
data class ListAlumnosUiState(
    val isLoading: Boolean = false,
    val allAlumnos: List<Alumno> = emptyList(), // Lista completa sin filtrar
    val filteredAlumnos: List<Alumno> = emptyList(), // Lista filtrada a mostrar
    val error: String? = null,
    // Estados para los filtros
    val dniFilter: String = "",
    val nombreFilter: String = "",
    val apellidosFilter: String = "",
    val cursoFilter: String = "",
    val claseFilter: String = "",
    val soloActivos: Boolean = true // Mantener filtro de activos si lo tenías
)

/**
 * ViewModel para la gestión de la pantalla de listado de alumnos
 */
@HiltViewModel
class ListAlumnosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListAlumnosUiState())
    val uiState: StateFlow<ListAlumnosUiState> = _uiState.asStateFlow()

    /**
     * Carga la lista de alumnos desde el repositorio
     */
    fun cargarAlumnos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = usuarioRepository.obtenerTodosLosAlumnos()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allAlumnos = result.data
                        )
                    }
                    // Aplicar filtros iniciales después de cargar
                    applyFilters()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar alumnos: ${result.exception?.message}"
                        )
                    }
                    Timber.e(result.exception, "Error al cargar alumnos")
                }
                is Result.Loading -> { /* Ya estamos en isLoading=true */ }
            }
        }
    }

    // --- Funciones para actualizar filtros de texto ---
    fun updateDniFilter(value: String) {
        _uiState.update { it.copy(dniFilter = value) }
        applyFilters()
    }

    fun updateNombreFilter(value: String) {
        _uiState.update { it.copy(nombreFilter = value) }
        applyFilters()
    }

    fun updateApellidosFilter(value: String) {
        _uiState.update { it.copy(apellidosFilter = value) }
        applyFilters()
    }

    fun updateCursoFilter(value: String) {
        _uiState.update { it.copy(cursoFilter = value) }
        applyFilters()
    }

    fun updateClaseFilter(value: String) {
        _uiState.update { it.copy(claseFilter = value) }
        applyFilters()
    }

    fun updateSoloActivosFilter(value: Boolean) {
        _uiState.update { it.copy(soloActivos = value) }
        applyFilters()
    }
    // --- Fin funciones de actualización ---

    // Función centralizada para aplicar todos los filtros
    private fun applyFilters() {
        viewModelScope.launch { // Lanzar en coroutine por si la lista es muy grande
            val state = _uiState.value
            val filtered = state.allAlumnos.filter { alumno ->
                (state.dniFilter.isBlank() || alumno.dni.contains(state.dniFilter, ignoreCase = true)) &&
                (state.nombreFilter.isBlank() || alumno.nombre.contains(state.nombreFilter, ignoreCase = true)) &&
                (state.apellidosFilter.isBlank() || alumno.apellidos.contains(state.apellidosFilter, ignoreCase = true)) &&
                (state.cursoFilter.isBlank() || alumno.curso.contains(state.cursoFilter, ignoreCase = true)) &&
                (state.claseFilter.isBlank() || alumno.clase.contains(state.claseFilter, ignoreCase = true)) &&
                (!state.soloActivos || alumno.activo) // Aplicar filtro activo
            }
            _uiState.update { it.copy(filteredAlumnos = filtered) }
        }
    }

    /**
     * Elimina un alumno por su ID (DNI)
     * @param alumnoId ID del alumno a eliminar
     */
    fun eliminarAlumno(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un caso real, aquí llamaríamos al repositorio para eliminar
                // Por ahora, hacemos una eliminación simulada para demostración
                _uiState.update { currentState ->
                    val alumnosActualizados = currentState.allAlumnos.filter { it.dni != alumnoId }
                    val alumnosFiltrados = if (currentState.soloActivos) {
                        alumnosActualizados.filter { (it as? Alumno)?.activo ?: true }
                    } else {
                        alumnosActualizados
                    }
                    
                    currentState.copy(
                        allAlumnos = alumnosActualizados,
                        filteredAlumnos = alumnosFiltrados,
                        isLoading = false
                    )
                }
                
                Timber.d("Alumno eliminado: $alumnoId")
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar alumno: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error al eliminar alumno $alumnoId")
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