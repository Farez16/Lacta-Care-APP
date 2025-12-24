package com.example.lactacare.vistas.admin.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.dominio.repository.IInventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventarioUiState(
    val isLoading: Boolean = false,
    val contenedores: List<ContenedorLecheDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val repository: IInventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarInventario()
    }

    fun cargarInventario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.obtenerInventario()
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contenedores = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar inventario"
                )
            }
        }
    }
}
