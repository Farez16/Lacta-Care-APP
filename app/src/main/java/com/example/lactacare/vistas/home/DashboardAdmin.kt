package com.example.lactacare.vistas.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.dominio.model.DashboardAdminStats

@Composable
fun DashboardAdmin(
    colorPrincipal: Color,
    colorAcento: Color,
    stats: DashboardAdminStats?,
    onNavGestion: () -> Unit = {},
    onNavReportes: () -> Unit = {}
) {
    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se pudo cargar la información.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Tarjeta Principal (Panel de Control)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colorPrincipal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Panel de Control", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Resumen en tiempo real", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                    Icon(Icons.Outlined.Analytics, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }

        // Estadísticas
        item {
            Column {
                Text("Estadísticas Globales", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // --- AQUÍ CONECTAMOS EL CLICK ---
                    AdminStatCard(
                        Modifier.weight(1f),
                        "Pacientes",
                        stats.totalUsuarios.toString(),
                        Icons.Outlined.Person,
                        colorPrincipal,
                        onClick = onNavGestion // <--- ¡CONECTADO!
                    )
                    AdminStatCard(
                        Modifier.weight(1f),
                        "Doctores",
                        stats.totalDoctores.toString(),
                        Icons.Outlined.MedicalServices,
                        colorPrincipal,
                        onClick = onNavGestion // <--- ¡CONECTADO!
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(Modifier.weight(1f), "Citas Hoy", stats.citasHoy.toString(), Icons.Outlined.Event, colorPrincipal)

                    val colorAlerta = if (stats.alertasActivas > 0) Color(0xFFEF5350) else Color.Gray
                    AdminStatCard(Modifier.weight(1f), "Alertas", stats.alertasActivas.toString(), Icons.Outlined.Warning, colorAlerta)
                }
            }
        }

        // Actividad Reciente
        item {
            Text("Actividad Reciente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (stats.actividadesRecientes.isEmpty()) {
                        Text("Sin actividad reciente", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    } else {
                        stats.actividadesRecientes.forEach { actividad ->
                            ListItem(
                                headlineContent = { Text(actividad.titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                                supportingContent = { Text(actividad.subtitulo, fontSize = 12.sp) },
                                leadingContent = {
                                    val icono = if (actividad.esAlerta) Icons.Outlined.Warning else Icons.Outlined.Info
                                    Icon(icono, null, tint = if (actividad.esAlerta) Color(0xFFEF5350) else colorPrincipal)
                                }
                            )
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    modifier: Modifier,
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit = {} // Parámetro para recibir el evento
) {
    Card(
        modifier = modifier.clickable { onClick() }, // Hacemos clickeable toda la tarjeta
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icono, null, tint = colorIcono, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(titulo, fontSize = 12.sp, color = Color.Gray)
        }
    }
}