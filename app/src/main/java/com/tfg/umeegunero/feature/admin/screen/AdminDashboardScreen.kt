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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.tfg.umeegunero.ui.theme.AcademicoColorDark
import com.tfg.umeegunero.data.model.Usuario

/**
 * Dashboard del administrador de la aplicación
 * 
 * Esta pantalla presenta un panel de control completo para la administración
 * general de la aplicación, incluyendo gestión de centros, reportes, configuración
 * del sistema y comunicaciones.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 * @param viewModel ViewModel que contiene la lógica de negocio del dashboard de administración
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 3.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Variables para control de animaciones
    var showContent by remember { mutableStateOf(false) }
    val currentDate = remember { 
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES")))
    }
    
    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    // Efecto para manejar navegación
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Panel de Administración",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AcademicoColorDark,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // Configuración
                    IconButton(onClick = { 
                        navController.navigate(AppScreens.Perfil.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                    
                    // Cerrar sesión
                    IconButton(onClick = { 
                        viewModel.logout()
                        navController.navigate(AppScreens.Welcome.route) {
                            popUpTo(AppScreens.AdminDashboard.route) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AcademicoColorDark)
            }
        } else {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Tarjeta de bienvenida
                    WelcomeCardV2(currentDate = currentDate, nombreAdmin = uiState.usuario?.nombre)

                    // --- GESTIÓN ACADÉMICA ---
                    SectionHeader(title = "Gestión Académica", icon = Icons.Default.Dashboard)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Centros",
                            descripcion = "Gestión de centros educativos",
                            icono = Icons.Default.Business,
                            color = AdminColor,
                            onClick = { navController.navigate(AppScreens.GestionCentros.route) }
                        )
                        CategoriaCard(
                            titulo = "Usuarios",
                            descripcion = "Gestión de usuarios",
                            icono = Icons.Default.People,
                            color = AdminColor,
                            onClick = { navController.navigate(AppScreens.GestionUsuarios.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Cursos",
                            descripcion = "Listado y gestión de cursos",
                            icono = Icons.Default.MenuBook,
                            color = Color(0xFF8E24AA),
                            onClick = { navController.navigate(AppScreens.Cursos.route) }
                        )
                        CategoriaCard(
                            titulo = "Clases",
                            descripcion = "Listado y gestión de clases",
                            icono = Icons.Default.School,
                            color = Color(0xFF3949AB),
                            onClick = { navController.navigate(AppScreens.Clases.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Profesores",
                            descripcion = "Listado de profesores",
                            icono = Icons.Default.School,
                            color = Color(0xFF039BE5),
                            onClick = { navController.navigate(AppScreens.ProfesorList.route) }
                        )
                        CategoriaCard(
                            titulo = "Alumnos",
                            descripcion = "Listado de alumnos",
                            icono = Icons.Default.ChildCare,
                            color = Color(0xFF43A047),
                            onClick = { navController.navigate(AppScreens.AlumnoList.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Familiares",
                            descripcion = "Listado de familiares",
                            icono = Icons.Default.FamilyRestroom,
                            color = Color(0xFFFB8C00),
                            onClick = { navController.navigate(AppScreens.FamiliarList.route) }
                        )
                        CategoriaCard(
                            titulo = "Administradores",
                            descripcion = "Administradores de la app",
                            icono = Icons.Default.AdminPanelSettings,
                            color = Color(0xFF6D4C41),
                            onClick = { navController.navigate(AppScreens.AdminList.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Admins de centro",
                            descripcion = "Administradores de centro",
                            icono = Icons.Default.AccountBalance,
                            color = Color(0xFF00897B),
                            onClick = { navController.navigate(AppScreens.AdminCentroList.route) }
                        )
                        CategoriaCard(
                            titulo = "Estadísticas",
                            descripcion = "Análisis y datos",
                            icono = Icons.Default.BarChart,
                            color = Color(0xFF1976D2),
                            onClick = { navController.navigate(AppScreens.Estadisticas.route) }
                        )
                    }

                    // --- CONFIGURACIÓN ---
                    SectionHeader(title = "Configuración", icon = Icons.Default.Settings)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Seguridad",
                            descripcion = "Políticas y ajustes de seguridad",
                            icono = Icons.Default.Security,
                            color = Color(0xFF1976D2),
                            onClick = { navController.navigate(AppScreens.Seguridad.route) }
                        )
                        CategoriaCard(
                            titulo = "Tema",
                            descripcion = "Oscuro / Claro",
                            icono = Icons.Default.Brightness6,
                            color = Color(0xFF388E3C),
                            onClick = { navController.navigate(AppScreens.Configuracion.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Correo electrónico",
                            descripcion = "Configurar email de soporte",
                            icono = Icons.Default.Email,
                            color = Color(0xFF00796B),
                            onClick = { navController.navigate(AppScreens.EmailConfig.route) }
                        )
                        CategoriaCard(
                            titulo = "Notificaciones",
                            descripcion = "Configuración de alertas",
                            icono = Icons.Default.Notifications,
                            color = Color(0xFFD32F2F),
                            onClick = { navController.navigate(AppScreens.Notificaciones.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Perfil",
                            descripcion = "Mi perfil de usuario",
                            icono = Icons.Default.Person,
                            color = Color(0xFF5E35B1),
                            onClick = { navController.navigate(AppScreens.Perfil.route) }
                        )
                        Spacer(modifier = Modifier.width(160.dp))
                    }

                    // --- COMUNICACIÓN Y SOPORTE ---
                    SectionHeader(title = "Comunicación y Soporte", icon = Icons.Default.SupportAgent)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Comunicados",
                            descripcion = "Gestión de anuncios y mensajes",
                            icono = Icons.Default.Campaign,
                            color = Color(0xFF1565C0),
                            onClick = { navController.navigate(AppScreens.ComunicadosCirculares.route) }
                        )
                        CategoriaCard(
                            titulo = "Bandeja de entrada",
                            descripcion = "Mensajes recibidos",
                            icono = Icons.Default.Inbox,
                            color = Color(0xFF00838F),
                            onClick = { navController.navigate(AppScreens.BandejaEntrada.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "Nuevo mensaje",
                            descripcion = "Redactar mensaje",
                            icono = Icons.Default.Edit,
                            color = Color(0xFF43A047),
                            onClick = { navController.navigate(AppScreens.ComponerMensaje.route) }
                        )
                        CategoriaCard(
                            titulo = "Soporte técnico",
                            descripcion = "Ayuda y contacto",
                            icono = Icons.Default.SupportAgent,
                            color = Color(0xFF6D4C41),
                            onClick = { navController.navigate(AppScreens.SoporteTecnico.route) }
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoriaCard(
                            titulo = "FAQ",
                            descripcion = "Preguntas frecuentes",
                            icono = Icons.Default.Help,
                            color = Color(0xFF1976D2),
                            onClick = { navController.navigate(AppScreens.FAQ.route) }
                        )
                        CategoriaCard(
                            titulo = "Términos",
                            descripcion = "Términos y condiciones",
                            icono = Icons.Default.Description,
                            color = Color(0xFF757575),
                            onClick = { navController.navigate(AppScreens.TerminosCondiciones.route) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
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
fun WelcomeCardV2(currentDate: String, nombreAdmin: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AcademicoColorDark.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!nombreAdmin.isNullOrBlank()) {
                    Text(
                        text = "Bienvenido/a, $nombreAdmin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Panel de administración",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Panel de administración",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AcademicoColorDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
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
    val usuario: Usuario? = null
)

/**
 * Previsualización profesional del dashboard de administrador.
 */
@Preview(showBackground = true)
@Composable
fun VistaPreviaDashboardAdmin() {
    UmeEguneroTheme {
        AdminDashboardScreen(navController = rememberNavController())
    }
}