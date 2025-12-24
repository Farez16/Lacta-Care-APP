package com.example.lactacare.vistas.bebe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BebeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    fun registrarBebe(nombre: String, fechaNacimiento: String, sexo: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            // Guardado SIMULADO pero PERSISTENTE (Local DataStore)
            // Ya que no hay endpoint de backend para bebé
            sessionManager.saveBabyData(nombre, fechaNacimiento, sexo)
            onFinished()
        }
    }
}
