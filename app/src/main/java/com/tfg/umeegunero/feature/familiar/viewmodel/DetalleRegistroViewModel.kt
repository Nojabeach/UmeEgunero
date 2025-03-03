package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.repository.Result
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
 * Estado UI para la pantalla de detalle de registro de actividad
 */
data class DetalleRegistroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registro: RegistroActividad? = null,
    val profesorNombre: String? = null
)

/**
 * ViewModel para la pantalla de detalle de registro de actividad de un alumno
 */
@HiltViewModel
class DetalleRegistroViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleRegistroUiState())
    val uiState: StateFlow<DetalleRegistroUiState> = _uiState.asStateFlow()

    init {
        // Obtener el ID del registro de la navegación
        val registroId = savedStateHandle.get<String>("registroId")

        if (registroId != null) {
            cargarRegistro(registroId)
        } else {
            _uiState.update {
                it.copy(error = "No se pudo obtener el ID del registro")
            }
        }
    }

    /**
     * Carga los datos del registro de actividad
     */
    private fun cargarRegistro(registroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val registroResult = usuarioRepository.getRegistroById(registroId)

                when (registroResult) {
                    is Result.Success -> {
                        val registro = registroResult.data

                        // Asegurarnos que el modelo de datos se procesa correctamente
                        // Por ejemplo, si hay alguna inconsistencia en las observaciones
                        val procesedRegister = when {
                            registro.observaciones is List<*> -> registro
                            else -> registro.copy(observaciones = emptyList()) // Asegurar que observaciones es una lista
                        }

                        _uiState.update {
                            it.copy(
                                registro = procesedRegister,
                                isLoading = false
                            )
                        }

                        // Cargamos el nombre del profesor
                        cargarProfesor(registro.profesorId)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar el registro: ${registroResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(registroResult.exception, "Error al cargar registro")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar registro")
            }
        }
    }

    /**
     * Carga los datos del profesor que creó el registro
     */
    private fun cargarProfesor(profesorId: String) {
        if (profesorId.isBlank()) return

        viewModelScope.launch {
            try {
                val profesorResult = usuarioRepository.getUsuarioPorDni(profesorId)

                when (profesorResult) {
                    is Result.Success -> {
                        val profesor = profesorResult.data
                        _uiState.update {
                            it.copy(
                                profesorNombre = "${profesor.nombre} ${profesor.apellidos}"
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(profesorResult.exception, "Error al cargar profesor")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar profesor")
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