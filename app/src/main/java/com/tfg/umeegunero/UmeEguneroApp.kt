package com.tfg.umeegunero

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.tfg.umeegunero.data.worker.EventoWorker
import com.tfg.umeegunero.data.worker.SincronizacionWorker
import com.tfg.umeegunero.notification.AppNotificationManager
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
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Timber para logging
        Timber.plant(Timber.DebugTree())
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Configurar Remote Config
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Inicializar canales de notificación
        notificationManager.createNotificationChannels()
        
        // Configurar tareas periódicas
        configurarSincronizacionPeriodica()
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
                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                syncRequest
            )
            
            enqueueUniquePeriodicWork(
                EVENTOS_WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                eventosRequest
            )
        }
        
        Timber.d("Sincronización periódica configurada")
        Timber.d("Revisión periódica de eventos configurada")
    }
    
    companion object {
        private const val SYNC_WORK_NAME = "sincronizacion_periodica"
        private const val EVENTOS_WORK_NAME = "revision_eventos"
    }
} 