package com.example.lactacare.vistas.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.navegacion.*
import com.example.lactacare.vistas.theme.*

@Composable
fun PantallaHome(
    rolUsuario: RolUsuario,
    onLogout: () -> Unit,
    // --- CORRECCIÓN: Agregamos estos parámetros para que MainActivity no falle ---
    onNavReservas: () -> Unit = {},
    onNavBebe: () -> Unit = {},
    onNavInfo: () -> Unit = {},
    // --------------------------------------------------------------------------
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // Determinar menú y colores según rol
    val (itemsMenu, colorPrincipal) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(menuPaciente, MomPrimary)
        RolUsuario.DOCTOR -> Pair(menuAdmin, DoctorPrimary)
        RolUsuario.ADMINISTRADOR -> Pair(menuAdmin, AdminPrimary)
    }

    // Estado para saber en qué ruta estamos
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Obtenemos el estado del ViewModel (nombre usuario, etc)
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
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
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
                        // Usamos el nombre real que viene del ViewModel
                        TopBarHome(saludo = uiState.nombreUsuario, colorIcono = colorPrincipal)

                        // Aquí iría tu DashboardPaciente real. Por ahora un placeholder.
                        PantallaEnConstruccion("Dashboard Paciente")

                        // Ejemplo: Botón temporal para probar navegación a Reservas
                        Button(onClick = onNavReservas, modifier = Modifier.padding(16.dp)) {
                            Text("Ir a Reservas (Prueba)")
                        }
                    }
                }
                composable(ItemMenu.PacienteBebe.ruta) { PantallaEnConstruccion("Mi Bebé") }
                composable(ItemMenu.PacienteChat.ruta) { PantallaEnConstruccion("Chat IA") }
                composable(ItemMenu.PacientePerfil.ruta) {
                    BotonCerrarSesion(onLogout, viewModel)
                }

                // --- RUTAS ADMIN / DOCTOR ---
                composable(ItemMenu.AdminDashboard.ruta) {
                    Column {
                        TopBarHome(saludo = uiState.nombreUsuario, colorIcono = colorPrincipal)
                        PantallaEnConstruccion("Dashboard Admin")
                    }
                }
                composable(ItemMenu.AdminInventario.ruta) { PantallaEnConstruccion("Inventario") }
                composable(ItemMenu.AdminLactarios.ruta) { PantallaEnConstruccion("Lactarios") }
                composable(ItemMenu.AdminPerfil.ruta) {
                    BotonCerrarSesion(onLogout, viewModel)
                }
            }
        }
    }
}

@Composable
fun BotonCerrarSesion(onLogout: () -> Unit, viewModel: HomeViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Button(onClick = {
            viewModel.cerrarSesion()
            onLogout()
        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Cerrar Sesión")
        }
    }
}