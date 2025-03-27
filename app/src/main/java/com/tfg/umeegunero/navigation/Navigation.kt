package com.tfg.umeegunero.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.tfg.umeegunero.data.model.UserType
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeUserType
import com.tfg.umeegunero.feature.common.support.screen.TechnicalSupportScreen
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Navegación principal de la aplicación
 * Esta implementación es una versión extendida de SimpleNavigation
 */
@Composable
fun Navigation(
    navController: NavHostController = rememberNavController(),
    onCloseApp: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    navController = navController,
                    onCloseDrawer = { 
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = AppScreens.Welcome.route
        ) {
            // Pantalla de bienvenida
            composable(route = AppScreens.Welcome.route) {
                WelcomeScreen(
                    onNavigateToLogin = { userType ->
                        val userTypeRoute = when (userType) {
                            WelcomeUserType.ADMIN -> "ADMIN"
                            WelcomeUserType.CENTRO -> "CENTRO"
                            WelcomeUserType.PROFESOR -> "PROFESOR"
                            WelcomeUserType.FAMILIAR -> "FAMILIAR"
                        }
                        navController.navigate(AppScreens.Login.createRoute(userTypeRoute))
                    },
                    onNavigateToRegister = {
                        navController.navigate(AppScreens.Registro.route)
                    },
                    onCloseApp = onCloseApp,
                    onNavigateToTechnicalSupport = {
                        navController.navigate(AppScreens.SoporteTecnico.route)
                    }
                )
            }
            
            // Pantalla de soporte técnico
            composable(route = AppScreens.SoporteTecnico.route) {
                TechnicalSupportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Pantalla de login
            composable(
                route = AppScreens.Login.route,
                arguments = listOf(
                    navArgument("userType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userTypeStr = backStackEntry.arguments?.getString("userType") ?: "FAMILIAR"
                val userType = remember(userTypeStr) {
                    when(userTypeStr) {
                        "ADMIN" -> UserType.ADMIN_APP
                        "CENTRO" -> UserType.ADMIN_CENTRO
                        "PROFESOR" -> UserType.PROFESOR
                        else -> UserType.FAMILIAR
                    }
                }
                
                LoginScreen(
                    userType = userType,
                    viewModel = hiltViewModel(),
                    onNavigateBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        val route = when(userType) {
                            UserType.ADMIN_APP -> AppScreens.AdminDashboard.route
                            UserType.ADMIN_CENTRO -> AppScreens.CentroDashboard.route
                            UserType.PROFESOR -> AppScreens.ProfesorDashboard.route
                            UserType.FAMILIAR -> AppScreens.FamiliarDashboard.route
                        }
                        navController.navigate(route) {
                            popUpTo(AppScreens.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Pantalla de registro
            composable(route = AppScreens.Registro.route) {
                RegistroScreen(
                    viewModel = hiltViewModel(),
                    onNavigateBack = { navController.popBackStack() },
                    onRegistroCompletado = {
                        navController.navigate(AppScreens.Login.createRoute("FAMILIAR")) {
                            popUpTo(AppScreens.Welcome.route)
                        }
                    }
                )
            }
        }
    }
} 