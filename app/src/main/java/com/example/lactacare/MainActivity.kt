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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lactacare.datos.dto.GoogleUserData
import com.example.lactacare.datos.dto.AuthState
import com.example.lactacare.vistas.auth.*
import com.example.lactacare.vistas.home.PantallaHome
import com.example.lactacare.vistas.admin.usuarios.PantallaGestionUsuarios
import com.example.lactacare.vistas.admin.usuarios.PantallaCrearEmpleado
import com.example.lactacare.vistas.doctor.atencion.PantallaAtencion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.datos.local.SessionManager
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween

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
                    val recuperarPasswordViewModel: RecuperarPasswordViewModel = hiltViewModel()

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
                        startDestination = rutaInicial,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(500)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(500)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(500)
                            )
                        }
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
                            arguments = listOf(navArgument("rolTexto") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val rolTexto =
                                backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try {
                                RolUsuario.valueOf(rolTexto)
                            } catch (e: Exception) {
                                RolUsuario.PACIENTE
                            }
                            val viewModel: AuthViewModel = hiltViewModel()
                            LaunchedEffect(rolEnum) { viewModel.setRol(rolEnum) }
                            // Google Login Incompleto
                            val incompleteData by viewModel.profileIncompleteData.collectAsState()
                            LaunchedEffect(incompleteData) {
                                incompleteData?.let { data ->
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "googleData",
                                        data.googleUserData
                                    )
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
                            // ⭐ NUEVO: Observar estado de cambio de contraseña temporal
                            val authState by viewModel.authState.collectAsState()
                            LaunchedEffect(authState) {
                                when (val state = authState) {
                                    is AuthState.PasswordChangeRequired -> {
                                        navController.navigate(
                                            "cambiar_password_temporal/${state.tempToken}/${state.correo}/${state.rol}"
                                        ) {
                                            popUpTo("login/$rolTexto") { inclusive = false }
                                        }
                                    }

                                    else -> {}
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
                                            launchSingleTop =
                                                true // Evita duplicar la pantalla si se clica rápido
                                        }
                                    },
                                    onCancelar = {
                                        navController.popBackStack()
                                    }
                                )
                            } else {
                                // Si los datos son nulos, mostramos carga un momento antes de salir
                                // Esto evita el parpadeo blanco instantáneo
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                            arguments = listOf(navArgument("rolTexto") {
                                type = NavType.StringType
                            })
                        ) {
                            val viewModel: RegistroPersonaViewModel = hiltViewModel()
                            PantallaRegistroPersona(
                                viewModel = viewModel,
                                onIrALogin = { navController.popBackStack() }
                            )
                        }
                        // --- PANTALLA RECUPERAR PASSWORD ---
                        // Pantalla 1: Solicitar Código
                        composable(
                            route = "recuperar_password/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val rolTexto =
                                backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try {
                                RolUsuario.valueOf(rolTexto)
                            } catch (e: Exception) {
                                RolUsuario.PACIENTE
                            }
                            LaunchedEffect(rolEnum) {
                                recuperarPasswordViewModel.setRol(rolEnum)
                            }
                            PantallaSolicitarCodigo(
                                rolUsuario = rolEnum,
                                viewModel = recuperarPasswordViewModel,
                                onVolver = { navController.popBackStack() },
                                onCodigoEnviado = { correo ->
                                    navController.navigate("verificar_codigo/$correo/$rolTexto")
                                }
                            )
                        }

                        // Pantalla 2: Verificar Código
                        composable(
                            route = "verificar_codigo/{correo}/{rolTexto}",
                            arguments = listOf(
                                navArgument("correo") { type = NavType.StringType },
                                navArgument("rolTexto") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val correo = backStackEntry.arguments?.getString("correo") ?: ""
                            val rolTexto =
                                backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try {
                                RolUsuario.valueOf(rolTexto)
                            } catch (e: Exception) {
                                RolUsuario.PACIENTE
                            }
                            PantallaVerificarCodigo(
                                correo = correo,
                                rolUsuario = rolEnum,
                                viewModel = recuperarPasswordViewModel,
                                onVolver = { navController.popBackStack() },
                                onCodigoVerificado = {
                                    navController.navigate("nueva_password/$rolTexto") {
                                        popUpTo("verificar_codigo/$correo/$rolTexto") {
                                            inclusive = true
                                        }
                                    }
                                },
                                onReenviarCodigo = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Pantalla 3: Nueva Contraseña
                        composable(
                            route = "nueva_password/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val rolTexto =
                                backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try {
                                RolUsuario.valueOf(rolTexto)
                            } catch (e: Exception) {
                                RolUsuario.PACIENTE
                            }
                            PantallaNuevaPassword(
                                rolUsuario = rolEnum,
                                viewModel = recuperarPasswordViewModel,
                                onVolver = { navController.popBackStack() },
                                onPasswordCambiada = {
                                    // Resetear el ViewModel antes de volver al login
                                    recuperarPasswordViewModel.resetState()
                                    navController.navigate("login/$rolTexto") {
                                        popUpTo("login/$rolTexto") { inclusive = true }
                                    }
                                }

                            )
                        }
                        // --- PANTALLA CAMBIAR CONTRASEÑA TEMPORAL ---
                        composable(
                            route = "cambiar_password_temporal/{token}/{correo}/{rol}",
                            arguments = listOf(
                                navArgument("token") { type = NavType.StringType },
                                navArgument("correo") { type = NavType.StringType },
                                navArgument("rol") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            val correo = backStackEntry.arguments?.getString("correo") ?: ""
                            val rol = backStackEntry.arguments?.getString("rol") ?: "MEDICO"

                            PantallaCambiarPasswordTemporal(
                                tempToken = token,
                                correo = correo,
                                rol = rol,
                                onPasswordCambiada = {
                                    // Volver al login después de cambiar contraseña
                                    navController.navigate("login/$rol") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        // --- PANTALLA HOME (PRINCIPAL) ---
                        composable(
                            route = "home/{rolTexto}",
                            arguments = listOf(navArgument("rolTexto") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val rolTexto =
                                backStackEntry.arguments?.getString("rolTexto") ?: "PACIENTE"
                            val rolEnum = try {
                                RolUsuario.valueOf(rolTexto)
                            } catch (e: Exception) {
                                RolUsuario.PACIENTE
                            }
                            PantallaHome(
                                rolUsuario = rolEnum,
                                onLogout = {
                                    navController.navigate("bienvenida") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavGestion = {
                                    navController.navigate("gestion_usuarios")
                                },
                                onNavAtencion = { idReserva, nombrePaciente ->
                                    navController.navigate("atencion_doctor/$idReserva/$nombrePaciente")
                                },
                                onNavReservas = { /* Navegar a reservas */ },
                                onNavBebe = { /* Navegar a perfil bebé */ },
                                onNavInfo = { /* Navegar a info */ },
                                onNavReportes = { navController.navigate("admin_reporte") }
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
                            PantallaCrearEmpleado(
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

                            PantallaAtencion(
                                idReserva = id,
                                onVolver = { navController.popBackStack() },
                                onContinuar = { reservaId, contenedores ->
                                    // TODO: Navegar a selección de ubicación
                                }
                            )
                        }
                        // --- PANTALLA: REPORTES ADMIN ---
                        composable("admin_reporte") {
                            com.example.lactacare.vistas.admin.reportes.PantallaReportesAdmin(
                                onVolver = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}