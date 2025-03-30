package com.tfg.umeegunero.feature.admin.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.feature.admin.viewmodel.AdminViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCentroAdded: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 3
    
    // Datos del formulario
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    
    // Validación
    var nombreError by remember { mutableStateOf("") }
    var direccionError by remember { mutableStateOf("") }
    var telefonoError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var latitudError by remember { mutableStateOf("") }
    var longitudError by remember { mutableStateOf("") }
    
    // Estado de carga
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    
    // Validación de formularios
    fun validateStep0(): Boolean {
        var isValid = true
        
        if (nombre.isEmpty()) {
            nombreError = "El nombre es obligatorio"
            isValid = false
        } else {
            nombreError = ""
        }
        
        return isValid
    }
    
    fun validateStep1(): Boolean {
        var isValid = true
        
        if (direccion.isEmpty()) {
            direccionError = "La dirección es obligatoria"
            isValid = false
        } else {
            direccionError = ""
        }
        
        if (telefono.isNotEmpty() && !telefono.matches(Regex("^[0-9]{9}$"))) {
            telefonoError = "Formato inválido (9 dígitos)"
            isValid = false
        } else {
            telefonoError = ""
        }
        
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email inválido"
            isValid = false
        } else {
            emailError = ""
        }
        
        return isValid
    }
    
    fun validateStep2(): Boolean {
        var isValid = true
        
        if (latitud.isNotEmpty()) {
            try {
                val lat = latitud.toDouble()
                if (lat < -90 || lat > 90) {
                    latitudError = "Latitud debe estar entre -90 y 90"
                    isValid = false
                } else {
                    latitudError = ""
                }
            } catch (e: NumberFormatException) {
                latitudError = "Formato numérico inválido"
                isValid = false
            }
        }
        
        if (longitud.isNotEmpty()) {
            try {
                val lng = longitud.toDouble()
                if (lng < -180 || lng > 180) {
                    longitudError = "Longitud debe estar entre -180 y 180"
                    isValid = false
                } else {
                    longitudError = ""
                }
            } catch (e: NumberFormatException) {
                longitudError = "Formato numérico inválido"
                isValid = false
            }
        }
        
        return isValid
    }
    
    fun validateForm(): Boolean {
        return validateStep0() && validateStep1() && validateStep2()
    }
    
    // Función para agregar un centro
    fun agregarCentro() {
        val isValid = validateForm()
        
        if (isValid) {
            val centro = Centro(
                id = "",
                nombre = nombre,
                direccion = direccion,
                telefono = telefono,
                email = email,
                latitud = latitud.toDoubleOrNull() ?: 0.0,
                longitud = longitud.toDoubleOrNull() ?: 0.0
            )
            
            isLoading = true
            
            viewModel.agregarCentro(centro) { success ->
                isLoading = false
                
                if (success) {
                    Toast.makeText(context, "Centro agregado correctamente", Toast.LENGTH_SHORT).show()
                    onCentroAdded()
                } else {
                    errorMessage = "Error al agregar el centro"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "Por favor, corrija los errores del formulario", Toast.LENGTH_SHORT).show()
        }
    }
    
    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Centro Educativo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Guardando centro...",
                    fullScreen = true
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Indicador de progreso
                    FormProgressIndicator(
                        currentStep = currentStep + 1,
                        totalSteps = totalSteps,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                    
                    // Título del paso actual
                    Text(
                        text = when (currentStep) {
                            0 -> "Información Básica"
                            1 -> "Dirección y Contacto"
                            2 -> "Ubicación Geográfica"
                            else -> ""
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    when (currentStep) {
                        0 -> {
                            // Paso 1: Información básica
                            OutlinedTextFieldWithError(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = "Nombre del centro",
                                errorMessage = nombreError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        1 -> {
                            // Paso 2: Dirección y contacto
                            OutlinedTextFieldWithError(
                                value = direccion,
                                onValueChange = { direccion = it },
                                label = "Dirección",
                                errorMessage = direccionError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextFieldWithError(
                                value = telefono,
                                onValueChange = { telefono = it },
                                label = "Teléfono",
                                errorMessage = telefonoError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextFieldWithError(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                errorMessage = emailError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        2 -> {
                            // Paso 3: Coordenadas
                            Text(
                                text = "Las coordenadas son opcionales, pero ayudan a ubicar el centro en el mapa.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            OutlinedTextFieldWithError(
                                value = latitud,
                                onValueChange = { latitud = it },
                                label = "Latitud",
                                errorMessage = latitudError,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextFieldWithError(
                                value = longitud,
                                onValueChange = { longitud = it },
                                label = "Longitud",
                                errorMessage = longitudError,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                },
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Navegación entre pasos
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStep == totalSteps - 1) {
                            Button(
                                onClick = { agregarCentro() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Guardar Centro")
                            }
                        } else {
                            Button(
                                onClick = {
                                    when (currentStep) {
                                        0 -> if (validateStep0()) currentStep++
                                        1 -> if (validateStep1()) currentStep++
                                        else -> {}
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Siguiente")
                            }
                        }
                        
                        if (currentStep > 0) {
                            Button(
                                onClick = { currentStep-- },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Anterior")
                            }
                        }
                    }
                }
            }
        }
    }
} 