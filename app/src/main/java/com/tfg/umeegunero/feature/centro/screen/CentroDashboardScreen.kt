package com.tfg.umeegunero.feature.centro.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.feature.centro.viewmodel.CentroDashboardViewModel
import com.tfg.umeegunero.ui.components.TemaSelector
import com.tfg.umeegunero.ui.components.TemaActual
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.navigation.NavigationStructure
import com.tfg.umeegunero.feature.common.screen.DummyScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroDashboardScreen(
    navController: NavController,
    viewModel: CentroDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    if (uiState.navigateToWelcome) {
        LaunchedEffect(true) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.CentroDashboard.route) { 
                    inclusive = true 
                }
            }
        }
    }
    
    val navItems = NavigationStructure.getNavItemsByTipo(TipoUsuario.ADMIN_CENTRO)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navItems = navItems,
                currentUser = currentUser,
                onNavigate = { route, isImplemented ->
                    handleNavigation(route, navController, viewModel, isImplemented)
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Panel de Centro") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF007AFF),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                DashboardContent(
                    navController = navController,
                    viewModel = viewModel,
                    currentUser = currentUser
                )
            }
        }
    }
}

/**
 * Función para manejar la navegación desde el menú lateral
 */
private fun handleNavigation(
    route: String,
    navController: NavController,
    viewModel: CentroDashboardViewModel,
    isImplemented: Boolean = true
): Boolean {
    // Primero verificamos si la opción está implementada
    if (!isImplemented) {
        // Si no está implementada, navegamos a la pantalla dummy
        val title = getRouteTitleForDummy(route)
        navController.navigate(AppScreens.Dummy.createRoute(title))
        return true
    }

    // Para rutas implementadas
    return when (route) {
        "centro_dashboard" -> {
            // Estamos en el dashboard, no hacemos nada
            true
        }
        "admin_dashboard/cursos" -> {
            navController.navigate(route)
            true
        }
        "admin_dashboard/clases" -> {
            navController.navigate(route)
            true
        }
        "admin_dashboard/profesores" -> {
            navController.navigate(AppScreens.GestionProfesores.route)
            true
        }
        "admin_dashboard/alumnos" -> {
            navController.navigate(route)
            true
        }
        "admin_dashboard/familiares" -> {
            navController.navigate(route)
            true
        }
        AppScreens.GestionCursosYClases.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.GestionNotificacionesCentro.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Config.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Perfil.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Calendario.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Estadisticas.route -> {
            navController.navigate(AppScreens.Estadisticas.route)
            true
        }
        AppScreens.Notificaciones.route -> {
            navController.navigate(route)
            true
        }
        "logout" -> {
            viewModel.logout()
            true
        }
        else -> {
            // Para otras rutas que existen pero no son manejadas específicamente
            if (route.startsWith("add_user") || route.contains("curso") || route.contains("clase")) {
                navController.navigate(route)
                return true
            }
            
            // Si no sabemos manejar la ruta, vamos a la pantalla dummy
            val title = getRouteTitleForDummy(route)
            navController.navigate(AppScreens.Dummy.createRoute(title))
            true
        }
    }
}

/**
 * Obtiene el título para la pantalla dummy basado en la ruta
 */
private fun getRouteTitleForDummy(route: String): String {
    return when {
        route.contains("cursos") -> "Gestión de Cursos"
        route.contains("clases") -> "Gestión de Clases"
        route.contains("calendario") -> "Calendario Escolar"
        route.contains("profesores") -> "Listado de Profesores"
        route.contains("asignar_profesores") -> "Asignar Profesores"
        route.contains("alumnos") -> "Listado de Alumnos"
        route.contains("asignar_alumnos") -> "Asignar Alumnos"
        route.contains("add_alumno") -> "Añadir Alumno"
        route.contains("solicitudes_vinculacion") -> "Solicitudes Pendientes"
        route.contains("familiares_vinculados") -> "Familiares Vinculados"
        route.contains("gestionar_vinculaciones") -> "Gestionar Vinculaciones"
        route.contains("vinculaciones") -> "Gestión de Vinculaciones"
        route.contains("hijos") -> "Gestión de Mis Hijos"
        route.contains("comunicaciones/bandeja") -> "Bandeja de Entrada"
        route.contains("comunicaciones/comunicados") -> "Comunicados"
        route.contains("comunicaciones/mensajes") -> "Mensajes Directos"
        route.contains("admins_centro") -> "Administradores del Centro"
        route.contains("config_centro") -> "Configuración del Centro"
        route.contains("permisos_roles") -> "Permisos y Roles"
        route.contains("datos_personales") -> "Datos Personales"
        route.contains("cambiar_password") -> "Cambiar Contraseña"
        else -> "Funcionalidad en Desarrollo"
    }
}

/**
 * Contenido principal del dashboard
 */
@Composable
private fun DashboardContent(
    navController: NavController,
    viewModel: CentroDashboardViewModel,
    currentUser: Usuario?
) {
    val cursos by viewModel.cursos.collectAsState(initial = emptyList())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sección de Acciones Rápidas
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Botones de acciones rápidas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Group,
                text = "Gestionar\nCursos y Clases",
                onClick = {
                    navController.navigate(AppScreens.GestionCursosYClases.route)
                },
                modifier = Modifier.weight(1f)
            )
            
            ActionButton(
                icon = Icons.Default.Notifications,
                text = "Gestionar\nNotificaciones",
                onClick = {
                    navController.navigate(AppScreens.GestionNotificacionesCentro.route)
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Lista de Cursos
        Text(
            text = "Cursos del Centro",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (cursos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay cursos disponibles")
            }
        } else {
            LazyColumn {
                items(cursos) { curso ->
                    CursoListItem(
                        curso = curso,
                        onEditClick = {
                            val centroId = "centro1" // Valor temporal
                            navController.navigate(AppScreens.Dummy.createRoute("Editar Curso: ${curso.nombre}"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .height(120.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Contenido del drawer de navegación
 */
@Composable
private fun DrawerContent(
    navItems: List<NavigationStructure.NavItem>,
    currentUser: Usuario?,
    onNavigate: (String, Boolean) -> Boolean,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
    ) {
        // Cabecera con información del usuario
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Administrador de Centro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = currentUser?.email ?: "Usuario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Divider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Lista de items de navegación
        LazyColumn {
            items(navItems) { item ->
                DrawerNavItem(
                    navItem = item,
                    onItemClick = { route, isImplemented ->
                        val handled = onNavigate(route, isImplemented)
                        if (handled) {
                            onCloseDrawer()
                        }
                    }
                )
                
                if (item.dividerAfter) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

/**
 * Item de navegación en el drawer
 */
@Composable
private fun DrawerNavItem(
    navItem: NavigationStructure.NavItem,
    onItemClick: (String, Boolean) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    
    Column {
        Surface(
            onClick = {
                if (navItem.subItems.isEmpty()) {
                    onItemClick(navItem.route, navItem.isImplemented)
                } else {
                    isExpanded.value = !isExpanded.value
                }
            },
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = navItem.icon,
                    contentDescription = navItem.title,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = navItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                
                if (navItem.badge != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(text = navItem.badge.toString())
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                if (navItem.subItems.isNotEmpty()) {
                    Icon(
                        imageVector = if (isExpanded.value) 
                            Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded.value) 
                            "Colapsar" else "Expandir"
                    )
                }
            }
        }
        
        // Mostrar subitems si está expandido
        if (isExpanded.value && navItem.subItems.isNotEmpty()) {
            navItem.subItems.forEach { subItem ->
                Surface(
                    onClick = { onItemClick(subItem.route, subItem.isImplemented) },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 56.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = subItem.icon,
                            contentDescription = subItem.title,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = subItem.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (subItem.badge != null) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(text = subItem.badge.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CursoListItem(
    curso: Curso,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = curso.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Edad: ${curso.edadMinima} - ${curso.edadMaxima} años",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Año académico: ${curso.anioAcademico}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = onEditClick,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "Editar",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CentroDashboardPreview() {
    UmeEguneroTheme {
        CentroDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CentroDashboardDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        CentroDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionCentroPreview() {
    UmeEguneroTheme {
        ConfiguracionScreen(
            perfil = PerfilConfiguracion.CENTRO
        )
    }
}