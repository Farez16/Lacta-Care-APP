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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // <--- IMPORTANTE: HILT
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.datos.dto.AuthState

// COLORES EXTRAÍDOS
val SlateGray = Color(0xFF546E7A)
val LightGray = Color(0xFFE0E0E0)
val OffWhite = Color(0xFFFEFEFE)
val White = Color.White

@Composable
fun PantallaLogin(
    // --- CORRECCIÓN CLAVE: Usamos hiltViewModel() ---
    viewModel: AuthViewModel = hiltViewModel(),
    // -----------------------------------------------
    onIrARegistro: (RolUsuario) -> Unit,
    onLoginExitoso: () -> Unit,
    onIrARecuperarPassword: () -> Unit
) {
    // Observamos el estado del ViewModel
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.mensajeError.collectAsState()
    val loginExitoso by viewModel.loginExitoso.collectAsState()
    val rol by viewModel.rolActual.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val profileIncompleteData by viewModel.profileIncompleteData.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var showCompletarPerfil by remember { mutableStateOf(false) }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data)
        }
    }

    // Reaccionar a cambios de estado
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onLoginExitoso()
            }
            is AuthState.ProfileIncomplete -> {
                showCompletarPerfil = true
            }
            else -> {}
        }
    }

    // Mostrar pantalla de completar perfil si es necesario (Google flow)
    if (showCompletarPerfil && profileIncompleteData != null) {
        PantallaCompletarPerfil(
            viewModel = viewModel,
            googleUserData = profileIncompleteData!!.googleUserData,
            rolSeleccionado = rol,
            onPerfilCompletado = {
                showCompletarPerfil = false
                onLoginExitoso()
            },
            onCancelar = {
                showCompletarPerfil = false
                viewModel.logout()
            }
        )
        return // Salimos para no dibujar el login debajo
    }

    // Login normal exitoso
    LaunchedEffect(loginExitoso) {
        if (loginExitoso) {
            onLoginExitoso()
        }
    }

    // --- CONFIGURACIÓN DE TEMA SEGÚN ROL ---
    data class TemaRol(
        val titulo: String,
        val icono: ImageVector,
        val colorPrincipal: Color,
        val colorAcento: Color,
        val colorIcono: Color,
        val colorFondoPantalla: Color = OffWhite
    )

    val tema = when (rol) {
        RolUsuario.ADMINISTRADOR -> TemaRol(
            titulo = "Portal Administrativo",
            icono = Icons.Default.Apartment,
            colorPrincipal = Color(0xFFA3C9A8),
            colorAcento = Color(0xFFA3C9A8).copy(alpha = 0.3f),
            colorIcono = Color(0xFF2E7D32)
        )
        RolUsuario.DOCTOR -> TemaRol(
            titulo = "Portal Médicos",
            icono = Icons.Default.HealthAndSafety,
            colorPrincipal = Color(0xFFB0C4DE),
            colorAcento = Color(0xFFB0C4DE).copy(alpha = 0.3f),
            colorIcono = Color(0xFF1565C0)
        )
        RolUsuario.PACIENTE -> TemaRol(
            titulo = "Bienvenido",
            icono = Icons.Default.ChildCare,
            colorPrincipal = Color(0xFFFFC0CB),
            colorAcento = Color(0xFFFFDDE2),
            colorIcono = SlateGray
        )
    }

    // --- INTERFAZ DE USUARIO ---
    Scaffold(
        containerColor = tema.colorFondoPantalla
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // HEADER: Nombre App
            Text(
                text = "LactaCare",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = SlateGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // ICONO CENTRAL CAMBIANTE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .aspectRatio(1f)
                    .background(tema.colorAcento, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tema.icono,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = tema.colorIcono
                )
            }

            // TÍTULO SEGÚN ROL
            Text(
                text = tema.titulo,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SlateGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
            )

            Text(
                text = "Cuidado inteligente, lactancia tranquila.",
                fontSize = 16.sp,
                color = SlateGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // SWITCH DE ROL / MODO (REGISTRO vs LOGIN)
            // Aquí usamos el switch para cambiar entre "Iniciar Sesión" y navegar a "Registrarse"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGray.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Opción Login (Activa)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(1.dp, RoundedCornerShape(8.dp))
                            .background(White, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold, color = SlateGray)
                    }
                    // Opción Registro (Botón)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onIrARegistro(rol) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Registrarse", fontWeight = FontWeight.SemiBold, color = SlateGray.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CAMPOS DE TEXTO ---

            // EMAIL
            CampoLoginHtml(
                label = "Email",
                valor = email,
                placeholder = "tu.email@ejemplo.com",
                onChange = { viewModel.onEmailChange(it) },
                colorFocus = tema.colorPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PASSWORD
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Contraseña",
                    fontWeight = FontWeight.SemiBold,
                    color = SlateGray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    placeholder = { Text("Tu contraseña", color = SlateGray.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = inputHtmlColors(tema.colorPrincipal),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null, tint = SlateGray.copy(alpha = 0.5f))
                        }
                    }
                )
            }

            // RECUPERAR PASSWORD
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = tema.colorPrincipal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onIrARecuperarPassword() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MENSAJES DE ERROR
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        error!!,
                        color = Color(0xFFC62828),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 18.sp
                    )
                }
            }

            // BOTÓN LOGIN
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tema.colorPrincipal,
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

            Spacer(modifier = Modifier.height(24.dp))

            // DIVISOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightGray)
                Text("O", fontSize = 14.sp, color = SlateGray.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = LightGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN GOOGLE
            OutlinedButton(
                onClick = {
                    try {
                        val signInIntent = viewModel.getGoogleSignInIntent()
                        googleSignInLauncher.launch(signInIntent)
                    } catch (e: Exception) {
                        // Manejo de error si no está configurado Google
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, LightGray),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = White),
                enabled = !isLoading
            ) {
                Text("G ", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continuar con Google", color = SlateGray, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FOOTER LEGAL
            val footerText = buildAnnotatedString {
                append("Al continuar, aceptas nuestra ")
                withStyle(SpanStyle(color = tema.colorPrincipal, fontWeight = FontWeight.SemiBold)) {
                    append("Política de Privacidad")
                }
                append(" y ")
                withStyle(SpanStyle(color = tema.colorPrincipal, fontWeight = FontWeight.SemiBold)) {
                    append("Términos de Servicio")
                }
                append(".")
            }
            Text(
                text = footerText,
                fontSize = 12.sp,
                color = SlateGray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// FUNCIONES AUXILIARES DE DISEÑO
@Composable
fun CampoLoginHtml(
    label: String,
    valor: String,
    placeholder: String,
    onChange: (String) -> Unit,
    colorFocus: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = SlateGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = SlateGray.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = inputHtmlColors(colorFocus),
            singleLine = true
        )
    }
}

@Composable
fun inputHtmlColors(colorFocus: Color) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    focusedBorderColor = colorFocus.copy(alpha = 0.5f),
    unfocusedBorderColor = LightGray,
    cursorColor = SlateGray,
    focusedTextColor = SlateGray,
    unfocusedTextColor = SlateGray
)