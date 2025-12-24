package com.example.lactacare.vistas.bebe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.vistas.theme.MomPrimary
import com.example.lactacare.vistas.theme.TextoOscuroClean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAnadirBebe(
    onVolver: () -> Unit,
    viewModel: BebeViewModel = hiltViewModel()
) {
    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var SexoSeleccionado by remember { mutableStateOf("Femenino") }
    var guardando by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nuevo Bebé", fontWeight = FontWeight.Bold, color = TextoOscuroClean) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, "Cancelar", tint = TextoOscuroClean)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Título decorativo
            Text(
                "Ingresa los datos de tu pequeño",
                fontSize = 16.sp,
                color = Color.Gray
            )

            // 1. Campo Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del bebé") },
                leadingIcon = { Icon(Icons.Outlined.Face, null, tint = MomPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MomPrimary,
                    focusedLabelColor = MomPrimary
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            // 2. Campo Fecha (Texto simple por ahora)
            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha de Nacimiento (YYYY-MM-DD)") },
                placeholder = { Text("Ej: 2025-05-20") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = MomPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MomPrimary,
                    focusedLabelColor = MomPrimary
                )
            )

            // 3. Selector de Género
            Text("Sexo", fontWeight = FontWeight.Bold, color = TextoOscuroClean)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BotonGenero(
                    texto = "Femenino",
                    colorBase = Color(0xFFF472B6), // Rosa
                    seleccionado = SexoSeleccionado == "Femenino",
                    onClick = { SexoSeleccionado = "Femenino" },
                    modifier = Modifier.weight(1f)
                )
                BotonGenero(
                    texto = "Masculino",
                    colorBase = Color(0xFF60A5FA), // Azul
                    seleccionado = SexoSeleccionado == "Masculino",
                    onClick = { SexoSeleccionado = "Masculino" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Botón Guardar
            Button(
                onClick = {
                    if (nombre.isNotEmpty() && fechaNacimiento.isNotEmpty()) {
                        guardando = true
                        viewModel.registrarBebe(nombre, fechaNacimiento, SexoSeleccionado) {
                            guardando = false
                            onVolver() // Regresar a la lista al terminar
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MomPrimary),
                shape = RoundedCornerShape(16.dp),
                enabled = !guardando && nombre.isNotEmpty()
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Bebé", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BotonGenero(
    texto: String,
    colorBase: Color,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (seleccionado) colorBase.copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (seleccionado) colorBase else Color.LightGray.copy(alpha = 0.5f)
    val textColor = if (seleccionado) colorBase else Color.Gray

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier
            .height(50.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = texto,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
