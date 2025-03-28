package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.navigation.NavigationStructure
import kotlinx.coroutines.launch

/**
 * Componente que integra el ViewModel con la pantalla de dashboard del profesor
 *
 * Este componente sirve como puente entre el ViewModel y la UI, permitiendo
 * la inyección de dependencias a través de Hilt y proporcionando los datos
 * necesarios a la pantalla de dashboard del profesor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiltProfesorDashboardScreen(
    navController: NavHostController,
    viewModel: ProfesorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.profesor
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    if (uiState.navigateToWelcome) {
        LaunchedEffect(true) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.ProfesorDashboard.route) { 
                    inclusive = true 
                }
            }
        }
    }
    
    val navItems = NavigationStructure.getNavItemsByTipo(TipoUsuario.PROFESOR)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxSize(),
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Spacer(modifier = Modifier.height(24.dp)) // Espacio para evitar el notch
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
                CenterAlignedTopAppBar(
                    title = { Text("Panel de Profesor") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ProfesorHomeContent(
                        alumnosPendientes = uiState.alumnosPendientes,
                        onCrearRegistroActividad = viewModel::crearRegistroActividad,
                        navController = navController
                    )
                }
            }
        }
    }
}

/**
 * Función para manejar la navegación desde el menú lateral
 */
private fun handleNavigation(
    route: String,
    navController: NavHostController,
    viewModel: ProfesorDashboardViewModel,
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
        "profesor_dashboard" -> {
            // Estamos en el dashboard, no hacemos nada
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
        AppScreens.Notificaciones.route -> {
            navController.navigate(route)
            true
        }
        "logout" -> {
            viewModel.logout()
            true
        }
        else -> {
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
        route.contains("clases") -> "Mis Clases"
        route.contains("alumnos") -> "Mis Alumnos"
        route.contains("comunicaciones") -> "Comunicaciones"
        route.contains("perfil") -> "Mi Perfil"
        else -> "Funcionalidad en Desarrollo"
    }
}

/**
 * Contenido principal de la pantalla del profesor
 */
@Composable
fun ProfesorHomeContent(
    alumnosPendientes: Int,
    onCrearRegistroActividad: () -> Unit,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tarjetas de información
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                title = "Alumnos Pendientes",
                value = alumnosPendientes.toString(),
                icon = Icons.Default.Person,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.Dummy.createRoute("Alumnos Pendientes"))
                    }
            )
            
            InfoCard(
                title = "Asistencia",
                value = "Pendiente",
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.Dummy.createRoute("Registro de Asistencia"))
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Segunda fila de tarjetas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                title = "Tareas",
                value = "2 Pendientes",
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.Dummy.createRoute("Tareas Pendientes"))
                    }
            )
            
            InfoCard(
                title = "Eventos",
                value = "Próximamente",
                icon = Icons.Default.CalendarToday,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.Dummy.createRoute("Calendario de Eventos"))
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Acciones rápidas
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Registrar Asistencia",
                icon = Icons.Default.CheckCircle,
                onClick = {
                    navController.navigate(AppScreens.Dummy.createRoute("Registrar Asistencia"))
                }
            )
            
            ActionButton(
                text = "Enviar Mensaje a Padres",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = {
                    navController.navigate(AppScreens.Dummy.createRoute("Mensajes a Padres"))
                }
            )
            
            ActionButton(
                text = "Crear Actividad",
                icon = Icons.Default.Add,
                onClick = onCrearRegistroActividad
            )
        }
    }
}

/**
 * Tarjeta de información con título, valor e icono
 */
@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Botón de acción con texto e icono
 */
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Contenido del drawer de navegación
 */
@Composable
fun DrawerContent(
    navItems: List<NavigationStructure.NavItem>,
    currentUser: Usuario?,
    onNavigate: (String, Boolean) -> Boolean,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera del drawer con información del usuario
        DrawerHeader(currentUser)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de opciones de navegación
        DrawerBody(
            items = navItems,
            onNavigate = onNavigate
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botón de cerrar drawer (opcional)
        TextButton(
            onClick = onCloseDrawer,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cerrar menú")
        }
    }
}

/**
 * Cabecera del drawer con información del usuario
 */
@Composable
fun DrawerHeader(
    usuario: Usuario?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.large
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = usuario?.nombre ?: "Usuario",
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = usuario?.email ?: "email@example.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Cuerpo del drawer con lista de opciones de navegación
 */
@Composable
fun DrawerBody(
    items: List<NavigationStructure.NavItem>,
    onNavigate: (String, Boolean) -> Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                selected = false,
                onClick = { onNavigate(item.route, item.isImplemented) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
