/**
 * Módulo de pantallas de autenticación para la aplicación UmeEgunero.
 * 
 * Este módulo contiene las pantallas relacionadas con el proceso de autenticación y registro de usuarios.
 * 
 * @see RegistroScreen
 * @see RegistroViewModel
 * @see RegistroUiState
 */
package com.tfg.umeegunero.feature.auth.screen

// Imports de Android y Jetpack Compose
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.SensorDoor
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.Segment
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions

/**
 * Función de extensión para calcular la luminosidad de un color.
 * 
 * Esta función calcula la luminosidad de un color RGB utilizando la fórmula estándar de luminosidad.
 * 
 * @return La luminosidad del color como un valor entre 0.0 y 1.0
 * 
 * @see Color
 */
fun Color.getLuminance(): Float {
    val r = this.red
    val g = this.green 
    val b = this.blue
    return (0.299f * r + 0.587f * g + 0.114f * b)
}

/**
 * Función de extensión para determinar si un ColorScheme es claro u oscuro.
 * 
 * @return `true` si el esquema de color es claro, `false` si es oscuro
 * 
 * @see ColorScheme
 */
fun ColorScheme.isLight(): Boolean {
    return background.getLuminance() > 0.5f
}

/**
 * Función de extensión para calcular el porcentaje de completado de la contraseña.
 * 
 * Esta función evalúa la fortaleza de una contraseña basándose en varios criterios:
 * - Longitud mínima (8 caracteres)
 * - Presencia de dígitos
 * - Presencia de letras mayúsculas y minúsculas
 * - Presencia de caracteres especiales
 * 
 * @return Un valor entre 0.0 y 1.0 que representa el porcentaje de completado
 * 
 * @sample "Password123!".calcularPorcentajeCompletado() // Devuelve 1.0
 * @sample "password".calcularPorcentajeCompletado() // Devuelve 0.25
 */
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

/**
 * Función para validar el formato de un DNI español.
 * 
 * Esta función implementa la validación completa de un DNI español, incluyendo:
 * - Formato correcto (8 números y 1 letra)
 * - Letras permitidas (excluye I, Ñ, O, U)
 * - Validación de la letra de control según el algoritmo oficial español
 * 
 * @param dni El DNI a validar
 * @return `true` si el DNI es válido, `false` en caso contrario
 * 
 * @throws NumberFormatException Si el número del DNI no es un entero válido
 * 
 * @sample validateDni("12345678Z") // Devuelve true para un DNI válido
 * @sample validateDni("12345678I") // Devuelve false para un DNI inválido
 */
private fun validateDni(dni: String): Boolean {
    val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
    if (!dniPattern.matches(dni.uppercase())) return false
    val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
    val numero = dni.substring(0, 8).toIntOrNull() ?: return false
    return dni.uppercase().getOrNull(8) == letras.getOrNull(numero % 23)
}

/**
 * Función para validar si el formulario es válido.
 * 
 * Esta función verifica que todos los campos obligatorios del formulario estén completos
 * y cumplan con las validaciones correspondientes.
 * 
 * @param uiState El estado actual del formulario
 * @return `true` si el formulario es válido, `false` en caso contrario
 * 
 * @see RegistroUiState
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
 * Función para validar si la contraseña es válida.
 * 
 * Esta función verifica que la contraseña cumpla con los requisitos mínimos:
 * - Longitud mínima de 6 caracteres
 * - Al menos una letra
 * - Al menos un dígito
 * 
 * @param password La contraseña a validar
 * @return `true` si la contraseña es válida, `false` en caso contrario
 */
private fun validatePassword(password: String): Boolean {
    return password.length >= 6 &&
           password.any { it.isLetter() } &&
           password.any { it.isDigit() }
}

/**
 * Función para calcular la fortaleza de la contraseña.
 * 
 * Esta función evalúa la fortaleza de una contraseña basándose en:
 * - Longitud
 * - Diversidad de caracteres (mayúsculas, minúsculas, números, especiales)
 * 
 * @param password La contraseña a evaluar
 * @return Un valor entre 0.0 y 1.0 que representa la fortaleza de la contraseña
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
 * Función para generar sugerencias de contraseñas seguras.
 * 
 * Esta función genera una lista de contraseñas seguras combinando:
 * - Palabras base relacionadas con la educación
 * - Números
 * - Caracteres especiales
 * 
 * @return Lista de sugerencias de contraseñas seguras
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
        
        "${word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}${number}${specialChar}"
    }
}

/**
 * Valida si los campos del Paso 1 (Datos Personales) del formulario son válidos.
 *
 * @param form El estado actual del formulario de registro.
 * @param errors Mapa de errores de validación en tiempo real (e.g., formato email).
 * @return `true` si los campos del Paso 1 son válidos, `false` en caso contrario.
 */
private fun isStep1Valid(form: RegistroUsuarioForm, errors: Map<String, String?>): Boolean {
    return form.email.isNotBlank() &&
           form.dni.isNotBlank() &&
           form.nombre.isNotBlank() &&
           form.apellidos.isNotBlank() &&
           form.telefono.isNotBlank() &&
           form.password.isNotBlank() &&
           validatePassword(form.password) && // Asumiendo que validatePassword ya existe
           form.password == form.confirmPassword && // Añadir validación de confirmación
           errors["email"] == null &&
           errors["dni"] == null &&
           errors["password"] == null &&
           errors["confirmPassword"] == null &&
           errors["nombre"] == null &&
           errors["apellidos"] == null &&
           errors["telefono"] == null
}

/**
 * Valida si los campos del Paso 2 (Dirección) del formulario son válidos.
 *
 * @param form El estado actual del formulario de registro.
 * @return `true` si los campos del Paso 2 son válidos, `false` en caso contrario.
 */
private fun isStep2Valid(form: RegistroUsuarioForm): Boolean {
    // Validar que los campos obligatorios de dirección no estén vacíos
    return form.direccion.calle.isNotBlank() &&
           form.direccion.numero.isNotBlank() && // Asumiendo número es obligatorio
           form.direccion.codigoPostal.isNotBlank() &&
           form.direccion.ciudad.isNotBlank() &&
           form.direccion.provincia.isNotBlank()
    // 'piso' puede ser opcional
}

/**
 * Valida si los campos del Paso 3 (Datos Alumnos/Centro) del formulario son válidos.
 *
 * @param form El estado actual del formulario de registro.
 * @return `true` si los campos del Paso 3 son válidos, `false` en caso contrario.
 */
private fun isStep3Valid(form: RegistroUsuarioForm): Boolean {
    // Validar que se haya seleccionado un centro
    val centroValido = form.centroId.isNotBlank()
    // Validar que todos los DNIs de alumnos introducidos sean válidos o estén vacíos
    val alumnosDniValidos = form.alumnosDni.all { it.isBlank() || validateDni(it) }
    // Opcional: Validar que al menos un DNI de alumno se haya introducido
    val alMenosUnAlumno = form.alumnosDni.any { it.isNotBlank() }

    // Devolver true si el centro es válido, los DNIs son válidos Y al menos un DNI ha sido añadido
    return centroValido && alumnosDniValidos && alMenosUnAlumno
}

/**
 * Determina si el paso actual del formulario es válido.
 *
 * @param uiState El estado actual de la UI de registro.
 * @return `true` si el paso actual es válido, `false` en caso contrario.
 */
private fun isCurrentStepValid(uiState: RegistroUiState): Boolean {
    return when (uiState.currentStep) {
        1 -> isStep1Valid(uiState.form, uiState.formErrors + mapOf( // Incluir errores en tiempo real
            "email" to uiState.emailError,
            "dni" to uiState.dniError,
            "password" to uiState.passwordError,
            "confirmPassword" to uiState.confirmPasswordError,
            "nombre" to uiState.nombreError,
            "apellidos" to uiState.apellidosError,
            "telefono" to uiState.telefonoError
        ))
        2 -> isStep2Valid(uiState.form)
        3 -> isStep3Valid(uiState.form)
        else -> false
    }
}

/**
 * Pantalla de registro para UmeEgunero.
 * 
 * Implementa un formulario de registro multi-paso con validaciones en tiempo real,
 * utilizando Jetpack Compose y Material 3.
 * 
 * ## Características
 * - Formulario multi-paso con indicador de progreso
 * - Validación en tiempo real de campos
 * - Gestión de DNIs de alumnos
 * - Selección de relación familiar
 * - Indicador de fortaleza de contraseña
 * - Términos y condiciones
 * 
 * @param viewModel ViewModel de registro
 * @param onNavigateBack Callback de navegación atrás
 * @param onRegistroCompletado Callback de registro exitoso
 * @param onNavigateToTerminosCondiciones Callback de navegación a términos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRegistroCompletado: () -> Unit,
    onNavigateToTerminosCondiciones: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Estado para controlar la visibilidad de los campos
    var showPasswordRequirements by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showPasswordSuggestions by remember { mutableStateOf(false) }
    var showDepuracionDialog by remember { mutableStateOf(false) }
    var showCamposFaltantesDialog by remember { mutableStateOf(false) }
    
    // Animaciones y estados visuales
    val passwordIconColor = if (validatePassword(uiState.form.password)) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.error

    val porcentajeCompletado = uiState.form.password.calcularPorcentajeCompletado()
    val isLight = MaterialTheme.colorScheme.isLight()

    // Gradiente de fondo
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
            TopAppBar(
                title = { Text("Registro Familiar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                         containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    ),
                    scrollBehavior = scrollBehavior
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    FormProgressIndicator(
                        progress = uiState.calculateOverallProgress(),
                        currentStep = uiState.currentStep,
                        totalSteps = uiState.totalSteps,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StepIndicator(
                        currentStep = uiState.currentStep,
                        totalSteps = uiState.totalSteps,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
            if (uiState.isLoading && uiState.currentStep == 0) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                             when (uiState.currentStep) {
                                 1 -> Step1Content(uiState = uiState, viewModel = viewModel)
                                 2 -> Step2Content(uiState = uiState, viewModel = viewModel)
                                 3 -> Step3Content(uiState = uiState, viewModel = viewModel, focusManager = focusManager)
                             }
                         }
                     }

                     if (uiState.currentStep == uiState.totalSteps) {
                         TermsAndConditionsCard(
                             onNavigateToTerminosCondiciones = onNavigateToTerminosCondiciones
                         )
                     }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            enabled = uiState.currentStep > 1 && !uiState.isLoading
                        ) {
                            Text("Anterior")
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (isCurrentStepValid(uiState)) {
                                     if (uiState.currentStep < uiState.totalSteps) {
                                        viewModel.nextStep()
                                    } else {
                                        viewModel.registrarUsuario()
                                    }
                                } else {
                                    showCamposFaltantesDialog = true
                                }
                            },
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading && uiState.currentStep > 0) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(if (uiState.currentStep < uiState.totalSteps) "Siguiente" else "Finalizar Registro")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCamposFaltantesDialog) {
        CamposFaltantesDialog(
            uiState = uiState,
            currentStep = uiState.currentStep,
            onDismiss = { showCamposFaltantesDialog = false }
        )
    }
}

/**
 * Calcula el progreso general del formulario multi-paso.
 * Ajustado para incluir campos de dirección y centro/alumnos.
 */
fun RegistroUiState.calculateOverallProgress(): Float {
     var camposCompletados = 0
     val totalCamposObligatorios = 13 // 7 (P1) + 5 (P2) + 1 (P3: Centro) (+1 si al menos un DNI alumno es obligatorio)

     // Paso 1
     if (form.email.isNotBlank() && emailError == null) camposCompletados++
     if (form.dni.isNotBlank() && dniError == null) camposCompletados++
     if (form.nombre.isNotBlank() && nombreError == null) camposCompletados++
     if (form.apellidos.isNotBlank() && apellidosError == null) camposCompletados++
     if (form.telefono.isNotBlank() && telefonoError == null) camposCompletados++
     if (form.password.isNotBlank() && passwordError == null && validatePassword(form.password)) camposCompletados++
     if (form.confirmPassword.isNotBlank() && confirmPasswordError == null && form.password == form.confirmPassword) camposCompletados++

     // Paso 2 (Dirección)
     if (form.direccion.calle.isNotBlank()) camposCompletados++
     if (form.direccion.numero.isNotBlank()) camposCompletados++
     if (form.direccion.codigoPostal.isNotBlank()) camposCompletados++
     if (form.direccion.ciudad.isNotBlank()) camposCompletados++
     if (form.direccion.provincia.isNotBlank()) camposCompletados++

     // Paso 3 (Centro/Alumnos)
     if (form.centroId.isNotBlank()) camposCompletados++
     // Opcional: añadir al progreso si al menos un DNI válido es introducido
     // if (form.alumnosDni.any { it.isNotBlank() && validateDni(it) }) camposCompletados++


     return if (totalCamposObligatorios > 0) {
         camposCompletados.toFloat() / totalCamposObligatorios.toFloat()
     } else {
         0f
     }
}

/**
 * Componente para el contenido del Paso 1 del formulario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Content(uiState: RegistroUiState, viewModel: RegistroViewModel) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showPasswordRequirements by remember { mutableStateOf(false) }
     val focusManager = LocalFocusManager.current

    // Email
                            FormField(
                                value = uiState.form.email,
         onValueChange = { viewModel.updateFormField("email", it) },
                                label = "Email",
                                placeholder = "ejemplo@dominio.com",
                                icon = Icons.Default.Email,
                                error = uiState.emailError,
         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
         supportingText = uiState.emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
     )
                            // DNI
    FormField(
                                value = uiState.form.dni,
        onValueChange = { viewModel.updateFormField("dni", it) },
        label = "DNI/NIE",
        placeholder = "12345678X",
        icon = Icons.Default.Pin,
        error = uiState.dniError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
         supportingText = uiState.dniError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } } ?: { Text("Será tu identificador único") }
    )
    // Nombre
     FormField(
         value = uiState.form.nombre,
         onValueChange = { viewModel.updateFormField("nombre", it) },
         label = "Nombre",
         placeholder = null,
         icon = Icons.Default.Person,
         error = uiState.nombreError,
         keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
         supportingText = uiState.nombreError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
     )
    // Apellidos
    FormField(
         value = uiState.form.apellidos,
         onValueChange = { viewModel.updateFormField("apellidos", it) },
         label = "Apellidos",
         placeholder = null,
         icon = Icons.Default.TextFormat, // O usar otro icono relevante
         error = uiState.apellidosError,
         keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
         supportingText = uiState.apellidosError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
     )
    // Teléfono
    FormField(
         value = uiState.form.telefono,
         onValueChange = { viewModel.updateFormField("telefono", it) },
         label = "Teléfono",
         placeholder = null,
         icon = Icons.Default.Phone,
         error = uiState.telefonoError,
         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
         supportingText = uiState.telefonoError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                            )
                            
                            // Contraseña
                            OutlinedTextField(
                                value = uiState.form.password,
         onValueChange = { viewModel.updateFormField("password", it) },
                                label = { Text("Contraseña") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
         leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { showPasswordRequirements = !showPasswordRequirements }) {
                     Icon(Icons.Default.Info, "Requisitos")
                                        }
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                         contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                                            )
                                        }
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
         keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
         isError = uiState.passwordError != null,
         supportingText = uiState.passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
     )
     // Indicador fortaleza y requisitos (si se quieren mantener)
     // ... (LinearProgressIndicator y AnimatedVisibility con PasswordRequirementsCard)

    // Confirmar Contraseña
    OutlinedTextField(
        value = uiState.form.confirmPassword,
        onValueChange = { viewModel.updateFormField("confirmPassword", it) },
        label = { Text("Confirmar Contraseña") },
        singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(
                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar"
                )
            }
        },
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        isError = uiState.confirmPasswordError != null || (uiState.form.password.isNotEmpty() && uiState.form.confirmPassword.isNotEmpty() && uiState.form.password != uiState.form.confirmPassword),
        supportingText = {
             if (uiState.confirmPasswordError != null) {
                 Text(uiState.confirmPasswordError!!, color = MaterialTheme.colorScheme.error)
             } else if (uiState.form.password.isNotEmpty() && uiState.form.confirmPassword.isNotEmpty() && uiState.form.password != uiState.form.confirmPassword) {
                 Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error)
             }
         }
    )

     // Tipo de relación familiar
     Column(modifier = Modifier.fillMaxWidth()) {
         Text(
             text = "Relación familiar",
             style = MaterialTheme.typography.titleMedium,
             fontWeight = FontWeight.Medium
         )
         Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.spacedBy(12.dp)
         ) {
             val tiposFamiliares = SubtipoFamiliar.entries.toList() // Usar enum directamente
             tiposFamiliares.forEach { tipo ->
                 RelacionFamiliarOptionCard(
                     selected = uiState.form.subtipo == tipo,
                     onClick = { viewModel.updateSubtipoFamiliar(tipo) },
                     title = tipo.name.lowercase().replaceFirstChar { it.titlecase() } // Formatear nombre
                 )
             }
         }
     }
}

/**
 * Componente para el contenido del Paso 2 del formulario (Dirección).
 */
@Composable
fun Step2Content(uiState: RegistroUiState, viewModel: RegistroViewModel) {
     val focusManager = LocalFocusManager.current

     Text("Dirección Postal", style = MaterialTheme.typography.titleLarge)
     Spacer(modifier = Modifier.height(16.dp))

     // Calle
     FormField(
         value = uiState.form.direccion.calle,
         onValueChange = { viewModel.updateFormField("calle", it) },
         label = "Calle",
         icon = Icons.AutoMirrored.Filled.Segment,
         keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
         keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
         error = null,
         placeholder = null,
         supportingText = null
     )
     // Número
     FormField(
         value = uiState.form.direccion.numero,
         onValueChange = { viewModel.updateFormField("numero", it) },
         label = "Número",
         icon = Icons.Filled.Numbers,
         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
         keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
         error = null,
         placeholder = null,
         supportingText = null
     )
     // Piso (Opcional?)
     FormField(
         value = uiState.form.direccion.piso ?: "",
         onValueChange = { viewModel.updateFormField("piso", it) },
         label = "Piso/Puerta (Opcional)",
         icon = Icons.Filled.SensorDoor,
         keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
         keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
         error = null,
         placeholder = null,
         supportingText = null
     )
     
     // Código Postal
     FormField(
         value = uiState.form.direccion.codigoPostal,
         onValueChange = { viewModel.updateFormField("codigoPostal", it) },
         label = "Código Postal",
         icon = Icons.Filled.LocalPostOffice,
         keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
         keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
         error = uiState.direccionError,
         placeholder = "28001",
         supportingText = { 
             if (uiState.isLoadingDireccion) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     CircularProgressIndicator(
                         modifier = Modifier.size(16.dp),
                         strokeWidth = 2.dp
                     )
                     Spacer(modifier = Modifier.width(8.dp))
                     Text("Buscando localización...")
                 }
             } else {
                 Text("Ingresa el código postal para autocompletar")
             }
         }
     )

     // Información de geolocalización si está disponible
     AnimatedVisibility(
         visible = uiState.coordenadasLatitud != null && uiState.coordenadasLongitud != null,
         enter = fadeIn() + expandVertically(),
         exit = fadeOut() + shrinkVertically()
     ) {
         Column(
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(vertical = 8.dp)
         ) {
             Text(
                 text = "Información de Localización",
                 style = MaterialTheme.typography.titleMedium,
                 color = MaterialTheme.colorScheme.primary
             )
             Spacer(modifier = Modifier.height(4.dp))
             
             // Coordenadas
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Icon(
                     imageVector = Icons.Default.Map,
                     contentDescription = null,
                     tint = MaterialTheme.colorScheme.secondary,
                     modifier = Modifier.size(16.dp)
                 )
                 Spacer(modifier = Modifier.width(4.dp))
                 Text(
                     text = "Coordenadas: ${uiState.coordenadasLatitud?.toString()?.take(8)}, ${uiState.coordenadasLongitud?.toString()?.take(8)}",
                     style = MaterialTheme.typography.bodyMedium
                 )
             }
             
             // Mapa estático
             // Este es un marcador de posición para un mapa estático
             // En una aplicación real, se usaría una imagen cargada desde una URL
             Card(
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(150.dp)
                     .padding(vertical = 8.dp),
                 colors = CardDefaults.cardColors(
                     containerColor = MaterialTheme.colorScheme.surfaceVariant
                 )
             ) {
                 Box(
                     modifier = Modifier.fillMaxSize(),
                     contentAlignment = Alignment.Center
                 ) {
                     Text("Mapa de la ubicación (${uiState.form.direccion.ciudad}, ${uiState.form.direccion.provincia})")
                 }
             }
         }
     }
     
     // Ciudad
     FormField(
         value = uiState.form.direccion.ciudad,
         onValueChange = { viewModel.updateFormField("ciudad", it) },
         label = "Ciudad",
         icon = Icons.Filled.LocationCity,
         keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
         keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
         error = null,
         placeholder = null,
         supportingText = null
     )
     // Provincia
     FormField(
         value = uiState.form.direccion.provincia,
         onValueChange = { viewModel.updateFormField("provincia", it) },
         label = "Provincia",
         icon = Icons.Filled.Map,
         keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
         keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
         error = null,
         placeholder = null,
         supportingText = null
     )
}

/**
 * Componente para el contenido del Paso 3 del formulario (Alumnos y Centro).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3Content(uiState: RegistroUiState, viewModel: RegistroViewModel, focusManager: androidx.compose.ui.focus.FocusManager) {
    var centroDropdownExpanded by remember { mutableStateOf(false) }

    Text("Datos Escolares", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    // Selección de Centro Educativo
    ExposedDropdownMenuBox(
        expanded = centroDropdownExpanded,
        onExpandedChange = { centroDropdownExpanded = !centroDropdownExpanded },
         modifier = Modifier.fillMaxWidth()
    ) {
                    OutlinedTextField(
            value = uiState.centros.find { it.id == uiState.form.centroId }?.nombre ?: "",
            onValueChange = { /* No editable directamente */ },
            readOnly = true,
            label = { Text("Centro Educativo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = centroDropdownExpanded) },
            modifier = Modifier
                .menuAnchor() // Importante para vincular el menú
                .fillMaxWidth(),
             leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
             isError = uiState.form.centroId.isBlank() && uiState.currentStep == 3 // Mostrar error si está vacío en este paso
        )
        ExposedDropdownMenu(
            expanded = centroDropdownExpanded,
            onDismissRequest = { centroDropdownExpanded = false }
        ) {
            if (uiState.isLoadingCentros) {
                 DropdownMenuItem(
                     text = { Text("Cargando centros...") },
                     onClick = { },
                     enabled = false
                 )
             } else if (uiState.centros.isEmpty()) {
                 DropdownMenuItem(
                     text = { Text("No hay centros disponibles") },
                     onClick = { },
                     enabled = false
                 )
            } else {
                 uiState.centros.forEach { centro ->
                     DropdownMenuItem(
                         text = { Text(centro.nombre) },
                         onClick = {
                             viewModel.updateFormField("centroId", centro.id)
                             centroDropdownExpanded = false
                         }
                     )
                 }
             }
        }
    }
     if (uiState.form.centroId.isBlank() && uiState.currentStep == 3) {
         Text("Debes seleccionar un centro", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
     }


    Spacer(modifier = Modifier.height(16.dp))

    // Sección para DNIs de alumnos (movida aquí desde el Card principal)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                text = "DNIs de Alumnos Vinculados", // Título más descriptivo
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Introduce el DNI de cada alumno que deseas vincular. El centro verificará esta información.",
                 style = MaterialTheme.typography.bodySmall,
                 modifier = Modifier.padding(bottom = 12.dp)
             )

            if (uiState.form.alumnosDni.isEmpty()) {
                Text(
                    "Aún no has añadido ningún DNI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                                uiState.form.alumnosDni.forEachIndexed { index, dni ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = dni,
                             // Usar onValueChange directamente para actualizar alumno específico
                                            onValueChange = { viewModel.updateAlumnoDni(index, it) },
                            label = { Text("DNI Alumno ${index + 1}") },
                                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = if (index == uiState.form.alumnosDni.lastIndex) ImeAction.Done else ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = { focusManager.clearFocus() }
                            ),
                             singleLine = true,
                                            isError = dni.isNotBlank() && !validateDni(dni),
                                            supportingText = {
                                                if (dni.isNotBlank() && !validateDni(dni)) {
                                                    Text(
                                        "Formato DNI inválido", // Mensaje más corto
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                    Icons.Default.Badge, // O usar otro icono como ChildCare
                                                    contentDescription = "DNI del alumno"
                                                )
                                            }
                                        )

                                        IconButton(
                                            onClick = { viewModel.removeAlumnoDni(index) },
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Icon(
                                 imageVector = Icons.Filled.Delete,
                                 contentDescription = "Eliminar DNI",
                                 tint = MaterialTheme.colorScheme.error
                             )
                         }
                    }
                 }
            }

             // Mostrar error si no se ha añadido ningún alumno y es obligatorio
             val showErrorAlMenosUno = !uiState.form.alumnosDni.any { it.isNotBlank() } && uiState.currentStep == 3
             if (showErrorAlMenosUno) {
                 Text(
                     "Debes añadir el DNI de al menos un alumno.",
                     color = MaterialTheme.colorScheme.error,
                     style = MaterialTheme.typography.bodySmall,
                     modifier = Modifier.padding(top = 4.dp)
                 )
                                }

                                Button(
                                    onClick = { viewModel.addAlumnoDni() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                    .padding(top = 16.dp) // Más espacio antes del botón
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Añadir DNI"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir DNI de Alumno")
            }
        }
    }
}

/**
 * Diálogo que muestra los campos faltantes o inválidos para el paso actual.
 * Utiliza los errores específicos del UiState para mensajes más precisos.
 */
@Composable
private fun CamposFaltantesDialog(
    uiState: RegistroUiState,
    currentStep: Int,
    onDismiss: () -> Unit
) {
    // Recalcular la lista de errores si cambian los inputs relevantes
    val camposConError = remember(currentStep, uiState.form, uiState.emailError, uiState.dniError,
                                uiState.nombreError, uiState.apellidosError, uiState.telefonoError,
                                uiState.passwordError, uiState.confirmPasswordError,
                                uiState.centroIdError, uiState.alumnosDniError) {
        mutableListOf<String>().apply {
            when (currentStep) {
                1 -> {
                    // Paso 1: Datos Personales
                    if (uiState.form.email.isBlank()) add("Email: Campo obligatorio")
                    else uiState.emailError?.let { add("Email: $it") }

                    if (uiState.form.dni.isBlank()) add("DNI/NIE: Campo obligatorio")
                    else uiState.dniError?.let { add("DNI/NIE: $it") }

                    if (uiState.form.nombre.isBlank()) add("Nombre: Campo obligatorio")
                    else uiState.nombreError?.let { add("Nombre: $it") }

                    if (uiState.form.apellidos.isBlank()) add("Apellidos: Campo obligatorio")
                    else uiState.apellidosError?.let { add("Apellidos: $it") }

                    if (uiState.form.telefono.isBlank()) add("Teléfono: Campo obligatorio")
                    else uiState.telefonoError?.let { add("Teléfono: $it") }

                    if (uiState.form.password.isBlank()) add("Contraseña: Campo obligatorio")
                    // Validar requisitos directamente aquí si no hay error específico en UiState
                    else if (!validatePassword(uiState.form.password)) add("Contraseña: No cumple los requisitos mínimos")
                    else uiState.passwordError?.let { add("Contraseña: $it") }

                    if (uiState.form.confirmPassword.isBlank()) add("Confirmar Contraseña: Campo obligatorio")
                    else if (uiState.form.password != uiState.form.confirmPassword) add("Confirmar Contraseña: Las contraseñas no coinciden")
                    else uiState.confirmPasswordError?.let { add("Confirmar Contraseña: $it") }
                }
                2 -> {
                    // Paso 2: Dirección
                    if (uiState.form.direccion.calle.isBlank()) add("Calle: Campo obligatorio")
                    // Añadir errores específicos si existen (calleError)

                    if (uiState.form.direccion.numero.isBlank()) add("Número: Campo obligatorio")

                    if (uiState.form.direccion.codigoPostal.isBlank()) add("Código Postal: Campo obligatorio")
                    // Añadir validación de formato si existe (codigoPostalError)

                    if (uiState.form.direccion.ciudad.isBlank()) add("Ciudad: Campo obligatorio")

                    if (uiState.form.direccion.provincia.isBlank()) add("Provincia: Campo obligatorio")
                }
                3 -> {
                    // Paso 3: Centro y Alumnos
                    if (uiState.form.centroId.isBlank()) add("Centro Educativo: Debes seleccionar uno")
                    else uiState.centroIdError?.let { add("Centro Educativo: $it") }

                    // Validar si se requiere al menos un alumno
                    if (uiState.form.alumnosDni.all { it.isBlank() }) {
                         add("Alumnos: Debes añadir el DNI de al menos un alumno")
                            } else {
                         // Validar formato de cada DNI introducido
                         uiState.form.alumnosDni.forEachIndexed { index, dni ->
                             if (dni.isNotBlank() && !validateDni(dni)) {
                                 add("DNI Alumno ${index + 1}: Formato inválido")
                            }
                        }
                    }
                     uiState.alumnosDniError?.let { add("Alumnos: $it") } // Error general de la lista
                }
            }
        }
    }
    
        AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Revisa estos campos") },
            text = {
            LazyColumn {
                item {
                    Text(
                        "Por favor, corrige los siguientes puntos del Paso $currentStep para continuar:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (camposConError.isNotEmpty()) {
                    items(camposConError.toList()) { errorMsg: String ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            "Parece que todo está correcto en este paso, pero la validación general falló. Contacta con soporte.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        }
                    }
                }
            },
            confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}

/**
 * Componente de tarjeta de requisitos de contraseña.
 * 
 * Muestra los requisitos de la contraseña y su estado de cumplimiento.
 * 
 * @param password Contraseña a validar
 */
@Composable
private fun PasswordRequirementsCard(password: String) {
    Card(
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

/**
 * Componente de ítem de requisito de contraseña.
 * 
 * @param text Texto del requisito
 * @param isMet Estado de cumplimiento
 * @param icon Icono del requisito
 */
@Composable
private fun PasswordRequirementItem(
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

/**
 * Componente de tarjeta de términos y condiciones.
 * 
 * @param onNavigateToTerminosCondiciones Callback de navegación
 */
@Composable
private fun TermsAndConditionsCard(
    onNavigateToTerminosCondiciones: () -> Unit
) {
    Card(
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
                TextButton(onClick = onNavigateToTerminosCondiciones) {
                    Text("Ver términos completos")
                }
            }
        }
    }
}

/**
 * Componente de tarjeta de opción de relación familiar.
 * 
 * @param selected Estado de selección
 * @param onClick Callback de clic
 * @param title Título de la opción
 */
@Composable
private fun RelacionFamiliarOptionCard(
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
 * Componente de indicador de paso actual.
 * 
 * @param currentStep Paso actual
 * @param totalSteps Total de pasos
 * @param modifier Modificador de composición
 */
@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val isCurrentStep = step + 1 == currentStep
            val isCompleted = step + 1 < currentStep
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrentStep -> MaterialTheme.colorScheme.primary
                            isCompleted -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Paso completado",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = "${step + 1}",
                        color = if (isCurrentStep) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (step < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (step + 1 < currentStep) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

/**
 * Componente reutilizable para campos del formulario.
 * 
 * @param value Valor actual
 * @param onValueChange Callback de cambio
 * @param label Etiqueta del campo
 * @param placeholder Texto de placeholder
 * @param icon Icono del campo
 * @param error Mensaje de error
 * @param keyboardOptions Opciones de teclado
 * @param keyboardActions Acciones de teclado
 * @param supportingText Texto de soporte
 * @param modifier Modificador de composición
 * @param singleLine Si el campo es de una sola línea
 * @param visualTransformation Transformación visual del texto
 */
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String?,
    icon: ImageVector,
    error: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    supportingText: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        },
        isError = error != null,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        supportingText = supportingText,
        visualTransformation = visualTransformation
    )
}

/**
 * Componente de indicador de progreso del formulario.
 * 
 * @param progress Progreso actual (0.0f - 1.0f)
 * @param currentStep Paso actual
 * @param totalSteps Total de pasos
 * @param modifier Modificador de composición
 */
@Composable
private fun FormProgressIndicator(
    progress: Float,
    currentStep: Int = 1,  // Valor por defecto para currentStep
    totalSteps: Int = 3,   // Valor por defecto para totalSteps
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Vista previa de la pantalla de registro en modo claro.
 * 
 * Esta vista previa muestra cómo se ve la pantalla de registro en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    UmeEguneroTheme {
        PreviewRegistroScreen()
    }
}

/**
 * Vista previa de la pantalla de registro en modo oscuro.
 * 
 * Esta vista previa muestra cómo se ve la pantalla de registro en modo oscuro.
 */
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegistroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        PreviewRegistroScreen()
    }
}

/**
 * Componente de vista previa para la pantalla de registro.
 * 
 * Este componente proporciona una vista previa de la pantalla de registro con datos de ejemplo.
 */
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
    ) { _ ->
        RegistroScreen(
            onNavigateBack = { },
            onRegistroCompletado = { },
            onNavigateToTerminosCondiciones = { }
        )
    }
}