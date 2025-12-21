package com.example.lactacare.vistas.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.ContenedorLeche
import com.example.lactacare.dominio.repository.InventarioRepository
import com.example.lactacare.dominio.model.Refrigerador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados de la UI
sealed class InventarioUiState {
    object Loading : InventarioUiState()
    data class Success(
        val refrigeradores: List<Refrigerador>,
        val stockLeche: List<ContenedorLeche>
    ) : InventarioUiState()
    data class Error(val mensaje: String) : InventarioUiState()
}

class InventarioViewModel(
    private val inventarioRepository: InventarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventarioUiState>(InventarioUiState.Loading)
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    init {
        cargarInventario()
    }

    fun cargarInventario() {
        viewModelScope.launch {
            _uiState.value = InventarioUiState.Loading
            try {
                // 1. Obtenemos los refris del lactario actual (ID 1 por defecto)
                val refris = inventarioRepository.obtenerRefrigeradores(idLactario = 1)

                // 2. Obtenemos algo de stock para mostrar (simulado por madre ID 1)
                // En una app real, aquí pedirías "todo el stock del lactario"
                val stock = inventarioRepository.obtenerStockPorMadre(idMadre = 1)

                _uiState.value = InventarioUiState.Success(
                    refrigeradores = refris,
                    stockLeche = stock
                )
            } catch (e: Exception) {
                _uiState.value = InventarioUiState.Error("Error cargando inventario: ${e.message}")
            }
        }
    }

    // Aquí agregaríamos funciones como guardarLeche() más adelante
}