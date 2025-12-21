package com.example.lactacare.di

// --- CORRECCIÓN AQUÍ: Agregamos .repository ---
import com.example.lactacare.dominio.repository.AuthRepository
// ----------------------------------------------

import com.example.lactacare.datos.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}