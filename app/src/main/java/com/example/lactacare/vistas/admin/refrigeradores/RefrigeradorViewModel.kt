package com.example.lactacare.vistas.admin.refrigeradores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.RefrigeradorDto
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.dominio.repository.ILactariosRepository
import com.example.lactacare.dominio.repository.IRefrigeradorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RefrigeradorUiState(
    val isLoading: Boolean = false,
    val refrigeradores: List<RefrigeradorDto> = emptyList(),
    val salas: List<SalaLactanciaDto> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null
)

@HiltViewModel
class RefrigeradorViewModel @Inject constructor(
    private val repository: IRefrigeradorRepository,
    private val lactariosRepository: ILactariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RefrigeradorUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Cargar Refrigeradores
            val refriResult = repository.obtenerRefrigeradores()
            // Cargar Sales (para el dropdown)
            val salasResult = lactariosRepository.obtenerSalas()

            if (refriResult.isSuccess && salasResult.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    refrigeradores = refriResult.getOrDefault(emptyList()),
                    salas = salasResult.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando datos: ${refriResult.exceptionOrNull()?.message ?: ""} ${salasResult.exceptionOrNull()?.message ?: ""}"
                )
            }
        }
    }

    fun crearRefrigerador(refri: RefrigeradorDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.crearRefrigerador(refri)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(mensaje = "Refrigerador creado")
                cargarDatos()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun actualizarRefrigerador(id: Long, refri: RefrigeradorDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.editarRefrigerador(id, refri)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(mensaje = "Refrigerador actualizado")
                cargarDatos()
            } else {
                 _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun eliminarRefrigerador(id: Long) {
         viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.eliminarRefrigerador(id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(mensaje = "Refrigerador eliminado")
                cargarDatos()
            } else {
                 _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
    
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null, error = null)
    }
}
