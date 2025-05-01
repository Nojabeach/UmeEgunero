package com.tfg.umeegunero.di

import com.tfg.umeegunero.data.network.NominatimApiService
import com.tfg.umeegunero.data.network.NominatimRetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para configuraciones de red.
 * 
 * Este módulo proporciona las dependencias relacionadas con servicios web 
 * y APIs externas utilizadas en la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Proporciona el servicio API de Nominatim utilizando el cliente Retrofit existente.
     * 
     * @return Implementación del servicio NominatimApiService
     */
    @Provides
    @Singleton
    fun provideNominatimApiService(): NominatimApiService {
        return NominatimRetrofitClient.nominatimApiService
    }
} 