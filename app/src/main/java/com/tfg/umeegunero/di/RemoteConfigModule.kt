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

/**
 * Módulo para proveer la instancia de Firebase Remote Config
 */
@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        
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
        remoteConfig.fetchAndActivate()
        
        return remoteConfig
    }
} 