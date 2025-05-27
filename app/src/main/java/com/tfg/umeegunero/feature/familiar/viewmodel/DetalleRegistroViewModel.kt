package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.feature.familiar.screen.DetalleRegistroUiState
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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
    fun cargarRegistro(registroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val registroResult = usuarioRepository.getRegistroById(registroId)

                when (registroResult) {
                    is Result.Success -> {
                        val registro = registroResult.data
                        // Si hay profesor, cargar su nombre
                        var profesorNombre: String? = null
                        if (registro.profesorId.isNotBlank()) {
                            val profesorResult = usuarioRepository.getUsuarioById(registro.profesorId)
                            
                            when (profesorResult) {
                                is Result.Success<*> -> {
                                    val profesor = profesorResult.data as Usuario
                                    profesorNombre = "${profesor.nombre} ${profesor.apellidos}"
                                }
                                else -> { /* No hacer nada, es un profesor opcional */ }
                            }
                        }
                        
                        // Actualizar el estado con los datos obtenidos
                        _uiState.update { it.copy(
                            isLoading = false,
                            registro = registro,
                            profesorNombre = profesorNombre
                        ) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Error al cargar registro: ${registroResult.exception?.message ?: "Error desconocido"}"
                        ) }
                    }
                    is Result.Loading -> {
                        // Mantener estado de carga
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar el registro")
                _uiState.update { it.copy(
                    error = "Error inesperado: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}