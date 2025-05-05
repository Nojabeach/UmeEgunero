package com.tfg.umeegunero.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Barra de búsqueda personalizada.
 *
 * @param query Texto actual de la búsqueda
 * @param onQueryChange Callback cuando cambia el texto
 * @param onSearch Callback cuando se realiza la búsqueda (presiona enter)
 * @param placeholder Texto de placeholder
 * @param autoFocus Si debe tener foco automáticamente
 * @param modifier Modificador para personalizar el componente
 * @param debounceTime Tiempo de debounce en milisegundos (0 para deshabilitar)
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit = {},
    placeholder: String = "Buscar...",
    autoFocus: Boolean = false,
    modifier: Modifier = Modifier,
    debounceTime: Long = 500
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    var debouncedText by remember { mutableStateOf(query) }
    
    // Efecto para debounce
    LaunchedEffect(query) {
        if (debounceTime > 0) {
            delay(debounceTime)
            debouncedText = query
        } else {
            debouncedText = query
        }
    }
    
    // Efecto para la búsqueda con debounce
    LaunchedEffect(debouncedText) {
        if (debouncedText.isNotEmpty()) {
            onSearch(debouncedText)
        }
    }
    
    // Efecto para auto-focus
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                AnimatedVisibility(visible = query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(query)
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Versión simplificada de la barra de búsqueda que maneja el estado internamente
 *
 * @param onSearch Callback cuando se realiza la búsqueda
 * @param placeholder Texto de placeholder
 * @param initialQuery Consulta inicial (opcional)
 * @param autoFocus Si debe tener foco automáticamente
 * @param modifier Modificador para personalizar el componente
 * @param debounceTime Tiempo de debounce en milisegundos (0 para deshabilitar)
 */
@Composable
fun SimpleSearchBar(
    onSearch: (String) -> Unit,
    placeholder: String = "Buscar...",
    initialQuery: String = "",
    autoFocus: Boolean = false,
    modifier: Modifier = Modifier,
    debounceTime: Long = 500
) {
    var query by remember { mutableStateOf(initialQuery) }
    
    SearchBar(
        query = query,
        onQueryChange = { query = it },
        onSearch = onSearch,
        placeholder = placeholder,
        autoFocus = autoFocus,
        modifier = modifier,
        debounceTime = debounceTime
    )
} 