package com.tfg.umeegunero.feature.common.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Componente que muestra una tarjeta para el módulo de mensajes
 * 
 * @param unreadCount Cantidad de mensajes no leídos
 * @param onClick Acción a ejecutar al hacer clic en la tarjeta
 * @param modifier Modificador para personalizar el aspecto visual
 * @param title Título de la tarjeta (por defecto: "Mensajes")
 * @param description Descripción de la tarjeta (por defecto: "Revisa tus mensajes y comunicados")
 */
@Composable
fun MessageCard(
    unreadCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Mensajes",
    description: String = "Revisa tus mensajes y comunicados"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Mensajes",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Indicador de mensajes no leídos
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Vista previa del componente MessageCard sin mensajes no leídos
 */
@Preview(showBackground = true)
@Composable
fun MessageCardPreview() {
    UmeEguneroTheme {
        MessageCard(
            unreadCount = 0,
            onClick = {}
        )
    }
}

/**
 * Vista previa del componente MessageCard con mensajes no leídos
 */
@Preview(showBackground = true)
@Composable
fun MessageCardWithUnreadPreview() {
    UmeEguneroTheme {
        MessageCard(
            unreadCount = 5,
            onClick = {}
        )
    }
}

/**
 * Vista previa del componente MessageCard con muchos mensajes no leídos
 */
@Preview(showBackground = true)
@Composable
fun MessageCardWithManyUnreadPreview() {
    UmeEguneroTheme {
        MessageCard(
            unreadCount = 120,
            onClick = {}
        )
    }
} 