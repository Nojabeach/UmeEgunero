package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Pantalla de detalle de tarea
 */
@Composable
fun DetalleTareaScreen(
    navController: NavController,
    tareaId: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Detalle de tarea $tareaId - En construcción")
    }
} 