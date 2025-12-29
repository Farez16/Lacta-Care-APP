package com.example.lactacare.vistas.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.AuthState
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.theme.*
import com.example.lactacare.vistas.components.ErrorDialog
import com.example.lactacare.vistas.components.ErrorDialogData
import com.example.lactacare.vistas.components.ErrorType

@Composable
fun PantallaLogin(
    viewModel: AuthViewModel = hiltViewModel(),
    onIrARegistro: (RolUsuario) -> Unit,
    onLoginExitoso: () -> Unit,
    onIrARecuperarPassword: () -> Unit
) {
    // 1. OBSERVABLES
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val rol by viewModel.rolActual.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val profileIncompleteData by viewModel.profileIncompleteData.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showCompletarPerfil by remember { mutableStateOf(false) }

    // Estados para controlar diálogos
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogData by remember { mutableStateOf<ErrorDialogData?>(null) }
    // 2. GOOGLE LAUNCHER
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data)
        }
    }
    // 3. EFECTO DE NAVEGACIÓN Y MANEJO DE ERRORES
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> onLoginExitoso()
            is AuthState.ProfileIncomplete -> showCompletarPerfil = true

            is AuthState.RolMismatch -> {
                errorDialogData = ErrorDialogData(
                    tipo = ErrorType.ROL_MISMATCH,
                    titulo = "Rol Incorrecto",
                    mensaje = state.mensaje,
                    rolCorrecto = state.rolCorrecto
                )
                showErrorDialog = true
            }
            is AuthState.UnauthorizedEmail -> {
                errorDialogData = ErrorDialogData(
                    tipo = ErrorType.UNAUTHORIZED_EMAIL,
                    titulo = "Correo No Autorizado",
                    mensaje = state.mensaje
                )
                showErrorDialog = true
            }
            is AuthState.UserNotFound -> {
                errorDialogData = ErrorDialogData(
                    tipo = ErrorType.USER_NOT_FOUND,
                    titulo = "Usuario No Encontrado",
                    mensaje = state.mensaje
                )
                showErrorDialog = true
            }
            is AuthState.InvalidCredentials -> {
                errorDialogData = ErrorDialogData(
                    tipo = ErrorType.INVALID_CREDENTIALS,
                    titulo = "Credenciales Incorrectas",
                    mensaje = state.mensaje
                )
                showErrorDialog = true
            }
            is AuthState.GenericError -> {
                errorDialogData = ErrorDialogData(
                    tipo = ErrorType.GENERIC,
                    titulo = "Error",
                    mensaje = state.mensaje
                )
                showErrorDialog = true
            }
            else -> {}
        }
    }
    // 4. MANEJO DE PERFIL INCOMPLETO (GOOGLE)
    if (showCompletarPerfil && profileIncompleteData != null) {
        PantallaCompletarPerfil(
            viewModel = viewModel,
            googleUserData = profileIncompleteData!!.googleUserData,
            onPerfilCompletado = {
                showCompletarPerfil = false
                onLoginExitoso()
            },
            onCancelar = {
                showCompletarPerfil = false
                viewModel.logout()
            }
        )
        return
    }
    // 5. CONFIGURACIÓN VISUAL SEGÚN ROL
    data class TemaVisual(
        val titulo: String,
        val icono: ImageVector,
        val colorPrimario: Color,
        val colorFondo: Color,
        val colorIconoBg: Color,
        val colorTexto: Color
    )
    val tema = when (rol) {
        RolUsuario.PACIENTE -> TemaVisual(
            titulo = "Bienvenida",
            icono = Icons.Default.ChildCare,
            colorPrimario = MomPrimary,
            colorFondo = MomBackground,
            colorIconoBg = MomAccent,
            colorTexto = SlateGray
        )
        RolUsuario.MEDICO -> TemaVisual(
            titulo = "Portal Médicos",
            icono = Icons.Default.HealthAndSafety,
            colorPrimario = DoctorPrimary,
            colorFondo = DoctorBackground,
            colorIconoBg = Color(0xFFBBDEFB),
            colorTexto = SlateGray
        )
        RolUsuario.ADMINISTRADOR -> TemaVisual(
            titulo = "Administración",
            icono = Icons.Default.Apartment,
            colorPrimario = AdminPrimary,
            colorFondo = AdminBackground,
            colorIconoBg = Color(0xFFC8E6C9),
            colorTexto = SlateGray
        )
    }
    // 6. UI PRINCIPAL
    Scaffold(containerColor = tema.colorFondo) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "LactaCare",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = tema.colorTexto
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(tema.colorIconoBg, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tema.icono,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    tint = tema.colorPrimario
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = tema.titulo,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = tema.colorTexto
            )
            Spacer(modifier = Modifier.height(32.dp))
            // Email
            CampoTextoLogin(
                label = "Correo Electrónico",
                valor = email,
                placeholder = "ejemplo@correo.com",
                colorFocus = tema.colorPrimario,
                onCambio = { viewModel.onEmailChange(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Contraseña",
                    fontWeight = FontWeight.SemiBold,
                    color = SlateGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tema.colorPrimario,
                        unfocusedBorderColor = BordeGrisClean,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null, tint = SlateGray)
                        }
                    },
                    singleLine = true
                )
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onIrARecuperarPassword) {
                    Text("¿Olvidaste tu contraseña?", color = SlateGray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // BOTÓN LOGIN
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tema.colorPrimario,
                    contentColor = White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // BOTÓN GOOGLE
            OutlinedButton(
                onClick = { googleSignInLauncher.launch(viewModel.getGoogleSignInIntent()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BordeGrisClean),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateGray)
            ) {
                Text("Continuar con Google", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (rol == RolUsuario.PACIENTE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿No tienes cuenta? ", color = SlateGray)
                    Text(
                        text = "Regístrate aquí",
                        color = tema.colorPrimario,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onIrARegistro(rol) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    // 7. DIÁLOGO UNIFICADO DE ERRORES (USANDO COMPONENTE COMPARTIDO)
    if (showErrorDialog && errorDialogData != null) {
        ErrorDialog(
            data = errorDialogData!!,
            onDismiss = {
                showErrorDialog = false
                errorDialogData = null
                viewModel.resetLoginState()
            }
        )
    }
}
@Composable
fun CampoTextoLogin(
    label: String,
    valor: String,
    placeholder: String,
    colorFocus: Color,
    onCambio: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = SlateGray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorFocus,
                unfocusedBorderColor = BordeGrisClean,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                cursorColor = colorFocus
            ),
            singleLine = true
        )
    }
}