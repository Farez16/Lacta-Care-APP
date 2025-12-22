// Archivo: LactaCareApplication.kt
package com.example.lactacare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LactaCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Aquí puedes inicializar librerías globales si es necesario
    }
}