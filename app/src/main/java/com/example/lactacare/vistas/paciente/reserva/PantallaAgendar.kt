package com.example.lactacare.vistas.paciente.reserva

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.vistas.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgendar(
    onVolver: () -> Unit,
    viewModel: AgendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val textoBusqueda by viewModel.busqueda.collectAsState()

    // Manejo del éxito de la reserva
    if (uiState.reservaExitosa) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.resetReservaExitosa()
                onVolver() 
            },
            title = { Text("¡Reserva Exitosa!") },
            text = { Text("Tu espacio en el lactario ha sido reservado para hoy.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetReservaExitosa()
                    onVolver()
                }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (uiState.error != null) {
         AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error ?: "Error desconocido") },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        containerColor = LactarioBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reservar Lactario", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoOscuroClean) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver", tint = TextoOscuroClean) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LactarioBg.copy(alpha = 0.95f))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // BUSCADOR
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                BuscadorEstiloPaciente(textoBusqueda) { viewModel.onBusquedaChanged(it) }
            }

            // CONTENIDO
            if (uiState.isLoading) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                     CircularProgressIndicator(color = MintPrimary) 
                 }
            } else {
                 ListaLactariosPaciente(uiState.lactarios, viewModel) // Pasamos VM para el click handle
            }
        }
    }
}

@Composable
fun ListaLactariosPaciente(lactarios: List<Lactario>, viewModel: AgendarViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (lactarios.isEmpty()) {
            item { EmptyStatePaciente() }
        } else {
            items(lactarios) { lactario ->
                CardSalaPaciente(
                    lactario = lactario,
                    onReservar = { viewModel.reservar(lactario.id.toLong()) }
                )
            }
        }
    }
}

@Composable
fun CardSalaPaciente(lactario: Lactario, onReservar: () -> Unit) {
    // Estado simulado visualmente correcto para la lista
    val statusColor = StatusGreen
    val statusText = "Disponible"

    val imagenUrl = "https://img.freepik.com/free-vector/hospital-reception-waiting-hall_107791-1823.jpg"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable { onReservar() } // Click para reservar
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // INFO
            Column(
                modifier = Modifier.weight(1.6f).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(statusText, color = statusColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(lactario.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuroClean, maxLines = 2)
                    Text(lactario.direccion, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("08:00 - 18:00", fontSize = 12.sp, color = Color.Gray)
                }

                Button(
                    onClick = onReservar,
                    colors = ButtonDefaults.buttonColors(containerColor = MomAccent, contentColor = Color(0xFFC13B84)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Reservar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            // FOTO
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).fillMaxHeight()) {
                Image(
                    painter = rememberAsyncImagePainter(imagenUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun BuscadorEstiloPaciente(texto: String, onTextoChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        shadowElevation = 2.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().height(54.dp)
    ) {
        TextField(
            value = texto,
            onValueChange = onTextoChange,
            placeholder = { Text("Buscar lactario...", color = Color.LightGray, fontSize = 15.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.LightGray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun EmptyStatePaciente() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp).background(SoftPink.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SearchOff, null, tint = SoftPink, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("No encontramos lactarios", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoOscuroClean)
    }
}
