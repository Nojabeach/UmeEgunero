package com.tfg.umeegunero.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente que muestra un globo de notificaciones pendientes
 * 
 * @param count Número de notificaciones pendientes
 * @param modifier Modificador para personalizar el componente
 * @param backgroundColor Color de fondo del globo
 * @param textColor Color del texto
 * @param maxCount Número máximo a mostrar antes de mostrar "+"
 */
@Composable
fun NotificationBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.error,
    textColor: Color = MaterialTheme.colorScheme.onError,
    maxCount: Int = 99
) {
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(
            animationSpec = tween(300),
            initialScale = 0.3f
        ) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(
            animationSpec = tween(300),
            targetScale = 0.3f
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        val scale by animateFloatAsState(
            targetValue = if (count > 0) 1f else 0f,
            animationSpec = tween(300),
            label = "badge_scale"
        )
        
        Box(
            modifier = Modifier
                .scale(scale)
                .size(20.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count <= maxCount) count.toString() else "$maxCount+",
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Componente que envuelve otro componente y añade un globo de notificaciones
 * en la esquina superior derecha
 * 
 * @param count Número de notificaciones pendientes
 * @param content Contenido al que añadir el globo
 * @param badgeOffset Desplazamiento del globo desde la esquina
 */
@Composable
fun BadgedBox(
    count: Int,
    modifier: Modifier = Modifier,
    badgeOffset: androidx.compose.ui.unit.DpOffset = androidx.compose.ui.unit.DpOffset((-4).dp, 4.dp),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        
        NotificationBadge(
            count = count,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(badgeOffset.x, badgeOffset.y)
        )
    }
} 