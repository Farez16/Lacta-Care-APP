package com.example.lactacare.vistas.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.vistas.theme.TextoOscuroClean

@Composable
fun DashboardDoctor(colorPrimary: Color, colorAccent: Color) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Resumen Rápido
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardStatCard(modifier = Modifier.weight(1f), "Pacientes", "12", Icons.Outlined.Groups, colorPrimary)
                DashboardStatCard(modifier = Modifier.weight(1f), "Pendientes", "4", Icons.Outlined.PendingActions, Color(0xFFFFB74D))
            }
        }

        // Agenda del Día
        item {
            Text("Agenda de Hoy", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuroClean)
            Spacer(modifier = Modifier.height(12.dp))

            // Simulación de lista de pacientes
            val pacientes = listOf("María Pérez" to "10:00 AM", "Laura Gómez" to "11:30 AM", "Ana Ruiz" to "02:00 PM")

            pacientes.forEach { (nombre, hora) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp).background(colorAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(nombre.first().toString(), fontWeight = FontWeight.Bold, color = colorPrimary, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuroClean)
                            Text("Consulta Lactancia", fontSize = 12.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier.background(colorPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(hora, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorPrimary)
                        }
                    }
                }
            }
        }
    }
}