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
import com.tfg.umeegunero.feature.admin.screen.components.CategoryCard
import com.tfg.umeegunero.feature.admin.screen.components.ActionButton

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
                    containerColor = AdminColor,
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
                CircularProgressIndicator(color = AdminColor)
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
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta de bienvenida
                    WelcomeCard(currentDate = currentDate)
                    
                    // Sección de accesos rápidos
                    SectionHeader(
                        title = "Accesos Rápidos",
                        icon = Icons.Default.Dashboard
                    )
                    
                    QuickActionsGrid(
                        onGestionCentros = { navController.navigate(AppScreens.GestionCentros.route) },
                        onAddCentro = { navController.navigate(AppScreens.AddCentro.route) },
                        onGestionCursos = { navController.navigate(AppScreens.GestionCursos.route) },
                        onGestionClases = {
                            // Navegación a la pantalla de clases con selector editable
                            navController.navigate("gestion_clases/0?centroId=&selectorCursoBloqueado=false")
                        },
                        onGestionUsuarios = { navController.navigate(AppScreens.GestionUsuarios.route) },
                        onAddUsuario = { navController.navigate(AppScreens.AddUser.createRoute(true)) }
                    )
                    
                    // Sección de vinculaciones
                    SectionHeader(
                        title = "Vinculaciones",
                        icon = Icons.Default.Link
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CategoryCard(
                            title = "Profesores - Clases",
                            description = "Asignar profesores a clases",
                            icon = Icons.Default.School,
                            color = Color(0xFF009688),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(AppScreens.VincularProfesorClase.route) }
                        )
                        
                        CategoryCard(
                            title = "Familiares - Alumnos",
                            description = "Vincular familiares con alumnos",
                            icon = Icons.Default.ChildCare,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(AppScreens.VincularAlumnoFamiliar.route) }
                        )
                    }
                    
                    // Sección de análisis y reportes
                    SectionHeader(
                        title = "Análisis y Reportes",
                        icon = Icons.Default.Assessment
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CategoryCard(
                            title = "Estadísticas",
                            description = "Datos generales del sistema",
                            icon = Icons.Default.PieChart,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(AppScreens.Estadisticas.route) }
                        )
                        
                        CategoryCard(
                            title = "Uso del Sistema",
                            description = "Métricas de uso y actividad",
                            icon = Icons.Default.Timeline,
                            color = Color(0xFFE65100),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(AppScreens.ReporteUso.route) }
                        )
                    }
                    
                    CategoryCard(
                        title = "Seguridad",
                        description = "Configuración de accesos y permisos",
                        icon = Icons.Default.Security,
                        color = Color(0xFF7B1FA2),
                        onClick = { navController.navigate(AppScreens.Seguridad.route) }
                    )
                    
                    // Sección de comunicación
                    SectionHeader(
                        title = "Comunicación",
                        icon = Icons.Default.Chat
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CategoryCard(
                            title = "Comunicados",
                            description = "Gestión de anuncios y mensajes",
                            icon = Icons.Default.Campaign,
                            color = Color(0xFF1565C0),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(AppScreens.ComunicadosCirculares.route) }
                        )
                        
                        CategoryCard(
                            title = "Notificaciones",
                            description = "Configuración de alertas",
                            icon = Icons.Default.Notifications,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(AppScreens.Notificaciones.route) }
                        )
                    }
                    
                    CategoryCard(
                        title = "Soporte Técnico",
                        description = "Configuración de email de soporte",
                        icon = Icons.Default.Email,
                        color = Color(0xFF00796B),
                        onClick = { navController.navigate(AppScreens.EmailConfigSoporte.route) }
                    )
                    
                    // Espaciador final para scroll
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onGestionCentros: () -> Unit,
    onAddCentro: () -> Unit,
    onGestionCursos: () -> Unit,
    onGestionClases: () -> Unit,
    onGestionUsuarios: () -> Unit,
    onAddUsuario: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
        modifier = Modifier.height(240.dp)
    ) {
        item {
            ActionButton(
                icon = Icons.Default.Business,
                text = "Centros",
                onClick = onGestionCentros
            )
        }
        
        item {
            ActionButton(
                icon = Icons.Default.AddBusiness,
                text = "Añadir Centro",
                onClick = onAddCentro
            )
        }
        
        item {
            ActionButton(
                icon = Icons.Default.School,
                text = "Cursos",
                onClick = onGestionCursos
            )
        }
        
        item {
            ActionButton(
                icon = Icons.Default.Groups,
                text = "Clases",
                onClick = onGestionClases
            )
        }
        
        item {
            ActionButton(
                icon = Icons.Default.People,
                text = "Usuarios",
                onClick = onGestionUsuarios
            )
        }
        
        item {
            ActionButton(
                icon = Icons.Default.PersonAdd,
                text = "Añadir Usuario",
                onClick = onAddUsuario
            )
        }
    }
}

/**
 * Tarjeta de bienvenida para el dashboard de administración
 */
@Composable
fun WelcomeCard(currentDate: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AdminColor.copy(alpha = 0.15f)
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
            // Información de bienvenida
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Bienvenido/a",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Panel de Administración",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Icono decorativo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AdminColor),
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
    val showListadoCentros: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    UmeEguneroTheme {
        AdminDashboardScreen(navController = rememberNavController())
    }
}