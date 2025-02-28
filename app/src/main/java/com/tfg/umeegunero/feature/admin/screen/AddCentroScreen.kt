package com.tfg.umeegunero.feature.admin.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroUiState
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreen(
    uiState: AddCentroUiState,
    onUpdateNombre: (String) -> Unit,
    onUpdateCalle: (String) -> Unit,
    onUpdateNumero: (String) -> Unit,
    onUpdateCodigoPostal: (String) -> Unit,
    onUpdateCiudad: (String) -> Unit,
    onUpdateProvincia: (String) -> Unit,
    onUpdateTelefono: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onSave: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit,
    onCentroAdded: () -> Unit,
    isEditMode: Boolean = false
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Calcular progreso del formulario
    val totalFields = 8 // Número total de campos en el formulario
    val filledFields = listOf(
        uiState.nombre.isNotBlank(),
        uiState.calle.isNotBlank(),
        uiState.numero.isNotBlank(),
        uiState.codigoPostal.isNotBlank(),
        uiState.ciudad.isNotBlank(),
        uiState.provincia.isNotBlank(),
        uiState.telefono.isNotBlank(),
        uiState.email.isNotBlank()
    ).count { it }
    val formProgress = filledFields.toFloat() / totalFields.toFloat()

    // Detectar éxito al añadir centro
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            val mensaje = if (isEditMode) "Centro educativo actualizado correctamente" else "Centro educativo añadido correctamente"
            snackbarHostState.showSnackbar(mensaje)
            // Esperar un poco antes de navegar de vuelta
            kotlinx.coroutines.delay(1500)
            onCentroAdded()
        }
    }

    // Mostrar errores en Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                onClearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Centro Educativo" else "Añadir Centro Educativo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            onSave()
                        },
                        enabled = !uiState.isLoading && uiState.isFormValid
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { formProgress },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Progreso del formulario: ${(formProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Información básica del centro
            FormSection(
                title = "Información del Centro",
                icon = Icons.Default.School
            ) {
                FormTextField(
                    value = uiState.nombre,
                    onValueChange = onUpdateNombre,
                    label = "Nombre del Centro",
                    icon = Icons.Default.School,
                    errorMessage = uiState.nombreError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            // Dirección del centro
            FormSection(
                title = "Dirección",
                icon = Icons.Default.Home
            ) {
                FormTextField(
                    value = uiState.calle,
                    onValueChange = onUpdateCalle,
                    label = "Calle",
                    icon = Icons.Default.Home,
                    errorMessage = uiState.calleError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormTextField(
                        value = uiState.numero,
                        onValueChange = onUpdateNumero,
                        label = "Número",
                        errorMessage = uiState.numeroError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Right) }
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FormTextField(
                        value = uiState.codigoPostal,
                        onValueChange = onUpdateCodigoPostal,
                        label = "Código Postal",
                        errorMessage = uiState.codigoPostalError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                FormTextField(
                    value = uiState.ciudad,
                    onValueChange = onUpdateCiudad,
                    label = "Ciudad",
                    icon = Icons.Default.LocationCity,
                    errorMessage = uiState.ciudadError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                FormTextField(
                    value = uiState.provincia,
                    onValueChange = onUpdateProvincia,
                    label = "Provincia",
                    icon = Icons.Default.LocationCity,
                    errorMessage = uiState.provinciaError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            // Contacto del centro
            FormSection(
                title = "Información de Contacto",
                icon = Icons.Default.Phone
            ) {
                FormTextField(
                    value = uiState.telefono,
                    onValueChange = onUpdateTelefono,
                    label = "Teléfono",
                    icon = Icons.Default.Phone,
                    errorMessage = uiState.telefonoError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                FormTextField(
                    value = uiState.email,
                    onValueChange = onUpdateEmail,
                    label = "Email",
                    icon = Icons.Default.Email,
                    errorMessage = uiState.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (uiState.isFormValid) {
                                onSave()
                            }
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de guardar
            Button(
                onClick = {
                    keyboardController?.hide()
                    onSave()
                },
                enabled = !uiState.isLoading && uiState.isFormValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (isEditMode) "Actualizar Centro" else "Guardar Centro",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!uiState.isFormValid) {
                AnimatedVisibility(
                    visible = !uiState.isFormValid,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(
                            text = "Complete todos los campos obligatorios para guardar",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FormSection(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            content()
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (errorMessage != null)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = errorMessage != null,
        supportingText = {
            if (errorMessage != null) {
                Text(text = errorMessage)
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

// Adaptador para pasar del ViewModel a la interfaz de la pantalla
@Composable
fun AddCentroScreenWithViewModel(
    viewModel: AddCentroViewModel,
    onNavigateBack: () -> Unit,
    onCentroAdded: () -> Unit,
    isEditMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    AddCentroScreen(
        uiState = uiState,
        onUpdateNombre = { viewModel.updateNombre(it) },
        onUpdateCalle = { viewModel.updateCalle(it) },
        onUpdateNumero = { viewModel.updateNumero(it) },
        onUpdateCodigoPostal = { viewModel.updateCodigoPostal(it) },
        onUpdateCiudad = { viewModel.updateCiudad(it) },
        onUpdateProvincia = { viewModel.updateProvincia(it) },
        onUpdateTelefono = { viewModel.updateTelefono(it) },
        onUpdateEmail = { viewModel.updateEmail(it) },
        onSave = { viewModel.saveCentro() },
        onClearError = { viewModel.clearError() },
        onNavigateBack = onNavigateBack,
        onCentroAdded = onCentroAdded,
        isEditMode = isEditMode
    )
}

/**
 * Componente para la pantalla de añadir/editar centro educativo utilizando Hilt para la inyección de dependencias.
 *
 * @param viewModel El ViewModel con la lógica de añadir/editar centro
 * @param onNavigateBack Función a ejecutar para volver atrás
 * @param onCentroAdded Función a ejecutar cuando se ha añadido/editado el centro
 * @param centroId ID del centro a editar (null si es un centro nuevo)
 */
@Composable
fun HiltAddCentroScreen(
    viewModel: AddCentroViewModel,
    onNavigateBack: () -> Unit,
    onCentroAdded: () -> Unit,
    centroId: String? = null
) {
    // Si tenemos un centroId, cargar los datos del centro para edición
    LaunchedEffect(centroId) {
        if (!centroId.isNullOrEmpty()) {
            viewModel.loadCentro(centroId)
        }
    }

    // Usar el adaptador para la pantalla
    AddCentroScreenWithViewModel(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onCentroAdded = onCentroAdded,
        isEditMode = !centroId.isNullOrEmpty()
    )
}

@Preview(showBackground = true)
@Composable
fun AddCentroScreenPreview() {
    // Crear un estado para el preview
    val previewState = AddCentroUiState(
        id = UUID.randomUUID().toString(),
        nombre = "Colegio San José",
        calle = "Avenida Principal",
        numero = "42",
        codigoPostal = "28001",
        ciudad = "Madrid",
        provincia = "Madrid",
        telefono = "912345678",
        email = "contacto@sanjose.edu"
    )

    UmeEguneroTheme {
        Surface {
            AddCentroScreen(
                uiState = previewState,
                onUpdateNombre = {},
                onUpdateCalle = {},
                onUpdateNumero = {},
                onUpdateCodigoPostal = {},
                onUpdateCiudad = {},
                onUpdateProvincia = {},
                onUpdateTelefono = {},
                onUpdateEmail = {},
                onSave = {},
                onClearError = {},
                onNavigateBack = {},
                onCentroAdded = {},
                isEditMode = false
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddCentroScreenDarkPreview() {
    // Crear un estado para el preview
    val previewState = AddCentroUiState(
        id = UUID.randomUUID().toString(),
        nombre = "Colegio San José",
        calle = "Avenida Principal",
        numero = "42",
        codigoPostal = "28001",
        ciudad = "Madrid",
        provincia = "Madrid",
        telefono = "912345678",
        email = "contacto@sanjose.edu"
    )

    UmeEguneroTheme(darkTheme = true) {
        Surface {
            AddCentroScreen(
                uiState = previewState,
                onUpdateNombre = {},
                onUpdateCalle = {},
                onUpdateNumero = {},
                onUpdateCodigoPostal = {},
                onUpdateCiudad = {},
                onUpdateProvincia = {},
                onUpdateTelefono = {},
                onUpdateEmail = {},
                onSave = {},
                onClearError = {},
                onNavigateBack = {},
                onCentroAdded = {},
                isEditMode = false
            )
        }
    }
}