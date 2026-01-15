package com.example.lactacare.vistas.doctor.reservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.DoctorReservaDto
import com.example.lactacare.dominio.repository.IDoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ListaReservasUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val reservas: List<DoctorReservaDto> = emptyList(),
    val error: String? = null,
    val mensajeExito: String? = null
)

@HiltViewModel
class ListaReservasViewModel @Inject constructor(
    private val repository: IDoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaReservasUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarReservasPendientes()
    }

    fun cargarReservasPendientes(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            val hoy = LocalDate.now().toString()
            val result = repository.obtenerAgendaDelDia(hoy)

            if (result.isSuccess) {
                val todasReservas = result.getOrDefault(emptyList())
                // Filtrar solo las que están EN RESERVA
                val reservasPendientes = todasReservas.filter { it.estado == "EN RESERVA" }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    reservas = reservasPendientes
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar reservas"
                )
            }
        }
    }

    fun cancelarReserva(reserva: DoctorReservaDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val reservaActualizada = reserva.copy(estado = "CANCELADO")
            val result = repository.actualizarReserva(reserva.id, reservaActualizada)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mensajeExito = "Reserva cancelada exitosamente"
                )
                // Recargar lista
                cargarReservasPendientes()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cancelar reserva"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(error = null, mensajeExito = null)
    }
}
