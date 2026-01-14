package com.example.lactacare.vistas.admin.usuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.datos.dto.*
import com.example.lactacare.datos.repository.AdminRepository
import com.example.lactacare.dominio.repository.ILactariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrearEmpleadoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    
    // --- PASO 1: DATOS PERSONALES ---
    val cedula: String = "",
    val primerNombre: String = "",
    val segundoNombre: String = "",
    val primerApellido: String = "",
    val segundoApellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",

    // --- PASO 2: ROL Y SALA ---
    val roles: List<RolDto> = emptyList(),
    val idRolSeleccionado: Int? = null,
    val salas: List<SalaLactanciaDto> = emptyList(),
    val idSalaSeleccionada: Int? = null, // Solo para doctores/admins de sala

    // --- PASO 3: HORARIO ---
    val horaInicio: String = "08:00",
    val horaFin: String = "17:00",
    // DÃ­as Laborables
    val lunes: Boolean = true,
    val martes: Boolean = true,
    val miercoles: Boolean = true,
    val jueves: Boolean = true,
    val viernes: Boolean = true,
    val sabado: Boolean = false,
    val domingo: Boolean = false
)

@HiltViewModel
class CrearEmpleadoViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val lactariosRepository: ILactariosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearEmpleadoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Cargar Salas
            val salasResult = lactariosRepository.obtenerSalas()
            // Roles (Simulados o endpoint pendiente, por ahora hardcoded si no hay endpoint)
            // Asumiremos lista estÃ¡tica por ahora si no hay endpoint claro, pero adminRepository deberÃ­a tenerlo
            // En ApiService agreguÃ© "obtenerRoles", pero no Repository. 
            // Para no bloquear, usarÃ© constantes, o implementarÃ© despuÃ©s.
            // USARÃ© constantes por ahora para simplificar la UI: 1=ADMIN, 2=MEDICO, etc.
            val rolesFake = listOf(
                RolDto(2, "MEDICO", "MÃ©dico del sistema"),
                RolDto(1, "ADMINISTRADOR", "Administrador del sistema")
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                salas = salasResult.getOrDefault(emptyList()),
                roles = rolesFake
            )
        }
    }

    fun onPersonalDataChange(
        cedula: String? = null, primerNombre: String? = null, segundoNombre: String? = null,
        primerApellido: String? = null, segundoApellido: String? = null,
        correo: String? = null, telefono: String? = null, fechaNacimiento: String? = null
    ) {
        _uiState.value = _uiState.value.copy(
            cedula = cedula ?: _uiState.value.cedula,
            primerNombre = primerNombre ?: _uiState.value.primerNombre,
            segundoNombre = segundoNombre ?: _uiState.value.segundoNombre,
            primerApellido = primerApellido ?: _uiState.value.primerApellido,
            segundoApellido = segundoApellido ?: _uiState.value.segundoApellido,
            correo = correo ?: _uiState.value.correo,
            telefono = telefono ?: _uiState.value.telefono,
            fechaNacimiento = fechaNacimiento ?: _uiState.value.fechaNacimiento
        )
    }

    fun onRolSalaChange(idRol: Int? = null, idSala: Int? = null) {
        _uiState.value = _uiState.value.copy(
            idRolSeleccionado = idRol ?: _uiState.value.idRolSeleccionado,
            idSalaSeleccionada = idSala ?: _uiState.value.idSalaSeleccionada
        )
    }

    fun onHorarioChange(inicio: String? = null, fin: String? = null) {
        _uiState.value = _uiState.value.copy(
            horaInicio = inicio ?: _uiState.value.horaInicio,
            horaFin = fin ?: _uiState.value.horaFin
        )
    }

    fun onDiaChange(dia: String, valor: Boolean) {
        _uiState.value = when(dia) {
            "Lun" -> _uiState.value.copy(lunes = valor)
            "Mar" -> _uiState.value.copy(martes = valor)
            "Mie" -> _uiState.value.copy(miercoles = valor)
            "Jue" -> _uiState.value.copy(jueves = valor)
            "Vie" -> _uiState.value.copy(viernes = valor)
            "Sab" -> _uiState.value.copy(sabado = valor)
            "Dom" -> _uiState.value.copy(domingo = valor)
            else -> _uiState.value
        }
    }

    fun guardarEmpleado() {
        val state = _uiState.value
        if (state.cedula.isBlank() || state.primerNombre.isBlank() || state.primerApellido.isBlank() || state.correo.isBlank()) {
            _uiState.value = state.copy(error = "Datos personales incompletos")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            // 1. Preparar DTOs
            val rolNombre = state.roles.find { it.id == state.idRolSeleccionado }?.nombre ?: "MEDICO"
            
            val requestEmpleado = CrearEmpleadoRequest(
                cedula = state.cedula,
                primerNombre = state.primerNombre,
                segundoNombre = state.segundoNombre,
                primerApellido = state.primerApellido,
                segundoApellido = state.segundoApellido,
                correo = state.correo,
                telefono = state.telefono,
                fechaNacimiento = state.fechaNacimiento.ifBlank { "1990-01-01" },
                rol = rolNombre
            )

            val horarioDto = HorariosEmpleadoDto(
                horaInicio = state.horaInicio,
                horaFin = state.horaFin
            )

            val diasDto = DiasLaborablesEmpleadoDto(
                lunes = state.lunes, martes = state.martes, miercoles = state.miercoles,
                jueves = state.jueves, viernes = state.viernes, sabado = state.sabado, domingo = state.domingo
            )

            // 2. Llamada al Repo Orquestador
            val result = adminRepository.crearEmpleadoConDetalles(
                request = requestEmpleado,
                horario = horarioDto,
                dias = diasDto,
                salaId = state.idSalaSeleccionada
            )

            if (result.isSuccess) {
                _uiState.value = state.copy(isLoading = false, success = true)
            } else {
                _uiState.value = state.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun limpiarError() { _uiState.value = _uiState.value.copy(error = null) }
    fun resetState() { _uiState.value = CrearEmpleadoUiState() }
}
