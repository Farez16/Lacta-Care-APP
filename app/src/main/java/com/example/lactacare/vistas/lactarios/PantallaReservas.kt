package com.example.lactacare.vistas.lactarios

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.lactacare.datos.MockLactarioRepository
import com.example.lactacare.datos.MockReservasRepository
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.vistas.theme.LactarioBg
import com.example.lactacare.vistas.theme.MintPrimary
import com.example.lactacare.vistas.theme.SoftPink
import com.example.lactacare.vistas.theme.StatusGreen
import com.example.lactacare.vistas.theme.StatusRed
import com.example.lactacare.vistas.theme.StatusYellow
import com.example.lactacare.vistas.theme.TextoOscuroClean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReservas(
    onVolver: () -> Unit,
    // CORRECCIÓN IMPORTANTE: Inyectamos AQUÍ los dos repositorios
    viewModel: LactarioViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LactarioViewModel(
                    lactarioRepository = MockLactarioRepository(),
                    reservasRepository = MockReservasRepository() // ¡Ahora sí se sincroniza!
                ) as T
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
    val filtroActivo by viewModel.filtroEstado.collectAsState()

    Scaffold(
        containerColor = LactarioBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lactarios Disponibles", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoOscuroClean) },
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
                BuscadorEstiloHtml(textoBusqueda) { viewModel.actualizarFiltros(nuevoTexto = it) }
            }

            // FILTROS
            FiltrosChipsHtml(filtroActivo) { viewModel.actualizarFiltros(nuevoEstado = it) }

            // CONTENIDO
            when (val estado = uiState) {
                is LactarioUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MintPrimary) }
                is LactarioUiState.Error -> Text("Error: ${estado.mensaje}", color = StatusRed, modifier = Modifier.padding(16.dp))
                is LactarioUiState.Success -> ListaLactariosHtml(estado.lactarios, viewModel)
            }
        }
    }
}

@Composable
fun ListaLactariosHtml(lactarios: List<Lactario>, viewModel: LactarioViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Mapa Estático
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter("https://img.freepik.com/free-vector/city-map-background-concept_23-2148003732.jpg"),
                        contentDescription = "Mapa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(16.dp).align(Alignment.BottomStart)
                    ) {
                        Text("📍 Ubicación simulada: Planta Baja", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        if (lactarios.isEmpty()) {
            item { EmptyStateHtml() }
        } else {
            items(lactarios) { lactario ->
                // AQUI USAMOS LA NUEVA FUNCIÓN 'calcularEstadoReal' QUE USA EL REPOSITORIO DE RESERVAS
                CardSalaHtml(
                    lactario = lactario,
                    estadoSimulado = viewModel.calcularEstadoReal(lactario.id), // Corregido: minúscula inicial
                    distanciaSimulada = viewModel.simularDistancia(lactario.id)
                )
            }
        }
    }
}

@Composable
fun CardSalaHtml(lactario: Lactario, estadoSimulado: String, distanciaSimulada: Int) {
    val (statusColor, statusText) = when (estadoSimulado) {
        "Disponible" -> StatusGreen to "Disponible ahora"
        "Ocupado" -> StatusRed to "Ocupado"
        "Reservado" -> StatusYellow to "Reservado"
        else -> Color.Gray to estadoSimulado
    }

    val imagenUrl = if (lactario.id % 2 == 0)
        "https://img.freepik.com/free-photo/modern-hospital-room-interior_1232-2182.jpg"
    else
        "https://img.freepik.com/free-vector/hospital-reception-waiting-hall_107791-1823.jpg"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable { /* AQUÍ IRA LA NAVEGACIÓN A DETALLE */ }
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
                    Text("09:00 - 18:00", fontSize = 12.sp, color = Color.Gray)
                }

                Surface(
                    color = MintPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("a ${distanciaSimulada}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MintPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
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

// --- COMPONENTES UI AUXILIARES ---

@Composable
fun BuscadorEstiloHtml(texto: String, onTextoChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        shadowElevation = 2.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().height(54.dp)
    ) {
        TextField(
            value = texto,
            onValueChange = onTextoChange,
            placeholder = { Text("Buscar por lugar o dirección...", color = Color.LightGray, fontSize = 15.sp) },
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
fun FiltrosChipsHtml(filtroSeleccionado: String, onFiltroSeleccionado: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val filtros = listOf("Todos", "Disponible", "Ocupado", "Reservado")
        items(filtros) { filtro ->
            val seleccionado = filtro == filtroSeleccionado
            val colorBase = when(filtro) {
                "Disponible" -> MintPrimary
                "Ocupado" -> StatusRed
                "Reservado" -> StatusYellow
                else -> Color.Gray
            }
            ChipHtml(texto = filtro, colorBase = colorBase, seleccionado = seleccionado, onClick = { onFiltroSeleccionado(filtro) })
        }
    }
}

@Composable
fun ChipHtml(texto: String, colorBase: Color, seleccionado: Boolean, onClick: () -> Unit) {
    val bgColor = if (seleccionado) colorBase else Color.White
    val txtColor = if (seleccionado) Color.White else colorBase
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        shadowElevation = if (seleccionado) 4.dp else 1.dp,
        modifier = Modifier.height(36.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = txtColor)
        }
    }
}

@Composable
fun EmptyStateHtml() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp).background(SoftPink.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SearchOff, null, tint = SoftPink, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("No encontramos lactarios", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoOscuroClean)
        Text("Prueba con otros filtros.", color = Color.Gray, fontSize = 14.sp)
    }
}