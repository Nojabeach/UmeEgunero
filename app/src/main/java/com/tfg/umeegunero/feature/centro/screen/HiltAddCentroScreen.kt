package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
import kotlinx.coroutines.launch

/**
 * Wrapper de Hilt para la pantalla de añadir/editar centro
 * Esta capa adicional nos permite inyectar el ViewModel con Hilt
 */
@Composable
fun HiltAddCentroScreen(
    viewModel: AddCentroViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCentroAdded: () -> Unit,
    centroId: String = ""
) {
    val uiState by viewModel.uiState.collectAsState()

    // Si se proporciona un ID, cargar el centro existente para edición
    LaunchedEffect(centroId) {
        if (centroId.isNotBlank()) {
            viewModel.loadCentro(centroId)
        }
    }

    // UI principal
    AddCentroScreen(
        uiState = uiState,
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
        onSaveCentro = viewModel::saveCentro,
        onNavigateBack = onNavigateBack,
        onClearError = viewModel::clearError
    )

    // Si se guarda con éxito, navegar hacia atrás
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onCentroAdded()
        }
    }
}

/**
 * Pantalla para añadir o editar un centro educativo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreen(
    uiState: com.tfg.umeegunero.feature.admin.viewmodel.AddCentroUiState,
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
    onSaveCentro: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    val isEditing = uiState.id.isNotBlank()
    val scrollState = rememberScrollState()
    val snackbarHostState = SnackbarHostState()
    val scope = rememberCoroutineScope()

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                onClearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Centro" else "Añadir Centro"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSaveCentro,
                        enabled = !uiState.isLoading && uiState.isFormValid
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Guardar"
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Sección de Información Básica
                Text(
                    text = "Información Básica",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre del Centro") },
                    isError = uiState.nombreError != null,
                    supportingText = {
                        uiState.nombreError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sección de Dirección
                Text(
                    text = "Dirección",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.calle,
                    onValueChange = onCalleChange,
                    label = { Text("Calle") },
                    isError = uiState.calleError != null,
                    supportingText = {
                        uiState.calleError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fila para Número y Código Postal
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.numero,
                        onValueChange = onNumeroChange,
                        label = { Text("Número") },
                        isError = uiState.numeroError != null,
                        supportingText = {
                            uiState.numeroError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = uiState.codigoPostal,
                        onValueChange = onCodigoPostalChange,
                        label = { Text("Código Postal") },
                        isError = uiState.codigoPostalError != null,
                        supportingText = {
                            uiState.codigoPostalError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila para Ciudad y Provincia
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.ciudad,
                        onValueChange = onCiudadChange,
                        label = { Text("Ciudad") },
                        isError = uiState.ciudadError != null,
                        supportingText = {
                            uiState.ciudadError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.provincia,
                        onValueChange = onProvinciaChange,
                        label = { Text("Provincia") },
                        isError = uiState.provinciaError != null,
                        supportingText = {
                            uiState.provinciaError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección de Contacto
                Text(
                    text = "Información de Contacto",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.telefono,
                    onValueChange = onTelefonoChange,
                    label = { Text("Teléfono") },
                    isError = uiState.telefonoError != null,
                    supportingText = {
                        uiState.telefonoError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    isError = uiState.emailError != null,
                    supportingText = {
                        uiState.emailError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Sección de contraseñas (solo para nuevos centros)
                if (!isEditing) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Credenciales de Acceso",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Contraseña") },
                        isError = uiState.passwordError != null,
                        supportingText = {
                            uiState.passwordError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("Confirmar Contraseña") },
                        isError = uiState.confirmPasswordError != null,
                        supportingText = {
                            uiState.confirmPasswordError?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de guardar
                Button(
                    onClick = onSaveCentro,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && uiState.isFormValid
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(text = if (isEditing) "Actualizar Centro" else "Guardar Centro")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Indicador de carga
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}