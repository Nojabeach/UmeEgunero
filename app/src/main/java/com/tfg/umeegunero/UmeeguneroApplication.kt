package com.tfg.umeegunero

import android.app.Application
import com.google.firebase.BuildConfig
import com.tfg.umeegunero.util.DebugUtils
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class UmeeguneroApplication : Application() {

    @Inject
    lateinit var debugUtils: DebugUtils

    override fun onCreate() {
        super.onCreate()

        // Inicializar Timber solo en debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Me aseguro de que haya un admin en el sistema
        // Lo dejo siempre activo, aunque podría limitarlo a DEBUG
        // si fuera necesario
        debugUtils.ensureAdminExists()
    }
}