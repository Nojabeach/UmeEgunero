package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI para la pantalla de registro de observaciones.
 */
data class ObservacionesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val observacionGuardada: Boolean = false
    // TODO: Añadir campos necesarios (ej. ID de alumno si es por alumno)
)

/**
 * ViewModel para la pantalla de registro de observaciones del profesor.
 *
 * TODO: Implementar la lógica para guardar una observación (texto, tipo, alumno asociado?)
 *       en el repositorio correspondiente.
 *
 * @author Maitane Ibáñez (2º DAM)
 */
@HiltViewModel
class ObservacionesProfesorViewModel @Inject constructor(
    // TODO: Inyectar repositorios necesarios (UsuarioRepository, RegistroRepository?)
) : ViewModel() {

    private val _uiState = MutableStateFlow(ObservacionesUiState())
    val uiState: StateFlow<ObservacionesUiState> = _uiState.asStateFlow()

    fun guardarObservacion(texto: String /*, otros parametros... */) {
        viewModelScope.launch {
            _uiState.value = ObservacionesUiState(isLoading = true)
            // TODO: Llamar al repositorio para guardar la observación
            // val result = repositorio.guardarObservacion(...)
            // Actualizar _uiState con el resultado (Success/Error y observacionGuardada=true)
            _uiState.value = ObservacionesUiState(isLoading = false, observacionGuardada = true) // Placeholder
        }
    }

    fun resetEstadoGuardado(){
        _uiState.value = _uiState.value.copy(observacionGuardada = false)
    }

    // TODO: Añadir función para cargar datos si la pantalla necesita mostrar observaciones previas
} 