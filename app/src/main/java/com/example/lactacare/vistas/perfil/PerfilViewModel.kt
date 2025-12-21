package com.example.lactacare.vistas.perfil

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.repository.AuthRepositoryImpl
import com.example.lactacare.dominio.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

data class PerfilUiState(
    val isLoading: Boolean = false,
    val nombreCompleto: String = "",
    val correo: String = "",
    val imagenPerfil: String? = null,
    val detalles: Map<String, String> = emptyMap(),
    val error: String? = null,
    val modoEdicion: Boolean = false,
    val guardandoCambios: Boolean = false,
    val cambiosGuardados: Boolean = false
)

class PerfilViewModel(
    private val authRepository: AuthRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState(isLoading = true))
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // Campos editables
    private val _telefono = MutableStateFlow("")
    val telefono = _telefono.asStateFlow()

    private val _nuevaImagenUri = MutableStateFlow<Uri?>(null)
    val nuevaImagenUri = _nuevaImagenUri.asStateFlow()

    /**
     * Carga el perfil del usuario desde la sesión actual
     */
    fun cargarPerfil(rol: RolUsuario) {
        viewModelScope.launch {
            _uiState.value = PerfilUiState(isLoading = true)

            try {
                // Obtener sesión actual
                val session = authRepository.getCurrentSession()

                if (session == null) {
                    _uiState.value = PerfilUiState(
                        error = "No hay sesión activa"
                    )
                    return@launch
                }

                // Obtener datos del usuario según el rol
                when (session.rol) {
                    RolUsuario.PACIENTE -> {
                        val paciente = authRepository.obtenerPacientePorId(session.userId.toInt())
                        if (paciente != null) {
                            _telefono.value = paciente.telefono

                            _uiState.value = PerfilUiState(
                                nombreCompleto = "${paciente.primerNombre} ${paciente.primerApellido}",
                                correo = paciente.correo,
                                imagenPerfil = paciente.fotoPerfil.takeIf { it.isNotEmpty() },
                                detalles = buildMap {
                                    put("Cédula", paciente.cedula)
                                    if (paciente.telefono.isNotEmpty()) {
                                        put("Teléfono", paciente.telefono)
                                    }
                                    if (paciente.fechaNacimiento.isNotEmpty()) {
                                        put("Fecha Nacimiento", paciente.fechaNacimiento)
                                    }
                                    if (paciente.discapacidad?.isNotEmpty() == true) {
                                        put("Discapacidad", paciente.discapacidad)
                                    }
                                }
                            )
                        } else {
                            _uiState.value = PerfilUiState(
                                error = "No se pudo cargar el perfil del paciente"
                            )
                        }
                    }

                    RolUsuario.DOCTOR -> {
                        val medico = authRepository.obtenerMedicoPorId(session.userId.toInt())
                        if (medico != null) {
                            _telefono.value = medico.telefono

                            _uiState.value = PerfilUiState(
                                nombreCompleto = "Dr. ${medico.primerNombre} ${medico.primerApellido}",
                                correo = medico.correo,
                                imagenPerfil = medico.fotoPerfil.takeIf { it.isNotEmpty() },
                                detalles = buildMap {
                                    if (medico.licenciaMedica.isNotEmpty()) {
                                        put("Licencia Médica", medico.licenciaMedica)
                                    }
                                    put("Cédula", medico.cedula)
                                    if (medico.telefono.isNotEmpty()) {
                                        put("Teléfono", medico.telefono)
                                    }
                                    if (medico.fechaNacimiento.isNotEmpty()) {
                                        put("Fecha Nacimiento", medico.fechaNacimiento)
                                    }
                                }
                            )
                        } else {
                            _uiState.value = PerfilUiState(
                                error = "No se pudo cargar el perfil del doctor"
                            )
                        }
                    }

                    RolUsuario.ADMINISTRADOR -> {
                        val admin = authRepository.obtenerAdminPorId(session.userId.toInt())
                        if (admin != null) {
                            _telefono.value = admin.telefono

                            _uiState.value = PerfilUiState(
                                nombreCompleto = "${admin.primerNombre} ${admin.primerApellido}",
                                correo = admin.correo,
                                imagenPerfil = admin.fotoPerfil.takeIf { it.isNotEmpty() },
                                detalles = buildMap {
                                    if (admin.codigoEmpleado.isNotEmpty()) {
                                        put("Código Empleado", admin.codigoEmpleado)
                                    }
                                    put("Cédula", admin.cedula)
                                    if (admin.telefono.isNotEmpty()) {
                                        put("Teléfono", admin.telefono)
                                    }
                                    if (admin.fechaNacimiento.isNotEmpty()) {
                                        put("Fecha Nacimiento", admin.fechaNacimiento)
                                    }
                                }
                            )
                        } else {
                            _uiState.value = PerfilUiState(
                                error = "No se pudo cargar el perfil del administrador"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PerfilUiState(
                    error = "Error al cargar perfil: ${e.message}"
                )
            }
        }
    }

    /**
     * Activa el modo edición
     */
    fun activarEdicion() {
        _uiState.value = _uiState.value.copy(modoEdicion = true, cambiosGuardados = false)
    }

    /**
     * Cancela el modo edición
     */
    fun cancelarEdicion() {
        _uiState.value = _uiState.value.copy(modoEdicion = false)
        // Recargar datos originales
        val session = authRepository.getCurrentSession()
        session?.let { cargarPerfil(it.rol) }
    }

    /**
     * Actualiza el teléfono
     */
    fun onTelefonoChange(nuevoTelefono: String) {
        _telefono.value = nuevoTelefono
    }

    /**
     * Actualiza la imagen de perfil
     */
    fun onImagenSeleccionada(uri: Uri?) {
        _nuevaImagenUri.value = uri
    }

    /**
     * Guarda los cambios del perfil
     */
    fun guardarCambios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardandoCambios = true)

            try {
                // TODO: Implementar llamada al backend para actualizar perfil
                // Por ahora simulamos éxito
                kotlinx.coroutines.delay(1500)

                _uiState.value = _uiState.value.copy(
                    guardandoCambios = false,
                    modoEdicion = false,
                    cambiosGuardados = true
                )

                // Recargar perfil
                val session = authRepository.getCurrentSession()
                session?.let { cargarPerfil(it.rol) }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardandoCambios = false,
                    error = "Error al guardar cambios: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpia el mensaje de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.value = _uiState.value.copy(cambiosGuardados = false)
    }

    /**
     * Factory para crear el ViewModel con dependencias
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
                return PerfilViewModel(
                    AuthRepositoryImpl(context)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}