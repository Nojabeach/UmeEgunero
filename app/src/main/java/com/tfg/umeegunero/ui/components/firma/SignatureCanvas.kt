package com.tfg.umeegunero.ui.components.firma

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.util.FirmaDigitalUtil

/**
 * Componente Canvas para capturar la firma digital del usuario
 * 
 * Permite al usuario dibujar una firma usando gestos táctiles y
 * captura los puntos para procesarlos posteriormente.
 * 
 * @param modifier Modificador para personalizar el aspecto del canvas
 * @param strokeWidth Grosor del trazo de la firma
 * @param strokeColor Color del trazo de la firma
 * @param backgroundColor Color de fondo del canvas
 * @param onFirmaChange Callback que se ejecuta cuando cambia la firma, recibe la lista de puntos
 */
@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    strokeColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    onFirmaChange: (List<FirmaDigitalUtil.PuntoFirma>) -> Unit = {}
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    
    // Lista con todos los trazos (cada trazo es un Path)
    var paths by remember { mutableStateOf(listOf<Path>()) }
    
    // Lista de puntos de la firma
    var puntosFirma by remember { mutableStateOf(listOf<FirmaDigitalUtil.PuntoFirma>()) }
    
    // Path actual que se está dibujando
    var currentPath by remember { mutableStateOf(Path()) }
    
    // Último punto donde se presionó
    var lastPosition by remember { mutableStateOf<Offset?>(null) }
    
    // Configuración visual del canvas
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPath = Path().apply {
                            moveTo(offset.x, offset.y)
                        }
                        lastPosition = offset
                        
                        // Añadir el primer punto
                        puntosFirma = puntosFirma + FirmaDigitalUtil.PuntoFirma(
                            x = offset.x,
                            y = offset.y,
                            presion = 1.0f
                        )
                    },
                    onDrag = { change, dragAmount ->
                        val newPosition = change.position
                        
                        // Actualizar el path actual
                        currentPath.lineTo(newPosition.x, newPosition.y)
                        
                        // Añadir el punto a la lista de puntos
                        puntosFirma = puntosFirma + FirmaDigitalUtil.PuntoFirma(
                            x = newPosition.x,
                            y = newPosition.y,
                            presion = 1.0f
                        )
                        
                        lastPosition = newPosition
                    },
                    onDragEnd = {
                        // Añadir el path actual a la lista
                        paths = paths + currentPath
                        currentPath = Path()
                        
                        // Notificar cambio en la firma
                        onFirmaChange(puntosFirma)
                    }
                )
            }
    ) {
        // Canvas para dibujar la firma
        Canvas(modifier = Modifier.matchParentSize()) {
            // Dibujar todos los paths previos (trazos completados)
            paths.forEach { path ->
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            
            // Dibujar el path actual (trazo en progreso)
            drawPath(
                path = currentPath,
                color = strokeColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
} 