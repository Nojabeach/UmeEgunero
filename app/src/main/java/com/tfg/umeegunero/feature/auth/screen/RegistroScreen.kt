package com.tfg.umeegunero.feature.auth.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring as ComposeSpring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.RegistroUsuarioForm
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.feature.auth.viewmodel.RegistroUiState
import com.tfg.umeegunero.feature.auth.viewmodel.RegistroViewModel
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.EscalatorWarning
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ContentCopy

// Importar los componentes de otros archivos
import com.tfg.umeegunero.feature.auth.screen.TipoFamiliarOptions
import com.tfg.umeegunero.feature.auth.screen.DireccionStep
import com.tfg.umeegunero.feature.auth.screen.AlumnosCentroStep
import com.tfg.umeegunero.BuildConfig

// Función para manejar la luminosidad ya que 'luminance' es privado
fun Color.getLuminance(): Float {
    // Esta función calcula la luminosidad de un color similar a 'luminance' internamente
    val r = this.red
    val g = this.green 
    val b = this.blue
    return (0.299f * r + 0.587f * g + 0.114f * b)
}

/**
 * Función de extensión para determinar si un ColorScheme es claro u oscuro
 */
fun ColorScheme.isLight(): Boolean {
    // La luminosidad del color background puede ser un buen indicador
    return background.getLuminance() > 0.5f
}

// Función para calcular el porcentaje de completado de la contraseña
fun String.calcularPorcentajeCompletado(): Float {
    var porcentaje = 0f
    
    // Longitud mínima (8 caracteres)
    if (this.length >= 8) porcentaje += 0.25f
    
    // Contiene dígitos
    if (this.contains(Regex("[0-9]"))) porcentaje += 0.25f
    
    // Contiene letras mayúsculas y minúsculas
    if (this.contains(Regex("[a-z]")) && this.contains(Regex("[A-Z]"))) porcentaje += 0.25f
    
    // Contiene caracteres especiales
    if (this.contains(Regex("[@#$%^&+=!]"))) porcentaje += 0.25f
    
    return porcentaje
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
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDepuracionDialog by remember { mutableStateOf(false) }
    
    // Simplificar código sin animaciones
    val passwordIconColor = if (validatePassword(uiState.form.password)) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.error

    // Calcular el porcentaje de completado
    val porcentajeCompletado = uiState.form.password.calcularPorcentajeCompletado()

    // Detector de éxito en el registro
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onRegistroCompletado()
        }
    }

    // Mostrar errores en Snackbar
    LaunchedEffect(uiState.error, uiState.passwordError) {
        if (uiState.error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(message = uiState.error ?: "Error desconocido")
                viewModel.clearError()
            }
        } else if (uiState.passwordError != null) {
            scope.launch {
                snackbarHostState.showSnackbar(message = uiState.passwordError ?: "")
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
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
        )
    }

    // Interfaz principal
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registro Familiar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            } else {
                // Formulario de registro
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progreso de registro
                    if (BuildConfig.DEBUG) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { showDepuracionDialog = true }) {
                                Icon(Icons.Default.Info, contentDescription = "Información de depuración")
                            }
                        }
                    }
                    
                    // Formulario elegante con efecto de elevación
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Datos de Acceso",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Email
                            OutlinedTextField(
                                value = uiState.form.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                label = { Text("Email") },
                                placeholder = { Text("ejemplo@dominio.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email"
                                    )
                                },
                                supportingText = {
                                    if (uiState.emailError != null) {
                                        Text(uiState.emailError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                isError = uiState.emailError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                )
                            )
                            
                            // DNI
                            OutlinedTextField(
                                value = uiState.form.dni,
                                onValueChange = { viewModel.updateDni(it) },
                                label = { Text("DNI/NIE") },
                                placeholder = { Text("12345678X") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Pin,
                                        contentDescription = "DNI/NIE"
                                    )
                                },
                                supportingText = {
                                    if (uiState.dniError != null) {
                                        Text(uiState.dniError!!, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Este documento servirá como tu identificador único")
                                    }
                                },
                                isError = uiState.dniError != null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                            
                            // Contraseña
                            OutlinedTextField(
                                value = uiState.form.password,
                                onValueChange = { viewModel.updatePassword(it) },
                                label = { Text("Contraseña") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Contraseña",
                                        tint = passwordIconColor
                                    )
                                },
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { showPasswordRequirements = !showPasswordRequirements }) {
                                            Icon(Icons.Default.Info, "Requisitos de contraseña")
                                        }
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
                                supportingText = {
                                    if (uiState.passwordError != null) {
                                        Text(uiState.passwordError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                isError = uiState.passwordError != null
                            )
                            
                            // Indicador de fortaleza de contraseña
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when {
                                            porcentajeCompletado < 0.5f -> "Débil"
                                            porcentajeCompletado < 0.75f -> "Media"
                                            porcentajeCompletado < 1f -> "Fuerte"
                                            else -> "Muy fuerte"
                                        },
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    
                                    Text(
                                        text = "${(porcentajeCompletado * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                
                                LinearProgressIndicator(
                                    progress = { porcentajeCompletado },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = when {
                                        porcentajeCompletado < 0.5f -> MaterialTheme.colorScheme.error
                                        porcentajeCompletado < 0.75f -> MaterialTheme.colorScheme.tertiary
                                        porcentajeCompletado < 1f -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            
                            // Animación para mostrar los requisitos de contraseña
                            AnimatedVisibility(
                                visible = showPasswordRequirements,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                PasswordRequirementsCard(password = uiState.form.password)
                            }
                            
                            // Sugerencias de contraseña
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        showPasswordSuggestions = true
                                    }
                                ) {
                                    Text("¿Necesitas ayuda con la contraseña?")
                                }
                            }
                        }
                    }
                    
                    // Nombre
                    OutlinedTextField(
                        value = uiState.form.nombre,
                        onValueChange = { viewModel.updateNombre(it) },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Nombre"
                            )
                        },
                        supportingText = {
                            if (uiState.nombreError != null) {
                                Text(uiState.nombreError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = uiState.nombreError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Apellidos
                    OutlinedTextField(
                        value = uiState.form.apellidos,
                        onValueChange = { viewModel.updateApellidos(it) },
                        label = { Text("Apellidos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TextFormat,
                                contentDescription = "Apellidos"
                            )
                        },
                        supportingText = {
                            if (uiState.apellidosError != null) {
                                Text(uiState.apellidosError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = uiState.apellidosError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Teléfono
                    OutlinedTextField(
                        value = uiState.form.telefono,
                        onValueChange = { viewModel.updateTelefono(it) },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Teléfono"
                            )
                        },
                        supportingText = {
                            if (uiState.telefonoError != null) {
                                Text(uiState.telefonoError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        isError = uiState.telefonoError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Tipo de relación familiar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Relación familiar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Opciones de relación familiar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val tiposFamiliares = listOf("Padre", "Madre", "Tutor")
                            
                            tiposFamiliares.forEach { tipo ->
                                RelacionFamiliarOptionCard(
                                    selected = uiState.form.subtipo.name == tipo.uppercase(),
                                    onClick = { 
                                        viewModel.updateSubtipoFamiliar(
                                            SubtipoFamiliar.valueOf(tipo.uppercase())
                                        ) 
                                    },
                                    title = tipo
                                )
                            }
                        }
                    }
                    
                    // Términos y condiciones
                    TermsAndConditionsCard()
                    
                    // Botón de registro
                    Button(
                        onClick = { 
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            viewModel.submitRegistration() 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isFormValid(uiState) && !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Registrarse")
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de sugerencias de contraseña
    if (showPasswordSuggestions) {
        AlertDialog(
            onDismissRequest = { showPasswordSuggestions = false },
            title = { Text("Sugerencias de contraseñas") },
            text = {
                Column {
                    Text(
                        "Estas son algunas sugerencias de contraseñas seguras:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val suggestions = generatePasswordSuggestions()
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = { 
                                    viewModel.updatePassword(suggestion)
                                    showPasswordSuggestions = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Usar esta contraseña"
                                )
                            }
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
    
    // Diálogo de depuración
    if (showDepuracionDialog) {
        AlertDialog(
            onDismissRequest = { showDepuracionDialog = false },
            title = { Text("Información de depuración") },
            text = {
                Column {
                    Text(
                        "Estado actual del formulario:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Email: ${uiState.form.email}")
                    Text("DNI: ${uiState.form.dni}")
                    Text("Nombre: ${uiState.form.nombre}")
                    Text("Apellidos: ${uiState.form.apellidos}")
                    Text("Teléfono: ${uiState.form.telefono}")
                    Text("Tipo familiar: ${uiState.form.subtipo.name ?: "No seleccionado"}")
                    Text("Porcentaje completado: ${(porcentajeCompletado * 100).toInt()}%")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Errores:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text("Email: ${uiState.emailError ?: "Sin errores"}")
                    Text("DNI: ${uiState.dniError ?: "Sin errores"}")
                    Text("Nombre: ${uiState.nombreError ?: "Sin errores"}")
                    Text("Apellidos: ${uiState.apellidosError ?: "Sin errores"}")
                    Text("Teléfono: ${uiState.telefonoError ?: "Sin errores"}")
                    Text("Contraseña: ${uiState.passwordError ?: "Sin errores"}")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Estado general:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text("Cargando: ${if (uiState.isLoading) "Sí" else "No"}")
                    Text("Error: ${uiState.error ?: "Sin errores"}")
                    Text("Éxito: ${if (uiState.success) "Sí" else "No"}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDepuracionDialog = false }) {
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Preview con LazyColumn
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Bienvenido a UmeEgunero",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Regístrate para acceder a la información de tus hijos",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Campos de ejemplo
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        OutlinedTextField(
                            value = "",
                            onValueChange = { },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = "",
                            onValueChange = { },
                            label = { Text("DNI") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            
            // Ejemplo de relación familiar
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Relación con el alumno",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RelacionFamiliarOptionCard(
                                selected = true,
                                onClick = { },
                                title = "Padre"
                            )
                            
                            RelacionFamiliarOptionCard(
                                selected = false,
                                onClick = { },
                                title = "Madre"
                            )
                            
                            RelacionFamiliarOptionCard(
                                selected = false,
                                onClick = { },
                                title = "Tutor"
                            )
                            
                            RelacionFamiliarOptionCard(
                                selected = false,
                                onClick = { },
                                title = "Otro"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirementsCard(password: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Requisitos de contraseña:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            PasswordRequirementItem(
                text = "Al menos 6 caracteres",
                isMet = password.length >= 6,
                icon = Icons.Default.TextFields
            )
            
            PasswordRequirementItem(
                text = "Al menos una letra",
                isMet = password.any { it.isLetter() },
                icon = Icons.Default.TextFormat
            )
            
            PasswordRequirementItem(
                text = "Al menos un número",
                isMet = password.any { it.isDigit() },
                icon = Icons.Default.Tag
            )
            
            PasswordRequirementItem(
                text = "Al menos un carácter especial",
                isMet = password.any { !it.isLetterOrDigit() },
                icon = Icons.Default.Lock
            )
        }
    }
}

@Composable
fun PasswordRequirementItem(
    text: String,
    isMet: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else icon,
            contentDescription = null,
            tint = if (isMet) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMet) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun TermsAndConditionsCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Términos y condiciones",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Al registrarte, aceptas nuestros términos y condiciones de uso y nuestra política de privacidad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { /* Abrir términos completos */ }) {
                    Text("Ver términos completos")
                }
            }
        }
    }
}

@Composable
fun RelacionFamiliarOptionCard(
    selected: Boolean,
    onClick: () -> Unit,
    title: String
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when(title) {
                    "Padre" -> Icons.Default.Man
                    "Madre" -> Icons.Default.Woman
                    "Tutor" -> Icons.Default.EscalatorWarning
                    else -> Icons.Default.Person
                },
                contentDescription = null,
                tint = if (selected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Función para calcular el porcentaje de completado del formulario
 */
private fun calcularPorcentajeCompletado(uiState: RegistroUiState): Float {
    var camposCompletados = 0
    val totalCampos = 7 // email, dni, nombre, apellidos, telefono, password, subtipo
    
    if (uiState.form.email.isNotBlank()) camposCompletados++
    if (uiState.form.dni.isNotBlank()) camposCompletados++
    if (uiState.form.nombre.isNotBlank()) camposCompletados++
    if (uiState.form.apellidos.isNotBlank()) camposCompletados++
    if (uiState.form.telefono.isNotBlank()) camposCompletados++
    if (uiState.form.password.isNotBlank()) camposCompletados++
    camposCompletados++
    
    return camposCompletados / totalCampos.toFloat()
}

/**
 * Función para validar si el formulario es válido
 */
private fun isFormValid(uiState: RegistroUiState): Boolean {
    return uiState.form.email.isNotBlank() &&
           uiState.form.dni.isNotBlank() &&
           uiState.form.nombre.isNotBlank() &&
           uiState.form.apellidos.isNotBlank() &&
           uiState.form.telefono.isNotBlank() &&
           uiState.form.password.isNotBlank() &&
           validatePassword(uiState.form.password) &&
           uiState.emailError == null &&
           uiState.dniError == null &&
           uiState.nombreError == null &&
           uiState.apellidosError == null &&
           uiState.telefonoError == null
}

/**
 * Función para validar si la contraseña es válida
 */
private fun validatePassword(password: String): Boolean {
    return password.length >= 6 &&
           password.any { it.isLetter() } &&
           password.any { it.isDigit() }
}

/**
 * Función para calcular la fortaleza de la contraseña
 */
private fun calculatePasswordStrength(password: String): Float {
    if (password.isBlank()) return 0f
    
    var strength = 0.0f
    
    // Longitud
    strength += minOf(0.4f, password.length * 0.033f)
    
    // Diversidad de caracteres
    val hasLowercase = password.any { it.isLowerCase() }
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    
    if (hasLowercase) strength += 0.15f
    if (hasUppercase) strength += 0.15f
    if (hasDigit) strength += 0.15f
    if (hasSpecial) strength += 0.15f
    
    return minOf(1.0f, strength)
}

/**
 * Función para generar sugerencias de contraseñas
 */
private fun generatePasswordSuggestions(): List<String> {
    val base = listOf(
        "Escuela", "Colegio", "Familia", "Educacion", "Aprender"
    )
    
    val numbers = listOf("2023", "2024", "123", "456", "789")
    val special = listOf("!", "@", "#", "$", "%")
    
    return List(4) { index ->
        val word = base[index % base.size]
        val number = numbers[index % numbers.size]
        val specialChar = special[index % special.size]
        
        "${word.capitalize()}${number}${specialChar}"
    }
}

/**
 * Componente para mostrar el progreso del formulario
 */
@Composable
fun FormProgressIndicator(progress: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${(progress * 100).toInt()}% completado",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}