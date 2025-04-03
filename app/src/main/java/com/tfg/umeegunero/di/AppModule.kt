package com.tfg.umeegunero.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.local.dao.ChatMensajeDao
import com.tfg.umeegunero.data.local.dao.ConversacionDao
import com.tfg.umeegunero.data.local.database.AppDatabase
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.data.repository.PreferenciasRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo principal de inyección de dependencias para la aplicación UmeEgunero.
 * 
 * Este módulo se encarga de proporcionar las dependencias principales que no están
 * directamente relacionadas con Firebase, como el repositorio de preferencias y
 * el objeto SharedPreferences para el almacenamiento local.
 * 
 * Funcionalidades:
 * - Proporciona el repositorio de preferencias para gestionar las configuraciones de usuario
 * - Proporciona el objeto SharedPreferences para almacenamiento ligero de datos
 * 
 * @author Estudiante 2º DAM
 * @see PreferenciasRepository
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Proporciona una instancia del repositorio de preferencias de usuario.
     * 
     * Este repositorio gestiona las preferencias del usuario como el tema de la aplicación
     * y otras configuraciones personales utilizando DataStore como almacenamiento.
     * 
     * @param context Contexto de la aplicación inyectado por Hilt
     * @return Instancia única (singleton) de PreferenciasRepository
     */
    @Provides
    @Singleton
    fun providePreferenciasRepository(
        @ApplicationContext context: Context
    ): PreferenciasRepository {
        return PreferenciasRepository(context)
    }
    
    /**
     * Proporciona una instancia de SharedPreferences para almacenamiento ligero.
     * 
     * Se utiliza para guardar datos simples como credenciales, configuraciones básicas
     * y estados temporales de la aplicación.
     * 
     * @param context Contexto de la aplicación inyectado por Hilt
     * @return Instancia única (singleton) de SharedPreferences
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("ume_egunero_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Proporciona el DAO para los mensajes de chat.
     */
    @Provides
    @Singleton
    fun provideChatMensajeDao(database: AppDatabase): ChatMensajeDao {
        return database.chatMensajeDao()
    }
    
    /**
     * Proporciona el DAO para las conversaciones.
     */
    @Provides
    @Singleton
    fun provideConversacionDao(database: AppDatabase): ConversacionDao {
        return database.conversacionDao()
    }
    
    /**
     * Proporciona el repositorio de chat.
     */
    @Provides
    @Singleton
    fun provideChatRepository(
        chatMensajeDao: ChatMensajeDao,
        conversacionDao: ConversacionDao,
        firestore: FirebaseFirestore
    ): ChatRepository {
        return ChatRepository(chatMensajeDao, conversacionDao, firestore)
    }
} 