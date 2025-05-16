package com.tfg.umeegunero.feature.common.perfil.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.feature.common.perfil.viewmodel.PerfilViewModel
import com.tfg.umeegunero.feature.common.perfil.viewmodel.PerfilUiState
import com.tfg.umeegunero.ui.theme.*
import com.tfg.umeegunero.util.getUserColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector
import com.tfg.umeegunero.ui.components.UserAvatar
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import timber.log.Timber

/**
 * Pantalla que muestra el perfil del usuario con diseño moderno Material 3
 * Permite visualizar y editar la información del perfil, incluida la ubicación
 * con latitud y longitud, además de visualizar un mapa de Google Maps
 * 
 * El diseño sigue las directrices de Material 3 y adopta el estilo visual de los
 * dashboards de la aplicación para una experiencia coherente
 * 
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 * @param viewModel ViewModel que contiene la lógica de negocio
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = hiltViewModel(),
    navController: NavController,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editMode by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    
    // Variable para controlar animaciones
    var showContent by remember { mutableStateOf(false) }
    
    // Obtener color específico del tipo de usuario unificado
    val userColor = remember(uiState.usuario) {
        val tipoUsuario = uiState.usuario?.perfiles?.firstOrNull()?.tipo
        getUserColor(tipoUsuario)
    }
    
    // Cargar perfil al inicio
    LaunchedEffect(Unit) {
        viewModel.cargarUsuario()
        showContent = true
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            scope.launch {
                snackbarHostState.showSnackbar(errorMsg)
                viewModel.clearError()
            }
        }
    }
    
    LaunchedEffect(uiState.success) {
        uiState.success?.let { successMsg ->
            scope.launch {
                snackbarHostState.showSnackbar(successMsg)
                viewModel.clearMensaje()
            }
        }
    }
    
    // Efecto para cargar automáticamente la ciudad al completar el código postal
    LaunchedEffect(uiState.direccionCP) {
        if (uiState.direccionCP.length == 5) {
            viewModel.obtenerCiudadPorCP(uiState.direccionCP)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Mi Perfil",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (!editMode) {
                        IconButton(onClick = { editMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            editMode = false
                            viewModel.guardarCambios()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar cambios"
                            )
                        }
                        
                        IconButton(onClick = {
                            editMode = false
                            viewModel.cancelarEdicion()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = userColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mostrar indicador de carga mientras se obtienen los datos
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center),
                    color = userColor
                )
            } else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    ),
                    exit = fadeOut()
                ) {
                    PerfilContent(
                        uiState = uiState,
                        editMode = editMode,
                        userColor = userColor,
                        onChangePassword = { showChangePasswordDialog = true },
                        onLogout = { showLogoutConfirmDialog = true },
                        onValueChange = { field, value ->
                            when (field) {
                                "nombre" -> viewModel.actualizarNombre(value)
                                "apellidos" -> viewModel.actualizarApellidos(value)
                                "telefono" -> viewModel.actualizarTelefono(value)
                                "direccionCalle" -> viewModel.actualizarDireccionCalle(value)
                                "direccionNumero" -> viewModel.actualizarDireccionNumero(value)
                                "direccionPiso" -> viewModel.actualizarDireccionPiso(value)
                                "direccionCP" -> viewModel.actualizarDireccionCP(value)
                                "direccionCiudad" -> viewModel.actualizarDireccionCiudad(value)
                                "direccionProvincia" -> viewModel.actualizarDireccionProvincia(value)
                                "latitud" -> viewModel.actualizarLatitud(value)
                                "longitud" -> viewModel.actualizarLongitud(value)
                            }
                        },
                        onGetCoordinates = {
                            viewModel.obtenerCoordenadasDeDireccion()
                        },
                        onAvatarChange = { uri ->
                            viewModel.subirAvatar(uri)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    )
                }
            }
        }
    }
    
    // Mostrar diálogos condicionales
    MostrarDialogos(
        showChangePasswordDialog = showChangePasswordDialog,
        onChangePasswordDismiss = { showChangePasswordDialog = false },
        onChangePassword = { current, new ->
            viewModel.cambiarContrasena(current, new)
            showChangePasswordDialog = false
        },
        showLogoutConfirmDialog = showLogoutConfirmDialog,
        onLogoutDismiss = { showLogoutConfirmDialog = false },
        onLogoutConfirm = {
            viewModel.cerrarSesion()
            showLogoutConfirmDialog = false
            // Navegar a la pantalla de inicio
            navController.navigate("welcome") {
                popUpTo(0) { inclusive = true }
            }
        },
        userColor = userColor
    )
}

/**
 * Muestra los diálogos correspondientes a las acciones
 * 
 * @param showChangePasswordDialog Si se debe mostrar el diálogo de cambio de contraseña
 * @param onChangePasswordDismiss Acción al cancelar el cambio de contraseña
 * @param onChangePassword Acción al confirmar el cambio de contraseña
 * @param showLogoutConfirmDialog Si se debe mostrar el diálogo de confirmación de cierre de sesión
 * @param onLogoutDismiss Acción al cancelar el cierre de sesión
 * @param onLogoutConfirm Acción al confirmar el cierre de sesión
 * @param userColor Color correspondiente al tipo de usuario
 */
@Composable
private fun MostrarDialogos(
    showChangePasswordDialog: Boolean,
    onChangePasswordDismiss: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    showLogoutConfirmDialog: Boolean,
    onLogoutDismiss: () -> Unit,
    onLogoutConfirm: () -> Unit,
    userColor: Color
) {
    // Diálogo para cambiar contraseña
    if (showChangePasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = onChangePasswordDismiss,
            icon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            title = { Text("Cambiar contraseña") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Contraseña actual") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null
                            )
                        }
                    )
                    
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        }
                    )
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            passwordError = "Las contraseñas no coinciden"
                            return@Button
                        }
                        onChangePassword(currentPassword, newPassword)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = userColor
                    )
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onChangePasswordDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para confirmar cierre de sesión
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = onLogoutDismiss,
            icon = { 
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp, 
                    contentDescription = null
                ) 
            },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = onLogoutConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onLogoutDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Campo para mostrar y editar información del perfil con diseño Material 3
 * 
 * @param icon Icono para mostrar junto al campo
 * @param label Etiqueta del campo
 * @param value Valor actual del campo
 * @param onValueChange Función a llamar cuando cambia el valor
 * @param editable Si el campo es editable o no
 * @param keyboardType Tipo de teclado a mostrar
 * @param helperText Texto de ayuda opcional
 */
@Composable
fun ProfileField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    editable: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    helperText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Campo editable o texto
        if (editable) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                supportingText = helperText?.let { { Text(it) } },
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = value.ifEmpty { "-" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (helperText != null) {
                        Text(
                            text = helperText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
    }
}

/**
 * Contenido principal de la pantalla de perfil
 * 
 * @param uiState Estado actual de la UI
 * @param editMode Si estamos en modo edición
 * @param userColor Color correspondiente al tipo de usuario
 * @param onChangePassword Acción al solicitar cambio de contraseña
 * @param onLogout Acción al solicitar cierre de sesión
 * @param onValueChange Acción al cambiar valores
 * @param onGetCoordinates Acción para obtener coordenadas
 * @param onAvatarChange Acción para cambiar el avatar
 * @param modifier Modificador para personalizar el layout
 */
@Composable
private fun PerfilContent(
    uiState: PerfilUiState,
    editMode: Boolean,
    userColor: Color,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onValueChange: (String, String) -> Unit,
    onGetCoordinates: () -> Unit,
    onAvatarChange: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de perfil con avatar y datos básicos
        PerfilHeaderCard(
            uiState = uiState,
            userColor = userColor,
            editMode = editMode,
            onAvatarChange = onAvatarChange,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Sección de información personal
        InformacionPersonalCard(
            uiState = uiState,
            editMode = editMode,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Sección de dirección con mapa
        DireccionCard(
            uiState = uiState,
            editMode = editMode,
            onValueChange = onValueChange,
            onGetCoordinates = onGetCoordinates,
            userColor = userColor,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Sección de acceso a la cuenta
        AccesoCard(
            uiState = uiState,
            onChangePassword = onChangePassword,
            onLogout = onLogout,
            userColor = userColor,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Cabecera de perfil con avatar y datos básicos
 */
@Composable
private fun PerfilHeaderCard(
    uiState: PerfilUiState,
    userColor: Color,
    editMode: Boolean,
    onAvatarChange: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tipoDisplay = uiState.usuario?.perfiles?.firstOrNull()?.tipo?.let { tipo ->
        when (tipo) {
            TipoUsuario.ADMIN_APP -> "Administrador del sistema"
            TipoUsuario.ADMIN_CENTRO -> "Administrador de centro"
            TipoUsuario.PROFESOR -> "Profesor"
            TipoUsuario.FAMILIAR -> "Familiar"
            TipoUsuario.ALUMNO -> "Alumno"
            TipoUsuario.DESCONOCIDO -> "Usuario"
            else -> "Usuario"
        }
    } ?: "Usuario"
    
    // Lanzador para seleccionar imagen
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAvatarChange(it) }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            userColor,
                            userColor.copy(alpha = 0.8f)
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-50).dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(4.dp, Color.White, CircleShape)
            ) {
                // Determinar si el usuario es administrador explícitamente
                val esAdmin = uiState.usuario?.perfiles?.any { 
                    it.tipo == TipoUsuario.ADMIN_APP 
                } == true
                
                // Registrar en log para depuración
                LaunchedEffect(esAdmin) {
                    Timber.d("PerfilScreen - Usuario es administrador: $esAdmin")
                    Timber.d("PerfilScreen - Avatar URL: ${uiState.usuario?.avatarUrl}")
                    Timber.d("PerfilScreen - Nombre completo: ${uiState.nombre} ${uiState.apellidos}")
                }
                
                UserAvatar(
                    imageUrl = uiState.usuario?.avatarUrl,
                    userName = "${uiState.nombre} ${uiState.apellidos}",
                    size = 100.dp,
                    borderWidth = 0.dp,
                    isAdmin = esAdmin
                )
                
                // Botón para cambiar avatar (solo en modo edición)
                if (editMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar imagen de perfil",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Nombre completo
            Text(
                text = "${uiState.nombre} ${uiState.apellidos}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Email
            Text(
                text = uiState.usuario?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Chip de tipo de usuario
            Surface(
                shape = RoundedCornerShape(50),
                color = userColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, userColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tipoUsuario = uiState.usuario?.perfiles?.firstOrNull()?.tipo
                    Icon(
                        imageVector = when (tipoUsuario) {
                            TipoUsuario.ADMIN_APP -> Icons.Default.AdminPanelSettings
                            TipoUsuario.ADMIN_CENTRO -> Icons.Default.Business
                            TipoUsuario.PROFESOR -> Icons.Default.School
                            TipoUsuario.FAMILIAR -> Icons.Default.People
                            TipoUsuario.ALUMNO -> Icons.Default.Face
                            TipoUsuario.DESCONOCIDO -> Icons.Default.Person
                            else -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = userColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = tipoDisplay,
                        color = userColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Tarjeta de información personal
 */
@Composable
private fun InformacionPersonalCard(
    uiState: PerfilUiState,
    editMode: Boolean,
    onValueChange: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de sección con icono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Información personal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Campos de información personal
            ProfileField(
                icon = Icons.Default.Person,
                label = "Nombre",
                value = uiState.nombre,
                onValueChange = { onValueChange("nombre", it) },
                editable = editMode
            )
            
            ProfileField(
                icon = Icons.Default.Person,
                label = "Apellidos",
                value = uiState.apellidos,
                onValueChange = { onValueChange("apellidos", it) },
                editable = editMode
            )
            
            ProfileField(
                icon = Icons.Default.Email,
                label = "Email",
                value = uiState.usuario?.email ?: "",
                onValueChange = { },
                editable = false
            )
            
            ProfileField(
                icon = Icons.Default.CreditCard,
                label = "DNI",
                value = uiState.usuario?.dni ?: "",
                onValueChange = { },
                editable = false
            )
            
            ProfileField(
                icon = Icons.Default.Phone,
                label = "Teléfono",
                value = uiState.telefono,
                onValueChange = { onValueChange("telefono", it) },
                editable = editMode,
                keyboardType = KeyboardType.Phone
            )
        }
    }
}

/**
 * Tarjeta de dirección con mapa
 */
@Composable
private fun DireccionCard(
    uiState: PerfilUiState,
    editMode: Boolean,
    onValueChange: (String, String) -> Unit,
    onGetCoordinates: () -> Unit,
    userColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de sección con icono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dirección",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Campos de dirección
            ProfileField(
                icon = Icons.Default.Home,
                label = "Calle",
                value = uiState.direccionCalle,
                onValueChange = { onValueChange("direccionCalle", it) },
                editable = editMode
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ProfileField(
                        icon = Icons.Default.Tag,
                        label = "Número",
                        value = uiState.direccionNumero,
                        onValueChange = { onValueChange("direccionNumero", it) },
                        editable = editMode,
                        keyboardType = KeyboardType.Number
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    ProfileField(
                        icon = Icons.Default.LocationOn,
                        label = "Código Postal",
                        value = uiState.direccionCP,
                        onValueChange = { onValueChange("direccionCP", it) },
                        editable = editMode,
                        keyboardType = KeyboardType.Number,
                        helperText = if (uiState.loadingCiudad) "Buscando ciudad..." else null
                    )
                }
            }
            
            ProfileField(
                icon = Icons.Default.LocationCity,
                label = "Ciudad",
                value = uiState.direccionCiudad,
                onValueChange = { onValueChange("direccionCiudad", it) },
                editable = editMode
            )
            
            ProfileField(
                icon = Icons.Default.Map,
                label = "Provincia",
                value = uiState.direccionProvincia,
                onValueChange = { onValueChange("direccionProvincia", it) },
                editable = editMode
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ProfileField(
                        icon = Icons.Default.MyLocation,
                        label = "Latitud",
                        value = uiState.latitud,
                        onValueChange = { onValueChange("latitud", it) },
                        editable = editMode,
                        keyboardType = KeyboardType.Decimal
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    ProfileField(
                        icon = Icons.Default.MyLocation,
                        label = "Longitud",
                        value = uiState.longitud,
                        onValueChange = { onValueChange("longitud", it) },
                        editable = editMode,
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mapa de Google Maps (se muestra si hay coordenadas válidas)
            if (uiState.latitud.isNotEmpty() && uiState.longitud.isNotEmpty()) {
                // Título de la sección de mapa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ubicación en mapa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Imagen del mapa usando la API estática de Google Maps
                val context = LocalContext.current
                val mapUrl = remember(uiState.latitud, uiState.longitud) {
                    "https://maps.googleapis.com/maps/api/staticmap" +
                    "?center=${uiState.latitud},${uiState.longitud}" +
                    "&zoom=15&size=600x300&scale=2" +
                    "&markers=color:red|${uiState.latitud},${uiState.longitud}" +
                    "&key=YOUR_API_KEY" // Aquí iría la clave de API real
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mapUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ubicación en mapa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón para obtener coordenadas (solo en modo edición)
            if (editMode) {
                ElevatedButton(
                    onClick = onGetCoordinates,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = userColor
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Obtener coordenadas de la dirección",
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de acceso a la cuenta
 */
@Composable
private fun AccesoCard(
    uiState: PerfilUiState,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    userColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de sección con icono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Información de acceso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Fecha de registro y último acceso
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
            val fechaRegistro = uiState.usuario?.fechaRegistro?.toDate()?.let { fecha -> formatter.format(fecha) } ?: "-"
            val ultimoAcceso = uiState.usuario?.ultimoAcceso?.toDate()?.let { fecha -> formatter.format(fecha) } ?: "-"
            
            ProfileField(
                icon = Icons.Default.DateRange,
                label = "Fecha de registro",
                value = fechaRegistro,
                onValueChange = { },
                editable = false
            )
            
            ProfileField(
                icon = Icons.Default.AccessTime,
                label = "Último acceso",
                value = ultimoAcceso,
                onValueChange = { },
                editable = false
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acceso
            ElevatedButton(
                onClick = onChangePassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = userColor.copy(alpha = 0.1f),
                    contentColor = userColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Cambiar contraseña",
                    color = Color.White
                )
            }
            
            ElevatedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Cerrar sesión",
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilScreenPreview() {
    UmeEguneroTheme {
        PerfilScreen(
            navController = rememberNavController()
        )
    }
} 