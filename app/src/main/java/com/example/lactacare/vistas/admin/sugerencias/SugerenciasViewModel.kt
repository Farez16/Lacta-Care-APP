package com.example.lactacare.vistas.admin.sugerencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.SugerenciaDto
import com.example.lactacare.datos.repository.SugerenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SugerenciasUiState(
    val isLoading: Boolean = false,
    val sugerencias: List<SugerenciaDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SugerenciasViewModel @Inject constructor(
    private val repository: SugerenciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SugerenciasUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarSugerencias()
    }

    fun cargarSugerencias() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.obtenerSugerencias()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, sugerencias = result.getOrDefault(emptyList()))
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun crear(sugerencia: SugerenciaDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.crearSugerencia(sugerencia)
            if (result.isSuccess) {
                cargarSugerencias()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.eliminarSugerencia(id)
            if (result.isSuccess) {
                cargarSugerencias()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}
