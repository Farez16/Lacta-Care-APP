package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.Refrigerador

interface RefrigeradorRepository {
    // Buscar todos los refris de UNA sala específica
    suspend fun obtenerPorLactario(idLactario: Int): List<Refrigerador>

    // Gestión (CRUD)
    suspend fun guardar(refrigerador: Refrigerador): Boolean
    suspend fun eliminar(id: Int): Boolean
}