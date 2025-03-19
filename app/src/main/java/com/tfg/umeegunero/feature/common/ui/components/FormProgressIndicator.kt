package com.tfg.umeegunero.feature.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Componente que muestra el progreso de un formulario multi-paso
 * @param currentStep el paso actual del formulario
 * @param totalSteps el número total de pasos del formulario
 * @param stepNames los nombres de cada paso (opcional)
 */
@Composable
fun FormProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepNames: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mostrar el indicador de cada paso
        for (i in 0 until totalSteps) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep
            
            // Indicador visual (círculo)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (i + 1).toString(),
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.onPrimary
                        isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Si hay un nombre definido para este paso, mostrarlo
            if (stepNames.isNotEmpty() && i < stepNames.size) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stepNames[i],
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.onBackground
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Agregar una línea entre los indicadores, excepto después del último
            if (i < totalSteps - 1) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
} 