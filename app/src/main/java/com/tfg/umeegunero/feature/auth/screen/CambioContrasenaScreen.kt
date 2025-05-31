/**
 * Módulo de pantallas de autenticación para la aplicación UmeEgunero.
 * 
 * Este módulo contiene las pantallas relacionadas con el proceso de autenticación y gestión de usuarios.
 * 
 * @see CambioContrasenaScreen
 * @see CambioContrasenaViewModel
 */
package com.tfg.umeegunero.feature.auth.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.viewmodel.CambioContrasenaViewModel
import com.tfg.umeegunero.ui.components.AppTopBar
import com.tfg.umeegunero.ui.components.LoadingDialog
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

/**
 * Pantalla para cambiar la contraseña de un usuario.
 * 
 * Implementa un formulario seguro para el cambio de contraseña con las siguientes características:
 * - Validación de contraseña actual
 * - Validación de nueva contraseña
 * - Confirmación de nueva contraseña
 * - Visualización/ocultación de contraseñas
 * - Feedback visual del proceso
 * - Manejo de errores
 * 
 * ## Características de Seguridad
 * - Enmascaramiento de contraseñas por defecto
 * - Validación de coincidencia de contraseñas
 * - Indicador de carga durante el proceso
 * - Mensajes de error descriptivos
 * 
 * @param dni DNI del usuario que cambiará su contraseña
 * @param onNavigateBack Callback para volver a la pantalla anterior
 * @param onPasswordChanged Callback que se ejecuta cuando la contraseña se cambia exitosamente
 * @param viewModel ViewModel que gestiona la lógica de cambio de contraseña
 * @param isFromLogin Indica si la pantalla se está mostrando desde el flujo de login
 * @param requiereNuevaContrasena Indica si se requiere nueva contraseña para el cambio
 * @param userType Tipo de usuario asociado al cambio de contraseña
 * @param onLoginCompleted Callback que se ejecuta cuando el login se completa exitosamente
 * 
 * @see CambioContrasenaViewModel
 * @see LoadingDialog
 * @see AppTopBar
 */
@Composable
fun CambioContrasenaScreen(
    dni: String,
    onNavigateBack: () -> Unit,
    onPasswordChanged: () -> Unit = {},
    viewModel: CambioContrasenaViewModel = hiltViewModel(),
    isFromLogin: Boolean = false,
    requiereNuevaContrasena: Boolean = false,
    userType: TipoUsuario? = null,
    onLoginCompleted: (() -> Unit)? = null
) {
    var contrasenaActual by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var mostrarContrasenaActual by remember { mutableStateOf(false) }
    var mostrarNuevaContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmarContrasena by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            if (isFromLogin && onLoginCompleted != null) {
                // Si viene del login, completar el proceso de login
                onLoginCompleted()
            } else {
                // Flujo normal de cambio de contraseña
                onPasswordChanged()
            }
        }
    }

    UmeEguneroTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = if (isFromLogin) {
                        if (requiereNuevaContrasena) "Establecer nueva contraseña" else "Cambio requerido"
                    } else {
                        "Cambiar Contraseña"
                    },
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNavigationClick = if (isFromLogin) {
                        // Si viene del login, no permitir volver atrás fácilmente
                        { /* No hacer nada o mostrar diálogo de confirmación */ }
                    } else {
                        onNavigateBack
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mostrar información contextual si viene del login
                    if (isFromLogin) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (requiereNuevaContrasena) {
                                        "Establece una contraseña segura"
                                    } else {
                                        "Actualización de seguridad requerida"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (requiereNuevaContrasena) {
                                        "Por tu seguridad, establece una contraseña personalizada."
                                    } else {
                                        "Tu contraseña ha expirado y debe ser actualizada."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    } else if (requiereNuevaContrasena) {
                        // Mostrar información para el flujo de "Olvidé mi contraseña"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Recuperación de contraseña",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Introduce una nueva contraseña para tu cuenta. No necesitas ingresar tu contraseña actual.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Campo de contraseña actual - solo si no requiere nueva contraseña
                    if (!requiereNuevaContrasena) {
                        OutlinedTextField(
                            value = contrasenaActual,
                            onValueChange = { contrasenaActual = it },
                            label = { Text("Contraseña Actual") },
                            singleLine = true,
                            visualTransformation = if (mostrarContrasenaActual) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            trailingIcon = {
                                IconButton(onClick = { mostrarContrasenaActual = !mostrarContrasenaActual }) {
                                    Icon(
                                        imageVector = if (mostrarContrasenaActual) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (mostrarContrasenaActual) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = nuevaContrasena,
                        onValueChange = { nuevaContrasena = it },
                        label = { Text("Nueva Contraseña") },
                        singleLine = true,
                        visualTransformation = if (mostrarNuevaContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(onClick = { mostrarNuevaContrasena = !mostrarNuevaContrasena }) {
                                Icon(
                                    imageVector = if (mostrarNuevaContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarNuevaContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmarContrasena,
                        onValueChange = { confirmarContrasena = it },
                        label = { Text("Confirmar Nueva Contraseña") },
                        singleLine = true,
                        visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            IconButton(onClick = { mostrarConfirmarContrasena = !mostrarConfirmarContrasena }) {
                                Icon(
                                    imageVector = if (mostrarConfirmarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (mostrarConfirmarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (requiereNuevaContrasena) {
                                // Si requiere nueva contraseña, no validar la actual
                                viewModel.cambiarContrasena(dni, "", nuevaContrasena, confirmarContrasena)
                            } else {
                                // Flujo normal con validación de contraseña actual
                                viewModel.cambiarContrasena(dni, contrasenaActual, nuevaContrasena, confirmarContrasena)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = if (requiereNuevaContrasena) {
                            // Si requiere nueva contraseña, solo validar que las nuevas estén completas
                            nuevaContrasena.isNotBlank() && confirmarContrasena.isNotBlank()
                        } else {
                            // Validación normal
                            contrasenaActual.isNotBlank() && nuevaContrasena.isNotBlank() && confirmarContrasena.isNotBlank()
                        }
                    ) {
                        Text(
                            if (requiereNuevaContrasena) "Establecer contraseña" else "Cambiar Contraseña"
                        )
                    }

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (uiState.isLoading) {
                    LoadingDialog()
                }
            }
        }
    }
}

/**
 * Vista previa del componente CambioContrasenaScreen.
 * 
 * Muestra una versión de prueba con datos de ejemplo.
 * 
 * @see CambioContrasenaScreen
 */
@Preview(showBackground = true)
@Composable
fun CambioContrasenaScreenPreview() {
    UmeEguneroTheme {
        CambioContrasenaScreen(
            dni = "12345678A",
            onNavigateBack = {},
            onPasswordChanged = {}
        )
    }
} 