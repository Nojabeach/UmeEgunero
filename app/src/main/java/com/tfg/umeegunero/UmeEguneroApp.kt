package com.tfg.umeegunero

import android.app.Application
import com.google.firebase.BuildConfig
import com.tfg.umeegunero.util.DebugUtils
import com.tfg.umeegunero.utils.RemoteConfigManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class UmeEguneroApp : Application() {

    @Inject
    lateinit var debugUtils: DebugUtils

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Timber solo en debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Inicializar Remote Config
        RemoteConfigManager.getInstance().initialize(this)

        // Me aseguro de que haya un admin en el sistema
        debugUtils.ensureAdminExists()
    }
} 