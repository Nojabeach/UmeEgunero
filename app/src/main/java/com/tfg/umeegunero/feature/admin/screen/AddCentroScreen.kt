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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.tfg.umeegunero.feature.common.components.FormProgressIndicator
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextFieldDefaults

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
        onGuardarClick = { /* Implementar guardado */ },
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
    val scope = rememberCoroutineScope()
    
    // Animación para el progreso del formulario
    val formProgress by animateFloatAsState(
        targetValue = calculateFormProgress(uiState),
        animationSpec = tween(durationMillis = 500),
        label = "formProgressAnimation"
    )
    
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
                title = { Text(text = "Añadir Centro") },
                navigationIcon = {
                    IconButton(onClick = onCancelarClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Indicador de progreso mejorado
            FormProgressIndicator(
                porcentaje = calcularPorcentajeCompletado(uiState)
            )
            
            // Formulario para datos de centro
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        // Icono circular
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Información del Centro",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Sección de información del centro
                    SectionCard(
                        title = "Información del Centro",
                        icon = Icons.Default.School
                    ) {
                        EnhancedFormTextField(
                            value = uiState.nombre,
                            onValueChange = onNombreChange,
                            label = "Nombre del Centro",
                            error = uiState.nombreError,
                            icon = Icons.Default.School,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sección de dirección
                    SectionCard(
                        title = "Dirección",
                        icon = Icons.Default.LocationCity,
                        modifier = Modifier.heightIn(min = 280.dp)
                    ) {
                        // Calle y número en la misma línea con proporción adecuada
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            EnhancedFormTextField(
                                value = uiState.calle,
                                onValueChange = onCalleChange,
                                label = "Calle",
                                error = uiState.calleError,
                                icon = Icons.Default.Home,
                                modifier = Modifier
                                    .weight(0.5f)
                                    .heightIn(min = 60.dp)
                            )
                            
                            EnhancedFormTextField(
                                value = uiState.numero,
                                onValueChange = onNumeroChange,
                                label = "Número",
                                error = uiState.numeroError,
                                icon = null,
                                modifier = Modifier
                                    .weight(0.5f)
                                    .heightIn(min = 60.dp),
                                keyboardType = KeyboardType.Number
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Código postal en línea separada para darle más importancia
                        EnhancedFormTextField(
                            value = uiState.codigoPostal,
                            onValueChange = onCodigoPostalChange,
                            label = "Código Postal",
                            error = uiState.codigoPostalError,
                            icon = Icons.Default.Place,
                            modifier = Modifier.fillMaxWidth(0.5f),
                            keyboardType = KeyboardType.Number,
                            maxLength = 5
                        )
                        
                        // Mostrar mensaje de error o carga para la búsqueda de ciudades
                        if (uiState.isBuscandoCiudades) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        } else if (uiState.errorBusquedaCiudades != null) {
                            Text(
                                text = uiState.errorBusquedaCiudades,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Dropdown para seleccionar ciudad si hay sugerencias
                        if (uiState.ciudadesSugeridas.isNotEmpty()) {
                            var expanded by remember { mutableStateOf(false) }
                            
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = uiState.ciudad,
                                    onValueChange = onCiudadChange,
                                    label = { Text("Ciudad") },
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
                                    isError = uiState.ciudadError != null,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    uiState.ciudadesSugeridas.forEach { ciudad ->
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
                        } else {
                            // Campo de texto normal para la ciudad si no hay sugerencias
                            EnhancedFormTextField(
                                value = uiState.ciudad,
                                onValueChange = onCiudadChange,
                                label = "Ciudad",
                                error = uiState.ciudadError,
                                icon = Icons.Default.LocationCity,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Dropdown para seleccionar provincia
                        var expandedProvincia by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedProvincia,
                            onExpandedChange = { expandedProvincia = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.provincia,
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
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvincia)
                                },
                                isError = uiState.provinciaError != null,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expandedProvincia,
                                onDismissRequest = { expandedProvincia = false }
                            ) {
                                provincias.forEach { provincia ->
                                    DropdownMenuItem(
                                        text = { Text(provincia) },
                                        onClick = {
                                            onProvinciaChange(provincia)
                                            expandedProvincia = false
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
                        
                        // Mostrar mapa con la ubicación si tenemos coordenadas
                        if (uiState.tieneUbicacionValida) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                    checked = uiState.mostrarMapa,
                                    onCheckedChange = { onToggleMapa() }
                                )
                            }
                            
                            AnimatedVisibility(
                                visible = uiState.mostrarMapa,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Componente de mapa mejorado
                                MapaUbicacion(
                                    latitud = uiState.latitud!!,
                                    longitud = uiState.longitud!!,
                                    direccion = uiState.direccionCompleta,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sección de contacto
                    SectionCard(
                        title = "Contacto",
                        icon = Icons.Default.Phone
                    ) {
                        EnhancedFormTextField(
                            value = uiState.telefono,
                            onValueChange = onTelefonoChange,
                            label = "Teléfono",
                            error = uiState.telefonoError,
                            icon = Icons.Default.Phone,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Phone
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EnhancedFormTextField(
                            value = uiState.email,
                            onValueChange = onEmailChange,
                            label = "Email",
                            error = uiState.emailError,
                            icon = Icons.Default.Email,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Email
                        )
                    }
                    
                    // Sección de contraseñas (solo para nuevos centros)
                    if (uiState.id.isBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionCard(
                            title = "Cuenta de Acceso",
                            icon = Icons.Default.Lock
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
                                modifier = Modifier.fillMaxWidth(),
                                keyboardType = KeyboardType.Password,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
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
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelarClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Cancelar")
                }
                
                Button(
                    onClick = onGuardarClick,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Guardar")
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
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Icono circular
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { 
                if (it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            label = { Text(label) },
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
            modifier = modifier.heightIn(min = 56.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )
        
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
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
    // Estado para el preview
    val previewState = AddCentroUiState(
        nombre = "IES Txurdinaga Behekoa BHI",
        calle = "Gabriel Aresti Bidea",
        numero = "8",
        codigoPostal = "48004",
        ciudad = "Bilbao",
        provincia = "Vizcaya / Bizkaia",
        telefono = "944125712",
        email = "info@txurdinagabehekoa.eus",
        password = "euskadi2025",
        confirmPassword = "euskadi2025"
    )

    UmeEguneroTheme {
        Surface {
            AddCentroScreenContent(
                uiState = previewState,
                provincias = listOf("Vizcaya / Bizkaia"),
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
    // Estado para el preview
    val previewState = AddCentroUiState(
        nombre = "IES Artaza-Romo BHI",
        calle = "Amaia Kalea",
        numero = "28",
        codigoPostal = "48930",
        ciudad = "Getxo",
        provincia = "Vizcaya / Bizkaia",
        telefono = "944633000",
        email = "secretaria@artazaromo.eus",
        password = "bizkaia2025",
        confirmPassword = "bizkaia2025"
    )

    UmeEguneroTheme(darkTheme = true) {
        Surface {
            AddCentroScreenContent(
                uiState = previewState,
                provincias = listOf("Vizcaya / Bizkaia"),
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
