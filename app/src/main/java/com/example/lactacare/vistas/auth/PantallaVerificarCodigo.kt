package com.example.lactacare.vistas.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Password
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
import com.example.lactacare.dominio.model.RolUsuario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVerificarCodigo(
    correo: String,
    rolUsuario: RolUsuario,
    onVolver: () -> Unit,
    onCodigoVerificado: () -> Unit,
    onReenviarCodigo: () -> Unit,
    viewModel: RecuperarPasswordViewModel = hiltViewModel()
) {
    val codigo by viewModel.codigo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    // Establecer correo y rol en el ViewModel
    LaunchedEffect(correo, rolUsuario) {
        viewModel.setCorreo(correo)
        viewModel.setRol(rolUsuario)
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
                title = { Text("Verificar Código") },
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
            Icon(
                imageVector = Icons.Outlined.Password,
                contentDescription = null,
                tint = colorPrincipal,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ingresa el Código",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hemos enviado un código de 6 dígitos a:",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = correo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorPrincipal,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // CAMPO CÓDIGO
            OutlinedTextField(
                value = codigo,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        viewModel.setCodigo(it)
                    }
                },
                label = { Text("Código de 6 dígitos") },
                placeholder = { Text("123456") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorPrincipal,
                    focusedLabelColor = colorPrincipal,
                    cursorColor = colorPrincipal
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mensaje != null && (mensaje!!.contains("inválido", ignoreCase = true) || mensaje!!.contains("expirado", ignoreCase = true))
            )

            // Mostrar mensaje de error
            if (mensaje != null && !mensaje!!.contains("válido") && !mensaje!!.contains("enviado")) {
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

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⏰",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            "El código expira en 15 minutos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            "Asegúrate de ingresarlo pronto",
                            fontSize = 12.sp,
                            color = Color(0xFFFF6F00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN VERIFICAR
            Button(
                onClick = {
                    viewModel.verificarCodigo(
                        onSuccess = onCodigoVerificado
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
                enabled = !isLoading && codigo.length == 6
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = if (rolUsuario == RolUsuario.PACIENTE) Color(0xFF546E7A) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Verificar Código",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN REENVIAR
            TextButton(
                onClick = onReenviarCodigo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "¿No recibiste el código? Reenviar",
                    color = colorPrincipal,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTÓN VOLVER
            TextButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Volver",
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
