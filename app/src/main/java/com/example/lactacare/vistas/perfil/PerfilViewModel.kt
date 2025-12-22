package com.example.lactacare.vistas.perfil

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilUiState(
    val isLoading: Boolean = false,
    val primerNombre: String = "",
    val apellido: String = "", // Para mostrarlo, aunque no se edite
    val imagenPerfil: String? = null,
    val detalles: Map<String, String> = emptyMap(),
    val error: String? = null,
    val modoEdicion: Boolean = false,
    val guardandoCambios: Boolean = false,
    val cambiosGuardados: Boolean = false
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState(isLoading = true))
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // Variable temporal para editar el nombre
    private val _nombreEdit = MutableStateFlow("")
    val nombreEdit = _nombreEdit.asStateFlow()

    // Variable temporal para la nueva imagen
    private val _nuevaImagenUri = MutableStateFlow<Uri?>(null)
    val nuevaImagenUri = _nuevaImagenUri.asStateFlow()

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val resultado = authRepository.getUserProfile()

            resultado.onSuccess { perfil ->
                // Inicializamos el campo de edición con el nombre actual
                _nombreEdit.value = perfil.primerNombre

                _uiState.value = PerfilUiState(
                    isLoading = false,
                    primerNombre = perfil.primerNombre,
                    apellido = perfil.apellido,
                    imagenPerfil = perfil.imagen,
                    // Aquí definimos la lista fija de detalles para TODOS
                    detalles = mapOf(
                        "Cédula" to perfil.cedula,
                        "Fecha Nacimiento" to perfil.fechaNacimiento,
                        "Rol" to perfil.rol
                    )
                )
            }.onFailure { e ->
                _uiState.value = PerfilUiState(isLoading = false, error = "Error: ${e.message}")
            }
        }
    }

    fun activarEdicion() {
        // Al activar edición, aseguramos que el texto editable tenga el valor actual
        _nombreEdit.value = _uiState.value.primerNombre
        _uiState.value = _uiState.value.copy(modoEdicion = true, cambiosGuardados = false)
    }

    fun cancelarEdicion() {
        _uiState.value = _uiState.value.copy(modoEdicion = false)
        _nuevaImagenUri.value = null
        _nombreEdit.value = _uiState.value.primerNombre // Restaurar nombre original
    }

    fun onNombreChange(nuevoNombre: String) {
        _nombreEdit.value = nuevoNombre
    }

    fun onImagenSeleccionada(uri: Uri?) {
        _nuevaImagenUri.value = uri
    }

    fun guardarCambios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardandoCambios = true)

            // Convertir imagen a Base64 si existe nueva
            val imagenBase64 = _nuevaImagenUri.value?.let { uriToBase64(context, it) }

            // Enviamos al backend solo lo que se permite editar: Nombre e Imagen
            val resultado = authRepository.updateUserProfile(
                nombre = _nombreEdit.value,
                imagenBase64 = imagenBase64
            )

            resultado.onSuccess {
                _uiState.value = _uiState.value.copy(
                    guardandoCambios = false,
                    modoEdicion = false,
                    cambiosGuardados = true
                )
                _nuevaImagenUri.value = null
                // Recargamos para ver los cambios confirmados por el servidor
                cargarPerfil()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    guardandoCambios = false,
                    error = "No se pudo guardar: ${e.message}"
                )
            }
        }
    }

    fun limpiarMensajeExito() {
        _uiState.value = _uiState.value.copy(cambiosGuardados = false)
    }

    private fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        } catch (e: Exception) {
            null
        }
    }
}