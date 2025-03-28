package com.tfg.umeegunero.feature.admin.components

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
 * Componente para mostrar estadísticas en una tarjeta
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
 * Componente para mostrar un conjunto de estadísticas
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
 * Modelo para un elemento de estadística
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