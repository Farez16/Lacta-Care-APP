package com.example.lactacare.vistas.admin.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.UsuarioResponseDto
import com.example.lactacare.datos.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GestionUiState(
    val isLoading: Boolean = false,
    val listaDoctores: List<UsuarioResponseDto> = emptyList(),
    val listaAdministradores: List<UsuarioResponseDto> = emptyList(),
    val listaPacientes: List<UsuarioResponseDto> = emptyList(),
    val listaSalas: List<com.example.lactacare.datos.dto.SalaLactanciaDto> = emptyList(),
    val tabSeleccionado: Int = 0 // 0 = Admins, 1 = Doctores, 2 = Pacientes
)

@HiltViewModel
class GestionUsuariosViewModel @Inject constructor(
    private val repository: AdminRepository,
    private val lactariosRepository: com.example.lactacare.dominio.repository.ILactariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarListas()
        cargarSalas()
    }

    fun cargarListas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Cargamos listas en paralelo
            val admins = repository.obtenerListaAdministradores()
            val doctores = repository.obtenerListaDoctores()
            val pacientes = repository.obtenerListaPacientes()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                listaAdministradores = admins,
                listaDoctores = doctores,
                listaPacientes = pacientes
            )
        }
    }

    private fun cargarSalas() {
        viewModelScope.launch {
            val result = lactariosRepository.obtenerSalas()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(listaSalas = result.getOrDefault(emptyList()))
            }
        }
    }

    fun cambiarTab(index: Int) {
        _uiState.value = _uiState.value.copy(tabSeleccionado = index)
    }
    
    fun crearEmpleadoCompleto(
        datosPersonales: com.example.lactacare.datos.dto.CrearEmpleadoRequest,
        horario: com.example.lactacare.datos.dto.HorariosEmpleadoDto?,
        dias: com.example.lactacare.datos.dto.DiasLaborablesEmpleadoDto?,
        salaId: Int?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.crearEmpleadoConDetalles(datosPersonales, horario, dias, salaId)
            
            if (result.isSuccess) {
                cargarListas() // Recargar lista
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }


    fun eliminarUsuario(
        id: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.eliminarUsuario(id)
            if (result.isSuccess) {
                cargarListas()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError(result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }

    fun actualizarEmpleado(
        id: Int,
        datos: com.example.lactacare.datos.dto.CrearEmpleadoRequest,
        horario: com.example.lactacare.datos.dto.HorariosEmpleadoDto?,
        dias: com.example.lactacare.datos.dto.DiasLaborablesEmpleadoDto?,
        salaId: Int?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Mapeo Rápido de Rol String -> ID (1 Admin, 2 Doctor)
            val rolId = when(datos.rol) {
                "ADMINISTRADOR" -> 1
                "MEDICO", "DOCTOR" -> 2
                else -> null
            }

            val updateDto = com.example.lactacare.datos.dto.PersonaEmpleadoUpdateDTO(
                cedula = datos.cedula,
                primerNombre = datos.primerNombre,
                primerApellido = datos.primerApellido,
                segundoApellido = datos.segundoApellido,
                correo = datos.correo,
                telefono = datos.telefono,
                fechaNacimiento = datos.fechaNacimiento,
                rolId = rolId,
                salaLactanciaId = salaId
                // Horario y Dias se manejan en el repo generando nuevos IDs
            )

            val result = repository.actualizarEmpleadoCompleto(id, updateDto, horario, dias, salaId)
            
            if (result.isSuccess) {
                cargarListas()
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError(result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }
}