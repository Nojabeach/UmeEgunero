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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserViewModel
import com.tfg.umeegunero.feature.common.users.viewmodel.AddUserUiState
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.AdminColor
import com.tfg.umeegunero.ui.theme.AlumnoColor
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

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
    navController: NavController,
    viewModel: AddUserViewModel = hiltViewModel(),
    isAdminApp: Boolean = true,
    tipoPreseleccionado: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Inicializar el tipo de usuario según el parámetro tipoPreseleccionado
    LaunchedEffect(tipoPreseleccionado) {
        tipoPreseleccionado?.let { tipo ->
            val tipoUsuario = when (tipo.lowercase()) {
                "admin" -> TipoUsuario.ADMIN_APP
                "centro" -> TipoUsuario.ADMIN_CENTRO
                "profesor" -> TipoUsuario.PROFESOR
                "familiar" -> TipoUsuario.FAMILIAR
                "alumno" -> TipoUsuario.ALUMNO
                else -> TipoUsuario.FAMILIAR
            }
            viewModel.updateTipoUsuario(tipoUsuario)
        }
    }
    
    // Inicializar flag de admin app
    LaunchedEffect(isAdminApp) {
        viewModel.setIsAdminApp(isAdminApp)
    }
    
    // Cargar centros disponibles
    LaunchedEffect(Unit) {
        viewModel.loadCentros()
    }
    
    // UI completa de la pantalla
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
        onUpdateCursoSeleccionado = viewModel::updateCursoSeleccionado,
        onUpdateClaseSeleccionada = viewModel::updateClaseSeleccionada,
        onUpdateFechaNacimiento = viewModel::updateFechaNacimiento,
        onSaveUser = viewModel::saveUser,
        onClearError = viewModel::clearError,
        onNavigateBack = { navController.popBackStack() }
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
 * @param onUpdateCursoSeleccionado Callback para seleccionar un curso (para alumnos)
 * @param onUpdateClaseSeleccionada Callback para seleccionar una clase (para alumnos)
 * @param onUpdateFechaNacimiento Callback para actualizar fecha de nacimiento (para alumnos)
 * @param onSaveUser Callback para guardar el usuario
 * @param onClearError Callback para limpiar errores
 * @param onNavigateBack Callback para volver atrás
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
    onUpdateCursoSeleccionado: (String) -> Unit,
    onUpdateClaseSeleccionada: (String) -> Unit,
    onUpdateFechaNacimiento: (String) -> Unit,
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
        TipoUsuario.ALUMNO -> "Alumno"
        else -> "Usuario"
    }

    // Color basado en el tipo de usuario
    val tipoColor = when (uiState.tipoUsuario) {
        TipoUsuario.ADMIN_APP -> AdminColor
        TipoUsuario.ADMIN_CENTRO -> CentroColor
        TipoUsuario.PROFESOR -> ProfesorColor
        TipoUsuario.FAMILIAR -> FamiliarColor
        TipoUsuario.ALUMNO -> AlumnoColor
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
                actions = {
                    // Botón de guardar
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            onSaveUser()
                        },
                        enabled = uiState.isFormValid && !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = if (uiState.isFormValid && !uiState.isLoading) 1f else 0.5f
                            )
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
                    progressColor = tipoColor,
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
                                                onUpdateCentroSeleccionado(centro.id)
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
                        onUpdateCursoSeleccionado = onUpdateCursoSeleccionado,
                        onUpdateClaseSeleccionada = onUpdateClaseSeleccionada,
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
                            Text(
                                text = "Credenciales de Acceso",
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
                                modifier = Modifier.fillMaxWidth(),
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
                }

                // Botón de guardar
                Button(
                    onClick = { 
                        keyboardController?.hide()
                        onSaveUser() 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tipoColor,
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
                    CircularProgressIndicator(color = tipoColor)
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
                onUpdateCursoSeleccionado = viewModel::updateCursoSeleccionado,
                onUpdateClaseSeleccionada = viewModel::updateClaseSeleccionada,
                onUpdateFechaNacimiento = viewModel::updateFechaNacimiento,
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
                onUpdateCursoSeleccionado = viewModel::updateCursoSeleccionado,
                onUpdateClaseSeleccionada = viewModel::updateClaseSeleccionada,
                onUpdateFechaNacimiento = viewModel::updateFechaNacimiento,
                onSaveUser = viewModel::saveUser,
                onClearError = viewModel::clearError,
                onNavigateBack = { }
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
 * Componente para la fecha de nacimiento (solo para alumnos)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaNacimientoField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    // Configuración del selector de fecha
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            onValueChange(dateFormatter.format(date))
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
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
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Fecha de nacimiento") },
            isError = error != null,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Seleccionar fecha"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Componente para seleccionar curso (para alumnos)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoDropdown(
    cursos: List<Curso>,
    selectedCurso: Curso?,
    onCursoSelected: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Registrar para depuración
    LaunchedEffect(cursos) {
        Timber.d("CursoDropdown: ${cursos.size} cursos disponibles")
        cursos.forEach { curso ->
            Timber.d("   - Curso: ${curso.nombre} (ID: ${curso.id}, CentroID: ${curso.centroId})")
        }
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Curso",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            OutlinedTextField(
                value = selectedCurso?.nombre ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Selecciona un curso") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown, 
                            contentDescription = "Seleccionar curso"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                cursos.forEach { curso ->
                    DropdownMenuItem(
                        text = { Text(curso.nombre) },
                        onClick = {
                            onCursoSelected(curso.id)
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
 * Componente para seleccionar clase (para alumnos)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaseDropdown(
    clases: List<Clase>,
    selectedClase: Clase?,
    onClaseSelected: (String) -> Unit,
    isLoading: Boolean,
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
                label = { Text("Selecciona una clase") },
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown, 
                            contentDescription = "Seleccionar clase"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
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
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Group,
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
    onUpdateCursoSeleccionado: (String) -> Unit,
    onUpdateClaseSeleccionada: (String) -> Unit,
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
            Text(
                text = "Información Académica",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            FechaNacimientoField(
                value = fechaNacimiento,
                onValueChange = onUpdateFechaNacimiento,
                error = fechaNacimientoError,
                modifier = Modifier.fillMaxWidth()
            )
            
            CursoDropdown(
                cursos = cursosDisponibles,
                selectedCurso = cursoSeleccionado,
                onCursoSelected = onUpdateCursoSeleccionado,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            ClaseDropdown(
                clases = clasesDisponibles,
                selectedClase = claseSeleccionada,
                onClaseSelected = onUpdateClaseSeleccionada,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}