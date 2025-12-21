package com.example.lactacare.dominio.model

data class Bebe(
    val id: Int,
    val idFamiliar: Int, // FK a Persona_Paciente
    val nombre: String,
    val fechaNacimiento: String, // Puedes usar LocalDate si manejas fechas complejas
    val genero: String
)