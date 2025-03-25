package com.tfg.umeegunero.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.data.model.TipoUsuario
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Estructura de navegación de la aplicación
 */
object NavigationStructure {

    /**
     * Representa un elemento de navegación en el menú
     */
    data class NavItem(
        val id: String = "",
        val title: String,
        val icon: ImageVector,
        val route: String,
        val badge: Int? = null,
        val subItems: List<NavItem> = emptyList(),
        val dividerAfter: Boolean = false,
        val isImplemented: Boolean = true, // Indica si la ruta está implementada
        val isExpandable: Boolean = false
    )

    /**
     * Obtiene los elementos de navegación según el tipo de usuario
     */
    fun getNavItemsByTipo(tipo: TipoUsuario): List<NavItem> {
        return when (tipo) {
            TipoUsuario.ADMIN_APP -> adminAppNavItems
            TipoUsuario.ADMIN_CENTRO -> adminCentroNavItems
            TipoUsuario.PROFESOR -> profesorNavItems
            TipoUsuario.FAMILIAR -> familiarNavItems
            else -> emptyList() // Para otros tipos como ALUMNO
        }
    }

    /**
     * Componente para el contenido del drawer
     */
    @Composable
    fun DrawerContent(
        navController: NavController,
        onDrawerClose: () -> Unit
    ) {
        var expandedItem by remember { mutableStateOf<String?>(null) }
        val navItems = getNavItemsByTipo(TipoUsuario.ADMIN_APP) // TODO: Obtener el tipo de usuario actual

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp)
        ) {
            navItems.forEach { item ->
                if (item.subItems.isNotEmpty()) {
                    ExpandableNavItem(
                        item = item,
                        isExpanded = expandedItem == item.id,
                        onExpand = { expandedItem = if (expandedItem == item.id) null else item.id },
                        onItemClick = { route ->
                            navController.navigate(route)
                            onDrawerClose()
                        }
                    )
                } else {
                    NavItem(
                        item = item,
                        onItemClick = { route ->
                            navController.navigate(route)
                            onDrawerClose()
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ExpandableNavItem(
        item: NavItem,
        isExpanded: Boolean,
        onExpand: () -> Unit,
        onItemClick: (String) -> Unit
    ) {
        Column {
            ListItem(
                headlineContent = { Text(item.title) },
                leadingContent = { Icon(item.icon, contentDescription = null) },
                trailingContent = {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir"
                    )
                },
                modifier = Modifier.clickable { onExpand() }
            )
            if (isExpanded) {
                item.subItems.forEach { subItem ->
                    NavItem(
                        item = subItem,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }

    @Composable
    private fun NavItem(
        item: NavItem,
        onItemClick: (String) -> Unit
    ) {
        ListItem(
            headlineContent = { Text(item.title) },
            leadingContent = { Icon(item.icon, contentDescription = null) },
            modifier = Modifier.clickable { onItemClick(item.route) }
        )
    }

    /**
     * Items de navegación para admin de la app
     */
    private val adminAppNavItems = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = AppScreens.AdminDashboard.route
        ),
        NavItem(
            title = "Centros Educativos",
            icon = Icons.Filled.School,
            route = "admin_dashboard/centros",
            subItems = listOf(
                NavItem(
                    title = "Listado de Centros",
                    icon = Icons.Filled.List,
                    route = "admin_dashboard/centros"
                ),
                NavItem(
                    title = "Añadir Centro",
                    icon = Icons.Filled.Add,
                    route = AppScreens.AddCentro.route
                )
            )
        ),
        NavItem(
            title = "Gestión Académica",
            icon = Icons.Filled.MenuBook,
            route = "admin_dashboard/academico",
            subItems = listOf(
                NavItem(
                    title = "Cursos",
                    icon = Icons.Filled.Class,
                    route = AppScreens.GestionCursos.route
                ),
                NavItem(
                    title = "Clases",
                    icon = Icons.Filled.Groups,
                    route = AppScreens.GestionClases.route
                ),
                NavItem(
                    title = "Calendario Escolar",
                    icon = Icons.Filled.CalendarMonth,
                    route = AppScreens.Calendario.route
                )
            )
        ),
        NavItem(
            title = "Gestión de Usuarios",
            icon = Icons.Filled.Group,
            route = "admin_dashboard/usuarios",
            subItems = listOf(
                NavItem(
                    title = "Profesores",
                    icon = Icons.Filled.Person,
                    route = AppScreens.ProfesorList.route
                ),
                NavItem(
                    title = "Alumnos",
                    icon = Icons.Filled.Person,
                    route = AppScreens.AlumnoList.route
                ),
                NavItem(
                    title = "Familiares",
                    icon = Icons.Filled.Group,
                    route = AppScreens.FamiliarList.route
                ),
                NavItem(
                    title = "Añadir Usuario",
                    icon = Icons.Filled.PersonAdd,
                    route = AppScreens.AddUser.createRoute(true)
                )
            )
        ),
        NavItem(
            title = "Gestionar Vinculaciones",
            icon = Icons.Filled.Link,
            route = AppScreens.Dummy.createRoute("Gestión de Vinculaciones")
        ),
        NavItem(
            title = "Estadísticas",
            icon = Icons.Filled.BarChart,
            route = AppScreens.Estadisticas.route
        ),
        NavItem(
            title = "Notificaciones",
            icon = Icons.Filled.Notifications,
            route = AppScreens.Notificaciones.route
        ),
        NavItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            route = AppScreens.Perfil.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Config.route,
            subItems = listOf(
                NavItem(
                    title = "Configuración General",
                    icon = Icons.Filled.Settings,
                    route = AppScreens.Config.route
                ),
                NavItem(
                    title = "Configuración Email",
                    icon = Icons.Filled.Email,
                    route = AppScreens.EmailConfig.route
                )
            ),
            isExpandable = true
        ),
        NavItem(
            title = "Cerrar Sesión",
            icon = Icons.Filled.Logout,
            route = "logout"
        )
    )

    /**
     * Elementos de navegación para el administrador de centro
     */
    private val adminCentroNavItems = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = AppScreens.CentroDashboard.route
        ),
        NavItem(
            title = "Gestión Académica",
            icon = Icons.Filled.MenuBook,
            route = "centro_dashboard/academico",
            subItems = listOf(
                NavItem(
                    title = "Cursos",
                    icon = Icons.Filled.Class,
                    route = AppScreens.GestionCursos.route
                ),
                NavItem(
                    title = "Clases",
                    icon = Icons.Filled.Groups,
                    route = AppScreens.GestionClases.route
                ),
                NavItem(
                    title = "Calendario Escolar",
                    icon = Icons.Filled.CalendarMonth,
                    route = AppScreens.Calendario.route
                )
            )
        ),
        NavItem(
            title = "Gestión de Usuarios",
            icon = Icons.Filled.Group,
            route = "centro_dashboard/usuarios",
            subItems = listOf(
                NavItem(
                    title = "Profesores",
                    icon = Icons.Filled.Person,
                    route = AppScreens.ProfesorList.route
                ),
                NavItem(
                    title = "Alumnos",
                    icon = Icons.Filled.Person,
                    route = AppScreens.AlumnoList.route
                ),
                NavItem(
                    title = "Familiares",
                    icon = Icons.Filled.Group,
                    route = AppScreens.FamiliarList.route
                ),
                NavItem(
                    title = "Añadir Usuario",
                    icon = Icons.Filled.PersonAdd,
                    route = AppScreens.AddUser.createRoute(false)
                )
            )
        ),
        NavItem(
            title = "Estadísticas",
            icon = Icons.Filled.BarChart,
            route = AppScreens.Estadisticas.route
        ),
        NavItem(
            title = "Notificaciones",
            icon = Icons.Filled.Notifications,
            route = AppScreens.Notificaciones.route
        ),
        NavItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            route = AppScreens.Perfil.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Config.route,
            subItems = listOf(
                NavItem(
                    title = "Configuración General",
                    icon = Icons.Filled.Settings,
                    route = AppScreens.Config.route
                ),
                NavItem(
                    title = "Configuración Email",
                    icon = Icons.Filled.Email,
                    route = AppScreens.EmailConfig.route
                )
            ),
            isExpandable = true
        ),
        NavItem(
            title = "Cerrar Sesión",
            icon = Icons.Filled.Logout,
            route = "logout"
        )
    )

    /**
     * Elementos de navegación para el profesor
     */
    val profesorNavItems = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = AppScreens.ProfesorDashboard.route
        ),
        NavItem(
            title = "Mis Clases",
            icon = Icons.Filled.Groups,
            route = AppScreens.Dummy.createRoute("Mis Clases")
        ),
        NavItem(
            title = "Alumnos",
            icon = Icons.Filled.Person,
            route = AppScreens.Dummy.createRoute("Mis Alumnos")
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = AppScreens.Dummy.createRoute("Comunicaciones")
        ),
        NavItem(
            title = "Notificaciones",
            icon = Icons.Filled.Notifications,
            route = AppScreens.Notificaciones.route
        ),
        NavItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            route = AppScreens.Perfil.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Config.route
        ),
        NavItem(
            title = "Cerrar Sesión",
            icon = Icons.Filled.Logout,
            route = "logout"
        )
    )

    /**
     * Elementos de navegación para el familiar
     */
    val familiarNavItems = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = AppScreens.FamiliarDashboard.route
        ),
        NavItem(
            title = "Mis Hijos",
            icon = Icons.Filled.ChildCare,
            route = AppScreens.Dummy.createRoute("Mis Hijos")
        ),
        NavItem(
            title = "Comunicaciones",
            icon = Icons.Filled.Email,
            route = AppScreens.Dummy.createRoute("Comunicaciones")
        ),
        NavItem(
            title = "Notificaciones",
            icon = Icons.Filled.Notifications,
            route = AppScreens.Notificaciones.route
        ),
        NavItem(
            title = "Perfil",
            icon = Icons.Filled.Person,
            route = AppScreens.Perfil.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            route = AppScreens.Config.route
        ),
        NavItem(
            title = "Cerrar Sesión",
            icon = Icons.Filled.Logout,
            route = "logout"
        )
    )
} 