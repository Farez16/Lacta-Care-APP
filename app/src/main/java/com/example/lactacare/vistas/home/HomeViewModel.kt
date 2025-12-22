package com.example.lactacare.vistas.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI
data class HomeUiState(
    val nombreUsuario: String = "Cargando...",
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        viewModelScope.launch {
            // CORRECCIÓN: Usamos 'userName' (que hicimos público en SessionManager)
            // en lugar de intentar acceder a 'context' (que es privado).
            sessionManager.userName.collectLatest { nombre ->
                _uiState.value = _uiState.value.copy(
                    nombreUsuario = nombre ?: "Usuario",
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