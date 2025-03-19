package com.tfg.umeegunero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Business
import androidx.compose.ui.graphics.vector.ImageVector
import com.tfg.umeegunero.data.model.TipoUsuario

/**
 * Estructura de navegación de la aplicación
 */
object NavigationStructure {

    /**
     * Representa un elemento de navegación en el menú
     */
    data class NavItem(
        val id: String,
        val title: String,
        val icon: ImageVector,
        val route: String,
        val badge: Int? = null,
        val subItems: List<NavItem> = emptyList(),
        val dividerAfter: Boolean = false,
        val isImplemented: Boolean = true // Indica si la ruta está implementada
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
     * Elementos de navegación para el administrador de la aplicación
     */
    private val adminAppNavItems = listOf(
        NavItem(
            id = "dashboard",
            title = "Panel de Control",
            icon = Icons.Default.Dashboard,
            route = "admin_dashboard",
            subItems = listOf(
                NavItem(
                    id = "estadisticas",
                    title = "Resumen de estadísticas",
                    icon = Icons.Default.Summarize,
                    route = "admin_dashboard/estadisticas",
                    isImplemented = false
                ),
                NavItem(
                    id = "notificaciones",
                    title = "Notificaciones y avisos",
                    icon = Icons.Default.Notifications,
                    route = "admin_dashboard/notificaciones",
                    isImplemented = false
                )
            )
        ),
        NavItem(
            id = "centros_educativos",
            title = "Centros Educativos",
            icon = Icons.Default.Business,
            route = "centros",
            subItems = listOf(
                NavItem(
                    id = "lista_centros",
                    title = "Listado de centros",
                    icon = Icons.AutoMirrored.Filled.List,
                    route = "admin_dashboard", // La lista se muestra en el dashboard
                    isImplemented = true
                ),
                NavItem(
                    id = "nuevo_centro",
                    title = "Añadir nuevo centro",
                    icon = Icons.Default.Add,
                    route = "add_centro",
                    isImplemented = true
                )
            )
        ),
        NavItem(
            id = "gestion_academica",
            title = "Gestión Académica",
            icon = Icons.Default.School,
            route = "gestion_academica",
            subItems = listOf(
                NavItem(
                    id = "cursos",
                    title = "Cursos",
                    icon = Icons.Default.ViewList,
                    route = "admin_dashboard/cursos",
                    isImplemented = false
                ),
                NavItem(
                    id = "clases",
                    title = "Clases",
                    icon = Icons.Default.Class,
                    route = "admin_dashboard/clases",
                    isImplemented = false
                ),
                NavItem(
                    id = "calendario",
                    title = "Calendario escolar",
                    icon = Icons.Default.CalendarMonth,
                    route = "admin_dashboard/calendario",
                    isImplemented = false
                )
            )
        ),
        NavItem(
            id = "gestion_personas",
            title = "Gestión de Personas",
            icon = Icons.Default.People,
            route = "gestion_personas",
            subItems = listOf(
                NavItem(
                    id = "administradores",
                    title = "Administradores de App",
                    icon = Icons.Default.AdminPanelSettings,
                    route = "add_user/true", // Admin de app = true
                    isImplemented = true
                ),
                NavItem(
                    id = "profesores",
                    title = "Profesorado",
                    icon = Icons.Default.People,
                    route = "profesores",
                    subItems = listOf(
                        NavItem(
                            id = "list_profesores",
                            title = "Lista de profesores",
                            icon = Icons.AutoMirrored.Filled.List,
                            route = "admin_dashboard/profesores",
                            isImplemented = false
                        ),
                        NavItem(
                            id = "asignar_profesores",
                            title = "Asignar profesores a cursos",
                            icon = Icons.Default.AssignmentInd,
                            route = "admin_dashboard/asignar_profesores",
                            isImplemented = false
                        ),
                        NavItem(
                            id = "add_profesor",
                            title = "Añadir nuevo profesor",
                            icon = Icons.Default.PersonAdd,
                            route = "add_user/false", // No es admin de app
                            isImplemented = true
                        )
                    )
                ),
                NavItem(
                    id = "alumnos",
                    title = "Alumnado",
                    icon = Icons.Default.Groups,
                    route = "alumnos",
                    subItems = listOf(
                        NavItem(
                            id = "list_alumnos",
                            title = "Lista de alumnos",
                            icon = Icons.AutoMirrored.Filled.List,
                            route = "admin_dashboard/alumnos",
                            isImplemented = false
                        ),
                        NavItem(
                            id = "asignar_alumnos",
                            title = "Asignar alumnos a clases",
                            icon = Icons.Default.AssignmentInd,
                            route = "admin_dashboard/asignar_alumnos",
                            isImplemented = false
                        ),
                        NavItem(
                            id = "add_alumno",
                            title = "Añadir nuevo alumno",
                            icon = Icons.Default.PersonAdd,
                            route = "admin_dashboard/add_alumno",
                            isImplemented = false
                        )
                    )
                ),
                NavItem(
                    id = "familias",
                    title = "Familias",
                    icon = Icons.Default.Groups,
                    route = "familias",
                    subItems = listOf(
                        NavItem(
                            id = "solicitudes_vinculacion",
                            title = "Solicitudes pendientes",
                            icon = Icons.Default.Link,
                            route = "admin_dashboard/solicitudes_vinculacion",
                            isImplemented = false
                        ),
                        NavItem(
                            id = "familiares_vinculados",
                            title = "Familiares vinculados",
                            icon = Icons.AutoMirrored.Filled.List,
                            route = "admin_dashboard/familiares_vinculados",
                            isImplemented = false
                        ),
                        NavItem(
                            id = "gestionar_vinculaciones",
                            title = "Gestionar vinculaciones",
                            icon = Icons.Default.Link,
                            route = "admin_dashboard/gestionar_vinculaciones",
                            isImplemented = false
                        )
                    )
                )
            )
        ),
        NavItem(
            id = "administracion",
            title = "Administración",
            icon = Icons.Default.AdminPanelSettings,
            route = "administracion",
            subItems = listOf(
                NavItem(
                    id = "admins_centro",
                    title = "Administradores del centro",
                    icon = Icons.Default.People,
                    route = "admin_dashboard/admins_centro",
                    isImplemented = false
                ),
                NavItem(
                    id = "config_centro",
                    title = "Configuración del centro",
                    icon = Icons.Default.Settings,
                    route = "admin_dashboard/config_centro",
                    isImplemented = false
                ),
                NavItem(
                    id = "permisos_roles",
                    title = "Permisos y roles",
                    icon = Icons.Default.Security,
                    route = "admin_dashboard/permisos_roles",
                    isImplemented = false
                )
            )
        ),
        NavItem(
            id = "mi_perfil",
            title = "Mi Perfil",
            icon = Icons.Default.AccountCircle,
            route = "mi_perfil",
            subItems = listOf(
                NavItem(
                    id = "datos_personales",
                    title = "Datos personales",
                    icon = Icons.Default.Person,
                    route = "admin_dashboard/datos_personales",
                    isImplemented = false
                ),
                NavItem(
                    id = "cambiar_password",
                    title = "Cambiar contraseña",
                    icon = Icons.Default.Key,
                    route = "admin_dashboard/cambiar_password",
                    isImplemented = false
                )
            ),
            dividerAfter = true
        ),
        NavItem(
            id = "configuracion",
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = "config",
            subItems = listOf(
                NavItem(
                    id = "tema_app",
                    title = "Tema de la aplicación",
                    icon = Icons.Default.DarkMode,
                    route = "config",
                    isImplemented = true
                )
            ),
            dividerAfter = true
        ),
        NavItem(
            id = "logout",
            title = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            route = "logout"
        )
    )

    /**
     * Elementos de navegación para el administrador de centro
     */
    private val adminCentroNavItems = listOf(
        NavItem(
            id = "dashboard",
            title = "Panel Principal",
            icon = Icons.Default.Dashboard,
            route = "centro_dashboard"
        ),
        NavItem(
            id = "profile",
            title = "Mi Perfil",
            icon = Icons.Default.Person,
            route = "centro_dashboard/profile",
            isImplemented = false
        ),
        NavItem(
            id = "configuracion",
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = "config",
            dividerAfter = true,
            isImplemented = true
        ),
        NavItem(
            id = "logout",
            title = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            route = "logout"
        )
    )

    /**
     * Elementos de navegación para el profesor
     */
    private val profesorNavItems = listOf(
        NavItem(
            id = "dashboard",
            title = "Panel Principal",
            icon = Icons.Default.Dashboard,
            route = "profesor_dashboard"
        ),
        NavItem(
            id = "profile",
            title = "Mi Perfil",
            icon = Icons.Default.Person,
            route = "profesor_dashboard/profile",
            isImplemented = false
        ),
        NavItem(
            id = "configuracion",
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = "config",
            dividerAfter = true,
            isImplemented = true
        ),
        NavItem(
            id = "logout",
            title = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            route = "logout"
        )
    )

    /**
     * Elementos de navegación para el familiar
     */
    private val familiarNavItems = listOf(
        NavItem(
            id = "dashboard",
            title = "Panel Principal",
            icon = Icons.Default.Dashboard,
            route = "familiar_dashboard"
        ),
        NavItem(
            id = "profile",
            title = "Mi Perfil",
            icon = Icons.Default.Person,
            route = "familiar_dashboard/profile",
            isImplemented = false
        ),
        NavItem(
            id = "configuracion",
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = "config",
            dividerAfter = true,
            isImplemented = true
        ),
        NavItem(
            id = "logout",
            title = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            route = "logout"
        )
    )
} 