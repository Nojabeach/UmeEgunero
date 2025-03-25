package com.tfg.umeegunero.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Las dependencias se han movido a FirebaseModule para evitar duplicación
} 