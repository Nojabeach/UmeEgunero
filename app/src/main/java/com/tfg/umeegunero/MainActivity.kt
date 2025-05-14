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
import com.google.firebase.messaging.FirebaseMessaging
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tfg.umeegunero.navigation.NavigationViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp

/**
 * Clase simple para representar datos de notificación
 */
data class NotificationData(
    val tipo: String,
    val data: Map<String, String> = emptyMap()
)

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
    
    // Instancia de FirebaseAuth
    private lateinit var auth: FirebaseAuth
    
    // ViewModel para la navegación
    private lateinit var navigationViewModel: NavigationViewModel
    
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        
        // Inicializar auth
        auth = FirebaseAuth.getInstance()
        
        // Inicializamos Firebase Messaging
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Timber.d("FCM Token: $token")
                // Guardar token en Firestore de forma segura
                guardarTokenDeFormaSegura(token)
            } else {
                Timber.e(task.exception, "No se pudo obtener el token de FCM")
            }
        }
        
        // Manejar posibles errores durante la inicialización de Firebase
        checkFirebaseInitialization()
        
        setContent {
            navigationViewModel = viewModel()
            
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
                                onCloseApp = { closeApp() },
                                navigationViewModel = navigationViewModel
                            )
                        }
                    }
                }
            }
            
            // Procesar el intent inicial si se abre desde una notificación
            intent?.let { handleIntent(it) }
        }
    }
    
    /**
     * Maneja un nuevo intent cuando la actividad ya está creada
     * Esto es importante para manejar notificaciones cuando la app está en segundo plano
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    /**
     * Procesa el intent para manejar notificaciones
     */
    private fun handleIntent(intent: Intent) {
        // Extraer datos para la navegación desde notificaciones
        val messageId = intent.getStringExtra("messageId")
        val messageType = intent.getStringExtra("messageType")
        val conversationId = intent.getStringExtra("conversationId")
        
        // Si tenemos un ID de mensaje, manejar la navegación
        if (messageId != null) {
            Timber.d("Notificación procesada - messageId: $messageId, tipo: $messageType, conversationId: $conversationId")
            
            lifecycleScope.launch {
                try {
                    navigationViewModel.handleNotificationNavigation(messageId, messageType)
                    // Registrar analítica de apertura desde notificación
                    Timber.i("Usuario navegó desde notificación: $messageType, messageId: $messageId")
                } catch (e: Exception) {
                    Timber.e(e, "Error al navegar desde notificación")
                    showErrorToast("Error al procesar la notificación")
                }
            }
        } else {
            // Revisa si hay un intent de navegación directo (desde un deep link)
            val destino = intent.getStringExtra("destino")
            val parametros = intent.getStringExtra("parametros")
            
            if (destino != null) {
                Timber.d("Deep link procesado - destino: $destino, parametros: $parametros")
                lifecycleScope.launch {
                    try {
                        // Construir ruta de navegación a partir de destino y parámetros
                        val ruta = if (parametros.isNullOrEmpty()) destino else "$destino/$parametros"
                        navigationViewModel.navigateTo(ruta)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al navegar por deep link")
                    }
                }
            }
        }
    }
    
    /**
     * Guarda el token del dispositivo de forma segura
     */
    private fun guardarTokenDeFormaSegura(token: String) {
        // Verificar que el usuario está autenticado
        val user = auth.currentUser
        if (user == null) {
            Timber.w("No se puede guardar el token FCM porque no hay usuario autenticado")
            return
        }
        
        try {
            // Primero obtenemos el DNI del usuario actual desde Firestore
            val userEmail = user.email
            if (userEmail.isNullOrEmpty()) {
                Timber.w("No se puede guardar el token FCM porque el usuario no tiene email")
                return
            }
            
            // Consultar el documento del usuario por email para obtener su DNI
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Timber.e("No se encontró usuario con email: $userEmail")
                        return@addOnSuccessListener
                    }
                    
                    val userDoc = querySnapshot.documents.first()
                    val dni = userDoc.getString("dni")
                    
                    if (dni.isNullOrEmpty()) {
                        Timber.e("El usuario no tiene DNI asignado: $userEmail")
                        return@addOnSuccessListener
                    }
                    
                    Timber.d("Guardando token FCM para el usuario con DNI: $dni")
                    
                    // Generar un ID único para el dispositivo
                    val deviceId = "device_${System.currentTimeMillis()}"
                    
                    // Estructura para actualizar el token FCM en las preferencias de notificaciones
                    // Siguiendo la estructura del usuario que has mostrado
                    val tokenUpdate = mapOf(
                        "preferencias.notificaciones.fcmToken" to token,
                        "preferencias.notificaciones.deviceId" to deviceId,
                        "preferencias.notificaciones.lastUpdated" to Timestamp.now()
                    )
                    
                    // Actualizar el documento con el DNI como ID
                    val dniDocRef = FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(dni)
                    
                    dniDocRef.update(tokenUpdate)
                        .addOnSuccessListener {
                            Timber.d("Token FCM actualizado correctamente en preferencias del usuario con DNI: $dni")
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "Error al actualizar token FCM en documento DNI: $dni")
                            
                            // Si el error es porque no existe el campo preferencias.notificaciones,
                            // intentamos crearlo con una estructura completa
                            val initialData = mapOf(
                                "preferencias" to mapOf(
                                    "notificaciones" to mapOf(
                                        "fcmToken" to token,
                                        "deviceId" to deviceId,
                                        "lastUpdated" to Timestamp.now(),
                                        "push" to true // Habilitamos push por defecto al registrar token
                                    )
                                )
                            )
                            
                            dniDocRef.set(initialData, SetOptions.merge())
                                .addOnSuccessListener {
                                    Timber.d("Preferencias de notificaciones creadas para usuario DNI: $dni")
                                }
                                .addOnFailureListener { innerE ->
                                    Timber.e(innerE, "Error al crear preferencias de notificaciones para DNI: $dni")
                                }
                        }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Error al buscar usuario por email: $userEmail")
                }
        } catch (e: Exception) {
            Timber.e(e, "Error general al guardar token FCM: ${e.message}")
        }
    }
    
    /**
     * Verifica la inicialización correcta de Firebase
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
                // Solo verificamos que podemos obtener las instancias sin asignarlas a variables
                FirebaseAuth.getInstance()
                FirebaseFirestore.getInstance()
                Timber.d("Servicios Firebase disponibles")
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