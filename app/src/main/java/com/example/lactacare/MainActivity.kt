package com.example.lactacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// Importaciones de Hilt
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Tus Vistas y Dominio
import com.example.lactacare.vistas.auth.PantallaBienvenida
import com.example.lactacare.vistas.auth.PantallaLogin
import com.example.lactacare.vistas.auth.PantallaRegistroPersona
import com.example.lactacare.vistas.bebe.PantallaAnadirBebe
import com.example.lactacare.vistas.bebe.PantallaMiBebe
import com.example.lactacare.vistas.home.PantallaHome
import com.example.lactacare.vistas.lactarios.PantallaDetalleInfo
import com.example.lactacare.vistas.lactarios.PantallaLactario
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.auth.AuthViewModel
import com.example.lactacare.vistas.auth.PantallaRecuperarPassword
import com.example.lactacare.vistas.auth.RegistroPersonaViewModel
// Datos
import com.example.lactacare.datos.local.SessionManager

@AndroidEntryPoint // <--- 1. ¡OBLIGATORIO! Convierte la Activity en consumidor de Hilt
class MainActivity : ComponentActivity() {

    // <--- 2. Inyectamos SessionManager para saber si hay usuario logueado
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

                    // <--- 3. Lógica de Inicio de Sesión (DataStore es asíncrono, usamos runBlocking para el arranque)
                    val startDestination = runBlocking {
                        val token = sessionManager.authToken.first()
                        val rol = sessionManager.userRole.first()

                        if (!token.isNullOrEmpty()) {
                            "home/${rol ?: "PACIENTE"}"
                        } else {
                            "bienvenida"
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        // 1. PANTALLA DE BIENVENIDA
                        composable("bienvenida") {
                            PantallaBienvenida(
                                onRolSeleccionado = { rol ->
                                    navController.navigate("login/${rol.name}")
                                }
                            )
                        }

                        // RECUPERAR PASSWORD
                        composable(
                            route = "recuperar_password/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = RolUsuario.valueOf(rolTexto)

                            // <--- 4. Usamos hiltViewModel() en lugar de Factory manual
                            val authViewModel: AuthViewModel = hiltViewModel()

                            PantallaRecuperarPassword(
                                rolUsuario = rolEnum,
                                viewModel = authViewModel,
                                onVolver = { navController.navigateUp() }
                            )
                        }

                        // 2. PANTALLA DE LOGIN
                        composable(
                            route = "login/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = RolUsuario.valueOf(rolTexto)

                            // <--- hiltViewModel inyecta Repo y SessionManager automáticamente
                            val viewModel: AuthViewModel = hiltViewModel()

                            // Pasamos el rol al ViewModel (esto es lógica de UI, está bien aquí)
                            // Nota: Asegúrate de tener una función en tu VM para setear el rol si la necesitas
                            // viewModel.setRol(rolEnum)

                            PantallaLogin(
                                viewModel = viewModel,
                                onIrARegistro = { rol ->
                                    navController.navigate("registro/${rol.name}")
                                },
                                onIrARecuperarPassword = {
                                    navController.navigate("recuperar_password/${rolEnum.name}")
                                },
                                onLoginExitoso = {
                                    // Al ser exitoso, el Repository ya guardó los datos en SessionManager.
                                    // Solo navegamos leyendo el estado actual o el rol que tenemos.
                                    navController.navigate("home/${rolEnum.name}") {
                                        popUpTo("bienvenida") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. PANTALLA DE REGISTRO
                        composable(
                            route = "registro/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = RolUsuario.valueOf(rolTexto)

                            // <--- También convertimos este a Hilt (Asegúrate de ponerle @HiltViewModel a la clase)
                            val viewModel: RegistroPersonaViewModel = hiltViewModel()
                            viewModel.setRol(rolEnum)

                            PantallaRegistroPersona(
                                viewModel = viewModel,
                                onIrALogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 4. PANTALLA HOME
                        composable(
                            route = "home/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = RolUsuario.valueOf(rolTexto)

                            PantallaHome(
                                rolUsuario = rolEnum,
                                onLogout = {
                                    // Logout usando el SessionManager inyectado
                                    runBlocking {
                                        sessionManager.clearSession()
                                    }
                                    navController.navigate("bienvenida") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavReservas = { navController.navigate("reservas") },
                                onNavBebe = { navController.navigate("mi_bebe") },
                                onNavInfo = { titulo -> navController.navigate("info/$titulo") }
                            )
                        }

                        // 5. RESTO DE PANTALLAS (Sin cambios mayores)
                        composable("reservas") {
                            PantallaLactario(onVolver = { navController.popBackStack() })
                        }

                        composable("mi_bebe") {
                            PantallaMiBebe(
                                onVolver = { navController.popBackStack() },
                                onNavAnadirBebe = { navController.navigate("anadir_bebe") }
                            )
                        }

                        composable("anadir_bebe") {
                            PantallaAnadirBebe(
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "info/{titulo}",
                            arguments = listOf(navArgument("titulo") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val titulo = backStackEntry.arguments?.getString("titulo") ?: "Información"
                            PantallaDetalleInfo(
                                titulo = titulo,
                                onVolver = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}