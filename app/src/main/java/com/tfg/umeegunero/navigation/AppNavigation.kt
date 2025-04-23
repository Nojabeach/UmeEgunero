package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Welcome.route
    ) {
        // Ruta comentada temporalmente hasta que se tenga la implementación correcta
        /*
        composable(
            route = AppScreens.AdminListaAdministradoresScreen.route
        ) {
            HiltListAdministradoresScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        */
        
        // Aquí irían el resto de rutas
    }
}