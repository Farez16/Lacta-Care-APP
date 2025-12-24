package com.example.lactacare.vistas.admin.lactarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.dominio.repository.ILactariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LactariosUiState(
    val isLoading: Boolean = false,
    val salas: List<SalaLactanciaDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class LactariosViewModel @Inject constructor(
    private val repository: ILactariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LactariosUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarSalas()
    }

    fun cargarSalas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.obtenerSalas()
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    salas = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar lactarios"
                )
            }
        }
    }
}
