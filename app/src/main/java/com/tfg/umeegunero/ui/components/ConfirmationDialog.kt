package com.tfg.umeegunero.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Diálogo de confirmación reutilizable para confirmar acciones importantes.
 *
 * @param title Título del diálogo
 * @param message Mensaje descriptivo para el usuario
 * @param confirmButtonText Texto del botón de confirmación
 * @param dismissButtonText Texto del botón de cancelación
 * @param icon Icono a mostrar (opcional)
 * @param iconTint Color del icono (por defecto color de advertencia)
 * @param isDestructive Indica si la acción es destructiva (cambia el color del botón de confirmación)
 * @param onConfirm Acción a ejecutar cuando el usuario confirma
 * @param onDismiss Acción a ejecutar cuando el usuario cancela
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar",
    icon: ImageVector = Icons.Default.Warning,
    iconTint: Color = MaterialTheme.colorScheme.error,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(dismissButtonText)
            }
        }
    )
} 