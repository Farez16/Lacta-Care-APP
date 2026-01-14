package com.example.lactacare.vistas.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
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
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.ValidationUtils

// --- COLORES ESTILO CLEAN ---
val TextoOscuroClean = Color(0xFF546E7A)
val BordeGrisClean = Color(0xFFE0E0E0)
val FondoBlancoClean = Color(0xFFFEFEFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroPersona(
    viewModel: RegistroPersonaViewModel = hiltViewModel(), // Usamos hiltViewModel()
    onIrALogin: () -> Unit
) {
    // Datos del ViewModel
    val datos by viewModel.uiState.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val registroExitoso by viewModel.registroExitoso.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // --- COLOR FIJO PARA PACIENTE (Rosado Suave) ---
    val colorPrincipal = Color(0xFFFFC0CB)

    // --- LÓGICA DE FECHAS SEGURA ---
    var mostrarCalendario by remember { mutableStateOf(false) }
    var errorEdad by remember { mutableStateOf<String?>(null) }

    // Fecha máxima permitida (hoy - 18 años)
    val fechaMaximaMillis = remember {
        LocalDate.now().minusYears(18)
            .atStartOfDay(ZoneOffset.UTC) // Usamos UTC para el DatePicker
            .toInstant()
            .toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = datos.fechaNacimiento.takeIf { it.isNotBlank() }?.let {
            try {
                LocalDate.parse(it)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            } catch (e: Exception) { fechaMaximaMillis }
        } ?: fechaMaximaMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= fechaMaximaMillis
            }
        }
    )

    // Diálogo del Calendario
    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convertimos usando UTC para evitar desfases horarios
                        val fechaSeleccionada = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()

                        if (ValidationUtils.esMayorDeEdad(fechaSeleccionada)) {
                            // Guardamos en formato String estándar ISO
                            viewModel.onFechaNacimientoChange(
                                fechaSeleccionada.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            )
                            errorEdad = null
                            mostrarCalendario = false
                        } else {
                            errorEdad = "Debes tener al menos 18 años para registrarte"
                        }
                    }
                }) {
                    Text("OK", color = colorPrincipal, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("Cancelar", color = TextoOscuroClean)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = FondoBlancoClean,
                selectedDayContainerColor = colorPrincipal,
                todayDateBorderColor = colorPrincipal,
                todayContentColor = colorPrincipal
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Navegación al éxito
    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            // Pequeña pausa para que el usuario vea el mensaje de éxito
            kotlinx.coroutines.delay(1000)
            onIrALogin()
            // Reseteamos el estado para que no vuelva a navegar si regresa
            viewModel.resetRegistroExitoso()
        }
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

            // TÍTULO
            Text(
                text = "Crear Cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuroClean
            )
            // SUBTÍTULO
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = colorPrincipal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registro de Paciente",
                    fontSize = 16.sp,
                    color = colorPrincipal,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SWITCH (Login / Registro)
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

            // --- FORMULARIO ---
            SeccionFormulario("Datos Personales", colorPrincipal)

            CampoRegistroClean(label = "Cédula / DNI", valor = datos.cedula, placeholder = "110...", icon = Icons.Outlined.Badge, teclado = KeyboardType.Number, colorFocus = colorPrincipal) { viewModel.onCedulaChange(it) }
// NUEVO: Mostrar error si existe
            if (datos.errorCedula != null) {
                Text(
                    text = datos.errorCedula!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Primer Nombre", valor = datos.primerNombre, placeholder = "Juan", icon = Icons.Outlined.Person, colorFocus = colorPrincipal) { viewModel.onPrimerNombreChange(it) }
                }
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Segundo Nombre", valor = datos.segundoNombre, placeholder = "Carlos", icon = Icons.Outlined.Person, colorFocus = colorPrincipal) { viewModel.onSegundoNombreChange(it) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Primer Apellido", valor = datos.primerApellido, placeholder = "Pérez", icon = Icons.Outlined.Person, colorFocus = colorPrincipal) { viewModel.onPrimerApellidoChange(it) }
                }
                Box(Modifier.weight(1f)) {
                    CampoRegistroClean(label = "Segundo Apellido", valor = datos.segundoApellido, placeholder = "Gómez", icon = Icons.Outlined.Person, colorFocus = colorPrincipal) { viewModel.onSegundoApellidoChange(it) }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                CampoRegistroCleanReadOnly(
                    label = "Fecha Nacimiento",
                    valor = datos.fechaNacimiento,
                    placeholder = "Seleccione su fecha",
                    icon = Icons.Outlined.DateRange,
                    colorFocus = colorPrincipal,
                    onClick = { mostrarCalendario = true }
                )
                if (errorEdad != null) {
                    Text(
                        text = errorEdad!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BordeGrisClean)
            Spacer(modifier = Modifier.height(16.dp))

            SeccionFormulario("Información Adicional", colorPrincipal)
            CampoRegistroClean(label = "Discapacidad (Opcional)", valor = datos.discapacidad, placeholder = "Detalle si aplica", icon = Icons.Outlined.Accessible, colorFocus = colorPrincipal) { viewModel.onDiscapacidadChange(it) }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BordeGrisClean)
            Spacer(modifier = Modifier.height(16.dp))

            SeccionFormulario("Cuenta", colorPrincipal)

            CampoRegistroClean(label = "Correo Electrónico", valor = datos.correo, placeholder = "tu@email.com", icon = Icons.Outlined.Email, teclado = KeyboardType.Email, colorFocus = colorPrincipal) { viewModel.onCorreoChange(it) }
// NUEVO: Mostrar error si existe
            if (datos.errorCorreo != null) {
                Text(
                    text = datos.errorCorreo!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
            CampoRegistroClean(label = "Teléfono", valor = datos.telefono, placeholder = "0987654321", icon = Icons.Outlined.Phone, teclado = KeyboardType.Phone, colorFocus = colorPrincipal) { viewModel.onTelefonoChange(it) }
// NUEVO: Mostrar error si existe
            if (datos.errorTelefono != null) {
                Text(
                    text = datos.errorTelefono!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
            // CAMPO CONTRASEÑA
            // CAMPO CONTRASEÑA (ENVUELTO EN COLUMN PARA CONSISTENCIA)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Contraseña",
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuroClean,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = datos.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    placeholder = { Text("Mínimo 8 caracteres", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = inputColorsClean(colorPrincipal),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    trailingIcon = {
                        val imagen = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imagen, contentDescription = null, tint = colorPrincipal)
                        }
                    }
                )

                if (datos.errorPassword != null) {
                    Text(
                        text = datos.errorPassword!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(14.dp)) // Ajustado para mantener el espaciado total de 30.dp

            // MENSAJE FEEDBACK
            if (mensaje != null) {
                Text(
                    text = mensaje ?: "",
                    color = if (registroExitoso) Color(0xFF4CAF50) else Color.Red, // Verde si éxito, Rojo si error
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (registroExitoso) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // BOTÓN REGISTRAR
            Button(
                onClick = { viewModel.registrar() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorPrincipal,
                    contentColor = TextoOscuroClean
                ),
                enabled = !cargando
            ) {
                if (cargando) CircularProgressIndicator(color = TextoOscuroClean, modifier = Modifier.size(24.dp))
                else Text("Confirmar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FOOTER LEGAL
            val textoLegal = buildAnnotatedString {
                append("Al registrarte aceptas los ")
                withStyle(SpanStyle(color = colorPrincipal, fontWeight = FontWeight.Bold)) { append("Términos") }
                append(" y ")
                withStyle(SpanStyle(color = colorPrincipal, fontWeight = FontWeight.Bold)) { append("Política de Privacidad") }
            }
            Text(text = textoLegal, fontSize = 11.sp, color = TextoOscuroClean.copy(alpha = 0.7f), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
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
    // Usamos el modificador clickable en la columna, pero deshabilitamos el textfield para que no abra teclado
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
            enabled = false, // Deshabilitado para que el click lo capture la Column
            placeholder = { Text(placeholder, color = TextoOscuroClean.copy(alpha = 0.4f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = BordeGrisClean,
                disabledTextColor = TextoOscuroClean,
                disabledPlaceholderColor = TextoOscuroClean.copy(alpha = 0.4f),
                disabledTrailingIconColor = colorFocus,
                disabledLabelColor = TextoOscuroClean,
                disabledContainerColor = Color.White // Mantener fondo blanco aunque esté disabled
            ),
            singleLine = true,
            trailingIcon = { Icon(icon, contentDescription = null, tint = colorFocus) }
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