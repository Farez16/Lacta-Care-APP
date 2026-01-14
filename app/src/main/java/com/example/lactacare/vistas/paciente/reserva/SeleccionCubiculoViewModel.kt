package com.example.lactacare.vistas.paciente.reserva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.CubiculoDto
import com.example.lactacare.datos.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeleccionCubiculoUiState(
    val cubiculos: List<CubiculoDto> = emptyList(),
    val cubiculoSeleccionado: CubiculoDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SeleccionCubiculoViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeleccionCubiculoUiState())
    val uiState: StateFlow<SeleccionCubiculoUiState> = _uiState.asStateFlow()

    fun cargarCubiculos(idSala: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val cubiculos = apiService.obtenerCubiculosSala(idSala)
                
                // Filtrar solo disponibles
                val disponibles = cubiculos.filter { it.disponible }
                
                _uiState.update {
                    it.copy(
                        cubiculos = disponibles,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al cargar cubículos: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun seleccionarCubiculo(cubiculo: CubiculoDto) {
        _uiState.update { it.copy(cubiculoSeleccionado = cubiculo) }
    }
}
