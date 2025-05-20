package com.tfg.umeegunero.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Singleton

/**
 * Módulo para la configuración global de Coil
 * 
 * Este módulo proporciona una configuración optimizada de Coil para toda la aplicación,
 * mejorando el tiempo de carga y rendimiento para imágenes.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    /**
     * Proporciona un ImageLoader configurado para optimizar la carga de imágenes
     */
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient // Usa el OkHttpClient proporcionado por NetworkModule
    ): ImageLoader {
        Timber.d("Creando ImageLoader optimizado para la aplicación")
        
        return ImageLoader.Builder(context)
            .crossfade(false) // Desactiva la animación de transición para carga instantánea
            .okHttpClient(okHttpClient)
            .respectCacheHeaders(false) // Ignora cabeceras de caché para imágenes de Firebase
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Usa 25% de la memoria disponible
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05) // 5% del espacio en disco
                    .build()
            }
            .build()
    }
} 