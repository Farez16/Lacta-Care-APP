package com.example.lactacare.vistas.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.dominio.model.Reservas
import com.example.lactacare.vistas.theme.DashboardBg
import com.example.lactacare.vistas.theme.DashboardPinkIcon
import com.example.lactacare.vistas.theme.DashboardTextDark
import com.example.lactacare.vistas.theme.DashboardTextLight
import com.example.lactacare.vistas.theme.MomAccent
import com.example.lactacare.vistas.theme.MomPrimary

@Composable
fun DashboardPaciente(
    colorPrimary: Color = MomPrimary,
    colorAccent: Color = MomAccent,
    nombreUsuario: String,
    proximaCita: Reservas?,
    nombreBebe: String?,

    // NAVEGACIÓN
    onNavReservas: () -> Unit, // Esto irá al Buscador (Nueva Reserva)
    onNavBebe: () -> Unit,
    onNavInfo: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // 1. TARJETA PRINCIPAL (Estado Actual)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Lactario Principal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DashboardTextLight)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (proximaCita != null) {
                        // CASO: TIENE CITA
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, null, tint = DashboardPinkIcon, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sala ${proximaCita.idLactario} Reservada", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DashboardTextDark)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tu reserva es hoy a las ${proximaCita.horaInicio}", fontSize = 16.sp, color = DashboardTextLight)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón secundario para ver detalles de ESTA cita
                        Button(
                            onClick = { /* TODO: Ir a Detalle Ticket */ },
                            colors = ButtonDefaults.buttonColors(containerColor = MomAccent, contentColor = Color(0xFFC13B84)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Ver Detalles", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                    } else {
                        // CASO: NO TIENE CITA
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.EventAvailable, null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sin reservas activas", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DashboardTextDark)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reserva tu espacio en el lactario ahora.", fontSize = 16.sp, color = DashboardTextLight)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón principal para Reservar
                        Button(
                            onClick = onNavReservas, // Va al buscador
                            colors = ButtonDefaults.buttonColors(containerColor = MomAccent, contentColor = Color(0xFFC13B84)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text("Reservar Ahora", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // 2. TARJETA MI BEBÉ
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MomAccent.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth().clickable { onNavBebe() }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(shape = CircleShape, color = Color.White, shadowElevation = 2.dp, modifier = Modifier.size(64.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.ChildCare, null, tint = DashboardPinkIcon, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = if (nombreBebe != null) nombreBebe else "Mi Bebé", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DashboardTextDark)
                    Text(text = if (nombreBebe != null) "Ver registro de crecimiento" else "Registra y sigue el crecimiento.", fontSize = 14.sp, color = DashboardTextLight, modifier = Modifier.padding(top = 4.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavBebe,
                        colors = ButtonDefaults.buttonColors(containerColor = MomAccent, contentColor = Color(0xFFC13B84)),
                        shape = RoundedCornerShape(50),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(if (nombreBebe != null) "Ver Perfil" else "Añadir Bebé", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. INFORMATIVO
        item {
            Column {
                Text("Informativo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DashboardTextDark, modifier = Modifier.padding(bottom = 12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val items = listOf(
                        Triple(Icons.Outlined.HealthAndSafety, "Beneficios", "Descubre ventajas"),
                        Triple(Icons.Outlined.WaterDrop, "Extracción", "Técnicas y consejos"),
                        Triple(Icons.Outlined.MenuBook, "Guía 101", "Guía para lactar")
                    )
                    items(items) { (icon, title, subtitle) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.width(160.dp).height(170.dp).clickable { onNavInfo(title) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(icon, null, tint = DashboardPinkIcon, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DashboardTextDark)
                                Text(subtitle, fontSize = 13.sp, color = DashboardTextLight, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        // 4. LISTA DE RESERVAS Y BOTÓN NUEVA (+)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // CABECERA DE SECCIÓN CON BOTÓN DE ACCIÓN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Próximas Reservas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DashboardTextDark)

                    // --- AQUÍ ESTÁ EL ACCESO SIEMPRE VISIBLE ---
                    TextButton(onClick = onNavReservas) {
                        Text("Nueva +", color = MomPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (proximaCita != null) {
                    ItemReservaDashboard(
                        sala = "Sala ${proximaCita.idLactario}",
                        hora = proximaCita.horaInicio,
                        duracion = "30 min"
                    )
                } else {
                    Text(
                        "No tienes reservas próximas.",
                        fontSize = 14.sp,
                        color = DashboardTextLight,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // 5. CONSEJO
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MomAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Outlined.Lightbulb, null, tint = DashboardPinkIcon, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Consejo del Día", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DashboardTextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("¡Mantenerse hidratada es clave! Intenta beber un vaso de agua cada vez que te extraigas leche.", fontSize = 14.sp, color = DashboardTextLight, lineHeight = 20.sp)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun ItemReservaDashboard(sala: String, hora: String, duracion: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(MomAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.EventAvailable, null, tint = DashboardPinkIcon, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("$sala - $hora", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DashboardTextDark)
                    Text("Duración: $duracion", fontSize = 14.sp, color = DashboardTextLight)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = DashboardTextLight.copy(alpha = 0.5f))
        }
    }
}