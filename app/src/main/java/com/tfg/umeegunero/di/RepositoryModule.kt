package com.tfg.umeegunero.di

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.feature.common.academico.repository.CalendarioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
} 