package com.tfg.umeegunero.feature.admin.screen

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    onNavigateBack: () -> Unit,
    onCentroAdded: () -> Unit,
    centroId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val provinciasLista by viewModel.provincias.collectAsState()

    // Si tenemos un centroId, cargar los datos del centro para edición
    LaunchedEffect(centroId) {
        if (!centroId.isNullOrEmpty()) {
            viewModel.loadCentro(centroId)
        }
    }

    val isEditMode = !centroId.isNullOrEmpty()

    AddCentroScreenContent(
        uiState = uiState,
        provincias = provinciasLista,
        onUpdateNombre = { viewModel.updateNombre(it) },
        onUpdateCalle = { viewModel.updateCalle(it) },
        onUpdateNumero = { viewModel.updateNumero(it) },
        onUpdateCodigoPostal = { viewModel.updateCodigoPostal(it) },
        onUpdateCiudad = { viewModel.updateCiudad(it) },
        onUpdateProvincia = { viewModel.updateProvincia(it) },
        onUpdateTelefono = { viewModel.updateTelefono(it) },
        onUpdateEmail = { viewModel.updateEmail(it) },
        onUpdatePassword = { viewModel.updatePassword(it) },
        onUpdateConfirmPassword = { viewModel.updateConfirmPassword(it) },
        onSave = { viewModel.saveCentro() },
        onClearError = { viewModel.clearError() },
        onNavigateBack = onNavigateBack,
        onCentroAdded = onCentroAdded,
        isEditMode = isEditMode,
        onSeleccionarCiudad = { viewModel.seleccionarCiudad(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreenContent(
    uiState: AddCentroUiState,
    provincias: List<String>,
    onUpdateNombre: (String) -> Unit,
    onUpdateCalle: (String) -> Unit,
    onUpdateNumero: (String) -> Unit,
    onUpdateCodigoPostal: (String) -> Unit,
    onUpdateCiudad: (String) -> Unit,
    onUpdateProvincia: (String) -> Unit,
    onUpdateTelefono: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onUpdateConfirmPassword: (String) -> Unit,
    onSave: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit,
    onCentroAdded: () -> Unit,
    isEditMode: Boolean = false,
    onSeleccionarCiudad: (Ciudad) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Calcular progreso del formulario
    val totalFields = if (isEditMode) 8 else 10 // Número total de campos en el formulario, incluidas las contraseñas si no es modo edición
    val filledFields = remember(uiState) {
        var count = 0
        if (uiState.nombre.isNotBlank() && uiState.nombreError == null) count++
        if (uiState.calle.isNotBlank() && uiState.calleError == null) count++
        if (uiState.numero.isNotBlank() && uiState.numeroError == null) count++
        if (uiState.codigoPostal.isNotBlank() && uiState.codigoPostalError == null) count++
        if (uiState.ciudad.isNotBlank() && uiState.ciudadError == null) count++
        if (uiState.provincia.isNotBlank() && uiState.provinciaError == null) count++
        if (uiState.telefono.isNotBlank() && uiState.telefonoError == null) count++
        if (uiState.email.isNotBlank() && uiState.emailError == null) count++
        if (!isEditMode) {
            if (uiState.password.isNotBlank() && uiState.passwordError == null) count++
            if (uiState.confirmPassword.isNotBlank() && uiState.confirmPasswordError == null) count++
        }
        count
    }
    val formProgress = filledFields.toFloat() / totalFields.toFloat()

    // Mostrar mensaje de éxito y navegar de vuelta
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = if (isEditMode) "Centro actualizado correctamente" else "Centro añadido correctamente",
                    duration = SnackbarDuration.Short
                )
            }
            onCentroAdded()
        }
    }

    // Mostrar mensaje de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
            onClearError()
        }
    }

    // Variable para controlar la visibilidad del menú desplegable de ciudades
    var showCiudadesDropdown by remember { mutableStateOf(false) }

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

                    Box(modifier = Modifier.weight(1f)) {
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
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (uiState.isBuscandoCiudades) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                    }
                }

                // Mostrar mensaje de error de búsqueda de ciudades
                if (uiState.errorBusquedaCiudades != null) {
                    Text(
                        text = uiState.errorBusquedaCiudades,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                // Campo de ciudad con dropdown para seleccionar de las sugerencias
                Box(modifier = Modifier.fillMaxWidth()) {
                    FormTextField(
                        value = uiState.ciudad,
                        onValueChange = onUpdateCiudad,
                        label = "Ciudad",
                        icon = Icons.Default.LocationCity,
                        errorMessage = uiState.ciudadError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (uiState.ciudadesSugeridas.isNotEmpty()) {
                                IconButton(onClick = { showCiudadesDropdown = !showCiudadesDropdown }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Mostrar ciudades"
                                    )
                                }
                            }
                        },
                        onFocus = { showCiudadesDropdown = uiState.ciudadesSugeridas.isNotEmpty() }
                    )

                    // Dropdown menu para mostrar las ciudades sugeridas
                    DropdownMenu(
                        expanded = showCiudadesDropdown && uiState.ciudadesSugeridas.isNotEmpty(),
                        onDismissRequest = { showCiudadesDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 200.dp)
                    ) {
                        uiState.ciudadesSugeridas.forEach { ciudad ->
                            DropdownMenuItem(
                                text = { Text("${ciudad.nombre} (${ciudad.provincia})") },
                                onClick = {
                                    onSeleccionarCiudad(ciudad)
                                    showCiudadesDropdown = false
                                }
                            )
                        }
                    }
                }

                // Selector de provincia con nombres multilingües
                ProvinciaSelector(
                    provincias = provincias,
                    provinciaSeleccionada = uiState.provincia,
                    onProvinciaSeleccionada = onUpdateProvincia,
                    errorMessage = uiState.provinciaError
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
                        imeAction = if (isEditMode) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            if (isEditMode) {
                                keyboardController?.hide()
                                if (uiState.isFormValid) {
                                    onSave()
                                }
                            } else {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        }
                    )
                )

                // Mostrar los campos de contraseña solo si no es modo edición
                if (!isEditMode) {
                    // Nuevo campo para la contraseña
                    PasswordTextField(
                        value = uiState.password,
                        onValueChange = onUpdatePassword,
                        label = "Contraseña de acceso",
                        errorMessage = uiState.passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Nuevo campo para confirmar la contraseña
                    PasswordTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onUpdateConfirmPassword,
                        label = "Confirmar contraseña",
                        errorMessage = uiState.confirmPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinciaSelector(
    provincias: List<String>,
    provinciaSeleccionada: String,
    onProvinciaSeleccionada: (String) -> Unit,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Provincias filtradas según lo que el usuario escribe
    val provinciasFiltradas = remember(searchText) {
        if (searchText.isEmpty()) {
            provincias
        } else {
            provincias.filter {
                it.contains(searchText, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = provinciaSeleccionada,
                onValueChange = {
                    searchText = it
                    onProvinciaSeleccionada(it)
                },
                label = { Text("Provincia") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = if (errorMessage != null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar provincia",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                isError = errorMessage != null,
                supportingText = {
                    if (errorMessage != null) {
                        Text(text = errorMessage)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Hacemos que el campo completo sea clickable para abrir el dropdown
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = !expanded }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f),
                properties = PopupProperties(focusable = true)
            ) {
                // Campo de búsqueda dentro del dropdown
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar provincia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true
                )

                // Lista de provincias filtradas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp)
                ) {
                    items(provinciasFiltradas) { provincia ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = provincia,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onProvinciaSeleccionada(provincia)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (errorMessage != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Password),
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
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    onFocus: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Detectar cuando el campo recibe el foco
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Focus) {
                onFocus?.invoke()
            }
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon,
        isError = errorMessage != null,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage)
            }
        },
        modifier = modifier.fillMaxWidth(),
        interactionSource = interactionSource
    )
}

// Extendiendo el ViewModel para añadir las propiedades necesarias para las contraseñas
fun AddCentroViewModel.updatePassword(password: String) {
    // Esta función debería implementarse en el ViewModel
}

fun AddCentroViewModel.updateConfirmPassword(confirmPassword: String) {
    // Esta función debería implementarse en el ViewModel
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
                onUpdateNombre = {},
                onUpdateCalle = {},
                onUpdateNumero = {},
                onUpdateCodigoPostal = {},
                onUpdateCiudad = {},
                onUpdateProvincia = {},
                onUpdateTelefono = {},
                onUpdateEmail = {},
                onUpdatePassword = {},
                onUpdateConfirmPassword = {},
                onSave = {},
                onClearError = {},
                onNavigateBack = {},
                onCentroAdded = {},
                isEditMode = false,
                onSeleccionarCiudad = {}
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
                onUpdateNombre = {},
                onUpdateCalle = {},
                onUpdateNumero = {},
                onUpdateCodigoPostal = {},
                onUpdateCiudad = {},
                onUpdateProvincia = {},
                onUpdateTelefono = {},
                onUpdateEmail = {},
                onUpdatePassword = {},
                onUpdateConfirmPassword = {},
                onSave = {},
                onClearError = {},
                onNavigateBack = {},
                onCentroAdded = {},
                isEditMode = false,
                onSeleccionarCiudad = {}
            )
        }
    }
}

private suspend fun buscarCiudadesEnGeoApi(codigoPostal: String, codigoProvincia: String): List<Ciudad> {
    return withContext(Dispatchers.IO) {
        try {
            val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val url = original.url.newBuilder()
                        .addQueryParameter("sandbox", "1")
                        .build()
                    val request = original.newBuilder().url(url).build()
                    chain.proceed(request)
                }
                .build()

            val response = httpClient.newCall(
                Request.Builder()
                    .url("https://apiv1.geoapi.es/codigosPostales?codigoProvincia=$codigoProvincia")
                    .build()
            ).execute()
            
            if (response.isSuccessful) {
                // Resto del código...
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener códigos postales de GeoAPI")
        }
        
        emptyList()
    }
}
