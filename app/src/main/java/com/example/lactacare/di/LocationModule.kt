package com.example.lactacare.di
import android.content.Context
import com.example.lactacare.datos.location.LocationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationHelper(
        @ApplicationContext context: Context
    ): LocationHelper {
        return LocationHelper(context)
    }
}