package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
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

/**
 * Estado de UI para la pantalla de gestión de alumnos
 */
data class ListAlumnoUiState(
    val alumnos: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // Estados para eliminación con familiares
    val showDeleteFamiliarDialog: Boolean = false,
    val alumnoAEliminar: Usuario? = null,
    val familiaresDelAlumno: List<Usuario> = emptyList()
)

/**
 * ViewModel para la gestión de alumnos
 */
@HiltViewModel
class ListAlumnoViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val familiarRepository: FamiliarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListAlumnoUiState(isLoading = true))
    val uiState: StateFlow<ListAlumnoUiState> = _uiState.asStateFlow()

    /**
     * Carga todos los alumnos desde el repositorio
     */
    fun loadAlumnos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.getUsersByType(TipoUsuario.ALUMNO)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                alumnos = result.data,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Alumnos cargados: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al cargar los alumnos"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar los alumnos")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los alumnos"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar los alumnos")
            }
        }
    }

    /**
     * Inicia el proceso de eliminación de un alumno
     * Primero verifica si tiene familiares vinculados
     * @param alumno Usuario alumno a eliminar
     */
    fun iniciarEliminacionAlumno(alumno: Usuario) {
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
     * @param dni DNI del alumno a eliminar
     */
    private fun eliminarAlumnoDirectamente(dni: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = usuarioRepository.borrarUsuarioByDni(dni)
                
                when (result) {
                    is Result.Success -> {
                        // Recargar la lista de alumnos después de eliminar
                        loadAlumnos()
                        Timber.d("Alumno eliminado con DNI: $dni")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.exception?.message ?: "Error al eliminar el alumno"
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar el alumno")
                    }
                    is Result.Loading -> {
                        // No hacemos nada aquí ya que hemos actualizado el estado de carga antes de la llamada
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al eliminar el alumno"
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar el alumno")
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
                        
                        // Recargar la lista de alumnos
                        _uiState.update { 
                            it.copy(
                                alumnoAEliminar = null,
                                familiaresDelAlumno = emptyList()
                            ) 
                        }
                        loadAlumnos()
                        
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