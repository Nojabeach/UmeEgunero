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
import timber.log.Timber
import kotlinx.coroutines.launch

/**
 * Pantalla central para la gestión de usuarios del sistema
 * Permite acceder a las diferentes categorías de usuarios según el perfil del administrador
 * 
 * @param viewModel ViewModel que gestiona la lógica de negocio
 * @param navController NavController para la navegación
 * @param onNavigateBack Callback para navegar hacia atrás
 * @param onNavigateToUserList Callback para navegar a la lista de usuarios de un tipo específico
 * @param onNavigateToAddUser Callback para navegar a la pantalla de añadir usuario
 * @param onNavigateToProfile Callback para navegar a la pantalla de perfil del usuario
 * @param isAdminApp Indica si el usuario actual es un administrador de aplicación (parámetro de respaldo)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    viewModel: GestionUsuariosViewModel = hiltViewModel(),
    navController: NavController,
    onNavigateBack: () -> Unit = {},
    onNavigateToUserList: (String) -> Unit = {},
    onNavigateToAddUser: (Boolean) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    isAdminApp: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.usuarioActual.collectAsState()
    val centroIdAdmin by viewModel.centroIdAdmin.collectAsState()
    
    // Añadir snackbarHostState y scope
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Determinar si es admin de aplicación basándose en los perfiles del usuario actual
    // Si el usuario tiene el perfil de ADMIN_APP, se muestra la interfaz para administradores de aplicación
    // Si no, se usa el parámetro isAdminApp como respaldo
    val esAdminApp = currentUser?.perfiles?.any { it.tipo == TipoUsuario.ADMIN_APP } ?: isAdminApp
    
    // Log para debugging
    LaunchedEffect(currentUser) {
        Timber.d("Usuario actual: ${currentUser?.nombre} ${currentUser?.apellidos}")
        Timber.d("Es admin de app: $esAdminApp")
        Timber.d("Perfiles: ${currentUser?.perfiles?.map { it.tipo }}")
    }

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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddUser(esAdminApp) },
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
                        
                        if (esAdminApp) {
                            // Interfaz para administrador de aplicación
                            Text(
                                text = "Administración del Sistema",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            UserCategoryCard(
                                title = "Administradores",
                                subtitle = "Gestión de administradores del sistema",
                                icon = Icons.Default.AdminPanelSettings,
                                onClick = { handleNavigateToUserList(AppScreens.AdminList.route) }
                            )
                            
                            UserCategoryCard(
                                title = "Administradores de Centro",
                                subtitle = "Gestión de administradores de centros educativos",
                                icon = Icons.Default.Business,
                                onClick = { handleNavigateToUserList(AppScreens.AdminCentroList.route) }
                            )
                        } else {
                            // Interfaz para administrador de centro
                            Text(
                                text = "Gestión de Personal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            // Profesores
                            UserCategoryCard(
                                title = "Profesores",
                                subtitle = "Gestión de profesores",
                                icon = Icons.Default.School,
                                onClick = { 
                                    // Opción 1: Navegar a la lista de profesores
                                    // Asegurarse de que centroIdAdmin no esté vacío antes de navegar
                                    if (centroIdAdmin.isNotBlank()) {
                                        handleNavigateToUserList(AppScreens.ProfesorList.createRoute(centroIdAdmin))
                                    } else {
                                        // Mostrar error si no se puede navegar (Admin Centro sin centroId)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Error: No se pudo determinar el centro para ver profesores.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        Timber.e("Error al navegar a ProfesorList desde GestionUsuarios: centroIdAdmin está vacío.")
                                    }
                                    
                                    // Opción alternativa si preferimos navegar directamente a añadir profesor:
                                    // navController.navigate(AppScreens.AddUser.createRoute(
                                    //     isAdminApp = esAdminApp, 
                                    //     tipoUsuario = TipoUsuario.PROFESOR.toString()
                                    // ))
                                }
                            )
                            
                            Text(
                                text = "Comunidad Educativa",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp)
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