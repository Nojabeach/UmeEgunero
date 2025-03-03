package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tfg.umeegunero.feature.familiar.screen.ChatScreen
import com.tfg.umeegunero.feature.familiar.screen.DetalleHijoScreen
import com.tfg.umeegunero.feature.familiar.screen.DetalleRegistroScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliarDashboardScreen

/**
 * Clase que contiene las rutas de navegación para el módulo familiar
 */
object FamiliarDestinations {
    const val DASHBOARD_ROUTE = "familiar_dashboard"
    const val DETALLE_REGISTRO_ROUTE = "detalle_registro/{registroId}"
    const val DETALLE_HIJO_ROUTE = "detalle_hijo/{alumnoDni}"
    const val CHAT_ROUTE = "chat/{profesorId}"
    const val CHAT_WITH_ALUMNO_ROUTE = "chat/{profesorId}/{alumnoId}"

    fun detalleRegistroRoute(registroId: String): String {
        return "detalle_registro/$registroId"
    }

    fun detalleHijoRoute(alumnoDni: String): String {
        return "detalle_hijo/$alumnoDni"
    }

    fun chatRoute(profesorId: String, alumnoId: String? = null): String {
        return if (alumnoId != null) {
            "chat/$profesorId/$alumnoId"
        } else {
            "chat/$profesorId"
        }
    }
}

/**
 * Configuración de navegación para el módulo familiar
 */
@Composable
fun FamiliarNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = FamiliarDestinations.DASHBOARD_ROUTE,
    onLogout: () -> Unit
) {
    val actions = remember(navController) { FamiliarNavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantalla principal de dashboard
        composable(
            route = FamiliarDestinations.DASHBOARD_ROUTE
        ) {
            FamiliarDashboardScreen(
                onLogout = onLogout,
                onNavigateToDetalleRegistro = actions.navigateToDetalleRegistro,
                onNavigateToDetalleHijo = actions.navigateToDetalleHijo,
                onNavigateToChat = actions.navigateToChat
            )
        }

        // Detalle de registro
        composable(
            route = FamiliarDestinations.DETALLE_REGISTRO_ROUTE,
            arguments = listOf(
                navArgument("registroId") { type = NavType.StringType }
            )
        ) { entry ->
            val registroId = entry.arguments?.getString("registroId") ?: ""
            DetalleRegistroScreen(
                onNavigateBack = actions.upPress,
                onNavigateToChat = actions.navigateToChat
            )
        }

        // Detalle de hijo/alumno
        composable(
            route = FamiliarDestinations.DETALLE_HIJO_ROUTE,
            arguments = listOf(
                navArgument("alumnoDni") { type = NavType.StringType }
            )
        ) { entry ->
            val alumnoDni = entry.arguments?.getString("alumnoDni") ?: ""
            DetalleHijoScreen(
                onNavigateBack = actions.upPress,
                onNavigateToChat = actions.navigateToChat
            )
        }

        // Chat con profesor (sin alumno específico)
        composable(
            route = FamiliarDestinations.CHAT_ROUTE,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.StringType }
            )
        ) { entry ->
            val profesorId = entry.arguments?.getString("profesorId") ?: ""
            ChatScreen(
                onNavigateBack = actions.upPress
            )
        }

        // Chat con profesor (con alumno específico)
        composable(
            route = FamiliarDestinations.CHAT_WITH_ALUMNO_ROUTE,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.StringType },
                navArgument("alumnoId") { type = NavType.StringType }
            )
        ) { entry ->
            val profesorId = entry.arguments?.getString("profesorId") ?: ""
            val alumnoId = entry.arguments?.getString("alumnoId") ?: ""
            ChatScreen(
                onNavigateBack = actions.upPress
            )
        }
    }
}

/**
 * Acciones de navegación para el módulo familiar
 */
class FamiliarNavigationActions(private val navController: NavHostController) {

    val upPress: () -> Unit = {
        navController.navigateUp()
    }

    val navigateToDetalleRegistro: (String) -> Unit = { registroId ->
        navController.navigate(FamiliarDestinations.detalleRegistroRoute(registroId))
    }

    val navigateToDetalleHijo: (String) -> Unit = { alumnoDni ->
        navController.navigate(FamiliarDestinations.detalleHijoRoute(alumnoDni))
    }

    val navigateToChat: (String, String?) -> Unit = { profesorId, alumnoId ->
        navController.navigate(FamiliarDestinations.chatRoute(profesorId, alumnoId))
    }
}