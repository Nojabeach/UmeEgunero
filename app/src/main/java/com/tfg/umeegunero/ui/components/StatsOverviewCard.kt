package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.AccessibilityUtils.accessibleClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School

/**
 * Componente reutilizable que muestra una tarjeta con información estadística.
 * 
 * Este componente presenta una estadística individual en forma de tarjeta visualmente
 * atractiva y accesible. Está diseñado para mostrar métricas clave de forma clara y
 * concisa, con un diseño consistente en toda la aplicación.
 * 
 * Características:
 * - Diseño elevado con sombra para destacar visualmente
 * - Icono personalizable para representar el tipo de estadística
 * - Color temático configurable para codificar visualmente diferentes categorías
 * - Soporte completo de accesibilidad con descripciones semánticas
 * - Interactividad opcional para navegar a vistas detalladas
 * 
 * Este componente se utiliza principalmente en dashboards administrativos y paneles
 * de control para mostrar KPIs (Key Performance Indicators) y métricas importantes.
 * 
 * @param title Título descriptivo de la estadística
 * @param value Valor numérico o textual de la estadística
 * @param icon Icono vectorial que representa visualmente la categoría de la estadística
 * @param color Color temático para el icono y el valor (debe contrastar con el fondo)
 * @param onClick Callback opcional que se ejecuta cuando el usuario pulsa la tarjeta
 * @param modifier Modificador de Compose para personalizar el layout
 * 
 * @see StatsOverviewRow Para mostrar múltiples estadísticas en fila
 * @see StatItem Para el modelo de datos que representa una estadística
 */
@Composable
fun StatsOverviewCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Descripción accesible
    val accessibilityDescription = "$title: $value"
    
    Card(
        modifier = modifier
            .accessibleClickable(
                description = accessibilityDescription,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Incluido en la descripción principal
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clearAndSetSemantics {} // Incluido en la descripción principal
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.clearAndSetSemantics {} // Incluido en la descripción principal
            )
        }
    }
}

/**
 * Componente contenedor que muestra una fila de tarjetas de estadísticas.
 * 
 * Este componente organiza múltiples [StatsOverviewCard] en una fila horizontal
 * con espaciado uniforme, adaptándose automáticamente al ancho disponible.
 * Facilita la visualización de múltiples métricas relacionadas en un formato
 * compacto y visualmente coherente.
 * 
 * El componente distribuye el espacio equitativamente entre todas las tarjetas,
 * asegurando un diseño equilibrado independientemente del número de estadísticas.
 * 
 * Casos de uso típicos:
 * - Dashboards administrativos
 * - Paneles de control de usuarios
 * - Resúmenes de actividad y rendimiento
 * - Headers de secciones con métricas clave
 *
 * @param stats Lista de [StatItem] que definen las estadísticas a mostrar
 * @param modifier Modificador de Compose para personalizar el layout
 * 
 * @see StatsOverviewCard Para la visualización individual de cada estadística
 * @see StatItem Para el modelo de datos que representa una estadística
 */
@Composable
fun StatsOverviewRow(
    stats: List<StatItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stats.forEach { stat ->
            StatsOverviewCard(
                title = stat.title,
                value = stat.value,
                icon = stat.icon,
                color = stat.color,
                onClick = stat.onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Modelo de datos para representar un elemento de estadística individual.
 * 
 * Esta clase de datos encapsula toda la información necesaria para representar
 * visualmente una estadística en la interfaz de usuario. Define tanto los aspectos
 * visuales (título, valor, icono, color) como el comportamiento interactivo (onClick).
 * 
 * Al separar los datos de la presentación, este modelo permite:
 * - Mayor flexibilidad en la fuente de datos
 * - Facilidad para transformar datos del backend en representaciones visuales
 * - Testabilidad mejorada de la lógica de presentación
 * - Reutilización en diferentes contextos de UI
 *
 * @property title Título descriptivo corto de la estadística
 * @property value Valor formateado como texto (puede incluir unidades o formato específico)
 * @property icon Icono vectorial que representa visualmente la categoría
 * @property color Color temático para destacar visualmente (debe seguir la paleta de la app)
 * @property onClick Callback que se ejecuta cuando el usuario interactúa con la estadística
 * 
 * @see StatsOverviewCard Para la representación visual de este modelo
 * @see StatsOverviewRow Para mostrar colecciones de estas estadísticas
 */
data class StatItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit = {}
)

@Preview(showBackground = true)
@Composable
fun StatsOverviewCardPreview() {
    UmeEguneroTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsOverviewCard(
                    title = "Centros",
                    value = "15",
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} 