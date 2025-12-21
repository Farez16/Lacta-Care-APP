package com.example.lactacare.vistas.inventario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lactacare.datos.MockInventarioRepository
import com.example.lactacare.dominio.model.ContenedorLeche
import com.example.lactacare.vistas.theme.TextoOscuroClean
import com.example.lactacare.vistas.theme.InvAmberBg
import com.example.lactacare.vistas.theme.InvFondo
import com.example.lactacare.vistas.theme.InvPinkPrimary
import com.example.lactacare.vistas.theme.InvPinkSoft
import com.example.lactacare.vistas.theme.InvGreenText
import com.example.lactacare.vistas.theme.InvGreenBg
import com.example.lactacare.vistas.theme.InvBlueText
import com.example.lactacare.vistas.theme.InvBlueBg
import com.example.lactacare.vistas.theme.InvRedText
import com.example.lactacare.vistas.theme.InvRedBg
import com.example.lactacare.vistas.theme.InvZincText
import com.example.lactacare.vistas.theme.InvZincBg




@Composable
fun PantallaInventario(
    // INYECCIÓN DE DEPENDENCIAS: Conectamos el ViewModel Real
    viewModel: InventarioViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InventarioViewModel(
                    inventarioRepository = MockInventarioRepository()
                ) as T
            }
        }
    )
) {
    // Escuchamos el estado del ViewModel (Datos Reales)
    val uiState by viewModel.uiState.collectAsState()

    // Estado local para los filtros visuales
    var filtroSeleccionado by remember { mutableStateOf("Todo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InvFondo)
            .padding(16.dp)
    ) {
        // --- CABECERA ---
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Inventario de Leche",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuroClean
            )
        }

        // --- MANEJO DE ESTADOS (Loading / Error / Success) ---
        when (val estado = uiState) {
            is InventarioUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = InvPinkPrimary)
                }
            }
            is InventarioUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${estado.mensaje}", color = InvRedText)
                }
            }
            is InventarioUiState.Success -> {
                // Si cargó bien, mostramos el contenido filtrado
                ContenidoInventario(
                    listaLeche = estado.stockLeche,
                    filtroSeleccionado = filtroSeleccionado,
                    onFiltroChanged = { filtroSeleccionado = it }
                )
            }
        }
    }
}

@Composable
fun ContenidoInventario(
    listaLeche: List<ContenedorLeche>,
    filtroSeleccionado: String,
    onFiltroChanged: (String) -> Unit
) {
    val volumenTotal = listaLeche.sumOf { it.cantidad }.toInt()

    // --- LÓGICA DE FILTRADO ACTUALIZADA ---
    val lotesVisibles = if (filtroSeleccionado == "Todo") {
        listaLeche
    } else {
        listaLeche.filter { contenedor ->
            when (filtroSeleccionado) {
                "Refrigerada" -> contenedor.estado.equals("Refrigerada", ignoreCase = true)
                "Congelada" -> contenedor.estado.equals("Congelada", ignoreCase = true)
                "Caducado" -> contenedor.estado.equals("Caducado", ignoreCase = true)
                "Retirada" -> contenedor.estado.equals("Retirada", ignoreCase = true) // <-- NUEVO
                else -> true
            }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ... (Sección de Tarjetas Resumen sigue igual) ...
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tarjeta TOTAL
                CardEstadistica(
                    titulo = "TOTAL ALMACENADO",
                    valor = "$volumenTotal",
                    unidad = "ml",
                    icono = Icons.Outlined.WaterDrop,
                    colorIcono = InvPinkPrimary,
                    bgCard = Color.White,
                    textColor = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                // Tarjeta POR CADUCAR
                CardEstadistica(
                    titulo = "POR CADUCAR",
                    valor = "0",
                    unidad = "frascos",
                    icono = Icons.Outlined.HourglassTop,
                    colorIcono = Color(0xFFF59E0B),
                    bgCard = InvAmberBg,
                    textColor = Color(0xFF78350F),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { SeccionReservasStatic() }

        // --- PESTAÑAS DE FILTRO ACTUALIZADAS ---
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Agregamos "Retirada" a la lista
                val filtros = listOf("Todo", "Refrigerada", "Congelada", "Caducado", "Retirada")
                items(filtros) { filtro ->
                    val activo = filtroSeleccionado == filtro
                    val bgColor = if (activo) Color(0xFFEC4899) else Color.White
                    val txtColor = if (activo) Color.White else Color.Gray
                    val border = if (activo) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
                    val elevation = if (activo) 4.dp else 0.dp

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(50),
                        shadowElevation = elevation,
                        border = border,
                        modifier = Modifier
                            .height(36.dp)
                            .clickable { onFiltroChanged(filtro) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(filtro, color = txtColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "REGISTROS RECIENTES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
        }

        if (lotesVisibles.isEmpty()) {
            item {
                Text("No hay registros en esta categoría.", modifier = Modifier.padding(top = 16.dp), color = Color.Gray)
            }
        } else {
            items(lotesVisibles) { contenedor ->
                ItemLote(contenedor)
            }
        }
    }
}

// --- COMPONENTES VISUALES ---

@Composable
fun SeccionReservasStatic() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mis Reservas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "Ver todas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = InvPinkPrimary,
                modifier = Modifier.clickable { }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(InvPinkSoft, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = InvPinkPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Sala de Lactancia - Piso 2", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Badge(texto = "CONFIRMADA", colorTexto = InvGreenText, colorFondo = InvGreenBg)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Miércoles 13 de Diciembre, 14:00 - 14:30", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun CardEstadistica(
    titulo: String, valor: String, unidad: String, icono: ImageVector,
    colorIcono: Color, bgCard: Color, textColor: Color, modifier: Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgCard),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(titulo, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(valor, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                Text(unidad, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
            }
        }
    }
}

@Composable
fun ItemLote(contenedor: ContenedorLeche) {
    // Mapeo dinámico: Estado Dominio -> Visual
    val (colorTxt, colorBg, icono, colorIcono, bgIcono) = when (contenedor.estado.lowercase()) {
        "refrigerado", "refrigerada" -> Quintuple(InvGreenText, InvGreenBg, Icons.Filled.Kitchen, InvPinkPrimary, InvPinkSoft)
        "congelado", "congelada" -> Quintuple(InvBlueText, InvBlueBg, Icons.Outlined.AcUnit, Color(0xFF2563EB), Color(0xFFDBEAFE))
        "caducado" -> Quintuple(InvRedText, InvRedBg, Icons.Outlined.Error, Color(0xFFDC2626), Color(0xFFFEE2E2))
        else -> Quintuple(InvZincText, InvZincBg, Icons.Filled.Archive, Color.Gray, Color(0xFFF4F4F5))
    }

    // Parseo simple de Fecha/Hora para visualización
    // Mock Format: "2025-12-10 08:30"
    val partesFecha = contenedor.fechaExtraccion.split(" ")
    val fechaVisual = if (partesFecha.size > 1) "${partesFecha[0]}, ${partesFecha[1]}" else contenedor.fechaExtraccion

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(bgIcono, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icono, null, tint = colorIcono, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("${contenedor.cantidad.toInt()} ml", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Outlined.Event, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(fechaVisual, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Badge(texto = contenedor.estado, colorTexto = colorTxt, colorFondo = colorBg)
            }

            // Botón Retirar (Lógica visual)
            if (contenedor.estado.lowercase().contains("refrigerad") || contenedor.estado.lowercase().contains("congelad")) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { /* TODO: Llamar a viewModel.retirarLeche(id) */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InvPinkSoft,
                            contentColor = Color(0xFFBE185D)
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(Icons.Filled.Archive, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retirar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun Badge(texto: String, colorTexto: Color, colorFondo: Color) {
    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, colorTexto.copy(alpha = 0.1f))
    ) {
        Text(
            text = texto,
            color = colorTexto,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// Clase auxiliar para manejar los 5 valores de estilo
data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)