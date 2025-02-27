package com.tfg.umeegunero.feature.auth.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.UserType
import com.tfg.umeegunero.feature.auth.viewmodel.LoginViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userType: UserType,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLoginSuccess: (UserType) -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showPassword by remember { mutableStateOf(false) }

    // Color según tipo de usuario
    val userTypeColor = when (userType) {
        UserType.ADMIN -> MaterialTheme.colorScheme.tertiary
        UserType.CENTRO -> Color(0xFF007AFF) // Azul iOS
        UserType.PROFESOR -> Color(0xFF34C759) // Verde iOS
        UserType.FAMILIAR -> Color(0xFF5856D6) // Púrpura iOS
    }

    // Título según tipo de usuario
    val userTypeTitle = when (userType) {
        UserType.ADMIN -> "Administrador"
        UserType.CENTRO -> "Centro Educativo"
        UserType.PROFESOR -> "Profesor"
        UserType.FAMILIAR -> "Familiar"
    }

    // Detectar éxito en el login
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            // Pasar el tipo de usuario a la función de navegación
            onLoginSuccess(userType)
        }
    }

    // Mostrar errores en Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearError()
            }
        }
    }

    // Determinar si estamos en modo claro u oscuro
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f

    // Crear un gradiente elegante para el fondo
    val gradientColors = if (!isLight) {
        // Gradiente para modo oscuro
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    } else {
        // Gradiente para modo claro
        listOf(
            Color(0xFFF0F4FF), // Azul muy claro
            Color(0xFFF8F9FF), // Casi blanco con tinte azul
            Color(0xFFF0FAFF)  // Azul muy claro con tinte cyan
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso $userTypeTitle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Logo e icono de perfil
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "UmeEgunero",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Acceso $userTypeTitle",
                        style = MaterialTheme.typography.titleMedium,
                        color = userTypeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Formulario de login
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Campo de email
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = userTypeColor
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            isError = uiState.emailError != null,
                            supportingText = {
                                if (uiState.emailError != null) {
                                    Text(text = uiState.emailError!!)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Campo de contraseña
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updatePassword(it) },
                            label = { Text("Contraseña") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = userTypeColor
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (uiState.isLoginEnabled) {
                                        viewModel.login(userType)
                                    }
                                }
                            ),
                            isError = uiState.passwordError != null,
                            supportingText = {
                                if (uiState.passwordError != null) {
                                    Text(text = uiState.passwordError!!)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Recuperar contraseña
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = onForgotPassword) {
                                Text(
                                    "¿Olvidaste tu contraseña?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = userTypeColor
                                )
                            }
                        }

                        // Botón de inicio de sesión
                        Button(
                            onClick = { viewModel.login(userType) },
                            enabled = uiState.isLoginEnabled && !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = userTypeColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AnimatedVisibility(
                                visible = uiState.isLoading,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            }

                            AnimatedVisibility(
                                visible = !uiState.isLoading,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    "Iniciar Sesión",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Instrucciones para acceso
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val instructionText = when (userType) {
                            UserType.ADMIN -> "Accede con tus credenciales de administrador de la aplicación."
                            UserType.CENTRO -> "Accede con las credenciales proporcionadas para la gestión del centro educativo."
                            UserType.PROFESOR -> "Introduce las credenciales asignadas por tu centro educativo."
                            UserType.FAMILIAR -> "Accede con el email y contraseña que utilizaste en el registro."
                        }

                        Text(
                            text = instructionText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Extensión para calcular la luminancia de un color
private fun Color.luminance(): Float {
    val red = this.red * 0.299f
    val green = this.green * 0.587f
    val blue = this.blue * 0.114f
    return red + green + blue
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLightPreview() {
    UmeEguneroTheme(darkTheme = false) {
        LoginScreen(
            userType = UserType.ADMIN,
            onNavigateBack = {},
            onLoginSuccess = { userType ->
                // Simular navegación para el preview
                when (userType) {
                    UserType.ADMIN -> println("Navegando a Admin Dashboard")
                    UserType.CENTRO -> println("Navegando a Centro Dashboard")
                    UserType.PROFESOR -> println("Navegando a Profesor Dashboard")
                    UserType.FAMILIAR -> println("Navegando a Familiar Dashboard")
                }
            }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        LoginScreen(
            userType = UserType.ADMIN,
            onNavigateBack = {},
            onLoginSuccess = { userType ->
                // Simular navegación para el preview
                when (userType) {
                    UserType.ADMIN -> println("Navegando a Admin Dashboard")
                    UserType.CENTRO -> println("Navegando a Centro Dashboard")
                    UserType.PROFESOR -> println("Navegando a Profesor Dashboard")
                    UserType.FAMILIAR -> println("Navegando a Familiar Dashboard")
                }
            }
        )
    }
}
