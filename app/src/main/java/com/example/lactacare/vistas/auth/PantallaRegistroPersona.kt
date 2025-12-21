package com.example.lactacare.vistas.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lactacare.dominio.model.RolUsuario
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.LocalDate // IMPORTADO
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// --- COLORES ESTILO CLEAN ---
val TextoOscuroClean = Color(0xFF546E7A)
val BordeGrisClean = Color(0xFFE0E0E0)
val FondoBlancoClean = Color(0xFFFEFEFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroPersona(
    viewModel: RegistroPersonaViewModel = viewModel(
        factory = RegistroPersonaViewModel.Factory(LocalContext.current)
    ),
    onIrALogin: () -> Unit
) {
    // Datos del ViewModel
    val datos by viewModel.uiState.collectAsState()
    val rol by viewModel.rolActual.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val registroExitoso by viewModel.registroExitoso.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // --- ESTADOS DEL CALENDARIO ---
    var mostrarCalendario by remember { mutableStateOf(false) }
    var errorEdad by remember { mutableStateOf<String?>(null) }

    // Fecha máxima permitida (hoy - 18 años)
    val fechaMaxima = remember {
        LocalDate.now().minusYears(18)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // Estado del DatePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = datos.fechaNacimiento.takeIf { it.isNotBlank() }?.let {
            try {
                LocalDate.parse(it)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } catch (e: Exception) { fechaMaxima }
        } ?: fechaMaxima,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= fechaMaxima
            }
        }
    )

    // --- DIÁLOGO DEL CALENDARIO ---
    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val fechaSeleccionada = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        if (esMayorDeEdad(fechaSeleccionada)) {
                            val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            viewModel.onFechaNacimientoChange(
                                fechaSeleccionada.format(formato)
                            )
                            errorEdad = null
                            mostrarCalendario = false
                        } else {
                            errorEdad = "Debes tener al menos 18 años para registrarte"
                        }
                    }
                }) {
                    Text("OK", color = Color(0xFF546E7A)) // Ajustado temporalmente para evitar error si 'tema' no se ha declarado
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            kotlinx.coroutines.delay(1000)
            onIrALogin()
        }
    }

    // --- 1. CONFIGURACIÓN DE TEMA SEGÚN ROL ---
    data class TemaRegistro(
        val colorPrincipal: Color,
        val tituloRol: String,
        val icono: ImageVector
    )

    val tema = when (rol) {
        RolUsuario.ADMINISTRADOR -> TemaRegistro(
            colorPrincipal = Color(0xFFA3C9A8),
            tituloRol = "Administrativo",
            icono = Icons.Default.Apartment
        )
        RolUsuario.DOCTOR -> TemaRegistro(
            colorPrincipal = Color(0xFFB0C4DE),
            tituloRol = "Médico",
            icono = Icons.Default.HealthAndSafety
        )
        RolUsuario.PACIENTE -> TemaRegistro(
            colorPrincipal = Color(0xFFFFC0CB),
            tituloRol = "Paciente",
            icono = Icons.Default.ChildCare
        )
    }

    Scaffold(
        containerColor = FondoBlancoClean
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

            Text(
                text = "Crear Cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuroClean
            )
            Text(
                text = "Registro de ${tema.tituloRol}",
                fontSize = 16.sp,
                color = tema.colorPrincipal,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SWITCH (Registrarse Activo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BordeGrisClean.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onIrALogin() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold, color = TextoOscuroClean.copy(alpha = 0.6f))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .shadow(2.dp, RoundedCornerShape(8.dp))
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Registrarse", fontWeight = FontWeight.Bold, color = TextoOscuroClean)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            SeccionFormulario("Datos Personales", tema.colorPrincipal)

            CampoRegistroClean(label = "Cédula / DNI", valor = datos.cedula, placeholder = "110...", icon = Icons.Outlined.Badge, teclado = KeyboardType.Number, colorFocus = tema.colorPrincipal) { viewModel.onCedulaChange(it) }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Primer Nombre", valor = datos.primerNombre, placeholder = "Juan", icon = Icons.Outlined.Person, colorFocus = tema.colorPrincipal) { viewModel.onPrimerNombreChange(it) }
                }
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Segundo Nombre", valor = datos.segundoNombre, placeholder = "Carlos", icon = Icons.Outlined.Person, colorFocus = tema.colorPrincipal) { viewModel.onSegundoNombreChange(it) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Primer Apellido", valor = datos.primerApellido, placeholder = "Pérez", icon = Icons.Outlined.Person, colorFocus = tema.colorPrincipal) { viewModel.onPrimerApellidoChange(it) }
                }
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Segundo Apellido", valor = datos.segundoApellido, placeholder = "Gómez", icon = Icons.Outlined.Person, colorFocus = tema.colorPrincipal) { viewModel.onSegundoApellidoChange(it) }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                CampoRegistroCleanReadOnly(
                    label = "Fecha Nacimiento",
                    valor = datos.fechaNacimiento,
                    placeholder = "Seleccione su fecha",
                    icon = Icons.Outlined.DateRange,
                    colorFocus = tema.colorPrincipal,
                    onClick = { mostrarCalendario = true }
                )
                if (errorEdad != null) {
                    Text(
                        text = errorEdad!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BordeGrisClean) // Corregido
            Spacer(modifier = Modifier.height(16.dp))

            when (rol) {
                RolUsuario.DOCTOR -> {
                    SeccionFormulario("Credenciales Médicas", tema.colorPrincipal)
                    CampoRegistroClean(label = "Codigo Médico", valor = datos.licenciaMedica, placeholder = "Nro. Licencia", icon = Icons.Outlined.HealthAndSafety, colorFocus = tema.colorPrincipal) { viewModel.onLicenciaChange(it) }
                }
                RolUsuario.ADMINISTRADOR -> {
                    SeccionFormulario("Creedenciales Administrativas", tema.colorPrincipal)
                    CampoRegistroClean(label = "Código Empleado", valor = datos.codigoEmpleado, placeholder = "ADM-001", icon = Icons.Outlined.Badge, colorFocus = tema.colorPrincipal) { viewModel.onCodigoEmpleadoChange(it) }

                }
                RolUsuario.PACIENTE -> {
                    SeccionFormulario("Información Adicional", tema.colorPrincipal)
                    CampoRegistroClean(label = "Discapacidad (Opcional)", valor = datos.discapacidad, placeholder = "Detalle si aplica", icon = Icons.Outlined.Accessible, colorFocus = tema.colorPrincipal) { viewModel.onDiscapacidadChange(it) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BordeGrisClean) // Corregido
            Spacer(modifier = Modifier.height(16.dp))

            SeccionFormulario("Cuenta", tema.colorPrincipal)

            CampoRegistroClean(label = "Correo Electrónico", valor = datos.correo, placeholder = "tu@email.com", icon = Icons.Outlined.Email, teclado = KeyboardType.Email, colorFocus = tema.colorPrincipal) { viewModel.onCorreoChange(it) }

            CampoRegistroClean(label = "Teléfono", valor = datos.telefono, placeholder = "0987654321", icon = Icons.Outlined.Phone, teclado = KeyboardType.Phone, colorFocus = tema.colorPrincipal) { viewModel.onTelefonoChange(it) }

            Text(
                text = "Contraseña",
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuroClean,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = datos.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = { Text("Mínimo 8 caracteres", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = inputColorsClean(tema.colorPrincipal),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    val imagen = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imagen, contentDescription = null, tint = tema.colorPrincipal)
                    }
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            if (mensaje != null) {
                Text(
                    text = mensaje ?: "",
                    color = if (registroExitoso) tema.colorPrincipal else Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (registroExitoso) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = { viewModel.registrar() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tema.colorPrincipal,
                    contentColor = if (rol == RolUsuario.PACIENTE) TextoOscuroClean else Color.White
                ),
                enabled = !cargando
            ) {
                if (cargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Confirmar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val textoLegal = buildAnnotatedString {
                append("Al registrarte aceptas los ")
                withStyle(SpanStyle(color = tema.colorPrincipal, fontWeight = FontWeight.Bold)) { append("Términos") }
                append(" y ")
                withStyle(SpanStyle(color = tema.colorPrincipal, fontWeight = FontWeight.Bold)) { append("Política de Privacidad") }
            }
            Text(text = textoLegal, fontSize = 11.sp, color = TextoOscuroClean.copy(alpha = 0.7f), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

fun esMayorDeEdad(fechaNacimiento: LocalDate): Boolean {
    val hoy = LocalDate.now()
    return fechaNacimiento.plusYears(18).isBefore(hoy) ||
            fechaNacimiento.plusYears(18).isEqual(hoy)
}

@Composable
fun SeccionFormulario(titulo: String, color: Color) {
    Text(
        text = titulo,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )
}

@Composable
fun CampoRegistroCleanReadOnly(
    label: String,
    valor: String,
    placeholder: String,
    icon: ImageVector,
    colorFocus: Color,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = TextoOscuroClean,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = { Text(placeholder, color = TextoOscuroClean.copy(alpha = 0.4f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = BordeGrisClean,
                disabledTextColor = TextoOscuroClean,
                disabledPlaceholderColor = TextoOscuroClean.copy(alpha = 0.4f),
                disabledTrailingIconColor = colorFocus,
                disabledLabelColor = TextoOscuroClean
            ),
            singleLine = true,
            trailingIcon = { Icon(icon, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CampoRegistroClean(
    label: String,
    valor: String,
    placeholder: String,
    icon: ImageVector,
    teclado: KeyboardType = KeyboardType.Text,
    colorFocus: Color,
    onCambio: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = TextoOscuroClean,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = { Text(placeholder, color = TextoOscuroClean.copy(alpha = 0.4f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = inputColorsClean(colorFocus),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            trailingIcon = { Icon(icon, contentDescription = null, tint = colorFocus) }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun inputColorsClean(colorFocus: Color) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = colorFocus,
    unfocusedBorderColor = BordeGrisClean,
    cursorColor = TextoOscuroClean,
    focusedTextColor = TextoOscuroClean,
    unfocusedTextColor = TextoOscuroClean
)