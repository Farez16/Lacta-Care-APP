package com.example.lactacare.dominio.model

data class Bebe(
    val idBebe: Int = 0,
    val nombre: String,
    val fechaNacimiento: String, // YYYY-MM-DD
    val peso: Double, // En kilogramos
    val talla: Double, // En centímetros
    val idFamiliar: Int // Relación con paciente
)