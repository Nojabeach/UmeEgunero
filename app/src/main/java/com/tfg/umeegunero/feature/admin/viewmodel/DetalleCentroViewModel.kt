package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.CentroRepository
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

data class DetalleCentroUiState(
    val centro: Centro? = null,
    val administradores: List<Usuario> = emptyList(),
    val profesores: List<Usuario> = emptyList(),
    val numAlumnos: Int = 0,
    val numClases: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetalleCentroViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val centroId: String = checkNotNull(savedStateHandle["centroId"])
    
    private val _uiState = MutableStateFlow(DetalleCentroUiState(isLoading = true))
    val uiState: StateFlow<DetalleCentroUiState> = _uiState.asStateFlow()

    init {
        loadCentro()
    }

    fun loadCentro() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = centroRepository.getCentroById(centroId)
                
                when (result) {
                    is Result.Success -> {
                        val centro = result.data
                        _uiState.update { it.copy(centro = centro) }
                        
                        // Cargar administradores
                        loadAdministradores(centro.adminIds)
                        
                        // Cargar profesores
                        loadProfesores(centro.profesorIds)
                        
                        // Cargar estadísticas
                        loadEstadisticas(centro.id)
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.exception.message ?: "Error al cargar el centro"
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar el centro")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Error inesperado al cargar el centro"
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar el centro")
            }
        }
    }

    private fun loadAdministradores(adminIds: List<String>) {
        viewModelScope.launch {
            try {
                val admins = mutableListOf<Usuario>()
                
                for (adminId in adminIds) {
                    val result = usuarioRepository.getUsuarioById(adminId)
                    if (result is Result.Success) {
                        admins.add(result.data)
                    }
                }
                
                _uiState.update { it.copy(administradores = admins) }
                checkLoadingComplete()
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar administradores del centro")
                checkLoadingComplete()
            }
        }
    }

    private fun loadProfesores(profesorIds: List<String>) {
        viewModelScope.launch {
            try {
                val profesores = mutableListOf<Usuario>()
                
                for (profesorId in profesorIds) {
                    val result = usuarioRepository.getUsuarioById(profesorId)
                    if (result is Result.Success) {
                        profesores.add(result.data)
                    }
                }
                
                _uiState.update { it.copy(profesores = profesores) }
                checkLoadingComplete()
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar profesores del centro")
                checkLoadingComplete()
            }
        }
    }
    
    private fun loadEstadisticas(centroId: String) {
        viewModelScope.launch {
            try {
                // En una implementación real, aquí obtendríamos el número de clases y alumnos del centro
                // Por ahora, usamos datos de ejemplo
                val numClases = 5
                val numAlumnos = 120
                
                _uiState.update { 
                    it.copy(
                        numClases = numClases,
                        numAlumnos = numAlumnos
                    ) 
                }
                checkLoadingComplete()
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar estadísticas del centro")
                checkLoadingComplete()
            }
        }
    }
    
    private fun checkLoadingComplete() {
        val currentState = _uiState.value
        if (currentState.centro != null && 
            (currentState.administradores.isNotEmpty() || currentState.centro.adminIds.isEmpty()) &&
            (currentState.profesores.isNotEmpty() || currentState.centro.profesorIds.isEmpty())) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 