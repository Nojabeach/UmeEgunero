package com.tfg.umeegunero.feature.auth.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.viewmodel.LoginViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.getUserColor
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión para la aplicación UmeEgunero.
 * 
 * Este componente implementa una interfaz de autenticación adaptada al tipo de usuario
 * que intenta acceder al sistema (administrador, centro educativo, profesor o familiar).
 * La interfaz se adapta visualmente según el tipo de usuario, mostrando colores y textos
 * específicos para mejorar la experiencia de usuario.
 * 
 * Características principales:
 * - Diseño Material 3 con animaciones y efectos visuales modernos
 * - Adaptación visual según tipo de usuario con colores distintivos
 * - Formulario con validación en tiempo real
 * - Soporte para recordar credenciales
 * - Manejo de errores con Snackbars
 * - Soporte para diferentes modos (claro/oscuro)
 * 
 * @param userType Tipo de usuario que intenta iniciar sesión, determina la apariencia y comportamiento
 * @param viewModel ViewModel que gestiona la lógica de autenticación
 * @param onNavigateBack Callback que se ejecuta cuando el usuario pulsa el botón de retroceso
 * @param onLoginSuccess Callback que se ejecuta cuando la autenticación es exitosa
 * @param onForgotPassword Callback que se ejecuta cuando el usuario pulsa "Olvidé mi contraseña"
 * @param onNecesitaPermisos Callback que se ejecuta cuando un familiar necesita configurar permisos de notificaciones
 * @param onNecesitaCambioContrasena Callback que se ejecuta cuando un usuario necesita cambiar su contraseña
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 3.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userType: TipoUsuario,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLoginSuccess: (TipoUsuario) -> Unit,
    onForgotPassword: (String) -> Unit = {},
    onNecesitaPermisos: () -> Unit = {},
    onNecesitaCambioContrasena: (String, TipoUsuario, Boolean) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Estados para la interfaz
    var showPassword by remember { mutableStateOf(false) }
    var rememberUser by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    // Animación para la tarjeta principal
    val cardElevation by animateFloatAsState(
        targetValue = if (uiState.isLoading) 8f else 4f,
        label = "Card Elevation Animation"
    )

    // Color según tipo de usuario unificado
    val userTypeColor = getUserColor(userType)

    // Título según tipo de usuario
    val userTypeTitle = when (userType) {
        TipoUsuario.ADMIN_APP -> "Administrador"
        TipoUsuario.ADMIN_CENTRO -> "Centro Educativo"
        TipoUsuario.PROFESOR -> "Profesor"
        TipoUsuario.FAMILIAR -> "Familiar"
        TipoUsuario.ALUMNO -> "Alumno"
        TipoUsuario.DESCONOCIDO -> "Usuario"
        TipoUsuario.OTRO -> "Usuario"
    }

    // Verificar si hay credenciales guardadas
    LaunchedEffect(Unit) {
        viewModel.checkSavedCredentials()
        // Actualizar el estado del checkbox según las preferencias guardadas
        rememberUser = viewModel.getRememberUserPreference()
        // Animación de entrada para el contenido
        showContent = true
    }
    
    // Cuando cambia el valor de rememberUser, actualizar en el ViewModel
    LaunchedEffect(rememberUser) {
        viewModel.updateRememberUser(rememberUser)
    }

    // Validar el formato del email en tiempo real
    LaunchedEffect(uiState.email) {
        if (uiState.email.isNotEmpty()) {
            viewModel.updateEmail(uiState.email)
        }
    }
    
    // Validar la longitud de la contraseña en tiempo real
    LaunchedEffect(uiState.password) {
        if (uiState.password.isNotEmpty()) {
            viewModel.updatePassword(uiState.password)
        }
    }

    // Detectar éxito en el login
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            // Pasar el tipo de usuario a la función de navegación
            onLoginSuccess(userType)
        }
    }
    
    // Detectar si necesita configurar permisos de notificaciones
    LaunchedEffect(uiState.necesitaPermisoNotificaciones) {
        if (uiState.necesitaPermisoNotificaciones) {
            onNecesitaPermisos()
        }
    }

    // Detectar si necesita cambio de contraseña
    LaunchedEffect(uiState.necesitaCambioContrasena, uiState.requiereNuevaContrasena) {
        if (uiState.necesitaCambioContrasena || uiState.requiereNuevaContrasena) {
            onNecesitaCambioContrasena(
                uiState.usuarioDni,
                uiState.userType ?: userType,
                uiState.requiereNuevaContrasena
            )
        }
    }

    // Mostrar diálogo de solicitud pendiente
    if (uiState.solicitudPendiente) {
        AlertDialog(
            onDismissRequest = { },
            title = { 
                Text(
                    text = "Solicitud pendiente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Column {
                    Text(
                        text = "Tienes una solicitud de vinculación pendiente de aprobar por el centro educativo.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No puedes acceder a la aplicación hasta que un administrador de centro apruebe tu solicitud.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Referencia de solicitud:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.solicitudId,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    // Cierra sesión y regresa a la pantalla anterior
                    viewModel.resetState()
                    onNavigateBack()
                }) {
                    Text("Aceptar")
                }
            },
            icon = { 
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
    
    // Mostrar diálogo de cambio de contraseña requerido
    if (uiState.necesitaCambioContrasena || uiState.requiereNuevaContrasena) {
        AlertDialog(
            onDismissRequest = { 
                // No permitir cerrar el diálogo hasta que cambie la contraseña
            },
            title = { 
                Text(
                    text = if (uiState.requiereNuevaContrasena) "Contraseña temporal" else "Cambio de contraseña requerido",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Column {
                    if (uiState.requiereNuevaContrasena) {
                        Text(
                            text = "Has iniciado sesión con una contraseña temporal o predeterminada.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Por tu seguridad, debes establecer una contraseña personalizada antes de continuar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Tu contraseña ha expirado o necesitas cambiarla por políticas de seguridad.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Debes actualizar tu contraseña antes de acceder a la aplicación.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Requisitos de la nueva contraseña:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "• Mínimo 6 caracteres\n• Al menos una letra\n• Al menos un número\n• Al menos un carácter especial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onNecesitaCambioContrasena(
                            uiState.usuarioDni,
                            uiState.userType ?: userType,
                            uiState.requiereNuevaContrasena
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = userTypeColor
                    )
                ) {
                    Text("Cambiar contraseña")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        // Limpiar estado y volver atrás
                        viewModel.limpiarEstadoCambioContrasena()
                        onNavigateBack()
                    }
                ) {
                    Text("Cancelar")
                }
            },
            icon = { 
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = userTypeColor
                )
            }
        )
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
            userTypeColor.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    } else {
        // Gradiente para modo claro
        listOf(
            userTypeColor.copy(alpha = 0.1f),
            Color(0xFFF8F9FF), // Casi blanco con tinte azul
            userTypeColor.copy(alpha = 0.05f)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Acceso $userTypeTitle",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = userTypeColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
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
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Logo e icono de perfil
                    Box(
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Fondo circular con color del usuario
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    color = userTypeColor.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        )
                        
                        // Logo
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.app_icon),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Título de la app y subtítulo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                shadowElevation = cardElevation
                                translationY = if (uiState.isLoading) -2f else 0f
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
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
                                            contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = userTypeColor.copy(alpha = 0.7f)
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
                                            viewModel.login(userType, rememberUser)
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
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            // Checkbox para recordar usuario
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberUser,
                                    onCheckedChange = { 
                                        rememberUser = it
                                        // Si activamos recordar usuario y hay credenciales guardadas, habilitamos login
                                        if (it && !uiState.email.isNullOrEmpty() && !uiState.password.isNullOrEmpty()) {
                                            viewModel.validateCredentials()
                                        }
                                    }
                                )
                                Text(
                                    text = "Recordar mis credenciales",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // Botón "Olvidé mi contraseña"
                            TextButton(
                                onClick = { 
                                    if (uiState.email.isNotEmpty()) {
                                        onForgotPassword(uiState.email)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Por favor, introduce tu email primero")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "¿Olvidaste tu contraseña?",
                                    color = userTypeColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Botón de inicio de sesión
                            Button(
                                onClick = { 
                                    viewModel.login(userType, rememberUser) 
                                },
                                enabled = uiState.isLoginEnabled && !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = userTypeColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = userTypeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Información de acceso",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = userTypeColor
                                )
                            }
                            
                            val instructionText = when (userType) {
                                TipoUsuario.ADMIN_APP -> "Accede con tus credenciales de administrador de la aplicación para gestionar el sistema en su totalidad."
                                TipoUsuario.ADMIN_CENTRO -> "Utiliza las credenciales proporcionadas para la gestión del centro educativo y su configuración."
                                TipoUsuario.PROFESOR -> "Introduce las credenciales asignadas por tu centro educativo para acceder a tus clases y alumnos."
                                TipoUsuario.FAMILIAR -> "Accede con el email y contraseña que utilizaste en el registro para ver la información de tus hijos."
                                TipoUsuario.ALUMNO -> "Utiliza los datos de acceso proporcionados por tu centro educativo."
                                else -> "Introduce tus credenciales para acceder al sistema."
                            }

                            Text(
                                text = instructionText,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Espaciador final para scroll
                    Spacer(modifier = Modifier.height(16.dp))
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
            userType = TipoUsuario.PROFESOR,
            onNavigateBack = {},
            onLoginSuccess = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        LoginScreen(
            userType = TipoUsuario.FAMILIAR,
            onNavigateBack = {},
            onLoginSuccess = {}
        )
    }
}
