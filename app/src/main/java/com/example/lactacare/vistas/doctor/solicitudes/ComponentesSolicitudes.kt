package com.example.lactacare.vistas.doctor.solicitudes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.datos.dto.SolicitudRetiroDto
import com.example.lactacare.vistas.theme.DoctorPrimary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SolicitudCard(
    solicitud: SolicitudRetiroDto,
    onMarcarRetirada: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con paciente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = solicitud.nombrePaciente,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CI: ${solicitud.cedulaPaciente}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                Surface(
                    color = Color(0xFFFFA726).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "POR RETIRAR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFFFFA726),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // Información del contenedor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    icon = Icons.Default.LocalDrink,
                    label = "Cantidad",
                    value = "${solicitud.cantidadMl} ml"
                )
                
                InfoItem(
                    icon = Icons.Default.LocationOn,
                    label = "Ubicación",
                    value = solicitud.ubicacion
                )
            }

            Spacer(Modifier.height(12.dp))

            // Fechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Extracción",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatearFecha(solicitud.fechaExtraccion),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Caducidad",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatearFecha(solicitud.fechaCaducidad),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (estaCaducado(solicitud.fechaCaducidad)) Color.Red else Color.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botón de acción
            Button(
                onClick = onMarcarRetirada,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DoctorPrimary
                )
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(Modifier.width(8.dp))
                Text("Marcar como Retirada")
            }
        }
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DoctorPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DialogConfirmarRetiro(
    solicitud: SolicitudRetiroDto,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Confirmar Retiro") },
        text = {
            Column {
                Text("¿Confirmar que el paciente retiró este contenedor?")
                Spacer(Modifier.height(8.dp))
                Text("Paciente: ${solicitud.nombrePaciente}", fontWeight = FontWeight.Bold)
                Text("Cantidad: ${solicitud.cantidadMl} ml")
                Text("Ubicación: ${solicitud.ubicacion}")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DoctorPrimary
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

private fun formatearFecha(fechaStr: String): String {
    return try {
        val fecha = LocalDateTime.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) {
        fechaStr
    }
}

private fun estaCaducado(fechaStr: String): Boolean {
    return try {
        val fecha = LocalDateTime.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        fecha.isBefore(LocalDateTime.now())
    } catch (e: Exception) {
        false
    }
}
