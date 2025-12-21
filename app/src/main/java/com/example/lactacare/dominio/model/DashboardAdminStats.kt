package com.example.lactacare.dominio.model

// Esta clase define QUÉ datos ve el admin
data class DashboardAdminStats(
    val totalUsuarios: Int,
    val totalDoctores: Int,
    val citasHoy: Int,
    val alertasActivas: Int,
    val actividadesRecientes: List<ActividadAdmin>
)

data class ActividadAdmin(
    val id: Int,
    val titulo: String,
    val subtitulo: String,
    val tipo: String // "ALERTA", "INFO", "NUEVO"
)