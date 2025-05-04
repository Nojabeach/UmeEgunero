package com.tfg.umeegunero.di

import com.tfg.umeegunero.data.service.EmailNotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para servicios.
 * 
 * Este módulo proporciona los servicios de aplicación como singletons
 * para su uso en diferentes partes de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    /**
     * Proporciona el servicio de notificaciones por email como singleton.
     *
     * @param httpClient Cliente HTTP inyectado para realizar las peticiones
     * @return Instancia singleton de EmailNotificationService
     */
    @Provides
    @Singleton
    fun provideEmailNotificationService(httpClient: HttpClient): EmailNotificationService {
        return EmailNotificationService(httpClient)
    }
} 