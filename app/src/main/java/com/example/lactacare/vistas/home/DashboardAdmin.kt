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
import androidx.compose.ui.graphics.asImageBitmap
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
    currentFilter: String = "Mes",
    onFilterChange: (String) -> Unit = {},
    onNavGestion: () -> Unit = {},
    onNavAlertas: () -> Unit = {},
    onNavReportes: () -> Unit = {}
) {
    if (stats == null) {
        // Estado de Error / Vacio (Si isLoading es false en el padre pero no llegaron datos)
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundPastel), 
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Error, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No se pudieron cargar los datos.", color = Color.Gray)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundPastel),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 0. Header InstituciÃ³n (MÃ³dulo Nuevo)
        item {
            stats.institucion?.let { inst ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val logoBitmap = remember(inst.logoInstitucion) {
                            try {
                                if (!inst.logoInstitucion.isNullOrEmpty()) {
                                    val imageBytes = android.util.Base64.decode(inst.logoInstitucion, android.util.Base64.DEFAULT)
                                    val decoded = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                    decoded.asImageBitmap()
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }

                        if (logoBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = logoBitmap,
                                contentDescription = "Logo InstituciÃ³n",
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                        } else {
                            // Si falla la decodificaciÃ³n o no hay logo, mostramos icono default
                            Icon(Icons.Outlined.Business, null, tint = NeonPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(16.dp))
                        }

                        Column {
                            Text(
                                text = inst.nombreInstitucion,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DarkCharcoal
                            )
                            Text(
                                text = "Panel Administrativo",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // 1. Filtros (Visuales - Minimalist pills)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Día", "Semana", "Mes", "Personalizado").forEach { filter ->
                    val isSelected = currentFilter == filter
                    val containerColor = if(isSelected) OliveAdmin else Color.Transparent
                    val contentColor = if(isSelected) Color.White else OliveTextSecondary
                    val borderColor = if(isSelected) Color.Transparent else Color(0xFFE0E0E0)
                    
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = containerColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .height(32.dp)
                            .clickable { onFilterChange(filter) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = filter, 
                                color = contentColor, 
                                fontSize = 13.sp, 
                                fontWeight = if(isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // 2. Tarjeta Principal (Panel de Control - Minimalist)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White), // White Surface
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)), // Subtle Border
                elevation = CardDefaults.cardElevation(0.dp), // Flat
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Panel de Control", color = OliveTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif)
                        Text("Resumen en tiempo real", color = OliveTextSecondary, fontSize = 14.sp)
                        
                        // MÃ©trica de Crecimiento
                        stats.crecimientoCitas?.let { crecimiento ->
                            Spacer(Modifier.height(16.dp))
                            Surface(
                                color = OliveAdmin.copy(alpha = 0.1f), 
                                shape = RoundedCornerShape(8.dp) // Less rounded
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icono = if(crecimiento >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown
                                    Icon(icono, null, tint = OliveAdmin, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${if(crecimiento > 0) "+" else ""}${String.format("%.1f", crecimiento)}% vs mes anterior",
                                        color = OliveAdmin,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    // Icono decorativo semi-transparente
                    Icon(
                        Icons.Outlined.Analytics, 
                        null, 
                        tint = OliveAdmin.copy(alpha = 0.2f), 
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

        // 4. Actividad Reciente - Minimal
        item {
            TarjetaPremium(titulo = "Actividad Reciente") {
                if (stats.actividadesRecientes.isEmpty()) {
                    Text("Sin actividad reciente", color = OliveTextSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    stats.actividadesRecientes.forEachIndexed { index, actividad ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(OliveAdmin.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icono = if (actividad.esAlerta) Icons.Outlined.Warning else Icons.Outlined.Info
                                Icon(icono, null, tint = if (actividad.esAlerta) Color(0xFFEF5350) else OliveAdmin, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(actividad.titulo, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = OliveTextPrimary)
                                Text(actividad.subtitulo, fontSize = 12.sp, color = OliveTextSecondary)
                            }
                        }
                        if (index < stats.actividadesRecientes.size - 1) {
                            Divider(color = Color(0xFFF5F5F5), thickness = 1.dp)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
            .height(110.dp) // Reduced height
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp), // Reduced padding
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp) // Smaller icon background
                    .background(colorIcono.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(18.dp))
            }
            
            Spacer(Modifier.height(8.dp)) // Reduced spacing

            Column {
                Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = OliveTextPrimary) // Smaller font
                Text(titulo, fontSize = 12.sp, color = OliveTextSecondary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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