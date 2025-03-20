package com.tfg.umeegunero.di

import android.content.Context
import android.content.SharedPreferences
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferenciasRepository(
        @ApplicationContext context: Context
    ): PreferenciasRepository {
        return PreferenciasRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("ume_egunero_prefs", Context.MODE_PRIVATE)
    }
} 