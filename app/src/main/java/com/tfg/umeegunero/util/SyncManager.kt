package com.tfg.umeegunero.util

import android.content.Context
import android.content.Intent
import com.tfg.umeegunero.service.SyncService
import dagger.hilt.android.qualifiers.ApplicationContext
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
        val intent = Intent(context, SyncService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Detiene el servicio de sincronización en segundo plano.
     */
    fun detenerServicioSincronizacion() {
        val intent = Intent(context, SyncService::class.java)
        context.stopService(intent)
    }
} 