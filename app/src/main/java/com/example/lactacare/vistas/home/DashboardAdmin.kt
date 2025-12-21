package com.example.lactacare.vistas.home

import androidx.compose.foundation.background
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
import com.example.lactacare.vistas.theme.BordeGrisClean
import com.example.lactacare.vistas.theme.TextoOscuroClean

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
            CircularProgressIndicator(color = colorPrincipal)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
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

        item {
            Column {
                Text("Estadísticas Globales", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // CAMBIO: Ahora llamamos a 'AdminStatCard' para evitar conflictos
                    AdminStatCard(Modifier.weight(1f), "Usuarios", stats.totalUsuarios.toString(), Icons.Outlined.Person, colorPrincipal)
                    AdminStatCard(Modifier.weight(1f), "Doctores", stats.totalDoctores.toString(), Icons.Outlined.MedicalServices, colorPrincipal)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(Modifier.weight(1f), "Citas Hoy", stats.citasHoy.toString(), Icons.Outlined.Event, colorPrincipal)

                    val colorAlerta = if (stats.alertasActivas > 0) Color(0xFFEF5350) else Color.Gray
                    AdminStatCard(Modifier.weight(1f), "Alertas", stats.alertasActivas.toString(), Icons.Outlined.Warning, colorAlerta)
                }
            }
        }

        item {
            Text("Actividad Reciente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    stats.actividadesRecientes.forEach { actividad ->
                        ListItem(
                            headlineContent = { Text(actividad.titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                            supportingContent = { Text(actividad.subtitulo, fontSize = 12.sp) },
                            leadingContent = {
                                val icono = if (actividad.tipo == "ALERTA") Icons.Outlined.Warning else Icons.Outlined.Info
                                Icon(icono, null, tint = if (actividad.tipo == "ALERTA") Color(0xFFEF5350) else colorPrincipal)
                            }
                        )
                        Divider(color = BordeGrisClean.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

// CAMBIO: Le cambiamos el nombre a esta función para que sea ÚNICA en este archivo
@Composable
fun AdminStatCard(
    modifier: Modifier,
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icono, null, tint = colorIcono, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
            Text(titulo, fontSize = 12.sp, color = Color.Gray)
        }
    }
}