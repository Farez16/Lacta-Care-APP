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
import com.example.lactacare.datos.repository.AuthRepositoryImpl
import com.example.lactacare.vistas.auth.PantallaBienvenida
import com.example.lactacare.vistas.auth.PantallaLogin
import com.example.lactacare.vistas.auth.PantallaRegistroPersona
import com.example.lactacare.vistas.bebe.PantallaAnadirBebe
import com.example.lactacare.vistas.bebe.PantallaMiBebe
import com.example.lactacare.vistas.home.PantallaHome
import com.example.lactacare.vistas.lactarios.PantallaDetalleInfo
import com.example.lactacare.vistas.lactarios.PantallaLactario
import com.example.lactacare.dominio.model.RolUsuario
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lactacare.vistas.auth.AuthViewModel
import com.example.lactacare.vistas.auth.PantallaRecuperarPassword
import com.example.lactacare.vistas.auth.RegistroPersonaViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val authRepository = AuthRepositoryImpl(this)

                    // Verificar si hay sesión activa
                    val startDestination = if (authRepository.isLoggedIn()) {
                        val session = authRepository.getCurrentSession()
                        "home/${session?.rol?.name ?: "PACIENTE"}"
                    } else {
                        "bienvenida"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        // 1. PANTALLA DE BIENVENIDA (Selección de Rol)
                        composable("bienvenida") {
                            PantallaBienvenida(
                                onRolSeleccionado = { rol ->
                                    navController.navigate("login/${rol.name}")
                                }
                            )
                        }
// NUEVA RUTA: Recuperar Contraseña con argumento de rol
                        composable(
                            route = "recuperar_password/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = RolUsuario.valueOf(rolTexto)

                            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(context))

                            PantallaRecuperarPassword(
                                rolUsuario = rolEnum,
                                viewModel = authViewModel,
                                onVolver = { navController.navigateUp() }
                            )
                        }
                        // 2. PANTALLA DE LOGIN (Con Google OAuth integrado)
                        composable(
                            route = "login/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = RolUsuario.valueOf(rolTexto)

                            // Crear ViewModel con Factory
                            val viewModel: AuthViewModel = viewModel(
                                factory = AuthViewModel.Factory(context)
                            )

                            // Establecer el rol seleccionado
                            viewModel.setRol(rolEnum)

                            PantallaLogin(
                                viewModel = viewModel,
                                onIrARegistro = { rol ->
                                    navController.navigate("registro/${rol.name}")
                                },
                                onIrARecuperarPassword = {
                                    // Pasamos el rol actual para que la pantalla de recuperación tenga los colores correctos
                                    navController.navigate("recuperar_password/${rolEnum.name}")
                                },
                                onLoginExitoso = {
                                    val session = authRepository.getCurrentSession()
                                    val rolActual = session?.rol?.name ?: rolEnum.name
                                    navController.navigate("home/$rolActual") {
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

                            // Crear ViewModel para registro
                            val viewModel: RegistroPersonaViewModel = viewModel(
                                factory = RegistroPersonaViewModel.Factory(LocalContext.current)
                            )
                            viewModel.setRol(rolEnum)

                            PantallaRegistroPersona(
                                viewModel = viewModel,
                                onIrALogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 4. PANTALLA HOME PRINCIPAL
                        composable(
                            route = "home/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = RolUsuario.valueOf(rolTexto)

                            PantallaHome(
                                rolUsuario = rolEnum,
                                onLogout = {
                                    // Cerrar sesión
                                    kotlinx.coroutines.runBlocking {
                                        authRepository.logout()
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

                        // 5. SUB-PANTALLAS DEL PACIENTE
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