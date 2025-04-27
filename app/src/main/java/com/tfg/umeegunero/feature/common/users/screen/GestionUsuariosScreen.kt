package com.tfg.umeegunero.feature.common.users.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.feature.common.users.viewmodel.GestionUsuariosViewModel
import com.tfg.umeegunero.feature.common.users.viewmodel.GestionUsuariosUiState
import androidx.navigation.NavController

/**
 * Pantalla central para la gestión de usuarios del sistema
 * Permite acceder a las diferentes categorías de usuarios según el perfil del administrador
 * 
 * @param viewModel ViewModel que gestiona la lógica de negocio
 * @param navController NavController para la navegación
 * @param isAdminApp Indica si el usuario es administrador de la aplicación
 * @param onNavigateBack Callback para navegar hacia atrás
 * @param onNavigateToUserList Callback para navegar a la lista de usuarios de un tipo específico
 * @param onNavigateToAddUser Callback para navegar a la pantalla de añadir usuario
 * @param onNavigateToProfile Callback para navegar a la pantalla de perfil del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    viewModel: GestionUsuariosViewModel = hiltViewModel(),
    navController: NavController,
    isAdminApp: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onNavigateToUserList: (String) -> Unit = {},
    onNavigateToAddUser: (Boolean) -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.usuarioActual.collectAsState()

    val handleNavigateToUserList: (String) -> Unit = { route ->
        navController.navigate(route)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Outlined.ManageAccounts,
                            contentDescription = "Editar perfil"
                        )
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddUser(isAdminApp) },
                icon = { Icon(Icons.Default.Add, "Añadir") },
                text = { Text("Nuevo Usuario") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is GestionUsuariosUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is GestionUsuariosUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: ${(uiState as GestionUsuariosUiState.Error).message}")
                    }
                }
                is GestionUsuariosUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Selecciona una categoría de usuarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Solo mostrar Administradores si es admin_app
                        if (isAdminApp) {
                            UserCategoryCard(
                                title = "Administradores",
                                subtitle = "Gestión de administradores del sistema",
                                icon = Icons.Default.AdminPanelSettings,
                                onClick = { handleNavigateToUserList(AppScreens.AdminList.route) }
                            )
                        }
                        
                        // Administradores de Centro
                        UserCategoryCard(
                            title = "Administradores de Centro",
                            subtitle = "Gestión de administradores de centros educativos",
                            icon = Icons.Default.Business,
                            onClick = { handleNavigateToUserList(AppScreens.AdminCentroList.route) }
                        )
                        
                        // Profesores
                        UserCategoryCard(
                            title = "Profesores",
                            subtitle = "Gestión de profesores",
                            icon = Icons.Default.School,
                            onClick = { handleNavigateToUserList(AppScreens.ProfesorList.route) }
                        )
                        
                        // Alumnos
                        UserCategoryCard(
                            title = "Alumnos",
                            subtitle = "Gestión de alumnos",
                            icon = Icons.Default.Person,
                            onClick = { handleNavigateToUserList(AppScreens.AlumnoList.route) }
                        )
                        
                        // Familiares
                        UserCategoryCard(
                            title = "Familiares",
                            subtitle = "Gestión de familiares",
                            icon = Icons.Default.People,
                            onClick = { handleNavigateToUserList(AppScreens.FamiliarList.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCategoryCard(
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