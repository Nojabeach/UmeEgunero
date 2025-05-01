package com.tfg.umeegunero.di

import com.tfg.umeegunero.util.EmailService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para proveer dependencias relacionadas con la funcionalidad de email.
 *
 * Este módulo provee un singleton del servicio de email para ser inyectado
 * en componentes que necesiten enviar correos electrónicos desde la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object EmailModule {
    
    /**
     * Provee una instancia singleton del servicio de correo electrónico.
     *
     * @return Instancia de EmailService configurada para su uso en la aplicación
     */
    @Provides
    @Singleton
    fun provideEmailService(): EmailService {
        return EmailService()
    }
} 