package com.tfg.umeegunero.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/**
 * Modelo de datos para representar una tarjeta de categoría en los dashboards.
 * Permite máxima flexibilidad y personalización visual y funcional.
 *
 * @param titulo Título principal de la tarjeta
 * @param descripcion Descripción breve o subtítulo
 * @param icono Icono representativo (ImageVector)
 * @param color Color principal de la tarjeta o del icono
 * @param onClick Acción a ejecutar al pulsar la tarjeta
 * @param iconTint Color opcional para tintar el icono (por defecto null, usa color)
 * @param border Indica si la tarjeta debe mostrar borde (por defecto true)
 * @param modifier Modificador opcional para la tarjeta
 * @param borderColor Color del borde (por defecto igual que color)
 * @param borderWidth Grosor del borde (por defecto 1.dp)
 * @param extraContent Contenido composable adicional opcional (por ejemplo, badges, chips, etc)
 */
data class CategoriaCardData(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
    val iconTint: Color? = null,
    val border: Boolean = true,
    val modifier: Modifier = Modifier,
    val borderColor: Color? = null,
    val borderWidth: Dp = Dp.Unspecified,
    val extraContent: (@Composable (() -> Unit))? = null
) 