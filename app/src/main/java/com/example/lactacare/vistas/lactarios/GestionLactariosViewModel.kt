package com.example.lactacare.vistas.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.dominio.repository.LactarioRepository
import com.example.lactacare.dominio.repository.RefrigeradorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla (UI State)
data class GestionLactariosUiState(
    val listaLactarios: List<Lactario> = emptyList(),
    val isLoading: Boolean = false,
    val mensajeError: String? = null
)

class GestionLactariosViewModel(
    private val lactarioRepo: LactarioRepository,
    private val refrigeradorRepo: RefrigeradorRepository // Inyectamos esto por si en el futuro validamos borrados
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionLactariosUiState())
    val uiState: StateFlow<GestionLactariosUiState> = _uiState.asStateFlow()

    init {
        // Cargar datos automáticamente al iniciar
        cargarLactarios()
    }

    // 1. CARGAR LISTA
    fun cargarLactarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val lactarios = lactarioRepo.obtenerLactarios()
                _uiState.update {
                    it.copy(listaLactarios = lactarios, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensajeError = "Error al cargar: ${e.message}", isLoading = false)
                }
            }
        }
    }

    // 2. GUARDAR (CREAR O EDITAR)
    // El repositorio sabe si es nuevo (id=0) o existente (id>0)
    fun guardarLactario(lactario: Lactario) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val exito = lactarioRepo.guardarLactario(lactario)

            if (exito) {
                cargarLactarios() // Recargar lista para ver cambios
            } else {
                _uiState.update {
                    it.copy(isLoading = false, mensajeError = "No se pudo guardar la sala")
                }
            }
        }
    }

    // 3. ELIMINAR
    fun eliminarLactario(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Opcional: Aquí podrías checar con refrigeradorRepo si la sala tiene neveras antes de borrar

            val exito = lactarioRepo.eliminarLactario(id)
            if (exito) {
                cargarLactarios()
            } else {
                _uiState.update {
                    it.copy(isLoading = false, mensajeError = "Error al eliminar la sala")
                }
            }
        }
    }

    // Limpiar mensaje de error
    fun limpiarError() {
        _uiState.update { it.copy(mensajeError = null) }
    }
}