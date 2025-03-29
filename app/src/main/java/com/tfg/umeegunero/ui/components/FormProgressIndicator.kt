package com.tfg.umeegunero.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview

/**
 * Componente que muestra un indicador de progreso para formularios multi-paso.
 * 
 * Este componente proporciona una barra de progreso visual que indica el avance del usuario
 * a través de un formulario de varios pasos o un proceso secuencial. Puede mostrar opcionalmente
 * etiquetas para el paso actual y el total de pasos, y es personalizable en términos de colores y tamaños.
 *
 * @param currentStep El paso actual en el que se encuentra el usuario (comienza desde 1)
 * @param totalSteps El número total de pasos en el formulario
 * @param modifier Modificador opcional para personalizar el diseño del componente
 * @param showLabels Si se deben mostrar etiquetas indicando el paso actual y el total (por defecto: true)
 * @param showStepText Si se debe mostrar el texto "Paso X de Y" (por defecto: true)
 * @param progressColor Color personalizado para la barra de progreso (por defecto: usa el color primario del tema)
 * @param trackColor Color personalizado para el fondo de la barra de progreso (por defecto: usa el color de superficie del tema)
 * @param stepLabel Etiqueta personalizada para mostrar antes del número de paso (por defecto: "Paso")
 * @param height Altura personalizada para la barra de progreso (por defecto: 8.dp)
 */
@Composable
fun FormProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showStepText: Boolean = true,
    progressColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    trackColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    stepLabel: String = "Paso",
    height: androidx.compose.ui.unit.Dp = 8.dp
) {
    // Validación de parámetros
    val validCurrentStep = currentStep.coerceIn(1, totalSteps)
    val progress = validCurrentStep.toFloat() / totalSteps.toFloat()
    
    // Animación del progreso
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "ProgressAnimation"
    )
    
    Column(modifier = modifier) {
        // Mostrar etiquetas si está habilitado
        if (showLabels) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Etiqueta para el paso actual
                if (showStepText) {
                    Text(
                        text = "$stepLabel $validCurrentStep de $totalSteps",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Porcentaje de finalización
                Text(
                    text = "${(progress * 100).toInt()}% completado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Barra de progreso
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            trackColor = trackColor,
            color = progressColor
        )
    }
}

/**
 * Vista previa del componente FormProgressIndicator.
 * 
 * Muestra tres ejemplos del indicador de progreso con diferentes configuraciones:
 * 1. Con etiquetas predeterminadas
 * 2. Sin etiquetas
 * 3. Con etiqueta personalizada
 */
@Preview(showBackground = true)
@Composable
fun FormProgressIndicatorPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Ejemplo 1: Con etiquetas predeterminadas
            Text(
                text = "Con etiquetas predeterminadas",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            FormProgressIndicator(
                currentStep = 2,
                totalSteps = 4,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Ejemplo 2: Sin etiquetas
            Text(
                text = "Sin etiquetas",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            FormProgressIndicator(
                currentStep = 3,
                totalSteps = 5,
                showLabels = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Ejemplo 3: Con etiqueta personalizada
            Text(
                text = "Con etiqueta personalizada",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            FormProgressIndicator(
                currentStep = 1,
                totalSteps = 3,
                stepLabel = "Etapa",
                progressColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 