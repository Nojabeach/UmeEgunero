package com.tfg.umeegunero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.tfg.umeegunero.data.model.TipoUsuario

object NavigationStructure {
    data class NavItem(
        val title: String,
        val icon: ImageVector,
        val route: String,
        val dividerAfter: Boolean = false,
        val badge: Int? = null,
        val subItems: List<NavItem> = emptyList(),
        val isImplemented: Boolean = true
    )

    fun getAdminNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Default.Home,
            route = AppScreens.AdminDashboard.route
        ),
        NavItem(
            title = "Gestión de Centros",
            icon = Icons.Default.Business,
            route = AppScreens.GestionCentros.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = AppScreens.Configuracion.route,
            dividerAfter = true
        )
    )

    fun getCentroNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Default.Home,
            route = AppScreens.CentroDashboard.route
        ),
        NavItem(
            title = "Gestión Académica",
            icon = Icons.Default.School,
            route = AppScreens.GestionAcademica.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = AppScreens.Configuracion.route,
            dividerAfter = true
        )
    )

    fun getProfesorNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Default.Home,
            route = AppScreens.ProfesorDashboard.route
        ),
        NavItem(
            title = "Calendario",
            icon = Icons.Default.CalendarToday,
            route = AppScreens.Calendario.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = AppScreens.Configuracion.route,
            dividerAfter = true
        )
    )

    fun getFamiliarNavItems(): List<NavItem> = listOf(
        NavItem(
            title = "Dashboard",
            icon = Icons.Default.Home,
            route = AppScreens.FamiliarDashboard.route
        ),
        NavItem(
            title = "Calendario",
            icon = Icons.Default.CalendarToday,
            route = AppScreens.Calendario.route
        ),
        NavItem(
            title = "Configuración",
            icon = Icons.Default.Settings,
            route = AppScreens.Configuracion.route,
            dividerAfter = true
        )
    )

    fun getNavItemsByTipo(tipo: TipoUsuario): List<NavItem> = when (tipo) {
        TipoUsuario.ADMIN_APP -> getAdminNavItems()
        TipoUsuario.ADMIN_CENTRO -> getCentroNavItems()
        TipoUsuario.PROFESOR -> getProfesorNavItems()
        TipoUsuario.FAMILIAR -> getFamiliarNavItems()
        TipoUsuario.ALUMNO -> emptyList() // Los alumnos no tienen navegación por ahora
    }

    fun getNavItemsByTipo(tipoString: String): List<NavItem> = when (tipoString.uppercase()) {
        "ADMIN_APP", "ADMIN" -> getAdminNavItems()
        "ADMIN_CENTRO", "CENTRO" -> getCentroNavItems()
        "PROFESOR" -> getProfesorNavItems()
        "FAMILIAR" -> getFamiliarNavItems()
        else -> emptyList()
    }
} 