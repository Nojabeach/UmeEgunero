package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.feature.admin.viewmodel.GestionUsuariosViewModel

/**
 * Pantalla central para la gestión de usuarios del sistema
 * Permite acceder a las diferentes categorías de usuarios: administradores, profesores, alumnos y familiares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    navController: NavController,
    viewModel: GestionUsuariosViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    navController.navigate(AppScreens.AddUser.createRoute(isAdminApp = true))
                },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Nuevo Usuario") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selecciona una categoría de usuarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Administradores
            UserCategoryCard(
                title = "Administradores",
                subtitle = "Gestión de administradores del sistema",
                icon = Icons.Default.AdminPanelSettings,
                onClick = {
                    navController.navigate(AppScreens.AdminList.route)
                }
            )
            
            // Administradores de Centro
            UserCategoryCard(
                title = "Administradores de Centro",
                subtitle = "Gestión de administradores de centros educativos",
                icon = Icons.Default.Business,
                onClick = {
                    navController.navigate(AppScreens.AdminCentroList.route)
                }
            )
            
            // Profesores
            UserCategoryCard(
                title = "Profesores",
                subtitle = "Gestión de profesores",
                icon = Icons.Default.School,
                onClick = {
                    navController.navigate(AppScreens.ProfesorList.route)
                }
            )
            
            // Alumnos
            UserCategoryCard(
                title = "Alumnos",
                subtitle = "Gestión de alumnos",
                icon = Icons.Default.Person,
                onClick = {
                    navController.navigate(AppScreens.AlumnoList.route)
                }
            )
            
            // Familiares
            UserCategoryCard(
                title = "Familiares",
                subtitle = "Gestión de familiares",
                icon = Icons.Default.People,
                onClick = {
                    navController.navigate(AppScreens.FamiliarList.route)
                }
            )
        }
    }
}

@Composable
fun UserCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir a $title",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 