package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.AdminColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.tfg.umeegunero.feature.admin.screen.components.SectionHeader
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCard
import com.tfg.umeegunero.feature.admin.screen.components.BotonAccion
import com.tfg.umeegunero.ui.theme.AppColors
import androidx.compose.foundation.BorderStroke
import com.tfg.umeegunero.data.model.Usuario
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import com.tfg.umeegunero.feature.admin.screen.components.DashboardItem
import androidx.compose.material3.HorizontalDivider

/**
 * Dashboard del administrador de la aplicación
 * 
 * Esta pantalla presenta un panel de control completo para la administración
 * general de la aplicación, incluyendo gestión de centros, reportes, configuración
 * del sistema y comunicaciones.
 *
 * @param navController NavController para la navegación
 * @param viewModel ViewModel que contiene la lógica de negocio del dashboard de administración
 * @param onNavigateToGestionUsuarios Callback para navegar a la gestión de usuarios
 * @param onNavigateToGestionCentros Callback para navegar a la gestión de centros
 * @param onNavigateToEstadisticas Callback para navegar a estadísticas
 * @param onNavigateToSeguridad Callback para navegar a seguridad
 * @param onNavigateToTema Callback para navegar a configuración de tema
 * @param onNavigateToEmailConfig Callback para navegar a configuración de email
 * @param onNavigateToComunicados Callback para navegar a comunicados
 * @param onNavigateToSoporteTecnico Callback para navegar a soporte técnico
 * @param onNavigateToFAQ Callback para navegar a FAQ
 * @param onNavigateToTerminos Callback para navegar a términos y condiciones
 * @param onNavigateToLogout Callback para cerrar sesión
 * @param onNavigateToProfile Callback para navegar a perfil
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 3.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onNavigateToGestionUsuarios: () -> Unit = {},
    onNavigateToGestionCentros: () -> Unit,
    onNavigateToEstadisticas: () -> Unit,
    onNavigateToSeguridad: () -> Unit,
    onNavigateToTema: () -> Unit,
    onNavigateToEmailConfig: () -> Unit,
    onNavigateToComunicados: () -> Unit,
    onNavigateToSoporteTecnico: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToTerminos: () -> Unit,
    onNavigateToLogout: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showEmailConfigDialog by remember { mutableStateOf(false) }
    var showComunicadosDialog by remember { mutableStateOf(false) }
    var showBandejaEntradaDialog by remember { mutableStateOf(false) }
    var showComponerMensajeDialog by remember { mutableStateOf(false) }
    var showSoporteTecnicoDialog by remember { mutableStateOf(false) }
    var showFAQDialog by remember { mutableStateOf(false) }
    var showTerminosDialog by remember { mutableStateOf(false) }
    var showEmailTestScreen by remember { mutableStateOf(false) }

    // En la sección de gestión de usuarios, solo mostrar la opción si el usuario es ADMIN_APP
    val puedeGestionarUsuarios = uiState.currentUser?.perfiles?.any { it.tipo.name == "ADMIN_APP" } == true

    // Diálogos
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onNavigateToLogout()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Cambiar tema") },
            text = { Text("¿Quieres cambiar el tema de la aplicación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showThemeDialog = false
                        onNavigateToTema()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showEmailConfigDialog) {
        AlertDialog(
            onDismissRequest = { showEmailConfigDialog = false },
            title = { Text("Configurar email") },
            text = { Text("¿Quieres configurar el email de soporte?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmailConfigDialog = false
                        onNavigateToEmailConfig()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailConfigDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showComunicadosDialog) {
        AlertDialog(
            onDismissRequest = { showComunicadosDialog = false },
            title = { Text("Comunicados") },
            text = { Text("¿Quieres ver los comunicados?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showComunicadosDialog = false
                        onNavigateToComunicados()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComunicadosDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showBandejaEntradaDialog) {
        AlertDialog(
            onDismissRequest = { showBandejaEntradaDialog = false },
            title = { Text("Bandeja de mensajes") },
            text = { Text("¿Quieres ver tu bandeja de mensajes unificada?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBandejaEntradaDialog = false
                        navController.navigate(AppScreens.UnifiedInbox.route)
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBandejaEntradaDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showComponerMensajeDialog) {
        AlertDialog(
            onDismissRequest = { showComponerMensajeDialog = false },
            title = { Text("Nuevo mensaje") },
            text = { Text("¿Quieres crear un nuevo mensaje?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showComponerMensajeDialog = false
                        navController.navigate(AppScreens.NewMessage.createRoute())
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComponerMensajeDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showSoporteTecnicoDialog) {
        AlertDialog(
            onDismissRequest = { showSoporteTecnicoDialog = false },
            title = { Text("Soporte técnico") },
            text = { Text("¿Quieres acceder al soporte técnico?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSoporteTecnicoDialog = false
                        onNavigateToSoporteTecnico()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSoporteTecnicoDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showFAQDialog) {
        AlertDialog(
            onDismissRequest = { showFAQDialog = false },
            title = { Text("Preguntas frecuentes") },
            text = { Text("¿Quieres ver las preguntas frecuentes?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFAQDialog = false
                        onNavigateToFAQ()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFAQDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showTerminosDialog) {
        AlertDialog(
            onDismissRequest = { showTerminosDialog = false },
            title = { Text("Términos y condiciones") },
            text = { Text("¿Quieres ver los términos y condiciones?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTerminosDialog = false
                        onNavigateToTerminos()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTerminosDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Panel de administración",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(AppScreens.PruebaEmail.route)
                        }
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Probar Email")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Editar perfil")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = AdminColor.copy(alpha = 0.05f)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Tarjeta de bienvenida animada
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                        WelcomeCardV2(
                            nombre = uiState.currentUser?.nombre,
                            fecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
                        )
                    }
                    }
                    // Separador visual
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    
                    // Resumen estadístico
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                            ResumenEstadisticasAdminCard(
                                totalCentros = uiState.totalCentros,
                                totalUsuarios = uiState.totalUsuarios,
                                nuevosCentros = uiState.nuevosCentros,
                                nuevosUsuarios = uiState.nuevosUsuarios,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Separador visual
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    // Sección: Gestión Académica
                    item {
                        Text(
                            text = "Gestión Académica",
                            style = MaterialTheme.typography.titleLarge,
                            color = AdminColor
                        )
                    }
                    // Primera fila: Centros y Usuarios (con animación y borde)
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Centros",
                                icono = Icons.Default.Business,
                                descripcion = "Gestionar centros educativos",
                                color = AdminColor,
                                    iconTint = AppColors.PurplePrimary,
                                    border = true,
                                onClick = onNavigateToGestionCentros,
                                modifier = Modifier.weight(1f)
                            )
                                if (puedeGestionarUsuarios) {
                            CategoriaCard(
                                titulo = "Usuarios",
                                icono = Icons.Default.People,
                                        descripcion = "Gestión de administradores de aplicación y centro",
                                color = AdminColor,
                                        iconTint = AppColors.PurpleTertiary,
                                        border = true,
                                onClick = onNavigateToGestionUsuarios,
                                modifier = Modifier.weight(1f)
                            )
                                }
                            }
                        }
                    }
                    // Segunda fila: Estadísticas (con animación y borde)
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        CategoriaCard(
                            titulo = "Estadísticas",
                            icono = Icons.Default.BarChart,
                            descripcion = "Ver estadísticas generales",
                            color = AdminColor,
                                iconTint = AppColors.Pink80,
                                border = true,
                            onClick = onNavigateToEstadisticas,
                            modifier = Modifier.fillMaxWidth()
                        )
                        }
                    }
                    // Separador visual
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    // Sección: Configuración
                    item {
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.titleLarge,
                            color = AdminColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    // Primera fila: Seguridad y Tema (con animación y borde)
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Seguridad",
                                icono = Icons.Default.Security,
                                descripcion = "Configurar aspectos de seguridad",
                                color = AdminColor,
                                    iconTint = AppColors.Green500,
                                    border = true,
                                onClick = onNavigateToSeguridad,
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Tema",
                                icono = Icons.Default.Palette,
                                descripcion = "Cambiar tema de la aplicación",
                                color = AdminColor,
                                    iconTint = AppColors.PurpleSecondary,
                                    border = true,
                                onClick = { showThemeDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            }
                        }
                    }
                    // Segunda fila: Email y Notificaciones (con animación y borde)
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Email",
                                icono = Icons.Default.Email,
                                descripcion = "Configurar email de soporte",
                                color = AdminColor,
                                iconTint = AppColors.Red500,
                                border = true,
                                onClick = { showEmailConfigDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                            }
                        }
                    }
                    // Separador visual
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    // Sección: Comunicación y Soporte
                    item {
                        Text(
                            text = "Comunicación y Soporte",
                            style = MaterialTheme.typography.titleLarge,
                            color = AdminColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    // Primera fila: Comunicados y Soporte técnico (con animación y borde)
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "Comunicados",
                                icono = Icons.Default.Campaign,
                                descripcion = "Ver y gestionar comunicados",
                                color = AdminColor,
                                    iconTint = AppColors.GradientStart,
                                    border = true,
                                onClick = { showComunicadosDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Soporte técnico",
                                icono = Icons.AutoMirrored.Filled.Help,
                                descripcion = "Acceder al soporte técnico",
                                color = AdminColor,
                                    iconTint = AppColors.FamiliarColor,
                                    border = true,
                                onClick = { showSoporteTecnicoDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            }
                        }
                    }
                    // Segunda fila: FAQ y Términos (con animación y borde)
                    item {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaCard(
                                titulo = "FAQ",
                                icono = Icons.Default.QuestionAnswer,
                                descripcion = "Ver preguntas frecuentes",
                                color = AdminColor,
                                    iconTint = AppColors.Alumno,
                                    border = true,
                                onClick = { showFAQDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            CategoriaCard(
                                titulo = "Términos",
                                icono = Icons.Default.Description,
                                descripcion = "Ver términos y condiciones",
                                color = AdminColor,
                                    iconTint = AppColors.AcademicoColorDark,
                                    border = true,
                                onClick = { showTerminosDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Llamada a la pantalla de prueba (ajustar si se renombra EmailTestScreen)
            if (showEmailTestScreen) {
                com.tfg.umeegunero.feature.admin.screen.test.EmailTestScreen(onClose = { showEmailTestScreen = false })
            }
        }
    }
}

/**
 * Grid de acciones rápidas para dashboards, usando el nuevo diseño compacto.
 */
@Composable
fun GridAccionesRapidas(
    onGestionCentros: () -> Unit,
    onGestionUsuarios: () -> Unit,
    onConfiguracion: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BotonAccion(
            icono = Icons.Default.Business,
            texto = "Centros",
                onClick = onGestionCentros
            )
        BotonAccion(
            icono = Icons.Default.People,
            texto = "Usuarios",
                onClick = onGestionUsuarios
            )
        BotonAccion(
            icono = Icons.Default.Security,
            texto = "Configuración",
                onClick = onConfiguracion
            )
    }
}

/**
 * Tarjeta de bienvenida mejorada para el dashboard de administración
 */
@Composable
private fun WelcomeCardV2(
    nombre: String?,
    fecha: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp, max = 100.dp),
        colors = CardDefaults.cardColors(
            containerColor = AdminColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (!nombre.isNullOrBlank()) {
                    Text(
                        text = "¡Bienvenido/a, $nombre!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AdminColor
                    )
                }
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AdminColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * ViewModel para la pantalla de administrador
 */
class AdminDashboardViewModel : androidx.lifecycle.ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState
    
    fun logout() {
        // Implementación básica para compilar
    }
}

/**
 * Estado de la UI para la pantalla de administrador
 */
data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showListadoCentros: Boolean = false,
    val currentUser: Usuario? = null
)

/**
 * Tarjeta de resumen estadístico para el dashboard de administrador
 */
@Composable
fun ResumenEstadisticasAdminCard(
    totalCentros: Int,
    totalUsuarios: Int,
    nuevosCentros: Int,
    nuevosUsuarios: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = AdminColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumen General",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AdminColor
                )
                
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = AdminColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Centros
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Centros",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AdminColor
                    )
                    Text(
                        text = "$totalCentros",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AdminColor
                    )
                    Text(
                        text = "+$nuevosCentros nuevos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Usuarios
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Usuarios",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AdminColor
                    )
                    Text(
                        text = "$totalUsuarios",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AdminColor
                    )
                    Text(
                        text = "+$nuevosUsuarios nuevos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Previsualización profesional del dashboard de administrador.
 */
@Preview(showBackground = true)
@Composable
fun VistaPreviaDashboardAdmin() {
    UmeEguneroTheme {
        AdminDashboardScreen(
            navController = rememberNavController(),
            onNavigateToGestionUsuarios = {},
            onNavigateToGestionCentros = {},
            onNavigateToEstadisticas = {},
            onNavigateToSeguridad = {},
            onNavigateToTema = {},
            onNavigateToEmailConfig = {},
            onNavigateToComunicados = {},
            onNavigateToSoporteTecnico = {},
            onNavigateToFAQ = {},
            onNavigateToTerminos = {},
            onNavigateToLogout = {}
        )
    }
}