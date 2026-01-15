package com.example.lactacare.vistas.doctor.ubicacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lactacare.datos.dto.RefrigeradorDisponibleDto
import com.example.lactacare.vistas.theme.DoctorPrimary

@Composable
fun PantallaAsignarCoordenadas(
    refrigerador: RefrigeradorDisponibleDto,
    contenedores: List<ContenedorConUbicacion>,
    contenedorActualIndex: Int,
    onAsignarUbicacion: (Int, Int, Int) -> Unit,
    onSiguiente: () -> Unit,
    onAnterior: () -> Unit,
    onFinalizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pisoSeleccionado by remember { mutableStateOf(1) }
    var filaSeleccionada by remember { mutableStateOf<Int?>(null) }
    var columnaSeleccionada by remember { mutableStateOf<Int?>(null) }

    val contenedorActual = contenedores.getOrNull(contenedorActualIndex)
    val esUltimo = contenedorActualIndex == contenedores.size - 1
    val esPrimero = contenedorActualIndex == 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Información del contenedor actual
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DoctorPrimary
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Contenedor ${contenedorActualIndex + 1} de ${contenedores.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${contenedorActual?.contenedor?.cantidadMl} ml",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Selector de piso
        Text("Seleccione Piso:", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (piso in 1..refrigerador.pisos) {
                FilterChip(
                    selected = pisoSeleccionado == piso,
                    onClick = { pisoSeleccionado = piso },
                    label = { Text("Piso $piso") }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Grilla de Filas x Columnas
        Text("Seleccione Ubicación (Fila x Columna):", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(refrigerador.columnas),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(refrigerador.filas * refrigerador.columnas) { index ->
                val fila = (index / refrigerador.columnas) + 1
                val columna = (index % refrigerador.columnas) + 1

                val estaOcupado = refrigerador.ubicacionesOcupadas.any {
                    it.piso == pisoSeleccionado && it.fila == fila && it.columna == columna
                }

                val estaAsignado = contenedores.any { cont ->
                    cont.piso == pisoSeleccionado && cont.fila == fila && cont.columna == columna
                }

                val estaSeleccionado = filaSeleccionada == fila && columnaSeleccionada == columna

                CeldaUbicacion(
                    fila = fila,
                    columna = columna,
                    estaOcupado = estaOcupado,
                    estaAsignado = estaAsignado,
                    estaSeleccionado = estaSeleccionado,
                    onClick = {
                        if (!estaOcupado && !estaAsignado) {
                            filaSeleccionada = fila
                            columnaSeleccionada = columna
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Leyenda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LeyendaItem(Color(0xFF66BB6A), "Disponible")
            LeyendaItem(Color.Red, "Ocupado")
            LeyendaItem(Color(0xFFFFA726), "Asignado")
        }

        Spacer(Modifier.height(16.dp))

        // Botones de navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!esPrimero) {
                OutlinedButton(
                    onClick = onAnterior,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Anterior")
                }
            }

            Button(
                onClick = {
                    if (filaSeleccionada != null && columnaSeleccionada != null) {
                        onAsignarUbicacion(pisoSeleccionado, filaSeleccionada!!, columnaSeleccionada!!)
                        if (esUltimo) {
                            onFinalizar()
                        } else {
                            onSiguiente()
                            filaSeleccionada = null
                            columnaSeleccionada = null
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = filaSeleccionada != null && columnaSeleccionada != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esUltimo) Color(0xFF66BB6A) else DoctorPrimary
                )
            ) {
                Text(if (esUltimo) "Finalizar" else "Siguiente")
                Spacer(Modifier.width(4.dp))
                Icon(if (esUltimo) Icons.Default.Check else Icons.Default.ArrowForward, null)
            }
        }
    }
}

@Composable
fun CeldaUbicacion(
    fila: Int,
    columna: Int,
    estaOcupado: Boolean,
    estaAsignado: Boolean,
    estaSeleccionado: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        estaOcupado -> Color.Red
        estaAsignado -> Color(0xFFFFA726)
        estaSeleccionado -> DoctorPrimary
        else -> Color(0xFF66BB6A)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .border(
                width = if (estaSeleccionado) 3.dp else 1.dp,
                color = if (estaSeleccionado) DoctorPrimary else backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !estaOcupado && !estaAsignado, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "F$fila",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
            Text(
                text = "C$columna",
                fontSize = 10.sp,
                color = backgroundColor
            )
        }
    }
}

@Composable
fun LeyendaItem(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .border(1.dp, color, RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(4.dp))
        Text(texto, fontSize = 12.sp)
    }
}
