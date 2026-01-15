package com.example.lactacare.vistas.paciente.reserva

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.vistas.theme.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// Función para abrir Google Maps
fun abrirGoogleMaps(context: Context, latitud: String, longitud: String, nombre: String) {
    val lat = latitud.toDoubleOrNull() ?: 0.0
    val lng = longitud.toDoubleOrNull() ?: 0.0

    // URI para Google Maps
    val uri = "geo:$lat,$lng?q=$lat,$lng($nombre)"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Si Google Maps no está instalado, abrir en navegador
        val webUri = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
        context.startActivity(webIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgendar(
    onVolver: () -> Unit,
    onNavSeleccionarHorario: (Long, String) -> Unit = { _, _ -> }, // ✅ AGREGAR
    viewModel: AgendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val textoBusqueda by viewModel.busqueda.collectAsState()
    val lactariosFiltrados by viewModel.lactariosFiltrados.collectAsState()

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
        // SwipeRefresh wrapper
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isLoading),
            onRefresh = { viewModel.cargarLactarios() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // BUSCADOR
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    BuscadorEstiloPaciente(textoBusqueda) { viewModel.onBusquedaChanged(it) }
                }

                // CONTENIDO
                if (uiState.isLoading && uiState.lactarios.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MintPrimary)
                    }
                } else {
                    ListaLactariosPaciente(
                        lactariosFiltrados,
                        viewModel,
                        onNavSeleccionarHorario
                    )
                }
            }
        }
    }
}

@Composable
fun ListaLactariosPaciente(
    lactarios: List<Lactario>,
    viewModel: AgendarViewModel,
    onNavSeleccionarHorario: (Long, String) -> Unit // ✅ AGREGAR
) {
    val context = LocalContext.current  // ✅ AGREGAR

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
                    onReservar = {  // ✅ CAMBIAR ESTO
                        onNavSeleccionarHorario(lactario.id.toLong(), lactario.nombre)
                    },
                    onVerMapa = {
                        abrirGoogleMaps(
                            context = context,
                            latitud = lactario.latitud,
                            longitud = lactario.longitud,
                            nombre = lactario.nombre
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CardSalaPaciente(
    lactario: Lactario,
    onReservar: () -> Unit,
    onVerMapa: () -> Unit
) {
    val statusColor = StatusGreen
    val statusText = "Disponible"

    val imagenUrl = "https://img.freepik.com/free-vector/hospital-reception-waiting-hall_107791-1823.jpg"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
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
                    Text(lactario.obtenerHorario(), fontSize = 12.sp, color = Color.Gray)
                }

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Ver Mapa
                    OutlinedButton(
                        onClick = onVerMapa,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MomPrimary
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mapa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Botón Reservar
                    Button(
                        onClick = onReservar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MomAccent,
                            contentColor = Color(0xFFC13B84)
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Reservar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))

            // FOTO (Icono Hospital Rosado)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)), // Pink 50
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LocalHospital,
                        contentDescription = "Hospital",
                        tint = Color(0xFFE91E63), // Pink 500
                        modifier = Modifier.size(48.dp)
                    )
                }
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
