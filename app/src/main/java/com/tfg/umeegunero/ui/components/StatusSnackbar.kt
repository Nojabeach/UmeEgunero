package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Tipos de mensaje para el snackbar
 */
enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO
}

/**
 * Función para mostrar un Snackbar personalizado según el tipo
 * 
 * @param hostState El estado del Snackbar host
 * @param message Mensaje a mostrar
 * @param type Tipo de mensaje (éxito, error, info)
 * @param actionLabel Texto del botón de acción (opcional)
 * @param duration Duración del snackbar
 * @param onAction Acción a ejecutar al hacer clic en el botón (opcional)
 * @param scope Scope de corrutina para lanzar el snackbar
 */
fun showSnackbar(
    hostState: SnackbarHostState,
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onAction: (() -> Unit)? = null,
    scope: CoroutineScope
) {
    scope.launch {
        val result = hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration,
            // Usamos el withDismissAction para que se muestre el botón de cerrar si no hay acción
            withDismissAction = actionLabel == null
        )
        
        if (result == SnackbarResult.ActionPerformed && onAction != null) {
            onAction()
        }
    }
}

/**
 * Host personalizado para Snackbar con diferentes tipos de mensaje
 * 
 * @param hostState Estado del SnackbarHost
 * @param modifier Modificador para personalizar el host
 */
@Composable
fun StatusSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.fillMaxWidth().padding(16.dp),
        snackbar = { snackbarData ->
            StatusSnackbar(
                data = snackbarData,
                type = getSnackbarType(snackbarData.visuals.message)
            )
        }
    )
}

/**
 * Snackbar personalizado con iconos y colores según el tipo de mensaje
 * 
 * @param data Datos del Snackbar
 * @param type Tipo de mensaje
 * @param modifier Modificador para personalizar el snackbar
 */
@Composable
fun StatusSnackbar(
    data: SnackbarData,
    type: SnackbarType = SnackbarType.INFO,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        SnackbarType.ERROR -> MaterialTheme.colorScheme.errorContainer
        SnackbarType.INFO -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        SnackbarType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        SnackbarType.INFO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val icon = when (type) {
        SnackbarType.SUCCESS -> Icons.Default.Check
        SnackbarType.ERROR -> Icons.Default.Error
        SnackbarType.INFO -> Icons.Default.Info
    }
    
    Snackbar(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        action = {
            if (data.visuals.actionLabel != null) {
                TextButton(
                    onClick = { data.performAction() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(data.visuals.actionLabel ?: "")
                }
            }
        },
        dismissAction = {
            if (data.visuals.withDismissAction) {
                TextButton(
                    onClick = { data.dismiss() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text("Cerrar")
                }
            }
        }
    ) {
        androidx.compose.foundation.layout.Row {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(data.visuals.message)
        }
    }
}

/**
 * Determina el tipo de mensaje basado en el contenido
 * Esta es una implementación simple, podría mejorarse con un sistema más sofisticado
 */
private fun getSnackbarType(message: String): SnackbarType {
    return when {
        message.contains("éxito", ignoreCase = true) ||
        message.contains("guardado", ignoreCase = true) ||
        message.contains("completado", ignoreCase = true) ||
        message.contains("correcto", ignoreCase = true) -> SnackbarType.SUCCESS
        
        message.contains("error", ignoreCase = true) ||
        message.contains("fallo", ignoreCase = true) ||
        message.contains("fallido", ignoreCase = true) ||
        message.contains("incorrecto", ignoreCase = true) -> SnackbarType.ERROR
        
        else -> SnackbarType.INFO
    }
}

/**
 * Composable para crear y recordar un SnackbarHostState junto con un scope
 */
@Composable
fun rememberSnackbarController(): Pair<SnackbarHostState, CoroutineScope> {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return Pair(hostState, scope)
} 