package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tfg.umeegunero.ui.components.DummyScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = AppScreens.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... existing code ...
        
        // Rutas para pantallas dummy
        composable(AppScreens.DummyGestionCursos.route) {
            DummyScreen(
                title = "Gestión de Cursos",
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(AppScreens.DummyGestionClases.route) {
            DummyScreen(
                title = "Gestión de Clases",
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(AppScreens.DummyGestionUsuarios.route) {
            DummyScreen(
                title = "Gestión de Usuarios",
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(AppScreens.DummyEstadisticas.route) {
            DummyScreen(
                title = "Estadísticas",
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        composable(AppScreens.DummyConfiguracion.route) {
            DummyScreen(
                title = "Configuración",
                onNavigateBack = { navController.navigateUp() }
            )
        }
        
        // ... existing code ...
    }
} 