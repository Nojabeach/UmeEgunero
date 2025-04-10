package com.tfg.umeegunero.feature.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * Componente de diálogo personalizado para selección de hora utilizando Material3
 * 
 * Este componente envuelve un AlertDialog de Material3 con una configuración específica
 * para mostrar un selector de tiempo (TimePicker) con botones de confirmación y cancelación.
 * 
 * @param onDismissRequest Callback invocado cuando se solicita cerrar el diálogo
 * @param onConfirm Callback invocado cuando se confirma la selección
 * @param content Composable que renderiza el contenido del diálogo (generalmente un TimePicker)
 * @param confirmButton Botón de confirmación personalizado (opcional)
 * @param dismissButton Botón de cancelación personalizado (opcional)
 */
@Composable
fun CustomTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                }
            }
        },
        confirmButton = {
            confirmButton?.invoke() ?: TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            dismissButton?.invoke() ?: TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
} 