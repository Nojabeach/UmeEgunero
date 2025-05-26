package com.tfg.umeegunero.feature.common.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
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
    val soloActivos: Boolean = true, // Mantener filtro de activos si lo tenías
    // Estados para eliminación con familiares
    val showDeleteFamiliarDialog: Boolean = false,
    val alumnoAEliminar: Alumno? = null,
    val familiaresDelAlumno: List<Usuario> = emptyList()
)

/**
 * ViewModel para la gestión de la pantalla de listado de alumnos
 */
@HiltViewModel
class ListAlumnosViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val familiarRepository: FamiliarRepository
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
     * Inicia el proceso de eliminación de un alumno
     * Primero verifica si tiene familiares vinculados
     * @param alumno Alumno a eliminar
     */
    fun iniciarEliminacionAlumno(alumno: Alumno) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Buscar familiares vinculados al alumno
                val familiaresResult = familiarRepository.getFamiliaresByAlumnoId(alumno.dni)
                
                when (familiaresResult) {
                    is Result.Success -> {
                        val familiares = familiaresResult.data
                        
                        if (familiares.isNotEmpty()) {
                            // Tiene familiares, mostrar diálogo para preguntar si eliminarlos
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    showDeleteFamiliarDialog = true,
                                    alumnoAEliminar = alumno,
                                    familiaresDelAlumno = familiares
                                )
                            }
                        } else {
                            // No tiene familiares, eliminar directamente
                            eliminarAlumnoDirectamente(alumno.dni)
                        }
                    }
                    is Result.Error -> {
                        Timber.w(familiaresResult.exception, "Error al buscar familiares, eliminando solo el alumno")
                        // Si hay error buscando familiares, eliminar solo el alumno
                        eliminarAlumnoDirectamente(alumno.dni)
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar familiares del alumno")
                // En caso de error, eliminar solo el alumno
                eliminarAlumnoDirectamente(alumno.dni)
            }
        }
    }

    /**
     * Elimina un alumno sin verificar familiares
     * @param alumnoId ID del alumno a eliminar
     */
    private fun eliminarAlumnoDirectamente(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = usuarioRepository.borrarUsuarioByDni(alumnoId)) {
                    is Result.Success -> {
                        // Actualización local de la lista después de eliminar
                        _uiState.update { currentState ->
                            val alumnosActualizados = currentState.allAlumnos.filter { it.dni != alumnoId }
                            
                            currentState.copy(
                                allAlumnos = alumnosActualizados,
                                isLoading = false
                            )
                        }
                        
                        // Volver a aplicar todos los filtros para actualizar la lista filtrada
                        applyFilters()
                        
                        Timber.d("Alumno eliminado correctamente: $alumnoId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar alumno: ${result.exception?.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar alumno $alumnoId")
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al eliminar alumno: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar alumno $alumnoId")
            }
        }
    }

    /**
     * Elimina un alumno y sus familiares vinculados
     * @param eliminarFamiliares Si true, elimina también los familiares
     */
    fun confirmarEliminacionConFamiliares(eliminarFamiliares: Boolean) {
        val alumno = _uiState.value.alumnoAEliminar ?: return
        val familiares = _uiState.value.familiaresDelAlumno
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    showDeleteFamiliarDialog = false,
                    error = null
                ) 
            }
            
            try {
                // Primero eliminar el alumno
                when (val result = usuarioRepository.borrarUsuarioByDni(alumno.dni)) {
                    is Result.Success -> {
                        Timber.d("Alumno ${alumno.dni} eliminado correctamente")
                        
                        // Si se solicita, eliminar también los familiares
                        if (eliminarFamiliares && familiares.isNotEmpty()) {
                            var familiaresEliminados = 0
                            var erroresFamiliares = 0
                            
                            for (familiar in familiares) {
                                try {
                                    // Verificar si el familiar tiene otros alumnos vinculados
                                    val otrosAlumnosResult = usuarioRepository.obtenerAlumnosPorFamiliar(familiar.dni)
                                    
                                    val tieneOtrosAlumnos = when (otrosAlumnosResult) {
                                        is Result.Success -> {
                                            // Filtrar el alumno que acabamos de eliminar
                                            otrosAlumnosResult.data.any { it.dni != alumno.dni }
                                        }
                                        else -> false
                                    }
                                    
                                    if (!tieneOtrosAlumnos) {
                                        // El familiar no tiene otros alumnos, se puede eliminar
                                        when (val familiarResult = usuarioRepository.borrarUsuarioByDni(familiar.dni)) {
                                            is Result.Success -> {
                                                familiaresEliminados++
                                                Timber.d("Familiar ${familiar.dni} eliminado correctamente")
                                            }
                                            is Result.Error -> {
                                                erroresFamiliares++
                                                Timber.e(familiarResult.exception, "Error al eliminar familiar ${familiar.dni}")
                                            }
                                            else -> { /* No hacer nada */ }
                                        }
                                    } else {
                                        Timber.d("Familiar ${familiar.dni} tiene otros alumnos vinculados, no se elimina")
                                    }
                                } catch (e: Exception) {
                                    erroresFamiliares++
                                    Timber.e(e, "Error al procesar familiar ${familiar.dni}")
                                }
                            }
                            
                            val mensaje = when {
                                erroresFamiliares == 0 && familiaresEliminados > 0 -> 
                                    "Alumno y $familiaresEliminados familiar(es) eliminados correctamente"
                                erroresFamiliares > 0 && familiaresEliminados > 0 -> 
                                    "Alumno y $familiaresEliminados familiar(es) eliminados. $erroresFamiliares familiar(es) no se pudieron eliminar"
                                erroresFamiliares > 0 && familiaresEliminados == 0 -> 
                                    "Alumno eliminado. No se pudieron eliminar los familiares"
                                else -> "Alumno eliminado correctamente"
                            }
                            
                            Timber.d(mensaje)
                        }
                        
                        // Actualizar la lista local
                        _uiState.update { currentState ->
                            val alumnosActualizados = currentState.allAlumnos.filter { it.dni != alumno.dni }
                            
                            currentState.copy(
                                allAlumnos = alumnosActualizados,
                                isLoading = false,
                                alumnoAEliminar = null,
                                familiaresDelAlumno = emptyList()
                            )
                        }
                        
                        // Volver a aplicar filtros
                        applyFilters()
                        
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar alumno: ${result.exception?.message}",
                                isLoading = false,
                                alumnoAEliminar = null,
                                familiaresDelAlumno = emptyList()
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar alumno ${alumno.dni}")
                    }
                    else -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                alumnoAEliminar = null,
                                familiaresDelAlumno = emptyList()
                            ) 
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al eliminar: ${e.message}",
                        isLoading = false,
                        alumnoAEliminar = null,
                        familiaresDelAlumno = emptyList()
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar alumno y familiares")
            }
        }
    }

    /**
     * Cancela el diálogo de eliminación de familiares
     */
    fun cancelarEliminacionFamiliares() {
        _uiState.update { 
            it.copy(
                showDeleteFamiliarDialog = false,
                alumnoAEliminar = null,
                familiaresDelAlumno = emptyList()
            ) 
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 