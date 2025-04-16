package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.centro.viewmodel.VincularProfesorClaseViewModel
import com.tfg.umeegunero.feature.common.screen.DummyScreen

/**
 * Pantalla para vincular profesores a clases
 */
@Composable
fun VincularProfesorClaseScreen(
    viewModel: VincularProfesorClaseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    DummyScreen(
        title = "Vincular Profesores y Clases",
        description = "Esta funcionalidad está siendo implementada y estará disponible próximamente. Disculpe las molestias.",
        onNavigateBack = onNavigateBack
    )
} 