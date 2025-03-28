package com.tfg.umeegunero.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Componente para mostrar un gráfico de barras
 * @param data Lista de valores a representar
 * @param modifier Modificador para personalizar el aspecto
 * @param barColor Color de las barras
 * @param barWidth Ancho de cada barra (en porcentaje del espacio disponible)
 */
@Composable
fun BarChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    barWidth: Float = 0.7f // Porcentaje del espacio disponible
) {
    if (data.isEmpty()) return
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Encontrar el valor máximo para escalar
        val maxValue = data.maxOrNull() ?: 1f
        
        // Calcular el ancho de cada columna y barra
        val columnWidth = canvasWidth / data.size
        val barWidthPx = columnWidth * barWidth
        
        // Dibujar cada barra
        for (i in data.indices) {
            val barHeight = (data[i] / maxValue) * canvasHeight
            val left = i * columnWidth + (columnWidth - barWidthPx) / 2
            val top = canvasHeight - barHeight
            
            // Dibujar la barra
            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidthPx, barHeight)
            )
        }
    }
} 