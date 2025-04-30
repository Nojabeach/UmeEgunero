/**
 * Módulo de creación y edición de centros educativos del sistema UmeEgunero.
 * 
 * Este módulo implementa la interfaz para la creación y edición de centros
 * educativos, incluyendo toda la información necesaria para su registro
 * en el sistema.
 */
package com.tfg.umeegunero.feature.admin.screen

import android.content.Intent
import android.net.Uri
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Ciudad
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
import com.tfg.umeegunero.feature.admin.viewmodel.AdminCentroUsuario
import com.tfg.umeegunero.ui.components.FormProgressIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla principal para la creación y edición de centros educativos.
 * 
 * Esta pantalla proporciona un formulario completo para la gestión de
 * centros educativos, permitiendo tanto la creación de nuevos centros
 * como la edición de los existentes.
 * 
 * ## Características
 * - Formulario completo de datos
 * - Validación en tiempo real
 * - Indicador de progreso
 * - Gestión de administradores
 * 
 * ## Funcionalidades
 * - Datos básicos del centro
 * - Información de contacto
 * - Ubicación geográfica
 * - Gestión de administradores
 * - Guardado y eliminación
 * 
 * ## Estados
 * - Modo creación/edición
 * - Validación de campos
 * - Proceso de guardado
 * - Confirmación de eliminación
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona la lógica del formulario
 * @param centroId Identificador del centro a editar (null para creación)
 * 
 * @see AddCentroViewModel
 * @see Centro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCentroScreen(
    navController: NavController,
    viewModel: AddCentroViewModel = hiltViewModel(),
    centroId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val provinciasLista by viewModel.provincias.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Si hay un ID de centro, cargar sus datos
    LaunchedEffect(centroId) {
        if (!centroId.isNullOrBlank()) {
            viewModel.loadCentro(centroId)
        }
    }
    
    // Observar cambios en el estado y navegar de vuelta si la operación fue exitosa
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.popBackStack()
        }
    }

    // Detectar si estamos en modo edición
    val isEditMode = uiState.id.isNotBlank()
    
    // Calcular el porcentaje de completado del formulario
    val porcentajeCompletado = calcularPorcentajeCompletado(uiState)
    
    // Efecto para mostrar errores en el Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    // Estado para el diálogo de confirmación de eliminación
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) "Editar Centro" else "Nuevo Centro Educativo",
                        style = MaterialTheme.typography.headlineSmall
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
                    if (isEditMode) {
                        // Botón de eliminar solo en modo edición
                        IconButton(
                            onClick = {
                                showDeleteConfirmation = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar centro",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    // Botón de guardar
                    IconButton(
                        onClick = { 
                            viewModel.guardarCentro()
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar centro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        AddCentroScreenContent(
            uiState = uiState,
            provincias = provinciasLista,
            porcentajeCompletado = porcentajeCompletado,
            isEditMode = isEditMode,
            onNombreChange = viewModel::updateNombre,
            onCalleChange = viewModel::updateCalle,
            onNumeroChange = viewModel::updateNumero,
            onCodigoPostalChange = viewModel::updateCodigoPostal,
            onCiudadChange = viewModel::updateCiudad,
            onProvinciaChange = viewModel::updateProvincia,
            onTelefonoChange = viewModel::updateTelefono,
            onCiudadSelected = viewModel::seleccionarCiudad,
            onToggleMapa = viewModel::toggleMapa,
            onSaveClick = {
                viewModel.guardarCentro()
            },
            onCancelClick = { navController.popBackStack() },
            onAddAdminCentro = viewModel::addAdminCentro,
            onRemoveAdminCentro = viewModel::removeAdminCentro,
            onUpdateAdminCentroDni = viewModel::updateAdminCentroDni,
            onUpdateAdminCentroNombre = viewModel::updateAdminCentroNombre,
            onUpdateAdminCentroApellidos = viewModel::updateAdminCentroApellidos,
            onUpdateAdminCentroEmail = viewModel::updateAdminCentroEmail,
            onUpdateAdminCentroTelefono = viewModel::updateAdminCentroTelefono,
            onUpdateAdminCentroPassword = viewModel::updateAdminCentroPassword,
            modifier = Modifier.padding(paddingValues)
        )
        
        // Diálogo de confirmación para eliminar centro
        if (showDeleteConfirmation) {
            DeleteCentroConfirmationDialog(
                onConfirm = {
                    viewModel.deleteCentro(uiState.id)
                },
                onDismiss = {
                    showDeleteConfirmation = false
                }
            )
        }
    }
}

/**
 * Contenido principal del formulario de centro educativo.
 * 
 * Este componente implementa el formulario completo para la gestión
 * de centros educativos, incluyendo todos los campos necesarios y
 * su validación.
 * 
 * ## Secciones
 * - Información básica
 * - Dirección y ubicación
 * - Contacto y comunicación
 * - Administradores del centro
 * 
 * @param uiState Estado actual del formulario
 * @param provincias Lista de provincias disponibles
 * @param porcentajeCompletado Porcentaje de campos completados
 * @param isEditMode Modo de edición activo
 * @param onNombreChange Callback para cambio de nombre
 * @param onCalleChange Callback para cambio de calle
 * @param onNumeroChange Callback para cambio de número
 * @param onCodigoPostalChange Callback para cambio de código postal
 * @param onCiudadChange Callback para cambio de ciudad
 * @param onProvinciaChange Callback para cambio de provincia
 * @param onTelefonoChange Callback para cambio de teléfono
 * @param onCiudadSelected Callback para selección de ciudad
 * @param onToggleMapa Callback para mostrar/ocultar mapa
 * @param onSaveClick Callback para guardar cambios
 * @param onCancelClick Callback para cancelar
 * @param onAddAdminCentro Callback para añadir administrador
 * @param onRemoveAdminCentro Callback para eliminar administrador
 * @param onUpdateAdminCentroDni Callback para actualizar DNI de administrador
 * @param onUpdateAdminCentroNombre Callback para actualizar nombre de administrador
 * @param onUpdateAdminCentroApellidos Callback para actualizar apellidos de administrador
 * @param onUpdateAdminCentroEmail Callback para actualizar email de administrador
 * @param onUpdateAdminCentroTelefono Callback para actualizar teléfono de administrador
 * @param onUpdateAdminCentroPassword Callback para actualizar contraseña de administrador
 * @param modifier Modificador de composición
 * 
 * @see AddCentroViewModel.AddCentroState
 */
@Composable
fun AddCentroScreenContent(
    uiState: AddCentroViewModel.AddCentroState,
    provincias: List<String>,
    porcentajeCompletado: Float,
    isEditMode: Boolean = false,
    onNombreChange: (String) -> Unit,
    onCalleChange: (String) -> Unit,
    onNumeroChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCiudadChange: (String) -> Unit,
    onProvinciaChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onCiudadSelected: (Ciudad) -> Unit,
    onToggleMapa: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAddAdminCentro: () -> Unit,
    onRemoveAdminCentro: (Int) -> Unit,
    onUpdateAdminCentroDni: (Int, String) -> Unit,
    onUpdateAdminCentroNombre: (Int, String) -> Unit,
    onUpdateAdminCentroApellidos: (Int, String) -> Unit,
    onUpdateAdminCentroEmail: (Int, String) -> Unit,
    onUpdateAdminCentroTelefono: (Int, String) -> Unit,
    onUpdateAdminCentroPassword: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val erroresCount = countErrors(uiState)
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Indicador de progreso del formulario
            FormProgressIndicator(
                progress = porcentajeCompletado,
                errorCount = erroresCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            // Sección de información básica
            SectionCard(
                title = "Información Básica",
                subtitle = "Datos generales del centro educativo",
                icon = Icons.Default.School,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Campo para el nombre del centro
                EnhancedFormTextField(
                    value = uiState.nombre,
                    onValueChange = onNombreChange,
                    label = "Nombre del Centro",
                    error = uiState.nombreError,
                    icon = Icons.Default.School,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = "Ej: IES Miguel de Cervantes"
                )
                
                // Campo para el teléfono
                EnhancedFormTextField(
                    value = uiState.telefono,
                    onValueChange = onTelefonoChange,
                    label = "Teléfono (opcional)",
                    error = uiState.telefonoError,
                    icon = Icons.Default.Phone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardType = KeyboardType.Phone,
                    placeholder = "Ej: 944123456"
                )
            }
            
            // Sección de dirección
            DireccionSection(
                uiState = uiState,
                onCalleChange = onCalleChange,
                onNumeroChange = onNumeroChange,
                onCodigoPostalChange = onCodigoPostalChange,
                onCiudadChange = onCiudadChange,
                onCiudadSelected = onCiudadSelected,
                onProvinciaChange = onProvinciaChange,
                onToggleMapa = onToggleMapa,
                provincias = provincias,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Sección de administradores
            SectionCard(
                title = "Administradores del Centro",
                subtitle = "Usuarios que podrán gestionar el centro",
                icon = Icons.Default.Lock,
                accentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Texto explicativo
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Al menos un administrador principal es obligatorio para gestionar el centro educativo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Este usuario se creará en el sistema con sus credenciales y podrá acceder a la plataforma para gestionar el centro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (uiState.adminCentroError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = uiState.adminCentroError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Lista de administradores
                uiState.adminCentro.forEachIndexed { index, admin ->
                    AdminCentroCard(
                        admin = admin,
                        index = index,
                        canDelete = uiState.adminCentro.size > 1,
                        onDniChange = { onUpdateAdminCentroDni(index, it) },
                        onNombreChange = { onUpdateAdminCentroNombre(index, it) },
                        onApellidosChange = { onUpdateAdminCentroApellidos(index, it) },
                        onEmailChange = { onUpdateAdminCentroEmail(index, it) },
                        onTelefonoChange = { onUpdateAdminCentroTelefono(index, it) },
                        onPasswordChange = { onUpdateAdminCentroPassword(index, it) },
                        onRemove = { onRemoveAdminCentro(index) },
                        isPrimary = index == 0
                    )
                }
                
                // Botón para añadir más administradores
                OutlinedButton(
                    onClick = onAddAdminCentro,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir otro administrador")
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón de cancelar
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancelar")
                }
                
                // Botón de guardar
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading && porcentajeCompletado > 0.5f,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isEditMode) "Actualizar" else "Guardar")
                    }
                }
            }
        }
        
        // Indicador de carga superpuesto
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddCentroScreenPreview() {
    UmeEguneroTheme {
        AddCentroScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(
    name = "Modo oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AddCentroScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        AddCentroScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Componente reutilizable para mostrar un indicador de progreso de formulario con contador de errores
 */
@Composable
fun FormProgressIndicator(
    progress: Float,
    errorCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Completado: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (errorCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$errorCount ${if (errorCount == 1) "error" else "errores"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (errorCount > 0) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Componente para tarjetas de sección con título e icono
 */
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                // Icono circular con efecto de elevación y color temático
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = accentColor,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            content()
        }
    }
}

/**
 * Componente para campos de texto con formato mejorado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    icon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    placeholder: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { 
                if (it.length <= maxLength) {
                    onValueChange(it)
                }
            },
            label = { 
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                ) 
            },
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else null,
            trailingIcon = trailingIcon,
            isError = error != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            modifier = modifier,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = placeholder?.let { { 
                Text(
                    text = it, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge
                ) 
            } }
        )

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Sección de dirección en el formulario
 */
@Composable
fun DireccionSection(
    uiState: AddCentroViewModel.AddCentroState,
    onCalleChange: (String) -> Unit,
    onNumeroChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCiudadChange: (String) -> Unit,
    onCiudadSelected: (Ciudad) -> Unit,
    onProvinciaChange: (String) -> Unit,
    onToggleMapa: () -> Unit,
    provincias: List<String>,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Ubicación del Centro", 
        subtitle = "Dirección física del centro educativo",
        icon = Icons.Default.LocationCity,
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        // Campo para la vía (en su propia fila)
        EnhancedFormTextField(
            value = uiState.calle,
            onValueChange = onCalleChange,
            label = "Vía / Calle",
            error = uiState.calleError,
            icon = Icons.Default.Home,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = "Ej: Calle Mayor"
        )

        // Campo para el número (en su propia fila)
        EnhancedFormTextField(
            value = uiState.numero,
            onValueChange = onNumeroChange,
            label = "Número",
            error = uiState.numeroError,
            icon = Icons.Default.Place,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardType = KeyboardType.Number,
            placeholder = "Ej: 25"
        )

        // Código postal (en su propia fila)
        EnhancedFormTextField(
            value = uiState.codigoPostal,
            onValueChange = onCodigoPostalChange,
            label = "Código Postal",
            error = uiState.codigoPostalError,
            icon = Icons.Default.Place,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (uiState.isBuscandoCiudades || uiState.errorBusquedaCiudades != null) 8.dp else 16.dp),
            keyboardType = KeyboardType.Number,
            maxLength = 5,
            placeholder = "Ej: 48001"
        )

        // Indicador de búsqueda o error para el código postal
        if (uiState.isBuscandoCiudades) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else if (uiState.errorBusquedaCiudades != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = uiState.errorBusquedaCiudades,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Ciudad (en su propia fila)
        if (uiState.ciudadesSugeridas.isNotEmpty()) {
            CiudadDropdown(
                ciudad = uiState.ciudad,
                onCiudadChange = onCiudadChange,
                ciudadesSugeridas = uiState.ciudadesSugeridas,
                onCiudadSelected = onCiudadSelected,
                ciudadError = uiState.ciudadError
            )
        } else {
            EnhancedFormTextField(
                value = uiState.ciudad,
                onValueChange = onCiudadChange,
                label = "Ciudad/Municipio",
                error = uiState.ciudadError,
                icon = Icons.Default.LocationCity,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = "Ej: Bilbao"
            )
        }

        // Provincia - Dropdown mejorado
        ProvinciaDropdown(
            provincia = uiState.provincia,
            onProvinciaChange = onProvinciaChange,
            provincias = provincias,
            provinciaError = uiState.provinciaError
        )

        // Mostrar mapa con la ubicación si tenemos coordenadas
        if (uiState.latitud != null && uiState.longitud != null) {
            MapaUbicacionSection(
                mostrarMapa = uiState.mostrarMapa,
                onToggleMapa = onToggleMapa,
                latitud = uiState.latitud,
                longitud = uiState.longitud,
                direccionCompleta = uiState.direccionCompleta
            )
        }
    }
}

/**
 * Dropdown para seleccionar ciudad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiudadDropdown(
    ciudad: String,
    onCiudadChange: (String) -> Unit,
    ciudadesSugeridas: List<Ciudad>,
    onCiudadSelected: (Ciudad) -> Unit,
    ciudadError: String?
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = ciudad,
            onValueChange = onCiudadChange,
            label = { Text("Ciudad/Municipio") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = ciudadError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            placeholder = { Text("Seleccione una ciudad", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            ciudadesSugeridas.forEach { ciudad ->
                DropdownMenuItem(
                    text = { Text("${ciudad.nombre} (${ciudad.provincia})") },
                    onClick = {
                        onCiudadSelected(ciudad)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

/**
 * Dropdown para seleccionar provincia
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinciaDropdown(
    provincia: String,
    onProvinciaChange: (String) -> Unit,
    provincias: List<String>,
    provinciaError: String?
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = provincia,
            onValueChange = onProvinciaChange,
            label = { Text("Provincia") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = provinciaError != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            placeholder = { Text("Seleccione una provincia", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            provincias.forEach { provincia ->
                DropdownMenuItem(
                    text = { Text(provincia) },
                    onClick = {
                        onProvinciaChange(provincia)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

/**
 * Sección para mostrar el mapa de ubicación
 */
@Composable
fun MapaUbicacionSection(
    mostrarMapa: Boolean,
    onToggleMapa: () -> Unit,
    latitud: Double,
    longitud: Double,
    direccionCompleta: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ubicación en mapa",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Switch(
            checked = mostrarMapa,
            onCheckedChange = { onToggleMapa() }
        )
    }

    AnimatedVisibility(
        visible = mostrarMapa,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        MapaUbicacion(
            latitud = latitud,
            longitud = longitud,
            direccionCompleta = direccionCompleta,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * Mapa de ubicación (versión simplificada que abre Google Maps)
 */
@Composable
fun MapaUbicacion(
    latitud: Double,
    longitud: Double,
    direccionCompleta: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Mapa de ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Coordenadas: $latitud, $longitud",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = direccionCompleta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Enlace para abrir en Google Maps
            Button(
                onClick = {
                    // Abrir Google Maps con las coordenadas
                    val gmmIntentUri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud($direccionCompleta)")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    // Verificar si Google Maps está instalado
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        // Si Google Maps no está instalado, abrir en el navegador
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitud,$longitud")
                        )
                        context.startActivity(browserIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Abrir en Google Maps",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Componente para mostrar un formulario de administrador de centro
 */
@Composable
fun AdminCentroCard(
    admin: AdminCentroUsuario,
    index: Int,
    canDelete: Boolean,
    onDniChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRemove: () -> Unit,
    isPrimary: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPrimary) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Título con etiqueta de "Principal" si corresponde
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isPrimary) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (isPrimary) "Administrador Principal" else "Administrador Adicional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                if (isPrimary) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Obligatorio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (canDelete) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar administrador",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // DNI
            EnhancedFormTextField(
                value = admin.dni,
                onValueChange = onDniChange,
                label = "DNI",
                error = admin.dniError,
                icon = Icons.Default.Place,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = "Ej: 12345678A"
            )
            
            // Nombre
            EnhancedFormTextField(
                value = admin.nombre,
                onValueChange = onNombreChange,
                label = "Nombre",
                error = admin.nombreError,
                icon = Icons.Default.School,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = "Ej: Juan"
            )
            
            // Apellidos
            EnhancedFormTextField(
                value = admin.apellidos,
                onValueChange = onApellidosChange,
                label = "Apellidos",
                error = admin.apellidosError,
                icon = Icons.Default.School,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = "Ej: López García"
            )
            
            // Email
            EnhancedFormTextField(
                value = admin.email,
                onValueChange = onEmailChange,
                label = "Email",
                error = admin.emailError,
                icon = Icons.Default.Email,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = "Ej: admin@centro.com",
                keyboardType = KeyboardType.Email
            )
            
            // Teléfono (opcional)
            EnhancedFormTextField(
                value = admin.telefono,
                onValueChange = onTelefonoChange,
                label = "Teléfono",
                error = admin.telefonoError,
                icon = Icons.Default.Phone,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = "Ej: 666777888 (opcional)",
                keyboardType = KeyboardType.Phone
            )
            
            // Contraseña
            EnhancedFormTextField(
                value = admin.password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                error = admin.passwordError,
                icon = Icons.Default.Lock,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.Visibility 
                            else 
                                Icons.Default.VisibilityOff,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = "Min. 8 caracteres, letras y números"
            )
            
            // Mensaje de ayuda para contraseña
            Text(
                text = "La contraseña debe tener al menos 8 caracteres e incluir letras y números.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
            
            // Si es el administrador principal, mostrar mensaje explicativo adicional
            if (isPrimary) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Este usuario es obligatorio y podrá acceder a la aplicación con el email y contraseña especificados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Diálogo de confirmación para eliminar un centro
 */
@Composable
fun DeleteCentroConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar centro",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    text = "¿Estás seguro de que deseas eliminar este centro educativo?",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Esta acción no se puede deshacer y también se eliminarán todos los datos asociados al centro, incluyendo administradores, profesores, alumnos y comunicaciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Esta operación es irreversible",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Eliminar centro")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Calcula el porcentaje de completado del formulario para añadir o editar un centro
 * @param uiState Estado actual del formulario
 * @return Valor entre 0.0 y 1.0 que representa el porcentaje de completado
 */
private fun calcularPorcentajeCompletado(uiState: AddCentroViewModel.AddCentroState): Float {
    var camposCompletados = 0
    var camposTotales = 6 // Campos principales obligatorios

    // Verificar campos principales
    if (uiState.nombre.isNotBlank()) camposCompletados++
    if (uiState.calle.isNotBlank()) camposCompletados++
    if (uiState.numero.isNotBlank()) camposCompletados++
    if (uiState.codigoPostal.isNotBlank()) camposCompletados++
    if (uiState.ciudad.isNotBlank()) camposCompletados++
    if (uiState.provincia.isNotBlank()) camposCompletados++

    // Verificar administradores (al menos uno con datos válidos)
    var adminCompletados = 0
    var adminTotales = uiState.adminCentro.size * 5 // DNI, nombre, apellidos, email, password

    uiState.adminCentro.forEach { admin ->
        if (admin.dni.isNotBlank()) adminCompletados++
        if (admin.nombre.isNotBlank()) adminCompletados++
        if (admin.apellidos.isNotBlank()) adminCompletados++
        if (admin.email.isNotBlank()) adminCompletados++
        if (admin.password.isNotBlank()) adminCompletados++
    }

    // Calcular porcentaje considerando tanto los campos básicos como los administradores
    val porcentajeCamposBasicos = camposCompletados.toFloat() / camposTotales.toFloat()
    val porcentajeAdmins = if (adminTotales > 0) adminCompletados.toFloat() / adminTotales.toFloat() else 0f
    
    // Combinar ambos porcentajes (dando más peso a los campos básicos)
    return (porcentajeCamposBasicos * 0.6f) + (porcentajeAdmins * 0.4f)
}

/**
 * Cuenta el número de errores en el estado del formulario
 */
private fun countErrors(uiState: AddCentroViewModel.AddCentroState): Int {
    var errores = 0

    // Errores en los campos básicos
    if (uiState.nombreError != null) errores++
    if (uiState.calleError != null) errores++
    if (uiState.numeroError != null) errores++
    if (uiState.codigoPostalError != null) errores++
    if (uiState.ciudadError != null) errores++
    if (uiState.provinciaError != null) errores++
    if (uiState.telefonoError != null) errores++
    
    // Errores en administradores
    if (uiState.adminCentroError != null) errores++
    
    uiState.adminCentro.forEach { admin ->
        if (admin.dniError != null) errores++
        if (admin.nombreError != null) errores++
        if (admin.apellidosError != null) errores++
        if (admin.emailError != null) errores++
        if (admin.telefonoError != null) errores++
        if (admin.passwordError != null) errores++
    }

    return errores
}

/**
 * Función de extensión que reemplaza el método capitalize() obsoleto
 */
private fun String.capitalizeFirst(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
} 