package com.tfg.umeegunero.feature.auth.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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

    // TODO: Mejoras pendientes de implementar
    // - Registro con credenciales de redes sociales: implementar la lógica de autenticación
    // - Mejoras de feedback visual: animaciones y transiciones más fluidas
    // - Formularios específicos según tipo de usuario: mostrar campos específicos según el subtipo familiar seleccionado
    // - Verificación de email mediante OTP: el código actual envía verificación pero no tiene flujo de validación
    // - Mejoras de accesibilidad: validar que todos los componentes sean accesibles
    // - Implementar un sistema avanzado de manejo de errores
    // - Completar la implementación del escaneo de código QR

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

    // Crear un gradiente elegante para el fondo, estilo iOS
    val gradientColors = if (!isLight) {
        // Gradiente para modo oscuro
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    } else {
        // Gradiente para modo claro, estilo iOS
        listOf(
            Color(0xFFF0F4FF), // Azul muy claro
            Color(0xFFF8F9FF), // Casi blanco con tinte azul
            Color(0xFFF0FAFF)  // Azul muy claro con tinte cyan
        )
    }

    // Validación en tiempo real
    fun validateFields() {
        viewModel.updateForm()
    }

    // Enviar código de verificación al email
    fun sendVerificationEmail() {
        viewModel.sendVerificationEmail()
    }

    // Botones de registro con redes sociales (mejorado)
    @Composable
    fun SocialMediaButtons() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "O regístrate con:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { /* Lógica para registro con Google */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    // Aquí iría el icono de Google
                    Text(
                        text = "Google",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = { /* Lógica para registro con Facebook */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1877F2) // Color azul de Facebook
                    )
                ) {
                    // Aquí iría el icono de Facebook
                    Text(
                        text = "Facebook",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = { /* Lógica para registro con Apple */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    // Aquí iría el icono de Apple
                    Text(
                        text = "Apple",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
    
    // Visualización mejorada de requisitos de seguridad de contraseña
    @Composable
    fun PasswordRequirements(password: String) {
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Requisitos de seguridad:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasMinLength) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasMinLength) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " Mínimo 8 caracteres",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasMinLength) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasUppercase) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasUppercase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " Al menos una mayúscula",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasUppercase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasLowercase) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasLowercase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " Al menos una minúscula",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasLowercase) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasDigit) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasDigit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " Al menos un número",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasDigit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (hasSpecialChar) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (hasSpecialChar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " Al menos un carácter especial",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasSpecialChar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    
    // Escaneo de código QR (botón mejorado)
    @Composable
    fun QRCodeScanButton() {
        Button(
            onClick = { viewModel.scanQRCode() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle, // Cambiar por un icono de QR cuando esté disponible
                contentDescription = "Escanear QR",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Escanear código QR de invitación")
        }
    }
    
    // Términos y condiciones (mejorado)
    @Composable
    fun TermsAndConditions() {
        var termsAccepted by remember { mutableStateOf(false) }
        var privacyDialogOpen by remember { mutableStateOf(false) }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { newValue -> termsAccepted = newValue }
                )
                
                Text(
                    text = "Acepto los ",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "términos y condiciones",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable { privacyDialogOpen = true }
                )
            }
            
            if (privacyDialogOpen) {
                AlertDialog(
                    onDismissRequest = { privacyDialogOpen = false },
                    title = { Text("Términos y Condiciones") },
                    text = {
                        Text(
                            "Al registrarte en nuestra aplicación, aceptas los términos y condiciones " +
                            "de uso. Esto incluye el almacenamiento de tus datos personales para " +
                            "brindarte el servicio y la comunicación con el centro educativo."
                        )
                    },
                    confirmButton = {
                        Button(onClick = { 
                            privacyDialogOpen = false
                            termsAccepted = true
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { privacyDialogOpen = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Familiar") },
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de progreso
                FormProgressIndicator(
                    currentStep = uiState.currentStep,
                    totalSteps = uiState.totalSteps
                )

                // Título del paso actual
                Text(
                    text = when (uiState.currentStep) {
                        1 -> "Datos Personales"
                        2 -> "Dirección"
                        3 -> "Alumnos y Centro"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Contenido del paso actual
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedContent(
                        targetState = uiState.currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { -it },
                                            animationSpec = tween(300)
                                        ) + fadeOut(animationSpec = tween(300))
                            } else {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = tween(300)
                                        ) + fadeOut(animationSpec = tween(300))
                            }
                        },
                        label = "Step Animation",
                        modifier = Modifier.weight(1f)
                    ) { step ->
                        when (step) {
                            1 -> DatosPersonalesStep(
                                dni = uiState.form.dni,
                                email = uiState.form.email,
                                password = uiState.form.password,
                                confirmPassword = uiState.form.confirmPassword,
                                nombre = uiState.form.nombre,
                                apellidos = uiState.form.apellidos,
                                telefono = uiState.form.telefono,
                                subtipo = uiState.form.subtipo,
                                onDniChange = { viewModel.updateDni(it) },
                                onEmailChange = { viewModel.updateEmail(it) },
                                onPasswordChange = { viewModel.updatePassword(it) },
                                onConfirmPasswordChange = { viewModel.updateConfirmPassword(it) },
                                onNombreChange = { viewModel.updateNombre(it) },
                                onApellidosChange = { viewModel.updateApellidos(it) },
                                onTelefonoChange = { viewModel.updateTelefono(it) },
                                onSubtipoChange = { viewModel.updateSubtipoFamiliar(it) },
                                errors = uiState.formErrors
                            )

                            2 -> DireccionStep(
                                calle = uiState.form.direccion.calle,
                                numero = uiState.form.direccion.numero,
                                piso = uiState.form.direccion.piso,
                                codigoPostal = uiState.form.direccion.codigoPostal,
                                ciudad = uiState.form.direccion.ciudad,
                                provincia = uiState.form.direccion.provincia,
                                onCalleChange = { viewModel.updateFormField("calle", it) },
                                onNumeroChange = { viewModel.updateFormField("numero", it) },
                                onPisoChange = { viewModel.updateFormField("piso", it) },
                                onCodigoPostalChange = {
                                    viewModel.updateFormField(
                                        "codigoPostal",
                                        it
                                    )
                                },
                                onCiudadChange = { viewModel.updateFormField("ciudad", it) },
                                onProvinciaChange = { viewModel.updateFormField("provincia", it) },
                                errors = uiState.formErrors
                            )

                            3 -> AlumnosCentroStep(
                                alumnos = uiState.form.alumnosDni,
                                centroId = uiState.form.centroId,
                                centros = uiState.centros,
                                isLoadingCentros = uiState.isLoadingCentros,
                                onAddAlumno = { viewModel.addAlumnoDni(it) },
                                onRemoveAlumno = { viewModel.removeAlumnoDni(it) },
                                onCentroSelect = { viewModel.updateFormField("centroId", it) },
                                errors = uiState.formErrors
                            )

                            else -> Box {}
                        }
                    }

                    // Botones de navegación al final de la columna
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.currentStep > 1) {
                                OutlinedButton(
                                    onClick = { viewModel.previousStep() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Anterior")
                                }
                            }

                            if (uiState.currentStep < uiState.totalSteps) {
                                Button(
                                    onClick = { viewModel.nextStep() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Siguiente")
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.submitRegistration() },
                                    enabled = !uiState.isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .height(24.dp)
                                                .padding(end = 8.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    Text("Registrarse")
                                }
                            }
                        }
                    }

                    // Requisitos de seguridad de contraseña
                    PasswordRequirements(uiState.form.password)

                    // Botones de redes sociales
                    SocialMediaButtons()

                    // Checkbox para términos y condiciones
                    TermsAndConditions()
                }
            }
        }
    }

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
                        isError = errors["dni"] != null,
                        supportingText = {
                            errors["dni"]?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            isError = errors["email"] != null,
                            supportingText = {
                                errors["email"]?.let { Text(it) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (email.isNotEmpty() && errors["email"] == null) {
                            IconButton(
                                onClick = { sendVerificationEmail() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verificar email",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

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
                        isError = errors["password"] != null,
                        supportingText = {
                            errors["password"]?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Mostrar requisitos de la contraseña si se está escribiendo
                    if (password.isNotEmpty()) {
                        PasswordRequirements(password)
                    }

                    // Confirmar Contraseña
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }) {
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
                        isError = errors["confirmPassword"] != null,
                        supportingText = {
                            if (errors["confirmPassword"] != null) {
                                Text(errors["confirmPassword"] ?: "")
                            } else if (confirmPassword.isNotEmpty() && confirmPassword == password) {
                                Text(
                                    text = "Las contraseñas coinciden",
                                    color = MaterialTheme.colorScheme.primary
                                )
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
                        isError = errors["nombre"] != null,
                        supportingText = {
                            errors["nombre"]?.let { Text(it) }
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
                        isError = errors["apellidos"] != null,
                        supportingText = {
                            errors["apellidos"]?.let { Text(it) }
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
                        isError = errors["telefono"] != null,
                        supportingText = {
                            errors["telefono"]?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tipo de familiar
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
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
    fun AlumnosCentroStep(
        alumnos: List<String>,
        centroId: String,
        centros: List<Centro>,
        isLoadingCentros: Boolean,
        onAddAlumno: (String) -> Unit,
        onRemoveAlumno: (String) -> Unit,
        onCentroSelect: (String) -> Unit,
        errors: Map<String, String>
    ) {
        val scrollState = rememberScrollState()
        var nuevoDni by remember { mutableStateOf("") }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Escanear código QR para invitación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Tienes un código de invitación?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    QRCodeScanButton()
                }
            }
            
            // Selección de centro
            if (!isLoadingCentros) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Selecciona un centro",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Dropdown de centros
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { /* No change needed for preview */ }
                        ) {
                            OutlinedTextField(
                                value = centros.find { it.id == centroId }?.nombre ?: "Selecciona un centro",
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = false,
                                onDismissRequest = { }
                            ) {
                                centros.forEach { centro ->
                                    DropdownMenuItem(
                                        text = { Text(centro.nombre) },
                                        onClick = { onCentroSelect(centro.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Lista de alumnos
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Alumnos asociados",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Lista de alumnos actuales
                    alumnos.forEach { alumno ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(alumno)
                            IconButton(onClick = { onRemoveAlumno(alumno) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar alumno")
                            }
                        }
                    }
                    
                    // Agregar nuevo alumno
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nuevoDni,
                            onValueChange = { nuevoDni = it },
                            label = { Text("DNI del alumno") },
                            modifier = Modifier.weight(1f),
                            isError = errors["alumno"] != null
                        )
                        IconButton(onClick = {
                            if (nuevoDni.isNotEmpty()) {
                                onAddAlumno(nuevoDni)
                                nuevoDni = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir alumno")
                        }
                    }
                    
                    // Mostrar error si existe
                    if (errors["alumno"] != null) {
                        Text(
                            text = errors["alumno"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
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