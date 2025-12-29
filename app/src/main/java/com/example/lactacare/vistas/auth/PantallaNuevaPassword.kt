package com.example.lactacare.vistas.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.dominio.model.RolUsuario
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNuevaPassword(
    rolUsuario: RolUsuario,
    onVolver: () -> Unit,
    onPasswordCambiada: () -> Unit,
    viewModel: RecuperarPasswordViewModel = hiltViewModel()
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    var passwordCambiada by remember { mutableStateOf(false) }
    val nuevaPassword by viewModel.nuevaPassword.collectAsState()
    val confirmarPassword by viewModel.confirmarPassword.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    // Establecer rol en el ViewModel
    LaunchedEffect(rolUsuario) {
        viewModel.setRol(rolUsuario)
    }
    // Navegar automáticamente después de cambiar contraseña
    LaunchedEffect(passwordCambiada) {
        if (passwordCambiada) {
            delay(3000) // Esperar 3 segundos
            onPasswordCambiada()
        }
    }
    // Colores según rol
    val colorPrincipal = when (rolUsuario) {
        RolUsuario.ADMINISTRADOR -> Color(0xFFA3C9A8)
        RolUsuario.MEDICO -> Color(0xFFB0C4DE)
        RolUsuario.PACIENTE -> Color(0xFFFFC0CB)
    }
    // Validaciones
    val passwordsCoinciden = nuevaPassword == confirmarPassword && nuevaPassword.isNotEmpty()
    val passwordValida = nuevaPassword.length >= 8
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Contraseña") },
                navigationIcon = {
                    if (!passwordCambiada) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF546E7A),
                    navigationIconContentColor = Color(0xFF546E7A)
                )
            )
        },
        containerColor = Color(0xFFFEFEFE)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!passwordCambiada) {
                // FORMULARIO DE NUEVA CONTRASEÑA
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = colorPrincipal,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Crear Nueva Contraseña",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF546E7A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tu nueva contraseña debe tener al menos 8 caracteres",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                // CAMPO NUEVA CONTRASEÑA
                OutlinedTextField(
                    value = nuevaPassword,
                    onValueChange = { viewModel.setNuevaPassword(it) },
                    label = { Text("Nueva Contraseña") },
                    placeholder = { Text("Mínimo 8 caracteres") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorPrincipal,
                        focusedLabelColor = colorPrincipal,
                        cursorColor = colorPrincipal
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                // CAMPO CONFIRMAR CONTRASEÑA
                OutlinedTextField(
                    value = confirmarPassword,
                    onValueChange = { viewModel.setConfirmarPassword(it) },
                    label = { Text("Confirmar Contraseña") },
                    placeholder = { Text("Repite la contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorPrincipal,
                        focusedLabelColor = colorPrincipal,
                        cursorColor = colorPrincipal
                    ),
                    visualTransformation = if (confirmarPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmarPasswordVisible = !confirmarPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmarPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (confirmarPasswordVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    singleLine = true,
                    isError = confirmarPassword.isNotEmpty() && !passwordsCoinciden
                )
                if (confirmarPassword.isNotEmpty() && !passwordsCoinciden) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // INDICADOR DE FORTALEZA
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (passwordValida) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (passwordValida) "✓" else "ℹ️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                if (passwordValida) "Contraseña válida" else "Requisitos de contraseña",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (passwordValida) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                            Text(
                                "• Mínimo 8 caracteres",
                                fontSize = 12.sp,
                                color = if (nuevaPassword.length >= 8) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }
                }
                if (mensaje != null && !mensaje!!.contains("exitosamente")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = mensaje!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // BOTÓN CAMBIAR CONTRASEÑA
                Button(
                    onClick = {
                        viewModel.cambiarPassword(
                            onSuccess = { passwordCambiada = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorPrincipal,
                        contentColor = if (rolUsuario == RolUsuario.PACIENTE) Color(0xFF546E7A) else Color.White
                    ),
                    enabled = !isLoading && passwordsCoinciden && passwordValida
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = if (rolUsuario == RolUsuario.PACIENTE) Color(0xFF546E7A) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Cambiar Contraseña",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Cancelar",
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // PANTALLA DE ÉXITO
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "¡Contraseña Actualizada!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF546E7A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tu contraseña ha sido cambiada exitosamente",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "✓ Ya puedes iniciar sesión con tu nueva contraseña",
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Serás redirigido al login en 3 segundos...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onPasswordCambiada,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorPrincipal,
                        contentColor = if (rolUsuario == RolUsuario.PACIENTE) Color(0xFF546E7A) else Color.White
                    )
                ) {
                    Text(
                        "Ir al Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

