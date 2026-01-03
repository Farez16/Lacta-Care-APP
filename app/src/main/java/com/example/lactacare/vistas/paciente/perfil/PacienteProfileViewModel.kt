package com.example.lactacare.vistas.paciente.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.dominio.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientProfileUiState(
    val nombre: String = "Cargando...",
    val correo: String = "",
    val nombreBebe: String? = null,
    val sessionClosed: Boolean = false
)

@HiltViewModel
class PatientProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            val nombreLocal = sessionManager.userName.first() ?: "Mamá"
            val bebe = sessionManager.babyName.first()
            
            _uiState.value = _uiState.value.copy(
                nombre = nombreLocal,
                nombreBebe = bebe
            )

            val result = authRepository.getUserProfile()
            if (result.isSuccess) {
                val profile = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    nombre = "${profile.primerNombre} ${profile.apellido}",
                    correo = profile.cedula
                )
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try { authRepository.logout() } catch (e: Exception) {}
            sessionManager.clearSession()
            _uiState.value = _uiState.value.copy(sessionClosed = true)
        }
    }
}
