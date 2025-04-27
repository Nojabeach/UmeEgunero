package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tfg.umeegunero.ui.components.DummyScreen
import com.tfg.umeegunero.feature.auth.screen.CambioContrasenaScreen
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.data.model.TipoUsuario
import androidx.navigation.NavType
import com.tfg.umeegunero.feature.common.users.screen.GestionUsuariosScreen
import com.tfg.umeegunero.feature.common.perfil.screen.EditProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = AppScreens.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Ruta para login
        composable(
            route = AppScreens.Login.route,
            arguments = listOf(
                navArgument("userType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userType = TipoUsuario.valueOf(
                backStackEntry.arguments?.getString("userType") ?: TipoUsuario.DESCONOCIDO.name
            )
            LoginScreen(
                userType = userType,
                onNavigateBack = { navController.navigateUp() },
                onLoginSuccess = { tipo ->
                    // Navegar al dashboard correspondiente según el tipo de usuario
                    when (tipo) {
                        TipoUsuario.ADMIN_APP -> navController.navigate(AppScreens.AdminDashboard.route)
                        TipoUsuario.ADMIN_CENTRO -> navController.navigate(AppScreens.CentroDashboard.route)
                        TipoUsuario.PROFESOR -> navController.navigate(AppScreens.ProfesorDashboard.route)
                        TipoUsuario.FAMILIAR -> navController.navigate(AppScreens.FamiliarDashboard.route)
                        else -> {}
                    }
                },
                onForgotPassword = { email ->
                    // Navegar a la pantalla de cambio de contraseña
                    navController.navigate(AppScreens.CambioContrasena.createRoute(email))
                }
            )
        }

        // Ruta para cambio de contraseña
        composable(
            route = AppScreens.CambioContrasena.route,
            arguments = AppScreens.CambioContrasena.arguments
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            CambioContrasenaScreen(
                dni = dni,
                onNavigateBack = { navController.navigateUp() },
                onPasswordChanged = {
                    // Volver a la pantalla de login después de cambiar la contraseña
                    navController.popBackStack(AppScreens.Login.route, false)
                }
            )
        }
        
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
        
        // Pantalla de gestión de usuarios
        composable(
            route = AppScreens.GestionUsuarios.route,
            arguments = listOf(
                navArgument("isAdminApp") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val isAdminApp = backStackEntry.arguments?.getBoolean("isAdminApp") ?: false
            GestionUsuariosScreen(
                navController = navController,
                isAdminApp = isAdminApp,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserList = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAddUser = { isAdmin ->
                    navController.navigate(AppScreens.AddUser.createRoute(isAdmin)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(AppScreens.EditProfile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // Pantalla de edición de perfil
        composable(route = AppScreens.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
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