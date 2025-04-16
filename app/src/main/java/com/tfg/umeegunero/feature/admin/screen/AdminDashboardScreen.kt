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
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
 * @version 2.0
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
            TopAppBar(
                title = {
                    Text(
                        text = "Panel de Administración",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { 
                        viewModel.logout()
                        navController.navigate(AppScreens.Welcome.route) {
                            popUpTo(AppScreens.AdminDashboard.route) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
                AdminDashboardContent(
                    currentDate = currentDate,
                    onNavigateToGestionCentros = { navController.navigate(AppScreens.GestionCentros.route) },
                    onNavigateToAddCentro = { navController.navigate(AppScreens.AddCentro.route) },
                    onNavigateToEstadisticas = { navController.navigate(AppScreens.Estadisticas.route) },
                    onNavigateToReporteUso = { navController.navigate(AppScreens.ReporteUso.route) },
                    onNavigateToSeguridad = { navController.navigate(AppScreens.Seguridad.route) },
                    onNavigateToComunicados = { navController.navigate(AppScreens.ComunicadosCirculares.route) },
                    onNavigateToNotificaciones = { navController.navigate(AppScreens.Notificaciones.route) },
                    onNavigateToPerfil = { navController.navigate(AppScreens.Perfil.route) },
                    modifier = Modifier.padding(paddingValues),
                    navController = navController
                )
            }
        }
    }
}

/**
 * Contenido principal del Dashboard de Administración
 */
@Composable
fun AdminDashboardContent(
    currentDate: String,
    onNavigateToGestionCentros: () -> Unit,
    onNavigateToAddCentro: () -> Unit,
    onNavigateToEstadisticas: () -> Unit,
    onNavigateToReporteUso: () -> Unit,
    onNavigateToSeguridad: () -> Unit,
    onNavigateToComunicados: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tarjeta de bienvenida
        AdminWelcomeCard(currentDate = currentDate)
        
        // Título para sección de gestión de centros
        Text(
            text = "Gestión de Centros",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        
        // Opciones de gestión de centros en formato lista
        AdminOptionCard(
            title = "Ver Centros",
            description = "Consultar listado de centros educativos",
            icon = Icons.Default.Business,
            color = MaterialTheme.colorScheme.primary,
            onClick = onNavigateToGestionCentros
        )
        
        AdminOptionCard(
            title = "Añadir Centro",
            description = "Crear nuevo centro educativo",
            icon = Icons.Default.Add,
            color = MaterialTheme.colorScheme.secondary,
            onClick = onNavigateToAddCentro
        )

        AdminOptionCard(
            title = "Gestión de Cursos",
            description = "Administrar cursos académicos",
            icon = Icons.Default.School,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = { navController.navigate(AppScreens.ListaCursos.route) }
        )
        
        AdminOptionCard(
            title = "Gestión de Clases",
            description = "Administrar clases y aulas",
            icon = Icons.Default.Group,
            color = MaterialTheme.colorScheme.errorContainer,
            onClick = { navController.navigate(AppScreens.ListaClases.route) }
        )
        
        // Título para sección de gestión de usuarios
        Text(
            text = "Gestión de Usuarios",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        
        // Opciones para gestión de usuarios
        AdminOptionCard(
            title = "Crear Usuario",
            description = "Añadir administradores, profesores o familiares",
            icon = Icons.Default.PersonAdd,
            color = MaterialTheme.colorScheme.primary,
            onClick = { navController.navigate(AppScreens.AddUser.createRoute(true)) }
        )
        
        AdminOptionCard(
            title = "Listado de Usuarios",
            description = "Ver todos los usuarios del sistema",
            icon = Icons.Default.People,
            color = MaterialTheme.colorScheme.secondary,
            onClick = { navController.navigate(AppScreens.GestionProfesores.route) }
        )
        
        // Título para sección de vinculaciones
        Text(
            text = "Vinculaciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        
        // Opciones para vinculaciones
        AdminOptionCard(
            title = "Profesores - Clases",
            description = "Asignar profesores a clases",
            icon = Icons.Default.School,
            color = Color(0xFF009688),
            onClick = { navController.navigate(AppScreens.VincularProfesorClase.route) }
        )
        
        AdminOptionCard(
            title = "Familiares - Alumnos",
            description = "Vincular familiares con alumnos",
            icon = Icons.Default.ChildCare,
            color = Color(0xFF2196F3),
            onClick = { navController.navigate(AppScreens.VincularAlumnoFamiliar.route) }
        )
        
        // Título para sección de análisis
        Text(
            text = "Análisis y Reportes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        
        // Opciones de análisis en formato lista vertical
        AdminOptionCard(
            title = "Estadísticas",
            description = "Datos generales del sistema",
            icon = Icons.Default.PieChart,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onNavigateToEstadisticas
        )
        
        AdminOptionCard(
            title = "Uso del Sistema",
            description = "Métricas de uso y actividad",
            icon = Icons.Default.Timeline,
            color = Color(0xFFE65100),
            onClick = onNavigateToReporteUso
        )
        
        AdminOptionCard(
            title = "Seguridad",
            description = "Configuración de accesos y permisos",
            icon = Icons.Default.Security,
            color = Color(0xFF7B1FA2),
            onClick = onNavigateToSeguridad
        )
        
        // Título para sección de comunicación
        Text(
            text = "Comunicación",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        
        // Opciones de comunicación en formato lista
        AdminOptionCard(
            title = "Comunicados y circulares",
            description = "Gestión de anuncios y mensajes generales",
            icon = Icons.Default.Campaign,
            color = Color(0xFF1565C0),
            onClick = onNavigateToComunicados
        )
        
        AdminOptionCard(
            title = "Notificaciones",
            description = "Configuración de alertas del sistema",
            icon = Icons.Default.Notifications,
            color = Color(0xFFD32F2F),
            onClick = onNavigateToNotificaciones
        )
        
        AdminOptionCard(
            title = "Mantenimiento de Soporte",
            description = "Configuración de email de soporte técnico",
            icon = Icons.Default.Email,
            color = Color(0xFF00796B),
            onClick = { navController.navigate(AppScreens.EmailConfigSoporte.route) }
        )
        
        // Perfil
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onNavigateToPerfil,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mi Perfil")
        }
        
        // Espaciador final para scroll
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Tarjeta de bienvenida para el dashboard de administración
 */
@Composable
fun AdminWelcomeCard(currentDate: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información de bienvenida
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Panel de Administración",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            // Icono decorativo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta de opción para el dashboard de administración
 */
@Composable
fun AdminOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Textos
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Flecha indicadora para la navegación
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
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

@Preview(showBackground = true)
@Composable
fun AdminDashboardContentPreview() {
    UmeEguneroTheme {
        Surface {
            AdminDashboardContent(
                currentDate = "Lunes, 10 de abril",
                onNavigateToGestionCentros = {},
                onNavigateToAddCentro = {},
                onNavigateToEstadisticas = {},
                onNavigateToReporteUso = {},
                onNavigateToSeguridad = {},
                onNavigateToComunicados = {},
                onNavigateToNotificaciones = {},
                onNavigateToPerfil = {},
                navController = rememberNavController()
            )
        }
    }
}