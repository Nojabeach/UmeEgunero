package com.tfg.umeegunero.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.repository.SyncRepository
import com.tfg.umeegunero.util.NetworkConnectivityManager
import com.tfg.umeegunero.util.FirmaDigitalUtil
import com.tfg.umeegunero.util.SyncManager
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.data.dao.OperacionPendienteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideFirmaDigitalUtil(): FirmaDigitalUtil = FirmaDigitalUtil

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(
        @ApplicationContext context: Context
    ): NetworkConnectivityManager = NetworkConnectivityManager(context)

    @Provides
    @Singleton
    fun provideSyncRepository(
        @ApplicationContext context: Context,
        operacionPendienteDao: OperacionPendienteDao,
        comunicadoRepository: ComunicadoRepository,
        firestore: FirebaseFirestore
    ): SyncRepository = SyncRepository(context, operacionPendienteDao, comunicadoRepository, firestore)

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context
    ): SyncManager = SyncManager(context)
} 