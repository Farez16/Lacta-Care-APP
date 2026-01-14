package com.example.lactacare.vistas.paciente.reserva

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.datos.dto.BloqueHorarioDto
import com.example.lactacare.vistas.theme.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSeleccionarFechaHora(
    lactarioId: Long,
    nombreSala: String,
    cubiculoId: Long,
    nombreCubiculo: String,
    onNavigateBack: () -> Unit,
    onReservaConfirmada: () -> Unit,
    viewModel: SeleccionarFechaHoraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    // Inicializar ViewModel con cubículo
    LaunchedEffect(Unit) {
        viewModel.inicializar(lactarioId, nombreSala, cubiculoId)
    }
    // Manejar reserva exitosa
    if (uiState.reservaCreada) {
        LaunchedEffect(Unit) {
            onReservaConfirmada()
        }
    }
    // Manejar errores
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error ?: "Error desconocido") },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("Cerrar")
                }
            }
        )
    }
    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // ✅ CAMBIAR ESTA CONVERSIÓN
                            val fecha = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.of("UTC"))  // ✅ Usar UTC en lugar de systemDefault
                                .toLocalDate()
                            viewModel.seleccionarFecha(fecha)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Seleccionar Horario",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            uiState.nombreSala,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MomPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DashboardBg
    ) { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isLoading),
            onRefresh = { viewModel.cargarDisponibilidad() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de Fecha
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Fecha seleccionada",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                formatearFecha(uiState.fechaSeleccionada),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DashboardTextDark
                            )
                        }
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MomPrimary
                        )
                    }
                }
                // Título de horarios
                Text(
                    "Horarios disponibles",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DashboardTextDark
                )
                // Grid de horarios
                if (uiState.isLoading && uiState.bloques.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MomPrimary)
                    }
                } else if (uiState.bloques.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay horarios disponibles",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.bloques) { bloque ->
                            BloqueHorarioItem(
                                bloque = bloque,
                                isSelected = bloque == uiState.bloqueSeleccionado,
                                onClick = { viewModel.seleccionarBloque(bloque) }
                            )
                        }
                    }
                }
                // Botón Confirmar
                Button(
                    onClick = { viewModel.confirmarReserva() },
                    enabled = uiState.bloqueSeleccionado != null && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MomPrimary,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            if (uiState.bloqueSeleccionado != null)
                                "Confirmar Reserva (${uiState.bloqueSeleccionado!!.horaInicio.substring(0, 5)} - ${uiState.bloqueSeleccionado!!.horaFin.substring(0, 5)})"
                            else
                                "Selecciona un horario",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun BloqueHorarioItem(
    bloque: BloqueHorarioDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !bloque.disponible -> Color.LightGray
        isSelected -> MomPrimary
        else -> Color.White
    }
    val textColor = when {
        !bloque.disponible -> Color.Gray
        isSelected -> Color.White
        else -> DashboardTextDark
    }
    val borderColor = when {
        isSelected -> MomPrimary
        else -> Color.LightGray
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .aspectRatio(1.2f)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = bloque.disponible) { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bloque.horaInicio.substring(0, 5),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
fun formatearFecha(fecha: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    return fecha.format(formatter).replaceFirstChar { it.uppercase() }
}