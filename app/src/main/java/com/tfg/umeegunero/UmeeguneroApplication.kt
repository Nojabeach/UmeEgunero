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

        // Inicializar Timber en modo debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Asegurarse de que existe un administrador
        // Puedes envolverlo en BuildConfig.DEBUG si solo quieres
        // que esto se ejecute en modo debug
        debugUtils.ensureAdminExists()
    }
}