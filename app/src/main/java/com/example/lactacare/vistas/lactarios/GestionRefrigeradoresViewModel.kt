package com.example.lactacare.vistas.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.Refrigerador
import com.example.lactacare.dominio.repository.RefrigeradorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado exclusivo para la pantalla de Refrigeradores
data class GestionRefrisUiState(
    val listaRefrigeradores: List<Refrigerador> = emptyList(),
    val isLoading: Boolean = false,
    val mensajeError: String? = null
)

class GestionRefrigeradoresViewModel(
    private val repo: RefrigeradorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionRefrisUiState())
    val uiState: StateFlow<GestionRefrisUiState> = _uiState.asStateFlow()

    private var idLactarioActual: Int = 0

    // 1. Cargar solo los de la sala seleccionada
    fun cargarRefrigeradores(idLactario: Int) {
        idLactarioActual = idLactario
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val refris = repo.obtenerPorLactario(idLactario)
                _uiState.update { it.copy(listaRefrigeradores = refris, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeError = e.message, isLoading = false) }
            }
        }
    }

    // 2. Guardar vinculando al ID de la sala
    fun guardarRefrigerador(refrigerador: Refrigerador) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val refriVinculado = refrigerador.copy(idLactario = idLactarioActual)
            if (repo.guardar(refriVinculado)) {
                cargarRefrigeradores(idLactarioActual)
            } else {
                _uiState.update { it.copy(isLoading = false, mensajeError = "Error al guardar") }
            }
        }
    }

    // 3. Eliminar
    fun eliminarRefrigerador(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (repo.eliminar(id)) {
                cargarRefrigeradores(idLactarioActual)
            } else {
                _uiState.update { it.copy(isLoading = false, mensajeError = "Error al eliminar") }
            }
        }
    }
}