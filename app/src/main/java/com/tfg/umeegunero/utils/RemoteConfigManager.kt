package com.tfg.umeegunero.utils

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tfg.umeegunero.R
import com.tfg.umeegunero.feature.common.support.screen.EmailSender

class RemoteConfigManager private constructor() {
    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun initialize(context: Context) {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hora
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Inicializar EmailSender con la configuración
                EmailSender.initialize(remoteConfig)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: RemoteConfigManager? = null

        fun getInstance(): RemoteConfigManager {
            return instance ?: synchronized(this) {
                instance ?: RemoteConfigManager().also { instance = it }
            }
        }
    }
} 