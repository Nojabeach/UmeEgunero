package com.tfg.umeegunero.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Indicador de progreso para formularios multi-etapa con botones de navegación.
 * Muestra el progreso actual del formulario y permite avanzar o retroceder.
 *
 * @param currentStep Etapa actual del formulario (comenzando desde 1)
 * @param totalSteps Número total de etapas
 * @param stepTitles Títulos para cada etapa (opcional)
 * @param onPreviousClick Acción a ejecutar al hacer clic en el botón Anterior
 * @param onNextClick Acción a ejecutar al hacer clic en el botón Siguiente/Finalizar
 * @param isLastStepCompleted Indica si la última etapa está completada (mostrará un icono de verificación)
 * @param modifier Modificador para personalizar el componente
 */
@Composable
fun FormProgressIndicatorWithNavigation(
    currentStep: Int,
    totalSteps: Int,
    stepTitles: List<String> = emptyList(),
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    isLastStepCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (isLastStepCompleted) 1f else (currentStep.toFloat() / totalSteps),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "ProgressAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Información de progreso
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Paso $currentStep de $totalSteps",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (isLastStepCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Barra de progreso
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        
        // Título de la etapa actual (si existe)
        if (stepTitles.isNotEmpty() && currentStep <= stepTitles.size) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stepTitles[currentStep - 1],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botones de navegación
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (currentStep > 1) {
                Button(
                    onClick = onPreviousClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anterior")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Button(
                onClick = onNextClick,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (currentStep < totalSteps) "Siguiente" else "Finalizar"
                )
            }
        }
    }
} 