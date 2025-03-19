package com.tfg.umeegunero.feature.common.config.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfiguracionUiState(
    val temaSeleccionado: TemaPref = TemaPref.SYSTEM,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Una interfaz para facilitar los tests del ConfiguracionViewModel
 */
interface IPreferenciasRepository {
    val temaPreferencia: Flow<TemaPref>
    suspend fun setTemaPreferencia(tema: TemaPref)
}

/**
 * Adaptador para PreferenciasRepository
 */
class PreferenciasRepositoryAdapter(
    private val repository: PreferenciasRepository
) : IPreferenciasRepository {
    override val temaPreferencia = repository.temaPreferencia
    override suspend fun setTemaPreferencia(tema: TemaPref) = repository.setTemaPreferencia(tema)
}

/**
 * Base abstracta del ConfiguracionViewModel
 */
abstract class ConfiguracionViewModelBase : ViewModel() {
    protected abstract val preferenciasRepository: IPreferenciasRepository

    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    init {
        cargarPreferencias()
    }

    private fun cargarPreferencias() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            preferenciasRepository.temaPreferencia.collect { tema ->
                _uiState.update { 
                    it.copy(
                        temaSeleccionado = tema,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setTema(tema: TemaPref) {
        viewModelScope.launch {
            try {
                preferenciasRepository.setTemaPreferencia(tema)
                _uiState.update { it.copy(temaSeleccionado = tema) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar la preferencia de tema") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * ViewModel para producción (inyectado por Hilt)
 */
@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    repository: PreferenciasRepository
) : ConfiguracionViewModelBase() {
    override val preferenciasRepository: IPreferenciasRepository = PreferenciasRepositoryAdapter(repository)
}

/**
 * ViewModel para pruebas y previsualizaciones
 */
class TestConfiguracionViewModel(
    override val preferenciasRepository: IPreferenciasRepository
) : ConfiguracionViewModelBase() 