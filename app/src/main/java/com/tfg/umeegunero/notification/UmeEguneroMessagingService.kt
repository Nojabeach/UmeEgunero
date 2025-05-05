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
import com.tfg.umeegunero.feature.common.comunicacion.model.UnifiedMessageRepository
import com.tfg.umeegunero.feature.common.comunicacion.model.MessageType

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
    private suspend fun guardarTokenEnFirestore(token: String) {
        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val userId = currentUser.uid
                
                // Generar un ID único para este token (basado en timestamp + uuid corto)
                val tokenId = "token_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().substring(0, 8)}"
                
                // Guardar el token en el documento del usuario
                db.collection("usuarios")
                    .document(userId)
                    .update("fcmTokens.$tokenId", token)
                    .addOnSuccessListener {
                        Timber.d("Token FCM guardado en Firestore para usuario $userId")
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error al guardar token FCM en Firestore para usuario $userId")
                    }
            } else {
                Timber.d("No hay usuario autenticado. El token se guardará cuando el usuario inicie sesión.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar token FCM en Firestore")
        }
    }
    
    /**
     * Llamado cuando se recibe un mensaje de FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Mensaje FCM recibido de: ${remoteMessage.from}")
        
        try {
            remoteMessage.notification?.let { notification ->
                val title = notification.title ?: ""
                val body = notification.body ?: ""
                
                // Usar el canal adecuado según el tipo de notificación
                val channelId = when (remoteMessage.data["tipo"]) {
                    "solicitud_vinculacion" -> AppNotificationManager.CHANNEL_ID_SOLICITUDES
                    "chat" -> AppNotificationManager.CHANNEL_ID_GENERAL
                    "registro_diario" -> AppNotificationManager.CHANNEL_ID_TAREAS
                    "incidencia" -> if (remoteMessage.data["urgente"] == "true") {
                        AppNotificationManager.CHANNEL_ID_INCIDENCIAS
                    } else {
                        AppNotificationManager.CHANNEL_ID_GENERAL
                    }
                    "asistencia" -> AppNotificationManager.CHANNEL_ID_ASISTENCIA
                    "unified_message" -> AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
                    else -> AppNotificationManager.CHANNEL_ID_GENERAL
                }
                
                // Generar un ID único para la notificación
                val notificationId = when (remoteMessage.data["tipo"]) {
                    "solicitud_vinculacion" -> remoteMessage.data["solicitudId"]?.hashCode()
                    "chat" -> remoteMessage.data["conversacionId"]?.hashCode()
                    "registro_diario" -> remoteMessage.data["alumnoId"]?.hashCode()
                    "incidencia" -> ("incidencia_${remoteMessage.data["alumnoId"]}_${System.currentTimeMillis()}").hashCode()
                    "asistencia" -> ("asistencia_${remoteMessage.data["alumnoId"]}_${remoteMessage.data["fecha"]}").hashCode()
                    else -> null
                } ?: System.currentTimeMillis().toInt()
                
                // Mostrar la notificación
                mostrarNotificacion(title, body, channelId, notificationId, remoteMessage.data)
            }
            
            // Procesar datos de la notificación
            if (remoteMessage.data.isNotEmpty()) {
                // Procesar según el tipo de notificación
                when (remoteMessage.data["tipo"]) {
                    "chat" -> procesarNotificacionChat(remoteMessage.data)
                    "registro_diario" -> procesarNotificacionRegistroDiario(remoteMessage.data)
                    "solicitud_vinculacion" -> procesarNotificacionSolicitud(remoteMessage.data)
                    "incidencia" -> procesarNotificacionIncidencia(remoteMessage.data)
                    "asistencia" -> procesarNotificacionAsistencia(remoteMessage.data)
                    "unified_message" -> procesarNotificacionUnificada(remoteMessage.data)
                }
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
        val notificationId = data["conversacionId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVO_MENSAJE_CHAT).apply {
            putExtra("conversacionId", data["conversacionId"])
            putExtra("remitente", data["remitente"])
            putExtra("remitenteId", data["remitenteId"])
            putExtra("alumnoId", data["alumnoId"])
        })
    }
    
    /**
     * Procesa notificaciones relacionadas con el registro diario
     */
    private fun procesarNotificacionRegistroDiario(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Actualización de registro diario"
        val mensaje = data["mensaje"] ?: "Se ha actualizado el registro diario"
        val channelId = AppNotificationManager.CHANNEL_ID_TAREAS
        val notificationId = data["alumnoId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_ACTUALIZACION_REGISTRO).apply {
            putExtra("alumnoId", data["alumnoId"])
            putExtra("fecha", data["fecha"])
        })
    }
    
    /**
     * Procesa notificaciones relacionadas con solicitudes de vinculación
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
        
        serviceScope.launch(Dispatchers.IO) {
            // Cargar el mensaje desde el repositorio
            val messageResult = unifiedMessageRepository.getMessageById(messageId)
            
            val titulo = data["titulo"] ?: "Nuevo mensaje"
            val mensaje = data["mensaje"] ?: "Has recibido un nuevo mensaje"
            val notificationId = messageId.hashCode()
            
            // Determinar canal según tipo de mensaje
            val channelId = when (data["messageType"]) {
                MessageType.NOTIFICATION.name -> AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
                MessageType.ANNOUNCEMENT.name -> AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
                MessageType.CHAT.name -> AppNotificationManager.CHANNEL_ID_GENERAL
                MessageType.INCIDENT.name -> AppNotificationManager.CHANNEL_ID_INCIDENCIAS
                MessageType.ATTENDANCE.name -> AppNotificationManager.CHANNEL_ID_ASISTENCIA
                MessageType.DAILY_RECORD.name -> AppNotificationManager.CHANNEL_ID_TAREAS
                MessageType.SYSTEM.name -> AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
                else -> AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION
            }
            
            mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
            
            // Enviar broadcast para actualizar la UI si la app está abierta
            sendBroadcast(Intent(ACTION_NUEVO_MENSAJE_UNIFICADO).apply {
                putExtra("messageId", messageId)
                putExtra("messageType", data["messageType"])
            })
        }
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
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        
        // Modificar el intent según el tipo de notificación para realizar deeplinks
        modificarIntentSegunCanal(intent, channelId, data)
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construir la notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        // Añadir opciones específicas según el tipo de canal
        when (channelId) {
            AppNotificationManager.CHANNEL_ID_INCIDENCIAS -> {
                // Para incidencias, hacer que la notificación sea persistente
                notification.setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            }
            
            AppNotificationManager.CHANNEL_ID_SOLICITUDES, 
            AppNotificationManager.CHANNEL_ID_ASISTENCIA -> {
                // Para solicitudes y asistencia, añadir vibración pero no persistente
                notification.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setVibrate(longArrayOf(0, 250, 250, 250))
            }
        }
            
        // Mostrar la notificación
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification.build())
        }
        
        Timber.d("Notificación mostrada: $titulo")
    }
    
    /**
     * Modificar el intent según el tipo de notificación para realizar deeplinks
     */
    private fun modificarIntentSegunCanal(intent: Intent?, channelId: String, data: Map<String, String>? = null) {
        intent?.apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            
            when (channelId) {
                AppNotificationManager.CHANNEL_ID_SOLICITUDES -> {
                    // Intent para abrir la pantalla de solicitudes de vinculación
                    putExtra("openSection", "solicitudes")
                }
                
                AppNotificationManager.CHANNEL_ID_GENERAL -> {
                    // Para notificaciones generales (incluidos mensajes)
                    // No hacemos cambios adicionales
                }
                
                AppNotificationManager.CHANNEL_ID_TAREAS -> {
                    // Intent para abrir la sección de registro diario/tareas
                    putExtra("openSection", "registrodiario")
                }
                
                AppNotificationManager.CHANNEL_ID_INCIDENCIAS -> {
                    // Intent para abrir la sección de incidencias (prioridad alta)
                    putExtra("openSection", "incidencias")
                    putExtra("urgent", true)
                }
                
                AppNotificationManager.CHANNEL_ID_ASISTENCIA -> {
                    // Intent para abrir la sección de asistencia
                    putExtra("openSection", "asistencia")
                }
                
                AppNotificationManager.CHANNEL_ID_UNIFIED_COMMUNICATION -> {
                    // Intent para abrir la bandeja de entrada unificada
                    putExtra("openSection", "unified_inbox")
                    data?.get("messageId")?.let { messageId ->
                        putExtra("messageId", messageId)
                    }
                }
            }
        }
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