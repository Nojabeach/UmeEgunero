package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
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
 * Estado de la UI para la pantalla de historial de actividad.
 */
data class RegistroActividadUiState(
    val isLoading: Boolean = false,
    val registros: List<RegistroActividad> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel para la pantalla de historial de actividad del familiar.
 *
 * Gestiona la carga y el estado de los registros de actividad para un alumno específico.
 *
 * @param registroRepository Repositorio para acceder a los datos de registros.
 */
@HiltViewModel
class RegistroActividadViewModel @Inject constructor(
    private val registroRepository: RegistroDiarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroActividadUiState())
    val uiState: StateFlow<RegistroActividadUiState> = _uiState.asStateFlow()

    /**
     * Carga los registros de actividad para un alumno específico.
     *
     * @param alumnoId ID del alumno cuyos registros se cargarán.
     */
    fun cargarRegistros(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = registroRepository.getRegistrosActividadByAlumnoId(alumnoId)) { 
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            registros = result.data.sortedByDescending { reg -> reg.fecha },
                            error = null
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar registros para alumno $alumnoId")
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar registros: ${result.exception?.localizedMessage}") }
                }
                else -> { _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }
} 