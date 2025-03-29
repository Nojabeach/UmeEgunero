package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Componente que muestra un mensaje cuando no hay contenido para mostrar.
 * Incluye un icono, un mensaje descriptivo y opcionalmente un botón de acción.
 *
 * @param message Mensaje a mostrar al usuario
 * @param icon Icono a mostrar (debe ser un ImageVector)
 * @param buttonText Texto del botón (si es null, no se muestra el botón)
 * @param onButtonClick Acción a ejecutar cuando se pulsa el botón
 * @param modifier Modificador para personalizar el componente
 */
@Composable
fun EmptyStateMessage(
    message: String,
    icon: ImageVector,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        if (buttonText != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onButtonClick
            ) {
                Text(text = buttonText)
            }
        }
    }
} 