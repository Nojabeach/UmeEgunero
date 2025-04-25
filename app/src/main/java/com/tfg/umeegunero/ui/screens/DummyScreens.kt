package com.tfg.umeegunero.ui.screens

import androidx.compose.runtime.Composable
import com.tfg.umeegunero.ui.components.DummyScreen

@Composable
fun DummyGestionCursos(onNavigateBack: () -> Unit) {
    DummyScreen(
        title = "Gestión de Cursos",
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun DummyGestionClases(onNavigateBack: () -> Unit) {
    DummyScreen(
        title = "Gestión de Clases",
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun DummyGestionUsuarios(onNavigateBack: () -> Unit) {
    DummyScreen(
        title = "Gestión de Usuarios",
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun DummyEstadisticas(onNavigateBack: () -> Unit) {
    DummyScreen(
        title = "Estadísticas",
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun DummyConfiguracion(onNavigateBack: () -> Unit) {
    DummyScreen(
        title = "Configuración",
        onNavigateBack = onNavigateBack
    )
} 