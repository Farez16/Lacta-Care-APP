package com.example.lactacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Importaciones de tus paquetes
import com.example.lactacare.vistas.auth.*
import com.example.lactacare.vistas.home.PantallaHome
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

                    // 1. LÓGICA DE INICIO: Verificar sesión y ROL
                    // Leemos token y rol guardado para saber a dónde enviar al usuario si ya estaba logueado
                    val (rutaInicial, rolGuardado) = runBlocking {
                        val token = sessionManager.authToken.first()
                        val rolString = sessionManager.userRole.first()

                        if (!token.isNullOrEmpty() && !rolString.isNullOrEmpty()) {
                            // Si tiene sesión, va directo al home con su rol
                            Pair("home/$rolString", rolString)
                        } else {
                            // Si no, va a la bienvenida
                            Pair("bienvenida", null)
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = rutaInicial
                    ) {
                        // --- 1. BIENVENIDA ---
                        composable("bienvenida") {
                            PantallaBienvenida(
                                onRolSeleccionado = { rol ->
                                    navController.navigate("login/${rol.name}")
                                }
                            )
                        }

                        // --- 2. LOGIN ---
                        composable(
                            route = "login/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch(e: Exception) { RolUsuario.PACIENTE }

                            val viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()

                            // Pasamos el rol al ViewModel para que pinte los colores correctos
                            LaunchedEffect(rolEnum) { viewModel.setRol(rolEnum) }

                            PantallaLogin(
                                viewModel = viewModel,
                                onIrARegistro = { rol ->
                                    navController.navigate("registro/${rol.name}")
                                },
                                onIrARecuperarPassword = {
                                    navController.navigate("recuperar_password/${rolEnum.name}")
                                },
                                onLoginExitoso = {
                                    // AL LOGIN EXITOSO: Navegamos al Home pasando el ROL
                                    // Esto asegura que el Home sepa si mostrar cosas de Paciente o Doctor
                                    navController.navigate("home/${rolEnum.name}") {
                                        popUpTo("bienvenida") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- 3. REGISTRO ---
                        composable(
                            route = "registro/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) {
                            val viewModel: RegistroPersonaViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                            PantallaRegistroPersona(
                                viewModel = viewModel,
                                onIrALogin = { navController.popBackStack() }
                            )
                        }

                        // --- 4. RECUPERAR PASSWORD ---
                        composable(
                            route = "recuperar_password/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch(e: Exception) { RolUsuario.PACIENTE }
                            val viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()

                            PantallaRecuperarPassword(
                                rolUsuario = rolEnum,
                                viewModel = viewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        // --- 5. HOME (PANTALLA PRINCIPAL REAL) ---
                        // Ahora recibe el rol como parámetro en la ruta: "home/PACIENTE" o "home/DOCTOR"
                        composable(
                            route = "home/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch(e: Exception) { RolUsuario.PACIENTE }

                            PantallaHome(
                                rolUsuario = rolEnum,
                                onLogout = {
                                    navController.navigate("bienvenida") {
                                        popUpTo(0) { inclusive = true } // Borra toda la pila
                                    }
                                },
                                // Aquí conectarás las navegaciones internas (detalles, reservas, etc)
                                onNavReservas = { /* TODO: Navegar a pantalla reservas */ },
                                onNavBebe = { /* TODO: Navegar a pantalla bebé */ },
                                onNavInfo = { /* TODO: Navegar a info */ }
                            )
                        }
                    }
                }
            }
        }
    }
}