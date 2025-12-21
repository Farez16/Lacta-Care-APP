// Archivo: LactaCareApplication.kt
package com.example.lactacare

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // <--- Esta anotación es OBLIGATORIA para Hilt
class LactaCareApplication : Application()