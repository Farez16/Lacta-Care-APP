package com.example.lactacare.vistas.admin.perfil

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

data class AdminProfileUiState(
    val isLoading: Boolean = false,
    val nombre: String = "Cargando...",
    val correo: String = "",
    val rol: String = "",
    val error: String? = null,
    val sessionClosed: Boolean = false
)

@HiltViewModel
class AdminProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Cargar datos locales rápidos (Nombre y Rol)
            val nombreLocal = sessionManager.userName.first() ?: "Usuario"
            val rolLocal = sessionManager.userRole.first() ?: "ADMINISTRADOR"
            
            _uiState.value = _uiState.value.copy(
                nombre = nombreLocal,
                rol = rolLocal
            )

            // 2. Cargar datos remotos completos (Para obtener el correo actualizado)
            val result = authRepository.getUserProfile()
            
            if (result.isSuccess) {
                val profile = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nombre = "${profile.primerNombre} ${profile.apellido}",
                    correo = profile.cedula, // Mostramos Cédula en lugar de correo por ahora
                    rol = profile.rol ?: rolLocal
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    // Si falla el back, nos quedamos con los datos locales y mostramos error discreto
                    error = "No se pudo sincronizar el perfil completo"
                )
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.value = _uiState.value.copy(sessionClosed = true)
            } catch (e: Exception) {
                // Forzar cierre local aunque falle API
                _uiState.value = _uiState.value.copy(sessionClosed = true)
            }
        }
    }
}
