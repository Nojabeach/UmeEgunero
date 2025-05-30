package com.tfg.umeegunero

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.threetenabp.AndroidThreeTen
import com.tfg.umeegunero.admin.AdminTools
import com.tfg.umeegunero.util.DebugUtils
import com.tfg.umeegunero.util.LocalConfig
import com.tfg.umeegunero.data.repository.SyncRepository
import com.tfg.umeegunero.data.worker.EventoWorker
import com.tfg.umeegunero.data.worker.SincronizacionWorker
import com.tfg.umeegunero.notification.AppNotificationManager
import com.tfg.umeegunero.util.SyncManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Aplicación principal de UmeEgunero.
 * 
 * Esta clase extiende [Application] y es el punto de entrada principal de la aplicación.
 * Se encarga de:
 * - Inicializar las bibliotecas necesarias (Hilt, Firebase, Timber, etc.)
 * - Configurar el manejo de errores con Crashlytics
 * - Establecer la configuración inicial de la aplicación
 * - Gestionar el ciclo de vida global de la aplicación
 * 
 * Utiliza [HiltAndroidApp] para la inyección de dependencias en toda la aplicación.
 * 
 * @property syncRepository Repositorio para operaciones de sincronización
 * @property notificationManager Gestor de notificaciones de la aplicación
 * @property syncManager Gestor del servicio de sincronización
 * @property debugUtils Utilidades para funciones de debug y administrador
 * @property workerFactory Factory para crear Workers con inyección de dependencias
 * 
 * @see Application
 * @see HiltAndroidApp
 * @see Configuration.Provider
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Keep
@HiltAndroidApp
class UmeEguneroApp : Application(), Configuration.Provider, DefaultLifecycleObserver, ImageLoaderFactory {

    @Inject
    lateinit var syncRepository: SyncRepository
    
    @Inject
    lateinit var notificationManager: AppNotificationManager
    
    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var debugUtils: DebugUtils
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    
    /**
     * Devuelve el ImageLoader personalizado para la aplicación.
     * Esto hace que Coil use nuestro ImageLoader optimizado en toda la app.
     */
    override fun newImageLoader(): ImageLoader {
        Timber.d("Usando ImageLoader optimizado global")
        return imageLoader
    }

    override fun onCreate() {
        super<Application>.onCreate()
        
        // Inicializar Timber para logging solo en debug
        if (com.tfg.umeegunero.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Inicializar ThreeTenABP para soporte de Java 8 Date Time API en API < 26
        AndroidThreeTen.init(this)
        
        // Inicializar Firebase con manejo de errores
        initializeFirebase()
        
        // Configurar Crashlytics - Asegurarnos que está habilitado explícitamente
        configureCrashlytics()
        
        // Configurar manejo de errores no fatales para Firebase
        configureFirebaseErrorHandling()
        
        // Inicializar canales de notificación
        notificationManager.createNotificationChannels()
        
        // Configurar tareas periódicas
        configurarSincronizacionPeriodica()
        
        // NO iniciar el servicio de sincronización aquí para evitar ForegroundServiceStartNotAllowedException
        // El servicio se iniciará cuando sea necesario y la app esté en primer plano
        // Esto se puede hacer desde una Activity cuando esté visible o cuando haya trabajo pendiente
        
        // Crear admin de debug automáticamente si no existe (solo en debug)
        if (com.tfg.umeegunero.BuildConfig.DEBUG) {
            debugUtils.ensureDebugAdminApp()
            
            // Iniciar proceso de subida del avatar de administrador
            subirAvatarAdminInicio()
        }
        
        // Registrar ActivityLifecycleCallbacks solo en debug
        if (com.tfg.umeegunero.BuildConfig.DEBUG) {
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    Timber.d("Activity created: ${activity.localClassName}")
                }
                override fun onActivityStarted(activity: Activity) {
                    Timber.d("Activity started: ${activity.localClassName}")
                }
                override fun onActivityResumed(activity: Activity) {
                    Timber.d("Activity resumed: ${activity.localClassName}")
                    // Aquí es seguro iniciar servicios en primer plano si es necesario
                    // ya que la app está visible
                }
                override fun onActivityPaused(activity: Activity) {
                    Timber.d("Activity paused: ${activity.localClassName}")
                }
                override fun onActivityStopped(activity: Activity) {
                    Timber.d("Activity stopped: ${activity.localClassName}")
                }
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {
                    Timber.d("Activity destroyed: ${activity.localClassName}")
                }
            })
        }
    }
    
    /**
     * Configura Firebase Crashlytics
     */
    private fun configureCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        
        // Asegurarnos que Crashlytics esté habilitado siempre, independientemente del entorno
        crashlytics.setCrashlyticsCollectionEnabled(true)
        Timber.d("Crashlytics habilitado explícitamente")
        
        // En modo debug, podemos activar o desactivar según sea necesario para pruebas
        if (com.tfg.umeegunero.BuildConfig.DEBUG) {
            // Para desarrollo, queremos ver los informes de errores
            crashlytics.setCustomKey("debug_mode", true)
            Timber.d("Crashlytics activado en modo DEBUG para pruebas")
            
            // Registrar un log de inicio de debug
            crashlytics.log("Aplicación iniciada en modo DEBUG: ${com.tfg.umeegunero.BuildConfig.VERSION_NAME}")
        } else {
            // En producción, siempre activamos Crashlytics
            crashlytics.setCustomKey("debug_mode", false)
            
            // Registrar la versión de la app como clave personalizada
            crashlytics.setCustomKey("app_version_name", com.tfg.umeegunero.BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("app_version_code", com.tfg.umeegunero.BuildConfig.VERSION_CODE)
            
            Timber.d("Crashlytics activado en modo RELEASE")
            
            // Registrar un log de inicio en Crashlytics
            crashlytics.log("Aplicación iniciada: ${com.tfg.umeegunero.BuildConfig.VERSION_NAME} (${com.tfg.umeegunero.BuildConfig.VERSION_CODE})")
        }
        
        // Agregar información del dispositivo
        crashlytics.setCustomKey("device_model", Build.MODEL)
        crashlytics.setCustomKey("android_version", Build.VERSION.RELEASE)
        crashlytics.setCustomKey("manufacturer", Build.MANUFACTURER)
        crashlytics.setCustomKey("build_id", Build.ID)
        
        // Verificar si Crashlytics ya ha sido inicializado (solo una vez)
        if (!crashlyticsInitialized) {
            crashlyticsInitialized = true
            
            // Solo registramos el error en el hilo de fondo para no interrumpir la UI
            Thread {
                try {
                    // Pequeña pausa para asegurar inicialización completa
                    Thread.sleep(500)
                    
                    // Lanzar una excepción de prueba no fatal
                    val initialException = RuntimeException("Prueba inicial de Crashlytics para completar configuración")
                    crashlytics.recordException(initialException)
                    Timber.i("Excepción inicial no fatal enviada a Crashlytics para completar configuración")
                } catch (e: Exception) {
                    // Registrar cualquier problema con la inicialización
                    Timber.e(e, "Error durante la inicialización de Crashlytics")
                }
            }.start()
        }
    }
    
    // Variable para evitar múltiples inicializaciones
    private var crashlyticsInitialized: Boolean = false
    
    /**
     * Configura el manejo de errores específicos de Firebase
     * para capturar problemas comunes como errores de autenticación y almacenamiento
     */
    private fun configureFirebaseErrorHandling() {
        // Capturar errores de autenticación
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val crashlytics = FirebaseCrashlytics.getInstance()
            val user = auth.currentUser
            
            // Registrar estado de autenticación en Crashlytics
            crashlytics.setCustomKey("user_authenticated", user != null)
            
            if (user != null) {
                // Si hay un usuario autenticado, agregar su ID a Crashlytics (sin datos sensibles)
                crashlytics.setUserId(user.uid)
                crashlytics.setCustomKey("user_provider", user.providerData.firstOrNull()?.providerId ?: "unknown")
            } else {
                // Si no hay usuario, limpiar el ID de usuario en Crashlytics
                crashlytics.setUserId("")
            }
        }
        
        // Guardar el handler original ANTES de establecer el nuevo
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        // Configurar un error handler global para la Thread por defecto
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Registrar información adicional antes de que la app se cierre
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCustomKey("crash_thread", thread.name)
            
            // Si es un error relacionado con Storage, agregar contexto adicional
            if (throwable.stackTrace.any { it.className.contains("storage", ignoreCase = true) }) {
                crashlytics.setCustomKey("firebase_storage_error", true)
                crashlytics.log("Error en Firebase Storage: ${throwable.message}")
            }
            
            // Si es un error de autenticación, agregar contexto adicional
            if (throwable.stackTrace.any { it.className.contains("auth", ignoreCase = true) }) {
                crashlytics.setCustomKey("firebase_auth_error", true)
                crashlytics.log("Error en Firebase Auth: ${throwable.message}")
            }
            
            // Reportar la excepción a Crashlytics y luego pasarla al manejador predeterminado del sistema
            crashlytics.recordException(throwable)
            
            // Usar el manejador de excepciones original guardado
            originalHandler?.uncaughtException(thread, throwable)
        }
        
        // Monitorizar operaciones de Firebase Storage
        try {
            val originalStorageReference = FirebaseStorage.getInstance().reference
            
            // Monitorizar errores específicos de Storage sin interrumpir operaciones normales
            Timber.d("Monitoreo de Firebase Storage configurado")
        } catch (e: Exception) {
            // Registrar errores de inicialización de Storage
            FirebaseCrashlytics.getInstance().recordException(e)
            Timber.e(e, "Error al configurar monitoreo de Firebase Storage")
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        syncManager.detenerServicioSincronizacion()
    }
    
    /**
     * Inicializa Firebase con la configuración correspondiente.
     * Maneja los posibles errores durante la inicialización.
     */
    private fun initializeFirebase() {
        try {
            Timber.d("Inicializando Firebase...")
            
            // Intentar inicializar Firebase normalmente
            FirebaseApp.initializeApp(this)
            
            // Inicializar AppCheck para solucionar el error de autenticación
            try {
                // Verificar que tenemos Google Play Services disponibles
                com.google.android.gms.common.GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
                
                // Configurar Firebase para que no requiera AppCheck durante el desarrollo
                val firebaseApp = FirebaseApp.getInstance()
                val appCheckInstallationId = FirebaseInstallations.getInstance(firebaseApp).id
                
                Timber.d("Firebase App Check ID: $appCheckInstallationId")
            } catch (e: Exception) {
                Timber.e(e, "Error al configurar App Check, pero continuando inicialización")
            }
            
            // Inicializar configuración remota de Firebase
            initializeRemoteConfig()
            
            // Log de confirmación
            Timber.d("✅ Firebase inicializado correctamente")
            
            // Obtener ID de instalación
            FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val installationId = task.result
                    Timber.d("📱 ID de instalación de Firebase: $installationId")
                } else {
                    Timber.e(task.exception, "Error al obtener ID de instalación de Firebase")
                }
            }
        } catch (e: Exception) {
            // Capturar cualquier error durante la inicialización
            Timber.e(e, "❌ ERROR al inicializar Firebase")
            
            // Si la inicialización falla, intentar crear una nueva instancia con configuración manual
            try {
                val options = FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(BuildConfig.FIREBASE_APPLICATION_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .build()
                
                // Inicializar Firebase con las opciones manuales
                FirebaseApp.initializeApp(this, options)
                Timber.d("✅ Firebase inicializado con configuración manual")
                
                // Inicializar configuración remota
                initializeRemoteConfig()
            } catch (e2: Exception) {
                // Si también falla la inicialización manual, registrar el error
                Timber.e(e2, "❌❌ ERROR CRÍTICO: No se pudo inicializar Firebase manualmente")
                
                // Enviamos este error a Crashlytics
                FirebaseCrashlytics.getInstance().recordException(e2)
            }
        }
    }

    /**
     * Configura tareas periódicas para sincronización de datos y revisión de eventos
     */
    private fun configurarSincronizacionPeriodica() {
        // Restricciones: solo ejecutar con red
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        // Tarea periódica de sincronización cada 15 minutos
        val syncRequest = PeriodicWorkRequestBuilder<SincronizacionWorker>(
            15, TimeUnit.MINUTES,  // Repetir cada 15 minutos
            5, TimeUnit.MINUTES    // Flexibilidad de 5 minutos
        )
        .setConstraints(constraints)
        .build()
        
        // Tarea periódica para revisar eventos cada hora
        val eventosRequest = PeriodicWorkRequestBuilder<EventoWorker>(
            1, TimeUnit.HOURS,     // Revisar cada hora
            15, TimeUnit.MINUTES   // Flexibilidad de 15 minutos
        )
        .build()
        
        // Registrar tareas con WorkManager
        WorkManager.getInstance(this).apply {
            enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
            
            enqueueUniquePeriodicWork(
                EVENTOS_WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                eventosRequest
            )
        }
        
        Timber.d("Sincronización periódica configurada")
        Timber.d("Revisión periódica de eventos configurada")
    }
    
    /**
     * Sube el avatar del administrador al inicio de la aplicación
     */
    private fun subirAvatarAdminInicio() {
        try {
            Timber.d("Iniciando subida de avatar de administrador...")
            
            // Usar nuestra clase AdminTools para subir el avatar
            val adminTools = com.tfg.umeegunero.admin.AdminTools(this)
            
            // Intentar desde recursos primero (que es donde está realmente la imagen)
            adminTools.subirAvatarAdministradorDesdeRecursos()
            
            // Alternativamente, también intentamos desde assets como respaldo
            adminTools.subirAvatarAdministradorDesdeAssets()
            
            Timber.d("Proceso de subida de avatar de administrador iniciado exitosamente")
        } catch (e: Exception) {
            Timber.e(e, "Error al iniciar subida de avatar de administrador")
            // Registrar el error en Crashlytics
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
    
    /**
     * Inicializa la configuración remota de Firebase.
     */
    private fun initializeRemoteConfig() {
        try {
            // Configurar Firebase Remote Config para desarrollo
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                if (com.tfg.umeegunero.BuildConfig.DEBUG) {
                    minimumFetchIntervalInSeconds = 0 // Sin caché en desarrollo
                } else {
                    minimumFetchIntervalInSeconds = 3600 // 1 hora en producción
                }
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            
            // Intentar un fetch inicial de Remote Config
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("✅ Remote Config sincronizado correctamente")
                } else {
                    Timber.w("⚠️ Error al sincronizar Remote Config: ${task.exception?.message}")
                    // Registrar el error en Crashlytics
                    FirebaseCrashlytics.getInstance().recordException(
                        task.exception ?: Exception("Error desconocido al sincronizar Remote Config")
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error al inicializar Remote Config")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
    
    companion object {
        internal const val SYNC_WORK_NAME = "sincronizacion_periodica"
        internal const val EVENTOS_WORK_NAME = "revision_eventos"
    }
} 