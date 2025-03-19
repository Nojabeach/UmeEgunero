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
import timber.log.Timber
import javax.inject.Inject

data class ConfiguracionUiState(
    val temaSeleccionado: TemaPref = TemaPref.SYSTEM,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Interfaz común para los ViewModel de configuración
 */
interface ConfiguracionViewModelBase {
    val uiState: StateFlow<ConfiguracionUiState>
    fun setTema(tema: TemaPref)
    fun clearError()
}

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
 * ViewModel para la configuración de la aplicación
 */
@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    repository: PreferenciasRepository
) : ViewModel(), ConfiguracionViewModelBase {
    protected val preferenciasRepository: IPreferenciasRepository = PreferenciasRepositoryAdapter(repository)

    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    override val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    init {
        Timber.d("Inicializando ConfiguracionViewModel")
        cargarPreferencias()
    }

    private fun cargarPreferencias() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                Timber.d("Cargando preferencias de tema")
                preferenciasRepository.temaPreferencia.collect { tema ->
                    Timber.d("Tema cargado: $tema")
                    _uiState.update { 
                        it.copy(
                            temaSeleccionado = tema,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar preferencias de tema")
                _uiState.update { it.copy(error = "Error al cargar preferencias: ${e.message}", isLoading = false) }
            }
        }
    }

    override fun setTema(tema: TemaPref) {
        viewModelScope.launch {
            try {
                Timber.d("Guardando tema: $tema")
                preferenciasRepository.setTemaPreferencia(tema)
                _uiState.update { it.copy(temaSeleccionado = tema) }
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar la preferencia de tema")
                _uiState.update { it.copy(error = "Error al guardar la preferencia de tema: ${e.message}") }
            }
        }
    }

    override fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * ViewModel para pruebas y previsualizaciones
 */
class TestConfiguracionViewModel(
    private val testPreferenciasRepository: IPreferenciasRepository
) : ViewModel(), ConfiguracionViewModelBase {
    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    override val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            testPreferenciasRepository.temaPreferencia.collect { tema ->
                _uiState.update { it.copy(temaSeleccionado = tema) }
            }
        }
    }
    
    override fun setTema(tema: TemaPref) {
        viewModelScope.launch {
            try {
                testPreferenciasRepository.setTemaPreferencia(tema)
                _uiState.update { it.copy(temaSeleccionado = tema) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar la preferencia de tema") }
            }
        }
    }
    
    override fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 