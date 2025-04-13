package com.tfg.umeegunero.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.tfg.umeegunero.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de notificaciones de la aplicación.
 *
 * Esta clase se encarga de:
 * - Crear canales de notificación para Android 8.0+
 * - Mostrar notificaciones
 */
@Singleton
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Inicializa los canales de notificación
     */
    fun initNotificationChannels() {
        createNotificationChannels()
    }

    /**
     * Crea los canales de notificación para Android 8.0+
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager

            // Canal para notificaciones de tareas
            val tareasChannel = NotificationChannel(
                CHANNEL_ID_TAREAS,
                CHANNEL_NAME_TAREAS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones relacionadas con tareas y entregas"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Canal para notificaciones generales
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            
            // Canal para notificaciones de sincronización
            val syncChannel = NotificationChannel(
                CHANNEL_ID_SYNC,
                CHANNEL_NAME_SYNC,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.sync_notification_channel_description)
                enableLights(false)
                enableVibration(false)
            }
            
            // Crear los canales
            notificationManager.createNotificationChannel(tareasChannel)
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(syncChannel)
            
            Timber.d("Canales de notificación creados")
        }
    }
    
    /**
     * Registra el dispositivo para recibir notificaciones FCM
     * @return Token FCM del dispositivo
     */
    suspend fun registerDeviceToken(): String {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Timber.d("Token FCM obtenido: $token")
            token
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener token FCM")
            ""
        }
    }
    
    /**
     * Muestra una notificación inmediata
     * 
     * @param title Título de la notificación
     * @param message Mensaje/contenido de la notificación
     * @param channelId ID del canal de notificación
     * @param notificationId ID único para la notificación
     */
    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        try {
            // Crear intent para abrir la app al pulsar la notificación
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Construir la notificación
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
                
            // Mostrar la notificación
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, notification)
            }
            
            Timber.d("Notificación mostrada: $title")
        } catch (e: Exception) {
            Timber.e(e, "Error al mostrar notificación")
        }
    }
    
    /**
     * Cancela todas las notificaciones activas
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
        Timber.d("Todas las notificaciones canceladas")
    }
    
    companion object {
        // Constantes para canales de notificación
        const val CHANNEL_ID_TAREAS = "channel_tareas"
        const val CHANNEL_NAME_TAREAS = "Tareas y Deberes"
        const val CHANNEL_ID_GENERAL = "channel_general"
        const val CHANNEL_NAME_GENERAL = "Notificaciones Generales"
        const val CHANNEL_ID_SYNC = "sync_service_channel"
        const val CHANNEL_NAME_SYNC = "Sincronización"
    }
} 