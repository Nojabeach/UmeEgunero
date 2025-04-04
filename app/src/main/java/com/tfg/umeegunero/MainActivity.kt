// Añadida función para cerrar la app desde la navegación
package com.tfg.umeegunero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.feature.common.splash.screen.SplashScreen
import com.tfg.umeegunero.navigation.Navigation
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.ui.theme.rememberDarkThemeState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Actividad principal de la aplicación UmeEgunero.
 * 
 * Esta actividad funciona como punto de entrada de la aplicación y se encarga de:
 * - Inicializar la pantalla de splash mediante la API SplashScreen de Android
 * - Configurar el tema de la aplicación (claro/oscuro) según las preferencias del usuario
 * - Establecer el sistema de navegación de la aplicación
 * - Administrar el flujo inicial de la aplicación (splash → welcome → navegación principal)
 *
 * La actividad utiliza Jetpack Compose para la construcción de su interfaz y
 * Hilt para la inyección de dependencias.
 *
 * @see SplashScreen Para la implementación de la pantalla de splash
 * @see Navigation Para la implementación del sistema de navegación
 * @see UmeEguneroTheme Para la configuración del tema de la aplicación
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    /**
     * Repositorio de preferencias inyectado por Hilt.
     * Se utiliza para obtener y almacenar las preferencias del usuario,
     * como el modo oscuro/claro.
     */
    @Inject
    lateinit var preferenciasRepository: PreferenciasRepository
    
    /**
     * Método de inicialización de la actividad.
     * 
     * Configura:
     * 1. La pantalla de splash
     * 2. El tema de la aplicación
     * 3. El sistema de navegación
     * 4. Habilita edge-to-edge para una experiencia inmersiva
     *
     * @param savedInstanceState Estado guardado de la actividad
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Usamos WindowCompat en lugar de enableEdgeToEdge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            UmeEguneroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isDarkTheme = rememberDarkThemeState(preferenciasRepository)
                    
                    // Estado para controlar la visualización del splash
                    var showSplash by remember { mutableStateOf(true) }
                    
                    if (showSplash) {
                        SplashScreen(
                            onSplashComplete = { 
                                showSplash = false
                            }
                        )
                    } else {
                        Navigation(
                            navController = navController,
                            startDestination = AppScreens.Welcome.route,
                            onCloseApp = { closeApp() }
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Función para cerrar la aplicación.
     * Es utilizada por la WelcomeScreen cuando se pulsa el botón de cerrar.
     */
    fun closeApp() {
        finishAndRemoveTask()
    }
}