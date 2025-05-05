package com.tfg.umeegunero.feature.admin.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.AdminColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.People
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.Dp
import com.tfg.umeegunero.ui.components.CategoriaCardData
import androidx.compose.ui.graphics.graphicsLayer
import timber.log.Timber

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AdminColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Card de categoría para dashboards de administración.
 * Compacta, moderna y accesible, con animaciones sutiles y feedback háptico.
 *
 * @param titulo Título principal del card
 * @param descripcion Descripción breve de la funcionalidad
 * @param icono Icono representativo
 * @param color Color principal del icono
 * @param onClick Acción al pulsar el card
 * @param modifier Modificador opcional
 */
@Composable
fun CategoriaCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    border: Boolean = true,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 8.dp else 3.dp,
        animationSpec = tween(durationMillis = 120), label = "elevacion"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120), label = "escala"
    )
    val alpha = if (enabled) 1f else 0.5f
    
    Card(
        modifier = modifier
            .width(160.dp)
            .height(120.dp)
            .scale(scale)
            .graphicsLayer(alpha = alpha)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(elevation),
        border = if (border) BorderStroke(1.dp, color.copy(alpha = if(enabled) 0.35f else 0.15f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if(enabled) 0.18f else 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = (iconTint ?: color).copy(alpha = if(enabled) 1f else 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = LocalContentColor.current.copy(alpha = if(enabled) 1f else 0.38f)
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if(enabled) 1f else 0.38f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Botón de acción para dashboards, compacto y coherente con CategoriaCard.
 * Incluye animaciones sutiles y feedback háptico.
 *
 * @param icono Icono principal
 * @param texto Texto del botón
 * @param onClick Acción al pulsar
 * @param modifier Modificador opcional
 */
@Composable
fun BotonAccion(
    icono: ImageVector,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 3.dp,
        animationSpec = tween(durationMillis = 120), label = "elevacion"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120), label = "escala"
    )
    
    Card(
        modifier = modifier
            .width(160.dp)
            .height(120.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AdminColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = texto,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Previsualización de CategoriaCard y BotonAccion para revisión visual y documentación.
 */
@Preview(showBackground = true)
@Composable
fun PreviewCategoriaCard() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CategoriaCard(
            titulo = "Centros",
            descripcion = "Gestión de centros",
            icono = Icons.Default.Business,
            color = AdminColor,
            onClick = {}
        )
        BotonAccion(
            icono = Icons.Default.People,
            texto = "Usuarios",
            onClick = {}
        )
    }
}

/**
 * Elemento del dashboard que muestra un título, subtítulo e icono.
 * Diseñado para ser usado en el dashboard del administrador.
 *
 * @param title Título del elemento
 * @param subtitle Subtítulo o descripción
 * @param icon Icono representativo
 * @param onClick Acción al hacer clic
 * @param modifier Modificador opcional
 */
@Composable
fun DashboardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Textos
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoriaCardBienvenida(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = color,
    borderWidth: Dp = 1.dp, // Más estrecho que CategoriaCard
    content: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                content?.invoke()
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Permite crear una CategoriaCardBienvenida directamente desde un objeto CategoriaCardData.
 * Así se facilita la integración y la reutilización en los dashboards.
 */
@Composable
fun CategoriaCardBienvenida(
    data: CategoriaCardData,
    modifier: Modifier = Modifier,
    borderColor: Color = data.borderColor ?: data.color,
    borderWidth: Dp = if (data.borderWidth != Dp.Unspecified) data.borderWidth else 1.dp,
    content: @Composable (() -> Unit)? = data.extraContent
) {
    CategoriaCardBienvenida(
        titulo = data.titulo,
        descripcion = data.descripcion,
        icono = data.icono,
        color = data.color,
        iconTint = data.iconTint ?: Color.White,
        modifier = modifier.then(data.modifier),
        borderColor = borderColor,
        borderWidth = borderWidth,
        content = content
    )
} 