package com.example.lactacare.vistas.admin.ia

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.DocumentoDto
import com.example.lactacare.datos.repository.DocumentosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GestionIAUiState(
    val isLoading: Boolean = false,
    val documentos: List<DocumentoDto> = emptyList(),
    val mensajeUsuario: String? = null
)

@HiltViewModel
class GestionIAViewModel @Inject constructor(
    private val repository: DocumentosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionIAUiState())
    val uiState = _uiState.asStateFlow()

    init {
        listarDocumentos()
    }

    fun listarDocumentos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val lista = repository.listarDocumentos()
            _uiState.value = _uiState.value.copy(isLoading = false, documentos = lista)
        }
    }

    fun subirDocumento(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, mensajeUsuario = null)
            val resultado = repository.subirDocumento(uri)
            
            if (resultado.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensajeUsuario = "Documento subido correctamente")
                listarDocumentos() // Refrescar lista
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, mensajeUsuario = "Error: ${resultado.exceptionOrNull()?.message}")
            }
        }
    }

    fun eliminarDocumento(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val exito = repository.eliminarDocumento(id)
            if (exito) {
                listarDocumentos()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, mensajeUsuario = "No se pudo eliminar el documento")
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensajeUsuario = null)
    }
}
