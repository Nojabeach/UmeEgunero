package com.tfg.umeegunero.notification

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * Servicio para procesar mensajes de Firebase Cloud Messaging (FCM).
 *
 * Este servicio maneja:
 * - Recepción de mensajes y notificaciones de FCM
 * - Actualización del token de FCM
 * - Procesamiento de diferentes tipos de notificaciones
 */
@AndroidEntryPoint
class UmeEguneroMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var preferenciasRepository: PreferenciasRepository
    
    @Inject
    lateinit var notificationManager: AppNotificationManager
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    /**
     * Llamado cuando se recibe un nuevo token de FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Nuevo token FCM recibido")
        
        // Guardar el token en DataStore
        serviceScope.launch {
            preferenciasRepository.guardarFcmToken(token)
            Timber.d("Token FCM guardado en preferencias")
        }
    }
    
    /**
     * Llamado cuando se recibe un mensaje de FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Mensaje FCM recibido de: ${remoteMessage.from}")
        
        try {
            // Procesar datos del mensaje
            remoteMessage.data.let { data ->
                Timber.d("Datos del mensaje: $data")
                
                // Determinar tipo de notificación
                when (data["tipo"]) {
                    "tarea" -> procesarNotificacionTarea(data)
                    "evento" -> procesarNotificacionEvento(data)
                    "chat" -> procesarNotificacionChat(data)
                    "solicitud_vinculacion" -> procesarNotificacionSolicitudVinculacion(data)
                    else -> procesarNotificacionGeneral(data)
                }
            }
            
            // Procesar notificación si está presente
            remoteMessage.notification?.let { notification ->
                Timber.d("Notificación: ${notification.title} - ${notification.body}")
                mostrarNotificacion(
                    notification.title ?: "Notificación",
                    notification.body ?: "Tienes una nueva notificación"
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al procesar el mensaje FCM")
        }
    }
    
    /**
     * Procesa notificaciones relacionadas con el chat
     */
    private fun procesarNotificacionChat(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Nuevo mensaje"
        val mensaje = data["mensaje"] ?: "Has recibido un nuevo mensaje"
        val channelId = AppNotificationManager.CHANNEL_ID_GENERAL
        val notificationId = data["chatId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVO_MENSAJE_CHAT).apply {
            putExtra("chatId", data["chatId"])
            putExtra("remitente", data["remitente"])
        })
    }
    
    /**
     * Procesa notificaciones relacionadas con tareas
     */
    private fun procesarNotificacionTarea(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Nueva tarea"
        val mensaje = data["mensaje"] ?: "Se te ha asignado una nueva tarea"
        val channelId = AppNotificationManager.CHANNEL_ID_TAREAS
        val notificationId = data["tareaId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId)
    }
    
    /**
     * Procesa notificaciones relacionadas con eventos
     */
    private fun procesarNotificacionEvento(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Nuevo evento"
        val mensaje = data["mensaje"] ?: "Se ha programado un nuevo evento"
        val channelId = AppNotificationManager.CHANNEL_ID_GENERAL
        val notificationId = data["eventoId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId)
    }
    
    /**
     * Procesa notificaciones de solicitudes de vinculación familiar-alumno
     */
    private fun procesarNotificacionSolicitudVinculacion(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Nueva solicitud de vinculación"
        val mensaje = data["mensaje"] ?: "Has recibido una nueva solicitud de vinculación familiar-alumno"
        val channelId = AppNotificationManager.CHANNEL_ID_SOLICITUDES
        val notificationId = data["solicitudId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVA_SOLICITUD_VINCULACION).apply {
            putExtra("solicitudId", data["solicitudId"])
            putExtra("familiarId", data["familiarId"])
            putExtra("alumnoDni", data["alumnoDni"])
            putExtra("centroId", data["centroId"])
        })
    }
    
    /**
     * Procesa notificaciones generales
     */
    private fun procesarNotificacionGeneral(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Notificación"
        val mensaje = data["mensaje"] ?: "Tienes una nueva notificación"
        val channelId = AppNotificationManager.CHANNEL_ID_GENERAL
        val notificationId = System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId)
    }
    
    /**
     * Muestra una notificación
     */
    private fun mostrarNotificacion(
        titulo: String,
        mensaje: String,
        channelId: String = AppNotificationManager.CHANNEL_ID_GENERAL,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        notificationManager.showNotification(titulo, mensaje, channelId, notificationId)
    }
    
    /**
     * Limpia recursos al destruir el servicio
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
    
    companion object {
        const val ACTION_NUEVO_MENSAJE_CHAT = "com.tfg.umeegunero.NUEVO_MENSAJE_CHAT"
        const val ACTION_NUEVA_SOLICITUD_VINCULACION = "com.tfg.umeegunero.NUEVA_SOLICITUD_VINCULACION"
    }
} 