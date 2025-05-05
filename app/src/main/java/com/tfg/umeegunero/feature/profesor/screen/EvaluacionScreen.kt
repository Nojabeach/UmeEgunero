package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Pantalla de evaluación académica
 */
@Composable
fun EvaluacionScreen(
    navController: NavController,
    alumnos: List<String> = emptyList()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pantalla de evaluación académica - En construcción")
    }
} 