package com.tfg.umeegunero.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo que proporciona el Context para la aplicación
 */
@Module
@InstallIn(SingletonComponent::class)
object ContextModule {
    
    /**
     * Proporciona el Context de la aplicación para ser inyectado en clases
     * que lo necesitan, como NotificationHelper y otros servicios.
     */
    @Singleton
    @Provides
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
} 