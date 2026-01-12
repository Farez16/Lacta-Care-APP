package com.example.lactacare.dominio.model

data class Lactario(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val correo: String,
    val telefono: String,
    val latitud: String,
    val longitud: String,
    val idInstitucion: Int,
    val horaApertura: String = "08:00",     // ✅ NUEVO
    val horaCierre: String = "18:00",       // ✅ NUEVO
    val nombreInstitucion: String = ""      // ✅ NUEVO
) {
    fun obtenerLatitudNum(): Double = latitud.toDoubleOrNull() ?: 0.0
    fun obtenerLongitudNum(): Double = longitud.toDoubleOrNull() ?: 0.0

    fun obtenerHorario(): String {          // ✅ NUEVO
        return "$horaApertura - $horaCierre"
    }
}