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

    PantallaPremiumAdmin(
        titulo = "Generar Reporte",
        onVolver = onVolver,
        floatingActionButton = {
            BotonPildora(
                text = "Exportar PDF",
                icon = Icons.Default.PictureAsPdf,
                onClick = {
                    val pdfService = PdfService(context)
                    val resultado = pdfService.generarReporte(uiState.stats)
                    if (resultado.isSuccess) {
                        Toast.makeText(context, "PDF guardado en Descargas: ${resultado.getOrNull()?.name}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error al generar PDF: ${resultado.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp).height(56.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ==========================================
            // SECCIÓN 1: FILTROS
            // ==========================================
            TarjetaPremium(titulo = "Filtros de Reporte") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Periodo", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BotonPildoraSeleccionable(
                            texto = "Día",
                            seleccionado = uiState.filtroFecha == FiltroFecha.Dia,
                            onClick = { viewModel.setFiltroFecha(FiltroFecha.Dia) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        BotonPildoraSeleccionable(
                            texto = "Semana",
                            seleccionado = uiState.filtroFecha == FiltroFecha.Semana,
                            onClick = { viewModel.setFiltroFecha(FiltroFecha.Semana) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        BotonPildoraSeleccionable(
                            texto = "Mes",
                            seleccionado = uiState.filtroFecha == FiltroFecha.Mes,
                            onClick = { viewModel.setFiltroFecha(FiltroFecha.Mes) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = MintPastel)

                    Text("Institución", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.filtroInstitucion?.nombreInstitucion ?: "Todas las Instituciones",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintPrimary,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas las Instituciones") },
                                onClick = { viewModel.setFiltroInstitucion(null); expanded = false }
                            )
                            uiState.instituciones.forEach { inst ->
                                DropdownMenuItem(
                                    text = { Text(inst.nombreInstitucion) },
                                    onClick = { viewModel.setFiltroInstitucion(inst); expanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECCIÓN 2: VISTA PREVIA & GRÁFICOS
            // ==========================================
            TarjetaPremium(titulo = "Visualización de Datos") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CleanBackground, RoundedCornerShape(50)) // Clean Background
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TabPildoraIcono(
                            icono = Icons.Default.BarChart,
                            seleccionado = uiState.tipoGrafico == TipoGrafico.Barras,
                            onClick = { viewModel.setTipoGrafico(TipoGrafico.Barras) },
                            modifier = Modifier.weight(1f)
                        )
                        TabPildoraIcono(
                            icono = Icons.Default.PieChart,
                            seleccionado = uiState.tipoGrafico == TipoGrafico.Pastel,
                            onClick = { viewModel.setTipoGrafico(TipoGrafico.Pastel) },
                            modifier = Modifier.weight(1f)
                        )
                        TabPildoraIcono(
                            icono = Icons.Default.ShowChart,
                            seleccionado = uiState.tipoGrafico == TipoGrafico.Lineas,
                            onClick = { viewModel.setTipoGrafico(TipoGrafico.Lineas) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().height(280.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = NeonPrimary)
                        } else if (uiState.stats != null) {
                            when (uiState.tipoGrafico) {
                                TipoGrafico.Barras -> SimpleBarChart(
                                    data = listOf(
                                        "Pacientes" to uiState.stats!!.totalUsuarios.toFloat(),
                                        "Médicos" to uiState.stats!!.totalDoctores.toFloat(),
                                        "Citas en Periodo" to uiState.stats!!.citasHoy.toFloat() // Label updated to reflect filtering
                                    ),
                                    color = NeonPrimary,
                                    modifier = Modifier.fillMaxSize().padding(16.dp)
                                )
                                TipoGrafico.Pastel -> SimplePieChart(
                                    data = listOf(
                                        "Activos" to uiState.stats!!.totalUsuarios.toFloat(),
                                        "Inactivos" to (uiState.stats!!.totalUsuarios * 0.1f)
                                    ),
                                    colors = listOf(NeonPrimary, NeonSecondary),
                                    modifier = Modifier.size(220.dp)
                                )
                                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.ShowChart, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Text("Gráfico de tendencias no disponible.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            Text("Sin datos para mostrar", color = Color.Gray)
                        }
                    }
                }
            }
            Spacer(Modifier.height(100.dp))
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
    val containerColor = if (seleccionado) NeonPrimary else Color.Transparent
    val contentColor = if (seleccionado) DarkCharcoal else Color.Gray.copy(alpha = 0.6f)
    val shadowElevation = if (seleccionado) 4.dp else 0.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = containerColor,
        shadowElevation = shadowElevation,
        modifier = modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icono, null, tint = contentColor, modifier = Modifier.size(20.dp))
        }
    }
}
