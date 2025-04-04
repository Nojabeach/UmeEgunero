package com.tfg.umeegunero.util

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.service.RemoteConfigService
import javax.inject.Inject

class RemoteConfigManager @Inject constructor(
    private val remoteConfigService: RemoteConfigService
) {
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
                // Inicializar configuración remota
                remoteConfigService.initialize(remoteConfig)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: RemoteConfigManager? = null

        fun getInstance(remoteConfigService: RemoteConfigService): RemoteConfigManager {
            return instance ?: synchronized(this) {
                instance ?: RemoteConfigManager(remoteConfigService).also { instance = it }
            }
        }
    }
} 