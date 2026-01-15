package com.example.lactacare.vistas.admin.reportes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lactacare.util.PdfService
import com.example.lactacare.vistas.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReportesAdmin(
    onVolver: () -> Unit,
    viewModel: ReportesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // DatePicker State
    var showDateRangePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = datePickerState.selectedStartDateMillis
                    val endMillis = datePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                         val start = java.time.Instant.ofEpochMilli(startMillis).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                         val end = java.time.Instant.ofEpochMilli(endMillis).atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                         viewModel.setRangoFechas(start, end)
                    }
                    showDateRangePicker = false
                }) {
                    Text("OK", color = OliveAdmin)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("Cancelar", color = OliveTextSecondary) }
            },
             colors = DatePickerDefaults.colors(
                 containerColor = Color.White,
                 titleContentColor = OliveAdmin,
                 headlineContentColor = OliveTextPrimary,
                 weekdayContentColor = OliveTextSecondary,
                 subheadContentColor = OliveTextSecondary,
                 dayContentColor = OliveTextPrimary,
                 selectedDayContainerColor = OliveAdmin,
                 selectedDayContentColor = Color.White,
                 todayContentColor = OliveAdmin,
                 todayDateBorderColor = OliveAdmin
             )
        ) {
            DateRangePicker(state = datePickerState, title = { Text("Seleccionar Periodo", modifier = Modifier.padding(16.dp)) })
        }
    }

    PantallaPremiumAdmin(
        titulo = "Reportes y Estadí­sticas",
        onVolver = onVolver,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Exportar PDF", color = Color.White) }, // White text on Olive
                icon = { Icon(Icons.Default.PictureAsPdf, null, tint = Color.White) }, // White icon
                onClick = {
                    val pdfService = PdfService(context)
                    val resultado = pdfService.generarReporte(
                        uiState.stats, 
                        uiState.filtroTipoReporte.name, 
                        uiState.tipoGrafico.name
                    )
                    if (resultado.isSuccess) {
                        Toast.makeText(context, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error: ${resultado.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = OliveAdmin, // Olive FAB
                modifier = Modifier.padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(OliveBackground) // Subtle background
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ==========================================
            // SECCIÓN 1: FILTROS (DISEÑO MINIMALISTA)
            // ==========================================
            TarjetaPremium(titulo = "Filtros") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    // A. TIPO DE REPORTE Y PERIODO
                    Text("Configuración General", style = MaterialTheme.typography.labelMedium, color = OliveTextSecondary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Tipo Reporte Dropdown/Button? Use Pills for simplicity
                         listOf(FiltroTipoReporte.Reservas, FiltroTipoReporte.Usuarios, FiltroTipoReporte.Doctores).forEach { tipo ->
                            val isSelected = uiState.filtroTipoReporte == tipo
                             BotonPildoraSeleccionable(
                                texto = tipo.name,
                                seleccionado = isSelected,
                                onClick = { viewModel.setFiltroTipoReporte(tipo) },
                                modifier = Modifier.weight(1f)
                             )
                         }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // B. PERIODO (Con DatePicker Logic)
                    Text("Periodo de Tiempo", style = MaterialTheme.typography.labelMedium, color = OliveTextSecondary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(FiltroFecha.Dia, FiltroFecha.Semana, FiltroFecha.Mes).forEach { filtro ->
                             BotonPildoraSeleccionable(
                                texto = filtro.name,
                                seleccionado = uiState.filtroFecha == filtro,
                                onClick = { viewModel.setFiltroFecha(filtro) },
                                modifier = Modifier.weight(1f)
                             )
                        }
                         // CUSTOM DATE PICKER BUTTON
                         BotonPildoraSeleccionable(
                            texto = if(uiState.filtroFecha == FiltroFecha.Personalizado) "${uiState.fechaInicio} - ${uiState.fechaFin}" else "Rango",
                            seleccionado = uiState.filtroFecha == FiltroFecha.Personalizado,
                            onClick = { showDateRangePicker = true },
                            modifier = Modifier.weight(1.5f)
                         )
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // C. FILTROS RELACIONALES (Institución y Estado)
                    Text("Detalles Especí­ficos", style = MaterialTheme.typography.labelMedium, color = OliveTextSecondary)
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Dropdown Institucion
                        var expandedInst by remember { mutableStateOf(false) }
                        Box(Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { expandedInst = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if(uiState.filtroInstitucion != null) OliveAdmin else Color(0xFFE0E0E0)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OliveTextPrimary)
                            ) {
                                Text(
                                    text = uiState.filtroInstitucion?.nombreInstitucion ?: "Institución: Todas",
                                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            DropdownMenu(expanded = expandedInst, onDismissRequest = { expandedInst = false }, modifier = Modifier.background(Color.White)) {
                                DropdownMenuItem(text = { Text("Todas") }, onClick = { viewModel.setFiltroInstitucion(null); expandedInst = false })
                                uiState.instituciones.forEach { inst ->
                                    DropdownMenuItem(text = { Text(inst.nombreInstitucion) }, onClick = { viewModel.setFiltroInstitucion(inst); expandedInst = false })
                                }
                            }
                        }

                        // Dropdown Estado (Solo si es Reservas)
                        if (uiState.filtroTipoReporte == FiltroTipoReporte.Reservas) {
                            var expandedEstado by remember { mutableStateOf(false) }
                             Box(Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { expandedEstado = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if(uiState.filtroEstado != FiltroEstado.Todos) OliveAdmin else Color(0xFFE0E0E0)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OliveTextPrimary)
                                ) {
                                    Text(uiState.filtroEstado.name)
                                }
                                DropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }, modifier = Modifier.background(Color.White)) {
                                     FiltroEstado.values().forEach { estado ->
                                        DropdownMenuItem(text = { Text(estado.name) }, onClick = { viewModel.setFiltroEstado(estado); expandedEstado = false })
                                     }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECCIÓN 2: VISUALIZACIÓN
            // ==========================================
            TarjetaPremium(titulo = "Visualización") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Type Switcher (Simple Icons)
                     Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)) 
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabPildoraIcono(Icons.Default.BarChart, uiState.tipoGrafico == TipoGrafico.Barras, { viewModel.setTipoGrafico(TipoGrafico.Barras) }, Modifier.weight(1f))
                        TabPildoraIcono(Icons.Default.PieChart, uiState.tipoGrafico == TipoGrafico.Pastel, { viewModel.setTipoGrafico(TipoGrafico.Pastel) }, Modifier.weight(1f))
                        TabPildoraIcono(Icons.Default.ShowChart, uiState.tipoGrafico == TipoGrafico.Lineas, { viewModel.setTipoGrafico(TipoGrafico.Lineas) }, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(24.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = OliveAdmin)
                        } else if (uiState.stats != null) {
                            // Prepare Data based on Filter
                            val chartData = when (uiState.filtroTipoReporte) {
                                FiltroTipoReporte.Reservas -> listOf(
                                    "Total" to uiState.stats!!.citasHoy.toFloat(),
                                    "Semana" to (uiState.stats!!.citasSemana.size.toFloat())
                                )
                                // FIX: Mostrar desglose para filtro "Usuarios"
                                FiltroTipoReporte.Usuarios -> listOf(
                                    "Pacientes" to uiState.stats!!.totalUsuarios.toFloat(),
                                    "Doctores" to uiState.stats!!.totalDoctores.toFloat()
                                )
                                FiltroTipoReporte.Doctores -> listOf("Doctores" to uiState.stats!!.totalDoctores.toFloat())
                                else -> listOf("Total" to 0f)
                            }
                            
                            val pieColors = listOf(
                                Color(0xFF6A8759), // Olive
                                Color(0xFFFFD54F), // Amber
                                Color(0xFF4FC3F7), // Light Blue
                                Color(0xFF81C784)  // Green
                            )

                            when (uiState.tipoGrafico) {
                                TipoGrafico.Barras -> SimpleBarChart(
                                    data = chartData,
                                    color = OliveAdmin,
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                                TipoGrafico.Pastel -> SimplePieChart(
                                    data = chartData,
                                    colors = pieColors,
                                    modifier = Modifier.fillMaxSize().padding(32.dp)
                                )
                                TipoGrafico.Lineas -> SimpleLineChart(
                                    data = chartData,
                                    color = OliveAdmin,
                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                )
                            }
                        } else {
                            Text("Sin datos", color = Color.Gray)
                        }
                    }
                    
                    // Feedback text about what is showing
                    Text(
                        text = "Mostrando ${uiState.filtroTipoReporte.name} (${uiState.filtroEstado}) del periodo ${uiState.filtroFecha}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OliveTextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
fun TabPildoraIcono(
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp), // Softer rectangle for tabs
        color = if (seleccionado) Color.White else Color.Transparent,
        shadowElevation = if(seleccionado) 2.dp else 0.dp,
        modifier = modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icono, null, tint = if(seleccionado) OliveAdmin else Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
