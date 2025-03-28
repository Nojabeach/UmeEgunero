package com.tfg.umeegunero.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Componente para mostrar un gráfico de líneas
 * @param data Lista de valores a representar
 * @param labels Lista de etiquetas para cada punto
 * @param modifier Modificador para personalizar el aspecto
 * @param lineColor Color de la línea
 * @param fillColor Color de relleno bajo la línea (opcional)
 */
@Composable
fun LineChart(
    data: List<Float>,
    labels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = lineColor.copy(alpha = 0.1f)
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
        val minValue = data.minOrNull() ?: 0f
        val range = (maxValue - minValue).coerceAtLeast(1f)
        
        // Calcular el espacio entre puntos
        val xStep = canvasWidth / (data.size - 1)
        
        // Crear el path para la línea
        val linePath = Path()
        // Crear el path para el área bajo la línea
        val fillPath = Path()
        
        // Iniciar el path en el primer punto
        val firstX = 0f
        val firstY = canvasHeight - ((data[0] - minValue) / range * canvasHeight)
        linePath.moveTo(firstX, firstY)
        fillPath.moveTo(firstX, canvasHeight)
        fillPath.lineTo(firstX, firstY)
        
        // Añadir puntos al path
        for (i in 1 until data.size) {
            val x = i * xStep
            val y = canvasHeight - ((data[i] - minValue) / range * canvasHeight)
            linePath.lineTo(x, y)
            fillPath.lineTo(x, y)
        }
        
        // Cerrar el path de relleno
        fillPath.lineTo(canvasWidth, canvasHeight)
        fillPath.close()
        
        // Dibujar el relleno
        drawPath(
            path = fillPath,
            color = fillColor
        )
        
        // Dibujar la línea
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
        
        // Dibujar puntos en cada valor
        for (i in data.indices) {
            val x = i * xStep
            val y = canvasHeight - ((data[i] - minValue) / range * canvasHeight)
            
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
} 