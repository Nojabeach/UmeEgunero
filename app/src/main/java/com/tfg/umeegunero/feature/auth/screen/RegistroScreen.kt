package com.tfg.umeegunero.feature.auth.screen

import android.content.res.Configuration
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring as ComposeSpring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.RegistroUsuarioForm
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.feature.auth.viewmodel.RegistroUiState
import com.tfg.umeegunero.feature.auth.viewmodel.RegistroViewModel
import com.tfg.umeegunero.feature.common.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.MenuAnchorType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

// Importar los componentes de otros archivos
import com.tfg.umeegunero.feature.auth.screen.TipoFamiliarOptions
import com.tfg.umeegunero.feature.auth.screen.DireccionStep
import com.tfg.umeegunero.feature.auth.screen.AlumnosCentroStep

// Extensión para verificar si el tema es claro
fun ColorScheme.isLight(): Boolean {
    // En Material 3, podemos usar esta aproximación para detectar si estamos en tema claro
    val backgroundColor = this.background
    // Calculamos un valor aproximado de luminosidad (0.0 - 1.0)
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return luminance > 0.5
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRegistroCompletado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showPasswordRequirements by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showPasswordSuggestions by remember { mutableStateOf(false) }
    
    // Simplificar código sin animaciones
    val passwordIconColor = if (isPasswordValid(uiState.form.password)) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.error

    // Calcular el porcentaje de completado
    val porcentajeCompletado = calcularPorcentajeCompletado(uiState)

    // Detector de éxito en el registro
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onRegistroCompletado()
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
    val isLight = MaterialTheme.colorScheme.isLight()

    // Crear un gradiente elegante para el fondo
    val gradientColors = if (!isLight) {
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    } else {
        listOf(
            Color(0xFFF0F4FF),
            Color(0xFFF8F9FF),
            Color(0xFFF0FAFF)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.isLoading) "Registrando..." else "Registro Familiar",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Indicador de progreso
                if (!uiState.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormProgressIndicator(
                            currentStep = (porcentajeCompletado * 100).toInt(),
                            totalSteps = 100
                        )
                        Text(
                            text = "${(porcentajeCompletado * 100).toInt()}% completado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Logo
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .padding(vertical = 32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                // Título y descripción
                Text(
                    text = "Bienvenido a UmeEgunero",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Regístrate para acceder a la información de tus hijos",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Formulario de registro
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (uiState.isLoading) 8.dp else 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Email con feedback
                        OutlinedTextField(
                            value = uiState.form.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            isError = uiState.formErrors["email"] != null,
                            supportingText = {
                                uiState.formErrors["email"]?.let { 
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Contraseña con feedback
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.form.password,
                                onValueChange = { viewModel.updatePassword(it) },
                                label = { Text("Contraseña") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { showPasswordSuggestions = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = "Sugerencias de contraseña",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { showPasswordRequirements = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Requisitos de contraseña",
                                                tint = passwordIconColor
                                            )
                                        }
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                            )
                                        }
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                isError = uiState.formErrors["password"] != null,
                                supportingText = {
                                    Column {
                                        uiState.formErrors["password"]?.let { 
                                            Text(
                                                text = it,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        if (uiState.form.password.isNotBlank()) {
                                            LinearProgressIndicator(
                                                progress = { calculatePasswordStrength(uiState.form.password) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .padding(top = 4.dp),
                                                color = when {
                                                    calculatePasswordStrength(uiState.form.password) < 0.3f -> MaterialTheme.colorScheme.error
                                                    calculatePasswordStrength(uiState.form.password) < 0.7f -> MaterialTheme.colorScheme.tertiary
                                                    else -> MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Botón de registro
                        Button(
                            onClick = { viewModel.submitRegistration() },
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .padding(end = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            }
                            Text("Registrarse con Email")
                        }

                        // Separador
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(
                                text = "o",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Divider(modifier = Modifier.weight(1f))
                        }

                        // Botón de registro con Google
                        OutlinedButton(
                            onClick = { /* Implementar registro con Google */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle, // Cambiar por icono de Google
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Registrarse con Google")
                        }
                    }
                }

                // Términos y condiciones
                TermsAndConditions()

                // Información sobre el proceso de aprobación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Proceso de Registro",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "1. Completa el registro con tu email o cuenta de Google\n" +
                                  "2. El administrador del centro revisará tu solicitud\n" +
                                  "3. Una vez aprobada, podrás acceder a la información de tus hijos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }

    // Diálogo mejorado de requisitos de contraseña
    if (showPasswordRequirements) {
        AlertDialog(
            onDismissRequest = { showPasswordRequirements = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Requisitos de Contraseña",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tu contraseña debe cumplir los siguientes requisitos:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    PasswordRequirementItem(
                        text = "Mínimo 8 caracteres",
                        isMet = uiState.form.password.length >= 8,
                        icon = Icons.Default.FormatSize
                    )
                    PasswordRequirementItem(
                        text = "Al menos una mayúscula",
                        isMet = uiState.form.password.any { it.isUpperCase() },
                        icon = Icons.Default.TextFormat
                    )
                    PasswordRequirementItem(
                        text = "Al menos una minúscula",
                        isMet = uiState.form.password.any { it.isLowerCase() },
                        icon = Icons.Default.TextFormat
                    )
                    PasswordRequirementItem(
                        text = "Al menos un número",
                        isMet = uiState.form.password.any { it.isDigit() },
                        icon = Icons.Default.Pin
                    )
                    PasswordRequirementItem(
                        text = "Al menos un carácter especial",
                        isMet = uiState.form.password.any { !it.isLetterOrDigit() },
                        icon = Icons.Default.Grade
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPasswordRequirements = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Diálogo de sugerencias de contraseña
    if (showPasswordSuggestions) {
        AlertDialog(
            onDismissRequest = { showPasswordSuggestions = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sugerencias de Contraseña",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Aquí tienes algunas sugerencias de contraseñas seguras:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    generatePasswordSuggestions().forEach { password ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.updatePassword(password)
                                    showPasswordSuggestions = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = password,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar contraseña",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPasswordSuggestions = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    UmeEguneroTheme {
        PreviewRegistroScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegistroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        PreviewRegistroScreen()
    }
}

@Composable
fun PreviewRegistroScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Familiar") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Aquí puedes agregar el contenido del scaffold para el preview
        Box(modifier = Modifier.padding(paddingValues)) {
            Text("Contenido de preview")
        }
    }
}

@Composable
private fun PasswordRequirementItem(
    text: String,
    isMet: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMet) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TermsAndConditions() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Al registrarte, aceptas nuestros ",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "términos y condiciones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { /* Implementar navegación a términos y condiciones */ }
        )
    }
}

private fun calcularPorcentajeCompletado(uiState: RegistroUiState): Float {
    var camposCompletados = 0
    var camposTotales = 2 // Email y contraseña

    if (uiState.form.email.isNotBlank()) camposCompletados++
    if (uiState.form.password.isNotBlank() && isPasswordValid(uiState.form.password)) camposCompletados++

    return camposCompletados.toFloat() / camposTotales.toFloat()
}

private fun isPasswordValid(password: String): Boolean {
    return password.length >= 8 &&
           password.any { it.isUpperCase() } &&
           password.any { it.isLowerCase() } &&
           password.any { it.isDigit() } &&
           password.any { !it.isLetterOrDigit() }
}

private fun calculatePasswordStrength(password: String): Float {
    var strength = 0f
    
    // Longitud mínima
    if (password.length >= 8) strength += 0.2f
    
    // Mayúsculas
    if (password.any { it.isUpperCase() }) strength += 0.2f
    
    // Minúsculas
    if (password.any { it.isLowerCase() }) strength += 0.2f
    
    // Números
    if (password.any { it.isDigit() }) strength += 0.2f
    
    // Caracteres especiales
    if (password.any { !it.isLetterOrDigit() }) strength += 0.2f
    
    return strength.coerceIn(0f, 1f)
}

private fun generatePasswordSuggestions(): List<String> {
    val adjectives = listOf("Fuerte", "Segura", "Rápida", "Ágil", "Valiente")
    val nouns = listOf("Contraseña", "Clave", "Llave", "Código", "Símbolo")
    val numbers = (1000..9999).random()
    val specialChars = listOf("!", "@", "#", "$", "%", "&", "*")
    
    return List(3) {
        "${adjectives.random()}${nouns.random()}$numbers${specialChars.random()}"
    }
}