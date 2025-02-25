package com.tfg.umeegunero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UmeEguneroTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current

                    WelcomeScreen(
                        onNavigateToLogin = { userType ->
                            // Aquí implementaremos la navegación a la pantalla de login
                            // cuando configuremos la navegación completa
                        },
                        onNavigateToRegister = {
                            // Navegación a registro
                        },
                        onNavigateToAdminLogin = {
                            // Navegación a login de administrador
                        },
                        onCloseApp = {
                            // Cerrar la aplicación
                            finishAffinity()
                        }
                    )
                }
            }
        }
    }
}