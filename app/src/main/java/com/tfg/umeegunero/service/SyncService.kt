package com.tfg.umeegunero.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.repository.SyncRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Servicio de sincronización en primer plano para procesar operaciones pendientes en UmeEgunero.
 * 
 * Este servicio se ejecuta como un servicio en primer plano (foreground service) para garantizar
 * que Android no lo termine cuando la aplicación no está activa. Se encarga de sincronizar
 * datos locales con Firebase Firestore, procesando operaciones que quedaron pendientes
 * cuando la aplicación estaba sin conexión.
 * 
 * Características principales:
 * - Ejecuta como servicio en primer plano con notificación persistente
 * - Procesa operaciones de sincronización de forma asíncrona
 * - Actualiza la notificación con el progreso de sincronización
 * - Se detiene automáticamente cuando no hay operaciones pendientes
 * - Maneja errores de sincronización y notifica al usuario
 * 
 * El servicio utiliza:
 * - [LifecycleService] para integración con el ciclo de vida de Android
 * - [SyncRepository] para gestionar las operaciones de sincronización
 * - Corrutinas para operaciones asíncronas
 * - Sistema de notificaciones para informar al usuario
 * 
 * @property syncRepository Repositorio que gestiona las operaciones de sincronización
 * @property serviceScope Scope de corrutinas para operaciones del servicio
 * @property notificationManager Gestor de notificaciones del sistema
 * @property sincronizacionJob Job de la corrutina de sincronización actual
 * 
 * @see SyncRepository
 * @see LifecycleService
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@AndroidEntryPoint
class SyncService : LifecycleService() {

    @Inject
    lateinit var syncRepository: SyncRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    private var sincronizacionJob: Job? = null

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "sync_service_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(0), 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification(0))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        // Cancelar el job anterior si existe
        sincronizacionJob?.cancel()
        
        // Iniciar la sincronización
        empezarSincronizacion()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        sincronizacionJob?.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.sync_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.sync_notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(pendingOperations: Int) = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(getString(R.string.sync_notification_title))
        .setContentText(getString(R.string.sync_notification_text, pendingOperations))
        .setSmallIcon(R.drawable.ic_sync)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, Class.forName("com.tfg.umeegunero.MainActivity")),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun actualizarNotificacion(pendingOperations: Int) {
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(pendingOperations)
        )
    }
    
    private fun actualizarNotificacionError() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.sync_notification_error_title))
            .setContentText(getString(R.string.sync_notification_error_text))
            .setSmallIcon(R.drawable.ic_sync)
            .setOngoing(false)
            .build()
            
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun empezarSincronizacion() {
        sincronizacionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val operaciones = syncRepository.obtenerNumeroOperacionesPendientes()
                if (operaciones > 0) {
                    actualizarNotificacion(operaciones)
                    
                    // Realizar la sincronización
                    syncRepository.procesarOperacionesPendientes()
                    
                    // Actualizar la notificación
                    val pendientes = syncRepository.obtenerNumeroOperacionesPendientes()
                    if (pendientes == 0) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    } else {
                        actualizarNotificacion(pendientes)
                    }
                } else {
                    // No hay operaciones pendientes, detener el servicio
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error en la sincronización")
                actualizarNotificacionError()
            }
        }
    }
} 