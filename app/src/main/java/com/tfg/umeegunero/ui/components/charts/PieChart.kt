package com.tfg.umeegunero.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Componente para mostrar un gráfico circular
 * @param data Lista de valores a representar
 * @param colors Lista de colores para cada segmento
 * @param modifier Modificador para personalizar el aspecto
 * @param strokeWidth Ancho del borde de cada segmento, si es nulo se rellena completamente
 */
@Composable
fun PieChart(
    data: List<Float>,
    colors: List<Color> = emptyList(),
    modifier: Modifier = Modifier,
    strokeWidth: Float? = null
) {
    if (data.isEmpty()) return
    
    // Calcular la suma total
    val total = data.sum()
    
    // Obtener colores para cada segmento
    val segmentColors = if (colors.size >= data.size) {
        colors
    } else {
        // Generar colores aleatorios si no hay suficientes
        List(data.size) { index ->
            colors.getOrElse(index) {
                val hue = (index * 360f / data.size) % 360f
                Color.hsl(hue, 0.7f, 0.5f)
            }
        }
    }
    
    Canvas(
        modifier = modifier.size(200.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = min(canvasWidth, canvasHeight) / 2f
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        
        // Empezamos en los -90 grados (arriba)
        var startAngle = -90f
        var sweepAngle: Float
        
        data.forEachIndexed { index, value ->
            sweepAngle = (value / total) * 360f
            
            if (strokeWidth != null) {
                // Dibujamos el arco con un borde
                drawArc(
                    color = segmentColors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
            } else {
                // Dibujamos el arco relleno
                drawArc(
                    color = segmentColors[index],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
            
            startAngle += sweepAngle
        }
    }
} 