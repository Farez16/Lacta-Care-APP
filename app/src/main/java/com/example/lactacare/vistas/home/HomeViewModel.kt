package com.example.lactacare.vistas.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.datos.repository.AdminRepository
import com.example.lactacare.dominio.model.DashboardAdminStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI
data class HomeUiState(
    val nombreUsuario: String = "Cargando...",
    val userId: Long? = null,
    val isLoading: Boolean = false,
    // Campo nuevo para guardar las estadísticas del Admin
    val adminStats: DashboardAdminStats? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val adminRepository: AdminRepository // Inyectamos el repo nuevo
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // 1. Obtenemos nombre, rol y userId de la sesión local
            val nombre = sessionManager.userName.first() ?: "Usuario"
            val rol = sessionManager.userRole.first() ?: "PACIENTE"
            val userId = sessionManager.userId.first()  // ✅ AGREGAR ESTA LÍNEA
            // 2. Lógica según el Rol
            if (rol == "ADMINISTRADOR") {
                // Si es Admin, pedimos las estadísticas a la API
                val stats = adminRepository.obtenerEstadisticas()
                _uiState.value = _uiState.value.copy(
                    nombreUsuario = nombre,
                    userId = userId,  // ✅ AGREGAR ESTA LÍNEA
                    adminStats = stats,
                    isLoading = false
                )
            } else {
                // Si es Paciente o Doctor, por ahora solo mostramos el nombre
                _uiState.value = _uiState.value.copy(
                    nombreUsuario = nombre,
                    userId = userId,  // ✅ AGREGAR ESTA LÍNEA
                    isLoading = false
                )
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }
}