package com.tfg.umeegunero.feature.admin.screen

import android.content.Intent
import android.net.Uri
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroUiState
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
import com.tfg.umeegunero.feature.common.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MenuAnchorType

// Datos para provincias con soporte multilingüe
data class Provincia(
    val codigo: String,
    val nombreCastellano: String,
    val nombreLocal: String? = null,
    val comunidadAutonoma: String
) {
    // Devuelve el nombre en el idioma local si existe, o el nombre en castellano si no
    fun getNombreCompleto(): String {
        return if (nombreLocal != null) {
            "$nombreCastellano / $nombreLocal"
        } else {
            nombreCastellano
        }
    }
}

// Datos para municipios con sus códigos postales
data class Municipio(
    val nombre: String,
    val nombreLocal: String? = null,
    val provinciaCodigo: String,
    val codigosPostales: List<String>
) {
    // Devuelve el nombre en el idioma local si existe, o el nombre en castellano si no
    fun getNombreCompleto(): String {
        return if (nombreLocal != null) {
            "$nombre / $nombreLocal"
        } else {
            nombre
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreen(
    viewModel: AddCentroViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val provinciasLista by viewModel.provincias.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Efecto para manejar la navegación de vuelta tras guardar exitosamente
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Centro guardado correctamente")
            // Volver atrás después de un breve retraso
            scope.launch {
                kotlinx.coroutines.delay(500)
                onNavigateBack()
            }
        }
    }

    AddCentroScreenContent(
        uiState = uiState,
        provincias = provinciasLista,
        snackbarHostState = snackbarHostState,
        onNombreChange = viewModel::updateNombre,
        onCalleChange = viewModel::updateCalle,
        onNumeroChange = viewModel::updateNumero,
        onCodigoPostalChange = viewModel::updateCodigoPostal,
        onCiudadChange = viewModel::updateCiudad,
        onProvinciaChange = viewModel::updateProvincia,
        onTelefonoChange = viewModel::updateTelefono,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onConfirmPasswordChange = viewModel::updateConfirmPassword,
        onCiudadSelected = viewModel::seleccionarCiudad,
        onToggleMapa = viewModel::toggleMapa,
        onGuardarClick = viewModel::saveCentro,
        onCancelarClick = onNavigateBack,
        onErrorDismiss = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreenContent(
    uiState: AddCentroUiState,
    provincias: List<String>,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNombreChange: (String) -> Unit,
    onCalleChange: (String) -> Unit,
    onNumeroChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCiudadChange: (String) -> Unit,
    onProvinciaChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCiudadSelected: (Ciudad) -> Unit,
    onToggleMapa: () -> Unit,
    onGuardarClick: () -> Unit,
    onCancelarClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Efecto para mostrar errores en el Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            onErrorDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Añadir Centro Educativo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onCancelarClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Indicador de progreso mejorado con estilo educativo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FormProgressIndicator(
                        currentStep = (calcularPorcentajeCompletado(uiState) * 10).toInt(),
                        totalSteps = 10,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Completa el formulario para dar de alta el centro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Formulario para datos de centro
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        // Icono circular con animación sutil
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 4.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Información del Centro",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Datos básicos del centro educativo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Campo directo para el nombre del centro con diseño mejorado
                    EnhancedFormTextField(
                        value = uiState.nombre,
                        onValueChange = onNombreChange,
                        label = "Nombre del Centro Educativo",
                        error = uiState.nombreError,
                        icon = Icons.Default.School,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        placeholder = "Ej: IES Gabriel Aresti"
                    )
                    
                    // Sección de dirección
                    DireccionSection(
                        uiState = uiState,
                        onCalleChange = onCalleChange,
                        onNumeroChange = onNumeroChange,
                        onCodigoPostalChange = onCodigoPostalChange,
                        onCiudadChange = onCiudadChange,
                        onCiudadSelected = onCiudadSelected,
                        onProvinciaChange = onProvinciaChange,
                        onToggleMapa = onToggleMapa,
                        provincias = provincias
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección de contacto con diseño mejorado
                    SectionCard(
                        title = "Datos de Contacto",
                        subtitle = "Información para comunicaciones oficiales",
                        icon = Icons.Default.Phone,
                        accentColor = MaterialTheme.colorScheme.tertiary
                    ) {
                        EnhancedFormTextField(
                            value = uiState.telefono,
                            onValueChange = onTelefonoChange,
                            label = "Teléfono de contacto",
                            error = uiState.telefonoError,
                            icon = Icons.Default.Phone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardType = KeyboardType.Phone,
                            placeholder = "Ej: 944123456"
                        )

                        EnhancedFormTextField(
                            value = uiState.email,
                            onValueChange = onEmailChange,
                            label = "Email del centro",
                            error = uiState.emailError,
                            icon = Icons.Default.Email,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardType = KeyboardType.Email,
                            placeholder = "Ej: secretaria@centro.edu.es"
                        )
                    }

                    // Sección de contraseñas (solo para nuevos centros)
                    if (uiState.id.isBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))

                        SectionCard(
                            title = "Cuenta de Acceso",
                            subtitle = "Credenciales para el portal del centro",
                            icon = Icons.Default.Lock,
                            accentColor = MaterialTheme.colorScheme.secondary
                        ) {
                            var passwordVisible by remember { mutableStateOf(false) }
                            var confirmPasswordVisible by remember { mutableStateOf(false) }

                            EnhancedFormTextField(
                                value = uiState.password,
                                onValueChange = onPasswordChange,
                                label = "Contraseña",
                                error = uiState.passwordError,
                                icon = Icons.Default.Lock,
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                            )

                            EnhancedFormTextField(
                                value = uiState.confirmPassword,
                                onValueChange = onConfirmPasswordChange,
                                label = "Confirmar Contraseña",
                                error = uiState.confirmPasswordError,
                                icon = Icons.Default.Lock,
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                            )
                            
                            // Texto informativo sobre las contraseñas
                            Text(
                                text = "La contraseña debe contener al menos 8 caracteres, incluyendo mayúsculas, minúsculas y números",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Botones de acción mejorados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelarClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Button(
                    onClick = onGuardarClick,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Guardar Centro",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                // Icono circular con efecto de elevación y color temático
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = accentColor,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    icon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    placeholder: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { 
                if (it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            label = { 
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                ) 
            },
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null,
            trailingIcon = trailingIcon,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = placeholder?.let { { 
                Text(
                    text = it, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge
                ) 
            } }
        )

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DireccionSection(
    uiState: AddCentroUiState,
    onCalleChange: (String) -> Unit,
    onNumeroChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCiudadChange: (String) -> Unit,
    onCiudadSelected: (Ciudad) -> Unit,
    onProvinciaChange: (String) -> Unit,
    onToggleMapa: () -> Unit,
    provincias: List<String>
) {
    SectionCard(
        title = "Ubicación del Centro", 
        subtitle = "Dirección física del centro educativo",
        icon = Icons.Default.LocationCity,
        accentColor = MaterialTheme.colorScheme.primary
    ) {
        // Campo para la vía (en su propia fila)
        EnhancedFormTextField(
            value = uiState.calle,
            onValueChange = onCalleChange,
            label = "Vía / Calle",
            error = uiState.calleError,
            icon = Icons.Default.Home,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = "Ej: Calle Mayor"
        )

        // Campo para el número (en su propia fila)
        EnhancedFormTextField(
            value = uiState.numero,
            onValueChange = onNumeroChange,
            label = "Número",
            error = uiState.numeroError,
            icon = Icons.Default.Place,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardType = KeyboardType.Number,
            placeholder = "Ej: 25"
        )

        // Código postal (en su propia fila)
        EnhancedFormTextField(
            value = uiState.codigoPostal,
            onValueChange = onCodigoPostalChange,
            label = "Código Postal",
            error = uiState.codigoPostalError,
            icon = Icons.Default.Place,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (uiState.isBuscandoCiudades || uiState.errorBusquedaCiudades != null) 8.dp else 16.dp),
            keyboardType = KeyboardType.Number,
            maxLength = 5,
            placeholder = "Ej: 48001"
        )

        // Indicador de búsqueda o error para el código postal
        if (uiState.isBuscandoCiudades) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else if (uiState.errorBusquedaCiudades != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = uiState.errorBusquedaCiudades,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Ciudad (en su propia fila)
        if (uiState.ciudadesSugeridas.isNotEmpty()) {
            CiudadDropdown(
                ciudad = uiState.ciudad,
                onCiudadChange = onCiudadChange,
                ciudadesSugeridas = uiState.ciudadesSugeridas,
                onCiudadSelected = onCiudadSelected,
                ciudadError = uiState.ciudadError
            )
        } else {
            EnhancedFormTextField(
                value = uiState.ciudad,
                onValueChange = onCiudadChange,
                label = "Ciudad/Municipio",
                error = uiState.ciudadError,
                icon = Icons.Default.LocationCity,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = "Ej: Bilbao"
            )
        }

        // Provincia - Dropdown mejorado
        ProvinciaDropdown(
            provincia = uiState.provincia,
            onProvinciaChange = onProvinciaChange,
            provincias = provincias,
            provinciaError = uiState.provinciaError
        )

        // Mostrar mapa con la ubicación si tenemos coordenadas - Diseño más atractivo
        if (uiState.tieneUbicacionValida) {
            MapaUbicacionSection(
                mostrarMapa = uiState.mostrarMapa,
                onToggleMapa = onToggleMapa,
                latitud = uiState.latitud!!,
                longitud = uiState.longitud!!,
                direccionCompleta = uiState.direccionCompleta
            )
        }
    }
}

@Composable
fun CiudadDropdown(
    ciudad: String,
    onCiudadChange: (String) -> Unit,
    ciudadesSugeridas: List<Ciudad>,
    onCiudadSelected: (Ciudad) -> Unit,
    ciudadError: String?
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = ciudad,
            onValueChange = onCiudadChange,
            label = { Text("Ciudad/Municipio") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = ciudadError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text("Seleccione una ciudad", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            ciudadesSugeridas.forEach { ciudad ->
                DropdownMenuItem(
                    text = { Text("${ciudad.nombre} (${ciudad.provincia})") },
                    onClick = {
                        onCiudadSelected(ciudad)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ProvinciaDropdown(
    provincia: String,
    onProvinciaChange: (String) -> Unit,
    provincias: List<String>,
    provinciaError: String?
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = provincia,
            onValueChange = onProvinciaChange,
            label = { Text("Provincia") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = provinciaError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text("Seleccione una provincia", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            provincias.forEach { provincia ->
                DropdownMenuItem(
                    text = { Text(provincia) },
                    onClick = {
                        onProvinciaChange(provincia)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MapaUbicacionSection(
    mostrarMapa: Boolean,
    onToggleMapa: () -> Unit,
    latitud: Double,
    longitud: Double,
    direccionCompleta: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ubicación en mapa",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Switch(
            checked = mostrarMapa,
            onCheckedChange = { onToggleMapa() }
        )
    }

    AnimatedVisibility(
        visible = mostrarMapa,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        MapaUbicacion(
            latitud = latitud,
            longitud = longitud,
            direccion = direccionCompleta,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun MapaUbicacion(
    latitud: Double,
    longitud: Double,
    direccion: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Aquí implementaremos el mapa con Google Maps o una alternativa
    // Por ahora, mostraremos un placeholder con la información
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Mapa de ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Coordenadas: $latitud, $longitud",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = direccion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Enlace para abrir en Google Maps
            Button(
                onClick = {
                    // Abrir Google Maps con las coordenadas
                    val gmmIntentUri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud($direccion)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    // Verificar si Google Maps está instalado
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        // Si Google Maps no está instalado, abrir en el navegador
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitud,$longitud")
                        )
                        context.startActivity(browserIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Abrir en Google Maps"
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Abrir en Google Maps",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Función para calcular el progreso del formulario
private fun calculateFormProgress(uiState: AddCentroUiState): Float {
    var fieldsCompleted = 0
    var totalFields = 7 // Campos obligatorios básicos

    // Verificar campos básicos
    if (uiState.nombre.isNotBlank()) fieldsCompleted++
    if (uiState.calle.isNotBlank()) fieldsCompleted++
    if (uiState.numero.isNotBlank()) fieldsCompleted++
    if (uiState.codigoPostal.isNotBlank()) fieldsCompleted++
    if (uiState.ciudad.isNotBlank()) fieldsCompleted++
    if (uiState.provincia.isNotBlank()) fieldsCompleted++
    if (uiState.telefono.isNotBlank()) fieldsCompleted++

    // Si es un nuevo centro, añadir campos de contraseña
    if (uiState.id.isBlank()) {
        totalFields += 2
        if (uiState.password.isNotBlank()) fieldsCompleted++
        if (uiState.confirmPassword.isNotBlank()) fieldsCompleted++
    }

    return fieldsCompleted.toFloat() / totalFields.toFloat()
}

/**
 * Calcula el porcentaje de completado del formulario para añadir o editar un centro
 * @param uiState Estado actual del formulario
 * @return Valor entre 0.0 y 1.0 que representa el porcentaje de completado
 */
private fun calcularPorcentajeCompletado(uiState: AddCentroUiState): Float {
    var camposCompletados = 0
    var camposTotales = 8 // Campos principales obligatorios

    // Verificar campos principales
    if (uiState.nombre.isNotBlank()) camposCompletados++
    if (uiState.calle.isNotBlank()) camposCompletados++
    if (uiState.numero.isNotBlank()) camposCompletados++
    if (uiState.codigoPostal.isNotBlank()) camposCompletados++
    if (uiState.ciudad.isNotBlank()) camposCompletados++
    if (uiState.provincia.isNotBlank()) camposCompletados++
    if (uiState.telefono.isNotBlank()) camposCompletados++
    if (uiState.email.isNotBlank()) camposCompletados++

    // Si es un nuevo centro, verificar las contraseñas
    if (uiState.id.isBlank()) {
        camposTotales += 2
        if (uiState.password.isNotBlank()) camposCompletados++
        if (uiState.confirmPassword.isNotBlank()) camposCompletados++
    }

    // Si hay errores en algún campo, reducir el porcentaje proporcionalmente
    val errores = countErrors(uiState)

    // Calcular porcentaje considerando también la ausencia de errores
    return (camposCompletados.toFloat() / camposTotales.toFloat()) * (1.0f - (errores.toFloat() / camposTotales.toFloat()))
}

/**
 * Cuenta el número de errores en el estado del formulario
 */
private fun countErrors(uiState: AddCentroUiState): Int {
    var errores = 0

    if (uiState.nombreError != null) errores++
    if (uiState.calleError != null) errores++
    if (uiState.numeroError != null) errores++
    if (uiState.codigoPostalError != null) errores++
    if (uiState.ciudadError != null) errores++
    if (uiState.provinciaError != null) errores++
    if (uiState.telefonoError != null) errores++
    if (uiState.emailError != null) errores++

    if (uiState.id.isBlank()) {
        if (uiState.passwordError != null) errores++
        if (uiState.confirmPasswordError != null) errores++
    }

    return errores
}

@Preview(showBackground = true)
@Composable
fun AddCentroScreenPreview() {
    // Estado para el preview con campos vacíos para mostrar los placeholders
    val previewState = AddCentroUiState(
        nombre = "",
        calle = "",
        numero = "",
        codigoPostal = "",
        ciudad = "",
        provincia = "",
        telefono = "",
        email = "",
        password = "",
        confirmPassword = "",
        tieneUbicacionValida = true,
        latitud = 43.2569629,
        longitud = -2.9045053,
        direccionCompleta = "Gabriel Aresti Bidea 8, 48004 Bilbao, Vizcaya",
        mostrarMapa = true
    )

    UmeEguneroTheme {
        Surface {
            AddCentroScreenContent(
                uiState = previewState,
                provincias = listOf("Vizcaya / Bizkaia", "Guipúzcoa / Gipuzkoa", "Álava / Araba"),
                onNombreChange = {},
                onCalleChange = {},
                onNumeroChange = {},
                onCodigoPostalChange = {},
                onCiudadChange = {},
                onProvinciaChange = {},
                onTelefonoChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onCiudadSelected = {},
                onToggleMapa = {},
                onGuardarClick = {},
                onCancelarClick = {},
                onErrorDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddCentroScreenDarkPreview() {
    // Estado para el preview con campos vacíos para mostrar los placeholders
    val previewState = AddCentroUiState(
        nombre = "",
        calle = "",
        numero = "",
        codigoPostal = "",
        ciudad = "",
        provincia = "",
        telefono = "",
        email = "",
        password = "",
        confirmPassword = "",
        tieneUbicacionValida = true,
        latitud = 43.3397326,
        longitud = -3.0098376,
        direccionCompleta = "Amaia Kalea 28, 48930 Getxo, Vizcaya",
        mostrarMapa = true
    )

    UmeEguneroTheme(darkTheme = true) {
        Surface {
            AddCentroScreenContent(
                uiState = previewState,
                provincias = listOf("Vizcaya / Bizkaia", "Guipúzcoa / Gipuzkoa", "Álava / Araba"),
                onNombreChange = {},
                onCalleChange = {},
                onNumeroChange = {},
                onCodigoPostalChange = {},
                onCiudadChange = {},
                onProvinciaChange = {},
                onTelefonoChange = {},
                onEmailChange = {},
                onPasswordChange = {},
                onConfirmPasswordChange = {},
                onCiudadSelected = {},
                onToggleMapa = {},
                onGuardarClick = {},
                onCancelarClick = {},
                onErrorDismiss = {}
            )
        }
    }
}
