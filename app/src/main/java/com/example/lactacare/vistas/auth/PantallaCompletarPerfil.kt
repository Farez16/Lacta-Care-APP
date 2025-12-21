package com.example.lactacare.vistas.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lactacare.datos.dto.GoogleUserData
import com.example.lactacare.dominio.model.RolUsuario

@Composable
fun PantallaCompletarPerfil(
    viewModel: AuthViewModel = viewModel(),
    googleUserData: GoogleUserData,
    rolSeleccionado: RolUsuario,
    onPerfilCompletado: () -> Unit,
    onCancelar: () -> Unit
) {
    var cedula by remember { mutableStateOf("") }
    var primerNombre by remember { mutableStateOf(googleUserData.givenName ?: "") }
    var segundoNombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf(googleUserData.familyName ?: "") }
    var segundoApellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var discapacidad by remember { mutableStateOf("Ninguna") }
    var codigoCredencial by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val loginExitoso by viewModel.loginExitoso.collectAsState()

    // Cuando se complete con éxito
    LaunchedEffect(loginExitoso) {
        if (loginExitoso) {
            onPerfilCompletado()
        }
    }

    // Tema según rol
    val colorPrincipal = when (rolSeleccionado) {
        RolUsuario.ADMINISTRADOR -> Color(0xFFA3C9A8)
        RolUsuario.DOCTOR -> Color(0xFFB0C4DE)
        RolUsuario.PACIENTE -> Color(0xFFFFC0CB)
    }

    Scaffold(
        containerColor = Color(0xFFFEFEFE)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // HEADER
            Text(
                text = "Completa tu Perfil",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF546E7A)
            )

            Text(
                text = "Registro como ${rolSeleccionado.name}",
                fontSize = 16.sp,
                color = colorPrincipal,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // IMAGEN DE PERFIL DE GOOGLE
            if (googleUserData.picture != null) {
                Card(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(50.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    AsyncImage(
                        model = googleUserData.picture,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = googleUserData.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF546E7A)
                )

                Text(
                    text = googleUserData.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // INFORMACIÓN IMPORTANTE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorPrincipal.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = colorPrincipal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (rolSeleccionado != RolUsuario.PACIENTE) {
                            "Se requiere un código de credenciales para registrarte como ${rolSeleccionado.name}"
                        } else {
                            "Completa los siguientes datos para finalizar tu registro"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF546E7A),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FORMULARIO
            Text(
                text = "Datos Personales",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorPrincipal,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoCompletarPerfil(
                label = "Cédula",
                valor = cedula,
                placeholder = "1104567890",
                icon = Icons.Outlined.Badge,
                teclado = KeyboardType.Number,
                colorFocus = colorPrincipal
            ) { cedula = it }

            // Nombres pre-llenados de Google
            CampoCompletarPerfil(
                label = "Primer Nombre *",
                valor = primerNombre,
                placeholder = "Juan",
                icon = Icons.Outlined.Person,
                colorFocus = colorPrincipal
            ) { primerNombre = it }

            CampoCompletarPerfil(
                label = "Segundo Nombre",
                valor = segundoNombre,
                placeholder = "Carlos",
                icon = Icons.Outlined.Person,
                colorFocus = colorPrincipal
            ) { segundoNombre = it }

            CampoCompletarPerfil(
                label = "Primer Apellido *",
                valor = primerApellido,
                placeholder = "Pérez",
                icon = Icons.Outlined.Person,
                colorFocus = colorPrincipal
            ) { primerApellido = it }

            CampoCompletarPerfil(
                label = "Segundo Apellido",
                valor = segundoApellido,
                placeholder = "Gómez",
                icon = Icons.Outlined.Person,
                colorFocus = colorPrincipal
            ) { segundoApellido = it }

            CampoCompletarPerfil(
                label = "Teléfono *",
                valor = telefono,
                placeholder = "0987654321",
                icon = Icons.Outlined.Phone,
                teclado = KeyboardType.Phone,
                colorFocus = colorPrincipal
            ) { telefono = it }

            CampoCompletarPerfil(
                label = "Fecha Nacimiento (YYYY-MM-DD) *",
                valor = fechaNacimiento,
                placeholder = "1990-01-15",
                icon = Icons.Outlined.DateRange,
                colorFocus = colorPrincipal
            ) { fechaNacimiento = it }

            // CAMPO DE DISCAPACIDAD (SOLO PACIENTES)
            if (rolSeleccionado == RolUsuario.PACIENTE) {
                CampoCompletarPerfil(
                    label = "Discapacidad (Opcional)",
                    valor = discapacidad,
                    placeholder = "Ninguna",
                    icon = Icons.Outlined.Accessible,
                    colorFocus = colorPrincipal
                ) { discapacidad = it }
            }

            // CÓDIGO DE CREDENCIALES (DOCTOR/ADMIN)
            if (rolSeleccionado != RolUsuario.PACIENTE) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Credenciales",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorPrincipal,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                CampoCompletarPerfil(
                    label = "Código de Credenciales *",
                    valor = codigoCredencial,
                    placeholder = if (rolSeleccionado == RolUsuario.DOCTOR) "DOCTOR2025" else "ADMIN2025",
                    icon = Icons.Outlined.Key,
                    colorFocus = colorPrincipal
                ) { codigoCredencial = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MENSAJES DE ERROR
            if (mensajeError != null) {
                Text(
                    text = mensajeError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // BOTÓN COMPLETAR
            Button(
                onClick = {
                    val request = com.example.lactacare.datos.dto.CompleteProfileRequest(
                        googleId = googleUserData.googleId,
                        cedula = cedula,
                        primerNombre = primerNombre,
                        segundoNombre = segundoNombre.ifEmpty { null },
                        primerApellido = primerApellido,
                        segundoApellido = segundoApellido.ifEmpty { null },
                        telefono = telefono.ifEmpty { null },
                        fechaNacimiento = fechaNacimiento,
                        discapacidad = if (rolSeleccionado == RolUsuario.PACIENTE) discapacidad else null,
                        codigoCredencial = if (rolSeleccionado != RolUsuario.PACIENTE) codigoCredencial else null
                    )

                    viewModel.completeProfile(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorPrincipal,
                    contentColor = if (rolSeleccionado == RolUsuario.PACIENTE) Color(0xFF546E7A) else Color.White
                ),
                enabled = !isLoading && cedula.isNotEmpty() && primerNombre.isNotEmpty() &&
                        primerApellido.isNotEmpty() && telefono.isNotEmpty() && fechaNacimiento.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Completar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN CANCELAR
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorPrincipal
                )
            ) {
                Text("Cancelar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CampoCompletarPerfil(
    label: String,
    valor: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    teclado: KeyboardType = KeyboardType.Text,
    colorFocus: Color,
    onCambio: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF546E7A),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = colorFocus,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                cursorColor = Color(0xFF546E7A),
                focusedTextColor = Color(0xFF546E7A),
                unfocusedTextColor = Color(0xFF546E7A)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            trailingIcon = {
                Icon(icon, contentDescription = null, tint = colorFocus)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}