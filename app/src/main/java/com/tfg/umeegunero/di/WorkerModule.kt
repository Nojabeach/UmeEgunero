package com.tfg.umeegunero.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para proporcionar configuración de WorkManager
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    /**
     * Proporciona la configuración de WorkManager
     */
    @Provides
    @Singleton
    fun provideWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
    
    /**
     * Inicializa WorkManager con la configuración personalizada
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context,
        configuration: Configuration
    ): WorkManager {
        // Inicializar WorkManager con nuestra configuración
        WorkManager.initialize(context, configuration)
        return WorkManager.getInstance(context)
    }
} 