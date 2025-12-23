package com.example.lactacare.dominio.model

data class Refrigerador(

    val id: Int,
    val idLactario: Int,
    val capacidadMax: Int,
    val pisos: Int,
    val filas: Int,
    val columnas: Int
)