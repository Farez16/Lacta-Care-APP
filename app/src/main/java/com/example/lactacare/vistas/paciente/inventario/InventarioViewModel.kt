package com.example.lactacare.vistas.paciente.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.datos.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class FiltroInventario {
    TODOS, REFRIGERADA, CADUCADA, RETIRADA
}

data class InventarioUiState(
    val contenedores: List<ContenedorLecheDto> = emptyList(),
    val contenedoresFiltrados: List<ContenedorLecheDto> = emptyList(),
    val filtroActual: FiltroInventario = FiltroInventario.TODOS,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mostrarDialogRetirar: Boolean = false,
    val contenedorSeleccionado: ContenedorLecheDto? = null,
    val mensajeExito: String? = null
)

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    fun cargarInventario(idPaciente: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val contenedores = apiService.obtenerInventarioPaciente(idPaciente)
                
                _uiState.update {
                    it.copy(
                        contenedores = contenedores,
                        contenedoresFiltrados = contenedores,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al cargar inventario: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun aplicarFiltro(filtro: FiltroInventario) {
        val contenedoresFiltrados = when (filtro) {
            FiltroInventario.TODOS -> _uiState.value.contenedores
            
            FiltroInventario.REFRIGERADA -> _uiState.value.contenedores.filter {
                it.estado.equals("Refrigerada", ignoreCase = true)
            }
            
            FiltroInventario.CADUCADA -> _uiState.value.contenedores.filter { contenedor ->
                try {
                    contenedor.fechaCaducidad?.let { fechaStr ->
                        val fechaCaducidad = LocalDateTime.parse(
                            fechaStr,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        )
                        fechaCaducidad.isBefore(LocalDateTime.now())
                    } ?: false
                } catch (e: Exception) {
                    false
                }
            }
            
            FiltroInventario.RETIRADA -> _uiState.value.contenedores.filter {
                it.estado.equals("Retirada", ignoreCase = true)
            }
        }

        _uiState.update {
            it.copy(
                filtroActual = filtro,
                contenedoresFiltrados = contenedoresFiltrados
            )
        }
    }

    fun mostrarDialogRetirar(contenedor: ContenedorLecheDto) {
        _uiState.update {
            it.copy(
                mostrarDialogRetirar = true,
                contenedorSeleccionado = contenedor
            )
        }
    }

    fun cancelarRetiro() {
        _uiState.update {
            it.copy(
                mostrarDialogRetirar = false,
                contenedorSeleccionado = null
            )
        }
    }

    fun solicitarRetiro(contenedor: ContenedorLecheDto, idPaciente: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensajeExito = null) }
            try {
                val response = apiService.solicitarRetiroContenedor(contenedor.id)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mensajeExito = "Solicitud de retiro enviada correctamente",
                            mostrarDialogRetirar = false,
                            contenedorSeleccionado = null
                        )
                    }
                    // Recargar inventario para reflejar el cambio de estado
                    cargarInventario(idPaciente)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al solicitar retiro: ${response.code()}",
                            mostrarDialogRetirar = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al retirar contenedor: ${e.message}",
                        isLoading = false,
                        mostrarDialogRetirar = false
                    )
                }
            }
        }
    }

    fun limpiarMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}
