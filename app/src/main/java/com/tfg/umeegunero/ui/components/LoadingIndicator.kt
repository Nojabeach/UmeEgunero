package com.tfg.umeegunero.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Componente que muestra un indicador de carga.
 * Puede ocupar la pantalla completa o solo un área más pequeña.
 * Opcionalmente puede mostrar un mensaje descriptivo.
 *
 * @param isLoading Indica si se debe mostrar el indicador de carga
 * @param message Mensaje opcional a mostrar debajo del indicador
 * @param fullScreen Si el indicador debe ocupar toda la pantalla
 * @param backgroundColor Color de fondo para el indicador (solo visible si fullScreen es true)
 * @param progressColor Color del indicador de progreso
 * @param modifier Modificador para personalizar el componente
 */
@Composable
fun LoadingIndicator(
    isLoading: Boolean = true,
    message: String? = null,
    fullScreen: Boolean = false,
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f),
    progressColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (!isLoading) return
    
    if (fullScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            LoadingContent(message, progressColor)
        }
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            LoadingContent(message, progressColor)
        }
    }
}

/**
 * Contenido interno del indicador de carga
 */
@Composable
private fun LoadingContent(
    message: String?,
    progressColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = progressColor,
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )
        
        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 