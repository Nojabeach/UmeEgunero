package com.tfg.umeegunero.feature.common.perfil.screen

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.common.perfil.viewmodel.PerfilViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla que muestra el perfil del usuario
 * Permite visualizar y editar la información del perfil, incluida la ubicación
 * con latitud y longitud, además de visualizar un mapa de Google Maps
 * 
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 * @param viewModel ViewModel que contiene la lógica de negocio
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editMode by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
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
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (!editMode) {
                        IconButton(onClick = { editMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar perfil",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            editMode = false
                            viewModel.guardarCambios()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar cambios",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        IconButton(onClick = {
                            editMode = false
                            viewModel.cancelarEdicion()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
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
                        .align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar y nombre
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.nombre.firstOrNull()?.toString()?.uppercase() ?: "U",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tipo de usuario
                    val tipoDisplay = uiState.usuario?.perfiles?.firstOrNull()?.tipo?.let { tipo ->
                        when (tipo) {
                            TipoUsuario.ADMIN_APP -> "Administrador del sistema"
                            TipoUsuario.ADMIN_CENTRO -> "Administrador de centro"
                            TipoUsuario.PROFESOR -> "Profesor"
                            TipoUsuario.FAMILIAR -> "Familiar"
                            TipoUsuario.ALUMNO -> "Alumno"
                            TipoUsuario.DESCONOCIDO -> "Usuario"
                        }
                    } ?: "Usuario"
                    
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = tipoDisplay,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Información de perfil
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Información personal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Nombre
                            ProfileField(
                                icon = Icons.Default.Person,
                                label = "Nombre",
                                value = uiState.nombre,
                                onValueChange = { viewModel.actualizarNombre(it) },
                                editable = editMode
                            )
                            
                            // Apellidos
                            ProfileField(
                                icon = Icons.Default.Person,
                                label = "Apellidos",
                                value = uiState.apellidos,
                                onValueChange = { viewModel.actualizarApellidos(it) },
                                editable = editMode
                            )
                            
                            // Email (no editable)
                            ProfileField(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = uiState.usuario?.email ?: "",
                                onValueChange = { },
                                editable = false
                            )
                            
                            // DNI (no editable)
                            ProfileField(
                                icon = Icons.Default.CreditCard,
                                label = "DNI",
                                value = uiState.usuario?.dni ?: "",
                                onValueChange = { },
                                editable = false
                            )
                            
                            // Teléfono
                            ProfileField(
                                icon = Icons.Default.Phone,
                                label = "Teléfono",
                                value = uiState.telefono,
                                onValueChange = { viewModel.actualizarTelefono(it) },
                                editable = editMode,
                                keyboardType = KeyboardType.Phone
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Dirección
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Dirección",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Calle
                            ProfileField(
                                icon = Icons.Default.Home,
                                label = "Calle",
                                value = uiState.direccionCalle,
                                onValueChange = { viewModel.actualizarDireccionCalle(it) },
                                editable = editMode
                            )
                            
                            // Número
                            ProfileField(
                                icon = Icons.Default.Tag,
                                label = "Número",
                                value = uiState.direccionNumero,
                                onValueChange = { viewModel.actualizarDireccionNumero(it) },
                                editable = editMode,
                                keyboardType = KeyboardType.Number
                            )
                            
                            // Código Postal
                            ProfileField(
                                icon = Icons.Default.LocationOn,
                                label = "Código Postal",
                                value = uiState.direccionCP,
                                onValueChange = { viewModel.actualizarDireccionCP(it) },
                                editable = editMode,
                                keyboardType = KeyboardType.Number,
                                helperText = if (uiState.loadingCiudad) "Buscando ciudad..." else null
                            )
                            
                            // Ciudad (se rellena automáticamente con el CP)
                            ProfileField(
                                icon = Icons.Default.LocationCity,
                                label = "Ciudad",
                                value = uiState.direccionCiudad,
                                onValueChange = { viewModel.actualizarDireccionCiudad(it) },
                                editable = editMode
                            )
                            
                            // Provincia
                            ProfileField(
                                icon = Icons.Default.Map,
                                label = "Provincia",
                                value = uiState.direccionProvincia,
                                onValueChange = { viewModel.actualizarDireccionProvincia(it) },
                                editable = editMode
                            )
                            
                            // Latitud
                            ProfileField(
                                icon = Icons.Default.MyLocation,
                                label = "Latitud",
                                value = uiState.latitud,
                                onValueChange = { viewModel.actualizarLatitud(it) },
                                editable = editMode,
                                keyboardType = KeyboardType.Decimal
                            )
                            
                            // Longitud
                            ProfileField(
                                icon = Icons.Default.MyLocation,
                                label = "Longitud",
                                value = uiState.longitud,
                                onValueChange = { viewModel.actualizarLongitud(it) },
                                editable = editMode,
                                keyboardType = KeyboardType.Decimal
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Mapa de Google Maps (se muestra si hay coordenadas válidas)
                            if (uiState.latitud.isNotEmpty() && uiState.longitud.isNotEmpty()) {
                                Text(
                                    text = "Ubicación en mapa",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
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
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
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
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Botón para obtener coordenadas actuales (en modo edición)
                                if (editMode) {
                                    Button(
                                        onClick = { viewModel.obtenerCoordenadasDeDireccion() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GpsFixed,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text("Obtener coordenadas de la dirección")
                                    }
                                }
                            } else if (editMode) {
                                // Botón para obtener coordenadas cuando no hay datos
                                Button(
                                    onClick = { viewModel.obtenerCoordenadasDeDireccion() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text("Obtener coordenadas de la dirección")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Información de acceso
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Información de acceso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Fecha de registro
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
                            val fechaRegistro = uiState.usuario?.fechaRegistro?.toDate()?.let { formatter.format(it) } ?: "-"
                            
                            ProfileField(
                                icon = Icons.Default.DateRange,
                                label = "Fecha de registro",
                                value = fechaRegistro,
                                onValueChange = { },
                                editable = false
                            )
                            
                            // Último acceso
                            val ultimoAcceso = uiState.usuario?.ultimoAcceso?.toDate()?.let { formatter.format(it) } ?: "-"
                            
                            ProfileField(
                                icon = Icons.Default.AccessTime,
                                label = "Último acceso",
                                value = ultimoAcceso,
                                onValueChange = { },
                                editable = false
                            )
                            
                            // Cambiar contraseña
                            Button(
                                onClick = { showChangePasswordDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text("Cambiar contraseña")
                            }
                            
                            // Cerrar sesión
                            Button(
                                onClick = { showLogoutConfirmDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text("Cerrar sesión")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
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
        }
    )
}

/**
 * Componente para mostrar los diálogos de la pantalla de perfil
 */
@Composable
private fun MostrarDialogos(
    showChangePasswordDialog: Boolean,
    onChangePasswordDismiss: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    showLogoutConfirmDialog: Boolean,
    onLogoutDismiss: () -> Unit,
    onLogoutConfirm: () -> Unit
) {
    // Diálogo para cambiar contraseña
    if (showChangePasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = onChangePasswordDismiss,
            title = { Text("Cambiar contraseña") },
            text = {
                Column {
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Contraseña actual") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva contraseña") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar contraseña") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
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
                    }
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
 * Campo para mostrar y editar información del perfil
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
        
        // Campo editable o texto
        if (editable) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    supportingText = helperText?.let { { Text(it) } }
                )
            }
        } else {
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
    }
    
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
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