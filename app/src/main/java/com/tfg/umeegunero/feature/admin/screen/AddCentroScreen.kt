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
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroUiState
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
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
    
    AddCentroScreenContent(
        uiState = uiState,
        provincias = provinciasLista,
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

@Composable
fun AddCentroScreenContent(
    uiState: AddCentroUiState,
    provincias: List<String>,
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Añadir Centro") },
                navigationIcon = {
                    IconButton(onClick = onCancelarClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Sección de información del centro
            Text(
                text = "Información del Centro",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            FormTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = "Nombre del Centro",
                error = uiState.nombreError,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección de dirección
            Text(
                text = "Dirección",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormTextField(
                    value = uiState.calle,
                    onValueChange = onCalleChange,
                    label = "Calle",
                    error = uiState.calleError,
                    modifier = Modifier.weight(0.7f)
                )
                
                FormTextField(
                    value = uiState.numero,
                    onValueChange = onNumeroChange,
                    label = "Número",
                    error = uiState.numeroError,
                    modifier = Modifier.weight(0.3f),
                    keyboardType = KeyboardType.Number
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FormTextField(
                value = uiState.codigoPostal,
                onValueChange = onCodigoPostalChange,
                label = "Código Postal",
                error = uiState.codigoPostalError,
                modifier = Modifier.fillMaxWidth(),
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
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        isError = uiState.ciudadError != null,
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
                                }
                            )
                        }
                    }
                }
            } else {
                // Campo de texto normal para la ciudad si no hay sugerencias
                FormTextField(
                    value = uiState.ciudad,
                    onValueChange = onCiudadChange,
                    label = "Ciudad",
                    error = uiState.ciudadError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvincia)
                    },
                    isError = uiState.provinciaError != null,
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
                    Text(
                        text = "Ubicación en mapa",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Switch(
                        checked = uiState.mostrarMapa,
                        onCheckedChange = { onToggleMapa() }
                    )
                }
                
                if (uiState.mostrarMapa) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Componente de mapa
                    MapaUbicacion(
                        latitud = uiState.latitud!!,
                        longitud = uiState.longitud!!,
                        direccion = uiState.direccionCompleta,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección de contacto
            Text(
                text = "Contacto",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            FormTextField(
                value = uiState.telefono,
                onValueChange = onTelefonoChange,
                label = "Teléfono",
                error = uiState.telefonoError,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Phone
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FormTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                error = uiState.emailError,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Email
            )
            
            // Sección de contraseñas (solo para nuevos centros)
            if (uiState.id.isBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Cuenta de Acceso",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                FormTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = "Contraseña",
                    error = uiState.passwordError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FormTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = "Confirmar Contraseña",
                    error = uiState.confirmPasswordError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelarClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Cancelar")
                }
                
                Button(
                    onClick = onGuardarClick,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isFormValid && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = "Guardar")
                    }
                }
            }
            
            // Mostrar mensaje de error general si existe
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
    Box(
        modifier = modifier
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Mapa de ubicación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Coordenadas: $latitud, $longitud",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = direccion,
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Enlace para abrir en Google Maps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Abrir en Google Maps",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Abrir en Google Maps",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
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

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = Int.MAX_VALUE,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = { 
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        modifier = modifier,
        singleLine = true
    )
    
    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}
