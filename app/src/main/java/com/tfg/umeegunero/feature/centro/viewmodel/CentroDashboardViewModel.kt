package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CentroDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CentroDashboardViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CentroDashboardUiState())
    val uiState: StateFlow<CentroDashboardUiState> = _uiState.asStateFlow()
} 