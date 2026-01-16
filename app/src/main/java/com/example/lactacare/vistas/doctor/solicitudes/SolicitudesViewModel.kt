package com.example.lactacare.vistas.doctor.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.SolicitudRetiroDto
import com.example.lactacare.datos.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SolicitudesUiState(
    val solicitudes: List<SolicitudRetiroDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mostrarDialogConfirmar: Boolean = false,
    val solicitudSeleccionada: SolicitudRetiroDto? = null,
    val mensajeExito: String? = null
)

@HiltViewModel
class SolicitudesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudesUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarSolicitudes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val solicitudes = apiService.obtenerSolicitudesRetiro()
                _uiState.value = _uiState.value.copy(
                    solicitudes = solicitudes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar solicitudes: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun mostrarDialogConfirmar(solicitud: SolicitudRetiroDto) {
        _uiState.value = _uiState.value.copy(
            mostrarDialogConfirmar = true,
            solicitudSeleccionada = solicitud
        )
    }

    fun cancelarConfirmacion() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogConfirmar = false,
            solicitudSeleccionada = null
        )
    }

    fun confirmarRetiro() {
        val solicitud = _uiState.value.solicitudSeleccionada ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val response = apiService.marcarComoRetirada(solicitud.idContenedor)
                
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        mostrarDialogConfirmar = false,
                        solicitudSeleccionada = null,
                        isLoading = false,
                        mensajeExito = "Contenedor marcado como retirado"
                    )
                    cargarSolicitudes() // Recargar lista
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error: ${response.message()}",
                        isLoading = false,
                        mostrarDialogConfirmar = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al marcar retiro: ${e.message}",
                    isLoading = false,
                    mostrarDialogConfirmar = false
                )
            }
        }
    }

    fun limpiarMensajeExito() {
        _uiState.value = _uiState.value.copy(mensajeExito = null)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
