package com.example.lactacare.dominio.model

data class Reservas(
    val id: Int,
    val idLactario: Int,
    val idPaciente: Int,
    val estado: String, // "Pendiente", "Confirmada", etc.
    val fecha: String,  // Mapea a DATE
    val horaInicio: String, // Mapea a TIME
    val horaFin: String
)