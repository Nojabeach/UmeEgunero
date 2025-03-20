package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.navigation.NavigationStructure
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiltFamiliarDashboardScreen(
    navController: NavController,
    viewModel: FamiliarDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.familiar
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    if (uiState.navigateToWelcome) {
        LaunchedEffect(true) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.FamiliarDashboard.route) { 
                    inclusive = true 
                }
            }
        }
    }
    
    val navItems = NavigationStructure.getNavItemsByTipo(TipoUsuario.FAMILIAR)
    
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
                    title = { Text("Panel Familiar") },
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
                    FamiliarHomeContent(
                        hijoSeleccionado = uiState.hijoSeleccionado,
                        registrosActividad = uiState.registrosActividad,
                        onNavigateToDetalleRegistro = { /* Implementar navegación */ },
                        onMarcarRegistroComoVisto = viewModel::marcarRegistroComoVisto
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
    navController: NavController,
    viewModel: FamiliarDashboardViewModel,
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
        "familiar_dashboard" -> {
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
        route.contains("hijos") -> "Mis Hijos"
        route.contains("comunicaciones") -> "Comunicaciones"
        route.contains("perfil") -> "Mi Perfil"
        else -> "Funcionalidad en Desarrollo"
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
                text = "Familiar",
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
                    onItemClick(navItem.route, true)
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
                    onClick = { onItemClick(subItem.route, true) },
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