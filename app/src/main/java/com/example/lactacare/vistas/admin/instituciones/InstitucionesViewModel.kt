package com.example.lactacare.vistas.admin.instituciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.Institucion
import com.example.lactacare.dominio.repository.IInstitucionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstitucionesUiState(
    val isLoading: Boolean = false,
    val instituciones: List<Institucion> = emptyList(),
    val error: String? = null,
    val mensajeExito: String? = null
)

@HiltViewModel
class InstitucionesViewModel @Inject constructor(
    private val repository: IInstitucionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstitucionesUiState())
    val uiState: StateFlow<InstitucionesUiState> = _uiState.asStateFlow()

    init {
        cargarInstituciones()
    }

    fun cargarInstituciones() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.obtenerInstituciones()
            result.onSuccess { lista ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    instituciones = lista,
                    error = null
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido al cargar instituciones"
                )
            }
        }
    }

    fun crearInstitucion(nombre: String, logoBase64: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, mensajeExito = null)
            val nueva = Institucion(nombreInstitucion = nombre, logoInstitucion = logoBase64)
            val result = repository.crearInstitucion(nueva)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mensajeExito = "Institución creada correctamente"
                )
                cargarInstituciones()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al crear institución"
                )
            }
        }
    }

    fun actualizarInstitucion(id: Long, nombre: String, logoBase64: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, mensajeExito = null)
            val editada = Institucion(idInstitucion = id, nombreInstitucion = nombre, logoInstitucion = logoBase64)
            val result = repository.editarInstitucion(id, editada)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mensajeExito = "Institución actualizada correctamente"
                )
                cargarInstituciones()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar institución"
                )
            }
        }
    }

    fun eliminarInstitucion(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, mensajeExito = null)
            val result = repository.eliminarInstitucion(id)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mensajeExito = "Institución eliminada"
                )
                cargarInstituciones()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al eliminar institución"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(error = null, mensajeExito = null)
    }
}
