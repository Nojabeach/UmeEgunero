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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import androidx.core.content.ContextCompat

/**
 * Servicio de mensajería de Firebase Cloud Messaging (FCM) para la aplicación UmeEgunero.
 * 
 * Este servicio extiende [FirebaseMessagingService] para manejar la recepción y procesamiento
 * de notificaciones push enviadas desde Firebase. Gestiona diferentes tipos de mensajes
 * según el contexto educativo de la aplicación, incluyendo solicitudes de vinculación,
 * mensajes de chat, registros diarios, incidencias y comunicados.
 * 
 * Funcionalidades principales:
 * - Recepción y procesamiento de mensajes FCM en tiempo real
 * - Actualización automática del token FCM en Firestore
 * - Clasificación de notificaciones por tipo y canal
 * - Integración con el sistema de mensajería unificada
 * - Gestión de preferencias de notificación por usuario
 * - Procesamiento de notificaciones en segundo plano
 * 
 * Tipos de notificaciones soportadas:
 * - **solicitud_vinculacion**: Solicitudes de vinculación familiar
 * - **chat**: Mensajes de comunicación directa
 * - **registro_diario**: Registros de actividades diarias
 * - **incidencia**: Reportes de incidencias (normales y urgentes)
 * - **asistencia**: Notificaciones de asistencia
 * - **unified_message**: Mensajes del sistema unificado
 * - **ANNOUNCEMENT**: Comunicados y circulares
 * - **SYSTEM**: Mensajes generados por el sistema
 * 
 * El servicio utiliza:
 * - [AppNotificationManager] para mostrar notificaciones locales
 * - [PreferenciasRepository] para gestionar tokens FCM
 * - [UnifiedMessageRepository] para mensajes unificados
 * - Corrutinas para operaciones asíncronas
 * 
 * @property preferenciasRepository Repositorio para gestionar preferencias de usuario
 * @property notificationManager Gestor de notificaciones de la aplicación
 * @property unifiedMessageRepository Repositorio para mensajes unificados
 * @property serviceScope Scope de corrutinas para operaciones del servicio
 * 
 * @see FirebaseMessagingService
 * @see AppNotificationManager
 * @see UnifiedMessageRepository
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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
        Timber.d("⚠️ [FCM] Nuevo token FCM recibido: $token")
        
        // Guardar el token en DataStore
        serviceScope.launch {
            try {
                preferenciasRepository.guardarFcmToken(token)
                Timber.d("⚠️ [FCM] Token FCM guardado en preferencias locales")
                
                // Guardar token en Firestore
                guardarTokenEnFirestore(token)
                
                // Log adicional para depurar
                Timber.d("⚠️ [FCM] Token FCM guardado y proceso de registro iniciado")
            } catch (e: Exception) {
                Timber.e(e, "⚠️ [FCM] ERROR crítico al guardar token FCM: ${e.message}")
            }
        }
    }
    
    /**
     * Guarda el token FCM en el documento del usuario actual en Firestore
     */
    private fun guardarTokenEnFirestore(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser == null) {
            Timber.e("⚠️ [FCM] ERROR: No hay usuario autenticado para guardar token FCM")
            return
        }
        
        val userUid = currentUser.uid
        Timber.d("⚠️ [FCM] Iniciando guardado de token para usuario Firebase UID: $userUid")
        
        try {
            // Primero obtenemos el DNI del usuario actual desde Firestore
            val userEmail = currentUser.email
            
            if (userEmail.isNullOrEmpty()) {
                Timber.e("⚠️ [FCM] ERROR: Usuario sin email para guardar token FCM")
                return
            }
            
            Timber.d("⚠️ [FCM] Buscando usuario con email: $userEmail para guardar token FCM")
            
            // Consultar el documento del usuario por email para obtener su DNI
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Timber.e("⚠️ [FCM] ERROR: No se encontró usuario con email: $userEmail")
                        return@addOnSuccessListener
                    }
                    
                    val userDoc = querySnapshot.documents.first()
                    val dni = userDoc.getString("dni")
                    
                    if (dni.isNullOrEmpty()) {
                        Timber.e("⚠️ [FCM] ERROR: El usuario no tiene DNI asignado: $userEmail")
                        return@addOnSuccessListener
                    }
                    
                    Timber.d("⚠️ [FCM] Actualizando token FCM para el usuario con DNI: $dni")
                    
                    // Generar un ID único para el dispositivo
                    val deviceId = "device_${System.currentTimeMillis()}"
                    
                    // Estructura para actualizar el token FCM en las preferencias de notificaciones
                    val tokenUpdate = mapOf(
                        "preferencias.notificaciones.fcmToken" to token,
                        "preferencias.notificaciones.deviceId" to deviceId,
                        "preferencias.notificaciones.lastUpdated" to Timestamp.now()
                    )
                    
                    // Actualizar el documento con el DNI como ID
                    val dniDocRef = FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(dni)
                    
                    dniDocRef.update(tokenUpdate)
                        .addOnSuccessListener {
                            Timber.d("⚠️ [FCM] ✅ Token FCM actualizado correctamente en Firestore para DNI: $dni")
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "⚠️ [FCM] ERROR al actualizar token FCM en documento DNI: $dni")
                            
                            // Si el error es porque no existe el campo preferencias.notificaciones,
                            // intentamos crearlo con una estructura completa
                            val initialData = mapOf(
                                "preferencias" to mapOf(
                                    "notificaciones" to mapOf(
                                        "fcmToken" to token,
                                        "deviceId" to deviceId,
                                        "lastUpdated" to Timestamp.now(),
                                        "push" to true // Habilitamos push por defecto al registrar token
                                    )
                                )
                            )
                            
                            Timber.d("⚠️ [FCM] Intentando crear estructura de preferencias completa para DNI: $dni")
                            
                            dniDocRef.set(initialData, SetOptions.merge())
                                .addOnSuccessListener {
                                    Timber.d("⚠️ [FCM] ✅ Preferencias de notificaciones creadas para usuario DNI: $dni")
                                }
                                .addOnFailureListener { innerE ->
                                    Timber.e(innerE, "⚠️ [FCM] ERROR CRÍTICO al crear preferencias de notificaciones para DNI: $dni")
                                }
                        }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "⚠️ [FCM] ERROR al buscar usuario por email: $userEmail")
                }
        } catch (e: Exception) {
            Timber.e(e, "⚠️ [FCM] ERROR general al guardar token FCM: ${e.message}")
        }
    }
    
    /**
     * Llamado cuando se recibe un mensaje de FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("⚠️ [FCM] Mensaje recibido de FCM: ${remoteMessage.data}")
        
        // Procesar el mensaje recibido
        val data = remoteMessage.data
        
        // IMPORTANTE: Siempre mostrar notificaciones, independientemente del estado de la app
        // La app puede estar:
        // 1. En primer plano (visible al usuario)
        // 2. En segundo plano (minimizada)
        // 3. Cerrada (no en memoria)
        
        // Determinar el tipo de notificación
        val messageType = data["messageType"] ?: data["tipo"] ?: "UNKNOWN"
        
        // Registrar información detallada en logs
        Timber.d("⚠️ [FCM] Tipo de mensaje: $messageType")
        Timber.d("⚠️ [FCM] Datos: $data")
        
        try {
            when (messageType) {
                "CHAT", "chat" -> procesarNotificacionChat(data)
                "SOLICITUD_VINCULACION", "solicitud_vinculacion" -> procesarNotificacionSolicitudVinculacion(data)
                "REGISTRO_DIARIO", "registro_diario" -> procesarNotificacionRegistroDiario(data)
                "INCIDENCIA", "incidencia" -> procesarNotificacionIncidencia(data)
                "ASISTENCIA", "asistencia" -> procesarNotificacionAsistencia(data)
                "ANNOUNCEMENT", "COMUNICADO", "comunicado" -> procesarNotificacionComunicado(data)
                "unified_message", "UNIFIED_MESSAGE" -> procesarNotificacionUnificada(data)
                else -> procesarNotificacionGenerica(data)
            }
            
            // Enviar broadcast general para actualizar cualquier UI que esté escuchando
            sendBroadcast(Intent(ACTION_NUEVA_NOTIFICACION).apply {
                putExtra("messageType", messageType)
                putExtra("data", HashMap(data))
            })
            
            Timber.d("⚠️ [FCM] Notificación procesada correctamente: $messageType")
        } catch (e: Exception) {
            Timber.e(e, "⚠️ [FCM] ERROR al procesar notificación: ${e.message}")
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
    private fun procesarNotificacionSolicitudVinculacion(data: Map<String, String>) {
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
     * Procesa notificaciones del sistema de mensajería unificado
     */
    private fun procesarNotificacionUnificada(data: Map<String, String>) {
        val messageId = data["messageId"] ?: return
        val messageType = data["messageType"] ?: "SYSTEM"
        val senderId = data["senderId"] ?: ""
        val senderName = data["senderName"] ?: ""
        
        Timber.d("⚠️ [FCM] 📬 Procesando notificación unificada: messageId=$messageId, messageType=$messageType, senderId=$senderId, senderName=$senderName")
        
        // Obtener los datos básicos de la notificación
        val titulo = data["title"] ?: data["titulo"] ?: "Nuevo mensaje"
        val mensaje = data["body"] ?: data["mensaje"] ?: data["content"] ?: "Has recibido un nuevo mensaje"
        val conversacionId = data["conversationId"] ?: ""
        val notificationId = messageId.hashCode()
        
        Timber.d("⚠️ [FCM] Detalles del mensaje: título=$titulo, conversacionId=$conversacionId")
        
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
        
        Timber.d("⚠️ [FCM] Canal seleccionado para notificación: $channelId")
        
        // Verificar específicamente si es de admin-centro a profesor
        val esAdminCentroAProfesor = senderId.isNotEmpty() && 
            data["senderRole"] == "ADMIN_CENTRO" && 
            data["receiverRole"] == "PROFESOR"
        
        if (esAdminCentroAProfesor) {
            Timber.d("⚠️ [FCM] Notificación de admin-centro a profesor detectada: $messageId")
        }
        
        // Crear el intent para cuando se toca la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("messageId", messageId)
            putExtra("messageType", messageType)
            putExtra("conversationId", conversacionId)
            putExtra("navigate_to", "unified_inbox")
            
            // Añadir información específica para navegación si es admin-centro a profesor
            if (esAdminCentroAProfesor) {
                putExtra("esAdminCentroAProfesor", true)
                putExtra("senderId", senderId)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Configurar prioridad alta para mensajes de admin-centro a profesor
        val priority = if (esAdminCentroAProfesor) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }
        
        // Crear la notificación con el PendingIntent específico
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            
        // Si es de admin-centro a profesor, añadir elementos adicionales
        if (esAdminCentroAProfesor) {
            notification.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
            
        val finalNotification = notification.build()
        
        // Registrar datos de notificación en logs
        Timber.i("⚠️ [FCM] 🔔 Mostrando notificación: id=$notificationId, canal=$channelId, título=$titulo")
        
        try {
            NotificationManagerCompat.from(this).notify(notificationId, finalNotification)
            Timber.d("⚠️ [FCM] ✅ Notificación mostrada correctamente: $notificationId")
        } catch (e: Exception) {
            Timber.e(e, "⚠️ [FCM] ❌ Error al mostrar notificación: $notificationId, error: ${e.message}")
        }
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVO_MENSAJE_UNIFICADO).apply {
            putExtra("messageId", messageId)
            putExtra("messageType", messageType)
            putExtra("conversationId", conversacionId)
        })
        
        Timber.d("⚠️ [FCM] Broadcast enviado para messageId=$messageId")
    }
    
    /**
     * Procesa notificaciones de comunicados oficiales
     */
    private fun procesarNotificacionComunicado(data: Map<String, String>) {
        val titulo = data["title"] ?: data["titulo"] ?: "Nuevo comunicado"
        val mensaje = data["body"] ?: data["mensaje"] ?: data["content"] ?: "Has recibido un nuevo comunicado oficial"
        val channelId = AppNotificationManager.CHANNEL_ID_ANNOUNCEMENTS
        val notificationId = data["messageId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        Timber.d("⚠️ [FCM] Procesando comunicado: $titulo")
        
        // Crear intent para abrir la app en la pantalla de comunicados
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "comunicados")
            putExtra("messageId", data["messageId"])
            
            // Añadir datos adicionales si están disponibles
            data.forEach { (key, value) ->
                putExtra(key, value)
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
            .build()
            
        showNotification(notificationId, notification)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_NUEVO_COMUNICADO).apply {
            putExtra("messageId", data["messageId"])
        })
    }
    
    /**
     * Procesa notificaciones genéricas que no encajan en otros tipos
     */
    private fun procesarNotificacionGenerica(data: Map<String, String>) {
        val titulo = data["title"] ?: data["titulo"] ?: "Nueva notificación"
        val mensaje = data["body"] ?: data["mensaje"] ?: data["content"] ?: "Tienes una nueva notificación"
        val channelId = AppNotificationManager.CHANNEL_ID_GENERAL
        val notificationId = data["messageId"]?.hashCode() ?: System.currentTimeMillis().toInt()
        
        Timber.d("⚠️ [FCM] Procesando notificación genérica: $titulo")
        
        // Crear intent para abrir la app en la pantalla principal
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            // Añadir datos para posible navegación específica
            data.forEach { (key, value) ->
                putExtra(key, value)
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()
            
        showNotification(notificationId, notification)
    }
    
    /**
     * Procesa notificaciones de registro diario
     */
    private fun procesarNotificacionRegistroDiario(data: Map<String, String>) {
        val titulo = data["titulo"] ?: "Registro diario"
        val mensaje = data["mensaje"] ?: "Hay un nuevo registro diario disponible"
        val channelId = AppNotificationManager.CHANNEL_ID_TAREAS
        val notificationId = ("registro_${data["alumnoId"]}_${data["fecha"]}").hashCode()
        
        mostrarNotificacion(titulo, mensaje, channelId, notificationId, data)
        
        // Enviar broadcast para actualizar la UI si la app está abierta
        sendBroadcast(Intent(ACTION_REGISTRO_DIARIO).apply {
            putExtra("alumnoId", data["alumnoId"])
            putExtra("fecha", data["fecha"])
        })
    }
    
    /**
     * Método auxiliar para mostrar notificaciones con manejo de permisos
     */
    private fun showNotification(notificationId: Int, notification: Notification) {
        try {
            with(NotificationManagerCompat.from(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // En Android 13+ necesitamos verificar permisos en tiempo de ejecución
                    val permissionGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                        this@UmeEguneroMessagingService, 
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    
                    if (permissionGranted) {
                        notify(notificationId, notification)
                        Timber.d("⚠️ [FCM] ✅ Notificación mostrada: $notificationId")
                    } else {
                        Timber.w("⚠️ [FCM] ⚠️ Permiso de notificaciones denegado en Android 13+")
                    }
                } else {
                    // En versiones anteriores no necesitamos verificar permisos
                    notify(notificationId, notification)
                    Timber.d("⚠️ [FCM] ✅ Notificación mostrada: $notificationId")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "⚠️ [FCM] ❌ Error al mostrar notificación: $notificationId")
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
        
        showNotification(notificationId, notification.build())
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
        const val ACTION_NUEVA_NOTIFICACION = "com.tfg.umeegunero.NUEVA_NOTIFICACION"
        const val ACTION_NUEVO_COMUNICADO = "com.tfg.umeegunero.NUEVO_COMUNICADO"
        const val ACTION_REGISTRO_DIARIO = "com.tfg.umeegunero.REGISTRO_DIARIO"
    }
} 