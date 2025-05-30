package com.tfg.umeegunero.feature.common.users.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserFormField
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserUiState
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserViewModel
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.util.getUserColor
import java.util.Calendar

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

    fun updateCentroSeleccionado(centroId: String) {
        val centro = _uiState.value.centrosDisponibles.find { it.id == centroId }
        _uiState.value = _uiState.value.copy(centroSeleccionado = centro)
    }

    fun updateCursoSeleccionado(cursoId: String) {
        val curso = _uiState.value.cursosDisponibles.find { it.id == cursoId }
        _uiState.value = _uiState.value.copy(cursoSeleccionado = curso)
    }

    fun updateClaseSeleccionada(claseId: String) {
        val clase = _uiState.value.clasesDisponibles.find { it.id == claseId }
        _uiState.value = _uiState.value.copy(claseSeleccionada = clase)
    }

    fun updateFechaNacimiento(fechaNacimiento: String) {
        _uiState.value = _uiState.value.copy(
            fechaNacimiento = fechaNacimiento,
            fechaNacimientoError = if (fechaNacimiento.isBlank()) "La fecha de nacimiento es obligatoria" else null
        )
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

    fun attemptSaveAndFocusError() {
        // Implementation needed
    }

    fun clearValidationAttemptTrigger() {
        // Implementation needed
    }

    fun dismissSuccessDialog() {
        // Implementation needed
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

/**
 * Pantalla de creación/edición de usuarios principal
 */
@Composable
fun AddUserScreen(
    navController: NavHostController,
    centroIdParam: String? = null,
    bloqueadoParam: Boolean = false,
    tipoUsuarioParam: String? = null,
    dniParam: String? = null,
    isAdminAppParam: Boolean = false,
    viewModel: AddUserViewModel = hiltViewModel()
) {
    // Inicializar el ViewModel
    LaunchedEffect(centroIdParam, bloqueadoParam, tipoUsuarioParam, dniParam) {
        viewModel.initialize(centroIdParam, bloqueadoParam, tipoUsuarioParam, isAdminAppParam)
        
        // Si tenemos un DNI, es modo edición
        if (!dniParam.isNullOrEmpty()) {
            viewModel.loadUser(dniParam)
        }
    }
    
    // Componente interno que maneja el contenido real
    AddUserScreenContent(
        navController = navController,
        viewModel = viewModel
    )
}

/**
 * Contenido de la pantalla de creación/edición de usuarios
 * Esta composable función se encarga de mostrar el contenido real de la pantalla
 */
@Composable
fun AddUserScreenContent(
    navController: NavHostController,
    viewModel: AddUserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Variables para el estado del formulario
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    
    // Focus requesters para cada campo
    val dniFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val nombreFocusRequester = remember { FocusRequester() }
    val apellidosFocusRequester = remember { FocusRequester() }
    val telefonoFocusRequester = remember { FocusRequester() }
    val centroFocusRequester = remember { FocusRequester() }
    val fechaNacimientoFocusRequester = remember { FocusRequester() }
    val cursoFocusRequester = remember { FocusRequester() }
    val claseFocusRequester = remember { FocusRequester() }
    
    // Estado para controlar la visibilidad de la contraseña
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Calcular porcentaje de completado del formulario (simple)
    val porcentajeCompletado = remember(uiState) {
        var camposRequeridos = 0
        var camposCompletados = 0
        
        // Campos comunes para todos
        camposRequeridos += 3 // DNI, nombre, apellidos
        if (uiState.dni.isNotBlank()) camposCompletados++
        if (uiState.nombre.isNotBlank()) camposCompletados++
        if (uiState.apellidos.isNotBlank()) camposCompletados++
        
        // Si no es alumno, necesita email y contraseña
        if (uiState.tipoUsuario != TipoUsuario.ALUMNO) {
            camposRequeridos += 2 // Email, contraseña
            if (uiState.email.isNotBlank()) camposCompletados++
            if (uiState.password.isNotBlank() && uiState.confirmPassword.isNotBlank()) camposCompletados++
        }
        
        // Si es profesor, admin centro o alumno, necesita centro
        if (uiState.tipoUsuario == TipoUsuario.PROFESOR || 
            uiState.tipoUsuario == TipoUsuario.ADMIN_CENTRO ||
            uiState.tipoUsuario == TipoUsuario.ALUMNO) {
            camposRequeridos++
            if (uiState.centroSeleccionado != null) camposCompletados++
        }
        
        // Si es alumno, necesita fecha de nacimiento, curso y clase
        if (uiState.tipoUsuario == TipoUsuario.ALUMNO) {
            camposRequeridos += 3 // Fecha, curso, clase
            if (uiState.fechaNacimiento.isNotBlank()) camposCompletados++
            if (uiState.cursoSeleccionado != null) camposCompletados++
            if (uiState.claseSeleccionada != null) camposCompletados++
        }
        
        if (camposRequeridos > 0) {
            camposCompletados.toFloat() / camposRequeridos.toFloat()
        } else {
            0f
        }
    }
    
    // Calcular el color del usuario basado en su tipo
    val userColor = getUserColor(uiState.tipoUsuario)
    
    // Manejar el efecto para mostrar error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            // Limpiar error después de mostrarlo
            viewModel.clearError()
        }
    }

    // Manejar el éxito
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            // Mostrar mensaje y navegar inmediatamente sin esperar
            snackbarHostState.showSnackbar(
                message = if (uiState.isEditMode) "Usuario actualizado correctamente" else "Usuario creado correctamente",
                duration = SnackbarDuration.Short
            )
            // Volver atrás inmediatamente sin delay
            navController.popBackStack()
        }
    }
    
    // Manejar el scroll a campo con error
    LaunchedEffect(uiState.firstInvalidField, uiState.validationAttemptFailed) {
        if (uiState.validationAttemptFailed && uiState.firstInvalidField != null) {
            when (uiState.firstInvalidField) {
                AddUserFormField.DNI -> dniFocusRequester.requestFocus()
                AddUserFormField.EMAIL -> emailFocusRequester.requestFocus()
                AddUserFormField.PASSWORD -> passwordFocusRequester.requestFocus()
                AddUserFormField.CONFIRM_PASSWORD -> confirmPasswordFocusRequester.requestFocus()
                AddUserFormField.NOMBRE -> nombreFocusRequester.requestFocus()
                AddUserFormField.APELLIDOS -> apellidosFocusRequester.requestFocus()
                AddUserFormField.TELEFONO -> telefonoFocusRequester.requestFocus()
                AddUserFormField.CENTRO -> centroFocusRequester.requestFocus()
                AddUserFormField.FECHA_NACIMIENTO -> fechaNacimientoFocusRequester.requestFocus()
                AddUserFormField.CURSO -> cursoFocusRequester.requestFocus()
                AddUserFormField.CLASE -> claseFocusRequester.requestFocus()
                else -> {} // Otros campos no tienen focus requester
            }
            
            // Limpiar el trigger
            viewModel.clearValidationAttemptTrigger()
        }
    }
    
    // Scaffold con TopAppBar
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.isEditMode) "Editar Usuario" else "Añadir Usuario",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = userColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Button(
                onClick = { viewModel.saveUser() },
                modifier = Modifier
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = userColor
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    ) { paddingValues ->
        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Barra de progreso
            FormProgressIndicator(
                currentStep = (porcentajeCompletado * 10).toInt() + 1,
                totalSteps = 10,
                progressColor = userColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Padding horizontal y vertical
            )
            
            // Contenedor principal con scroll
            Column(
                modifier = Modifier
                    .weight(1f) // Para que ocupe el espacio restante
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tipo de usuario
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).padding(end = 8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tipo de Usuario",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Selector de tipo de usuario usando getUserTypes()
                        TipoUsuarioSelector(
                            tipoActual = uiState.tipoUsuario,
                            tiposBloqueados = uiState.isTipoUsuarioBloqueado,
                            isEditMode = uiState.isEditMode,
                            viewModel = viewModel,
                            onUpdateTipoUsuario = { viewModel.updateTipoUsuario(it) }
                        )

                        // Si es profesor, admin centro o alumno, mostrar selector de centro
                        AnimatedVisibility(
                            visible = uiState.tipoUsuario == TipoUsuario.PROFESOR ||
                                    uiState.tipoUsuario == TipoUsuario.ADMIN_CENTRO ||
                                    uiState.tipoUsuario == TipoUsuario.ALUMNO
                        ) {
                            Column {
                                Text(
                                    text = "Centro Educativo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )

                                CentroDropdown(
                                    centroSeleccionado = uiState.centroSeleccionado?.nombre ?: "",
                                    onCentroSelected = { centroId ->
                                        viewModel.updateCentroSeleccionado(centroId)
                                    },
                                    centros = uiState.centrosDisponibles.map { 
                                        it.nombre to it.id 
                                    },
                                    error = null,
                                    isLoading = uiState.isLoading,
                                    focusRequester = centroFocusRequester,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isCentroSeleccionadoBloqueado
                                )
                            }
                        }
                    }
                }
                
                // DNI, Nombre, Apellidos
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).padding(end = 8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Información Personal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // DNI
                        OutlinedTextField(
                            value = uiState.dni,
                            onValueChange = { viewModel.updateDni(it) },
                            label = { Text("DNI/NIE") },
                            isError = uiState.dniError != null,
                            supportingText = {
                                if (uiState.dniError != null) {
                                    Text(text = uiState.dniError!!)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { nombreFocusRequester.requestFocus() }
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = if (uiState.dniError != null) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(dniFocusRequester)
                        )

                        // Nombre
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = { viewModel.updateNombre(it) },
                            label = { Text("Nombre") },
                            isError = uiState.nombreError != null,
                            supportingText = {
                                if (uiState.nombreError != null) {
                                    Text(text = uiState.nombreError!!)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { apellidosFocusRequester.requestFocus() }
                            ),
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
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(nombreFocusRequester)
                        )

                        // Apellidos
                        OutlinedTextField(
                            value = uiState.apellidos,
                            onValueChange = { viewModel.updateApellidos(it) },
                            label = { Text("Apellidos") },
                            isError = uiState.apellidosError != null,
                            supportingText = {
                                if (uiState.apellidosError != null) {
                                    Text(text = uiState.apellidosError!!)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { telefonoFocusRequester.requestFocus() }
                            ),
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
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(apellidosFocusRequester)
                        )

                        // Teléfono
                        OutlinedTextField(
                            value = uiState.telefono,
                            onValueChange = { viewModel.updateTelefono(it) },
                            label = { Text("Teléfono") },
                            isError = uiState.telefonoError != null,
                            supportingText = {
                                if (uiState.telefonoError != null) {
                                    Text(text = uiState.telefonoError!!)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { 
                                    if (uiState.tipoUsuario != TipoUsuario.ALUMNO) {
                                        emailFocusRequester.requestFocus() 
                                    } else {
                                        fechaNacimientoFocusRequester.requestFocus()
                                    }
                                }
                            ),
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
                            visualTransformation = PhoneNumberVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(telefonoFocusRequester)
                        )
                    }
                }

                // Mostrar campos específicos para no alumnos (email, contraseña)
                AnimatedVisibility(
                    visible = uiState.tipoUsuario != TipoUsuario.ALUMNO,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp).padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Cuenta",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Email
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { viewModel.updateEmail(it) },
                                label = { Text("Email") },
                                isError = uiState.emailError != null,
                                supportingText = {
                                    if (uiState.emailError != null) {
                                        Text(text = uiState.emailError!!)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { passwordFocusRequester.requestFocus() }
                                ),
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
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(emailFocusRequester)
                            )

                            // Contraseña
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { viewModel.updatePassword(it) },
                                label = { Text("Contraseña") },
                                isError = uiState.passwordError != null,
                                supportingText = {
                                    if (uiState.passwordError != null) {
                                        Text(text = uiState.passwordError!!)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                                ),
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
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(passwordFocusRequester)
                            )

                            // Confirmar contraseña
                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = { viewModel.updateConfirmPassword(it) },
                                label = { Text("Confirmar Contraseña") },
                                isError = uiState.confirmPasswordError != null,
                                supportingText = {
                                    if (uiState.confirmPasswordError != null) {
                                        Text(text = uiState.confirmPasswordError!!)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { keyboardController?.hide() }
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (uiState.confirmPasswordError != null) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(confirmPasswordFocusRequester)
                            )
                        }
                    }
                }

                // Mostrar campos específicos para alumnos
                AnimatedVisibility(
                    visible = uiState.tipoUsuario == TipoUsuario.ALUMNO,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    AlumnoFields(
                        fechaNacimiento = uiState.fechaNacimiento,
                        fechaNacimientoError = uiState.fechaNacimientoError,
                        cursoSeleccionado = uiState.cursoSeleccionado,
                        cursosDisponibles = uiState.cursosDisponibles,
                        claseSeleccionada = uiState.claseSeleccionada,
                        clasesDisponibles = uiState.clasesDisponibles,
                        numeroSS = uiState.numeroSS ?: "",
                        numeroSSError = uiState.numeroSSError,
                        condicionesMedicas = uiState.condicionesMedicas ?: "",
                        condicionesMedicasError = uiState.condicionesMedicasError,
                        alergias = uiState.alergias ?: "",
                        medicacion = uiState.medicacion ?: "",
                        necesidadesEspeciales = uiState.necesidadesEspeciales ?: "",
                        observaciones = uiState.observaciones ?: "",
                        observacionesMedicas = uiState.observacionesMedicas ?: "",
                        isLoading = uiState.isLoading,
                        isEditMode = uiState.isEditMode,
                        isCentroBloqueado = uiState.isCentroBloqueado,
                        onUpdateFechaNacimiento = { viewModel.updateFechaNacimiento(it) },
                        onCursoSelected = { viewModel.updateCursoSeleccionado(it) },
                        onUpdateClaseSeleccionada = { viewModel.updateClaseSeleccionada(it) },
                        onUpdateNumeroSS = { viewModel.updateNumeroSS(it) },
                        onUpdateCondicionesMedicas = { viewModel.updateCondicionesMedicas(it) },
                        onUpdateAlergias = { viewModel.updateAlergias(it) },
                        onUpdateMedicacion = { viewModel.updateMedicacion(it) },
                        onUpdateNecesidadesEspeciales = { viewModel.updateNecesidadesEspeciales(it) },
                        onUpdateObservaciones = { viewModel.updateObservaciones(it) },
                        onUpdateObservacionesMedicas = { viewModel.updateObservacionesMedicas(it) },
                        fechaNacimientoFocusRequester = fechaNacimientoFocusRequester,
                        cursoFocusRequester = cursoFocusRequester,
                        claseFocusRequester = claseFocusRequester,
                        numeroSSFocusRequester = FocusRequester(),
                        condicionesMedicasFocusRequester = FocusRequester(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(64.dp)) // Espacio para el botón flotante
            }
        }
    }
}

/**
 * Componente para seleccionar el tipo de usuario
 */
@Composable
fun TipoUsuarioSelector(
    tipoActual: TipoUsuario,
    tiposBloqueados: Boolean,
    isEditMode: Boolean,
    viewModel: AddUserViewModel,
    onUpdateTipoUsuario: (TipoUsuario) -> Unit
) {
    // Obtener los tipos de usuario disponibles del ViewModel
    val tiposDisponibles = viewModel.getUserTypes()
    
    Column {
        tiposDisponibles.forEach { tipoUsuario ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (tipoActual == tipoUsuario),
                        onClick = { 
                            if (!tiposBloqueados) {
                                onUpdateTipoUsuario(tipoUsuario) 
                            }
                        },
                        enabled = !tiposBloqueados
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (tipoActual == tipoUsuario),
                    onClick = { 
                        if (!tiposBloqueados) {
                            onUpdateTipoUsuario(tipoUsuario) 
                        }
                    },
                    enabled = !tiposBloqueados
                )

                Text(
                    text = when (tipoUsuario) {
                        TipoUsuario.ADMIN_APP -> "Administrador de Aplicación"
                        TipoUsuario.ADMIN_CENTRO -> "Administrador de Centro"
                        TipoUsuario.PROFESOR -> "Profesor"
                        TipoUsuario.FAMILIAR -> "Familiar"
                        TipoUsuario.ALUMNO -> "Alumno"
                        else -> "Otro"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp),
                    color = if (tiposBloqueados && tipoActual != tipoUsuario) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) 
                        else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Mensaje informativo cuando el tipo está bloqueado
        if (tiposBloqueados) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (tipoActual == TipoUsuario.PROFESOR && !isEditMode)
                        "Tipo de usuario preseleccionado como profesor"
                    else if (tipoActual == TipoUsuario.ALUMNO && !isEditMode)
                        "Tipo de usuario preseleccionado como alumno"
                    else
                        "No se permite cambiar el tipo de usuario en modo edición",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Campo para la fecha de nacimiento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaNacimientoField(
    value: String,
    onFechaNacimientoChanged: (String) -> Unit,
    error: String?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Creamos el DatePickerDialog con Material3
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { 
                showDatePicker = false 
            },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
                        val year = calendar.get(Calendar.YEAR)
                        val formattedDate = "$day/$month/$year"
                        onFechaNacimientoChanged(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDatePicker = false 
                }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onFechaNacimientoChanged,
        label = { Text(text = "Fecha de Nacimiento") },
        placeholder = { Text(text = "dd/mm/aaaa") },
        supportingText = {
            if (error != null) {
                Text(text = error)
            } else {
                Text("Presione el icono de calendario para seleccionar la fecha")
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = if (error != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Seleccionar fecha",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        isError = error != null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
        singleLine = true,
        readOnly = true
    )
}

/**
 * Dropdown para seleccionar un centro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroDropdown(
    centroSeleccionado: String,
    onCentroSelected: (String) -> Unit,
    centros: List<Pair<String, String>>, // Pair de (nombre, id)
    error: String?,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (!isLoading && enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = centroSeleccionado.ifEmpty { "Seleccionar centro educativo" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Centro Educativo") },
            trailingIcon = {
                if (enabled) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Centro bloqueado",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = if (!enabled) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.primary
                )
            },
            colors = if (!enabled) {
                OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
            } else {
                ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            },
            enabled = !isLoading && enabled,
            isError = error != null,
            supportingText = if (!enabled && centroSeleccionado.isNotEmpty()) {
                { Text("Centro educativo bloqueado", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) }
            } else {
                error?.let { { Text(text = it) } }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            } else if (centros.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay centros disponibles") },
                    onClick = { expanded = false }
                )
            } else {
                centros.forEach { (nombre, id) ->
                    DropdownMenuItem(
                        text = { Text(nombre) },
                        onClick = {
                            onCentroSelected(id)
                            expanded = false
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

/**
 * Dropdown para seleccionar un curso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoDropdown(
    cursoSeleccionado: String,
    onCursoSelected: (String) -> Unit,
    cursos: List<Pair<String, String>>, // Pair de (nombre, id)
    error: String?,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (!isLoading && enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = cursoSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text("Curso") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            enabled = !isLoading && enabled,
            isError = error != null,
            supportingText = error?.let { { Text(text = it) } },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }
            } else if (cursos.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay cursos disponibles") },
                    onClick = { expanded = false }
                )
            } else {
                cursos.forEach { (nombre, id) ->
                    DropdownMenuItem(
                        text = { Text(nombre) },
                        onClick = {
                            onCursoSelected(id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Componente para seleccionar clase (para alumnos)
 */
@Composable
fun ClaseDropdown(
    claseSeleccionada: String,
    clases: List<Clase>,
    cursoSeleccionado: Curso?,
    onClaseSelected: (String) -> Unit,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val cursoHaSidoSeleccionado = cursoSeleccionado != null
    
    Column(modifier = modifier) {
        Text(
            text = "Clase",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            OutlinedTextField(
                value = claseSeleccionada,
                onValueChange = { },
                readOnly = true,
                label = { Text("Clase") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { if (cursoHaSidoSeleccionado && enabled) expanded = true }) {
                        Icon(
                            Icons.Default.ArrowDropDown, 
                            contentDescription = "Seleccionar clase",
                            tint = if (cursoHaSidoSeleccionado && enabled) 
                                   MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                },
                enabled = cursoHaSidoSeleccionado && enabled,
                supportingText = {
                    if (!cursoHaSidoSeleccionado) {
                        Text("Primero seleccione un curso")
                    }
                }
            )
            
            DropdownMenu(
                expanded = expanded && cursoHaSidoSeleccionado && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp, 
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                clases.forEach { clase ->
                    DropdownMenuItem(
                        text = { Text(clase.nombre) },
                        onClick = {
                            onClaseSelected(clase.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Sección de campos específicos para alumnos
 */
@Composable
fun AlumnoFields(
    fechaNacimiento: String,
    fechaNacimientoError: String?,
    cursoSeleccionado: Curso?,
    cursosDisponibles: List<Curso>,
    claseSeleccionada: Clase?,
    clasesDisponibles: List<Clase>,
    numeroSS: String = "",
    numeroSSError: String? = null, 
    condicionesMedicas: String = "",
    condicionesMedicasError: String? = null,
    alergias: String = "",
    medicacion: String = "",
    necesidadesEspeciales: String = "",
    observaciones: String = "",
    observacionesMedicas: String = "",
    isLoading: Boolean,
    isEditMode: Boolean,
    isCentroBloqueado: Boolean,
    onUpdateFechaNacimiento: (String) -> Unit,
    onCursoSelected: (String) -> Unit,
    onUpdateClaseSeleccionada: (String) -> Unit,
    onUpdateNumeroSS: (String) -> Unit,
    onUpdateCondicionesMedicas: (String) -> Unit,
    onUpdateAlergias: (String) -> Unit,
    onUpdateMedicacion: (String) -> Unit,
    onUpdateNecesidadesEspeciales: (String) -> Unit,
    onUpdateObservaciones: (String) -> Unit,
    onUpdateObservacionesMedicas: (String) -> Unit,
    fechaNacimientoFocusRequester: FocusRequester,
    cursoFocusRequester: FocusRequester,
    claseFocusRequester: FocusRequester,
    numeroSSFocusRequester: FocusRequester = FocusRequester(),
    condicionesMedicasFocusRequester: FocusRequester = FocusRequester(),
    modifier: Modifier = Modifier
) {
    // Estado para controlar si el campo de Número SS tiene el foco
    var isNumeroSSFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current // Para quitar el foco

    Card(
        modifier = modifier,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp).padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Información del Alumno",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Fecha de nacimiento
            FechaNacimientoField(
                value = fechaNacimiento,
                onFechaNacimientoChanged = { nuevaFecha ->
                    onUpdateFechaNacimiento(nuevaFecha)
                },
                error = fechaNacimientoError,
                focusRequester = fechaNacimientoFocusRequester
            )

            // Curso
            val cursoDisplayValue = cursoSeleccionado?.nombre ?: stringResource(id = R.string.add_user_placeholder_curso)
            CursoDropdown(
                cursoSeleccionado = cursoDisplayValue,
                onCursoSelected = { cursoId -> onCursoSelected(cursoId) },
                cursos = cursosDisponibles.map { it.nombre to it.id },
                error = null,
                isLoading = isLoading,
                focusRequester = cursoFocusRequester,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !(isEditMode && isCentroBloqueado)
            )

            // Clase
            val claseDisplayValue = claseSeleccionada?.nombre ?: stringResource(id = R.string.add_user_placeholder_clase)
            val cursoRealmenteSeleccionado = cursoSeleccionado != null
            ClaseDropdown(
                claseSeleccionada = claseDisplayValue,
                clases = clasesDisponibles,
                cursoSeleccionado = cursoSeleccionado,
                onClaseSelected = { claseId ->
                    onUpdateClaseSeleccionada(claseId)
                },
                isLoading = isLoading,
                focusRequester = claseFocusRequester,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && cursoRealmenteSeleccionado && !(isEditMode && isCentroBloqueado)
            )
            
            // Número de Seguridad Social
            OutlinedTextField(
                value = numeroSS,
                onValueChange = { newValue ->
                    val newDigitsOnly = newValue.filter { it.isDigit() }.take(12)
                    onUpdateNumeroSS(newDigitsOnly)
                },
                label = { Text("Número de Seguridad Social") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (numeroSSError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (numeroSS.length == 12) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { condicionesMedicasFocusRequester.requestFocus() },
                    onDone = { focusManager.clearFocus() }
                ),
                isError = numeroSSError != null,
                supportingText = {
                    if (numeroSSError != null) {
                        Text(text = numeroSSError)
                    } else {
                        Row {
                            Text(if (numeroSS.length == 12) "Nº SS completo " else "Introduce los 12 dígitos ")
                            Text("(Opcional)", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(numeroSSFocusRequester),
                singleLine = true,
                visualTransformation = VisualTransformation.None
            )

            // Condiciones médicas
            OutlinedTextField(
                value = condicionesMedicas,
                onValueChange = onUpdateCondicionesMedicas,
                label = { Text("Condiciones Médicas") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = if (condicionesMedicasError != null)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = condicionesMedicasError != null,
                supportingText = {
                    if (condicionesMedicasError != null) {
                        Text(text = condicionesMedicasError)
                    } else {
                        Column {
                            Text("Enfermedades, discapacidades u otras condiciones médicas relevantes")
                        Text("(Opcional)", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(condicionesMedicasFocusRequester),
                minLines = 2,
                maxLines = 3
            )

            // Alergias
            OutlinedTextField(
                value = alergias,
                onValueChange = onUpdateAlergias,
                label = { Text("Alergias") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    Column {
                        Text("Alergias a alimentos, materiales, etc. (separadas por comas)")
                        Text("(Opcional)", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Medicación
            OutlinedTextField(
                value = medicacion,
                onValueChange = onUpdateMedicacion,
                label = { Text("Medicación") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    Column {
                        Text("Medicamentos que toma regularmente (separados por comas)")
                            Text("(Opcional)", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Necesidades Especiales
            OutlinedTextField(
                value = necesidadesEspeciales,
                onValueChange = onUpdateNecesidadesEspeciales,
                label = { Text("Necesidades Especiales") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    Column {
                        Text("Necesidades educativas especiales o adaptaciones requeridas")
                        Text("(Opcional)", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Observaciones Médicas
            OutlinedTextField(
                value = observacionesMedicas,
                onValueChange = onUpdateObservacionesMedicas,
                label = { Text("Observaciones Médicas") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    Column {
                        Text("Otras observaciones relevantes sobre la salud")
                        Text("(Opcional)", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Observaciones Generales
            OutlinedTextField(
                value = observaciones,
                onValueChange = onUpdateObservaciones,
                label = { Text("Observaciones Generales") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { cursoFocusRequester.requestFocus() }
                ),
                supportingText = {
                    Column {
                        Text("Información adicional relevante")
                        Text("(Opcional)", color = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
        }
    }
}

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Limpiar no dígitos para lógica interna
        val digitsOnly = text.text.filter { it.isDigit() }
        val formattedNumber = buildString {
            for (i in digitsOnly.indices) {
                append(digitsOnly[i])
                // Añadir espacio después del 3er y 6º dígito
                if (i == 2 || i == 5) {
                    if (i != digitsOnly.lastIndex) { // No añadir espacio al final
                        append(' ')
                    }
                }
            }
        }.take(11) // Limitar a 9 dígitos + 2 espacios = 11 caracteres

        // Mapeo de offsets para que el cursor se mueva correctamente
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val spacesBefore = when {
                    offset <= 3 -> 0
                    offset <= 6 -> 1
                    else -> 2
                }
                return (offset + spacesBefore).coerceAtMost(formattedNumber.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val spacesBefore = when {
                    offset <= 3 -> 0
                    offset <= 7 -> 1 // 3 dig + 1 espacio
                    else -> 2 // 6 dig + 2 espacios
                }
                return (offset - spacesBefore).coerceAtLeast(0)
            }
        }

        return TransformedText(AnnotatedString(formattedNumber), offsetMapping)
    }
}

/**
 * Transformación visual para formato de número de Seguridad Social: XX/XXXXXXXX/XX
 */
class SeguridadSocialVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }.take(12)

        val formattedText = buildString {
            digitsOnly.forEachIndexed { index, char ->
                append(char)
                if ((index == 1 && digitsOnly.length > 2) || (index == 9 && digitsOnly.length > 10)) {
                    append('/')
                }
            }
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(originalOffset: Int): Int {
                // `originalOffset` es la posición en `digitsOnly`
                // Si hay 'n' dígitos, ¿cuántas barras hay ANTES de esos 'n' dígitos?
                var slashesBefore = 0
                if (originalOffset > 2) slashesBefore++ // Primera barra después del 2º dígito
                if (originalOffset > 10) slashesBefore++ // Segunda barra después del 10º dígito

                // La posición transformada es la original + las barras que van antes.
                return (originalOffset + slashesBefore).coerceAtMost(formattedText.length)
            }

            override fun transformedToOriginal(transformedOffset: Int): Int {
                // `transformedOffset` es la posición en `formattedText` (con barras)
                // Contamos cuántos dígitos hay hasta esa posición en el texto formateado.
                // Ese es el `originalOffset` efectivo.
                return formattedText.take(transformedOffset).count { it.isDigit() }
                                 .coerceAtMost(digitsOnly.length)
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetTranslator)
    }
}

// Extensión para encontrar la N-ésima ocurrencia de un carácter que cumple un predicado
private fun String.indexOfNth(predicate: (Char) -> Boolean, indexSelector: (Int) -> Boolean): Int? {
    var count = 0
    this.forEachIndexed { i, c ->
        if (predicate(c)) {
            if (indexSelector(count)) {
                return i
            }
            count++
        }
    }
    return null
}

/**
 * Componente que muestra los requisitos de contraseña
 */
@Composable
private fun PasswordRequirementsCard(
    password: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Requisitos de contraseña:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        PasswordRequirementItem(
            text = "Al menos 6 caracteres",
            isMet = password.length >= 6,
            icon = Icons.Default.TextFields
        )
        
        PasswordRequirementItem(
            text = "Al menos una letra",
            isMet = password.any { it.isLetter() },
            icon = Icons.Default.TextFormat
        )
        
        PasswordRequirementItem(
            text = "Al menos un número",
            isMet = password.any { it.isDigit() },
            icon = Icons.Default.Tag
        )
        
        PasswordRequirementItem(
            text = "Al menos un carácter especial",
            isMet = password.any { !it.isLetterOrDigit() },
            icon = Icons.Default.Lock
        )
    }
}

/**
 * Ítem individual que muestra un requisito de contraseña
 */
@Composable
private fun PasswordRequirementItem(
    text: String,
    isMet: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isMet) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else icon,
            contentDescription = null,
            tint = if (isMet) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMet) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Normal
        )
    }
}