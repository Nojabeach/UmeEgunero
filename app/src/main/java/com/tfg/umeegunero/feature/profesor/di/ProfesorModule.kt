package com.tfg.umeegunero.feature.profesor.di

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.feature.profesor.repository.RubricaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para proporcionar las dependencias del módulo de profesor.
 * Define los proveedores para los repositorios específicos del profesor.
 */
@Module
@InstallIn(SingletonComponent::class)
object ProfesorModule {
    
    /**
     * Proporciona una instancia del repositorio de rúbricas.
     *
     * @param firestore Instancia de Firebase Firestore
     * @return Instancia del repositorio de rúbricas
     */
    @Provides
    @Singleton
    fun provideRubricaRepository(firestore: FirebaseFirestore): RubricaRepository {
        return RubricaRepository(firestore)
    }
} 