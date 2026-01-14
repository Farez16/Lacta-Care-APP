package com.example.lactacare.vistas.admin.ia

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.DocumentoDto
import com.example.lactacare.dominio.repository.IIARepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class IAUiState(
    val isLoading: Boolean = false,
    val documentos: List<DocumentoDto> = emptyList(),
    val error: String? = null,
    val mensaje: String? = null
)

@HiltViewModel
class IAViewModel @Inject constructor(
    private val repository: IIARepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(IAUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDocumentos()
    }

    fun cargarDocumentos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.listarDocumentos()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    documentos = result.getOrDefault(emptyList())
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun subirDocumento(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, mensaje = null)
            
            // Convertir Uri a File temporal
            val file = uriToFile(uri)
            if (file != null) {
                val result = repository.subirDocumento(file)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mensaje = "Documento subido correctamente"
                    )
                    cargarDocumentos() // Recargar lista
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al subir: ${result.exceptionOrNull()?.message}"
                    )
                }
                // Limpiar temp file
                 try { file.delete() } catch(e: Exception) {}
            } else {
                 _uiState.value = _uiState.value.copy(isLoading = false, error = "No se pudo procesar el archivo")
            }
        }
    }

    fun eliminarDocumento(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.eliminarDocumento(id)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    mensaje = "Documento eliminado"
                )
                cargarDocumentos()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al eliminar: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null, error = null)
    }

    // Helper para Uri -> File
    private fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".pdf", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
