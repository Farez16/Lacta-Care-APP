package com.example.lactacare.vistas.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.dominio.model.RolUsuario
import com.example.lactacare.vistas.components.ErrorDialog  // ⭐ AGREGAR
import com.example.lactacare.vistas.components.ErrorDialogData
import com.example.lactacare.vistas.components.ErrorType
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSolicitarCodigo(
    rolUsuario: RolUsuario,
    onVolver: () -> Unit,
    onCodigoEnviado: (String) -> Unit,
    viewModel: RecuperarPasswordViewModel = hiltViewModel()
) {
    var codigoEnviado by remember { mutableStateOf(false) }
    val correo by viewModel.correo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogData by remember { mutableStateOf<ErrorDialogData?>(null) }

    // ⭐ MOVER AQUÍ: Establecer rol en el ViewModel
    LaunchedEffect(rolUsuario) {
        viewModel.setRol(rolUsuario)
    }

    // ⭐ MOVER AQUÍ: Observar cambios en el mensaje para mostrar diálogos
    LaunchedEffect(mensaje) {
        mensaje?.let { msg ->
            when {
                msg.startsWith("GOOGLE_ACCOUNT:") -> {
                    errorDialogData = ErrorDialogData(
                        tipo = ErrorType.GOOGLE_ACCOUNT,
                        titulo = "Cuenta de Google",
                        mensaje = msg.replace("GOOGLE_ACCOUNT: ", "")
                    )
                    showErrorDialog = true
                }
                msg.startsWith("ROL_MISMATCH:") -> {
                    errorDialogData = ErrorDialogData(
                        tipo = ErrorType.ROL_MISMATCH,
                        titulo = "Rol Incorrecto",
                        mensaje = msg.replace("ROL_MISMATCH: ", "")
                    )
                    showErrorDialog = true
                }
                msg.startsWith("UNAUTHORIZED_EMAIL:") -> {
                    errorDialogData = ErrorDialogData(
                        tipo = ErrorType.UNAUTHORIZED_EMAIL,
                        titulo = "Correo No Autorizado",
                        mensaje = msg.replace("UNAUTHORIZED_EMAIL: ", "")
                    )
                    showErrorDialog = true
                }
                msg.startsWith("USER_NOT_FOUND:") -> {
                    errorDialogData = ErrorDialogData(
                        tipo = ErrorType.USER_NOT_FOUND,
                        titulo = "Usuario No Encontrado",
                        mensaje = msg.replace("USER_NOT_FOUND: ", "")
                    )
                    showErrorDialog = true
                }
            }
        }
    }

    // Colores según rol
    val colorPrincipal = when (rolUsuario) {
        RolUsuario.ADMINISTRADOR -> Color(0xFFA3C9A8)
        RolUsuario.MEDICO -> Color(0xFFB0C4DE)
        RolUsuario.PACIENTE -> Color(0xFFFFC0CB)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            if (!codigoEnviado) {
                // FORMULARIO PARA INGRESAR EMAIL
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = colorPrincipal,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF546E7A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingresa tu correo electrónico y te enviaremos un código de 6 dígitos para restablecer tu contraseña",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                // CAMPO EMAIL
                OutlinedTextField(
                    value = correo,
                    onValueChange = { viewModel.setCorreo(it) },
                    label = { Text("Correo Electrónico") },
                    placeholder = { Text("tu@email.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorPrincipal,
                        focusedLabelColor = colorPrincipal,
                        focusedLeadingIconColor = colorPrincipal,
                        cursorColor = colorPrincipal
                    ),
                    singleLine = true,
                    isError = mensaje != null && !mensaje!!.contains("enviado")
                )

                if (mensaje != null && !mensaje!!.contains("enviado") && !mensaje!!.startsWith("GOOGLE_ACCOUNT:") && !mensaje!!.startsWith("ROL_MISMATCH:") && !mensaje!!.startsWith("UNAUTHORIZED_EMAIL:") && !mensaje!!.startsWith("USER_NOT_FOUND:")) {
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

                // BOTÓN ENVIAR
                Button(
                    onClick = {
                        viewModel.solicitarCodigo(
                            onSuccess = { codigoEnviado = true }
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
                    enabled = !isLoading && correo.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = if (rolUsuario == RolUsuario.PACIENTE) Color(0xFF546E7A) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Enviar Código",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN VOLVER
                TextButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Volver al inicio de sesión",
                        color = colorPrincipal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // PANTALLA DE CONFIRMACIÓN
                Icon(
                    imageVector = Icons.Outlined.MarkEmailRead,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "¡Código Enviado!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF546E7A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Te hemos enviado un código de 6 dígitos a:",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = correo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorPrincipal,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "📧 Revisa tu bandeja de entrada",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF546E7A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Si no ves el correo, revisa tu carpeta de spam\n" +
                                    "• El código expirará en 15 minutos\n" +
                                    "• Si no lo recibes, puedes solicitar uno nuevo",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onCodigoEnviado(correo) },
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
                        "Ingresar Código",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { codigoEnviado = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Reenviar código",
                        color = colorPrincipal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // ⭐ MOVER AQUÍ: Mostrar diálogo de error (FUERA del Scaffold)
    if (showErrorDialog && errorDialogData != null) {
        ErrorDialog(
            data = errorDialogData!!,
            onDismiss = {
                showErrorDialog = false
                errorDialogData = null
                viewModel.setCorreo("")  // Limpiar correo
            }
        )
    }
}