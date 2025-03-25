package com.tfg.umeegunero.feature.common.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CentroDashboardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<CentroDashboardUiState>(CentroDashboardUiState.Initial)
    val uiState: StateFlow<CentroDashboardUiState> = _uiState

    fun navigateToGestionCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.value = CentroDashboardUiState.NavigatingToGestionCursos(centroId)
        }
    }

    fun navigateToGestionClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.value = CentroDashboardUiState.NavigatingToGestionClases(cursoId)
        }
    }

    // ... existing code ...
}

sealed class CentroDashboardUiState {
    object Initial : CentroDashboardUiState()
    data class NavigatingToGestionCursos(val centroId: String) : CentroDashboardUiState()
    data class NavigatingToGestionClases(val cursoId: String) : CentroDashboardUiState()
    // ... existing code ...
} 