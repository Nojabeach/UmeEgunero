package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tfg.umeegunero.data.model.EmailSoporteConfig
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar la configuración de la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para manejar las preferencias y configuraciones
 * de usuario, incluyendo temas, notificaciones, idioma y otras preferencias
 * personalizables.
 *
 * Características principales:
 * - Almacenamiento de preferencias de usuario
 * - Gestión de configuraciones globales de la aplicación
 * - Sincronización de preferencias entre dispositivos
 * - Soporte para configuraciones específicas de rol (profesor, familiar, admin)
 *
 * El repositorio permite:
 * - Personalizar la experiencia de usuario
 * - Configurar preferencias de privacidad
 * - Gestionar permisos de notificaciones
 * - Seleccionar temas y apariencia
 * - Configurar opciones de accesibilidad
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property dataStore Almacenamiento de preferencias de Android
 * @property remoteConfig Servicio de configuración remota de Firebase
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class ConfigRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val emailConfigCollection = firestore.collection("config")
    private val emailConfigDocId = "email_soporte"
    
    /**
     * Obtiene la configuración del email de soporte
     */
    suspend fun getEmailSoporteConfig(): EmailSoporteConfig {
        return try {
            val document = emailConfigCollection.document(emailConfigDocId).get().await()
            if (document.exists()) {
                document.toObject(EmailSoporteConfig::class.java) ?: EmailSoporteConfig()
            } else {
                // Si no existe la configuración, crea un documento con los valores por defecto
                val defaultConfig = EmailSoporteConfig()
                emailConfigCollection.document(emailConfigDocId).set(defaultConfig).await()
                defaultConfig
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener configuración de email de soporte")
            EmailSoporteConfig()
        }
    }
    
    /**
     * Guarda la configuración del email de soporte
     */
    suspend fun saveEmailSoporteConfig(config: EmailSoporteConfig): Boolean {
        return try {
            emailConfigCollection.document(emailConfigDocId)
                .set(config, SetOptions.merge())
                .await()
            Timber.d("Configuración de email guardada correctamente")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar configuración de email de soporte")
            false
        }
    }
} 