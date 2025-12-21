package com.example.lactacare.vistas.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lactacare.datos.*
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.chat.PantallaChat
import com.example.lactacare.vistas.inventario.PantallaInventario
import com.example.lactacare.vistas.perfil.PantallaPerfil
import com.example.lactacare.vistas.theme.AdminBackground
import com.example.lactacare.vistas.theme.AdminPrimary
import com.example.lactacare.vistas.theme.DoctorBackground
import com.example.lactacare.vistas.theme.DoctorPrimary
import com.example.lactacare.vistas.theme.MomAccent
import com.example.lactacare.vistas.theme.MomPrimary
import com.example.lactacare.vistas.theme.TextoOscuroClean

@Composable
fun PantallaHome(
    rolUsuario: RolUsuario,
    onLogout: () -> Unit,
    // --- CORRECCIÓN AQUÍ ---
    // Renombrado de 'onNavegarANuevaReserva' a 'onNavReservas' para coincidir con MainActivity
    onNavReservas: () -> Unit = {},
    onNavegarADetalleReserva: () -> Unit = {},
    onNavBebe: () -> Unit = {},
    onNavInfo: (String) -> Unit = {},

    viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    pacienteRepo = MockPacienteRepository(),
                    medicoRepo = MockMedicoRepository(),
                    adminRepo = MockAdminRepository(),
                    reservaRepo = MockReservasRepository(),
                    bebeRepo = MockBebeRepository()
                ) as T
            }
        }
    )
) {
    LaunchedEffect(rolUsuario) { viewModel.setRol(rolUsuario) }

    val seccionActual by viewModel.seccionActual.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val (colorPrincipal, colorAcento) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(MomPrimary, MomAccent)
        RolUsuario.DOCTOR -> Pair(DoctorPrimary, DoctorBackground)
        RolUsuario.ADMINISTRADOR -> Pair(AdminPrimary, AdminBackground)
    }

    val menuItems = viewModel.obtenerMenuPorRol(rolUsuario)

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            BottomNavBarFlotante(
                items = menuItems,
                seccionActual = seccionActual,
                colorActivo = colorPrincipal,
                onItemClick = { viewModel.seleccionarSeccion(it) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // TopBar dinámica
            if (seccionActual.id == "inicio") {
                TopBarHome(saludo = uiState.nombreUsuario, colorIcono = TextoOscuroClean)
            }

            Box(modifier = Modifier.weight(1f)) {
                when (seccionActual.id) {
                    // 1. DASHBOARD PRINCIPAL (Inicio)
                    "inicio" -> {
                        when (rolUsuario) {
                            RolUsuario.PACIENTE -> DashboardPaciente(
                                colorPrimary = colorPrincipal,
                                colorAccent = colorAcento,
                                nombreUsuario = uiState.nombreUsuario,
                                proximaCita = uiState.proximaReserva,
                                nombreBebe = uiState.nombreBebe,
                                // Pasamos el parámetro corregido aquí:
                                onNavReservas = onNavReservas,
                                onNavBebe = onNavBebe,
                                onNavInfo = onNavInfo
                            )

                            RolUsuario.DOCTOR -> DashboardDoctor(colorPrincipal, colorAcento)

                            RolUsuario.ADMINISTRADOR -> DashboardAdmin(
                                colorPrincipal = colorPrincipal,
                                colorAcento = colorAcento,
                                stats = uiState.datosAdmin,
                                onNavGestion = { viewModel.seleccionarSeccion(com.example.lactacare.vistas.navegacion.ItemMenu.Lactarios) },
                                onNavReportes = { /* Acción futura */ }
                            )
                        }
                    }

                    // 2. SECCIONES DE ADMIN
                    "Lactarios" -> {
                        // Asegúrate de tener importada esta pantalla o usa la ruta completa si no la has movido
                        com.example.lactacare.vistas.admin.PantallaGestionLactarios(
                            onVolver = { viewModel.seleccionarSeccion(com.example.lactacare.vistas.navegacion.ItemMenu.Inicio) }
                        )
                    }

                    "empleados" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Pantalla Gestión de Empleados", color = Color.Gray)
                        }
                    }

                    // 3. SECCIONES COMUNES
                    "chatbot" -> PantallaChat()
                    "registros" -> PantallaInventario()
                    "perfil" -> PantallaPerfil(rolUsuario = rolUsuario, onLogout = onLogout)

                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("En construcción: ${seccionActual.titulo}")
                    }
                }
            }
        }
    }
}