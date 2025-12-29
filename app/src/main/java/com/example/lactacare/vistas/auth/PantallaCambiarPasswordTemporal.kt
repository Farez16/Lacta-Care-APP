package com.example.lactacare.vistas.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCambiarPasswordTemporal(
    tempToken: String,
    correo: String,
    rol: String,
    onPasswordCambiada: () -> Unit,
    viewModel: CambiarPasswordTemporalViewModel = hiltViewModel()
) {
    val passwordActual by viewModel.passwordActual.collectAsState()
    val nuevaPassword by viewModel.nuevaPassword.collectAsState()
    val confirmarPassword by viewModel.confirmarPassword.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    var mostrarPasswordActual by remember { mutableStateOf(false) }
    var mostrarNuevaPassword by remember { mutableStateOf(false) }
    var mostrarConfirmarPassword by remember { mutableStateOf(false) }
    val colorPrincipal = when (rol.uppercase()) {
        "ADMINISTRADOR" -> Color(0xFFA3C9A8)
        "MEDICO", "DOCTOR" -> Color(0xFFB0C4DE)
        else -> Color(0xFFFFC0CB)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar Contraseña Temporal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF546E7A)
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
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = colorPrincipal,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Cambiar Contraseña Temporal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Por seguridad, debes cambiar tu contraseña temporal antes de continuar",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            // CAMPO CONTRASEÑA ACTUAL
            OutlinedTextField(
                value = passwordActual,
                onValueChange = { viewModel.setPasswordActual(it) },
                label = { Text("Contraseña Temporal") },
                placeholder = { Text("Ingresa tu contraseña temporal") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorPrincipal,
                    focusedLabelColor = colorPrincipal,
                    cursorColor = colorPrincipal
                ),
                singleLine = true,
                visualTransformation = if (mostrarPasswordActual)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { mostrarPasswordActual = !mostrarPasswordActual }) {
                        Icon(
                            imageVector = if (mostrarPasswordActual)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                singleLine = true,
                visualTransformation = if (mostrarNuevaPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { mostrarNuevaPassword = !mostrarNuevaPassword }) {
                        Icon(
                            imageVector = if (mostrarNuevaPassword)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            // CAMPO CONFIRMAR CONTRASEÑA
            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = { viewModel.setConfirmarPassword(it) },
                label = { Text("Confirmar Nueva Contraseña") },
                placeholder = { Text("Repite la nueva contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorPrincipal,
                    focusedLabelColor = colorPrincipal,
                    cursorColor = colorPrincipal
                ),
                singleLine = true,
                visualTransformation = if (mostrarConfirmarPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { mostrarConfirmarPassword = !mostrarConfirmarPassword }) {
                        Icon(
                            imageVector = if (mostrarConfirmarPassword)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                },
                isError = mensaje != null && !mensaje!!.contains("exitosamente")
            )
            // Mensaje de error
            if (mensaje != null && !mensaje!!.contains("exitosamente")) {
                Text(
                    text = mensaje!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Tarjeta informativa
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "📌 Requisitos de contraseña:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Mínimo 8 caracteres\n" +
                                "• Al menos una letra mayúscula\n" +
                                "• Al menos un número\n" +
                                "• Al menos un carácter especial",
                        fontSize = 13.sp,
                        color = Color(0xFFFF6F00),
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // BOTÓN CAMBIAR CONTRASEÑA
            Button(
                onClick = {
                    viewModel.cambiarPassword(
                        token = tempToken,
                        correo = correo,
                        onSuccess = onPasswordCambiada
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorPrincipal,
                    contentColor = if (rol.uppercase() == "PACIENTE")
                        Color(0xFF546E7A) else Color.White
                ),
                enabled = !isLoading &&
                        passwordActual.isNotEmpty() &&
                        nuevaPassword.isNotEmpty() &&
                        confirmarPassword.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = if (rol.uppercase() == "PACIENTE")
                            Color(0xFF546E7A) else Color.White,
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
        }
    }
}