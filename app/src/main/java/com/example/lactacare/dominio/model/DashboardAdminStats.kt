package com.example.lactacare.dominio.model

data class DashboardAdminStats(
    val totalUsuarios: Int,
    val totalDoctores: Int,
    val citasHoy: Int,
    val alertasActivas: Int,
    val actividadesRecientes: List<ActividadReciente>,
    val citasSemana: List<Pair<String, Int>> = emptyList(),
    val crecimientoCitas: Double? = null, // Comparativa Mes Actual vs Anterior
    val institucion: com.example.lactacare.dominio.model.Institucion? = null
)

data class ActividadReciente(
    val titulo: String,
    val subtitulo: String,
    val esAlerta: Boolean
)