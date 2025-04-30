/**
 * Módulo de pantallas de autenticación para la aplicación UmeEgunero.
 * 
 * Este módulo contiene los componentes relacionados con el proceso de recuperación
 * de contraseña y gestión de credenciales de usuario.
 * 
 * @see RecuperarPasswordScreen
 * @see RecuperarPasswordViewModel
 */
package com.tfg.umeegunero.feature.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.feature.auth.viewmodel.RecuperarPasswordViewModel
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla principal para la recuperación de contraseña.
 * 
 * Implementa un flujo completo de recuperación de contraseña con las siguientes características:
 * - Formulario de entrada de email
 * - Validación en tiempo real
 * - Feedback visual del proceso
 * - Gestión de estados de carga
 * - Manejo de errores
 * - Confirmación de envío
 * 
 * ## Estados de la pantalla
 * - Estado inicial: Formulario de email
 * - Estado de carga: Indicador de progreso
 * - Estado de éxito: Mensaje de confirmación
 * - Estado de error: Mensaje de error en Snackbar
 * 
 * ## Seguridad
 * - Validación de formato de email
 * - Prevención de múltiples envíos
 * - Mensajes de error genéricos
 * 
 * @param viewModel ViewModel que gestiona la lógica de recuperación
 * @param onNavigateBack Callback para volver a la pantalla anterior
 * 
 * @see RecuperarPasswordViewModel
 * @see RecuperarPasswordForm
 * @see SuccessContent
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarPasswordScreen(
    viewModel: RecuperarPasswordViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Efecto para mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    // Efecto para mostrar éxito
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            scope.launch {
                snackbarHostState.showSnackbar("Revisa tu correo para instrucciones de recuperación")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Si está en proceso de envío, mostramos loading
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                // Si se ha enviado correctamente, mostramos confirmación
                if (uiState.success) {
                    SuccessContent(onNavigateBack = onNavigateBack)
                } else {
                    // Formulario para recuperar contraseña
                    RecuperarPasswordForm(
                        email = uiState.email,
                        emailError = uiState.emailError,
                        onEmailChange = { viewModel.updateEmail(it) },
                        onSubmit = { viewModel.recuperarPassword() }
                    )
                }
            }
        }
    }
}

/**
 * Formulario para la recuperación de contraseña.
 * 
 * Componente que implementa el formulario de entrada de email con validación
 * y feedback visual en tiempo real.
 * 
 * ## Características
 * - Campo de email con validación
 * - Indicador de errores
 * - Botón de envío
 * - Soporte para teclado
 * 
 * @param email Email actual
 * @param emailError Error de validación si existe
 * @param onEmailChange Callback para cambios en el email
 * @param onSubmit Callback para el envío del formulario
 */
@Composable
fun RecuperarPasswordForm(
    email: String,
    emailError: String?,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ingresa tu correo electrónico para recuperar tu contraseña",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Campo de email con validación en tiempo real
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Correo Electrónico") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null
                    )
                },
                isError = emailError != null,
                supportingText = { 
                    if (emailError != null) {
                        Text(emailError)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recuperar Contraseña")
            }
        }
    }
}

/**
 * Contenido mostrado después de un envío exitoso.
 * 
 * Muestra un mensaje de confirmación y las instrucciones para el usuario
 * después de enviar el correo de recuperación.
 * 
 * ## Elementos
 * - Título de confirmación
 * - Mensaje informativo
 * - Botón para volver
 * 
 * @param onNavigateBack Callback para volver a la pantalla anterior
 */
@Composable
fun SuccessContent(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Correo enviado",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Hemos enviado un correo con instrucciones para restablecer tu contraseña. Por favor revisa tu bandeja de entrada.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Volver al inicio de sesión")
        }
    }
}

/**
 * Vista previa del formulario de recuperación en estado normal.
 */
@Preview(showBackground = true)
@Composable
fun RecuperarPasswordScreenPreview() {
    UmeEguneroTheme {
        RecuperarPasswordForm(
            email = "usuario@example.com",
            emailError = null,
            onEmailChange = {},
            onSubmit = {}
        )
    }
}

/**
 * Vista previa del formulario de recuperación con error.
 */
@Preview(showBackground = true)
@Composable
fun RecuperarPasswordScreenErrorPreview() {
    UmeEguneroTheme {
        RecuperarPasswordForm(
            email = "usuarioincorrecto",
            emailError = "El correo electrónico no es válido",
            onEmailChange = {},
            onSubmit = {}
        )
    }
}

/**
 * Vista previa del contenido de éxito.
 */
@Preview(showBackground = true)
@Composable
fun SuccessContentPreview() {
    UmeEguneroTheme {
        SuccessContent(onNavigateBack = {})
    }
}

/**
 * Vista previa del formulario en modo oscuro.
 */
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecuperarPasswordScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        RecuperarPasswordForm(
            email = "usuario@example.com",
            emailError = null,
            onEmailChange = {},
            onSubmit = {}
        )
    }
} 