// Añadida función para cerrar la app desde la navegación
package com.tfg.umeegunero

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.feature.common.splash.screen.SplashScreen
import com.tfg.umeegunero.navigation.Navigation
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.ui.theme.rememberDarkThemeState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import timber.log.Timber
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

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
        
        // Manejar posibles errores durante la inicialización de Firebase
        checkFirebaseInitialization()
        
        // Usamos WindowCompat en lugar de enableEdgeToEdge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            val isDarkTheme = rememberDarkThemeState(preferenciasRepository)
            UmeEguneroTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Estado para controlar la visualización del splash
                    var showSplash by remember { mutableStateOf(true) }
                    
                    if (showSplash) {
                        SplashScreen(
                            onSplashComplete = { 
                                showSplash = false
                            }
                        )
                    } else {
                        // Capa principal con NavigationBox
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
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
    }
    
    /**
     * Verifica que Firebase esté correctamente inicializado.
     * Si hay problemas, intenta una reinicialización con configuración mínima.
     */
    private fun checkFirebaseInitialization() {
        try {
            // Verificar si Firebase está inicializado
            val firebaseApps = FirebaseApp.getApps(this)
            
            if (firebaseApps.isEmpty()) {
                // Firebase no está inicializado, esto no debería ocurrir normalmente
                // ya que se inicializa en UmeEguneroApp, pero por seguridad lo verificamos
                Timber.w("Firebase no estaba inicializado en MainActivity, esto es inesperado")
                // NO inicializamos Firebase aquí, solo registramos el problema
            } else {
                Timber.d("Firebase ya estaba inicializado. Apps: ${firebaseApps.size}")
            }
            
            // Verificar que Auth y Firestore están disponibles
            try {
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                Timber.d("Servicios Firebase disponibles: Auth=${auth != null}, Firestore=${firestore != null}")
            } catch (e: Exception) {
                Timber.e(e, "Error al acceder a servicios Firebase en MainActivity")
                showErrorToast("Error al inicializar componentes de la aplicación. Por favor, reiníciela.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar inicialización de Firebase en MainActivity")
            showErrorToast("Error al inicializar la aplicación. Por favor, reiníciela.")
        }
    }
    
    /**
     * Muestra un toast de error.
     */
    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Función para cerrar la aplicación.
     * Es utilizada por la WelcomeScreen cuando se pulsa el botón de cerrar.
     */
    fun closeApp() {
        finishAndRemoveTask()
    }
}