package com.example.lactacare.dominio.model

data class Reservas(
    val idReserva: Int = 0,
    val idPaciente: Int,
    val idLactario: Int,
    val fecha: String, // YYYY-MM-DD
    val horaInicio: String, // HH:mm
    val horaFin: String, // HH:mm
    val estado: String = "ACTIVA" // ACTIVA, COMPLETADA, CANCELADA
)