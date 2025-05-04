package com.tfg.umeegunero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.familiar.onboarding.PermisoNotificacionesScreen
import timber.log.Timber

// Rutas principales
const val RUTA_INICIO = "inicio"
const val RUTA_LOGIN_FAMILIAR = "login_familiar"
const val RUTA_DASHBOARD_FAMILIAR = "dashboard_familiar"
const val RUTA_RECUPERAR_PASSWORD = "recuperar_password"
const val RUTA_PERMISO_NOTIFICACIONES = "permiso_notificaciones"

/**
 * Componente principal de navegación de la aplicación
 */
@Composable
fun NavigationComponent(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = RUTA_INICIO
    ) {
        // Pantalla de inicio
        composable(route = RUTA_INICIO) {
            Timber.d("📱 PANTALLA ACTUAL: inicio (ruta: $RUTA_INICIO)")
            // Aquí iría el código de la pantalla de inicio
        }
        
        // Pantalla de login familiar
        composable(route = RUTA_LOGIN_FAMILIAR) {
            Timber.d("📱 PANTALLA ACTUAL: login_familiar (ruta: $RUTA_LOGIN_FAMILIAR)")
            LoginScreen(
                userType = TipoUsuario.FAMILIAR,
                onNavigateBack = { navController.navigateUp() },
                onLoginSuccess = { 
                    navController.navigate(RUTA_DASHBOARD_FAMILIAR) {
                        popUpTo(RUTA_INICIO) {
                            inclusive = true
                        }
                    }
                },
                onForgotPassword = { email ->
                    navController.navigate("$RUTA_RECUPERAR_PASSWORD/$email")
                },
                onNecesitaPermisos = {
                    navController.navigate(RUTA_PERMISO_NOTIFICACIONES) {
                        popUpTo(RUTA_LOGIN_FAMILIAR) {
                            inclusive = false
                        }
                    }
                }
            )
        }
        
        // Pantalla de permisos de notificaciones
        composable(route = RUTA_PERMISO_NOTIFICACIONES) {
            Timber.d("📱 PANTALLA ACTUAL: permiso_notificaciones (ruta: $RUTA_PERMISO_NOTIFICACIONES)")
            PermisoNotificacionesScreen(
                onContinuar = {
                    navController.navigate(RUTA_DASHBOARD_FAMILIAR) {
                        popUpTo(RUTA_LOGIN_FAMILIAR) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        
        // Pantalla de recuperación de contraseña
        composable(
            route = "$RUTA_RECUPERAR_PASSWORD/{email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            Timber.d("📱 PANTALLA ACTUAL: recuperar_password (ruta: $RUTA_RECUPERAR_PASSWORD/$email)")
            // Aquí iría el código de la pantalla de recuperación de contraseña
        }
        
        // Dashboard familiar
        composable(route = RUTA_DASHBOARD_FAMILIAR) {
            Timber.d("📱 PANTALLA ACTUAL: dashboard_familiar (ruta: $RUTA_DASHBOARD_FAMILIAR)")
            // Aquí iría el código del dashboard familiar
        }
        
        // Aquí se añadirían más rutas según sea necesario
    }
} 