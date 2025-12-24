package com.example.lactacare.vistas.doctor.perfil

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

data class DoctorProfileUiState(
    val isLoading: Boolean = false,
    val nombre: String = "Cargando...",
    val correo: String = "",
    val rol: String = "",
    val error: String? = null,
    val sessionClosed: Boolean = false
)

@HiltViewModel
class DoctorProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val nombreLocal = sessionManager.userName.first() ?: "Doctor"
            val rolLocal = sessionManager.userRole.first() ?: "DOCTOR"
            
            _uiState.value = _uiState.value.copy(
                nombre = nombreLocal,
                rol = rolLocal
            )

            // Intentar cargar correo del back
            val result = authRepository.getUserProfile()
            if (result.isSuccess) {
                val profile = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nombre = "${profile.primerNombre} ${profile.apellido}",
                    correo = profile.cedula,
                    rol = profile.rol ?: rolLocal
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                // Ignorar error de red al salir
            }
            sessionManager.clearSession()
            _uiState.value = _uiState.value.copy(sessionClosed = true)
        }
    }
}
