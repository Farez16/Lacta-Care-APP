package com.example.lactacare.vistas.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.example.lactacare.vistas.admin.inventario.PantallaInventario
import com.example.lactacare.vistas.admin.lactarios.PantallaLactarios
import com.example.lactacare.vistas.admin.perfil.PantallaPerfilAdmin
import com.example.lactacare.vistas.admin.usuarios.PantallaGestionUsuarios // Importante
import com.example.lactacare.vistas.bebe.PantallaAnadirBebe
import com.example.lactacare.vistas.chat.PantallaChat
import com.example.lactacare.vistas.doctor.home.PantallaHomeDoctor
import com.example.lactacare.vistas.doctor.perfil.PantallaPerfilDoctor
import com.example.lactacare.vistas.paciente.home.PantallaHomePaciente
import com.example.lactacare.vistas.paciente.perfil.PantallaPerfilPaciente
import com.example.lactacare.vistas.paciente.reserva.PantallaAgendar
import kotlinx.coroutines.launch

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
    
    // 3. ESTADO DEL DRAWER (NUEVO)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = rolUsuario == RolUsuario.ADMINISTRADOR || rolUsuario == RolUsuario.MEDICO, // Gesture solo para admins/medicos
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                // Cabecera del Drawer
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.CenterStart) {
                    Text("Menú Principal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorPrincipal)
                }
                Divider()
                // Opción Gestión IA
                NavigationDrawerItem(
                    label = { Text(text = "IA") },
                    selected = currentRoute == ItemMenu.AdminIA.ruta,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(ItemMenu.AdminIA.ruta) {
                           launchSingleTop = true
                        }
                    },
                    icon = { Icon(ItemMenu.AdminIA.icono, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Opción Sugerencias
                NavigationDrawerItem(
                    label = { Text(text = "Sugerencias") },
                    selected = currentRoute == ItemMenu.AdminSugerencias.ruta,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(ItemMenu.AdminSugerencias.ruta) {
                           launchSingleTop = true
                        }
                    },
                    icon = { Icon(ItemMenu.AdminSugerencias.icono, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Opción Imagenes
                NavigationDrawerItem(
                    label = { Text(text = "Imágenes") },
                    selected = currentRoute == ItemMenu.AdminImagenes.ruta,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(ItemMenu.AdminImagenes.ruta) {
                           launchSingleTop = true
                        }
                    },
                    icon = { Icon(ItemMenu.AdminImagenes.icono, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
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
                            // Cuando vamos al Home, limpiamos todo el stack para que sea la base real
                             if (item.ruta == itemsMenu.first().ruta) {
                                popUpTo(0) { saveState = false } // Limpiar todo el historial al ir a inicio
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
                                // Pasamos el evento de abrir drawer
                                TopBarHome(
                                    saludo = uiState.nombreUsuario, 
                                    colorIcono = colorPrincipal,
                                    onMenuClick = {
                                        scope.launch { drawerState.open() }
                                    }
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
                                        onNavGestion = { 
                                            navController.navigate(ItemMenu.AdminGestionUsuarios.ruta) 
                                        },
                                        onNavAlertas = {
                                            navController.navigate(ItemMenu.AdminAlertas.ruta)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                    // REEMPLAZO: Inventario -> Refrigeradores
                    composable(ItemMenu.AdminRefrigeradores.ruta) { 
                        com.example.lactacare.vistas.admin.refrigeradores.PantallaRefrigeradores() 
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
                    
                    // --- RUTAS MENU LATERAL ---
                    composable(ItemMenu.AdminIA.ruta) {
                         com.example.lactacare.vistas.admin.ia.PantallaGestionIA()
                    }
                    composable(ItemMenu.AdminAlertas.ruta) {
                         com.example.lactacare.vistas.admin.alertas.PantallaAlertas()
                    }
                    composable(ItemMenu.AdminSugerencias.ruta) {
                         com.example.lactacare.vistas.admin.sugerencias.PantallaSugerencias()
                    }
                    composable(ItemMenu.AdminImagenes.ruta) {
                         com.example.lactacare.vistas.admin.imagenes.PantallaImagenes()
                    }
                    
                    // --- GESTIÓN USUARIOS ---
                    composable(ItemMenu.AdminGestionUsuarios.ruta) {
                        com.example.lactacare.vistas.admin.usuarios.PantallaGestionUsuarios(
                            onVolver = { navController.popBackStack() },
                            onCrearDoctor = { /* TODO: Implementar creación */ }
                        )
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