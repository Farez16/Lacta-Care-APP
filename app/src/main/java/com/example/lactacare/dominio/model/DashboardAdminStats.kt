package com.example.lactacare.dominio.model

data class DashboardAdminStats(
    val totalUsuarios: Int,
    val totalDoctores: Int,
    val citasHoy: Int,
    val alertasActivas: Int,
    val actividadesRecientes: List<ActividadReciente>
)

data class ActividadReciente(
    val titulo: String,
    val subtitulo: String,
    val esAlerta: Boolean
)