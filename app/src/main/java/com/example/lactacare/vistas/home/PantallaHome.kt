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

@Composable
fun PantallaHome(
    rolUsuario: RolUsuario,
    onLogout: () -> Unit,
    onNavReservas: () -> Unit = {},
    onNavBebe: () -> Unit = {},
    onNavInfo: () -> Unit = {},
    onNavGestion: () -> Unit = {}, // ✅ Recibimos la navegación desde MainActivity
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // 1. Configuración de Menú y Colores
    val (itemsMenu, colorPrincipal) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(menuPaciente, MomPrimary)
        RolUsuario.DOCTOR -> Pair(menuAdmin, DoctorPrimary)
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
                    Column {
                        TopBarHome(saludo = uiState.nombreUsuario, colorIcono = colorPrincipal)
                        PantallaEnConstruccion("Dashboard Paciente (Pronto)")
                    }
                }
                composable(ItemMenu.PacienteBebe.ruta) { PantallaEnConstruccion("Mi Bebé") }
                composable(ItemMenu.PacienteChat.ruta) { PantallaEnConstruccion("Chat IA") }
                composable(ItemMenu.PacientePerfil.ruta) { BotonCerrarSesion(onLogout, viewModel) }

                // --- RUTAS ADMIN ---
                composable(ItemMenu.AdminDashboard.ruta) {
                    Column {
                        TopBarHome(saludo = uiState.nombreUsuario, colorIcono = colorPrincipal)

                        // Lógica de carga
                        if (uiState.isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = colorPrincipal)
                            }
                        } else {
                            // ✅ AQUÍ ESTÁ EL CAMBIO
                            DashboardAdmin(
                                colorPrincipal = colorPrincipal,
                                colorAcento = Color(0xFFE1F5FE),
                                stats = uiState.adminStats,
                                onNavGestion = onNavGestion // <--- ¡Conectado el cable que faltaba!
                            )
                        }
                    }
                }
                composable(ItemMenu.AdminInventario.ruta) { PantallaEnConstruccion("Inventario") }
                composable(ItemMenu.AdminLactarios.ruta) { PantallaEnConstruccion("Lactarios") }
                composable(ItemMenu.AdminPerfil.ruta) { BotonCerrarSesion(onLogout, viewModel) }
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