package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tfg.umeegunero.feature.common.mensajeria.ConversacionesScreen
import com.tfg.umeegunero.feature.profesor.screen.ChatScreen as ProfesorChatScreen
import com.tfg.umeegunero.feature.familiar.screen.ChatScreen as FamiliarChatScreen

/**
 * Componente principal de navegación para la aplicación UmeEgunero.
 * Configura el NavHost con todas las rutas disponibles.
 *
 * @param navController Controlador de navegación que gestionará las transiciones entre pantallas
 * @param startDestination Ruta inicial donde comenzará la navegación
 */
@Composable
fun Navigation(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantallas de autenticación
        composable(
            route = Screens.Login.route,
            arguments = Screens.Login.arguments
        ) {
            // Placeholder para LoginScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(
            route = Screens.Registro.route,
            arguments = Screens.Registro.arguments
        ) {
            // Placeholder para RegistroScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(
            route = Screens.RecuperarPassword.route,
            arguments = Screens.RecuperarPassword.arguments
        ) {
            // Placeholder para RecuperarPasswordScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        // Dashboards
        composable(
            route = Screens.AdminDashboard.route,
            arguments = Screens.AdminDashboard.arguments
        ) {
            // Placeholder para AdminDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(
            route = Screens.CentroDashboard.route,
            arguments = Screens.CentroDashboard.arguments
        ) {
            // Placeholder para CentroDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(
            route = Screens.ProfesorDashboard.route,
            arguments = Screens.ProfesorDashboard.arguments
        ) {
            // Placeholder para ProfesorDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(
            route = Screens.FamiliarDashboard.route,
            arguments = Screens.FamiliarDashboard.arguments
        ) {
            // Placeholder para FamiliarDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        // Pantallas de mensajería
        composable(
            route = Screens.Conversaciones.route,
            arguments = Screens.Conversaciones.arguments
        ) { backStackEntry ->
            val esFamiliar = backStackEntry.arguments?.getBoolean("esFamiliar") ?: false
            ConversacionesScreen(
                esFamiliar = esFamiliar,
                onNavigateToChat = { conversacionId, usuarioDestino ->
                    if (esFamiliar) {
                        navController.navigate(Screens.ChatFamiliar.createRoute(usuarioDestino))
                    } else {
                        navController.navigate(Screens.ChatProfesor.createRoute(usuarioDestino))
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Chat para profesor
        composable(
            route = Screens.ChatProfesor.route,
            arguments = Screens.ChatProfesor.arguments
        ) { backStackEntry ->
            val familiarId = backStackEntry.arguments?.getString("familiarId") ?: ""
            ProfesorChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Chat para familiar
        composable(
            route = Screens.ChatFamiliar.route,
            arguments = Screens.ChatFamiliar.arguments
        ) { backStackEntry ->
            val profesorId = backStackEntry.arguments?.getString("profesorId") ?: ""
            FamiliarChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Extensión para facilitar la navegación tipo-seguro.
 * Proporciona métodos para navegar usando directamente las clases Screens.
 * 
 * @param route La pantalla destino como objeto Screens
 * @param popUpToRoute Opcional. Ruta hasta la que hacer pop en la pila de navegación
 * @param inclusive Si true, la ruta popUpToRoute también será eliminada de la pila
 * @param singleTop Si true, evita múltiples copias de la misma ruta en la pila
 */
fun NavHostController.navigateTo(
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false,
    singleTop: Boolean = false
) {
    navigate(route) {
        if (popUpToRoute != null) {
            popUpTo(popUpToRoute) {
                this.inclusive = inclusive
            }
        }
        this.launchSingleTop = singleTop
    }
} 