package com.tfg.umeegunero.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
            val intent = Intent(context, SyncService::class.java)
            
            // En Android 12 (API 31) y superior, hay restricciones para iniciar servicios en primer plano
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Verificar si la app está en primer plano antes de usar startForegroundService
                if (isAppInForeground()) {
                    Timber.d("Iniciando servicio de sincronización en primer plano")
                    context.startForegroundService(intent)
                } else {
                    Timber.d("App en segundo plano, iniciando servicio normal")
                    context.startService(intent)
                }
            } else {
                // Para versiones anteriores, usar startService normalmente
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Capturar y registrar cualquier error al iniciar el servicio
            Timber.e(e, "Error al iniciar el servicio de sincronización")
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