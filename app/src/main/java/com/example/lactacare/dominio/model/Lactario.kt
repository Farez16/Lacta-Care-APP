package com.example.lactacare.dominio.model

data class Lactario(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val correo: String,
    val telefono: String,
    val latitud: String,
    val longitud: String,
    val idInstitucion: Int
) {
    fun obtenerLatitudNum(): Double = latitud.toDoubleOrNull() ?: 0.0
    fun obtenerLongitudNum(): Double = longitud.toDoubleOrNull() ?: 0.0
}