package com.example.lactacare.di

import com.example.lactacare.datos.repository.AlertasRepository
import com.example.lactacare.datos.repository.AuthRepositoryImpl
import com.example.lactacare.datos.repository.ChatRepositoryImpl
import com.example.lactacare.datos.repository.DoctorRepository
import com.example.lactacare.datos.repository.InventoryRepository
import com.example.lactacare.datos.repository.LactariosRepository
import com.example.lactacare.datos.repository.PatientRepository
import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.dominio.repository.ChatRepository
import com.example.lactacare.dominio.repository.IAlertasRepository
import com.example.lactacare.dominio.repository.IDoctorRepository
import com.example.lactacare.dominio.repository.IInventoryRepository
import com.example.lactacare.dominio.repository.ILactariosRepository
import com.example.lactacare.dominio.repository.IPatientRepository
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

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(
        inventoryRepository: InventoryRepository
    ): IInventoryRepository

    @Binds
    @Singleton
    abstract fun bindLactariosRepository(
        lactariosRepository: LactariosRepository
    ): ILactariosRepository

    @Binds
    @Singleton
    abstract fun bindDoctorRepository(
        doctorRepository: DoctorRepository
    ): IDoctorRepository

    @Binds
    @Singleton
    abstract fun bindPatientRepository(
        patientRepository: PatientRepository
    ): IPatientRepository

    // Agregado del primer archivo
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    // Agregado del segundo archivo
    @Binds
    @Singleton
    abstract fun bindAlertasRepository(
        alertasRepository: AlertasRepository
    ): IAlertasRepository
}