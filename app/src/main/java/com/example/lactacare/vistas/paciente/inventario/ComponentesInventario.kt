package com.example.lactacare.vistas.paciente.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.vistas.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TarjetaContenedor(
    contenedor: ContenedorLecheDto,
    onRetirarClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con cantidad y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🥛 ${contenedor.cantidadMililitros ?: 0} ml",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DashboardPinkIcon
                )
                
                Badge(
                    containerColor = when (contenedor.estado?.uppercase()) {
                        "REFRIGERADA" -> Color(0xFF4CAF50)  // Verde
                        "CONGELADA" -> Color(0xFF00BCD4)   // Cyan
                        "RETIRADA" -> Color.Gray           // Gris
                        "CADUCADA" -> Color(0xFFF44336)    // Rojo
                        else -> Color(0xFFFF9800)          // Naranja
                    },
                    contentColor = Color.White
                ) {
                    Text(
                        text = contenedor.estado ?: "Desconocido",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fecha de extracción
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extracción: ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatearFecha(contenedor.fechaHoraExtraccion),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Fecha de caducidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Caduca: ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatearFecha(contenedor.fechaHoraCaducidad),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (estaCaducado(contenedor.fechaHoraCaducidad)) Color.Red else Color.Black
                )
            }

            // Botón Retirar (solo si está REFRIGERADA)
            if (contenedor.estado?.equals("Refrigerada", ignoreCase = true) == true) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onRetirarClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MomPrimary
                    )
                ) {
                    Text("Retirar")
                }
            }
        }
    }
}

@Composable
fun DialogConfirmarRetiro(
    contenedor: ContenedorLecheDto,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text("Confirmar Retiro")
        },
        text = {
            Column {
                Text("¿Deseas retirar este contenedor?")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cantidad: ${contenedor.cantidadMililitros} ml", fontWeight = FontWeight.Bold)
                Text("Estado: ${contenedor.estado}")
                Text("Fecha: ${formatearFecha(contenedor.fechaHoraExtraccion)}")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

// Función auxiliar para formatear fechas
private fun formatearFecha(fechaISO: String?): String {
    return try {
        val fecha = LocalDateTime.parse(fechaISO, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        "Fecha no disponible"
    }
}

// Función auxiliar para verificar si está caducado
private fun estaCaducado(fechaISO: String?): Boolean {
    return try {
        val fechaCaducidad = LocalDateTime.parse(fechaISO, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        fechaCaducidad.isBefore(LocalDateTime.now())
    } catch (e: Exception) {
        false
    }
}
