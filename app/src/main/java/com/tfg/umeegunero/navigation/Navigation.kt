package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tfg.umeegunero.feature.common.mensajeria.ConversacionesScreen
import com.tfg.umeegunero.feature.profesor.screen.ChatScreen as ProfesorChatScreen
import com.tfg.umeegunero.feature.familiar.screen.ChatScreen as FamiliarChatScreen

@Composable
fun Navigation(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantallas de autenticación
        composable(Screens.Login.route) {
            // Placeholder para LoginScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(Screens.Registro.route) {
            // Placeholder para RegistroScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        // Dashboards
        composable(Screens.AdminDashboard.route) {
            // Placeholder para AdminDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(Screens.CentroDashboard.route) {
            // Placeholder para CentroDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(Screens.ProfesorDashboard.route) {
            // Placeholder para ProfesorDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        composable(Screens.FamiliarDashboard.route) {
            // Placeholder para FamiliarDashboardScreen
            // Se implementará con el componente correcto cuando esté disponible
        }
        
        // Pantallas de mensajería
        composable(
            route = Screens.Conversaciones.route,
            arguments = listOf(
                navArgument("esFamiliar") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val esFamiliar = backStackEntry.arguments?.getBoolean("esFamiliar") ?: false
            ConversacionesScreen(
                esFamiliar = esFamiliar,
                onNavigateToChat = { conversacionId, usuarioDestino ->
                    if (esFamiliar) {
                        navController.navigate("chat_profesor/$usuarioDestino")
                    } else {
                        navController.navigate("chat_familiar/$usuarioDestino")
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Chat para profesor
        composable(
            route = "chat_familiar/{familiarId}",
            arguments = listOf(
                navArgument("familiarId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val familiarId = backStackEntry.arguments?.getString("familiarId") ?: ""
            ProfesorChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Chat para familiar
        composable(
            route = "chat_profesor/{profesorId}",
            arguments = listOf(
                navArgument("profesorId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val profesorId = backStackEntry.arguments?.getString("profesorId") ?: ""
            FamiliarChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 