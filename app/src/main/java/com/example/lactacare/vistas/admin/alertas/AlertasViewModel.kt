package com.example.lactacare.vistas.admin.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.SistemaAlertaDto
import com.example.lactacare.dominio.repository.IAlertasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertasUiState(
    val isLoading: Boolean = false,
    val alertas: List<SistemaAlertaDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AlertasViewModel @Inject constructor(
    private val repository: IAlertasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertasUiState())
    val uiState = _uiState.asStateFlow()

    init {
        obtenerAlertas()
    }

    fun obtenerAlertas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.obtenerAlertas()
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alertas = result.getOrDefault(emptyList()).sortedByDescending { it.id } // Ordenar por mas reciente
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar alertas"
                )
            }
        }
    }
}
