package com.example.lactacare.datos

import com.example.lactacare.dominio.model.Refrigerador
import com.example.lactacare.dominio.repository.RefrigeradorRepository
import kotlinx.coroutines.delay

class MockRefrigeradorRepository : RefrigeradorRepository {

    // BASE DE DATOS MUTABLE EN MEMORIA
    private val dbRefrigeradores = mutableListOf(
        // Refris de la Sala 1 (idLactario = 1)
        Refrigerador(id = 1, idLactario = 1, capacidadMax = 50, pisos = 2, filas = 5, columnas = 5),
        Refrigerador(id = 2, idLactario = 1, capacidadMax = 30, pisos = 2, filas = 3, columnas = 5),

        // Refris de la Sala 3 (idLactario = 3)
        Refrigerador(id = 3, idLactario = 3, capacidadMax = 100, pisos = 4, filas = 5, columnas = 5)
    )

    override suspend fun obtenerPorLactario(idLactario: Int): List<Refrigerador> {
        delay(300) // Simula carga
        return dbRefrigeradores.filter { it.idLactario == idLactario }
    }

    override suspend fun guardar(refrigerador: Refrigerador): Boolean {
        delay(500)
        return if (refrigerador.id == 0) {
            // CREAR (Nuevo ID)
            val nuevoId = (dbRefrigeradores.maxOfOrNull { it.id } ?: 0) + 1
            dbRefrigeradores.add(refrigerador.copy(id = nuevoId))
            true
        } else {
            // EDITAR
            val index = dbRefrigeradores.indexOfFirst { it.id == refrigerador.id }
            if (index != -1) {
                dbRefrigeradores[index] = refrigerador
                true
            } else {
                false
            }
        }
    }

    override suspend fun eliminar(id: Int): Boolean {
        delay(300)
        return dbRefrigeradores.removeIf { it.id == id } // Elimina si encuentra el ID
    }
}