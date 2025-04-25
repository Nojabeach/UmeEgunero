package com.tfg.umeegunero.data.service

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
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
                
                remoteConfig.fetchAndActivate().await()
                val password = remoteConfig.getString(SMTP_PASSWORD_KEY)
                Timber.d("Contraseña SMTP obtenida desde Remote Config")
                password
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener la contraseña SMTP desde Remote Config")
                ""
            }
        }
    }

    /**
     * Actualiza la contraseña SMTP en Remote Config
     * @param newPassword Nueva contraseña SMTP
     * @return true si la actualización fue exitosa
     */
    suspend fun updateSMTPPassword(newPassword: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!::remoteConfig.isInitialized) {
                    Timber.e("RemoteConfig no ha sido inicializado")
                    return@withContext false
                }

                // Configuramos para actualización inmediata
                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(0)
                    .build()

                // Actualizamos la configuración
                remoteConfig.setConfigSettingsAsync(configSettings).await()

                // Establecemos el nuevo valor
                val defaults = mapOf(SMTP_PASSWORD_KEY to newPassword)
                remoteConfig.setDefaultsAsync(defaults).await()

                // Forzamos la actualización y activación
                val updated = remoteConfig.fetchAndActivate().await()
                
                if (updated) {
                    // Verificamos que el valor se haya actualizado correctamente
                    val currentPassword = remoteConfig.getString(SMTP_PASSWORD_KEY)
                    if (currentPassword == newPassword) {
                        Timber.d("Contraseña SMTP actualizada y verificada correctamente en Remote Config")
                        true
                    } else {
                        Timber.e("La contraseña SMTP no se actualizó correctamente en Remote Config")
                        false
                    }
                } else {
                    Timber.e("No se pudo actualizar la contraseña SMTP en Remote Config")
                    false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar la contraseña SMTP en Remote Config")
                false
            }
        }
    }
} 