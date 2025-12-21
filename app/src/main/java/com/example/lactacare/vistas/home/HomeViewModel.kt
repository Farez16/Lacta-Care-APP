package com.example.lactacare.vistas.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lactacare.dominio.model.DashboardAdminStats
import com.example.lactacare.dominio.model.Reservas
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.dominio.repository.AdminRepository
import com.example.lactacare.dominio.repository.BebeRepository
import com.example.lactacare.dominio.repository.MedicoRepository
import com.example.lactacare.dominio.repository.PacienteRepository
import com.example.lactacare.dominio.repository.ReservasRepository
import com.example.lactacare.vistas.navegacion.ItemMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. ACTUALIZAMOS EL ESTADO PARA INCLUIR ADMIN
data class HomeUiState(
    val nombreUsuario: String = "Cargando...",
    val isLoading: Boolean = false,

    // Datos Paciente
    val proximaReserva: Reservas? = null,
    val nombreBebe: String? = null,

    // Datos Admin (NUEVO)
    val datosAdmin: DashboardAdminStats? = null
)

class HomeViewModel(
    private val pacienteRepo: PacienteRepository,
    private val medicoRepo: MedicoRepository,
    private val adminRepo: AdminRepository,
    private val reservaRepo: ReservasRepository,
    private val bebeRepo: BebeRepository
) : ViewModel() {

    private val _rolActual = MutableStateFlow(RolUsuario.PACIENTE)
    val rolActual: StateFlow<RolUsuario> = _rolActual.asStateFlow()

    private val _seccionActual = MutableStateFlow<ItemMenu>(ItemMenu.Inicio)
    val seccionActual: StateFlow<ItemMenu> = _seccionActual.asStateFlow()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun setRol(rol: RolUsuario) {
        _rolActual.value = rol
        cargarDatosUsuario(rol)
    }

    private fun cargarDatosUsuario(rol: RolUsuario) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val idUsuario = 1 // Simulamos usuario logueado

            // A. Cargar Nombre según Rol (Tu lógica original)
            val nombreRecuperado = when (rol) {
                RolUsuario.PACIENTE -> {
                    val p = pacienteRepo.obtenerPacientePorId(idUsuario)
                    p?.primerNombre ?: "Mamá"
                }
                RolUsuario.DOCTOR -> {
                    val m = medicoRepo.obtenerMedicoPorId(idUsuario)
                    "Dr. ${m?.primerNombre ?: "Especialista"}"
                }
                RolUsuario.ADMINISTRADOR -> {
                    val a = adminRepo.obtenerAdminPorId(idUsuario)
                    "Admin. ${a?.primerNombre ?: "Sistema"}"
                }
            }

            // B. Variables para los datos específicos
            var proximaCita: Reservas? = null
            var miBebe: String? = null
            var statsAdmin: DashboardAdminStats? = null // Variable para admin

            // C. Cargar datos específicos según el rol
            when (rol) {
                RolUsuario.PACIENTE -> {
                    proximaCita = reservaRepo.obtenerProximaReservaPaciente(idUsuario)
                    val bebes = bebeRepo.obtenerBebesPorFamiliar(idUsuario)
                    if (bebes.isNotEmpty()) {
                        miBebe = bebes[0].nombre
                    }
                }
                RolUsuario.ADMINISTRADOR -> {
                    // AQUÍ LLAMAMOS A LA NUEVA LÓGICA
                    statsAdmin = adminRepo.obtenerEstadisticas()
                }
                RolUsuario.DOCTOR -> {
                    // Pendiente: cargar agenda doctor
                }
            }

            // D. Actualizar el estado con TODO
            _uiState.value = HomeUiState(
                nombreUsuario = "Hola, $nombreRecuperado",
                isLoading = false,
                proximaReserva = proximaCita,
                nombreBebe = miBebe,
                datosAdmin = statsAdmin // Guardamos los datos del admin
            )
        }
    }

    fun seleccionarSeccion(item: ItemMenu) {
        _seccionActual.value = item
    }

    fun obtenerMenuPorRol(rol: RolUsuario): List<ItemMenu> {
        return when (rol) {
            RolUsuario.PACIENTE -> listOf(ItemMenu.Inicio, ItemMenu.Chatbot, ItemMenu.Registros, ItemMenu.Perfil)
            RolUsuario.DOCTOR -> listOf(ItemMenu.Inicio, ItemMenu.Agenda, ItemMenu.Pacientes, ItemMenu.Perfil)
            RolUsuario.ADMINISTRADOR -> listOf(ItemMenu.Inicio, ItemMenu.Empleados, ItemMenu.Lactarios, ItemMenu.Perfil)
        }
    }
}