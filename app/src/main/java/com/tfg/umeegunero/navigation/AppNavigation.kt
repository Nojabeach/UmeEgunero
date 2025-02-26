// Modificamos AppNavigation.kt para pasar la función de cierre
package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tfg.umeegunero.data.model.UserType
import com.tfg.umeegunero.feature.admin.screen.AddCentroScreen
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegistroScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen

/**
 * Sealed class para las rutas de navegación de la app
 */
sealed class AppScreens(val route: String) {
    // Pantallas de acceso/autenticación
    object Welcome : AppScreens("welcome")
    object Login : AppScreens("login/{userType}") {
        fun createRoute(userType: String) = "login/$userType"
    }
    object Register : AppScreens("register")

    // Pantallas principales según tipo de usuario
    object AdminDashboard : AppScreens("admin_dashboard")
    object CentroDashboard : AppScreens("centro_dashboard")
    object ProfesorDashboard : AppScreens("profesor_dashboard")
    object FamiliarDashboard : AppScreens("familiar_dashboard")
    // Pantallas de administración
    object AddCentro : AppScreens("add_centro")
    // Otras pantallas
    object StudentDetail : AppScreens("student_detail/{studentId}") {
        fun createRoute(studentId: String) = "student_detail/$studentId"
    }
    object ReportDetail : AppScreens("report_detail/{reportId}") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    object Chat : AppScreens("chat/{recipientId}") {
        fun createRoute(recipientId: String) = "chat/$recipientId"
    }
}

/**
 * Navegación principal de la aplicación
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    onCloseApp: () -> Unit = {} // Agregamos el parámetro para cerrar la app
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Welcome.route
    ) {
        // Pantalla de bienvenida
        composable(route = AppScreens.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { userType ->
                    val userTypeStr = when(userType) {
                        UserType.ADMIN -> "ADMIN"
                        UserType.CENTRO -> "CENTRO"
                        UserType.PROFESOR -> "PROFESOR"
                        UserType.FAMILIAR -> "FAMILIAR"
                    }
                    navController.navigate(AppScreens.Login.createRoute(userTypeStr))
                },
                onNavigateToRegister = {
                    navController.navigate(AppScreens.Register.route)
                },
                onCloseApp = onCloseApp // Pasamos la función de cierre
            )
        }

        // Pantalla de inicio de sesión (recibe tipo de usuario)
        composable(
            route = AppScreens.Login.route,
            arguments = listOf(
                navArgument("userType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val userTypeStr = backStackEntry.arguments?.getString("userType") ?: "FAMILIAR"
            val userType = remember(userTypeStr) {
                when(userTypeStr) {
                    "ADMIN" -> UserType.ADMIN
                    "CENTRO" -> UserType.CENTRO
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
                        UserType.ADMIN -> AppScreens.AdminDashboard.route
                        UserType.CENTRO -> AppScreens.CentroDashboard.route
                        UserType.PROFESOR -> AppScreens.ProfesorDashboard.route
                        UserType.FAMILIAR -> AppScreens.FamiliarDashboard.route
                    }
                    navController.navigate(route) {
                        popUpTo(AppScreens.Welcome.route) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    // TODO: Implementar recuperación de contraseña
                }
            )
        }

        // Pantalla de registro
        composable(route = AppScreens.Register.route) {
            RegistroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onRegistroCompletado = {
                    // Navegamos a la pantalla de inicio de sesión para familiares
                    navController.navigate(AppScreens.Login.createRoute("FAMILIAR")) {
                        popUpTo(AppScreens.Register.route) { inclusive = true }
                    }
                }
            )
        }
        // Pantalla de añadir centro (admin)
        composable(route = AppScreens.AddCentro.route) {
            AddCentroScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() },
                onCentroAdded = { navController.popBackStack() }
            )
        }
        // Añade aquí las demás pantallas a medida que las vayas implementando
    }
}