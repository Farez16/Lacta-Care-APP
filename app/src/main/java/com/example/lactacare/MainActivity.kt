package com.example.lactacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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
                    // Verificamos si ya existe una sesión guardada antes de cargar la interfaz
                    val (rutaInicial, rolGuardado) = runBlocking {
                        val token = sessionManager.authToken.first()
                        val rolString = sessionManager.userRole.first()

                        if (!token.isNullOrEmpty() && !rolString.isNullOrEmpty()) {
                            // Sesión activa -> Ir al Home
                            Pair("home/$rolString", rolString)
                        } else {
                            // Sin sesión -> Ir a Bienvenida
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

                            // Configuramos el rol actual en el ViewModel al entrar
                            LaunchedEffect(rolEnum) { viewModel.setRol(rolEnum) }

                            // OBSERVER 1: PERFIL INCOMPLETO (GOOGLE)
                            val incompleteData by viewModel.profileIncompleteData.collectAsState()

                            LaunchedEffect(incompleteData) {
                                incompleteData?.let { data ->
                                    // Guardamos los datos parcelables en el handle de navegación para pasarlos
                                    navController.currentBackStackEntry?.savedStateHandle?.set("googleData", data.googleUserData)
                                    // Navegamos a la pantalla de completar
                                    navController.navigate("completar_perfil")
                                }
                            }

                            // OBSERVER 2: LOGIN EXITOSO (NORMAL O GOOGLE COMPLETO)
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
                                    // Esta callback es redundante si usamos el LaunchedEffect de arriba,
                                    // pero la dejamos por seguridad si la UI lo llama directamente.
                                    navController.navigate("home/${rolEnum.name}") {
                                        popUpTo("bienvenida") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- PANTALLA COMPLETAR PERFIL (GOOGLE) ---
                        composable("completar_perfil") {
                            // Recuperamos el objeto GoogleUserData enviado desde Login
                            val googleUserData = navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.get<GoogleUserData>("googleData")

                            if (googleUserData != null) {
                                PantallaCompletarPerfil(
                                    googleUserData = googleUserData,
                                    onPerfilCompletado = {
                                        // Asumimos que es Paciente por defecto al registrarse con Google
                                        navController.navigate("home/PACIENTE") {
                                            popUpTo("bienvenida") { inclusive = true }
                                        }
                                    },
                                    onCancelar = {
                                        navController.popBackStack()
                                    }
                                )
                            } else {
                                // Seguridad: Si no hay datos, volvemos atrás
                                LaunchedEffect(Unit) { navController.popBackStack() }
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
                                        popUpTo(0) { inclusive = true } // Limpia toda la pila de navegación
                                    }
                                },
                                // Defines aquí tus navegaciones internas del Home
                                onNavReservas = { /* Navegar a reservas */ },
                                onNavBebe = { /* Navegar a perfil bebé */ },
                                onNavInfo = { /* Navegar a info */ }
                            )
                        }
                    }
                }
            }
        }
    }
}