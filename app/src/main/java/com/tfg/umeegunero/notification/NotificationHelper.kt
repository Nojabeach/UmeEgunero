package com.tfg.umeegunero.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import com.tfg.umeegunero.data.service.NotificationService
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Clase de utilidad para gestionar notificaciones en toda la aplicación
 */
@Singleton
class NotificationHelper @Inject constructor(
    private val context: Context,
    private val preferenciasRepository: PreferenciasRepository,
    private val notificationService: NotificationService,
    private val notificationManager: AppNotificationManager
) {
    
    /**
     * Comprueba si los permisos de notificación están concedidos
     */
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones anteriores a Android 13, no se requiere permiso explícito
        }
    }
    
    /**
     * Abre la configuración de notificaciones del sistema para la aplicación
     */
    fun openNotificationSettings() {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error al abrir la configuración de notificaciones")
            
            // Alternativa: abrir la configuración general de la app
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Timber.e(e2, "Error al abrir la configuración de la aplicación")
            }
        }
    }
    
    /**
     * Registra el token FCM para el usuario actual
     */
    suspend fun registerUserToken(userId: String) {
        try {
            val token = notificationManager.registerDeviceToken()
            if (token.isNotEmpty()) {
                notificationService.registrarTokenFCM(userId, token) { success ->
                    if (success) {
                        Timber.d("Token registrado correctamente para el usuario $userId")
                    } else {
                        Timber.e("Error al registrar token para el usuario $userId")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar token FCM")
        }
    }
    
    /**
     * Actualiza las preferencias de notificación del usuario
     */
    fun updateNotificationPreferences(
        userId: String, 
        enable: Boolean,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            notificationService.configurarPreferenciasNotificacion(
                userId = userId,
                habilitar = enable,
                onCompletion = onComplete
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar preferencias de notificación")
            onComplete(false, "Error: ${e.message}")
        }
    }
    
    /**
     * Cancela todas las notificaciones activas
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAllNotifications()
    }
    
    /**
     * Envía una notificación de incidencia (disponible solo para profesores)
     */
    fun enviarNotificacionIncidencia(
        alumnoId: String,
        profesorId: String,
        titulo: String,
        mensaje: String,
        urgente: Boolean = false,
        onComplete: (Boolean, String) -> Unit
    ) {
        notificationService.enviarNotificacionIncidencia(
            alumnoId = alumnoId,
            profesorId = profesorId,
            titulo = titulo,
            mensaje = mensaje,
            urgente = urgente,
            onCompletion = onComplete
        )
    }
    
    /**
     * Envía una notificación de asistencia (disponible solo para profesores)
     */
    fun enviarNotificacionAsistencia(
        alumnoId: String,
        tipoEvento: String, // AUSENCIA, RETRASO, RECOGIDA_TEMPRANA
        titulo: String,
        mensaje: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        notificationService.enviarNotificacionAsistencia(
            alumnoId = alumnoId,
            tipoEvento = tipoEvento,
            titulo = titulo,
            mensaje = mensaje,
            onCompletion = onComplete
        )
    }
    
    /**
     * Envía una notificación de chat
     */
    fun enviarNotificacionChat(
        receptorId: String,
        conversacionId: String,
        titulo: String,
        mensaje: String,
        remitente: String,
        remitenteId: String,
        alumnoId: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        notificationService.enviarNotificacionChat(
            receptorId = receptorId,
            conversacionId = conversacionId,
            titulo = titulo, 
            mensaje = mensaje,
            remitente = remitente,
            remitenteId = remitenteId,
            alumnoId = alumnoId,
            onCompletion = onComplete
        )
    }
    
    /**
     * Envía una notificación de registro diario
     */
    fun enviarNotificacionRegistroDiario(
        alumnoId: String,
        profesorId: String,
        titulo: String = "Actualización de registro diario",
        mensaje: String = "",
        onComplete: (Boolean, String) -> Unit
    ) {
        notificationService.enviarNotificacionRegistroDiario(
            alumnoId = alumnoId,
            profesorId = profesorId,
            titulo = titulo,
            mensaje = mensaje,
            onCompletion = onComplete
        )
    }
    
    /**
     * Procesa cambios importantes en el registro diario para enviar notificaciones automáticas
     */
    fun procesarActualizacionRegistroDiario(
        registroId: String,
        alumnoId: String,
        profesorId: String,
        cambiosImportantes: Boolean
    ) {
        notificationService.procesarActualizacionRegistroDiario(
            registroId = registroId,
            alumnoId = alumnoId,
            profesorId = profesorId,
            cambiosImportantes = cambiosImportantes
        )
    }
} 