package com.tfg.umeegunero.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AlumnoRepositoryImpl
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.data.repository.PersonalDocenteRepository
import com.tfg.umeegunero.data.repository.UserRepository
import com.tfg.umeegunero.util.FirestoreQueryUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar dependencias relacionadas con Firestore
 */
@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    /**
     * Proporciona una instancia de FirestoreQueryUtil
     */
    @Provides
    @Singleton
    fun provideFirestoreQueryUtil(firestore: FirebaseFirestore): FirestoreQueryUtil {
        return FirestoreQueryUtil(firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): UserRepository = UserRepository(firestore, auth)

    @Provides
    @Singleton
    fun provideAlumnoRepository(
        firestore: FirebaseFirestore
    ): AlumnoRepository = AlumnoRepositoryImpl(firestore)

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

    @Provides
    @Singleton
    fun provideComunicadoRepository(
        firestore: FirebaseFirestore,
        firestoreQueryUtil: FirestoreQueryUtil
    ): ComunicadoRepository = ComunicadoRepository(firestore, firestoreQueryUtil)
} 