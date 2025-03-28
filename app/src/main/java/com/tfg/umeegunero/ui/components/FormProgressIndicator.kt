package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Componente para mostrar el progreso en formularios multi-paso.
 * 
 * Muestra una barra de progreso con el paso actual y el total.
 * 
 * @param currentStep Paso actual del formulario
 * @param totalSteps Total de pasos en el formulario
 * @param modifier Modificador opcional para personalizar el componente
 * @param pasoLabel Texto personalizado para mostrar en lugar de "Paso"
 */
@Composable
fun FormProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    pasoLabel: String = "Paso"
) {
    val progress = currentStep.toFloat() / totalSteps
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$pasoLabel $currentStep de $totalSteps",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            color = MaterialTheme.colorScheme.primary,
            strokeCap = StrokeCap.Round
        )
    }
} 