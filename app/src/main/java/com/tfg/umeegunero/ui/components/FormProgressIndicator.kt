package com.tfg.umeegunero.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Componente para mostrar el progreso en formularios multi-paso.
 * 
 * Muestra una barra de progreso con indicación visual del progreso actual.
 * 
 * @param currentStep Paso actual del formulario
 * @param totalSteps Total de pasos en el formulario
 * @param modifier Modificador opcional para personalizar el componente
 * @param showLabel Indica si se debe mostrar el texto con el porcentaje
 * @param color Color principal de la barra de progreso
 */
@Composable
fun FormProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val progress = (currentStep.toFloat() / totalSteps).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "ProgressAnimation"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showLabel) {
            Text(
                text = "${(progress * 100).toInt()}% completado",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Barra de progreso estilizada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
        
        // Indicadores de pasos
        if (totalSteps <= 10) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..totalSteps) {
                    val isCompleted = i <= currentStep
                    val stepColor = if (isCompleted) color else MaterialTheme.colorScheme.surfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(stepColor)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
} 