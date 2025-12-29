package com.example.lactacare.vistas.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.navegacion.*
import com.example.lactacare.vistas.theme.*
import com.example.lactacare.vistas.admin.inventario.PantallaInventario
import com.example.lactacare.vistas.admin.lactarios.PantallaLactarios
import com.example.lactacare.vistas.admin.perfil.PantallaPerfilAdmin
import com.example.lactacare.vistas.bebe.PantallaAnadirBebe
import com.example.lactacare.vistas.chat.PantallaChat
import com.example.lactacare.vistas.doctor.home.PantallaHomeDoctor
import com.example.lactacare.vistas.doctor.perfil.PantallaPerfilDoctor
import com.example.lactacare.vistas.paciente.home.PantallaHomePaciente
import com.example.lactacare.vistas.paciente.perfil.PantallaPerfilPaciente
import com.example.lactacare.vistas.paciente.reserva.PantallaAgendar

@Composable
fun PantallaHome(
    rolUsuario: RolUsuario,
    onLogout: () -> Unit,
    onNavReservas: () -> Unit = { /* Default empty handled in NavGraph */ },
    onNavBebe: () -> Unit = {},
    onNavInfo: () -> Unit = {},
    onNavGestion: () -> Unit = {},
    onNavAtencion: (Long, String) -> Unit = { _, _ -> }, // Nuevo: Para ir a atender
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // --- ACCIONES DE NAVEGACIÓN ---
    val goReservas = { navController.navigate("agendar_paciente") }
    // ----------------------------

    // 1. Configuración de Menú y Colores
    val (itemsMenu, colorPrincipal) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(menuPaciente, MomPrimary)
        RolUsuario.MEDICO -> Pair(menuAdmin, DoctorPrimary)
        RolUsuario.ADMINISTRADOR -> Pair(menuAdmin, AdminPrimary)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 2. Datos del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = {
            BottomNavBarFlotante(
                items = itemsMenu,
                rutaActual = currentRoute,
                colorActivo = colorPrincipal,
                onItemClick = { item ->
                    navController.navigate(item.ruta) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            NavHost(
                navController = navController,
                startDestination = itemsMenu.first().ruta
            ) {
                // --- RUTAS PACIENTE ---
                composable(ItemMenu.PacienteInicio.ruta) {
                     PantallaHomePaciente(
                         nombreUsuario = uiState.nombreUsuario,
                         onLogout = onLogout,
                         onNavReservas = goReservas,
                         onNavBebe = onNavBebe,
                         onNavInfo = { _ -> onNavInfo() },
                         onNavChat = { navController.navigate(ItemMenu.PacienteChat.ruta) }
                     )
                }
                composable(ItemMenu.PacienteBebe.ruta) { 
                    PantallaAnadirBebe(onVolver = { navController.popBackStack() })
                }
                composable(ItemMenu.PacienteChat.ruta) { PantallaChat() }
                composable(ItemMenu.PacientePerfil.ruta) { 
                    PantallaPerfilPaciente(onLogout = onLogout)
                }

                // --- NUEVA RUTA INTERNA: AGENDAR ---
                composable("agendar_paciente") {
                    PantallaAgendar(onVolver = { navController.popBackStack() })
                }
                // -----------------------------------

                // --- RUTAS ADMIN ---
                composable(ItemMenu.AdminDashboard.ruta) {
                    when (rolUsuario) {
                        RolUsuario.MEDICO -> {
                            PantallaHomeDoctor(
                                onLogout = {
                                    viewModel.cerrarSesion()
                                    onLogout()
                                },
                                onAtender = onNavAtencion
                            )
                        }
                        else -> {
                            Column {
                                TopBarHome(saludo = uiState.nombreUsuario, colorIcono = colorPrincipal)

                                if (uiState.isLoading) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = colorPrincipal)
                                    }
                                } else {
                                    DashboardAdmin(
                                        colorPrincipal = colorPrincipal,
                                        colorAcento = Color(0xFFE1F5FE),
                                        stats = uiState.adminStats,
                                        onNavGestion = onNavGestion
                                    )
                                }
                            }
                        }
                    }
                }
                composable(ItemMenu.AdminInventario.ruta) { 
                    // Pantalla Real de Inventario
                    PantallaInventario() 
                }
                composable(ItemMenu.AdminLactarios.ruta) { 
                     PantallaLactarios() 
                }
                composable(ItemMenu.AdminPerfil.ruta) { 
                    when (rolUsuario) {
                        RolUsuario.MEDICO -> {
                            PantallaPerfilDoctor(onLogout = onLogout)
                        }
                        else -> {
                            PantallaPerfilAdmin(onLogout = onLogout)
                        }
                    }
                }
            }
        }
    }
}

// Botón auxiliar pequeño
@Composable
fun BotonCerrarSesion(onLogout: () -> Unit, viewModel: HomeViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            viewModel.cerrarSesion()
            onLogout()
        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Cerrar Sesión")
        }
    }
}