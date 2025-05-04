package com.tfg.umeegunero.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.AuthRepositoryImpl
import com.tfg.umeegunero.data.repository.CiudadRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.ClaseRepositoryImpl
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.service.RemoteConfigService
import com.tfg.umeegunero.util.DebugUtils
import com.tfg.umeegunero.util.FirestoreCache
import com.tfg.umeegunero.util.ErrorHandler
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.functions.FirebaseFunctions
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
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideStorageReference(storage: FirebaseStorage): StorageReference {
        return storage.reference
    }

    @Provides
    @Singleton
    fun provideRemoteConfigService(remoteConfig: FirebaseRemoteConfig): RemoteConfigService {
        val service = RemoteConfigService()
        service.initialize(remoteConfig)
        return service
    }

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        return FirebaseFunctions.getInstance()
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        remoteConfigService: RemoteConfigService
    ): UsuarioRepository {
        return UsuarioRepository(firebaseAuth, firestore, remoteConfigService)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        usuarioRepository: UsuarioRepository,
        comunicadoRepository: ComunicadoRepository,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, usuarioRepository, comunicadoRepository, firestore)
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
    fun provideCentroRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        firestoreCache: FirestoreCache,
        errorHandler: ErrorHandler
    ): CentroRepository {
        return CentroRepository(firestore, auth, firestoreCache, errorHandler)
    }
    
    @Provides
    @Singleton
    fun provideCiudadRepository(@ApplicationContext context: Context): CiudadRepository {
        return CiudadRepository(context)
    }

    @Provides
    @Singleton
    fun provideFirestoreCache(): FirestoreCache {
        return FirestoreCache()
    }

    @Provides
    @Singleton
    fun provideErrorHandler(): ErrorHandler {
        return ErrorHandler()
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}