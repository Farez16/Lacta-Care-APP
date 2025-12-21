package com.example.lactacare.datos

import com.example.lactacare.dominio.model.ContenedorLeche
import com.example.lactacare.dominio.repository.InventarioRepository
import com.example.lactacare.dominio.model.Refrigerador
import kotlinx.coroutines.delay

class MockInventarioRepository : InventarioRepository {

    override suspend fun obtenerRefrigeradores(idLactario: Int): List<Refrigerador> {
        delay(500)
        return listOf(Refrigerador(1, idLactario, 100, 3, 5, 5))
    }

    override suspend fun obtenerStockPorMadre(idMadre: Int): List<ContenedorLeche> {
        delay(800)
        return listOf(
            ContenedorLeche(1, 101, "12 Dic 09:30", "2025-03-12", "Refrigerada", 150.0),
            ContenedorLeche(2, 102, "11 Dic 11:00", "2025-06-11", "Congelada", 120.0),
            ContenedorLeche(3, 103, "10 Dic 14:00", "2025-06-10", "Congelada", 200.0),
            ContenedorLeche(4, 104, "08 Dic 08:00", "2024-12-08", "Caducado", 80.0),

            // --- NUEVO ITEM: RETIRADA ---
            ContenedorLeche(
                id = 5,
                idAtencion = 105,
                fechaExtraccion = "07 Dic 14:00", // Fecha antigua
                fechaCaducidad = "2024-12-07",
                estado = "Retirada",              // Estado Solicitado
                cantidad = 100.0
            )
        )
    }

    override suspend fun guardarContenedor(contenedor: ContenedorLeche): Boolean {
        delay(1000); return true
    }

    override suspend fun retirarContenedor(idContenedor: Int): Boolean {
        delay(1000); return true
    }
}