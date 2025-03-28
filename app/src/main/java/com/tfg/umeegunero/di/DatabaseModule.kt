package com.tfg.umeegunero.di

import android.content.Context
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar dependencias relacionadas con la base de datos Room.
 * 
 * Este módulo se encarga de proporcionar la instancia de la base de datos local
 * y los DAOs necesarios para acceder a ella.
 * 
 * Funcionalidades:
 * - Proporciona la instancia única de AppDatabase
 * - Proporciona el DAO para acceder a los registros de actividad
 * 
 * @author Estudiante 2º DAM
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Proporciona una instancia única de la base de datos Room.
     * 
     * @param context Contexto de la aplicación inyectado por Hilt
     * @return Instancia única (singleton) de AppDatabase
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    /**
     * Proporciona el DAO para acceder a los registros de actividad.
     * 
     * @param database Instancia de la base de datos
     * @return DAO para acceder a los registros de actividad
     */
    @Provides
    @Singleton
    fun provideRegistroActividadDao(database: AppDatabase): RegistroActividadDao {
        return database.registroActividadDao()
    }
} 