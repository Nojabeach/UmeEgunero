package com.tfg.umeegunero.feature.familiar.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ConsultaRegistroDiarioUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registros: List<RegistroActividad> = emptyList(),
    val alumnoId: String = ""
)

@HiltViewModel
class ConsultaRegistroDiarioViewModel @Inject constructor(
    private val registroDiarioRepository: RegistroDiarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConsultaRegistroDiarioUiState())
    val uiState: StateFlow<ConsultaRegistroDiarioUiState> = _uiState.asStateFlow()
    
    /**
     * Carga los registros de un alumno
     */
    fun cargarRegistros(alumnoId: String, limit: Long = 30) {
        _uiState.update { it.copy(isLoading = true, error = null, alumnoId = alumnoId) }
        
        viewModelScope.launch {
            try {
                registroDiarioRepository.obtenerRegistrosAlumno(alumnoId, limit)
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false, 
                                        registros = result.data.sortedByDescending { reg -> reg.fecha }
                                    ) 
                                }
                            }
                            is Result.Error -> {
                                Timber.e(result.exception, "Error al obtener registros del alumno $alumnoId")
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false, 
                                        error = result.exception.message ?: "Error desconocido"
                                    ) 
                                }
                            }
                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoading = true) }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros del alumno $alumnoId")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error desconocido al cargar registros"
                    ) 
                }
            }
        }
    }
    
    /**
     * Marca un registro como visto
     */
    fun marcarComoVisto(registroId: String) {
        viewModelScope.launch {
            try {
                val result = registroDiarioRepository.marcarComoVisualizado(registroId)
                
                if (result is Result.Success) {
                    // Actualizar la lista de registros
                    _uiState.update { state ->
                        state.copy(
                            registros = state.registros.map { registro ->
                                if (registro.id == registroId) {
                                    registro.copy(
                                        vistoPorFamiliar = true,
                                        visualizadoPorFamiliar = true
                                    )
                                } else {
                                    registro
                                }
                            }
                        )
                    }
                } else if (result is Result.Error) {
                    Timber.e(result.exception, "Error al marcar registro como visto: $registroId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar registro como visto: $registroId")
            }
        }
    }
    
    /**
     * Obtiene los registros no visualizados
     */
    fun cargarRegistrosNoVisualizados(alumnosIds: List<String>) {
        if (alumnosIds.isEmpty()) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = registroDiarioRepository.obtenerRegistrosNoVisualizados(alumnosIds)
                
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                registros = result.data.sortedByDescending { reg -> reg.fecha }
                            ) 
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener registros no visualizados")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.exception.message ?: "Error desconocido"
                            ) 
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros no visualizados")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error desconocido al cargar registros"
                    ) 
                }
            }
        }
    }
} 