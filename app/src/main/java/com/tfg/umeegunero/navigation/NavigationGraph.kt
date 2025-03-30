package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.tfg.umeegunero.feature.familiar.screen.ActividadesPreescolarScreen
import com.tfg.umeegunero.feature.profesor.screen.ActividadesPreescolarProfesorScreen

/**
 * Grafo de navegación para el perfil de Familiar
 */
fun NavGraphBuilder.familiarNavGraph(
    navController: NavHostController,
    userId: String,
    userName: String
) {
    navigation(
        startDestination = AppScreens.FamiliarDashboard.route,
        route = "familiar_graph"
    ) {
        // Dashboard del familiar
        composable(AppScreens.FamiliarDashboard.route) {
            // FamiliarDashboardScreen(navController, userId)
        }
        
        // Actividades preescolares
        composable(AppScreens.ActividadesPreescolar.route) {
            ActividadesPreescolarScreen(
                navController = navController,
                familiarId = userId
            )
        }
        
        // Otras pantallas del familiar...
    }
}

/**
 * Grafo de navegación para el perfil de Profesor
 */
fun NavGraphBuilder.profesorNavGraph(
    navController: NavHostController,
    userId: String,
    userName: String
) {
    navigation(
        startDestination = AppScreens.ProfesorDashboard.route,
        route = "profesor_graph"
    ) {
        // Dashboard del profesor
        composable(AppScreens.ProfesorDashboard.route) {
            // ProfesorDashboardScreen(navController, userId)
        }
        
        // Actividades preescolares
        composable(AppScreens.ActividadesPreescolarProfesor.route) {
            ActividadesPreescolarProfesorScreen(
                profesorId = userId,
                profesorNombre = userName,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Otras pantallas del profesor...
    }
} 