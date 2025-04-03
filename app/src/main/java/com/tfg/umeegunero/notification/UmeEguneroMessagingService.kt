package com.tfg.umeegunero.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Servicio para recibir y procesar mensajes de Firebase Cloud Messaging
 */
@AndroidEntryPoint
class UmeEguneroMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    @Inject
    lateinit var preferenciasRepository: PreferenciasRepository
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Se llama cuando se recibe un nuevo token de FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("Nuevo token FCM recibido: $token")
        
        // Guardar el token en las preferencias
        coroutineScope.launch {
            try {
                preferenciasRepository.guardarFcmToken(token)
                
                // Aquí podrías enviar el token al servidor backend
                // para asociarlo con el usuario actual
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar token FCM")
            }
        }
    }
    
    /**
     * Se llama cuando se recibe un mensaje de FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("Mensaje recibido de: ${remoteMessage.from}")
        
        // Verificar si el mensaje contiene datos
        remoteMessage.data.isNotEmpty().let {
            Timber.d("Datos del mensaje: ${remoteMessage.data}")
            
            // Procesar los datos según el tipo de notificación
            handleMessage(remoteMessage)
        }
        
        // Verificar si el mensaje contiene notificación
        remoteMessage.notification?.let {
            Timber.d("Mensaje de notificación: ${it.body}")
            
            // Mostrar la notificación directamente
            notificationManager.showNotification(
                it.title ?: "UmeEgunero",
                it.body ?: "Tienes una nueva notificación",
                remoteMessage.messageId.hashCode()
            )
        }
    }
    
    /**
     * Procesa el mensaje según su tipo
     */
    private fun handleMessage(remoteMessage: RemoteMessage) {
        try {
            val data = remoteMessage.data
            val type = data["type"] ?: "general"
            val title = data["title"] ?: "UmeEgunero"
            val message = data["message"] ?: "Tienes una nueva notificación"
            
            when (type) {
                "tarea_nueva" -> {
                    val tareaId = data["tareaId"] ?: ""
                    if (tareaId.isNotEmpty()) {
                        // Aquí podrías cargar la tarea desde el repositorio
                        // y mostrar una notificación más específica
                    }
                }
                "tarea_calificada" -> {
                    val tareaId = data["tareaId"] ?: ""
                    val calificacion = data["calificacion"] ?: ""
                    val notificationMessage = if (calificacion.isNotEmpty()) {
                        "$message (Calificación: $calificacion)"
                    } else {
                        message
                    }
                    
                    notificationManager.showNotification(
                        title,
                        notificationMessage,
                        "tarea_calificada_${tareaId}".hashCode(),
                        NotificationManager.CHANNEL_ID_TAREAS
                    )
                }
                "recordatorio" -> {
                    notificationManager.showNotification(
                        title,
                        message,
                        "recordatorio_${remoteMessage.messageId}".hashCode(),
                        NotificationManager.CHANNEL_ID_TAREAS
                    )
                }
                else -> {
                    // Notificación general
                    notificationManager.showNotification(
                        title,
                        message,
                        remoteMessage.messageId.hashCode()
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al procesar mensaje FCM")
        }
    }
    
    // Constantes para los tipos de notificaciones
    companion object {
        const val CHANNEL_ID_TAREAS = "channel_tareas"
        const val CHANNEL_ID_GENERAL = "channel_general"
    }
} 