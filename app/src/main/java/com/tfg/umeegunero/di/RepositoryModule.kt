package com.tfg.umeegunero.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.repository.ActividadPreescolarRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepositoryImpl
import com.tfg.umeegunero.data.repository.AlumnoRepositoryImpl
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.EstadisticasRepository
import com.tfg.umeegunero.data.repository.LocalRegistroActividadRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.data.repository.SolicitudRepository
import com.tfg.umeegunero.notification.AppNotificationManager
import com.tfg.umeegunero.data.service.NotificationService
import com.tfg.umeegunero.data.service.EmailNotificationService
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.StorageRepository
import com.tfg.umeegunero.data.repository.EtiquetaRepository
import com.tfg.umeegunero.network.ImgBBClient
import com.tfg.umeegunero.data.local.dao.ChatMensajeDao
import com.tfg.umeegunero.data.local.dao.ConversacionDao
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.util.DefaultAvatarsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Dagger/Hilt que proporciona las implementaciones de los repositorios.
 * Este módulo se encarga de crear y configurar todas las instancias de repositorios
 * que se utilizarán en la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCalendarioRepository(firestore: FirebaseFirestore): CalendarioRepository {
        return CalendarioRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideEventoRepository(
        firestore: FirebaseFirestore
    ): EventoRepository {
        return EventoRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideAsistenciaRepository(
        firestore: FirebaseFirestore
    ): AsistenciaRepository {
        return AsistenciaRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideAlumnoRepository(
        firestore: FirebaseFirestore
    ): AlumnoRepository {
        return AlumnoRepositoryImpl(firestore)
    }
    
    /**
     * Proporciona una instancia del repositorio unificado de mensajes.
     * Este repositorio centraliza la gestión de todas las comunicaciones de la aplicación.
     *
     * @param firestore Instancia de FirebaseFirestore
     * @param authRepository Repositorio de autenticación
     * @param usuarioRepository Repositorio de usuarios
     * @return Instancia de UnifiedMessageRepository
     */
    @Provides
    @Singleton
    fun provideUnifiedMessageRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository,
        usuarioRepository: UsuarioRepository
    ): UnifiedMessageRepository {
        return UnifiedMessageRepository(firestore, authRepository, usuarioRepository)
    }
    
    /**
     * Proporciona una instancia del repositorio de estadísticas.
     * Este repositorio maneja las métricas de uso de la aplicación.
     *
     * @param firestore Instancia de FirebaseFirestore
     * @return Instancia de EstadisticasRepository
     */
    @Provides
    @Singleton
    fun provideEstadisticasRepository(
        firestore: FirebaseFirestore
    ): EstadisticasRepository {
        return EstadisticasRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideRegistroDiarioRepository(
        firestore: FirebaseFirestore,
        localRegistroRepository: LocalRegistroActividadRepository,
        @ApplicationContext context: Context
    ): RegistroDiarioRepository {
        return RegistroDiarioRepository(firestore, localRegistroRepository, context)
    }
    
    /**
     * Proporciona una instancia del repositorio local para los registros de actividad.
     * Este repositorio maneja la persistencia local con Room.
     *
     * @param registroActividadDao DAO para acceder a los registros de actividad
     * @return Instancia de LocalRegistroActividadRepository
     */
    @Provides
    @Singleton
    fun provideLocalRegistroActividadRepository(
        registroActividadDao: RegistroActividadDao
    ): LocalRegistroActividadRepository {
        return LocalRegistroActividadRepository(registroActividadDao)
    }
    
    /**
     * Proporciona una instancia del repositorio para cursos académicos.
     * Este repositorio maneja las operaciones con cursos de los centros educativos.
     *
     * @return Instancia de CursoRepository
     */
    @Provides
    @Singleton
    fun provideCursoRepository(): CursoRepository {
        return CursoRepository()
    }
    
    /**
     * Proporciona una instancia del repositorio para actividades preescolares.
     * Este repositorio maneja las operaciones con actividades adaptadas para niños de 2-3 años.
     *
     * @param firestore Instancia de FirebaseFirestore
     * @return Instancia de ActividadPreescolarRepository
     */
    @Provides
    @Singleton
    fun provideActividadPreescolarRepository(
        firestore: FirebaseFirestore
    ): ActividadPreescolarRepository {
        return ActividadPreescolarRepository(firestore)
    }
    
    /**
     * Proporciona una instancia del repositorio para la gestión de familiares.
     * Este repositorio maneja las operaciones de vinculación entre familiares y alumnos.
     *
     * @param firestore Instancia de FirebaseFirestore
     * @return Instancia de FamiliarRepository
     */
    @Provides
    @Singleton
    fun provideFamiliarRepository(
        firestore: FirebaseFirestore
    ): com.tfg.umeegunero.data.repository.FamiliarRepository {
        return com.tfg.umeegunero.data.repository.FamiliarRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideSolicitudRepository(
        firestore: FirebaseFirestore,
        notificationManager: AppNotificationManager,
        notificationService: NotificationService,
        centroRepository: CentroRepository,
        usuarioRepository: UsuarioRepository,
        alumnoRepository: AlumnoRepository,
        emailNotificationService: EmailNotificationService,
        unifiedMessageRepository: UnifiedMessageRepository
    ): SolicitudRepository {
        return SolicitudRepository(
            firestore,
            notificationManager,
            notificationService,
            centroRepository,
            usuarioRepository,
            alumnoRepository,
            emailNotificationService,
            unifiedMessageRepository
        )
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        @ApplicationContext context: Context,
        storage: FirebaseStorage,
        imgBBClient: ImgBBClient
    ): StorageRepository {
        return StorageRepository(context, storage, imgBBClient)
    }
    
    @Provides
    @Singleton
    fun provideEtiquetaRepository(firestore: FirebaseFirestore): EtiquetaRepository {
        return EtiquetaRepository(firestore)
    }

    /**
     * Proporciona una instancia del repositorio de chat.
     * Este repositorio maneja todas las operaciones relacionadas con mensajes y conversaciones.
     *
     * @param chatMensajeDao DAO para operaciones con mensajes
     * @param conversacionDao DAO para operaciones con conversaciones
     * @param firestore Instancia de FirebaseFirestore
     * @param storage Instancia de FirebaseStorage
     * @param authRepository Repositorio de autenticación
     * @return Instancia de ChatRepository
     */
    @Provides
    @Singleton
    fun provideChatRepository(
        chatMensajeDao: ChatMensajeDao,
        conversacionDao: ConversacionDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        authRepository: AuthRepository
    ): ChatRepository {
        return ChatRepository(chatMensajeDao, conversacionDao, firestore, storage, authRepository)
    }

    @Provides
    @Singleton
    fun provideDefaultAvatarsManager(
        @ApplicationContext context: Context,
        storageRepository: StorageRepository
    ): DefaultAvatarsManager {
        return DefaultAvatarsManager(context, storageRepository)
    }
} 