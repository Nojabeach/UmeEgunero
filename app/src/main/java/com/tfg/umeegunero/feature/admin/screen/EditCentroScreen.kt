package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator

/**
 * Pantalla para editar un centro educativo existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCentroScreen(
    navController: NavController,
    centroId: String,
    viewModel: AddCentroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar los datos del centro al inicio
    LaunchedEffect(centroId) {
        viewModel.loadCentro(centroId)
    }
    
    // Observar cambios en el estado y navegar de vuelta si la operación fue exitosa
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Centro Educativo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                LoadingIndicator()
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
                onSeleccionarCiudad = viewModel::seleccionarCiudad,
                onGuardar = {
                    val centro = Centro(
                        id = uiState.id,
                        nombre = uiState.nombre,
                        direccion = Direccion(
                            calle = uiState.calle,
                            numero = uiState.numero,
                            codigoPostal = uiState.codigoPostal,
                            ciudad = uiState.ciudad,
                            provincia = uiState.provincia
                        ),
                        contacto = Contacto(
                            telefono = uiState.telefono,
                            email = uiState.adminCentro.firstOrNull()?.email ?: ""
                        ),
                        latitud = uiState.latitud ?: 0.0,
                        longitud = uiState.longitud ?: 0.0
                    )
                    viewModel.saveCentro(centro)
                },
                onNavigateBack = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EditCentroContent(
    uiState: com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel.AddCentroState,
    onUpdateNombre: (String) -> Unit,
    onUpdateCalle: (String) -> Unit,
    onUpdateNumero: (String) -> Unit,
    onUpdateCodigoPostal: (String) -> Unit,
    onUpdateCiudad: (String) -> Unit,
    onUpdateProvincia: (String) -> Unit,
    onUpdateTelefono: (String) -> Unit,
    onSeleccionarCiudad: (com.tfg.umeegunero.data.model.Ciudad) -> Unit,
    onGuardar: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val provinciasLista = listOf("Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz", "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real", "Córdoba", "Cuenca", "Girona", "Granada", "Guadalajara", "Guipúzcoa", "Huelva", "Huesca", "Islas Baleares", "Jaén", "La Coruña", "La Rioja", "Las Palmas", "León", "Lérida", "Lugo", "Madrid", "Málaga", "Murcia", "Navarra", "Orense", "Palencia", "Pontevedra", "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria", "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza")
    var expandedProvincia by remember { mutableStateOf(false) }
    
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
        ExposedDropdownMenuBox(
            expanded = expandedProvincia,
            onExpandedChange = { expandedProvincia = it }
        ) {
            OutlinedTextField(
                value = uiState.provincia,
                onValueChange = onUpdateProvincia,
                label = { Text("Provincia *") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = uiState.provinciaError != null,
                supportingText = uiState.provinciaError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvincia)
                }
            )
            
            ExposedDropdownMenu(
                expanded = expandedProvincia,
                onDismissRequest = { expandedProvincia = false }
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
        
        // Contacto
        Text(
            text = "Información de Contacto",
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
                    imageVector = Icons.Default.Call,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.telefonoError != null,
            supportingText = uiState.telefonoError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            
            Button(
                onClick = onGuardar,
                modifier = Modifier.weight(1f),
                enabled = isFormValid(uiState)
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}

private fun isFormValid(state: com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel.AddCentroState): Boolean {
    return state.nombre.isNotBlank() && state.nombreError == null &&
           state.calle.isNotBlank() && state.calleError == null &&
           state.numero.isNotBlank() && state.numeroError == null &&
           state.codigoPostal.isNotBlank() && state.codigoPostalError == null &&
           state.ciudad.isNotBlank() && state.ciudadError == null &&
           state.provincia.isNotBlank() && state.provinciaError == null &&
           (state.telefono.isBlank() || state.telefonoError == null)
} 