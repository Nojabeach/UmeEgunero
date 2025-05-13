package com.tfg.umeegunero.notification

import android.content.Intent
import android.app.PendingIntent
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.MainActivity
import com.tfg.umeegunero.navigation.AppScreens

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
    
    @Inject
    lateinit var unifiedMessageRepository: UnifiedMessageRepository
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    /**
     * Llamado cuando se recibe un nuevo token de FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Nuevo token FCM recibido: $token")
        
        // Guardar el token en DataStore
        serviceScope.launch {
            preferenciasRepository.guardarFcmToken(token)
            Timber.d("Token FCM guardado en preferencias")
            
            // Guardar token en Firestore
            guardarTokenEnFirestore(token)
        }
    }
    
    /**
     * Guarda el token FCM en el documento del usuario actual en Firestore
     */
    private fun guardarTokenEnFirestore(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        
        try {
            val userId = currentUser.uid
            val userDocRef = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
            
            // Añadir el token como un campo en un mapa para permitir múltiples dispositivos
            userDocRef.get().addOnSuccessListener { document ->
                val fcmTokens = document.get("fcmTokens") as? Map<String, String> ?: mapOf()
                val updatedTokens = fcmTokens.toMutableMap().apply {
                    this[token] = token
                }
                
                userDocRef.update("fcmTokens", updatedTokens)
                    .addOnSuccessListener {
                        Timber.d("Token FCM actualizado en Firestore")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error al actualizar token FCM en Firestore")
                        
                        // Intentar crear el documento si no existe
                        userDocRef.set(mapOf("fcmTokens" to mapOf(token to token)))
                            .addOnSuccessListener {
                                Timber.d("Token FCM guardado en documento nuevo")
                            }
                            .addOnFailureListener { innerE ->
                                Timber.e(innerE, "Error al crear documento para token FCM")
                            }
                    }
            }.addOnFailureListener { e ->
                Timber.e(e, "Error al obtener documento del usuario para token FCM")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error general al guardar token FCM: ${e.message}")
        }
    }
    
    /**
     * Procesa mensajes recibidos de FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Mensaje FCM recibido de: ${remoteMessage.from}")
        
        try {
            // Priorizar el campo 'messageType' del payload data para determinar el tipo de mensaje
            val messageType = remoteMessage.data["messageType"] ?: remoteMessage.data["tipo"]
            val messageId = remoteMessage.data["messageId"]
            
            if (messageId != null) {
                // Este es un mensaje unificado, procesarlo como tal
                procesarNotificacionUnificada(remoteMessage.data)
                return
            }
            
            // Procesamiento para formatos de mensajes antiguos
            remoteMessage.notification?.let { notification ->
                val title = notification.title ?: ""
                val body = notification.body ?: ""
                
                // Usar el canal adecuado según el tipo de notificación
                val channelId = when (messageType) {
                    "solicitud_vinculacion" -> AppNotificationManager.CHANNEL_ID_SOLICITUDES
                    "chat" -> AppNotificationManager.CHANNEL_ID_CHAT
                    "registro_diario" -> AppNotificationManager.CHANNEL_ID_TAREAS
                    "incidencia" -> if (remoteMessage.data["urgente"] == "true") {
                        AppNotificationManager.CHANNEL_ID_INCIDENCIAS
                    } else {
                        AppNotificationManager.CHANNEL_ID_GENERAL
                    }
                    "asistencia" -> AppNotificationManager.CHANNEL_ID_ASISTENCIA
                    "unified_message", "ANNOUNCEMENT", "CHAT", "INCIDENT", "ATTENDANCE", "DAILY_RECORD", "NOTIFICATION", "SYSTEM" -> 
                        AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
                    else -> AppNotificationManager.CHANNEL_ID_GENERAL
                }
                
                // Generar un ID único para la notificación
                val notificationId = when {
                    messageId != null -> messageId.hashCode()
                    remoteMessage.data.containsKey("solicitudId") -> remoteMessage.data["solicitudId"]?.hashCode() ?: System.currentTimeMillis().toInt()
                    remoteMessage.data.containsKey("mensajeId") -> remoteMessage.data["mensajeId"]?.hashCode() ?: System.currentTimeMillis().toInt()
                    else -> System.currentTimeMillis().toInt()
                }
                
                // Procesar según el tipo de notificación
                when (messageType) {
                    "solicitud_vinculacion" -> procesarNotificacionSolicitud(remoteMessage.data)
                    "chat" -> procesarNotificacionChat(remoteMessage.data)
                    "incidencia" -> procesarNotificacionIncidencia(remoteMessage.data)
                    "asistencia" -> procesarNotificacionAsistencia(remoteMessage.data)
                    "unified_message", "ANNOUNCEMENT", "CHAT", "INCIDENT", "ATTENDANCE", "DAILY_RECORD", "NOTIFICATION", "SYSTEM" -> 
                        procesarNotificacionUnificada(remoteMessage.data)
                    else -> {
                        // Notificación general
                        mostrarNotificacion(title, body, channelId, notificationId, remoteMessage.data)
                    }
                }
            } ?: run {
                // Sin objeto de notificación, procesar según datos
                if (remoteMessage.data.isNotEmpty()) {
                    val title = remoteMessage.data["title"] ?: remoteMessage.data["titulo"] ?: "Nueva notificación"
                    val body = remoteMessage.data["body"] ?: remoteMessage.data["mensaje"] ?: remoteMessage.data["content"] ?: "Tienes una nueva notificación"
                    
                    val channelId = when (messageType) {
                        "solicitud_vinculacion" -> AppNotificationManager.CHANNEL_ID_SOLICITUDES
                        "chat" -> AppNotificationManager.CHANNEL_ID_CHAT
                        "registro_diario" -> AppNotificationManager.CHANNEL_ID_TAREAS
                        "incidencia" -> if (remoteMessage.data["urgente"] == "true") {
                            AppNotificationManager.CHANNEL_ID_INCIDENCIAS
                        } else {
                            AppNotificationManager.CHANNEL_ID_GENERAL
                        }
                        "asistencia" -> AppNotificationManager.CHANNEL_ID_ASISTENCIA
                        "unified_message", "ANNOUNCEMENT", "CHAT", "INCIDENT", "ATTENDANCE", "DAILY_RECORD", "NOTIFICATION", "SYSTEM" -> 
                            AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
                        else -> AppNotificationManager.CHANNEL_ID_GENERAL
                    }
                    
                    // Generar un ID único para la notificación
                    val notificationId = when {
                        messageId != null -> messageId.hashCode()
                        remoteMessage.data.containsKey("solicitudId") -> remoteMessage.data["solicitudId"]?.hashCode() ?: System.currentTimeMillis().toInt()
                        remoteMessage.data.containsKey("mensajeId") -> remoteMessage.data["mensajeId"]?.hashCode() ?: System.currentTimeMillis().toInt()
                        else -> System.currentTimeMillis().toInt()
                    }
                    
                    // Procesar según el tipo de notificación
                    when (messageType) {
                        "solicitud_vinculacion" -> procesarNotificacionSolicitud(remoteMessage.data)
                        "chat" -> procesarNotificacionChat(remoteMessage.data)
                        "incidencia" -> procesarNotificacionIncidencia(remoteMessage.data)
                        "asistencia" -> procesarNotificacionAsistencia(remoteMessage.data)
                        "unified_message", "ANNOUNCEMENT", "CHAT", "INCIDENT", "ATTENDANCE", "DAILY_RECORD", "NOTIFICATION", "SYSTEM" -> 
                            procesarNotificacionUnificada(remoteMessage.data)
                        else -> {
                            // Notificación general
                            mostrarNotificacion(title, body, channelId, notificationId, remoteMessage.data)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al procesar notificación FCM")
            
            // Intentar mostrar algo aunque haya error
            val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Nueva notificación"
            val body = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "Tienes una nueva notificación"
            mostrarNotificacion(title, body, AppNotificationManager.CHANNEL_ID_GENERAL, System.currentTimeMillis().toInt())
        }
    }
    
    /**
     * Procesa notificaciones de chat
     */
    private fun procesarNotificacionChat(data: Map<String, String>) {
        val titulo = data["titulo"] ?: data["title"] ?: "Nuevo mensaje"
        val mensaje = data["mensaje"] ?: data["body"] ?: data["content"] ?: "Has recibido un nuevo mensaje"
        val conversacionId = data["conversacionId"] ?: ""
        val mensajeId = data["mensajeId"] ?: data["messageId"] ?: ""
        val remitente = data["remitente"] ?: data["senderName"] ?: ""
        
        val channelId = AppNotificationManager.CHANNEL_ID_CHAT
        val notificationId = mensajeId.ifEmpty { conversacionId }.hashCode()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVO_MENSAJE_CHAT).apply {
            putExtra("mensajeId", mensajeId)
            putExtra("conversacionId", conversacionId)
            putExtra("remitente", remitente)
        })
    }
    
    /**
     * Procesa notificaciones de solicitudes de vinculación
     */
    private fun procesarNotificacionSolicitud(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Nueva solicitud"
        val mensaje = data["mensaje"] ?: "Hay una nueva solicitud pendiente"
        val channelId = AppNotificationManager.CHANNEL_ID_SOLICITUDES
        val notificationId = data["solicitudId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        val action = if (data["click_action"] == "SOLICITUD_PENDIENTE") {
            ACTION_NUEVA_SOLICITUD
        } else {
            ACTION_SOLICITUD_PROCESADA
        }
        
        sendBroadcast(Intent(action).apply {
            putExtra("solicitudId", data["solicitudId"])
            putExtra("centroId", data["centroId"])
            putExtra("estado", data["estado"])
        })
    }
    
    /**
     * Procesa notificaciones relacionadas con incidencias
     */
    private fun procesarNotificacionIncidencia(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Incidencia"
        val mensaje = data["mensaje"] ?: "Se ha reportado una incidencia"
        val urgente = data["urgente"] == "true"
        
        val channelId = if (urgente) {
            AppNotificationManager.CHANNEL_ID_INCIDENCIAS
        } else {
            AppNotificationManager.CHANNEL_ID_GENERAL
        }
        
        val notificationId = ("incidencia_${data["alumnoId"]}_${System.currentTimeMillis()}").hashCode()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVA_INCIDENCIA).apply {
            putExtra("alumnoId", data["alumnoId"])
            putExtra("profesorId", data["profesorId"])
            putExtra("urgente", urgente)
        })
    }
    
    /**
     * Procesa notificaciones relacionadas con asistencia
     */
    private fun procesarNotificacionAsistencia(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Asistencia"
        val mensaje = data["mensaje"] ?: "Actualización de asistencia"
        val channelId = AppNotificationManager.CHANNEL_ID_ASISTENCIA
        val notificationId = ("asistencia_${data["alumnoId"]}_${data["fecha"]}").hashCode()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_ASISTENCIA).apply {
            putExtra("alumnoId", data["alumnoId"])
            putExtra("tipoEvento", data["tipoEvento"])
            putExtra("fecha", data["fecha"])
        })
    }
    
    /**
     * Procesa notificaciones del sistema de comunicación unificado
     */
    private fun procesarNotificacionUnificada(data: Map<String, String>) {
        val messageId = data["messageId"] ?: return
        val messageType = data["messageType"] ?: "SYSTEM"
        
        // Obtener los datos básicos de la notificación
        val titulo = data["title"] ?: data["titulo"] ?: "Nuevo mensaje"
        val mensaje = data["body"] ?: data["mensaje"] ?: data["content"] ?: "Has recibido un nuevo mensaje"
        val conversacionId = data["conversationId"] ?: ""
        val notificationId = messageId.hashCode()
        
        // Determinar canal según tipo de mensaje
        val channelId = when (messageType) {
            MessageType.CHAT.name -> AppNotificationManager.CHANNEL_ID_CHAT
            MessageType.ANNOUNCEMENT.name -> AppNotificationManager.CHANNEL_ID_ANNOUNCEMENTS
            MessageType.INCIDENT.name -> AppNotificationManager.CHANNEL_ID_INCIDENCIAS
            MessageType.ATTENDANCE.name -> AppNotificationManager.CHANNEL_ID_ASISTENCIA
            MessageType.DAILY_RECORD.name -> AppNotificationManager.CHANNEL_ID_TAREAS
            MessageType.NOTIFICATION.name, 
            MessageType.SYSTEM.name -> AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
            else -> AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
        }
        
        // Crear el intent para cuando se toca la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("messageId", messageId)
            putExtra("messageType", messageType)
            putExtra("conversationId", conversacionId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Crear la notificación con el PendingIntent específico
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()
        
        NotificationManagerCompat.from(this).notify(notificationId, notification)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVO_MENSAJE_UNIFICADO).apply {
            putExtra("messageId", messageId)
            putExtra("messageType", messageType)
            putExtra("conversationId", conversacionId)
        })
    }
    
    /**
     * Muestra una notificación
     */
    private fun mostrarNotificacion(
        titulo: String,
        mensaje: String,
        channelId: String,
        notificationId: Int,
        data: Map<String, String>? = null
    ) {
        // Crear intent para abrir la app al pulsar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        // Añadir datos relevantes al intent
        data?.forEach { (key, value) ->
            intent.putExtra(key, value)
        }
        
        // Asegurar que tenemos algunos datos clave para la navegación
        if (data?.containsKey("messageId") == true) {
            val messageType = data["messageType"]
            
            // No es necesario agregar esto de nuevo, ya se añadió en el bucle anterior
            // intent.putExtra("messageId", data["messageId"])
            // intent.putExtra("messageType", messageType)
            
            // Si es un mensaje de chat, agregar el ID de la conversación
            if (messageType == MessageType.CHAT.name && data.containsKey("conversationId")) {
                // Ya se añadió en el bucle anterior
                // intent.putExtra("conversationId", data["conversationId"])
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            
        // Si es un canal de alta prioridad, agregar luz y vibración
        if (channelId == AppNotificationManager.CHANNEL_ID_INCIDENCIAS) {
            notification.setVibrate(longArrayOf(0, 500, 250, 500))
            notification.setLights(0xFF0000, 3000, 3000) // Rojo
        }
        
        NotificationManagerCompat.from(this).notify(notificationId, notification.build())
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
        const val ACTION_ACTUALIZACION_REGISTRO = "com.tfg.umeegunero.ACTUALIZACION_REGISTRO"
        const val ACTION_NUEVA_SOLICITUD = "com.tfg.umeegunero.NUEVA_SOLICITUD"
        const val ACTION_SOLICITUD_PROCESADA = "com.tfg.umeegunero.SOLICITUD_PROCESADA"
        const val ACTION_NUEVA_INCIDENCIA = "com.tfg.umeegunero.NUEVA_INCIDENCIA"
        const val ACTION_ASISTENCIA = "com.tfg.umeegunero.ASISTENCIA"
        const val ACTION_NUEVO_MENSAJE_UNIFICADO = "com.tfg.umeegunero.NUEVO_MENSAJE_UNIFICADO"
    }
} 