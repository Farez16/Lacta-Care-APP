package com.example.lactacare.vistas.doctor.atencion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.DoctorReservaDto
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.dominio.repository.AuthRepository
import com.example.lactacare.dominio.repository.IDoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class ContenedorItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cantidadMl: BigDecimal
)

data class AtencionUiState(
    val isLoading: Boolean = false,
    val reserva: DoctorReservaDto? = null,
    val nombreDoctor: String = "",
    val contenedores: List<ContenedorItem> = emptyList(),
    val cantidadActual: String = "",
    val totalMl: BigDecimal = BigDecimal.ZERO,
    val error: String? = null,
    val errorCantidad: String? = null
)

@HiltViewModel
class AtencionViewModel @Inject constructor(
    private val repository: IDoctorRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AtencionUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarDatosIniciales(idReserva: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Cargar nombre del doctor
            val email = sessionManager.userEmail.first()
            if (!email.isNullOrEmpty()) {
                val result = authRepository.getEmpleadoData(email)
                if (result.isSuccess) {
                    val empleado = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        nombreDoctor = "${empleado?.primerNombre ?: ""} ${empleado?.primerApellido ?: ""}".trim()
                    )
                }
            }

            // Cargar datos de la reserva
            val hoy = java.time.LocalDate.now().toString()
            val agendaResult = repository.obtenerAgendaDelDia(hoy)
            
            if (agendaResult.isSuccess) {
                val reserva = agendaResult.getOrDefault(emptyList())
                    .find { it.id == idReserva }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reserva = reserva
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos de la reserva"
                )
            }
        }
    }

    fun actualizarCantidad(cantidad: String) {
        _uiState.value = _uiState.value.copy(
            cantidadActual = cantidad,
            errorCantidad = null
        )
    }

    fun agregarContenedor() {
        val cantidad = _uiState.value.cantidadActual.trim()
        
        // Validar que no esté vacío
        if (cantidad.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorCantidad = "Ingrese una cantidad"
            )
            return
        }

        // Validar que sea un número válido
        val cantidadBigDecimal = try {
            BigDecimal(cantidad)
        } catch (e: NumberFormatException) {
            _uiState.value = _uiState.value.copy(
                errorCantidad = "Ingrese un número válido"
            )
            return
        }

        // Validar que sea mayor a 0
        if (cantidadBigDecimal <= BigDecimal.ZERO) {
            _uiState.value = _uiState.value.copy(
                errorCantidad = "La cantidad debe ser mayor a 0"
            )
            return
        }

        // Validar que no exceda un límite razonable (ej: 500ml)
        if (cantidadBigDecimal > BigDecimal(500)) {
            _uiState.value = _uiState.value.copy(
                errorCantidad = "La cantidad no puede exceder 500 ml"
            )
            return
        }

        // Agregar contenedor
        val nuevoContenedor = ContenedorItem(cantidadMl = cantidadBigDecimal)
        val nuevaLista = _uiState.value.contenedores + nuevoContenedor
        val nuevoTotal = nuevaLista.sumOf { it.cantidadMl }

        _uiState.value = _uiState.value.copy(
            contenedores = nuevaLista,
            cantidadActual = "",
            totalMl = nuevoTotal,
            errorCantidad = null
        )
    }

    fun eliminarContenedor(contenedorId: String) {
        val nuevaLista = _uiState.value.contenedores.filter { it.id != contenedorId }
        val nuevoTotal = nuevaLista.sumOf { it.cantidadMl }

        _uiState.value = _uiState.value.copy(
            contenedores = nuevaLista,
            totalMl = nuevoTotal
        )
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
