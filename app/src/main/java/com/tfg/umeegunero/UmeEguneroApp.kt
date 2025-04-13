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
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        
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
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Timber.d("Firebase inicializado correctamente")
            }
            
            // Configurar Remote Config solo si Firebase se inicializó correctamente
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
                val configSettings = remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600
                }
                remoteConfig.setConfigSettingsAsync(configSettings)
                Timber.d("Remote Config configurado correctamente")
            } else {
                Timber.e("Firebase no se pudo inicializar, no se configurará Remote Config")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al inicializar Firebase")
            
            // Crear una configuración predeterminada como fallback
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1045944201521:android:1d17f66b49657aef2ac010")
                    .setApiKey("AIzaSyCO6F98FkXnEoHGS_svEgtWiZdbI3IcVaY")
                    .setProjectId("umeegunero")
                    .build()
                
                FirebaseApp.initializeApp(this, options, "default")
                Timber.d("Firebase inicializado con configuración manual")
            } catch (e2: Exception) {
                Timber.e(e2, "No se pudo inicializar Firebase con configuración manual")
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
    
    companion object {
        internal const val SYNC_WORK_NAME = "sincronizacion_periodica"
        internal const val EVENTOS_WORK_NAME = "revision_eventos"
    }
} 