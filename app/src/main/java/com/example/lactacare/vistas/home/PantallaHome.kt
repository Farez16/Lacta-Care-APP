package com.example.lactacare.vistas.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // Determinar menú y colores según rol
    val (itemsMenu, colorPrincipal) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(menuPaciente, MomPrimary)
        RolUsuario.DOCTOR -> Pair(menuAdmin, DoctorPrimary) // Usamos menú admin por ahora para doc
        RolUsuario.ADMINISTRADOR -> Pair(menuAdmin, AdminPrimary)
    }

    // Estado para saber en qué ruta estamos
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color(0xFFFAFAFA), // Tu color de fondo original
        bottomBar = {
            // USAMOS TU BARRA FLOTANTE PERSONALIZADA
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

            // CONTENIDO CAMBIANTE
            NavHost(
                navController = navController,
                startDestination = itemsMenu.first().ruta
            ) {
                // --- RUTAS PACIENTE ---
                composable(ItemMenu.PacienteInicio.ruta) {
                    // Aquí llamamos a tu TopBar + Dashboard
                    Column {
                        TopBarHome(saludo = "Mamá", colorIcono = colorPrincipal)
                        PantallaEnConstruccion("Dashboard Paciente")
                        // Aquí iría tu DashboardStatCard cuando tengamos datos
                    }
                }
                composable(ItemMenu.PacienteBebe.ruta) { PantallaEnConstruccion("Mi Bebé") }
                composable(ItemMenu.PacienteChat.ruta) { PantallaEnConstruccion("Chat IA") }
                composable(ItemMenu.PacientePerfil.ruta) {
                    BotonCerrarSesion(onLogout, viewModel)
                }

                // --- RUTAS ADMIN ---
                composable(ItemMenu.AdminDashboard.ruta) {
                    Column {
                        TopBarHome(saludo = "Admin", colorIcono = colorPrincipal)
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