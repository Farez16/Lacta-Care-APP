package com.example.lactacare.datos.dto

data class DoctorEstadisticasDto(
    val citasHoy: Int = 0,
    val pendientes: Int = 0,
    val atenciones: Int = 0,
    val solicitudesRetiro: Int = 0,
    val proximaReserva: DoctorReservaDto? = null
)
