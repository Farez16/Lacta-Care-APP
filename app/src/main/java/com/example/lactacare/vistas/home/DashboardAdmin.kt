package com.example.lactacare.vistas.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.dominio.model.DashboardAdminStats
import com.example.lactacare.vistas.theme.*

@Composable
fun DashboardAdmin(
    colorPrincipal: Color, // Mantenemos firma original, aunque internamente usaremos MintPrimary si coincide
    colorAcento: Color,
    stats: DashboardAdminStats?,
    onNavGestion: () -> Unit = {},
    onNavAlertas: () -> Unit = {},
    onNavReportes: () -> Unit = {}
) {
    if (stats == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundPastel), 
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MintPrimary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundPastel),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Filtros (Visuales)
        item {
            var selectedFilter by remember { mutableStateOf("Mes") }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Día", "Semana", "Mes", "Personalizado").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    BotonPildoraSeleccionable(
                        texto = filter,
                        seleccionado = isSelected,
                        onClick = { selectedFilter = filter },
                        modifier = Modifier
                    )
                }
            }
        }

        // 2. Tarjeta Principal (Panel de Control)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NeonPrimary), // Neon Background
                shape = RoundedCornerShape(24.dp), 
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Panel de Control", color = DarkCharcoal, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Resumen en tiempo real", color = DarkCharcoal.copy(alpha = 0.8f), fontSize = 14.sp)
                        
                        // Métrica de Crecimiento
                        stats.crecimientoCitas?.let { crecimiento ->
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = Color.Black.copy(alpha = 0.1f), // Overlay oscuro sutil para legibilidad
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icono = if(crecimiento >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown
                                    Icon(icono, null, tint = DarkCharcoal, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${if(crecimiento > 0) "+" else ""}${String.format("%.1f", crecimiento)}% vs mes anterior",
                                        color = DarkCharcoal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    // Icono decorativo semi-transparente
                    Icon(
                        Icons.Outlined.Analytics, 
                        null, 
                        tint = DarkCharcoal.copy(alpha = 0.2f), // Tint oscuro sutil
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        // 3. Estadísticas (Grid 2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Estadísticas Globales", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCardPremium(
                        modifier = Modifier.weight(1f),
                        titulo = "Pacientes",
                        valor = stats.totalUsuarios.toString(),
                        icono = Icons.Outlined.Person,
                        colorIcono = NeonPrimary,
                        onClick = onNavGestion
                    )
                    AdminStatCardPremium(
                        modifier = Modifier.weight(1f),
                        titulo = "Doctores",
                        valor = stats.totalDoctores.toString(),
                        icono = Icons.Outlined.MedicalServices,
                        colorIcono = NeonPrimary, 
                        onClick = onNavGestion
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCardPremium(
                        modifier = Modifier.weight(1f),
                        titulo = "Citas Hoy",
                        valor = stats.citasHoy.toString(),
                        icono = Icons.Outlined.Event,
                        colorIcono = NeonPrimary,
                        onClick = onNavReportes
                    )

                    val colorAlerta = if (stats.alertasActivas > 0) Color(0xFFEF5350) else Color.Gray
                    AdminStatCardPremium(
                        modifier = Modifier.weight(1f),
                        titulo = "Alertas",
                        valor = stats.alertasActivas.toString(),
                        icono = Icons.Outlined.Warning,
                        colorIcono = colorAlerta,
                        onClick = onNavAlertas
                    )
                }
            }
        }

        // 4. Actividad Reciente
        item {
            TarjetaPremium(titulo = "Actividad Reciente") {
                if (stats.actividadesRecientes.isEmpty()) {
                    Text("Sin actividad reciente", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    stats.actividadesRecientes.forEachIndexed { index, actividad ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BackgroundPastel, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icono = if (actividad.esAlerta) Icons.Outlined.Warning else Icons.Outlined.Info
                                Icon(icono, null, tint = if (actividad.esAlerta) Color(0xFFEF5350) else NeonPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(actividad.titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuroClean)
                                Text(actividad.subtitulo, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        if (index < stats.actividadesRecientes.size - 1) {
                            HorizontalDivider(color = BackgroundPastel)
                        }
                    }
                }
            }
        }

        // 5. Gráfico Semanal
        item {
            TarjetaPremium(titulo = "Reservas de la Semana") {
                Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.Center) {
                    if (stats.citasSemana.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.BarChart, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Sin reservas esta semana", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        WeeklyChart(
                            data = stats.citasSemana,
                            barColor = NeonPrimary
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun AdminStatCardPremium(
    modifier: Modifier,
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
            .height(140.dp) // Altura fija para uniformidad
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false) // Misma sombra sutil
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorIcono.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(24.dp))
            }
            
            Column {
                Text(valor, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
                Text(titulo, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun WeeklyChart(
    data: List<Pair<String, Int>>,
    barColor: Color
) {
    // Reutilizamos lógica de dibujo pero adaptada a Premium (si es necesario)
    // Por ahora igual, solo asegurando colores correctos
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val maxVal = data.maxOfOrNull { it.second } ?: 1
        val barWidth = size.width / (data.size * 2f)
        val space = size.width / data.size
        val safeMax = if (maxVal == 0) 1 else maxVal

        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value.toFloat() / safeMax) * (size.height * 0.7f)
            val x = index * space + (space - barWidth) / 2
            val y = size.height - barHeight - 40f 

            drawRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                
                // Nota: Canvas drawRect no soporta rounded corners nativamente facil sin path
                // Si quisieramos "Pill Bars", usariamos drawRoundRect
            )
        }
        
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f), // Linea más sutil
            start = androidx.compose.ui.geometry.Offset(0f, size.height - 40f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height - 40f),
            strokeWidth = 2f
        )
    }
    
    Row(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (day, _) ->
            Text(day, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}