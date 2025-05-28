package com.tfg.umeegunero.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tfg.umeegunero.data.worker.SincronizacionWorker
import com.tfg.umeegunero.service.SyncService
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de sincronización que se encarga de iniciar y detener el servicio 
 * de sincronización en segundo plano.
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Inicia el servicio de sincronización en segundo plano.
     */
    fun iniciarServicioSincronizacion() {
        try {
            // En Android 12 (API 31) y superior, hay restricciones severas para iniciar servicios en primer plano
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Para Android 12+, usar WorkManager en lugar de un servicio en primer plano
                // cuando la app está en segundo plano
                if (!isAppInForeground()) {
                    Timber.d("App en segundo plano en Android 12+, usando WorkManager")
                    iniciarSincronizacionConWorkManager()
                    return
                }
            }
            
            val intent = Intent(context, SyncService::class.java)
            
            // Verificar si la app está en primer plano antes de usar startForegroundService
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isAppInForeground()) {
                    Timber.d("Iniciando servicio de sincronización en primer plano")
                    context.startForegroundService(intent)
                } else {
                    // Para versiones anteriores a Android 12, intentar con startService normal
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        Timber.d("App en segundo plano, iniciando servicio normal")
                        context.startService(intent)
                    } else {
                        // En Android 12+, usar WorkManager si estamos en segundo plano
                        Timber.d("App en segundo plano en Android 12+, usando WorkManager como fallback")
                        iniciarSincronizacionConWorkManager()
                    }
                }
            } else {
                // Para versiones anteriores a Android O, usar startService normalmente
                context.startService(intent)
            }
        } catch (e: android.app.ForegroundServiceStartNotAllowedException) {
            // Capturar específicamente la excepción de servicio en primer plano
            Timber.w("No se puede iniciar servicio en primer plano, usando WorkManager")
            iniciarSincronizacionConWorkManager()
        } catch (e: Exception) {
            // Capturar y registrar cualquier otro error al iniciar el servicio
            Timber.e(e, "Error al iniciar el servicio de sincronización")
            FirebaseCrashlytics.getInstance().recordException(e)
            // Intentar con WorkManager como fallback
            iniciarSincronizacionConWorkManager()
        }
    }

    /**
     * Inicia la sincronización usando WorkManager como alternativa
     */
    private fun iniciarSincronizacionConWorkManager() {
        try {
            val workRequest = OneTimeWorkRequestBuilder<SincronizacionWorker>()
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync_work",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Timber.d("Sincronización programada con WorkManager")
        } catch (e: Exception) {
            Timber.e(e, "Error al programar sincronización con WorkManager")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    /**
     * Verifica si la aplicación está actualmente en primer plano
     */
    private fun isAppInForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    /**
     * Detiene el servicio de sincronización en segundo plano.
     */
    fun detenerServicioSincronizacion() {
        try {
            val intent = Intent(context, SyncService::class.java)
            context.stopService(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error al detener el servicio de sincronización")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
} 