package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.centro.viewmodel.CentroDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.LoadingIndicator

/**
 * Pantalla principal del dashboard para centros educativos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroDashboardScreen(
    navController: NavController,
    viewModel: CentroDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estructura básica con Scaffold
    Scaffold(
        topBar = {
            CenterioTopAppBar(
                onNavigateToNotifications = {
                    navController.navigate(AppScreens.Notificaciones.route)
                },
                onNavigateToPerfil = {
                    navController.navigate(AppScreens.Perfil.route)
                }
            )
        }
    ) { paddingValues ->
        
        // Contenido principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar indicador de carga o contenido
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                CentroDashboardContent(
                    navController = navController,
                    nombre = uiState.nombreCentro
                )
            }
        }
    }
}

/**
 * Barra superior del dashboard de centro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CenterioTopAppBar(
    onNavigateToNotifications: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Dashboard Centro",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            // Botón de notificaciones
            IconButton(onClick = onNavigateToNotifications) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones"
                )
            }
            
            // Botón de perfil
            IconButton(onClick = onNavigateToPerfil) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Contenido principal del dashboard de centro con grid de opciones
 */
@Composable
private fun CentroDashboardContent(
    navController: NavController,
    nombre: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tarjeta de bienvenida
        WelcomeCard(nombre = nombre)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Grid de opciones de gestión
        Text(
            text = "Gestión del Centro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Opciones en un grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Lista de opciones del dashboard
            items(dashboardOptions) { option ->
                DashboardOptionItem(
                    option = option,
                    onClick = {
                        when (option.id) {
                            "cursos" -> navController.navigate(AppScreens.GestionCursosYClases.route)
                            "profesores" -> navController.navigate("gestion_profesores")
                            "alumnos" -> navController.navigate("gestion_alumnos")
                            "familias" -> navController.navigate("vinculacion_familiar")
                            "calendario" -> navController.navigate(AppScreens.Calendario.route)
                            "notificaciones" -> navController.navigate("gestion_notificaciones_centro")
                            else -> { /* No hacer nada */ }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Tarjeta de bienvenida con información del centro
 */
@Composable
private fun WelcomeCard(nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Bienvenido/a!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Item para cada opción del dashboard
 */
@Composable
private fun DashboardOptionItem(
    option: DashboardOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Título
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Definición de una opción del dashboard
 */
data class DashboardOption(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String = ""
)

/**
 * Lista de opciones disponibles en el dashboard
 */
private val dashboardOptions = listOf(
    DashboardOption(
        id = "cursos",
        title = "Cursos y Clases",
        icon = Icons.Default.School,
        description = "Gestión de cursos y clases"
    ),
    DashboardOption(
        id = "profesores",
        title = "Profesorado",
        icon = Icons.Default.People,
        description = "Gestión de profesores"
    ),
    DashboardOption(
        id = "alumnos",
        title = "Alumnado",
        icon = Icons.Default.Face,
        description = "Gestión de alumnos"
    ),
    DashboardOption(
        id = "familias",
        title = "Familias",
        icon = Icons.Default.Group,
        description = "Vinculación de familias"
    ),
    DashboardOption(
        id = "calendario",
        title = "Calendario",
        icon = Icons.Default.DateRange,
        description = "Calendario de eventos"
    ),
    DashboardOption(
        id = "notificaciones",
        title = "Notificaciones",
        icon = Icons.Default.Notifications,
        description = "Envío de notificaciones"
    )
) 