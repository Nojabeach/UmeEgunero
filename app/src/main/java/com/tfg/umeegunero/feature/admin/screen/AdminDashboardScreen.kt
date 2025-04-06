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
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")) 
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
                    onNavigateToReporteRendimiento = { navController.navigate(AppScreens.ReporteRendimiento.route) },
                    onNavigateToSeguridad = { navController.navigate(AppScreens.Config.route) },
                    onNavigateToComunicados = { navController.navigate(AppScreens.Comunicados.route) },
                    onNavigateToNotificaciones = { navController.navigate(AppScreens.Notificaciones.route) },
                    onNavigateToPerfil = { navController.navigate(AppScreens.Perfil.route) },
                    modifier = Modifier.padding(paddingValues)
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
    onNavigateToReporteRendimiento: () -> Unit,
    onNavigateToSeguridad: () -> Unit,
    onNavigateToComunicados: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de bienvenida
        AdminWelcomeCard(currentDate = currentDate)
        
        // Título para sección de gestión de centros
        Text(
            text = "Gestión de Centros",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        
        // Opciones de gestión de centros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdminOptionCard(
                title = "Ver Centros",
                description = "Consultar listado",
                icon = Icons.Default.Business,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToGestionCentros
            )
            
            AdminOptionCard(
                title = "Añadir Centro",
                description = "Crear nuevo centro",
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAddCentro
            )
        }
        
        // Título para sección de análisis
        Text(
            text = "Análisis y Reportes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        
        // Opciones de análisis
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false,
            modifier = Modifier.height(200.dp)
        ) {
            item {
                AdminOptionCard(
                    title = "Estadísticas",
                    description = "Datos generales",
                    icon = Icons.Default.PieChart,
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = onNavigateToEstadisticas
                )
            }
            
            item {
                AdminOptionCard(
                    title = "Uso del Sistema",
                    description = "Métricas de uso",
                    icon = Icons.Default.Timeline,
                    color = Color(0xFFE65100),
                    onClick = onNavigateToReporteUso
                )
            }
            
            item {
                AdminOptionCard(
                    title = "Rendimiento",
                    description = "Métricas académicas",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF00897B),
                    onClick = onNavigateToReporteRendimiento
                )
            }
            
            item {
                AdminOptionCard(
                    title = "Seguridad",
                    description = "Accesos y permisos",
                    icon = Icons.Default.Security,
                    color = Color(0xFF7B1FA2),
                    onClick = onNavigateToSeguridad
                )
            }
        }
        
        // Título para sección de comunicación
        Text(
            text = "Comunicación",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        
        // Opciones de comunicación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdminOptionCard(
                title = "Comunicados",
                description = "Anuncios generales",
                icon = Icons.Default.Campaign,
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToComunicados
            )
            
            AdminOptionCard(
                title = "Notificaciones",
                description = "Alertas del sistema",
                icon = Icons.Default.Notifications,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToNotificaciones
            )
        }
        
        // Perfil
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onNavigateToPerfil,
            modifier = Modifier.fillMaxWidth(),
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
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Textos
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                onNavigateToReporteRendimiento = {},
                onNavigateToSeguridad = {},
                onNavigateToComunicados = {},
                onNavigateToNotificaciones = {},
                onNavigateToPerfil = {}
            )
        }
    }
}