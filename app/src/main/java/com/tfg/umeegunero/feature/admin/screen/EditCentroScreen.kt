package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.feature.admin.viewmodel.EditCentroViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import android.net.Uri
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.lazy.LazyColumn
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroUiState

/**
 * Pantalla para editar un centro educativo existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCentroScreen(
    navController: NavController,
    centroId: String,
    viewModel: EditCentroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(key1 = Unit) {
        viewModel.loadCentro()
    }
    
    LaunchedEffect(key1 = uiState.success) {
        if (uiState.success) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Editar Centro") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            EditCentroContent(
                uiState = uiState,
                onUpdateNombre = viewModel::updateNombre,
                onUpdateCalle = viewModel::updateCalle,
                onUpdateNumero = viewModel::updateNumero,
                onUpdateCodigoPostal = viewModel::updateCodigoPostal,
                onUpdateCiudad = viewModel::updateCiudad,
                onUpdateProvincia = viewModel::updateProvincia,
                onUpdateTelefono = viewModel::updateTelefono,
                onUpdateAdminEmail = viewModel::updateAdminEmail,
                onUpdateAdminPassword = viewModel::updateAdminPassword,
                onUpdateLatitud = viewModel::updateLatitud,
                onUpdateLongitud = viewModel::updateLongitud,
                onSeleccionarCiudad = viewModel::seleccionarCiudad,
                onGuardar = viewModel::updateCentro,
                onNavigateBack = { navController.popBackStack() },
                onAddAdminCentro = viewModel::addAdminCentro,
                onRemoveAdminCentro = viewModel::removeAdminCentro,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EditCentroContent(
    uiState: AddCentroUiState,
    onUpdateNombre: (String) -> Unit,
    onUpdateCalle: (String) -> Unit,
    onUpdateNumero: (String) -> Unit,
    onUpdateCodigoPostal: (String) -> Unit,
    onUpdateCiudad: (String) -> Unit,
    onUpdateProvincia: (String) -> Unit,
    onUpdateTelefono: (String) -> Unit,
    onUpdateAdminEmail: (Int, String) -> Unit,
    onUpdateAdminPassword: (Int, String) -> Unit,
    onUpdateLatitud: (Double?) -> Unit,
    onUpdateLongitud: (Double?) -> Unit,
    onSeleccionarCiudad: (com.tfg.umeegunero.data.model.Ciudad) -> Unit,
    onGuardar: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddAdminCentro: () -> Unit,
    onRemoveAdminCentro: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val provinciasLista = listOf("Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz", "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real", "Córdoba", "Cuenca", "Girona", "Granada", "Guadalajara", "Guipúzcoa", "Huelva", "Huesca", "Islas Baleares", "Jaén", "La Coruña", "La Rioja", "Las Palmas", "León", "Lérida", "Lugo", "Madrid", "Málaga", "Murcia", "Navarra", "Orense", "Palencia", "Pontevedra", "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria", "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza")
    var expandedProvincia by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Editar Centro Educativo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Datos básicos
        Text(
            text = "Datos Básicos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Nombre del centro
        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = onUpdateNombre,
            label = { Text("Nombre del centro *") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nombreError != null,
            supportingText = uiState.nombreError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Dirección
        Text(
            text = "Dirección",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Calle
        OutlinedTextField(
            value = uiState.calle,
            onValueChange = onUpdateCalle,
            label = { Text("Calle *") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.calleError != null,
            supportingText = uiState.calleError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Número
        OutlinedTextField(
            value = uiState.numero,
            onValueChange = onUpdateNumero,
            label = { Text("Número *") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Pin,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.numeroError != null,
            supportingText = uiState.numeroError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Código Postal
        OutlinedTextField(
            value = uiState.codigoPostal,
            onValueChange = onUpdateCodigoPostal,
            label = { Text("Código Postal *") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Pin,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.codigoPostalError != null,
            supportingText = uiState.codigoPostalError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Ciudad
        OutlinedTextField(
            value = uiState.ciudad,
            onValueChange = onUpdateCiudad,
            label = { Text("Ciudad *") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.ciudadError != null,
            supportingText = uiState.ciudadError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        
        // Mostrar sugerencias de ciudades si hay
        if (uiState.ciudadesSugeridas.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Ciudades sugeridas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    uiState.ciudadesSugeridas.forEach { ciudad ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSeleccionarCiudad(ciudad) }
                        ) {
                            Text(
                                text = "${ciudad.nombre}, ${ciudad.provincia}",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Divider()
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Provincia
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.provincia,
                onValueChange = onUpdateProvincia,
                label = { Text("Provincia *") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { expandedProvincia = !expandedProvincia }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Desplegar provincias"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.provinciaError != null,
                supportingText = uiState.provinciaError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            
            DropdownMenu(
                expanded = expandedProvincia,
                onDismissRequest = { expandedProvincia = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                provinciasLista.forEach { provincia ->
                    DropdownMenuItem(
                        text = { Text(provincia) },
                        onClick = {
                            onUpdateProvincia(provincia)
                            expandedProvincia = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Coordenadas geográficas
        Text(
            text = "Coordenadas Geográficas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Latitud
        OutlinedTextField(
            value = uiState.latitud?.toString() ?: "",
            onValueChange = { 
                try {
                    // Si había un valor de 0.0 y es el comienzo de una edición, borrarlo completamente
                    val newValue = if (uiState.latitud == 0.0 && it.isNotEmpty()) it else it
                    val latValue = if (newValue.isBlank()) null else newValue.toDouble()
                    onUpdateLatitud(latValue)
                } catch (e: NumberFormatException) {
                    // Intentar limpiar el texto para que sea un número válido
                    val cleanText = it.replace(",", ".")
                    try {
                        val latValue = if (cleanText.isBlank()) null else cleanText.toDouble()
                        onUpdateLatitud(latValue)
                    } catch (e: NumberFormatException) {
                        // Seguimos permitiendo la entrada del texto para que pueda seguir editando
                        if (it.isEmpty()) {
                            onUpdateLatitud(null)
                        }
                    }
                }
            },
            label = { Text("Latitud") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            supportingText = { Text("Ej: 40.416775 (Madrid)") }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Longitud
        OutlinedTextField(
            value = uiState.longitud?.toString() ?: "",
            onValueChange = { 
                try {
                    // Si había un valor de 0.0 y es el comienzo de una edición, borrarlo completamente
                    val newValue = if (uiState.longitud == 0.0 && it.isNotEmpty()) it else it
                    val longValue = if (newValue.isBlank()) null else newValue.toDouble()
                    onUpdateLongitud(longValue)
                } catch (e: NumberFormatException) {
                    // Intentar limpiar el texto para que sea un número válido
                    val cleanText = it.replace(",", ".")
                    try {
                        val longValue = if (cleanText.isBlank()) null else cleanText.toDouble()
                        onUpdateLongitud(longValue)
                    } catch (e: NumberFormatException) {
                        // Seguimos permitiendo la entrada del texto para que pueda seguir editando
                        if (it.isEmpty()) {
                            onUpdateLongitud(null)
                        }
                    }
                }
            },
            label = { Text("Longitud") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            supportingText = { Text("Ej: -3.703790 (Madrid)") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contacto
        Text(
            text = "Contacto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Teléfono
        OutlinedTextField(
            value = uiState.telefono,
            onValueChange = onUpdateTelefono,
            label = { Text("Teléfono") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.telefonoError != null,
            supportingText = uiState.telefonoError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sección de Administradores
        Text(
            text = "Administradores del Centro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (uiState.adminCentroError != null) {
            Text(
                text = uiState.adminCentroError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Lista de administradores
        uiState.adminCentro.forEachIndexed { index, admin ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Título del administrador
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (index == 0) "Administrador Principal" else "Administrador Adicional ${index}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Botón para eliminar administrador (solo para administradores secundarios)
                        if (index > 0) {
                            IconButton(
                                onClick = { onRemoveAdminCentro(index) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar administrador",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    // Email
                    OutlinedTextField(
                        value = admin.email,
                        onValueChange = { onUpdateAdminEmail(index, it) },
                        label = { Text("Email *") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = admin.emailError != null,
                        supportingText = admin.emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Contraseña
                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = admin.password,
                        onValueChange = { onUpdateAdminPassword(index, it) },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = admin.passwordError != null,
                        supportingText = {
                            if (admin.passwordError != null) {
                                Text(admin.passwordError)
                            } else {
                                Text("Deje en blanco para mantener la contraseña actual")
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }
        }
        
        // Botón para añadir un nuevo administrador
        if (uiState.adminCentro.size < 3) { // Limitar a 3 administradores
            Button(
                onClick = onAddAdminCentro,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir administrador",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Administrador")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onGuardar,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading &&
                        uiState.nombre.isNotBlank() &&
                        uiState.calle.isNotBlank() &&
                        uiState.numero.isNotBlank() &&
                        uiState.codigoPostal.isNotBlank() &&
                        uiState.ciudad.isNotBlank() &&
                        uiState.provincia.isNotBlank() &&
                        (uiState.adminCentro.isEmpty() || uiState.adminCentro[0].email.isNotBlank())
            ) {
                Text("Guardar")
            }
        }
    }
}

private fun isFormValid(state: AddCentroUiState): Boolean {
    return state.nombre.isNotBlank() && state.nombreError == null &&
           state.calle.isNotBlank() && state.calleError == null &&
           state.numero.isNotBlank() && state.numeroError == null &&
           state.codigoPostal.isNotBlank() && state.codigoPostalError == null &&
           state.ciudad.isNotBlank() && state.ciudadError == null &&
           state.provincia.isNotBlank() && state.provinciaError == null &&
           (state.telefono.isBlank() || state.telefonoError == null)
}

@Preview(showBackground = true)
@Composable
fun EditCentroScreenPreview() {
    UmeEguneroTheme {
        // Simplemente mostrar un formulario básico para el preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Editar Centro Educativo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = "IES Valle Inclán",
                onValueChange = { },
                label = { Text("Nombre del centro") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = "Calle Principal",
                onValueChange = { },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = "28001 Madrid",
                onValueChange = { },
                label = { Text("Código Postal y Ciudad") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditCentroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        // Simplemente mostrar un formulario básico para el preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Editar Centro Educativo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = "IES Valle Inclán",
                onValueChange = { },
                label = { Text("Nombre del centro") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = "Calle Principal",
                onValueChange = { },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = "28001 Madrid",
                onValueChange = { },
                label = { Text("Código Postal y Ciudad") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 