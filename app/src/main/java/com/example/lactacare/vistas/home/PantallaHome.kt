package com.example.lactacare.vistas.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.remote.creation.first
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.navegacion.*
import com.example.lactacare.vistas.theme.*
// Imports de Vistas Admin
import com.example.lactacare.vistas.admin.lactarios.PantallaLactarios
import com.example.lactacare.vistas.admin.perfil.PantallaPerfilAdmin
import com.example.lactacare.vistas.admin.usuarios.PantallaGestionUsuarios

import com.example.lactacare.vistas.admin.refrigeradores.PantallaRefrigeradores // Usamos Refrigeradores (más específico que Inventario)
import com.example.lactacare.vistas.admin.ia.PantallaGestionIA
import com.example.lactacare.vistas.admin.alertas.PantallaAlertas
import com.example.lactacare.vistas.admin.sugerencias.PantallaSugerencias
import com.example.lactacare.vistas.admin.imagenes.PantallaImagenes
import com.example.lactacare.vistas.admin.instituciones.PantallaInstituciones
// Imports de Vistas Paciente/Bebe/Chat/Doctor
import com.example.lactacare.vistas.bebe.PantallaAnadirBebe
import com.example.lactacare.vistas.chat.PantallaChat
import com.example.lactacare.vistas.doctor.home.PantallaHomeDoctor
import com.example.lactacare.vistas.doctor.perfil.PantallaPerfilDoctor
import com.example.lactacare.vistas.paciente.home.PantallaHomePaciente
import com.example.lactacare.vistas.paciente.perfil.PantallaPerfilPaciente
import com.example.lactacare.vistas.paciente.reserva.PantallaAgendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking  // ✅ AGREGAR ESTA LÍNEA
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.lactacare.vistas.paciente.reserva.PantallaSeleccionarFechaHora
@Composable
fun PantallaHome(
    rolUsuario: RolUsuario,
    onLogout: () -> Unit,
    onNavReservas: () -> Unit = { /* Default empty handled in NavGraph */ },
    onNavBebe: () -> Unit = {},
    onNavInfo: () -> Unit = {},
    onNavReportes: () -> Unit = {}, // Nuevo callback
    onNavGestion: () -> Unit = {},
    onNavAtencion: (Long, String) -> Unit = { _, _ -> },
    onNavMisReservas: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // --- ACCIONES DE NAVEGACIÓN ---
    val goReservas = { navController.navigate("agendar_paciente") }
    val goMisReservas = { navController.navigate("mis_reservas_paciente") }
    // ----------------------------

    // 1. Configuración de Menú y Colores según Rol
    val (itemsMenu, colorPrincipal) = when (rolUsuario) {
        RolUsuario.PACIENTE -> Pair(menuPaciente, MomPrimary)
        RolUsuario.MEDICO -> Pair(menuAdmin, DoctorPrimary)
        RolUsuario.ADMINISTRADOR -> Pair(menuAdmin, AdminPrimary)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val uiState by viewModel.uiState.collectAsState()

    // 2. ESTADO DEL DRAWER (Menú Lateral)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val gesturesEnabled = rolUsuario == RolUsuario.ADMINISTRADOR || rolUsuario == RolUsuario.MEDICO

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                // Cabecera del Drawer
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Menú Principal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorPrincipal)
                }
                Divider()

                // --- Ítems del Menú Lateral (Solo Admin/Medico) ---
                val drawerItems = listOf(
                    Triple(ItemMenu.AdminIA, "IA", ItemMenu.AdminIA.ruta),
                    Triple(ItemMenu.AdminSugerencias, "Sugerencias", ItemMenu.AdminSugerencias.ruta),
                    Triple(ItemMenu.AdminImagenes, "Imágenes", ItemMenu.AdminImagenes.ruta),
                    Triple(ItemMenu.AdminReporte, "Generar Reporte", "action_report")
                )

                drawerItems.forEach { (item, label, ruta) ->
                    NavigationDrawerItem(
                        label = { Text(text = label) },
                        selected = currentRoute == ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (ruta == "action_report") {
                                onNavReportes() // USAR CALLBACK DE NAVEGACION
                            } else {
                                navController.navigate(ruta) { launchSingleTop = true }
                            }
                        },
                        icon = { Icon(item.icono, null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFFFAFAFA),
            bottomBar = {
                BottomNavBarFlotante(
                    items = itemsMenu,
                    rutaActual = currentRoute,
                    colorActivo = colorPrincipal,
                    onItemClick = { item ->
                        navController.navigate(item.ruta) {
                            if (item.ruta == itemsMenu.first().ruta) {
                                popUpTo(0) { saveState = false }
                            } else {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
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
                    // ==========================================
                    // RUTAS PACIENTE
                    // ==========================================
                    composable(ItemMenu.PacienteInicio.ruta) {
                        PantallaHomePaciente(
                            nombreUsuario = uiState.nombreUsuario,
                            onLogout = onLogout,
                            onNavReservas = goReservas,
                            onNavBebe = onNavBebe,
                            onNavInfo = { _ -> onNavInfo() },
                            onNavChat = { navController.navigate(ItemMenu.PacienteChat.ruta) },
                            onNavMisReservas = goMisReservas
                        )
                    }
                    composable(ItemMenu.PacienteBebe.ruta) {
                        PantallaAnadirBebe(onVolver = { navController.popBackStack() })
                    }
                    composable(ItemMenu.PacienteInventario.ruta) {
                        val pacienteId = uiState.userId ?: 0L
                        com.example.lactacare.vistas.paciente.inventario.PantallaInventario(
                            idPaciente = pacienteId
                        )
                    }
                    composable(ItemMenu.PacienteChat.ruta) { PantallaChat() }
                    composable(ItemMenu.PacientePerfil.ruta) {
                        PantallaPerfilPaciente(onLogout = onLogout)
                    }
                    composable("agendar_paciente") {
                        PantallaAgendar(
                            onVolver = { navController.popBackStack() },
                            onNavSeleccionarHorario = { lactarioId, nombreSala ->
                                // Navegar a selección de cubículo primero
                                navController.navigate("seleccionar_cubiculo/$lactarioId/$nombreSala")
                            }
                        )
                    }
                    // NUEVA RUTA: Selección de Cubículo
                    composable(
                        route = "seleccionar_cubiculo/{idSala}/{nombreSala}",
                        arguments = listOf(
                            navArgument("idSala") { type = NavType.LongType },
                            navArgument("nombreSala") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val idSala = backStackEntry.arguments?.getLong("idSala") ?: 0L
                        val nombreSala = backStackEntry.arguments?.getString("nombreSala") ?: ""

                        com.example.lactacare.vistas.paciente.reserva.PantallaSeleccionCubiculo(
                            idSala = idSala,
                            nombreSala = nombreSala,
                            onVolver = { navController.popBackStack() },
                            onCubiculoSeleccionado = { cubiculoId, nombreCubiculo ->
                                navController.navigate("seleccionar_fecha_hora/$idSala/$nombreSala/$cubiculoId/$nombreCubiculo")
                            }
                        )
                    }
                    // ✅ RUTA SELECCIONAR FECHA/HORA (Actualizada con cubículo)
                    composable(
                        route = "seleccionar_fecha_hora/{lactarioId}/{nombreSala}/{cubiculoId}/{nombreCubiculo}",
                        arguments = listOf(
                            navArgument("lactarioId") { type = NavType.LongType },
                            navArgument("nombreSala") { type = NavType.StringType },
                            navArgument("cubiculoId") { type = NavType.LongType },
                            navArgument("nombreCubiculo") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val lactarioId = backStackEntry.arguments?.getLong("lactarioId") ?: 0L
                        val nombreSala = backStackEntry.arguments?.getString("nombreSala") ?: ""
                        val cubiculoId = backStackEntry.arguments?.getLong("cubiculoId") ?: 0L
                        val nombreCubiculo = backStackEntry.arguments?.getString("nombreCubiculo") ?: ""

                        PantallaSeleccionarFechaHora(
                            lactarioId = lactarioId,
                            nombreSala = nombreSala,
                            cubiculoId = cubiculoId,
                            nombreCubiculo = nombreCubiculo,
                            onNavigateBack = { navController.popBackStack() },
                            onReservaConfirmada = {
                                navController.navigate("mis_reservas_paciente") {
                                    popUpTo("home_paciente") { inclusive = false }
                                }
                            }
                        )
                    }
                    // ✅ RUTA MIS RESERVAS - SOLUCIÓN COMPLETA
                    composable("mis_reservas_paciente") {
                        val pacienteId = uiState.userId ?: 0L

                        com.example.lactacare.vistas.paciente.reserva.PantallaMisReservas(
                            pacienteId = pacienteId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // ==========================================
                    // RUTAS ADMIN / MEDICO
                    // ==========================================
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
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    LaunchedEffect(uiState.mensaje) {
                                        uiState.mensaje?.let {
                                            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
                                            viewModel.limpiarMensaje()
                                        }
                                    }

                                    TopBarHome(
                                        saludo = uiState.nombreUsuario,
                                        colorIcono = colorPrincipal,
                                        onMenuClick = { scope.launch { drawerState.open() } }
                                    )

                                    if (uiState.isLoading) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = colorPrincipal)
                                        }
                                    } else {
                                        DashboardAdmin(
                                            colorPrincipal = colorPrincipal,
                                            colorAcento = Color(0xFFE1F5FE),
                                            stats = uiState.adminStats,
                                            onNavGestion = { navController.navigate(ItemMenu.AdminGestionUsuarios.ruta) },
                                            onNavAlertas = { navController.navigate(ItemMenu.AdminAlertas.ruta) },
                                            onNavReportes = onNavReportes // PASAMOS EL CALLBACK
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- Pantallas Principales Admin/Doctor ---
                    composable(ItemMenu.AdminRefrigeradores.ruta) {
                        // Medico solo ve, Admin edita
                        val esEditable = rolUsuario == RolUsuario.ADMINISTRADOR
                        PantallaRefrigeradores(
                            esEditable = esEditable,
                            primaryColor = if (esEditable) NeonPrimary else DoctorPrimary
                        ) 
                    }
                    composable(ItemMenu.AdminLactarios.ruta) {
                        val esEditable = rolUsuario == RolUsuario.ADMINISTRADOR
                        PantallaLactarios(
                            esEditable = esEditable,
                            primaryColor = if (esEditable) NeonPrimary else DoctorPrimary
                        )
                    }
                    composable(ItemMenu.AdminInstituciones.ruta) {
                        val esEditable = rolUsuario == RolUsuario.ADMINISTRADOR
                        PantallaInstituciones(
                            esEditable = esEditable,
                            primaryColor = if (esEditable) NeonPrimary else DoctorPrimary
                        ) 
                    }
                    composable(ItemMenu.AdminPerfil.ruta) {
                        when (rolUsuario) {
                            RolUsuario.MEDICO -> PantallaPerfilDoctor(onLogout = onLogout)
                            else -> PantallaPerfilAdmin(onLogout = onLogout)
                        }
                    }

                    // --- Rutas del Drawer y Gestión ---
                    composable(ItemMenu.AdminIA.ruta) { PantallaGestionIA() }
                    composable(ItemMenu.AdminAlertas.ruta) { PantallaAlertas() }
                    composable(ItemMenu.AdminSugerencias.ruta) { PantallaSugerencias() }
                    composable(ItemMenu.AdminImagenes.ruta) { PantallaImagenes() }

                    composable(ItemMenu.AdminGestionUsuarios.ruta) {
                        PantallaGestionUsuarios(
                            onVolver = { navController.popBackStack() },
                            onCrearDoctor = { navController.navigate("admin_crear_doctor") }
                        )
                    }
                    
                    composable("admin_crear_doctor") {
                        com.example.lactacare.vistas.admin.usuarios.PantallaCrearEmpleado(
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

// Botón auxiliar (opcional, por si se necesita en layouts de error)
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