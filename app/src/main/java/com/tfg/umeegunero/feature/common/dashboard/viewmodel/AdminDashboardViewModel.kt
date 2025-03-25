package com.tfg.umeegunero.feature.common.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Initial)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState

    fun navigateToGestionCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState.NavigatingToGestionCursos(centroId)
        }
    }

    fun navigateToGestionClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState.NavigatingToGestionClases(cursoId)
        }
    }

    // ... existing code ...
}

sealed class AdminDashboardUiState {
    object Initial : AdminDashboardUiState()
    data class NavigatingToGestionCursos(val centroId: String) : AdminDashboardUiState()
    data class NavigatingToGestionClases(val cursoId: String) : AdminDashboardUiState()
    // ... existing code ...
} 