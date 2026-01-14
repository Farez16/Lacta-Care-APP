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
    TODO, CADUCADA, REFRIGERADA, CONGELADA, RETIRADA
}

data class InventarioUiState(
    val contenedores: List<ContenedorLecheDto> = emptyList(),
    val contenedoresFiltrados: List<ContenedorLecheDto> = emptyList(),
    val filtroActual: FiltroInventario = FiltroInventario.TODO,
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
            FiltroInventario.TODO -> _uiState.value.contenedores
            
            FiltroInventario.CADUCADA -> _uiState.value.contenedores.filter { contenedor ->
                try {
                    val fechaCaducidad = LocalDateTime.parse(
                        contenedor.fechaHoraCaducidad,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )
                    fechaCaducidad.isBefore(LocalDateTime.now())
                } catch (e: Exception) {
                    false
                }
            }
            
            FiltroInventario.REFRIGERADA -> _uiState.value.contenedores.filter {
                it.estado == "Refrigerada"
            }
            
            FiltroInventario.CONGELADA -> _uiState.value.contenedores.filter {
                it.estado == "Congelada"
            }
            
            FiltroInventario.RETIRADA -> _uiState.value.contenedores.filter {
                it.estado == "Retirada"
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

    fun confirmarRetiro(idPaciente: Long) {
        val contenedor = _uiState.value.contenedorSeleccionado ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val response = apiService.retirarContenedor(contenedor.id)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            mostrarDialogRetirar = false,
                            contenedorSeleccionado = null,
                            isLoading = false,
                            mensajeExito = "Contenedor retirado exitosamente"
                        )
                    }
                    
                    // Recargar inventario
                    cargarInventario(idPaciente)
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Error al retirar: ${response.message()}",
                            isLoading = false,
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
