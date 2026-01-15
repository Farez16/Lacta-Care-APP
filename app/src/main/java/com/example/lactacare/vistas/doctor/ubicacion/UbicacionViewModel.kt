package com.example.lactacare.vistas.doctor.ubicacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.RefrigeradorDisponibleDto
import com.example.lactacare.datos.dto.CrearAtencionCompletaRequest
import com.example.lactacare.datos.dto.AtencionCompletaResponse
import com.example.lactacare.datos.local.SessionManager
import com.example.lactacare.dominio.repository.IDoctorRepository
import com.example.lactacare.vistas.doctor.atencion.ContenedorItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class ContenedorConUbicacion(
    val contenedor: ContenedorItem,
    val piso: Int? = null,
    val fila: Int? = null,
    val columna: Int? = null
) {
    fun tieneUbicacionCompleta(): Boolean = piso != null && fila != null && columna != null
}

data class UbicacionUiState(
    val isLoading: Boolean = false,
    val refrigeradores: List<RefrigeradorDisponibleDto> = emptyList(),
    val refrigeradorSeleccionado: RefrigeradorDisponibleDto? = null,
    val contenedores: List<ContenedorConUbicacion> = emptyList(),
    val contenedorActualIndex: Int = 0,
    val error: String? = null,
    val atencionGuardada: AtencionCompletaResponse? = null
)

@HiltViewModel
class UbicacionViewModel @Inject constructor(
    private val repository: IDoctorRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UbicacionUiState())
    val uiState = _uiState.asStateFlow()

    fun inicializar(idReserva: Long, contenedores: List<ContenedorItem>, idSala: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                contenedores = contenedores.map { ContenedorConUbicacion(it) }
            )

            // Cargar refrigeradores de la sala
            val result = repository.obtenerRefrigeradoresPorSala(idSala)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    refrigeradores = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al cargar refrigeradores"
                )
            }
        }
    }

    fun seleccionarRefrigerador(refrigerador: RefrigeradorDisponibleDto) {
        _uiState.value = _uiState.value.copy(
            refrigeradorSeleccionado = refrigerador
        )
    }

    fun asignarUbicacion(piso: Int, fila: Int, columna: Int) {
        val contenedoresActualizados = _uiState.value.contenedores.toMutableList()
        val index = _uiState.value.contenedorActualIndex
        
        if (index < contenedoresActualizados.size) {
            contenedoresActualizados[index] = contenedoresActualizados[index].copy(
                piso = piso,
                fila = fila,
                columna = columna
            )
            
            _uiState.value = _uiState.value.copy(
                contenedores = contenedoresActualizados
            )
        }
    }

    fun siguienteContenedor() {
        val nuevoIndex = _uiState.value.contenedorActualIndex + 1
        if (nuevoIndex < _uiState.value.contenedores.size) {
            _uiState.value = _uiState.value.copy(
                contenedorActualIndex = nuevoIndex
            )
        }
    }

    fun anteriorContenedor() {
        val nuevoIndex = _uiState.value.contenedorActualIndex - 1
        if (nuevoIndex >= 0) {
            _uiState.value = _uiState.value.copy(
                contenedorActualIndex = nuevoIndex
            )
        }
    }

    fun guardarAtencionCompleta(idReserva: Long) {
        viewModelScope.launch {
            // Validar que todos los contenedores tengan ubicación
            if (!todosContenedoresTienenUbicacion()) {
                _uiState.value = _uiState.value.copy(
                    error = "Todos los contenedores deben tener una ubicación asignada"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Obtener ID del empleado
            val empleadoId = sessionManager.userId.first()?.toLong() ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo identificar al doctor"
                )
                return@launch
            }

            // Crear request
            val contenedoresDto = _uiState.value.contenedores.map { contenedor ->
                CrearAtencionCompletaRequest.ContenedorDto(
                    cantidadMl = contenedor.contenedor.cantidadMl,
                    idRefrigerador = _uiState.value.refrigeradorSeleccionado!!.id,
                    piso = contenedor.piso!!,
                    fila = contenedor.fila!!,
                    columna = contenedor.columna!!
                )
            }

            val request = CrearAtencionCompletaRequest(
                idReserva = idReserva,
                idEmpleado = empleadoId,
                contenedores = contenedoresDto
            )

            val result = repository.crearAtencionCompleta(request)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    atencionGuardada = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error al guardar atención"
                )
            }
        }
    }

    private fun todosContenedoresTienenUbicacion(): Boolean {
        return _uiState.value.contenedores.all { it.tieneUbicacionCompleta() }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
