package com.example.lactacare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Importaciones de tus paquetes
import com.example.lactacare.vistas.auth.*
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

                    // Verificación de sesión al arrancar
                    val startDestination = runBlocking {
                        val token = sessionManager.authToken.first()
                        if (!token.isNullOrEmpty()) "home_placeholder" else "bienvenida"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        // 1. BIENVENIDA
                        composable("bienvenida") {
                            PantallaBienvenida(
                                onRolSeleccionado = { rol ->
                                    navController.navigate("login/${rol.name}")
                                }
                            )
                        }

                        // 2. LOGIN (Maneja la lógica de AuthState internamente)
                        composable(
                            route = "login/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch(e: Exception) { RolUsuario.PACIENTE }

                            val viewModel: AuthViewModel = hiltViewModel()

                            LaunchedEffect(rolEnum) {
                                viewModel.setRol(rolEnum)
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
                                    navController.navigate("home_placeholder") {
                                        popUpTo("bienvenida") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. RECUPERAR PASSWORD (CORREGIDO: Pasando rolUsuario)
                        composable(
                            route = "recuperar_password/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val rolTexto = backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try { RolUsuario.valueOf(rolTexto) } catch(e: Exception) { RolUsuario.PACIENTE }

                            val viewModel: AuthViewModel = hiltViewModel()

                            PantallaRecuperarPassword(
                                rolUsuario = rolEnum, // <-- Se pasa el objeto RolUsuario requerido
                                viewModel = viewModel,
                                onVolver = { navController.popBackStack() }
                            )
                        }

                        // 4. REGISTRO
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

                        // 5. HOME TEMPORAL
                        composable("home_placeholder") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("¡Conexión Exitosa!", style = MaterialTheme.typography.headlineMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Estás logueado con el Backend real.")
                                    Button(onClick = {
                                        runBlocking { sessionManager.clearSession() }
                                        navController.navigate("bienvenida") { popUpTo(0) }
                                    }) {
                                        Text("Cerrar Sesión")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}