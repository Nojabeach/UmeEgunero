package com.tfg.umeegunero.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.EventoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AsistenciaRepositoryImpl
import com.tfg.umeegunero.data.repository.AlumnoRepositoryImpl
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.TareaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        return AlumnoRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideNotificacionRepository(firestore: FirebaseFirestore): NotificacionRepository {
        return NotificacionRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideComunicadoRepository(firestore: FirebaseFirestore): ComunicadoRepository {
        return ComunicadoRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideTareaRepository(firestore: FirebaseFirestore): TareaRepository {
        return TareaRepository(firestore)
    }
    
    @Provides
    @Singleton
    fun provideRegistroDiarioRepository(
        firestore: FirebaseFirestore,
        registroActividadDao: RegistroActividadDao,
        @ApplicationContext context: Context
    ): RegistroDiarioRepository {
        return RegistroDiarioRepository(firestore, registroActividadDao, context)
    }
} 