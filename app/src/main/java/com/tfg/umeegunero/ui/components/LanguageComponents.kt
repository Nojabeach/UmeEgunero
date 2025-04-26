package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Componente de selección de idioma.
 * 
 * @param currentLanguage Código del idioma actualmente seleccionado
 * @param onLanguageSelected Callback que se ejecuta cuando se selecciona un idioma
 */
@Composable
fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "Español" to "ES",
        "English" to "EN",
        "Euskara" to "EU",
        "Català" to "CA",
        "Galego" to "GL"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        languages.forEach { (name, code) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (currentLanguage == code)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                    .clickable { onLanguageSelected(code) }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (currentLanguage == code)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (currentLanguage == code) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Diálogo de selección de idioma.
 * 
 * @param onLanguageSelected Callback que se ejecuta cuando se selecciona un idioma
 * @param onDismiss Callback que se ejecuta cuando se cierra el diálogo
 */
@Composable
fun LanguageSelectionDialog(
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "Español" to "ES",
        "English" to "EN",
        "Euskara" to "EU",
        "Català" to "CA",
        "Galego" to "GL"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar idioma") },
        text = {
            Column {
                languages.forEach { (name, code) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                onLanguageSelected(code)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 