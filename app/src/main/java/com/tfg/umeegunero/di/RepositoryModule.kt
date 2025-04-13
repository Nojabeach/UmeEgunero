package com.tfg.umeegunero.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.repository.ActividadPreescolarRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepositoryImpl
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ExportacionRepository
import com.tfg.umeegunero.data.repository.ExportacionRepositoryImpl
import com.tfg.umeegunero.data.repository.LocalRegistroActividadRepository
import com.tfg.umeegunero.data.repository.MensajeRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.TareaRepository
import com.tfg.umeegunero.util.FirestoreQueryUtil
import com.tfg.umeegunero.data.repository.PersonalDocenteRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.inject.Named

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
    fun provideNotificacionRepository(firestore: FirebaseFirestore): NotificacionRepository {
        return NotificacionRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideTareaRepository(firestore: FirebaseFirestore): TareaRepository {
        return TareaRepository(firestore)
    }
    
    /**
     * Proporciona una instancia del repositorio de mensajes.
     * Este repositorio maneja la comunicación entre usuarios.
     *
     * @param firestore Instancia de FirebaseFirestore
     * @param authRepository Repositorio de autenticación
     * @param storage Instancia de FirebaseStorage
     * @return Instancia de MensajeRepository
     */
    @Provides
    @Singleton
    fun provideMensajeRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository,
        storage: FirebaseStorage
    ): MensajeRepository {
        return MensajeRepository(firestore, authRepository, storage)
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

    @Provides
    @Named("personalDocenteCollection")
    fun providePersonalDocenteCollection(
        firestore: FirebaseFirestore
    ): CollectionReference {
        return firestore.collection("personal_docente")
    }

    @Provides
    @Singleton
    fun providePersonalDocenteRepository(
        @Named("personalDocenteCollection") personalDocenteCollection: CollectionReference,
        firestore: FirebaseFirestore
    ): PersonalDocenteRepository {
        return PersonalDocenteRepository(personalDocenteCollection, firestore)
    }
    
    /**
     * Proporciona una instancia del repositorio de exportación de datos.
     * Este repositorio permite exportar diferentes tipos de datos a formatos como PDF y CSV.
     *
     * @param firestore Instancia de FirebaseFirestore
     * @param alumnoRepository Repositorio de alumnos
     * @param registroDiarioRepository Repositorio de registros diarios
     * @param comunicadoRepository Repositorio de comunicados
     * @param asistenciaRepository Repositorio de asistencia
     * @param context Contexto de la aplicación
     * @return Instancia de ExportacionRepository
     */
    @Provides
    @Singleton
    fun provideExportacionRepository(
        firestore: FirebaseFirestore,
        alumnoRepository: AlumnoRepository,
        registroDiarioRepository: RegistroDiarioRepository,
        comunicadoRepository: ComunicadoRepository,
        asistenciaRepository: AsistenciaRepository,
        @ApplicationContext context: Context
    ): ExportacionRepository {
        return ExportacionRepositoryImpl(
            firestore,
            alumnoRepository,
            registroDiarioRepository,
            comunicadoRepository,
            asistenciaRepository,
            context
        )
    }
} 