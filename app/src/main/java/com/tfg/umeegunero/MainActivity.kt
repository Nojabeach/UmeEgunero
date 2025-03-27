// Añadida función para cerrar la app desde la navegación
package com.tfg.umeegunero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.feature.common.splash.screen.SplashScreen
import com.tfg.umeegunero.feature.common.welcome.screen.WelcomeScreen
import com.tfg.umeegunero.navigation.Navigation
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.ui.theme.rememberDarkThemeState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferenciasRepository: PreferenciasRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UmeEguneroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(
                            onSplashComplete = {
                                showSplash = false
                            }
                        )
                    } else {
                        val isDarkTheme = rememberDarkThemeState(preferenciasRepository)
                        val navController = rememberNavController()
                        Navigation(
                            navController = navController,
                            onCloseApp = { finish() }
                        )
                    }
                }
            }
        }
    }
}