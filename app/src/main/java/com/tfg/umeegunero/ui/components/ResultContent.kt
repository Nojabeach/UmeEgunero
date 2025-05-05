package com.tfg.umeegunero.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tfg.umeegunero.util.Result

/**
 * Componente que maneja los diferentes estados de un Result y muestra
 * el componente adecuado según el estado actual.
 *
 * @param result Objeto Result a procesar
 * @param emptyMessage Mensaje a mostrar cuando el resultado es vacío
 * @param errorTitle Título opcional para mostrar con el error
 * @param onRetry Acción a ejecutar al pulsar el botón de reintentar (opcional)
 * @param modifier Modificador para personalizar el componente
 * @param contentEmpty Función para determinar si el contenido debe considerarse vacío
 * @param content Contenido a mostrar cuando el resultado es Success y no está vacío
 */
@Composable
fun <T> ResultContent(
    result: Result<T>,
    emptyMessage: String = "No hay datos disponibles",
    errorTitle: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentEmpty: (T) -> Boolean = { data ->
        when (data) {
            is Collection<*> -> data.isEmpty()
            is Array<*> -> data.isEmpty()
            is Map<*, *> -> data.isEmpty()
            is String -> data.isBlank()
            null -> true
            else -> false
        }
    },
    content: @Composable (T) -> Unit
) {
    when (result) {
        is Result.Loading -> {
            LoadingContent(modifier = modifier)
        }
        is Result.Success -> {
            val data = result.data
            if (contentEmpty(data)) {
                EmptyContent(
                    message = emptyMessage,
                    onAction = onRetry,
                    actionText = onRetry?.let { "Reintentar" },
                    modifier = modifier
                )
            } else {
                content(data)
            }
        }
        is Result.Error -> {
            ErrorContent(
                title = errorTitle,
                message = result.message ?: "Ha ocurrido un error inesperado",
                onRetry = onRetry,
                modifier = modifier
            )
        }
    }
}

/**
 * Extensión de ResultContent que simplifica el uso con listas.
 */
@Composable
fun <T> ResultContentList(
    result: Result<List<T>>,
    emptyMessage: String = "No hay elementos disponibles",
    errorTitle: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable (List<T>) -> Unit
) {
    ResultContent(
        result = result,
        emptyMessage = emptyMessage,
        errorTitle = errorTitle,
        onRetry = onRetry,
        modifier = modifier,
        contentEmpty = { it.isEmpty() },
        content = content
    )
} 