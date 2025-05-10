package com.tfg.umeegunero.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modelo de datos para representar una tarjeta de categoría en los dashboards.
 * Permite máxima flexibilidad y personalización visual y funcional.
 *
 * @param titulo Título principal de la tarjeta
 * @param descripcion Descripción breve o subtítulo
 * @param icono Icono representativo (ImageVector)
 * @param color Color principal de la tarjeta o del icono
 * @param onClick Acción a ejecutar al pulsar la tarjeta
 * @param modifier Modificador opcional para la tarjeta
 * @param iconTint Color opcional para tintar el icono (por defecto null, usa color)
 */
data class CategoriaCardData(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
    val modifier: Modifier = Modifier,
    val iconTint: Color? = null
) 