package com.example.lactacare.vistas.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.lactacare.datos.dto.GoogleUserData
import com.example.lactacare.ui.theme.SlateGray

// Colores fijos para Paciente
val PatientColor = Color(0xFFFFC0CB) // Rosa Pastel
val PatientColorDark = Color(0xFFF06292) // Rosa más oscuro para textos


@Composable
fun PantallaCompletarPerfil(
    viewModel: AuthViewModel = hiltViewModel(),
    googleUserData: GoogleUserData,
    onPerfilCompletado: () -> Unit,
    onCancelar: () -> Unit
) {
    // --- ESTADOS DEL FORMULARIO ---
    var cedula by remember { mutableStateOf("") }

    // Pre-llenamos con datos que vienen de Google
    var primerNombre by remember { mutableStateOf(googleUserData.givenName ?: "") }
    var segundoNombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf(googleUserData.familyName ?: "") }
    var segundoApellido by remember { mutableStateOf("") }

    var telefono by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var discapacidad by remember { mutableStateOf("Ninguna") }

    // --- ESTADOS DEL VIEWMODEL ---
    val isLoading by viewModel.isLoading.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val loginExitoso by viewModel.loginExitoso.collectAsState()

    // --- EFECTOS ---
    LaunchedEffect(loginExitoso) {
        if (loginExitoso) {
            onPerfilCompletado()
        }
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
                color = SlateGray
            )

            Text(
                text = "Registro de Paciente",
                fontSize = 16.sp,
                color = PatientColorDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TARJETA DE USUARIO GOOGLE (Visualización)
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
                    color = SlateGray
                )

                Text(
                    text = googleUserData.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // INFO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PatientColor.copy(alpha = 0.2f)
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
                        tint = PatientColorDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Confirma tus datos personales para brindarte un mejor seguimiento en tu lactancia.",
                        fontSize = 14.sp,
                        color = SlateGray,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- FORMULARIO DE DATOS ---
            Text(
                text = "Datos Personales",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PatientColorDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoCompletarPerfil(
                label = "Cédula *",
                valor = cedula,
                placeholder = "1104567890",
                icon = Icons.Outlined.Badge,
                teclado = KeyboardType.Number,
                colorFocus = PatientColorDark
            ) { cedula = it }

            CampoCompletarPerfil(
                label = "Primer Nombre *",
                valor = primerNombre,
                placeholder = "Juan",
                icon = Icons.Outlined.Person,
                colorFocus = PatientColorDark
            ) { primerNombre = it }

            CampoCompletarPerfil(
                label = "Segundo Nombre",
                valor = segundoNombre,
                placeholder = "Carlos",
                icon = Icons.Outlined.Person,
                colorFocus = PatientColorDark
            ) { segundoNombre = it }

            CampoCompletarPerfil(
                label = "Primer Apellido *",
                valor = primerApellido,
                placeholder = "Pérez",
                icon = Icons.Outlined.Person,
                colorFocus = PatientColorDark
            ) { primerApellido = it }

            CampoCompletarPerfil(
                label = "Segundo Apellido",
                valor = segundoApellido,
                placeholder = "Gómez",
                icon = Icons.Outlined.Person,
                colorFocus = PatientColorDark
            ) { segundoApellido = it }

            CampoCompletarPerfil(
                label = "Teléfono *",
                valor = telefono,
                placeholder = "0987654321",
                icon = Icons.Outlined.Phone,
                teclado = KeyboardType.Phone,
                colorFocus = PatientColorDark
            ) { telefono = it }

            CampoCompletarPerfil(
                label = "Fecha Nacimiento (YYYY-MM-DD) *",
                valor = fechaNacimiento,
                placeholder = "1990-01-15",
                icon = Icons.Outlined.DateRange,
                colorFocus = PatientColorDark
            ) { fechaNacimiento = it }

            CampoCompletarPerfil(
                label = "Discapacidad (Opcional)",
                valor = discapacidad,
                placeholder = "Ninguna",
                icon = Icons.Outlined.Accessible,
                colorFocus = PatientColorDark
            ) { discapacidad = it }

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
                        discapacidad = discapacidad,
                    )
                    viewModel.completeProfile(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PatientColor,
                    contentColor = SlateGray
                ),
                enabled = !isLoading && cedula.isNotEmpty() && primerNombre.isNotEmpty() &&
                        primerApellido.isNotEmpty() && telefono.isNotEmpty() && fechaNacimiento.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = SlateGray,
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
                    contentColor = SlateGray
                ),
                border = BorderStroke(1.dp, SlateGray.copy(alpha = 0.5f))
            ) {
                Text("Cancelar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// COMPONENTE AUXILIAR
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
            color = SlateGray,
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
                cursorColor = SlateGray,
                focusedTextColor = SlateGray,
                unfocusedTextColor = SlateGray
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