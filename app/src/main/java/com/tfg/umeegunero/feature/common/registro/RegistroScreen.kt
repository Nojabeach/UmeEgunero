package com.tfg.umeegunero.feature.common.registro

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.CentroEducativo
import com.tfg.umeegunero.feature.common.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch

// Definición del estado y eventos para el registro (temporales hasta implementar viewmodel)
data class RegistroUiState(
    val isLoading: Boolean = false, 
    val error: String? = null,
    val success: Boolean = false,
    val currentStep: Int = 1,
    val totalSteps: Int = 3,
    
    // Datos personales (paso 1)
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    
    // Dirección (paso 2)
    val calle: String = "",
    val numero: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = "",
    
    // Centro y alumnos (paso 3)
    val centroEducativo: CentroEducativo? = null,
    val centroNombre: String = "",
    val alumnos: List<String> = emptyList(),
    
    // Errores de validación
    val nombreError: String? = null,
    val apellidosError: String? = null,
    val emailError: String? = null,
    val telefonoError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val calleError: String? = null,
    val numeroError: String? = null,
    val codigoPostalError: String? = null,
    val ciudadError: String? = null,
    val provinciaError: String? = null,
    val centroError: String? = null
)

sealed class RegistroEvent {
    object NextStep : RegistroEvent()
    object PreviousStep : RegistroEvent()
    object Submit : RegistroEvent()
    object ClearError : RegistroEvent()
}

// Extensión para verificar si el tema es claro
fun ColorScheme.isLight(): Boolean {
    // En Material 3, podemos usar esta aproximación para detectar si estamos en tema claro
    val backgroundColor = this.background
    // Calculamos un valor aproximado de luminosidad (0.0 - 1.0)
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return luminance > 0.5
}

/**
 * Calcula el porcentaje de completado del formulario de registro
 * @param uiState El estado actual del formulario
 * @return Un valor entre 0.0 y 1.0 que representa el progreso
 */
private fun calcularPorcentajeCompletado(uiState: RegistroUiState): Float {
    var camposCompletados = 0
    var camposTotales = 0
    
    // Para paso 1: Datos personales
    if (uiState.currentStep >= 1) {
        camposTotales += 6 // Nombre, apellidos, email, teléfono, contraseña, confirmar contraseña
        if (uiState.nombre.isNotBlank()) camposCompletados++
        if (uiState.apellidos.isNotBlank()) camposCompletados++
        if (uiState.email.isNotBlank()) camposCompletados++
        if (uiState.telefono.isNotBlank()) camposCompletados++
        if (uiState.password.isNotBlank()) camposCompletados++
        if (uiState.confirmPassword.isNotBlank()) camposCompletados++
    }
    
    // Para paso 2: Dirección
    if (uiState.currentStep >= 2) {
        camposTotales += 5 // Calle, número, código postal, ciudad, provincia
        if (uiState.calle.isNotBlank()) camposCompletados++
        if (uiState.numero.isNotBlank()) camposCompletados++
        if (uiState.codigoPostal.isNotBlank()) camposCompletados++
        if (uiState.ciudad.isNotBlank()) camposCompletados++
        if (uiState.provincia.isNotBlank()) camposCompletados++
    }
    
    // Para paso 3: Centro y alumnos
    if (uiState.currentStep >= 3) {
        camposTotales += 2 // Centro educativo y al menos un alumno
        if (uiState.centroEducativo != null || uiState.centroNombre.isNotBlank()) camposCompletados++
        if (uiState.alumnos.isNotEmpty()) camposCompletados++
    }
    
    // Si no hay campos a completar, retornar 0
    if (camposTotales == 0) return 0f
    
    // Calcular porcentaje considerando solo los pasos visibles hasta ahora
    val porcentajeBase = camposCompletados.toFloat() / camposTotales.toFloat()
    
    // Ajustar según errores (si hay errores de validación, reducir el porcentaje)
    val errores = contarErrores(uiState)
    if (errores > 0) {
        return porcentajeBase * (1.0f - (errores.toFloat() / camposTotales.toFloat()))
    }
    
    return porcentajeBase
}

/**
 * Cuenta el número de errores de validación presentes en el estado
 */
private fun contarErrores(uiState: RegistroUiState): Int {
    var errores = 0
    
    if (uiState.currentStep >= 1) {
        if (uiState.nombreError != null) errores++
        if (uiState.apellidosError != null) errores++
        if (uiState.emailError != null) errores++
        if (uiState.telefonoError != null) errores++
        if (uiState.passwordError != null) errores++
        if (uiState.confirmPasswordError != null) errores++
    }
    
    if (uiState.currentStep >= 2) {
        if (uiState.calleError != null) errores++
        if (uiState.numeroError != null) errores++
        if (uiState.codigoPostalError != null) errores++
        if (uiState.ciudadError != null) errores++
        if (uiState.provinciaError != null) errores++
    }
    
    if (uiState.currentStep >= 3) {
        if (uiState.centroError != null) errores++
    }
    
    return errores
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    uiState: RegistroUiState,
    onNavigateBack: () -> Unit,
    onRegistroSuccess: () -> Unit,
    onEvent: (RegistroEvent) -> Unit
) {
    // TODO: Mejoras pendientes para la pantalla de registro
    // - Implementar validación en tiempo real de todos los campos
    // - Añadir verificación de email mediante código o enlace
    // - Mostrar requisitos de seguridad de contraseña visualmente
    // - Implementar registro con credenciales de redes sociales
    // - Añadir detección automática de ubicación para centros
    // - Mejorar feedback visual durante el proceso de registro
    // - Incluir opción para escanear código QR de invitación
    // - Implementar formularios específicos según tipo de usuario
    // - Añadir términos y condiciones con aceptación explícita
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Detector de éxito en el registro
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onRegistroSuccess()
        }
    }

    // Mostrar errores en Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                onEvent(RegistroEvent.ClearError)
            }
        }
    }

    // Determinar si estamos en modo claro u oscuro
    val isLight = MaterialTheme.colorScheme.isLight()

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
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de progreso mejorado con porcentaje
                FormProgressIndicator(
                    porcentaje = calcularPorcentajeCompletado(uiState)
                )
                
                // Paso actual
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        HorizontalDivider()
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Aquí iría el contenido específico de cada paso
                        Text(
                            text = "Formulario de registro - Paso ${uiState.currentStep}",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de navegación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(
                            onClick = { onEvent(RegistroEvent.PreviousStep) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Anterior")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (uiState.currentStep < uiState.totalSteps) {
                        Button(
                            onClick = { onEvent(RegistroEvent.NextStep) },
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
                            onClick = { onEvent(RegistroEvent.Submit) },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    UmeEguneroTheme {
        RegistroScreen(
            uiState = RegistroUiState(
                nombre = "María",
                apellidos = "García López",
                email = "maria@example.com",
                telefono = "678123456"
            ),
            onNavigateBack = {},
            onRegistroSuccess = {},
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegistroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        RegistroScreen(
            uiState = RegistroUiState(),
            onNavigateBack = {},
            onRegistroSuccess = {},
            onEvent = {}
        )
    }
} 