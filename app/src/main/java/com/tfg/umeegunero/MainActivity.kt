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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.util.Result
import androidx.core.content.res.ResourcesCompat
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.tfg.umeegunero.data.repository.StorageRepository
import androidx.core.net.toUri
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import android.graphics.Matrix

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
        
        // Forzar la actualización del avatar del administrador
        actualizarAvatarAdministrador()
        
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
                
                // Verificar si existe algún administrador en la base de datos
                checkAndCreateDefaultAdmin()
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
     * Verifica si existe algún usuario administrador, y si no existe, crea uno por defecto
     */
    private fun checkAndCreateDefaultAdmin() {
        val firestore = FirebaseFirestore.getInstance()
        val EMAIL_ADMIN_DEFAULT = "admin@eguneroko.com"
        
        // Inicializar y configurar Remote Config
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hora
            .build()
        
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // No establecemos valores por defecto, debe configurarse en la consola de Firebase
        val defaults = mapOf("SMTP_PASSWORD" to "")
        remoteConfig.setDefaultsAsync(defaults)
        
        // Obtener la última configuración de Firebase Remote Config
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Obtener la contraseña de Remote Config
                val password = remoteConfig.getString("SMTP_PASSWORD")
                
                if (password.isBlank()) {
                    Timber.w("No se ha configurado SMTP_PASSWORD en Firebase Remote Config")
                    showErrorToast("Es necesario configurar la clave SMTP_PASSWORD en Firebase Remote Config para crear un administrador")
                    return@addOnCompleteListener
                }
                
                Timber.d("Contraseña obtenida de Remote Config correctamente")
                
                // Primer paso: Verificar si ya existe un administrador en Firestore
                firestore.collection("usuarios")
                    .whereEqualTo("perfiles.0.tipo", TipoUsuario.ADMIN_APP.name)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { adminQuerySnapshot ->
                        val existeAdmin = !adminQuerySnapshot.isEmpty
                        
                        if (existeAdmin) {
                            Timber.d("Ya existe al menos un usuario administrador en Firestore")
                            return@addOnSuccessListener
                        }
                        
                        // No hay administrador en Firestore, verificar si existe el email en Auth
                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(EMAIL_ADMIN_DEFAULT)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    val signInMethods = authTask.result?.signInMethods ?: emptyList<String>()
                                    
                                    // Verificar si el email ya existe en Firestore (documento)
                                    firestore.collection("usuarios")
                                        .whereEqualTo("email", EMAIL_ADMIN_DEFAULT)
                                        .get()
                                        .addOnSuccessListener { emailQuerySnapshot ->
                                            val existeEnFirestore = !emailQuerySnapshot.isEmpty
                                            
                                            if (signInMethods.isNotEmpty()) {
                                                // El email ya existe en Auth
                                                Timber.d("El email $EMAIL_ADMIN_DEFAULT ya existe en Firebase Auth")
                                                
                                                if (existeEnFirestore) {
                                                    // Ya existe en Auth y en Firestore, pero no es Admin
                                                    Timber.w("El email $EMAIL_ADMIN_DEFAULT ya está en uso por otro usuario que no es administrador")
                                                    showErrorToast("El email $EMAIL_ADMIN_DEFAULT ya está en uso por otro usuario que no es administrador. Por favor, utiliza otro email para el administrador.")
                                                    return@addOnSuccessListener
                                                } else {
                                                    // Existe en Auth pero no en Firestore, obtener UID e iniciar sesión
                                                    Timber.d("El usuario existe en Auth pero no en Firestore. Iniciando sesión para obtener UID...")
                                                    FirebaseAuth.getInstance().signInWithEmailAndPassword(
                                                        EMAIL_ADMIN_DEFAULT, password
                                                    ).addOnCompleteListener { signInTask ->
                                                        if (signInTask.isSuccessful) {
                                                            val currentUser = signInTask.result?.user
                                                            if (currentUser != null) {
                                                                createAdminDocument(currentUser.uid, EMAIL_ADMIN_DEFAULT)
                                                                // Cerrar sesión después de crear el documento
                                                                FirebaseAuth.getInstance().signOut()
                                                            }
                                                        } else {
                                                            Timber.e(signInTask.exception, "Error al iniciar sesión con el usuario administrador existente")
                                                            showErrorToast("El usuario $EMAIL_ADMIN_DEFAULT existe en Authentication, pero la clave en Remote Config no coincide. Actualiza SMTP_PASSWORD en Firebase Remote Config.")
                                                        }
                                                    }
                                                }
                                            } else {
                                                // No existe en Auth
                                                if (existeEnFirestore) {
                                                    // Existe en Firestore pero no en Auth - Caso anómalo
                                                    Timber.w("El email $EMAIL_ADMIN_DEFAULT existe en Firestore pero no en Auth")
                                                    
                                                    // Eliminar primero el documento en Firestore para evitar duplicados
                                                    val doc = emailQuerySnapshot.documents.first()
                                                    val docId = doc.id
                                                    
                                                    firestore.collection("usuarios").document(docId)
                                                        .delete()
                                                        .addOnSuccessListener {
                                                            Timber.d("Documento inconsistente eliminado. Creando nuevo usuario completo.")
                                                            
                                                            // Ahora creamos el usuario en Auth primero
                                                            crearUsuarioCompleto(EMAIL_ADMIN_DEFAULT, password, firestore)
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Timber.e(e, "Error al eliminar documento inconsistente: ${e.message}")
                                                            showErrorToast("Error al corregir inconsistencia en la base de datos. Contacta con soporte.")
                                                        }
                                                } else {
                                                    // No existe ni en Auth ni en Firestore - Caso normal para crear nuevo
                                                    Timber.d("El usuario no existe ni en Auth ni en Firestore. Creando usuario completo...")
                                                    
                                                    // Crear primero en Auth y luego en Firestore
                                                    crearUsuarioCompleto(EMAIL_ADMIN_DEFAULT, password, firestore)
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Timber.e(e, "Error al verificar si el email ya existe en Firestore: ${e.message}")
                                        }
                                } else {
                                    Timber.e(authTask.exception, "Error al verificar email en Firebase Auth: ${authTask.exception?.message}")
                                    showErrorToast("Error al verificar disponibilidad del email en Firebase. Verifica la conexión a internet.")
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error al buscar administradores: ${e.message}")
                    }
            } else {
                // Error al obtener la configuración
                Timber.e(task.exception, "Error al obtener la configuración de Remote Config: ${task.exception?.message}")
                showErrorToast("No se pudo obtener la configuración desde Firebase Remote Config. Verifica la conexión y que SMTP_PASSWORD esté configurado.")
            }
        }
    }
    
    /**
     * Crea un usuario completo primero en Authentication y luego en Firestore
     */
    private fun crearUsuarioCompleto(email: String, password: String, firestore: FirebaseFirestore) {
        // 1. Crear en Authentication primero
        Timber.d("Creando usuario administrador en Firebase Auth")
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { createTask ->
                if (createTask.isSuccessful) {
                    val user = createTask.result?.user
                    if (user != null) {
                        // 2. Después crear en Firestore
                        createAdminDocument(user.uid, email)
                    } else {
                        Timber.e("Error: usuario creado en Auth pero no se pudo obtener")
                    }
                } else {
                    Timber.e(createTask.exception, "No se pudo crear el usuario administrador en Firebase Auth: ${createTask.exception?.message}")
                    showErrorToast("Error al crear el usuario administrador. Verifica que SMTP_PASSWORD esté configurada correctamente en Firebase Remote Config.")
                }
            }
    }
    
    /**
     * Crea el documento del usuario administrador en Firestore
     */
    private fun createAdminDocument(uid: String, email: String) {
        val firestore = FirebaseFirestore.getInstance()
        
        // DNI personalizado para el administrador
        val dniAdmin = "45678698P"
        
        // Crear perfil de administrador
        val perfil = mapOf(
            "tipo" to TipoUsuario.ADMIN_APP.name,
            "verificado" to true,
            "centroId" to "", // Sin centro específico al ser ADMIN_APP
            "subtipo" to null // No aplica para administradores
        )
        
        // Crear dirección completa
        val direccion = mapOf(
            "calle" to "Calle Mayor",
            "numero" to "25",
            "piso" to "3B",
            "codigoPostal" to "48004",
            "ciudad" to "Bilbao",
            "provincia" to "Bizkaia",
            "pais" to "España",
            "latitud" to "43.2630126",
            "longitud" to "-2.9350039"
        )
        
        // Preferencias más detalladas
        val preferencias = mapOf(
            "idiomaApp" to "es",
            "tema" to "SYSTEM", // LIGHT, DARK o SYSTEM
            "notificaciones" to mapOf(
                "push" to true,
                "email" to true,
                "deviceId" to "device_${System.currentTimeMillis()}",
                "lastUpdated" to Timestamp.now(),
                "fcmToken" to "",
                "fcmTokens" to mapOf<String, String>() // Para múltiples dispositivos
            )
        )
        
        // Timestamp actual para fechas
        val ahora = Timestamp.now()
        
        // URL temporal del avatar - usando el dominio correcto de Firebase Storage
        val defaultAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/avatares%2Fadmin%2F${dniAdmin}.png?alt=media"
        
        // Obtener el StorageRepository mediante inyección de dependencias
        // o directamente para este caso específico
        val storageRepository = StorageRepository(
            applicationContext,
            FirebaseStorage.getInstance(),
            com.tfg.umeegunero.network.ImgBBClient(applicationContext)
        )
        
        // Crear el documento inicialmente con avatar temporal
        val adminData = mapOf(
            "dni" to dniAdmin,
            "nombre" to "Maitane",
            "apellidos" to "Ibañez Irazabal",
            "email" to email,
            "telefono" to "605761050",
            "perfiles" to listOf(perfil),
            "activo" to true,
            "estado" to "ACTIVO", // PENDIENTE, ACTIVO, BLOQUEADO, INACTIVO, ELIMINADO
            "firebaseUid" to uid,
            "fechaRegistro" to ahora,
            "ultimoAcceso" to ahora,
            "direccion" to direccion,
            "preferencias" to preferencias,
            "avatarUrl" to defaultAvatarUrl, // URL referencia a Firebase Storage
            "metadata" to mapOf(
                "createdBy" to "SYSTEM",
                "updatedAt" to ahora,
                "version" to 1,
                "notas" to "Administrador principal creado automáticamente"
            )
        )
        
        // Guardar el documento en Firestore
        firestore.collection("usuarios")
            .document(dniAdmin)
            .set(adminData)
            .addOnSuccessListener {
                Timber.d("✅ Usuario administrador creado correctamente en Firestore")
                showErrorToast("Administrador por defecto creado con email: $email - Puede iniciar sesión ahora")
                
                // Después de crear el usuario, iniciar sesión con sus credenciales para conseguir permisos de escritura
                // y luego subir el avatar
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                
                if (currentUser != null && currentUser.uid == uid) {
                    // Ya estamos autenticados como el usuario recién creado
                    lifecycleScope.launch {
                        subirAvatarDesdeRecursos(storageRepository, dniAdmin)
                    }
                } else {
                    // Necesitamos autenticarnos como usuario admin primero
                    val password = FirebaseRemoteConfig.getInstance().getString("SMTP_PASSWORD")
                    if (password.isNotBlank()) {
                        Timber.d("Iniciando sesión como admin para subir avatar...")
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                Timber.d("Inicio de sesión exitoso, subiendo avatar...")
                                lifecycleScope.launch {
                                    subirAvatarDesdeRecursos(storageRepository, dniAdmin)
                                    // Restaurar el usuario original después
                                    auth.signOut()
                                }
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Error al iniciar sesión para subir avatar: ${e.message}")
                            }
                    } else {
                        Timber.e("No se pudo obtener la contraseña para autenticación")
                    }
                }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "❌ Error al crear usuario administrador en Firestore")
            }
    }
    
    /**
     * Sube un avatar desde los recursos de la aplicación a Firebase Storage
     * y actualiza el documento del usuario con la URL del avatar
     */
    private suspend fun subirAvatarDesdeRecursos(
        storageRepository: StorageRepository,
        userId: String
    ) = withContext(Dispatchers.IO) {
        try {
            // En lugar de buscar un archivo local que no existirá en el dispositivo,
            // usaremos directamente la URL proporcionada
            val avatarUrl = "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/AdminAvatar.png?alt=media&token=83de83e1-f266-40bd-8aec-d46aa02fcea6"
            
            Timber.d("✅ Usando URL de avatar de administrador: $avatarUrl")
            
            // Actualizar directamente el documento en Firestore con la URL proporcionada
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .update("avatarUrl", avatarUrl)
                .addOnSuccessListener {
                    Timber.d("✅ URL de avatar actualizada correctamente en Firestore")
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "❌ Error al actualizar avatar en Firestore: ${e.message}")
                }
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Error general al actualizar URL de avatar: ${e.message}")
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
    
    /**
     * Actualiza el avatar del administrador en Firestore
     * directamente con la URL proporcionada
     */
    private fun actualizarAvatarAdministrador() {
        val firestore = FirebaseFirestore.getInstance()
        val adminAvatarUrl = "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/AdminAvatar.png?alt=media&token=83de83e1-f266-40bd-8aec-d46aa02fcea6"
        
        // Buscar usuarios con perfil ADMIN_APP
        firestore.collection("usuarios")
            .whereEqualTo("perfiles.0.tipo", TipoUsuario.ADMIN_APP.name)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Timber.d("No se encontró ningún administrador para actualizar el avatar")
                    return@addOnSuccessListener
                }
                
                val adminDoc = querySnapshot.documents.first()
                val adminId = adminDoc.id
                
                // Actualizar el campo avatarUrl
                firestore.collection("usuarios")
                    .document(adminId)
                    .update("avatarUrl", adminAvatarUrl)
                    .addOnSuccessListener {
                        Timber.d("✅ Avatar de administrador actualizado correctamente con URL: $adminAvatarUrl")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "❌ Error al actualizar el avatar del administrador: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "❌ Error al buscar administradores: ${e.message}")
            }
    }
}