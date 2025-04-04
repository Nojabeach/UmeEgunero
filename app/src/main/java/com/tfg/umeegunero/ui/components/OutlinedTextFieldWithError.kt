package com.tfg.umeegunero.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de texto con soporte para mensaje de error.
 * Muestra un campo de texto con estilo OutlinedTextField y un mensaje de error debajo si es necesario.
 * 
 * @param value Valor actual del campo de texto
 * @param onValueChange Función de callback llamada cuando cambia el valor
 * @param label Etiqueta del campo de texto
 * @param placeholder Placeholder del campo de texto
 * @param errorMessage Mensaje de error (si es null, no se muestra ningún error)
 * @param isError Indica si el campo tiene un error
 * @param enabled Indica si el campo está habilitado
 * @param readOnly Indica si el campo es de solo lectura
 * @param singleLine Indica si el campo debe ser de una sola línea
 * @param maxLines Número máximo de líneas
 * @param keyboardOptions Opciones de teclado como tipo y acciones
 * @param keyboardActions Acciones de teclado
 * @param visualTransformation Transformación visual del texto (por ejemplo, para contraseñas)
 * @param leadingIcon Icono a mostrar al inicio del campo
 * @param modifier Modificador para personalizar el componente
 */
@Composable
fun OutlinedTextFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    errorMessage: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasError = isError || errorMessage != null
    
    Column(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(text = placeholder) }
            } else null,
            isError = hasError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            trailingIcon = if (hasError) {
                {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            leadingIcon = leadingIcon,
            supportingText = if (errorMessage != null) {
                { Text(text = errorMessage) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Sobrecarga del campo de texto con validación que acepta keyboardType e imeAction directamente
 * para mantener compatibilidad con código existente.
 */
@Composable
fun OutlinedTextFieldWithError(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    error: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    placeholder: String = ""
) {
    val effectiveErrorMessage = if (errorMessage.isNotEmpty()) errorMessage else error
    val hasError = isError || effectiveErrorMessage.isNotEmpty()
    
    OutlinedTextFieldWithError(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        errorMessage = if (effectiveErrorMessage.isNotEmpty()) effectiveErrorMessage else null,
        isError = hasError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        leadingIcon = leadingIcon,
        modifier = modifier
    )
} 