package com.tfg.umeegunero.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Componente que maneja los diferentes estados de contenido (carga, error, vacío) 
 * y muestra el componente adecuado según el estado actual.
 *
 * @param isLoading Indica si está en estado de carga
 * @param isEmpty Indica si no hay contenido para mostrar
 * @param error Mensaje de error (null si no hay error)
 * @param emptyMessage Mensaje a mostrar cuando no hay contenido
 * @param onRetry Acción a ejecutar al pulsar el botón de reintentar (opcional)
 * @param modifier Modificador para personalizar el componente
 * @param content Contenido a mostrar cuando no está en estado de carga, error o vacío
 */
@Composable
fun ContentStateHandler(
    isLoading: Boolean,
    isEmpty: Boolean,
    error: String? = null,
    emptyMessage: String = "No hay contenido disponible",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when {
        // Primero verificar si hay error
        error != null -> {
            ErrorContent(
                message = error,
                onRetry = onRetry,
                modifier = modifier
            )
        }
        // Luego verificar si está cargando
        isLoading -> {
            LoadingContent(modifier = modifier)
        }
        // Después verificar si está vacío
        isEmpty -> {
            EmptyContent(
                message = emptyMessage,
                onAction = onRetry,
                actionText = onRetry?.let { "Reintentar" },
                modifier = modifier
            )
        }
        // Si ninguna de las anteriores, mostrar el contenido
        else -> {
            content()
        }
    }
} 