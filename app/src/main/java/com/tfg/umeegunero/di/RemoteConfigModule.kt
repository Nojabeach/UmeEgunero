package com.tfg.umeegunero.di

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import timber.log.Timber

/**
 * Módulo para proveer la instancia de Firebase Remote Config
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        
        try {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hora para entorno de producción
                .build()
            
            remoteConfig.setConfigSettingsAsync(configSettings)
            
            // Valores por defecto (como respaldo si falla Remote Config)
            val defaults = mapOf<String, Any>(
                "smtp_password" to ""
            )
            
            remoteConfig.setDefaultsAsync(defaults)
            
            // Forzamos una actualización inicial
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Remote Config inicializado correctamente")
                } else {
                    Timber.e(task.exception, "Error al inicializar Remote Config")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al configurar Remote Config")
        }
        
        return remoteConfig
    }
} 