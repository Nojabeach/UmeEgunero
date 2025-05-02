package com.tfg.umeegunero.feature.profesor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ObservacionesProfesorScreen(
    // Aquí irán los parámetros necesarios, como NavController, ViewModel, etc.
) {
    // TODO: Implementar la UI para la gestión de observaciones
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Pantalla de Observaciones (Profesor)")
    }
}

@Preview(showBackground = true)
@Composable
fun ObservacionesProfesorScreenPreview() {
    ObservacionesProfesorScreen()
} 