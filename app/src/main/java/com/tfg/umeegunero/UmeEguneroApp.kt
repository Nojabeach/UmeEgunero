package com.tfg.umeegunero

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.jakewharton.threetenabp.AndroidThreeTen
import com.tfg.umeegunero.data.worker.EventoWorker
import com.tfg.umeegunero.data.worker.SincronizacionWorker
import com.tfg.umeegunero.notification.AppNotificationManager
import com.tfg.umeegunero.util.SyncManager
import com.tfg.umeegunero.util.DebugUtils
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Clase principal de la aplicación UmeEgunero.
 * 
 * Se encarga de inicializar componentes principales como Firebase,
 * Timber para logging, y configurar canales de notificación.
 */
@HiltAndroidApp
class UmeEguneroApp : Application(), Configuration.Provider {

    @Inject
    lateinit var notificationManager: AppNotificationManager
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var debugUtils: DebugUtils
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Timber para logging solo en debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Inicializar ThreeTenABP para soporte de Java 8 Date Time API en API < 26
        AndroidThreeTen.init(this)
        
        // Inicializar Firebase con manejo de errores
        initializeFirebase()
        
        // Inicializar canales de notificación
        notificationManager.createNotificationChannels()
        
        // Configurar tareas periódicas
        configurarSincronizacionPeriodica()
        
        // Iniciar el servicio de sincronización
        syncManager.iniciarServicioSincronizacion()
        
        // Crear admin de debug automáticamente si no existe (solo en debug)
        if (BuildConfig.DEBUG) {
            debugUtils.ensureDebugAdminApp()
            
            // Iniciar proceso de subida del avatar de administrador
            subirAvatarAdminInicio()
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        syncManager.detenerServicioSincronizacion()
    }
    
    /**
     * Inicializa Firebase con manejo de errores
     */
    private fun initializeFirebase() {
        try {
            // Inicializar Firebase con configuración predeterminada
            FirebaseApp.initializeApp(this)
            Timber.d("Firebase inicializado correctamente")
            
            // Configurar Firebase Remote Config para desarrollo
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                if (BuildConfig.DEBUG) {
                    minimumFetchIntervalInSeconds = 0 // Sin caché en desarrollo
                } else {
                    minimumFetchIntervalInSeconds = 3600 // 1 hora en producción
                }
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            
            // Intentar un fetch inicial de Remote Config
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Remote Config sincronizado correctamente")
                } else {
                    Timber.w("Error al sincronizar Remote Config: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al inicializar Firebase")
            // Mostrar información detallada del error para depuración
            e.printStackTrace()
            
            // Reintentar la inicialización con una configuración mínima
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(BuildConfig.APPLICATION_ID)
                    .build()
                
                if (FirebaseApp.getApps(this).isEmpty()) {
                    FirebaseApp.initializeApp(this, options)
                    Timber.d("Firebase reinicializado con configuración mínima")
                }
            } catch (e2: Exception) {
                Timber.e(e2, "Error fatal al inicializar Firebase con configuración mínima")
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
        }
    }
    
    companion object {
        internal const val SYNC_WORK_NAME = "sincronizacion_periodica"
        internal const val EVENTOS_WORK_NAME = "revision_eventos"
    }
} 