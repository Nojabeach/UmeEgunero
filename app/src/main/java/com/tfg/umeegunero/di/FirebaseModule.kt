package com.tfg.umeegunero.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.AuthRepositoryImpl
import com.tfg.umeegunero.data.repository.CiudadRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.ClaseRepositoryImpl
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.DebugUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UsuarioRepository {
        return UsuarioRepository(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        usuarioRepository: UsuarioRepository
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, usuarioRepository)
    }

    @Provides
    @Singleton
    fun provideDebugUtils(usuarioRepository: UsuarioRepository): DebugUtils {
        return DebugUtils(usuarioRepository)
    }

    @Provides
    @Singleton
    fun provideClaseRepository(
        firestore: FirebaseFirestore
    ): ClaseRepository {
        return ClaseRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideCursoRepository(
        firestore: FirebaseFirestore
    ): CursoRepository {
        return CursoRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideCentroRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CentroRepository {
        return CentroRepository(firestore, auth)
    }
    
    @Provides
    @Singleton
    fun provideCiudadRepository(@ApplicationContext context: Context): CiudadRepository {
        return CiudadRepository(context)
    }
}