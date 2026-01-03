package com.example.lactacare.vistas.admin.refrigeradores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.RefrigeradorDto
import com.example.lactacare.datos.repository.RefrigeradoresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RefrigeradoresUiState(
    val isLoading: Boolean = false,
    val refrigeradores: List<RefrigeradorDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RefrigeradoresViewModel @Inject constructor(
    private val repository: RefrigeradoresRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RefrigeradoresUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarRefrigeradores()
    }

    fun cargarRefrigeradores() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.obtenerRefrigeradores()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, refrigeradores = result.getOrDefault(emptyList()))
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun crear(refri: RefrigeradorDto) {
        viewModelScope.launch {
            val result = repository.crearRefrigerador(refri)
            if (result.isSuccess) cargarRefrigeradores() else manejarError(result.exceptionOrNull())
        }
    }

    fun editar(refri: RefrigeradorDto) {
        viewModelScope.launch {
            val result = repository.editarRefrigerador(refri.id, refri)
            if (result.isSuccess) cargarRefrigeradores() else manejarError(result.exceptionOrNull())
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            val result = repository.eliminarRefrigerador(id)
            if (result.isSuccess) cargarRefrigeradores() else manejarError(result.exceptionOrNull())
        }
    }

    private fun manejarError(e: Throwable?) {
        _uiState.value = _uiState.value.copy(error = e?.message ?: "Error desconocido")
    }
}
