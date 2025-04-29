package com.tfg.umeegunero.feature.common.users.screen

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notes
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserFormField
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserViewModel
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserUiState
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.getUserColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    navController: NavHostController,
    viewModel: AddUserViewModel = hiltViewModel(),
    isAdminApp: Boolean,
    tipoPreseleccionado: String?,
    centroIdInicial: String?,
    centroBloqueadoInicial: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()

    // Inicializar el ViewModel con los parámetros de navegación una sola vez
    LaunchedEffect(Unit) {
        viewModel.initialize(
            centroId = centroIdInicial,
            bloqueado = centroBloqueadoInicial,
            tipoUsuarioStr = tipoPreseleccionado,
            isAdminAppFlag = isAdminApp
        )
    }

    // Pasar la función onCursoSelected del viewModel directamente
    AddUserScreen(
        uiState = uiState,
        onUpdateDni = viewModel::updateDni,
        onUpdateEmail = viewModel::updateEmail,
        onUpdatePassword = viewModel::updatePassword,
        onUpdateConfirmPassword = viewModel::updateConfirmPassword,
        onUpdateNombre = viewModel::updateNombre,
        onUpdateApellidos = viewModel::updateApellidos,
        onUpdateTelefono = viewModel::updateTelefono,
        onUpdateTipoUsuario = viewModel::updateTipoUsuario,
        onUpdateCentroSeleccionado = viewModel::updateCentroSeleccionado,
        onCursoSelectedAlumno = viewModel::onCursoSelected,
        onUpdateClaseSeleccionada = viewModel::updateClaseSeleccionada,
        onUpdateFechaNacimiento = viewModel::updateFechaNacimiento,
        onSaveUser = viewModel::saveUser,
        onClearError = viewModel::clearError,
        onNavigateBack = { navController.popBackStack() },
        onAttemptSaveAndFocusError = viewModel::attemptSaveAndFocusError,
        onClearValidationAttemptTrigger = viewModel::clearValidationAttemptTrigger,
        onDismissSuccessDialog = viewModel::dismissSuccessDialog,
        viewModelRef = viewModel
    )
}

/**
 * Pantalla para crear o editar usuarios en el sistema
 *
 * Esta pantalla proporciona una interfaz completa para la gestión de usuarios,
 * adaptándose dinámicamente según el tipo de usuario seleccionado y mostrando
 * los campos relevantes para cada caso.
 *
 * Características:
 * - Formulario adaptativo según el tipo de usuario (Admin, Centro, Profesor, Familiar, Alumno)
 * - Validación en tiempo real de todos los campos
 * - Indicador visual de progreso del formulario
 * - Selección de centro, curso y clase para los tipos que lo requieren
 * - Campos adicionales específicos para alumnos (fecha de nacimiento)
 * - Animaciones suaves para una mejor experiencia de usuario
 * - Retroalimentación visual inmediata de errores
 *
 * @param uiState Estado actual de la interfaz de usuario
 * @param onUpdateDni Callback para actualizar el DNI
 * @param onUpdateEmail Callback para actualizar el email
 * @param onUpdatePassword Callback para actualizar la contraseña
 * @param onUpdateConfirmPassword Callback para confirmar la contraseña
 * @param onUpdateNombre Callback para actualizar el nombre
 * @param onUpdateApellidos Callback para actualizar los apellidos
 * @param onUpdateTelefono Callback para actualizar el teléfono
 * @param onUpdateTipoUsuario Callback para cambiar el tipo de usuario
 * @param onUpdateCentroSeleccionado Callback para seleccionar un centro
 * @param onCursoSelectedAlumno Callback para seleccionar un curso (para alumnos)
 * @param onUpdateClaseSeleccionada Callback para seleccionar una clase (para alumnos)
 * @param onUpdateFechaNacimiento Callback para actualizar fecha de nacimiento (para alumnos)
 * @param onSaveUser Callback para guardar el usuario
 * @param onClearError Callback para limpiar errores
 * @param onNavigateBack Callback para volver atrás
 * @param onAttemptSaveAndFocusError Callback para intentar guardar y enfocar el primer error
 * @param onClearValidationAttemptTrigger Callback para limpiar el indicador de intento de validación
 * @param onDismissSuccessDialog Callback para cerrar el diálogo de éxito
 * @param viewModelRef Referencia al ViewModel para llamadas internas
 */
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
    onUpdateCentroSeleccionado: (String) -> Unit,
    onCursoSelectedAlumno: (String) -> Unit,
    onUpdateClaseSeleccionada: (String) -> Unit,
    onUpdateFechaNacimiento: (String) -> Unit,
    onSaveUser: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit,
    onAttemptSaveAndFocusError: () -> Unit,
    onClearValidationAttemptTrigger: () -> Unit,
    onDismissSuccessDialog: () -> Unit,
    viewModelRef: ViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Focus Requesters para cada campo
    val dniFocusRequester = remember { FocusRequester() }
    val nombreFocusRequester = remember { FocusRequester() }
    val apellidosFocusRequester = remember { FocusRequester() }
    val telefonoFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val centroFocusRequester = remember { FocusRequester() } // Para el TextField del dropdown
    val fechaNacimientoFocusRequester = remember { FocusRequester() } // Para el TextField
    val cursoFocusRequester = remember { FocusRequester() } // Para el TextField del dropdown
    val claseFocusRequester = remember { FocusRequester() } // Para el TextField del dropdown

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
        TipoUsuario.ALUMNO -> "Alumno"
        else -> "Usuario"
    }

    // Color unificado según tipo de usuario
    val userColor = getUserColor(uiState.tipoUsuario)

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

    LaunchedEffect(uiState.validationAttemptFailed) {
        if (uiState.validationAttemptFailed) {
            val fieldToFocus = uiState.firstInvalidField
            Timber.d("Intento de validación fallido, primer campo inválido: $fieldToFocus")
            val focusRequester = when (fieldToFocus) {
                AddUserFormField.DNI -> dniFocusRequester
                AddUserFormField.NOMBRE -> nombreFocusRequester
                AddUserFormField.APELLIDOS -> apellidosFocusRequester
                AddUserFormField.TELEFONO -> telefonoFocusRequester
                AddUserFormField.EMAIL -> emailFocusRequester
                AddUserFormField.PASSWORD -> passwordFocusRequester
                AddUserFormField.CONFIRM_PASSWORD -> confirmPasswordFocusRequester
                AddUserFormField.CENTRO -> centroFocusRequester
                AddUserFormField.FECHA_NACIMIENTO -> fechaNacimientoFocusRequester
                AddUserFormField.CURSO -> cursoFocusRequester
                AddUserFormField.CLASE -> claseFocusRequester
                null -> null
            }

            if (focusRequester != null) {
                 scope.launch {
                    delay(100) // Pequeño delay
                    try {
                        focusRequester.requestFocus()
                        Timber.d("Foco solicitado para: $fieldToFocus")
                        // Intentar mostrar teclado puede ser inconsistente
                        // keyboardController?.show()
                    } catch (e: Exception) {
                        Timber.e(e, "Error al intentar enfocar campo: $fieldToFocus")
                    }
                 }
            }
            // Limpiar el trigger después de intentar enfocar
             onClearValidationAttemptTrigger()
        }
    }

    // Diálogo de éxito
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar tocando fuera */ },
            title = { Text("Éxito") },
            text = { Text("Usuario guardado correctamente.") },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            confirmButton = {
                TextButton(onClick = {
                    onDismissSuccessDialog()
                    onNavigateBack()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.isEditMode) "Editar $tipoUsuarioText" else "Nuevo $tipoUsuarioText",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Contenido principal con pantalla de carga superpuesta
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenedor principal con scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Indicador de progreso
                FormProgressIndicator(
                    currentStep = (porcentajeCompletado * 10).toInt() + 1,
                    totalSteps = 10,
                    progressColor = userColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
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

                        // Mostrar opciones según el tipo de administrador
                        val usuariosDisponibles = if (uiState.isAdminApp) {
                            // Admin App puede crear todos los tipos
                            listOf(
                                TipoUsuario.ADMIN_APP,
                                TipoUsuario.ADMIN_CENTRO,
                                TipoUsuario.PROFESOR,
                                TipoUsuario.FAMILIAR,
                                TipoUsuario.ALUMNO
                            )
                        } else {
                            // Admin Centro solo puede crear profesores, familiares y alumnos
                            listOf(
                                TipoUsuario.PROFESOR,
                                TipoUsuario.FAMILIAR,
                                TipoUsuario.ALUMNO
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
                                            TipoUsuario.ALUMNO -> "Alumno"
                                            else -> "Otro"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

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
                                        onUpdateCentroSeleccionado(centroId)
                                    },
                                    centros = uiState.centrosDisponibles.map { 
                                        it.nombre to it.id 
                                    },
                                    error = null,
                                    isLoading = uiState.isLoading,
                                    focusRequester = centroFocusRequester,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                            onValueChange = onUpdateDni,
                            label = { Text("DNI") },
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
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
                            modifier = Modifier.fillMaxWidth().focusRequester(dniFocusRequester),
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
                                keyboardType = KeyboardType.Text,
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
                            modifier = Modifier.fillMaxWidth().focusRequester(nombreFocusRequester),
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
                                keyboardType = KeyboardType.Text,
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
                            modifier = Modifier.fillMaxWidth().focusRequester(apellidosFocusRequester),
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
                            modifier = Modifier.fillMaxWidth().focusRequester(telefonoFocusRequester),
                            visualTransformation = PhoneNumberVisualTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }

                // Para alumnos, mostrar campos específicos
                AnimatedVisibility(
                    visible = uiState.tipoUsuario == TipoUsuario.ALUMNO
                ) {
                    AlumnoFields(
                        fechaNacimiento = uiState.fechaNacimiento,
                        fechaNacimientoError = uiState.fechaNacimientoError,
                        cursoSeleccionado = uiState.cursoSeleccionado,
                        cursosDisponibles = uiState.cursosDisponibles,
                        claseSeleccionada = uiState.claseSeleccionada,
                        clasesDisponibles = uiState.clasesDisponibles,
                        isLoading = uiState.isLoading,
                        onUpdateFechaNacimiento = onUpdateFechaNacimiento,
                        onCursoSelected = onCursoSelectedAlumno,
                        onUpdateClaseSeleccionada = onUpdateClaseSeleccionada,
                        fechaNacimientoFocusRequester = fechaNacimientoFocusRequester,
                        cursoFocusRequester = cursoFocusRequester,
                        claseFocusRequester = claseFocusRequester,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Credenciales (solo para usuarios excepto alumnos)
                AnimatedVisibility(
                    visible = uiState.tipoUsuario != TipoUsuario.ALUMNO
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
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp).padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Credenciales de Acceso",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

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
                                modifier = Modifier.fillMaxWidth().focusRequester(emailFocusRequester),
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
                                            imageVector = if (showPassword) 
                                                Icons.Default.VisibilityOff 
                                            else 
                                                Icons.Default.Visibility,
                                            contentDescription = if (showPassword)
                                                "Ocultar contraseña"
                                            else
                                                "Mostrar contraseña"
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) 
                                    VisualTransformation.None 
                                else 
                                    PasswordVisualTransformation(),
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
                                modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            // Confirmar contraseña
                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = onUpdateConfirmPassword,
                                label = { Text("Confirmar contraseña") },
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
                                            imageVector = if (showConfirmPassword) 
                                                Icons.Default.VisibilityOff 
                                            else 
                                                Icons.Default.Visibility,
                                            contentDescription = if (showConfirmPassword)
                                                "Ocultar contraseña"
                                            else
                                                "Mostrar contraseña"
                                        )
                                    }
                                },
                                visualTransformation = if (showConfirmPassword) 
                                    VisualTransformation.None 
                                else 
                                    PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { 
                                        keyboardController?.hide()
                                        if (uiState.isFormValid) {
                                            onSaveUser()
                                        } else {
                                            // Mostrar Snackbar si el formulario no es válido
                                            scope.launch { // Necesita un CoroutineScope
                                                snackbarHostState.showSnackbar(
                                                    message = "Por favor, complete todos los campos requeridos correctamente.",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                ),
                                isError = uiState.confirmPasswordError != null,
                                supportingText = {
                                    if (uiState.confirmPasswordError != null) {
                                        Text(text = uiState.confirmPasswordError)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().focusRequester(confirmPasswordFocusRequester),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                // Botón de guardar
                Button(
                    onClick = { 
                        keyboardController?.hide()
                        // Añadir comprobación de validez y feedback
                        if (uiState.isFormValid) {
                            onSaveUser() 
                        } else {
                            // Lanzar trigger para focus y mostrar snackbar genérico
                            onAttemptSaveAndFocusError()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Por favor, complete todos los campos requeridos correctamente.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = userColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isEditMode) "Actualizar" else "Guardar",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Indicador de carga general
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = userColor)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddUserScreenPreview() {
    val factory = remember { PreviewAddUserViewModelFactory() }
    val viewModel: PreviewAddUserViewModel = viewModel(factory = factory)
    val uiState = viewModel.uiState.copy(
        isCentroBloqueado = true,
        initialCentroId = "1",
        tipoUsuario = TipoUsuario.ALUMNO,
        centroSeleccionado = viewModel.uiState.centrosDisponibles.find { it.id == "1" }
    )

    UmeEguneroTheme {
        Surface {
            AddUserScreen(
                uiState = uiState,
                onUpdateDni = viewModel::updateDni,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onUpdateConfirmPassword = viewModel::updateConfirmPassword,
                onUpdateNombre = viewModel::updateNombre,
                onUpdateApellidos = viewModel::updateApellidos,
                onUpdateTelefono = viewModel::updateTelefono,
                onUpdateTipoUsuario = viewModel::updateTipoUsuario,
                onUpdateCentroSeleccionado = viewModel::updateCentroSeleccionado,
                onCursoSelectedAlumno = viewModel::updateCursoSeleccionado,
                onUpdateClaseSeleccionada = viewModel::updateClaseSeleccionada,
                onUpdateFechaNacimiento = viewModel::updateFechaNacimiento,
                onSaveUser = viewModel::saveUser,
                onClearError = viewModel::clearError,
                onNavigateBack = { },
                onAttemptSaveAndFocusError = { Timber.d("Preview: Attempt Save (no-op)") },
                onClearValidationAttemptTrigger = { Timber.d("Preview: Clear Trigger (no-op)") },
                onDismissSuccessDialog = { Timber.d("Preview: Dismiss Success (no-op)") },
                viewModelRef = viewModel
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddUserScreenDarkPreview() {
    val viewModel: PreviewAddUserViewModel = viewModel(factory = PreviewAddUserViewModelFactory())
    val uiState = viewModel.uiState

    UmeEguneroTheme(darkTheme = true) {
        Surface {
            AddUserScreen(
                uiState = uiState,
                onUpdateDni = viewModel::updateDni,
                onUpdateEmail = viewModel::updateEmail,
                onUpdatePassword = viewModel::updatePassword,
                onUpdateConfirmPassword = viewModel::updateConfirmPassword,
                onUpdateNombre = viewModel::updateNombre,
                onUpdateApellidos = viewModel::updateApellidos,
                onUpdateTelefono = viewModel::updateTelefono,
                onUpdateTipoUsuario = viewModel::updateTipoUsuario,
                onUpdateCentroSeleccionado = viewModel::updateCentroSeleccionado,
                onCursoSelectedAlumno = viewModel::updateCursoSeleccionado,
                onUpdateClaseSeleccionada = viewModel::updateClaseSeleccionada,
                onUpdateFechaNacimiento = viewModel::updateFechaNacimiento,
                onSaveUser = viewModel::saveUser,
                onClearError = viewModel::clearError,
                onNavigateBack = { },
                onAttemptSaveAndFocusError = { Timber.d("Preview: Attempt Save (no-op)") },
                onClearValidationAttemptTrigger = { Timber.d("Preview: Clear Trigger (no-op)") },
                onDismissSuccessDialog = { Timber.d("Preview: Dismiss Success (no-op)") },
                viewModelRef = viewModel
            )
        }
    }
}

/**
 * Calcula el porcentaje de completado del formulario basado en los campos rellenados.
 *
 * @param uiState Estado actual de la interfaz de usuario
 * @return Porcentaje de completado entre 0.0 y 1.0
 */
private fun calcularPorcentajeCompletado(uiState: AddUserUiState): Float {
    var fieldsCompleted = 0
    var totalFields = 0
    
    // Campos básicos (obligatorios para todos los tipos de usuario)
    totalFields += 3 // DNI, nombre, apellidos
    if (uiState.dni.isNotBlank()) fieldsCompleted++
    if (uiState.nombre.isNotBlank()) fieldsCompleted++
    if (uiState.apellidos.isNotBlank()) fieldsCompleted++
    
    // Campo de teléfono (obligatorio pero separado para claridad)
    totalFields++
    if (uiState.telefono.isNotBlank()) fieldsCompleted++
    
    // Para todos excepto alumnos, credenciales de acceso
    if (uiState.tipoUsuario != TipoUsuario.ALUMNO) {
        totalFields += 3 // Email, contraseña, confirmación
        if (uiState.email.isNotBlank()) fieldsCompleted++
        if (uiState.password.isNotBlank()) fieldsCompleted++
        if (uiState.confirmPassword.isNotBlank()) fieldsCompleted++
    }
    
    // Para profesores y administradores de centro, centro educativo
    if (uiState.tipoUsuario == TipoUsuario.PROFESOR || 
        uiState.tipoUsuario == TipoUsuario.ADMIN_CENTRO) {
        totalFields++
        if (uiState.centroSeleccionado != null) fieldsCompleted++
    }
    
    // Para alumnos, campos adicionales
    if (uiState.tipoUsuario == TipoUsuario.ALUMNO) {
        totalFields += 4 // Centro, fecha nacimiento, curso, clase
        if (uiState.centroSeleccionado != null) fieldsCompleted++
        if (uiState.fechaNacimiento.isNotBlank()) fieldsCompleted++
        if (uiState.cursoSeleccionado != null) fieldsCompleted++
        if (uiState.claseSeleccionada != null) fieldsCompleted++
    }
    
    // Cálculo del porcentaje (evitar división por cero)
    return if (totalFields > 0) {
        fieldsCompleted.toFloat() / totalFields.toFloat()
    } else {
        0f
    }
}

/**
 * Campo para la fecha de nacimiento
 */
@Composable
fun FechaNacimientoField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = millis
                        }
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val year = calendar.get(Calendar.YEAR)
                        val formattedDate = "$day/$month/$year"
                        onValueChange(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = "Fecha de Nacimiento") },
        placeholder = { Text(text = "dd/mm/aaaa") },
        leadingIcon = { 
            Icon(
                imageVector = Icons.Default.Cake,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Seleccionar Fecha"
                )
            }
        },
        isError = error != null,
        supportingText = error?.let { { Text(text = it) } },
        readOnly = true,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusRequester.requestFocus() }),
        modifier = modifier.focusRequester(focusRequester)
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isLoading) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = centroSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text("Centro") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            enabled = !isLoading,
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
    cursos: List<String>,
    error: String?,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isLoading) expanded = it },
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
            enabled = !isLoading,
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
                cursos.forEach { curso ->
                    DropdownMenuItem(
                        text = { Text(curso) },
                        onClick = {
                            onCursoSelected(curso)
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
    clases: List<Clase>,
    selectedClase: Clase?,
    onClaseSelected: (String) -> Unit,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = "Clase",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else if (clases.isEmpty()) {
            Text(
                text = "Primero selecciona un curso",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            OutlinedTextField(
                value = selectedClase?.nombre ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Clase") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar clase")
                    }
                }
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
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
    isLoading: Boolean,
    onUpdateFechaNacimiento: (String) -> Unit,
    onCursoSelected: (String) -> Unit,
    onUpdateClaseSeleccionada: (String) -> Unit,
    fechaNacimientoFocusRequester: FocusRequester,
    cursoFocusRequester: FocusRequester,
    claseFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
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
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Información Académica",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            FechaNacimientoField(
                value = fechaNacimiento,
                onValueChange = onUpdateFechaNacimiento,
                error = fechaNacimientoError,
                focusRequester = fechaNacimientoFocusRequester,
                modifier = Modifier.fillMaxWidth()
            )
            
            CursoDropdown(
                cursoSeleccionado = cursoSeleccionado?.id ?: "",
                onCursoSelected = onCursoSelected,
                cursos = cursosDisponibles.map { it.id },
                error = null,
                isLoading = isLoading,
                focusRequester = cursoFocusRequester,
                modifier = Modifier.fillMaxWidth()
            )
            
            ClaseDropdown(
                clases = clasesDisponibles,
                selectedClase = claseSeleccionada,
                onClaseSelected = onUpdateClaseSeleccionada,
                isLoading = isLoading,
                focusRequester = claseFocusRequester,
                modifier = Modifier.fillMaxWidth()
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