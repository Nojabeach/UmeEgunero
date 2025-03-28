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
 * Módulo Dagger/Hilt que proporciona las dependencias relacionadas con la base de datos Room.
 * Se instala en el componente SingletonComponent para garantizar una única instancia en toda la app.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Proporciona una instancia singleton de la base de datos Room.
     *
     * @param context Contexto de la aplicación
     * @return Instancia de AppDatabase
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    /**
     * Proporciona el DAO para operaciones con RegistroActividad.
     *
     * @param appDatabase Instancia de la base de datos
     * @return Implementación de RegistroActividadDao
     */
    @Provides
    fun provideRegistroActividadDao(appDatabase: AppDatabase): RegistroActividadDao {
        return appDatabase.registroActividadDao()
    }
} 