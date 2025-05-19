package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tfg.umeegunero.feature.auth.screen.CambioContrasenaScreen
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen
import com.tfg.umeegunero.feature.profesor.screen.ProfesorDashboardScreen
import com.tfg.umeegunero.feature.familiar.screen.FamiliaDashboardScreen
import com.tfg.umeegunero.feature.common.perfil.screen.PerfilScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = AppScreens.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Rutas de autenticación
        composable(route = AppScreens.Login.route) { backStackEntry ->
            // Extraer el parámetro userType de la ruta
            val userTypeString = backStackEntry.arguments?.getString("userType") ?: "DESCONOCIDO"
            val userType = try {
                com.tfg.umeegunero.data.model.TipoUsuario.valueOf(userTypeString)
            } catch (e: Exception) {
                com.tfg.umeegunero.data.model.TipoUsuario.DESCONOCIDO
            }
            com.tfg.umeegunero.feature.auth.screen.LoginScreen(
                userType = userType,
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = { /* Navegación tras login */ }
            )
        }
        composable(route = AppScreens.CambioContrasena.route) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            com.tfg.umeegunero.feature.auth.screen.CambioContrasenaScreen(
                dni = dni,
                onNavigateBack = { navController.popBackStack() },
                onPasswordChanged = { /* Acción tras cambio */ }
            )
        }
        // Dashboards principales
        composable(route = AppScreens.ProfesorDashboard.route) {
            ProfesorDashboardScreen(navController = navController)
        }
        composable(route = AppScreens.FamiliarDashboard.route) {
            com.tfg.umeegunero.feature.familiar.screen.FamiliaDashboardScreen(navController = navController)
        }
        composable(route = AppScreens.AdminDashboard.route) {
            com.tfg.umeegunero.feature.admin.screen.AdminDashboardScreen(
                navController = navController,
                onNavigateToGestionUsuarios = {},
                onNavigateToGestionCentros = {},
                onNavigateToEstadisticas = {},
                onNavigateToSeguridad = {},
                onNavigateToTema = {},
                onNavigateToEmailConfig = {},
                onNavigateToComunicados = {},
                onNavigateToBandejaEntrada = {},
                onNavigateToComponerMensaje = {},
                onNavigateToSoporteTecnico = {},
                onNavigateToFAQ = {},
                onNavigateToTerminos = {},
                onNavigateToLogout = {},
                onNavigateToProfile = {}
            )
        }
        // Perfil
        composable(route = AppScreens.Perfil.route) {
            PerfilScreen(navController = navController)
        }
        // Detalle de alumno
        composable(
            route = AppScreens.DetalleAlumno.route,
            arguments = AppScreens.DetalleAlumno.arguments
        ) { backStackEntry ->
            val dni = backStackEntry.arguments?.getString("dni") ?: ""
            
            // Determinar el tipo de usuario actual
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val email = currentUser.email ?: ""
                // Usar pantalla completa para profesores y administradores
                if (email.contains("profesor") || email.contains("admin") || email.contains("centro")) {
                    // Si es profesor o administrador, mostrar la vista completa
                    com.tfg.umeegunero.feature.profesor.screen.DetalleAlumnoProfesorScreen(
                        navController = navController,
                        alumnoId = dni
                    )
                } else {
                    // Solo para familiares mostrar una vista más limitada
                    com.tfg.umeegunero.feature.common.users.screen.UserDetailScreen(
                        navController = navController,
                        userId = dni
                    )
                }
            } else {
                // Si no hay usuario logueado, mostrar vista básica
                com.tfg.umeegunero.feature.common.users.screen.UserDetailScreen(
                    navController = navController,
                    userId = dni
                )
            }
        }
    }
} 