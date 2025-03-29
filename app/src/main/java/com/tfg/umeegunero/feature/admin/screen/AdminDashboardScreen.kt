package com.tfg.umeegunero.feature.admin.screen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Contacto
import com.tfg.umeegunero.data.model.Direccion
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.SubtipoFamiliar
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardUiState
import com.tfg.umeegunero.feature.admin.viewmodel.AdminDashboardViewModel
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.ui.theme.getNombreTema
import com.tfg.umeegunero.feature.common.config.components.TemaSelector
import com.tfg.umeegunero.feature.common.config.components.TemaActual
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.navigation.NavigationStructure
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.feature.common.screen.DummyScreen
import com.tfg.umeegunero.navigation.NavigationStructure.NavItem
import androidx.navigation.PopUpToBuilder
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import com.tfg.umeegunero.util.PaginationUtils
import com.tfg.umeegunero.util.AccessibilityUtils.accessibleClickable
import com.tfg.umeegunero.feature.admin.components.PaginatedCentrosList
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import com.tfg.umeegunero.feature.admin.components.StatsOverviewCard
import com.tfg.umeegunero.feature.admin.components.StatsOverviewRow
import com.tfg.umeegunero.feature.admin.components.StatItem
import androidx.compose.material.icons.filled.Warning
import com.tfg.umeegunero.feature.admin.components.UserManagementPanel

/**
 * Dashboard principal para administradores del sistema UmeEgunero.
 * 
 * Esta pantalla funciona como centro de control para los administradores, proporcionando:
 * 
 * - Panel de estadísticas generales (usuarios, centros, actividad)
 * - Acceso rápido a la gestión de centros educativos
 * - Gestión de usuarios del sistema (administradores, profesores, familiares)
 * - Herramientas de configuración y mantenimiento del sistema
 * - Visualización de notificaciones y alertas importantes
 * 
 * El diseño implementa un cajón de navegación lateral (drawer) que permite acceder
 * a todas las funcionalidades administrativas, adaptándose a los permisos del usuario.
 * La interfaz está organizada en secciones con tarjetas informativas y accesos directos
 * a las funciones más utilizadas.
 * 
 * Este dashboard utiliza Material Design 3 con soporte para temas claro/oscuro
 * y se integra con el sistema de navegación principal de la aplicación.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 * @param viewModel ViewModel que gestiona el estado y la lógica del dashboard de administración
 *
 * @see AdminDashboardViewModel Para la lógica de negocio
 * @see NavController Para la navegación
 * @see NavigationStructure Para la estructura de navegación de la aplicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    if (uiState.navigateToWelcome) {
        LaunchedEffect(true) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.AdminDashboard.route) { 
                    inclusive = true 
                }
            }
        }
    }
    
    val navItems = NavigationStructure.getNavItemsByTipo(TipoUsuario.ADMIN_APP)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxSize(),
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                DrawerContent(
                    navItems = navItems,
                    currentUser = currentUser,
                    onNavigate = { route, isImplemented ->
                        val handled = handleNavigation(route, navController, viewModel, isImplemented)
                        if (handled) {
                            scope.launch { drawerState.close() }
                        }
                        handled
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel de Administración") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú",
                                tint = MaterialTheme.colorScheme.onPrimary
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                DashboardContent(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    withDismissAction = true
                )
                viewModel.clearError()
            }
        }
    }

    // Mostrar mensaje de éxito si existe
    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    withDismissAction = true
                )
                viewModel.clearMensajeExito()
            }
        }
    }
}

@Composable
private fun AdminDashboardContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado
        Text(
            text = "Bienvenido al Panel de Administración",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tarjetas de acceso rápido
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccesoRapidoCard(
                title = "Centros",
                icon = Icons.Default.School,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = { /* Navegar a gestión de centros */ },
                modifier = Modifier.weight(1f)
            )
            
            AccesoRapidoCard(
                title = "Usuarios",
                icon = Icons.Default.Person,
                color = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { /* Navegar a gestión de usuarios */ },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccesoRapidoCard(
                title = "Config.",
                icon = Icons.Default.Settings,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { /* Navegar a configuración */ },
                modifier = Modifier.weight(1f)
            )
            
            AccesoRapidoCard(
                title = "Ayuda",
                icon = Icons.Default.Info,
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { /* Mostrar ayuda */ },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Resumen de estadísticas
        Text(
            text = "Resumen del sistema",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        EstadisticasResumen(
            totalCentros = 15,
            totalUsuarios = 324,
            nuevosRegistros = 8,
            alertas = 2
        )
    }
}

@Composable
fun AccesoRapidoCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EstadisticasResumen(
    totalCentros: Int,
    totalUsuarios: Int,
    nuevosRegistros: Int,
    alertas: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Primera fila
        StatsOverviewRow(
            stats = listOf(
                StatItem(
                    title = "Centros",
                    value = totalCentros.toString(),
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.primary
                ),
                StatItem(
                    title = "Usuarios",
                    value = totalUsuarios.toString(),
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        )
        
        // Segunda fila
        StatsOverviewRow(
            stats = listOf(
                StatItem(
                    title = "Nuevos",
                    value = nuevosRegistros.toString(),
                    icon = Icons.Default.Add,
                    color = MaterialTheme.colorScheme.tertiary
                ),
                StatItem(
                    title = "Alertas",
                    value = alertas.toString(),
                    icon = Icons.Default.Warning,
                    color = if (alertas > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardContentPreview() {
    UmeEguneroTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AdminDashboardContent()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AdminDashboardContentDarkPreview() {
    UmeEguneroTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AdminDashboardContent()
        }
    }
}

@Composable
fun DummyScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title - En desarrollo",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CentrosEducativosContentPreview() {
    UmeEguneroTheme {
        val centros = List(10) { index ->
            Centro(
                id = "centro_$index",
                nombre = "Centro Educativo ${index + 1}",
                direccion = Direccion(
                    calle = "Calle Principal $index",
                    ciudad = "Madrid"
                ),
                contacto = Contacto(
                    telefono = "91234567$index",
                    email = "centro$index@educacion.es"
                ),
                activo = index % 3 != 0
            )
        }
        
        Surface(color = MaterialTheme.colorScheme.background) {
            PaginatedCentrosList(
                centros = centros,
                isLoading = false
            )
        }
    }
}

@Composable
fun UsuariosContent(
    viewModel: AdminDashboardViewModel,
    usuarios: List<Usuario>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    UserManagementPanel(
        usuarios = usuarios,
        isLoading = isLoading,
        onDelete = { usuario -> viewModel.deleteUsuario(usuario.dni) },
        onEdit = { /* Navegación a pantalla de edición */ },
        onResetPassword = { dni, password -> viewModel.resetPassword(dni, password) },
        onToggleActive = { usuario, activo -> viewModel.toggleUsuarioActivo(usuario, activo) },
        modifier = modifier
    )
}

@Composable
fun UsuarioItem(
    usuario: Usuario,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${usuario.nombre} ${usuario.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Obtener el tipo de usuario primario
                val tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.FAMILIAR
                val tipoText = when (tipoUsuario) {
                    TipoUsuario.ADMIN_APP -> "Administrador App"
                    TipoUsuario.ADMIN_CENTRO -> "Administrador Centro"
                    TipoUsuario.PROFESOR -> "Profesor"
                    TipoUsuario.FAMILIAR -> "Familiar"
                    else -> "Alumno"
                }

                Text(
                    text = "$tipoText • ${usuario.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botones de acción
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardPreview() {
    UmeEguneroTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AdminDashboardScreen(
                navController = rememberNavController()
            )
        }
    }
}

/**
 * Maneja la navegación hacia diferentes rutas
 */
private fun handleNavigation(
    route: String,
    navController: NavController,
    viewModel: AdminDashboardViewModel,
    isImplemented: Boolean
): Boolean {
    // Si la ruta no está implementada, navegamos a una pantalla dummy
    if (!isImplemented) {
        val title = getRouteTitleForDummy(route)
        navController.navigate(AppScreens.Dummy.createRoute(title))
        return true
    }

    return when (route) {
        AppScreens.AdminDashboard.route -> {
            // Estamos en el dashboard, no hacemos nada
            true
        }
        AppScreens.Cursos.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Clases.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.ProfesorList.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.AlumnoList.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.FamiliarList.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.GestionCentros.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.AddCentro.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Configuracion.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Perfil.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Notificaciones.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.EmailConfig.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.Estadisticas.route -> {
            navController.navigate(route)
            true
        }
        AppScreens.DetalleCentro.route -> {
            // Esta ruta incluye parámetros, por lo que no debe navegarse directamente
            // Se manejará en sus respectivas llamadas
            true
        }
        AppScreens.EditCentro.route -> {
            // Esta ruta incluye parámetros, por lo que no debe navegarse directamente
            // Se manejará en sus respectivas llamadas
            true
        }
        "logout" -> {
            viewModel.logout()
            true
        }
        else -> {
            // Para cualquier otra ruta no manejada, mostramos una pantalla dummy
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
        route.contains("comunicaciones/comunicados") -> "Sistema de Comunicados"
        route.contains("reportes/uso") -> "Estadísticas de Uso de Plataforma"
        route.contains("reportes/rendimiento") -> "Métricas de Rendimiento"
        route.contains("usuarios/administradores") -> "Gestión de Administradores"
        route.contains("configuracion/seguridad") -> "Configuración de Seguridad"
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
    viewModel: AdminDashboardViewModel
) {
    // Utilizamos un estado para saber si estamos mostrando la pantalla de bienvenida o el listado
    val uiState by viewModel.uiState.collectAsState()
    val showListadoCentros = uiState.showListadoCentros
    
    if (!showListadoCentros) {
        // Pantalla de bienvenida
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .semantics { 
                    contentDescription = "Bienvenido al Panel de Administración. Selecciona una opción del menú lateral para comenzar." 
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bienvenido al Panel de Administración",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clearAndSetSemantics {} // Ya incluido en el contenedor principal
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Selecciona una opción del menú lateral para comenzar",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics {} // Ya incluido en el contenedor principal
            )
        }
    } else {
        // Listado de centros
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { 
                    contentDescription = "Panel de administración de centros educativos" 
                }
        ) {
            CentrosListContent(
                navController = navController,
                viewModel = viewModel
            )
            
            // FAB para añadir nuevo centro con soporte de accesibilidad
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(AppScreens.AddCentro.route) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir centro") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .semantics { 
                        contentDescription = "Añadir nuevo centro educativo" 
                        role = Role.Button
                    }
            )
        }
    }
}

@Composable
private fun CentrosListContent(
    navController: NavController,
    viewModel: AdminDashboardViewModel
) {
    val centros by viewModel.centros.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Listado de centros",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .semantics { contentDescription = "Encabezado: Listado de centros" }
            )
            
            // Usamos el componente reutilizable para la lista paginada
            PaginatedCentrosList(
                centros = centros,
                isLoading = uiState.isLoading,
                onCentroClick = { centro ->
                    navController.navigate(AppScreens.DetalleCentro.createRoute(centro.id))
                },
                onDeleteCentro = { centroId ->
                    viewModel.deleteCentro(centroId)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Contenido del drawer de navegación
 */
@Composable
private fun DrawerContent(
    navItems: List<NavItem>,
    currentUser: Usuario?,
    onNavigate: (String, Boolean) -> Boolean,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        // Perfil del usuario
        UserProfileHeader(currentUser)
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de navegación
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(navItems) { navItem ->
                DrawerNavItem(
                    navItem = navItem, 
                    onItemClick = onNavigate
                )
            }
        }
        
        // Botón de cerrar sesión
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        
        NavigationDrawerItem(
            label = { Text("Cerrar Sesión") },
            selected = false,
            onClick = { onNavigate("logout", true) },
            icon = { 
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión"
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun UserProfileHeader(currentUser: Usuario?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar del usuario
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUser?.nombre?.firstOrNull()?.toString() ?: "A",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Nombre del usuario
        Text(
            text = "${currentUser?.nombre ?: "Admin"} ${currentUser?.apellidos ?: ""}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        // Rol del usuario
        Text(
            text = "Administrador del Sistema",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DrawerNavItem(
    navItem: NavItem,
    onItemClick: (String, Boolean) -> Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val hasSubItems = navItem.subItems.isNotEmpty()
    
    Column {
        // Ítem principal
        NavigationDrawerItem(
            label = { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = navItem.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (hasSubItems) {
                        Icon(
                            imageVector = if (expanded) 
                                            Icons.Default.ExpandLess 
                                          else 
                                            Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Contraer" else "Expandir",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            badge = {
                if (navItem.badge != null && navItem.badge > 0) {
                    Badge { Text(text = navItem.badge.toString()) }
                } else if (navItem.description.isNotEmpty() && !hasSubItems) {
                    Text(
                        text = navItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            selected = false,
            onClick = {
                if (hasSubItems) {
                    expanded = !expanded
                    true
                } else {
                    onItemClick(navItem.route, navItem.isImplemented)
                }
            },
            icon = {
                Icon(
                    imageVector = navItem.icon,
                    contentDescription = navItem.title,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        
        // Subitems
        if (expanded && hasSubItems) {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                navItem.subItems.forEach { subItem ->
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = subItem.title,
                                style = MaterialTheme.typography.bodyMedium
                            ) 
                        },
                        badge = {
                            if (subItem.badge != null && subItem.badge > 0) {
                                Badge { Text(text = subItem.badge.toString()) }
                            } else if (subItem.description.isNotEmpty()) {
                                Text(
                                    text = subItem.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        selected = false,
                        onClick = { onItemClick(subItem.route, subItem.isImplemented) },
                        icon = {
                            Icon(
                                imageVector = subItem.icon,
                                contentDescription = subItem.title,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
        
        // Divisor después del ítem si es necesario
        if (navItem.dividerAfter) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}