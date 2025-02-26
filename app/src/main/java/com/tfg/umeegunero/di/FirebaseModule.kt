package com.tfg.umeegunero.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.DebugUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideDebugUtils(usuarioRepository: UsuarioRepository): DebugUtils {
        return DebugUtils(usuarioRepository)
    }
}