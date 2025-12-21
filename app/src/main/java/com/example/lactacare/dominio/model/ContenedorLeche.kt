package com.example.lactacare.dominio.model

data class ContenedorLeche(
    val id: Int,
    val idAtencion: Int,
    val fechaExtraccion: String, // DATETIME
    val fechaCaducidad: String,  // DATETIME
    val estado: String,          // Ej: "Congelado"
    val cantidad: Double         // DECIMAL
)