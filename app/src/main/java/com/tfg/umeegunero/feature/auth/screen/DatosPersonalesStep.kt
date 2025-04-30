/**
 * Módulo de pantallas de autenticación para la aplicación UmeEgunero.
 * 
 * Este módulo contiene los componentes relacionados con el proceso de registro
 * y gestión de datos personales de los usuarios.
 * 
 * @see DatosPersonalesStep
 */
package com.tfg.umeegunero.feature.auth.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Paso del formulario de registro para la introducción de datos personales.
 * 
 * Este componente implementa un formulario completo para la captura de datos personales
 * con validación en tiempo real y feedback visual.
 * 
 * ## Campos del formulario
 * - DNI/NIE del usuario
 * - Email de contacto
 * - Contraseña y confirmación
 * - Nombre y apellidos
 * - Teléfono de contacto
 * - Tipo de relación familiar
 * 
 * ## Características
 * - Validación en tiempo real de todos los campos
 * - Feedback visual de errores
 * - Enmascaramiento de contraseñas
 * - Organización en tarjetas por tipo de datos
 * - Soporte para scroll vertical
 * 
 * @param dni DNI/NIE del usuario
 * @param email Email de contacto
 * @param password Contraseña
 * @param confirmPassword Confirmación de contraseña
 * @param nombre Nombre del usuario
 * @param apellidos Apellidos del usuario
 * @param telefono Teléfono de contacto
 * @param subtipo Tipo de relación familiar
 * @param onDniChange Callback para cambios en DNI
 * @param onEmailChange Callback para cambios en email
 * @param onPasswordChange Callback para cambios en contraseña
 * @param onConfirmPasswordChange Callback para cambios en confirmación de contraseña
 * @param onNombreChange Callback para cambios en nombre
 * @param onApellidosChange Callback para cambios en apellidos
 * @param onTelefonoChange Callback para cambios en teléfono
 * @param onSubtipoChange Callback para cambios en tipo de relación
 * @param errors Mapa de errores por campo
 * 
 * @see SubtipoFamiliar
 */
@Composable
fun DatosPersonalesStep(
    dni: String,
    email: String,
    password: String,
    confirmPassword: String,
    nombre: String,
    apellidos: String,
    telefono: String,
    subtipo: SubtipoFamiliar,
    onDniChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onSubtipoChange: (SubtipoFamiliar) -> Unit,
    errors: Map<String, String>
) {
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // DNI
                OutlinedTextField(
                    value = dni,
                    onValueChange = onDniChange,
                    label = { Text("DNI") },
                    leadingIcon = {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("dni"),
                    supportingText = {
                        if (errors.containsKey("dni")) {
                            Text(errors["dni"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("email"),
                    supportingText = {
                        if (errors.containsKey("email")) {
                            Text(errors["email"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("password"),
                    supportingText = {
                        if (errors.containsKey("password")) {
                            Text(errors["password"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirmar Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("confirmPassword"),
                    supportingText = {
                        if (errors.containsKey("confirmPassword")) {
                            Text(errors["confirmPassword"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Datos personales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("nombre"),
                    supportingText = {
                        if (errors.containsKey("nombre")) {
                            Text(errors["nombre"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Apellidos
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = onApellidosChange,
                    label = { Text("Apellidos") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = errors.containsKey("apellidos"),
                    supportingText = {
                        if (errors.containsKey("apellidos")) {
                            Text(errors["apellidos"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Teléfono
                OutlinedTextField(
                    value = telefono,
                    onValueChange = onTelefonoChange,
                    label = { Text("Teléfono") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    isError = errors.containsKey("telefono"),
                    supportingText = {
                        if (errors.containsKey("telefono")) {
                            Text(errors["telefono"] ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tipo de familiar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¿Qué relación tienes con el alumno?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TipoFamiliarOptions(
                    selectedType = subtipo,
                    onTypeSelected = onSubtipoChange
                )
            }
        }
    }
}

@Composable
fun TipoFamiliarOptions(
    selectedType: SubtipoFamiliar,
    onTypeSelected: (SubtipoFamiliar) -> Unit
) {
    Column {
        SubtipoFamiliar.values().forEach { tipo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedType == tipo,
                    onClick = { onTypeSelected(tipo) }
                )

                Text(
                    text = when(tipo) {
                        SubtipoFamiliar.PADRE -> "Padre"
                        SubtipoFamiliar.MADRE -> "Madre"
                        SubtipoFamiliar.TUTOR -> "Tutor/a legal"
                        SubtipoFamiliar.OTRO -> "Otro familiar"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Vista previa del componente DatosPersonalesStep en modo claro.
 * 
 * Muestra una versión de prueba con datos de ejemplo y sin errores.
 */
@Preview(
    name = "DatosPersonalesStep - Light Mode",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun DatosPersonalesStepPreview() {
    UmeEguneroTheme {
        DatosPersonalesStep(
            dni = "12345678X",
            email = "ejemplo@email.com",
            password = "password123",
            confirmPassword = "password123",
            nombre = "Juan",
            apellidos = "García López",
            telefono = "600123456",
            subtipo = SubtipoFamiliar.PADRE,
            onDniChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onNombreChange = {},
            onApellidosChange = {},
            onTelefonoChange = {},
            onSubtipoChange = {},
            errors = emptyMap()
        )
    }
}

/**
 * Vista previa del componente DatosPersonalesStep en modo oscuro.
 * 
 * Muestra una versión de prueba con datos de ejemplo y sin errores.
 */
@Preview(
    name = "DatosPersonalesStep - Dark Mode",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DatosPersonalesStepDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        DatosPersonalesStep(
            dni = "12345678X",
            email = "ejemplo@email.com",
            password = "password123",
            confirmPassword = "password123",
            nombre = "Juan",
            apellidos = "García López",
            telefono = "600123456",
            subtipo = SubtipoFamiliar.PADRE,
            onDniChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onNombreChange = {},
            onApellidosChange = {},
            onTelefonoChange = {},
            onSubtipoChange = {},
            errors = mapOf("dni" to "Este DNI ya está registrado")
        )
    }
}

/**
 * Vista previa del componente DatosPersonalesStep con errores.
 * 
 * Muestra una versión de prueba con datos de ejemplo y errores de validación.
 */
@Preview(
    name = "DatosPersonalesStep - With Errors",
    showBackground = true,
    backgroundColor = 0xFFF0F0F0
)
@Composable
fun DatosPersonalesStepErrorPreview() {
    UmeEguneroTheme {
        DatosPersonalesStep(
            dni = "12345678X",
            email = "ejemplo@email.com",
            password = "password123",
            confirmPassword = "password123",
            nombre = "Juan",
            apellidos = "García López",
            telefono = "600123456",
            subtipo = SubtipoFamiliar.PADRE,
            onDniChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onNombreChange = {},
            onApellidosChange = {},
            onTelefonoChange = {},
            onSubtipoChange = {},
            errors = mapOf("dni" to "Este DNI ya está registrado")
        )
    }
}