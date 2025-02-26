package com.tfg.umeegunero

import android.app.Application
import com.google.firebase.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class UmeeguneroApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar Timber en modo debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}