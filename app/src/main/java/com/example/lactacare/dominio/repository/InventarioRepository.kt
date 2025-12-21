package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.ContenedorLeche
import com.example.lactacare.dominio.model.Refrigerador

interface InventarioRepository {
    // Obtener la estructura física (Refrigeradores)
    suspend fun obtenerRefrigeradores(idLactario: Int): List<Refrigerador>

    // Obtener la leche guardada (Stock)
    suspend fun obtenerStockPorMadre(idMadre: Int): List<ContenedorLeche>

    // Acciones futuras
    suspend fun guardarContenedor(contenedor: ContenedorLeche): Boolean
    suspend fun retirarContenedor(idContenedor: Int): Boolean
}