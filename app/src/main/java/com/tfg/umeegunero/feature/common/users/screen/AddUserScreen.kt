package com.tfg.umeegunero.feature.common.users.screen

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.AdminColor
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Estado de UI para la pantalla de añadir usuario
 */
data class AddUserUiState(
    val dni: String = "",
    val dniError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val nombre: String = "",
    val nombreError: String? = null,
    val apellidos: String = "",
    val apellidosError: String? = null,
    val telefono: String = "",
    val telefonoError: String? = null,
    val tipoUsuario: TipoUsuario = TipoUsuario.FAMILIAR,
    val centroSeleccionado: Centro? = null,
    val centrosDisponibles: List<Centro> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isAdminApp: Boolean = true // Usar para limitar el tipo de usuarios que se pueden crear
) {
    val isFormValid: Boolean get() =
        dni.isNotBlank() && dniError == null &&
                email.isNotBlank() && emailError == null &&
                password.isNotBlank() && passwordError == null &&
                confirmPassword.isNotBlank() && confirmPasswordError == null &&
                nombre.isNotBlank() && nombreError == null &&
                apellidos.isNotBlank() && apellidosError == null &&
                telefono.isNotBlank() && telefonoError == null &&
                (tipoUsuario != TipoUsuario.PROFESOR && tipoUsuario != TipoUsuario.ADMIN_CENTRO || centroSeleccionado != null)
}

/**
 * ViewModel de muestra para el preview
 */
class PreviewAddUserViewModel : ViewModel() {
    private val _uiState = mutableStateOf(
        AddUserUiState(
            dni = "12345678A",
            email = "usuario@ejemplo.com",
            password = "contraseña",
            confirmPassword = "contraseña",
            nombre = "María",
            apellidos = "López García",
            telefono = "666777888",
            tipoUsuario = TipoUsuario.FAMILIAR,
            centrosDisponibles = listOf(
                Centro(id = "1", nombre = "Colegio San José"),
                Centro(id = "2", nombre = "Escuela Infantil Luna")
            ),
            isAdminApp = true
        )
    )

    val uiState: AddUserUiState get() = _uiState.value

    fun updateDni(dni: String) {
        _uiState.value = _uiState.value.copy(
            dni = dni,
            dniError = if (dni.isBlank()) "El DNI es obligatorio" else null
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = if (email.isBlank()) "El email es obligatorio" else null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = if (password.isBlank()) "La contraseña es obligatoria" else null
        )
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = if (confirmPassword != _uiState.value.password)
                "Las contraseñas no coinciden" else null
        )
    }

    fun updateNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(
            nombre = nombre,
            nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null
        )
    }

    fun updateApellidos(apellidos: String) {
        _uiState.value = _uiState.value.copy(
            apellidos = apellidos,
            apellidosError = if (apellidos.isBlank()) "Los apellidos son obligatorios" else null
        )
    }

    fun updateTelefono(telefono: String) {
        _uiState.value = _uiState.value.copy(
            telefono = telefono,
            telefonoError = if (telefono.isBlank()) "El teléfono es obligatorio" else null
        )
    }

    fun updateTipoUsuario(tipoUsuario: TipoUsuario) {
        _uiState.value = _uiState.value.copy(tipoUsuario = tipoUsuario)
    }

    fun updateCentroSeleccionado(centro: Centro?) {
        _uiState.value = _uiState.value.copy(centroSeleccionado = centro)
    }

    fun saveUser() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            success = true
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Factory para crear el ViewModel de preview
 */
class PreviewAddUserViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PreviewAddUserViewModel() as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    uiState: AddUserUiState,
    onUpdateDni: (String) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onUpdateConfirmPassword: (String) -> Unit,
    onUpdateNombre: (String) -> Unit,
    onUpdateApellidos: (String) -> Unit,
    onUpdateTelefono: (String) -> Unit,
    onUpdateTipoUsuario: (TipoUsuario) -> Unit,
    onUpdateCentroSeleccionado: (Centro?) -> Unit,
    onSaveUser: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Variables para UI de campos
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var showCentrosDropdown by remember { mutableStateOf(false) }

    // Calcular progreso del formulario
    val porcentajeCompletado = calcularPorcentajeCompletado(uiState)

    // Título basado en el tipo de usuario que se está creando
    val tipoUsuarioText = when (uiState.tipoUsuario) {
        TipoUsuario.ADMIN_APP -> "Administrador de Aplicación"
        TipoUsuario.ADMIN_CENTRO -> "Administrador de Centro"
        TipoUsuario.PROFESOR -> "Profesor"
        TipoUsuario.FAMILIAR -> "Familiar"
        else -> "Usuario"
    }

    // Color basado en el tipo de usuario
    val tipoColor = when (uiState.tipoUsuario) {
        TipoUsuario.ADMIN_APP -> AdminColor
        TipoUsuario.ADMIN_CENTRO -> CentroColor
        TipoUsuario.PROFESOR -> ProfesorColor
        TipoUsuario.FAMILIAR -> FamiliarColor
        else -> MaterialTheme.colorScheme.primary
    }

    // Manejo de mensajes de éxito y error
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Usuario guardado correctamente")
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir $tipoUsuarioText") },
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
                            onSaveUser()
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
                    containerColor = tipoColor,
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
                // Calcular el porcentaje de completado
                FormProgressIndicator(
                    currentStep = porcentajeCompletado.toInt(),
                    totalSteps = 100
                )
            }

            // Selección de tipo de usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tipo de Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Mostrar opciones según el tipo de administrador
                    val usuariosDisponibles = if (uiState.isAdminApp) {
                        // Admin App puede crear todos los tipos
                        listOf(
                            TipoUsuario.ADMIN_APP,
                            TipoUsuario.ADMIN_CENTRO,
                            TipoUsuario.PROFESOR,
                            TipoUsuario.FAMILIAR
                        )
                    } else {
                        // Admin Centro solo puede crear profesores y familiares
                        listOf(
                            TipoUsuario.PROFESOR,
                            TipoUsuario.FAMILIAR
                        )
                    }

                    Column {
                        usuariosDisponibles.forEach { tipo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (uiState.tipoUsuario == tipo),
                                        onClick = { onUpdateTipoUsuario(tipo) }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (uiState.tipoUsuario == tipo),
                                    onClick = { onUpdateTipoUsuario(tipo) }
                                )

                                Text(
                                    text = when (tipo) {
                                        TipoUsuario.ADMIN_APP -> "Administrador de Aplicación"
                                        TipoUsuario.ADMIN_CENTRO -> "Administrador de Centro"
                                        TipoUsuario.PROFESOR -> "Profesor"
                                        TipoUsuario.FAMILIAR -> "Familiar"
                                        else -> "Otro"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Si es profesor o admin centro, mostrar selector de centro
                    AnimatedVisibility(
                        visible = uiState.tipoUsuario == TipoUsuario.PROFESOR ||
                                uiState.tipoUsuario == TipoUsuario.ADMIN_CENTRO
                    ) {
                        Column {
                            Text(
                                text = "Centro Educativo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )

                            OutlinedTextField(
                                value = uiState.centroSeleccionado?.nombre ?: "",
                                onValueChange = { },
                                label = { Text("Seleccionar centro") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showCentrosDropdown = true }) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = "Seleccionar"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            DropdownMenu(
                                expanded = showCentrosDropdown,
                                onDismissRequest = { showCentrosDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                uiState.centrosDisponibles.forEach { centro ->
                                    DropdownMenuItem(
                                        text = { Text(centro.nombre) },
                                        onClick = {
                                            onUpdateCentroSeleccionado(centro)
                                            showCentrosDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Información personal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // DNI
                    OutlinedTextField(
                        value = uiState.dni,
                        onValueChange = onUpdateDni,
                        label = { Text("DNI") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (uiState.dniError != null)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = uiState.dniError != null,
                        supportingText = {
                            if (uiState.dniError != null) {
                                Text(text = uiState.dniError)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Nombre
                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = onUpdateNombre,
                        label = { Text("Nombre") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (uiState.nombreError != null)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = uiState.nombreError != null,
                        supportingText = {
                            if (uiState.nombreError != null) {
                                Text(text = uiState.nombreError)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Apellidos
                    OutlinedTextField(
                        value = uiState.apellidos,
                        onValueChange = onUpdateApellidos,
                        label = { Text("Apellidos") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (uiState.apellidosError != null)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = uiState.apellidosError != null,
                        supportingText = {
                            if (uiState.apellidosError != null) {
                                Text(text = uiState.apellidosError)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Teléfono
                    OutlinedTextField(
                        value = uiState.telefono,
                        onValueChange = onUpdateTelefono,
                        label = { Text("Teléfono") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = if (uiState.telefonoError != null)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = uiState.telefonoError != null,
                        supportingText = {
                            if (uiState.telefonoError != null) {
                                Text(text = uiState.telefonoError)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }

            // Información de acceso
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información de Acceso",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Email
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onUpdateEmail,
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (uiState.emailError != null)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = uiState.emailError != null,
                        supportingText = {
                            if (uiState.emailError != null) {
                                Text(text = uiState.emailError)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Contraseña
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onUpdatePassword,
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.passwordError != null)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = uiState.passwordError != null,
                        supportingText = {
                            if (uiState.passwordError != null) {
                                Text(text = uiState.passwordError)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Confirmar contraseña
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onUpdateConfirmPassword,
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.confirmPasswordError != null)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (uiState.isFormValid) {
                                    onSaveUser()
                                }
                            }
                        ),
                        isError = uiState.confirmPasswordError != null,
                        supportingText = {
                            if (uiState.confirmPasswordError != null) {
                                Text(text = uiState.confirmPasswordError)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }

            // Botón de guardar
            Button(
                onClick = {
                    keyboardController?.hide()
                    onSaveUser()
                },
                enabled = !uiState.isLoading && uiState.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tipoColor,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
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
                    text = "Guardar Usuario",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Mensaje informativo sobre el tipo de usuario
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información sobre ${tipoUsuarioText}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val infoText = when (uiState.tipoUsuario) {
                            TipoUsuario.ADMIN_APP -> "Los administradores de aplicación tienen control total sobre la plataforma, incluyendo la gestión de centros y usuarios."
                            TipoUsuario.ADMIN_CENTRO -> "Los administradores de centro pueden gestionar profesores y alumnos dentro de su centro educativo."
                            TipoUsuario.PROFESOR -> "Los profesores pueden gestionar las aulas asignadas, registrar actividades y comunicarse con las familias."
                            TipoUsuario.FAMILIAR -> "Los familiares pueden ver la información de sus hijos/as y comunicarse con los profesores."
                            else -> "Este tipo de usuario tiene permisos específicos en el sistema."
                        }

                        Text(
                            text = infoText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddUserScreenPreview() {
    val viewModel: PreviewAddUserViewModel = viewModel(factory = PreviewAddUserViewModelFactory())

    UmeEguneroTheme {
        Surface {
            AddUserScreen(
                uiState = viewModel.uiState,
                onUpdateDni = viewModel::updateDni,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onUpdateConfirmPassword = viewModel::updateConfirmPassword,
                onUpdateNombre = viewModel::updateNombre,
                onUpdateApellidos = viewModel::updateApellidos,
                onUpdateTelefono = viewModel::updateTelefono,
                onUpdateTipoUsuario = viewModel::updateTipoUsuario,
                onUpdateCentroSeleccionado = viewModel::updateCentroSeleccionado,
                onSaveUser = viewModel::saveUser,
                onClearError = viewModel::clearError,
                onNavigateBack = { }
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddUserScreenDarkPreview() {
    val viewModel: PreviewAddUserViewModel = viewModel(factory = PreviewAddUserViewModelFactory())

    UmeEguneroTheme(darkTheme = true) {
        Surface {
            AddUserScreen(
                uiState = viewModel.uiState,
                onUpdateDni = viewModel::updateDni,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onUpdateConfirmPassword = viewModel::updateConfirmPassword,
                onUpdateNombre = viewModel::updateNombre,
                onUpdateApellidos = viewModel::updateApellidos,
                onUpdateTelefono = viewModel::updateTelefono,
                onUpdateTipoUsuario = viewModel::updateTipoUsuario,
                onUpdateCentroSeleccionado = viewModel::updateCentroSeleccionado,
                onSaveUser = viewModel::saveUser,
                onClearError = viewModel::clearError,
                onNavigateBack = { }
            )
        }
    }
}

/**
 * Calcula el porcentaje de completado del formulario
 * @param uiState Estado actual del formulario
 * @return Valor entre 0 y 1 que representa el porcentaje de completado
 */
private fun calcularPorcentajeCompletado(uiState: AddUserUiState): Float {
    var fieldsCompleted = 0
    var totalFields = 7 // Campos obligatorios básicos
    
    // Verificar campos básicos
    if (uiState.dni.isNotBlank()) fieldsCompleted++
    if (uiState.email.isNotBlank()) fieldsCompleted++
    if (uiState.nombre.isNotBlank()) fieldsCompleted++
    if (uiState.apellidos.isNotBlank()) fieldsCompleted++
    if (uiState.telefono.isNotBlank()) fieldsCompleted++
    if (uiState.password.isNotBlank()) fieldsCompleted++
    if (uiState.confirmPassword.isNotBlank()) fieldsCompleted++
    
    // Para profesores y administradores de centro, verificar selección de centro
    if (uiState.tipoUsuario == TipoUsuario.PROFESOR || 
        uiState.tipoUsuario == TipoUsuario.ADMIN_CENTRO) {
        totalFields++
        if (uiState.centroSeleccionado != null) fieldsCompleted++
    }
    
    return fieldsCompleted.toFloat() / totalFields.toFloat()
}