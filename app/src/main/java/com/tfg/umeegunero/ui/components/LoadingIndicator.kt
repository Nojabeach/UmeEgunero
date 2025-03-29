package com.tfg.umeegunero.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Indicador de carga con posibilidad de mostrar un mensaje.
 *
 * @param isLoading Si se debe mostrar el indicador de carga
 * @param modifier Modificador para personalizar el componente
 * @param message Mensaje opcional para mostrar bajo el indicador
 * @param fullScreen Si debe ocupar toda la pantalla con fondo semitransparente
 */
@Composable
fun LoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null,
    fullScreen: Boolean = false
) {
    if (!isLoading) return
    
    // Animación para hacer pulsar el indicador
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingTransition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "LoadingAlpha"
    )
    
    if (fullScreen) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
        ) {
            LoadingContent(alpha, message, modifier)
        }
    } else {
        LoadingContent(alpha, message, modifier)
    }
}

@Composable
private fun LoadingContent(
    alpha: Float,
    message: String?,
    modifier: Modifier
) {
    Box(
        modifier = modifier.then(Modifier.padding(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .alpha(alpha),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            
            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
} 