package com.tfg.umeegunero.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.auth.screen.ForgotPasswordScreen
import com.tfg.umeegunero.feature.auth.screen.LoginScreen
import com.tfg.umeegunero.feature.auth.screen.RegisterScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

// Rutas para la autenticación
sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object Register : AuthScreen("register")
    object ForgotPassword : AuthScreen("forgot_password")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (Firebase.auth.currentUser != null) "main" else "login",
        modifier = modifier
    ) {
        composable("login") {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.material3.Text("Login Screen Placeholder")
            }
        }
        
        composable("register") {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.material3.Text("Register Screen Placeholder")
            }
        }
        
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("main") {
            // Placeholder para MainScreen hasta que se implemente
            androidx.compose.material3.Surface {
                androidx.compose.material3.Text("Main Screen")
            }
        }
    }
} 