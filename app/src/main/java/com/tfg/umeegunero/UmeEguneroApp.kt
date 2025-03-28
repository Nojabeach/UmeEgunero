package com.tfg.umeegunero

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.BuildConfig
import com.tfg.umeegunero.data.worker.SincronizacionWorker
import com.tfg.umeegunero.util.DebugUtils
import com.tfg.umeegunero.util.RemoteConfigManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class UmeEguneroApp : Application() {

    @Inject
    lateinit var debugUtils: DebugUtils

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Timber solo en debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Inicializar Remote Config
        RemoteConfigManager.getInstance().initialize(this)

        // Me aseguro de que haya un admin en el sistema
        debugUtils.ensureAdminExists()
        
        // Configurar el trabajo periódico de sincronización
        configurarSincronizacionPeriodica()
    }
    
    /**
     * Configura un trabajo periódico para sincronizar registros pendientes.
     * 
     * El trabajo se ejecutará cada 15 minutos cuando haya conexión a Internet.
     */
    private fun configurarSincronizacionPeriodica() {
        Timber.d("Configurando sincronización periódica")
        
        // Definimos las restricciones: necesitamos conexión a internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Creamos la solicitud de trabajo periódica (cada 15 minutos)
        val sincronizacionRequest = PeriodicWorkRequestBuilder<SincronizacionWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        // Registramos el trabajo, reemplazando cualquier trabajo previo con el mismo nombre
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            SincronizacionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            sincronizacionRequest
        )
    }
} 