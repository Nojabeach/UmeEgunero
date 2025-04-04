package com.tfg.umeegunero.feature.admin.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.components.OutlinedTextFieldWithError

// Clase para gestionar el estado de la pantalla
class AddCentroState {
    // Estados del formulario
    var currentStep by mutableStateOf(0)
    val totalSteps = 3
    
    // Datos del formulario
    var nombre by mutableStateOf("")
    var direccion by mutableStateOf("")
    var telefono by mutableStateOf("")
    var email by mutableStateOf("")
    var latitud by mutableStateOf("")
    var longitud by mutableStateOf("")
    
    // Estados de validación
    var nombreError by mutableStateOf("")
    var direccionError by mutableStateOf("")
    var telefonoError by mutableStateOf("")
    var emailError by mutableStateOf("")
    var latitudError by mutableStateOf("")
    var longitudError by mutableStateOf("")
    
    // Estado de carga
    var isLoading by mutableStateOf(false)
    
    // Funciones de validación
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
    
    fun nextStep() {
        if (currentStep < totalSteps - 1) {
            when (currentStep) {
                0 -> {
                    if (validateStep0()) {
                        currentStep += 1
                    }
                }
                1 -> {
                    if (validateStep1()) {
                        currentStep += 1
                    }
                }
            }
        }
    }
    
    fun previousStep() {
        if (currentStep > 0) {
            currentStep -= 1
        }
    }
    
    fun agregarCentro(viewModel: AdminViewModel, context: Context, onCentroAdded: () -> Unit) {
        if (validateForm()) {
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
                    Toast.makeText(context, "Error al agregar el centro", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "Por favor, corrija los errores del formulario", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onCentroAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    // Crear estado de la pantalla
    val state = remember { AddCentroState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Centro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de progreso
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Paso ${state.currentStep + 1} de ${state.totalSteps}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (state.currentStep + 1).toFloat() / state.totalSteps },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Contenido según el paso actual
            when (state.currentStep) {
                0 -> {
                    // Paso 1: Nombre del centro
                    Text(
                        text = "Información Básica",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextFieldWithError(
                        value = state.nombre,
                        onValueChange = { state.nombre = it },
                        label = "Nombre del Centro",
                        errorMessage = state.nombreError,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                }
                
                1 -> {
                    // Paso 2: Datos de contacto
                    Text(
                        text = "Datos de Contacto",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextFieldWithError(
                        value = state.direccion,
                        onValueChange = { state.direccion = it },
                        label = "Dirección",
                        errorMessage = state.direccionError,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextFieldWithError(
                        value = state.telefono,
                        onValueChange = { state.telefono = it },
                        label = "Teléfono",
                        errorMessage = state.telefonoError,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextFieldWithError(
                        value = state.email,
                        onValueChange = { state.email = it },
                        label = "Email",
                        errorMessage = state.emailError,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )
                    )
                }
                
                2 -> {
                    // Paso 3: Ubicación
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Coordenadas (opcional)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextFieldWithError(
                        value = state.latitud,
                        onValueChange = { state.latitud = it },
                        label = "Latitud",
                        errorMessage = state.latitudError,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextFieldWithError(
                        value = state.longitud,
                        onValueChange = { state.longitud = it },
                        label = "Longitud",
                        errorMessage = state.longitudError,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.currentStep > 0) {
                    TextButton(
                        onClick = { state.previousStep() }
                    ) {
                        Text("Anterior")
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }
                
                if (state.currentStep < state.totalSteps - 1) {
                    Button(
                        onClick = { state.nextStep() }
                    ) {
                        Text("Siguiente")
                    }
                } else {
                    Button(
                        onClick = { state.agregarCentro(viewModel, context, onCentroAdded) }
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
        
        // Indicador de carga
        if (state.isLoading) {
            LoadingIndicator(message = "Guardando centro...")
        }
    }
} 