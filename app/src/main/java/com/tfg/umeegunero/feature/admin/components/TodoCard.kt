package com.tfg.umeegunero.feature.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.AccessibilityUtils.accessibleClickable

/**
 * Componente reutilizable para mostrar tarjetas de tareas o acciones pendientes
 * con información de progreso y prioridad
 */
@Composable
fun TodoCard(
    title: String,
    description: String,
    icon: ImageVector,
    progress: Float = 1f,
    isHighPriority: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Crear descripción accesible para la tarjeta
    val progressText = when {
        progress >= 1f -> "Completado"
        progress > 0f -> "En progreso: ${(progress * 100).toInt()}%"
        else -> "Pendiente"
    }
    
    val priorityText = if (isHighPriority) "Alta prioridad" else "Prioridad normal"
    
    val accessibilityDescription = "$title. $description. Estado: $progressText. $priorityText."
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .accessibleClickable(
                description = accessibilityDescription,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighPriority) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) 
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isHighPriority) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Incluido en la descripción principal
                    tint = if (isHighPriority) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (isHighPriority) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null, // Incluido en la descripción principal
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (progress >= 1f) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null, // Incluido en la descripción principal
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clearAndSetSemantics {} // Incluido en la descripción principal
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clearAndSetSemantics {}, // Incluido en la descripción principal
                color = when {
                    progress >= 1f -> MaterialTheme.colorScheme.primary
                    progress >= 0.5f -> MaterialTheme.colorScheme.tertiary
                    else -> if (isHighPriority) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoCardPreview() {
    UmeEguneroTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TodoCard(
                    title = "Tarea completada",
                    description = "Esta tarea ya se ha completado y no requiere acción",
                    icon = Icons.Default.Check,
                    progress = 1f
                )
                
                TodoCard(
                    title = "Tarea en progreso",
                    description = "Esta tarea está en proceso de realización",
                    icon = Icons.Default.Check,
                    progress = 0.6f
                )
                
                TodoCard(
                    title = "Tarea urgente pendiente",
                    description = "Esta tarea requiere atención inmediata",
                    icon = Icons.Default.Warning,
                    progress = 0.1f,
                    isHighPriority = true
                )
            }
        }
    }
} 