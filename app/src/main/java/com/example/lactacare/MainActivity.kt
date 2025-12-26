package com.example.lactacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lactacare.datos.dto.GoogleUserData
import com.example.lactacare.vistas.auth.*
import com.example.lactacare.vistas.home.PantallaHome
// --- IMPORT NUEVO ---
import com.example.lactacare.vistas.admin.usuarios.PantallaGestionUsuarios
import com.example.lactacare.vistas.admin.creardoctor.PantallaCrearDoctor
import com.example.lactacare.vistas.doctor.atencion.PantallaAtencion
// --------------------
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.datos.local.SessionManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // 1. DETERMINAR RUTA INICIAL
                    val (rutaInicial, rolGuardado) = runBlocking {
                        val token = sessionManager.authToken.first()
                        val rolString = sessionManager.userRole.first()

                        if (!token.isNullOrEmpty() && !rolString.isNullOrEmpty()) {
                            Pair("home/$rolString", rolString)
                        } else {
                            Pair("bienvenida", null)
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = rutaInicial
                    ) {

                        // --- PANTALLA DE BIENVENIDA ---
                        composable("bienvenida") {
                            PantallaBienvenida(
                                onRolSeleccionado = { rol ->
                                    navController.navigate("login/${rol.name}")
                                }
                            )
                        }

                        // --- PANTALLA DE LOGIN ---
                        composable(
                            route = "login/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch (e: Exception) { RolUsuario.PACIENTE }

                            val viewModel: AuthViewModel = hiltViewModel()
                            LaunchedEffect(rolEnum) { viewModel.setRol(rolEnum) }

                            // Google Login Incompleto
                            val incompleteData by viewModel.profileIncompleteData.collectAsState()
                            LaunchedEffect(incompleteData) {
                                incompleteData?.let { data ->
                                    navController.currentBackStackEntry?.savedStateHandle?.set("googleData", data.googleUserData)
                                    navController.navigate("completar_perfil")
                                }
                            }

                            // Login Exitoso
                            val loginExitoso by viewModel.loginExitoso.collectAsState()
                            LaunchedEffect(loginExitoso) {
                                if (loginExitoso) {
                                    navController.navigate("home/${rolEnum.name}") {
                                        popUpTo("bienvenida") { inclusive = true }
                                    }
                                }
                            }

                            PantallaLogin(
                                viewModel = viewModel,
                                onIrARegistro = { rol ->
                                    navController.navigate("registro/${rol.name}")
                                },
                                onIrARecuperarPassword = {
                                    navController.navigate("recuperar_password/${rolEnum.name}")
                                },
                                onLoginExitoso = {
                                    navController.navigate("home/${rolEnum.name}") {
                                        popUpTo("bienvenida") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- PANTALLA COMPLETAR PERFIL (GOOGLE) ---
                        composable("completar_perfil") { backStackEntry ->
                            // USAMOS remember PARA QUE LOS DATOS SOBREVIVAN A LA RECOMPOSICIÓN
                            val googleUserData = remember {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.get<GoogleUserData>("googleData")
                            }

                            if (googleUserData != null) {
                                PantallaCompletarPerfil(
                                    googleUserData = googleUserData,
                                    onPerfilCompletado = {
                                        // Navegación limpia al Home
                                        navController.navigate("home/PACIENTE") {
                                            popUpTo("bienvenida") { inclusive = true }
                                            launchSingleTop = true // Evita duplicar la pantalla si se clica rápido
                                        }
                                    },
                                    onCancelar = {
                                        navController.popBackStack()
                                    }
                                )
                            } else {
                                // Si los datos son nulos, mostramos carga un momento antes de salir
                                // Esto evita el parpadeo blanco instantáneo
                                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    CircularProgressIndicator()
                                    LaunchedEffect(Unit) {
                                        // Pequeña pausa de seguridad
                                        kotlinx.coroutines.delay(100)
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }

                        // --- PANTALLA DE REGISTRO MANUAL ---
                        composable(
                            route = "registro/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) {
                            val viewModel: RegistroPersonaViewModel = hiltViewModel()
                            PantallaRegistroPersona(
                                viewModel = viewModel,
                                onIrALogin = { navController.popBackStack() }
                            )
                        }

                        // --- PANTALLA RECUPERAR PASSWORD ---
                        composable(
                            route = "recuperar_password/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch (e: Exception) { RolUsuario.PACIENTE }
                            val viewModel: AuthViewModel = hiltViewModel()

                            PantallaRecuperarPassword(
                                rolUsuario = rolEnum,
                                viewModel = viewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        // --- PANTALLA HOME (PRINCIPAL) ---
                        composable(
                            route = "home/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch (e: Exception) { RolUsuario.PACIENTE }

                            PantallaHome(
                                rolUsuario = rolEnum,
                                onLogout = {
                                    navController.navigate("bienvenida") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                // --- CONEXIÓN: AL TOCAR LAS TARJETAS DEL DASHBOARD ADMIN ---
                                onNavGestion = {
                                    navController.navigate("gestion_usuarios")
                                },
                                // NUEVO: Navegación del Doctor
                                onNavAtencion = { idReserva, nombrePaciente ->
                                    navController.navigate("atencion_doctor/$idReserva/$nombrePaciente")
                                },
                                // ------------------------------------------------------------
                                onNavReservas = { /* Navegar a reservas */ },
                                onNavBebe = { /* Navegar a perfil bebé */ },
                                onNavInfo = { /* Navegar a info */ }
                            )
                        }

                        // --- NUEVA PANTALLA: GESTIÓN DE USUARIOS (ADMIN) ---
                        composable("gestion_usuarios") {
                            PantallaGestionUsuarios(
                                onVolver = { navController.popBackStack() },
                                onCrearDoctor = {
                                    navController.navigate("crear_doctor")
                                }
                            )
                        }

                        // --- PANTALLA: CREAR DOCTOR (ADMIN) ---
                        composable("crear_doctor") {
                            PantallaCrearDoctor(
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        // --- PANTALLA: ATENCIÓN DOCTOR ---
                        composable(
                            route = "atencion_doctor/{id}/{nombre}",
                            arguments = listOf(
                                navArgument("id") { type = NavType.LongType },
                                navArgument("nombre") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getLong("id") ?: 0L
                            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Paciente"
                            
                            PantallaAtencion(
                                reservaId = id,
                                nombrePaciente = nombre,
                                onVolver = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}