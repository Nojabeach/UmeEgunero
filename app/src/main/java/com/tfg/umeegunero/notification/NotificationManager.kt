package com.tfg.umeegunero.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Tarea
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de notificaciones para la aplicación
 * 
 * Se encarga de:
 * - Registrar tokens FCM
 * - Programar notificaciones para tareas
 * - Mostrar notificaciones
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging
) {
    companion object {
        const val CHANNEL_ID_TAREAS = "channel_tareas"
        const val CHANNEL_NAME_TAREAS = "Tareas"
        const val CHANNEL_ID_GENERAL = "channel_general"
        const val CHANNEL_NAME_GENERAL = "Notificaciones Generales"
        
        private const val NOTIFICATION_ID_TAREA_BASE = 1000
    }
    
    /**
     * Inicializa los canales de notificación
     */
    fun initNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as NotificationManager
            
            // Canal para tareas
            val channelTareas = NotificationChannel(
                CHANNEL_ID_TAREAS,
                CHANNEL_NAME_TAREAS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones relacionadas con tareas"
                enableVibration(true)
            }
            
            // Canal para notificaciones generales
            val channelGeneral = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
            }
            
            notificationManager.createNotificationChannels(listOf(channelTareas, channelGeneral))
        }
    }
    
    /**
     * Registra el token del dispositivo en FCM
     * @return token de FCM para este dispositivo
     */
    suspend fun registerDeviceToken(): String {
        return try {
            val token = firebaseMessaging.token.await()
            Timber.d("FCM Token: $token")
            // Aquí se podría guardar el token en Firestore asociado al usuario
            token
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener token FCM")
            ""
        }
    }
    
    /**
     * Programa una notificación para una tarea específica
     * @param tarea La tarea para la que programar recordatorios
     */
    fun scheduleTaskNotification(tarea: Tarea) {
        try {
            // Si la tarea tiene fecha de entrega, programamos recordatorio
            val fechaEntrega = tarea.fechaEntrega?.toDate()
            if (fechaEntrega != null) {
                val now = Date()
                
                // Si la fecha de entrega es futura
                if (fechaEntrega.after(now)) {
                    // Calcular tiempo hasta 24h antes de la entrega
                    val tiempoEntrega = fechaEntrega.time - now.time
                    val tiempoRecordatorio = fechaEntrega.time - TimeUnit.HOURS.toMillis(24)
                    
                    // Si aún estamos a más de 24h, programar recordatorio para 24h antes
                    if (tiempoEntrega > TimeUnit.HOURS.toMillis(24)) {
                        val title = "Recordatorio de tarea"
                        val message = "La tarea '${tarea.titulo}' vence mañana"
                        
                        // En una implementación real, aquí usaríamos WorkManager o AlarmManager
                        // Para este ejemplo, mostramos directamente la notificación
                        showNotification(
                            title,
                            message,
                            tarea.id.hashCode() + NOTIFICATION_ID_TAREA_BASE,
                            CHANNEL_ID_TAREAS
                        )
                        
                        Timber.d("Notificación programada para la tarea ${tarea.titulo}")
                    } else {
                        // Si estamos a menos de 24h, mostrar recordatorio ahora
                        val title = "Tarea próxima a vencer"
                        val message = "La tarea '${tarea.titulo}' vence pronto"
                        
                        showNotification(
                            title,
                            message,
                            tarea.id.hashCode() + NOTIFICATION_ID_TAREA_BASE,
                            CHANNEL_ID_TAREAS
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al programar notificación")
        }
    }
    
    /**
     * Cancela una notificación programada
     * @param tareaId ID de la tarea cuya notificación cancelar
     */
    fun cancelNotification(tareaId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
        notificationManager.cancel(tareaId.hashCode() + NOTIFICATION_ID_TAREA_BASE)
    }
    
    /**
     * Muestra una notificación inmediatamente
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param notificationId ID único para la notificación
     * @param channelId ID del canal de notificación
     */
    fun showNotification(
        title: String,
        message: String,
        notificationId: Int,
        channelId: String = CHANNEL_ID_GENERAL
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
        
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    /**
     * Notifica sobre una nueva tarea asignada
     * @param tarea Tarea recién asignada
     */
    fun notifyNewTask(tarea: Tarea) {
        val title = "Nueva tarea asignada"
        val message = "Se te ha asignado la tarea '${tarea.titulo}'"
        
        showNotification(
            title,
            message,
            tarea.id.hashCode() + NOTIFICATION_ID_TAREA_BASE,
            CHANNEL_ID_TAREAS
        )
    }
    
    /**
     * Notifica sobre una tarea calificada
     * @param tarea Tarea calificada
     */
    fun notifyTaskGraded(tarea: Tarea) {
        val title = "Tarea calificada"
        val message = "Tu tarea '${tarea.titulo}' ha sido calificada"
        
        showNotification(
            title,
            message,
            tarea.id.hashCode() + NOTIFICATION_ID_TAREA_BASE,
            CHANNEL_ID_TAREAS
        )
    }
} 