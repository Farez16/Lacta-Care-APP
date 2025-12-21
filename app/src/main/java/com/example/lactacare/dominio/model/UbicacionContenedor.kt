package com.example.lactacare.dominio.model

data class UbicacionContenedor(
    val id: Int,
    val idContenedor: Int,
    val idRefrigerador: Int,
    val piso: Int,
    val fila: Int,
    val columna: Int
)