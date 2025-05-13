package com.tfg.umeegunero.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
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
                description = "Notificaciones relacionadas con tareas, registro diario y entregas"
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
                description = "Notificaciones generales y mensajes de chat"
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
            
            // Canal para notificaciones de solicitudes de vinculación
            val solicitudesChannel = NotificationChannel(
                CHANNEL_ID_SOLICITUDES,
                CHANNEL_NAME_SOLICITUDES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de solicitudes de vinculación familiar-alumno"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Canal para notificaciones de incidencias
            val incidenciasChannel = NotificationChannel(
                CHANNEL_ID_INCIDENCIAS,
                CHANNEL_NAME_INCIDENCIAS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de incidencias importantes que requieren atención inmediata"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Canal para notificaciones de asistencia
            val asistenciaChannel = NotificationChannel(
                CHANNEL_ID_ASISTENCIA,
                CHANNEL_NAME_ASISTENCIA,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre asistencia, retrasos y ausencias"
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Añadir nuevo canal para el sistema de comunicación unificado
            val unifiedCommunicationChannel = NotificationChannel(
                CHANNEL_ID_UNIFIED_COMMUNICATION,
                CHANNEL_NAME_UNIFIED_COMMUNICATION,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones del sistema de comunicación unificado"
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
            
            // Canal para mensajes de chat (más específico que el general)
            val chatChannel = NotificationChannel(
                CHANNEL_ID_CHAT,
                CHANNEL_NAME_CHAT,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes de chat privados"
                enableLights(true)
                lightColor = Color.MAGENTA
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Canal para anuncios y comunicados
            val announcementsChannel = NotificationChannel(
                CHANNEL_ID_ANNOUNCEMENTS,
                CHANNEL_NAME_ANNOUNCEMENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Comunicados oficiales y anuncios del centro educativo"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            // Crear todos los canales
            notificationManager.createNotificationChannel(tareasChannel)
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(syncChannel)
            notificationManager.createNotificationChannel(solicitudesChannel)
            notificationManager.createNotificationChannel(incidenciasChannel)
            notificationManager.createNotificationChannel(asistenciaChannel)
            notificationManager.createNotificationChannel(unifiedCommunicationChannel)
            notificationManager.createNotificationChannel(chatChannel)
            notificationManager.createNotificationChannel(announcementsChannel)
            
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
     * @param intent Intent personalizado para cuando se pulsa la notificación
     */
    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt(),
        intent: Intent? = null
    ) {
        try {
            // Crear intent para cuando se pulsa la notificación
            val pendingIntent = if (intent != null) {
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                // Intent predeterminado para abrir la app
                val defaultIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                PendingIntent.getActivity(
                    context,
                    0,
                    defaultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            
            // Elegir la prioridad según el canal
            val priority = when(channelId) {
                CHANNEL_ID_INCIDENCIAS -> NotificationCompat.PRIORITY_MAX
                CHANNEL_ID_SOLICITUDES, CHANNEL_ID_TAREAS, CHANNEL_ID_ASISTENCIA, CHANNEL_ID_UNIFIED_COMMUNICATION -> NotificationCompat.PRIORITY_HIGH
                CHANNEL_ID_GENERAL -> NotificationCompat.PRIORITY_DEFAULT
                CHANNEL_ID_SYNC -> NotificationCompat.PRIORITY_LOW
                else -> NotificationCompat.PRIORITY_DEFAULT
            }
            
            // Construir la notificación
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
                
            // Mostrar la notificación
            with(NotificationManagerCompat.from(context)) {
                // Verificar que tenemos permiso para mostrar notificaciones
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED) {
                    notify(notificationId, notification)
                } else {
                    Timber.w("Permiso de notificaciones no concedido")
                }
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
        const val CHANNEL_NAME_TAREAS = "Tareas y Registro Diario"
        const val CHANNEL_ID_GENERAL = "channel_general"
        const val CHANNEL_NAME_GENERAL = "Notificaciones Generales"
        const val CHANNEL_ID_SYNC = "sync_service_channel"
        const val CHANNEL_NAME_SYNC = "Sincronización"
        const val CHANNEL_ID_SOLICITUDES = "channel_solicitudes_vinculacion"
        const val CHANNEL_NAME_SOLICITUDES = "Solicitudes de Vinculación"
        const val CHANNEL_ID_INCIDENCIAS = "channel_incidencias"
        const val CHANNEL_NAME_INCIDENCIAS = "Incidencias Importantes"
        const val CHANNEL_ID_ASISTENCIA = "channel_asistencia"
        const val CHANNEL_NAME_ASISTENCIA = "Asistencia y Ausencias"
        
        // Constantes para el canal de comunicación unificada
        const val CHANNEL_ID_UNIFIED_COMMUNICATION = "unified_communication"
        const val CHANNEL_NAME_UNIFIED_COMMUNICATION = "Sistema de Comunicación Unificado"
        
        // Canales de notificación
        const val CHANNEL_ID_CHAT = "channel_chat"
        const val CHANNEL_NAME_CHAT = "Mensajes de chat"
        
        const val CHANNEL_ID_ANNOUNCEMENTS = "channel_announcements"
        const val CHANNEL_NAME_ANNOUNCEMENTS = "Comunicados"
    }
} 