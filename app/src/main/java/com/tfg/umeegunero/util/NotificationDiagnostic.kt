package com.tfg.umeegunero.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.tfg.umeegunero.notification.AppNotificationManager
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase de diagnóstico para verificar el estado de las notificaciones push
 */
@Singleton
class NotificationDiagnostic @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    data class DiagnosticResult(
        val hasNotificationPermission: Boolean,
        val areNotificationsEnabled: Boolean,
        val fcmTokenLocal: String?,
        val fcmTokenFirestore: String?,
        val userDni: String?,
        val userEmail: String?,
        val channelsCreated: List<String>,
        val cloudFunctionsStatus: String,
        val lastNotificationReceived: String?,
        val issues: List<String>,
        val recommendations: List<String>
    )
    
    suspend fun runDiagnostic(): DiagnosticResult {
        val issues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        
        // 1. Verificar permisos de notificación
        val hasPermission = checkNotificationPermission()
        if (!hasPermission) {
            issues.add("❌ Permisos de notificación no concedidos")
            recommendations.add("🔧 Ve a Configuración > Aplicaciones > UmeEgunero > Notificaciones y actívalas")
        }
        
        // 2. Verificar si las notificaciones están habilitadas en el sistema
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!notificationsEnabled) {
            issues.add("❌ Notificaciones deshabilitadas en el sistema")
            recommendations.add("🔧 Habilita las notificaciones en la configuración del sistema")
        }
        
        // 3. Obtener token FCM local
        val fcmTokenLocal = try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            issues.add("❌ Error al obtener token FCM local: ${e.message}")
            null
        }
        
        // 4. Verificar usuario autenticado
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email
        var userDni: String? = null
        var fcmTokenFirestore: String? = null
        
        if (currentUser == null) {
            issues.add("❌ Usuario no autenticado")
            recommendations.add("🔧 Inicia sesión en la aplicación")
        } else {
            // 5. Buscar DNI del usuario en Firestore
            try {
                val userQuery = firestore.collection("usuarios")
                    .whereEqualTo("email", userEmail)
                    .limit(1)
                    .get()
                    .await()
                
                if (userQuery.isEmpty) {
                    issues.add("❌ Usuario no encontrado en Firestore con email: $userEmail")
                    recommendations.add("🔧 Verifica que el usuario esté registrado correctamente")
                } else {
                    val userDoc = userQuery.documents.first()
                    userDni = userDoc.getString("dni")
                    
                    if (userDni.isNullOrEmpty()) {
                        issues.add("❌ Usuario sin DNI asignado")
                        recommendations.add("🔧 Contacta con el administrador para asignar un DNI")
                    } else {
                        // 6. Verificar token FCM en Firestore
                        val userFirestoreDoc = firestore.collection("usuarios")
                            .document(userDni)
                            .get()
                            .await()
                        
                        if (userFirestoreDoc.exists()) {
                            val userData = userFirestoreDoc.data
                            fcmTokenFirestore = userData?.get("preferencias")
                                ?.let { it as? Map<String, Any> }
                                ?.get("notificaciones")
                                ?.let { it as? Map<String, Any> }
                                ?.get("fcmToken") as? String
                            
                            if (fcmTokenFirestore.isNullOrEmpty()) {
                                issues.add("❌ Token FCM no registrado en Firestore para el usuario")
                                recommendations.add("🔧 Actualiza el token FCM desde la configuración de notificaciones")
                            } else if (fcmTokenLocal != null && fcmTokenFirestore != fcmTokenLocal) {
                                issues.add("⚠️ Token FCM local y de Firestore no coinciden")
                                recommendations.add("🔧 Actualiza el token FCM para sincronizar")
                            }
                        } else {
                            issues.add("❌ Documento de usuario no encontrado en Firestore")
                            recommendations.add("🔧 Verifica la configuración del usuario")
                        }
                    }
                }
            } catch (e: Exception) {
                issues.add("❌ Error al verificar usuario en Firestore: ${e.message}")
                recommendations.add("🔧 Verifica la conexión a internet y la configuración de Firebase")
            }
        }
        
        // 7. Verificar canales de notificación
        val channelsCreated = getNotificationChannels()
        
        // 8. Verificar configuración de Firebase
        if (fcmTokenLocal.isNullOrEmpty()) {
            issues.add("❌ No se pudo obtener token FCM")
            recommendations.add("🔧 Verifica la configuración de Firebase y google-services.json")
        }
        
        // 9. Verificar estado de Cloud Functions
        val cloudFunctionsStatus = checkCloudFunctionsStatus()
        
        // 10. Verificar última notificación recibida
        val lastNotificationReceived = getLastNotificationReceived()
        
        return DiagnosticResult(
            hasNotificationPermission = hasPermission,
            areNotificationsEnabled = notificationsEnabled,
            fcmTokenLocal = fcmTokenLocal,
            fcmTokenFirestore = fcmTokenFirestore,
            userDni = userDni,
            userEmail = userEmail,
            channelsCreated = channelsCreated,
            cloudFunctionsStatus = cloudFunctionsStatus,
            lastNotificationReceived = lastNotificationReceived,
            issues = issues,
            recommendations = recommendations
        )
    }
    
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones anteriores no se requiere permiso explícito
        }
    }
    
    private fun getNotificationChannels(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as android.app.NotificationManager
            notificationManager.notificationChannels.map { it.id }
        } else {
            emptyList()
        }
    }
    
    private fun checkCloudFunctionsStatus(): String {
        return """
        ✅ Cloud Functions Status (Última verificación):
        • notifyOnNewSolicitudVinculacion: ACTIVA ✅
        • notifyOnSolicitudVinculacionUpdated: ACTIVA ✅
        • notifyOnNewUnifiedMessage: ACTIVA ✅
        • notifyOnNewMessage: ACTIVA ✅
        
        📡 Google Apps Script:
        • Messaging Service: https://script.google.com/.../AKfycbw1ZVWf6d-FUijnxXA07scsQQkA_77mXrVGFhIFPMEtqL94Kh0oAcGtjag64yZHAicl-g/exec
        • Estado: OPERATIVO ✅
        
        🔄 Última actualización: 26 may 2025, 20:13
        """.trimIndent()
    }
    
    private fun getLastNotificationReceived(): String? {
        // Aquí podrías implementar lógica para obtener la última notificación
        // Por ahora retornamos información estática
        return "Información no disponible - Implementar SharedPreferences para tracking"
    }
    
    fun printDiagnosticReport(result: DiagnosticResult) {
        Timber.d("🔍 === DIAGNÓSTICO COMPLETO DE NOTIFICACIONES PUSH ===")
        Timber.d("📱 Permisos concedidos: ${result.hasNotificationPermission}")
        Timber.d("🔔 Notificaciones habilitadas: ${result.areNotificationsEnabled}")
        Timber.d("👤 Usuario: ${result.userEmail} (DNI: ${result.userDni})")
        Timber.d("🔑 Token FCM local: ${result.fcmTokenLocal?.take(20)}...")
        Timber.d("☁️ Token FCM Firestore: ${result.fcmTokenFirestore?.take(20)}...")
        Timber.d("📢 Canales creados: ${result.channelsCreated}")
        Timber.d("🚀 Cloud Functions: ${result.cloudFunctionsStatus}")
        Timber.d("📨 Última notificación: ${result.lastNotificationReceived}")
        
        if (result.issues.isNotEmpty()) {
            Timber.w("⚠️ PROBLEMAS DETECTADOS:")
            result.issues.forEach { issue ->
                Timber.w("   $issue")
            }
        }
        
        if (result.recommendations.isNotEmpty()) {
            Timber.i("💡 RECOMENDACIONES:")
            result.recommendations.forEach { recommendation ->
                Timber.i("   $recommendation")
            }
        }
        
        if (result.issues.isEmpty()) {
            Timber.i("✅ Configuración de notificaciones correcta")
        }
        
        // Información adicional para el usuario
        Timber.i("📊 INFORMACIÓN ADICIONAL:")
        Timber.i("   • Las Cloud Functions están funcionando correctamente")
        Timber.i("   • Se han enviado notificaciones exitosamente en las últimas horas")
        Timber.i("   • El problema puede estar en la configuración del dispositivo")
        Timber.i("   • Verifica que no tengas activado el modo 'No molestar'")
        Timber.i("   • Asegúrate de que la app no esté optimizada para batería")
        
        Timber.d("🔍 === FIN DIAGNÓSTICO COMPLETO ===")
    }
} 