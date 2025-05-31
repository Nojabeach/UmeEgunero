/**
 * Ejemplo de integración del sistema de cambio de contraseña obligatorio
 * en el grafo de navegación de UmeEgunero
 */

// En tu NavGraph principal
composable(
    route = "login/{userType}",
    arguments = listOf(navArgument("userType") { type = NavType.StringType })
) { backStackEntry ->
    val userTypeString = backStackEntry.arguments?.getString("userType") ?: ""
    val userType = TipoUsuario.valueOf(userTypeString)
    
    LoginScreen(
        userType = userType,
        onNavigateBack = { navController.popBackStack() },
        onLoginSuccess = { tipo ->
            // Login exitoso normal - navegar al dashboard correspondiente
            val destination = when (tipo) {
                TipoUsuario.ADMIN_APP -> "admin_dashboard"
                TipoUsuario.ADMIN_CENTRO -> "centro_dashboard"
                TipoUsuario.PROFESOR -> "profesor_dashboard"
                TipoUsuario.FAMILIAR -> "familiar_dashboard"
                else -> "dashboard"
            }
            navController.navigate(destination) {
                popUpTo("login/{userType}") { inclusive = true }
            }
        },
        onForgotPassword = { email ->
            navController.navigate("recuperar_password?email=$email")
        },
        onNecesitaPermisos = {
            navController.navigate("configurar_permisos")
        },
        onNecesitaCambioContrasena = { dni, tipo, requiereNueva ->
            // 🔐 NUEVA FUNCIONALIDAD: Navegar al cambio de contraseña obligatorio
            navController.navigate(
                "cambio_contrasena_login/$dni?userType=${tipo.name}&requiereNueva=$requiereNueva"
            ) {
                // No permitir volver atrás hasta completar el cambio
                popUpTo("login/{userType}") { inclusive = false }
            }
        }
    )
}

// Nueva ruta para cambio de contraseña desde login
composable(
    route = "cambio_contrasena_login/{dni}?userType={userType}&requiereNueva={requiereNueva}",
    arguments = listOf(
        navArgument("dni") { type = NavType.StringType },
        navArgument("userType") { 
            type = NavType.StringType
            defaultValue = TipoUsuario.DESCONOCIDO.name 
        },
        navArgument("requiereNueva") { 
            type = NavType.BoolType
            defaultValue = false 
        }
    )
) { backStackEntry ->
    val dni = backStackEntry.arguments?.getString("dni") ?: ""
    val userTypeString = backStackEntry.arguments?.getString("userType") ?: ""
    val requiereNueva = backStackEntry.arguments?.getBoolean("requiereNueva") ?: false
    val userType = try { 
        TipoUsuario.valueOf(userTypeString) 
    } catch (e: Exception) { 
        TipoUsuario.DESCONOCIDO 
    }
    
    // Obtener el LoginViewModel desde el BackStackEntry anterior para completar el login
    val loginViewModel: LoginViewModel = hiltViewModel(
        remember { navController.getBackStackEntry("login/${userType.name}") }
    )
    
    CambioContrasenaScreen(
        dni = dni,
        isFromLogin = true,
        requiereNuevaContrasena = requiereNueva,
        userType = userType,
        onNavigateBack = {
            // Si viene del login, limpiar estado y volver al login
            loginViewModel.limpiarEstadoCambioContrasena()
            navController.popBackStack("login/${userType.name}", false)
        },
        onLoginCompleted = {
            // 🎉 Completar el proceso de login después del cambio exitoso
            loginViewModel.completarLoginDespuesCambioContrasena()
            
            // Navegar al dashboard correspondiente
            val destination = when (userType) {
                TipoUsuario.ADMIN_APP -> "admin_dashboard"
                TipoUsuario.ADMIN_CENTRO -> "centro_dashboard"
                TipoUsuario.PROFESOR -> "profesor_dashboard"
                TipoUsuario.FAMILIAR -> "familiar_dashboard"
                else -> "dashboard"
            }
            
            navController.navigate(destination) {
                // Limpiar todo el stack hasta llegar al dashboard
                popUpTo("login/${userType.name}") { inclusive = true }
            }
        }
    )
}

// Ruta normal para cambio de contraseña desde perfil (mantener funcionalidad existente)
composable(
    route = "cambio_contrasena/{dni}",
    arguments = listOf(navArgument("dni") { type = NavType.StringType })
) { backStackEntry ->
    val dni = backStackEntry.arguments?.getString("dni") ?: ""
    
    CambioContrasenaScreen(
        dni = dni,
        onNavigateBack = { navController.popBackStack() },
        onPasswordChanged = { 
            navController.popBackStack()
            // Mostrar mensaje de éxito
        }
    )
} 