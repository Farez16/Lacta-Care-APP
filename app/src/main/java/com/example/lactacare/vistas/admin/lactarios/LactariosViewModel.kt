package com.example.lactacare.vistas.admin.lactarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.SalaLactanciaConCubiculosDTO
import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.dominio.model.Institucion
import com.example.lactacare.dominio.repository.IInstitucionRepository
import com.example.lactacare.dominio.repository.ILactariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LactariosUiState(
    val isLoading: Boolean = false,
    val salas: List<SalaLactanciaDto> = emptyList(),
    val instituciones: List<Institucion> = emptyList(), // Para el dropdown
    val error: String? = null,
    val mensajeExito: String? = null
)

@HiltViewModel
class LactariosViewModel @Inject constructor(
    private val repository: ILactariosRepository,
    private val institucionRepository: IInstitucionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LactariosUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarSalas()
        cargarInstituciones()
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
    
    fun cargarInstituciones() {
        viewModelScope.launch {
            // No bloqueamos loading principal, carga en paralelo
            val result = institucionRepository.obtenerInstituciones()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    instituciones = result.getOrDefault(emptyList())
                )
            }
        }
    }

    fun crearLactario(sala: SalaLactanciaDto, numeroCubiculos: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, mensajeExito = null)
            
            val dto = SalaLactanciaConCubiculosDTO(
                salaLactancia = sala,
                numeroCubiculos = numeroCubiculos
            )
            
            val result = repository.crearSalaConCubiculos(dto)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mensajeExito = "Sala creada correctamente"
                )
                cargarSalas()
            } else {
               manejarError(result.exceptionOrNull())
            }
        }
    }

    fun editarLactario(id: Long, sala: SalaLactanciaDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, mensajeExito = null)
            val result = repository.editarSala(id, sala)
            if (result.isSuccess) {
                 _uiState.value = _uiState.value.copy(isLoading = false, mensajeExito = "Sala actualizada")
                cargarSalas()
            } else {
                manejarError(result.exceptionOrNull())
            }
        }
    }

    fun eliminarLactario(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.eliminarSala(id)
            if (result.isSuccess) {
                 _uiState.value = _uiState.value.copy(isLoading = false, mensajeExito = "Sala eliminada")
                cargarSalas() 
            } else { 
                manejarError(result.exceptionOrNull()) 
            }
        }
    }

    private fun manejarError(e: Throwable?) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = e?.message ?: "Ocurrió un error")
    }
    
    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(error = null, mensajeExito = null)
    }
}
