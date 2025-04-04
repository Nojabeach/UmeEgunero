package com.tfg.umeegunero.data.service

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para acceder a los valores de Firebase Remote Config
 */
@Singleton
class RemoteConfigService @Inject constructor() {
    
    private lateinit var remoteConfig: FirebaseRemoteConfig
    
    companion object {
        const val SMTP_PASSWORD_KEY = "smtp_password"
    }
    
    /**
     * Inicializa el servicio con la instancia de RemoteConfig
     */
    fun initialize(config: FirebaseRemoteConfig) {
        this.remoteConfig = config
        Timber.d("RemoteConfigService inicializado")
    }
    
    /**
     * Obtiene la contraseña SMTP almacenada en Remote Config
     */
    suspend fun getSMTPPassword(): String {
        return withContext(Dispatchers.IO) {
            try {
                if (!::remoteConfig.isInitialized) {
                    Timber.e("RemoteConfig no ha sido inicializado")
                    return@withContext ""
                }
                
                // Nos aseguramos de tener la configuración más reciente
                remoteConfig.fetchAndActivate().await()
                val password = remoteConfig.getString(SMTP_PASSWORD_KEY)
                Timber.d("Contraseña SMTP obtenida desde Remote Config")
                password
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener la contraseña SMTP desde Remote Config")
                "" // En caso de error devolvemos una cadena vacía
            }
        }
    }
} 