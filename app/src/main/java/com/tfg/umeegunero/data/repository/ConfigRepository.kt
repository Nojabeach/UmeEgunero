package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.tfg.umeegunero.data.model.EmailSoporteConfig
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar la configuración global de la aplicación
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