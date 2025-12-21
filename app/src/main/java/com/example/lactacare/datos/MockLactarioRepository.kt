package com.example.lactacare.datos

import com.example.lactacare.dominio.model.Institucion
import com.example.lactacare.dominio.model.Lactario
import com.example.lactacare.dominio.repository.LactarioRepository
import kotlinx.coroutines.delay

class MockLactarioRepository : LactarioRepository {

    // --- CORRECCIÓN CLAVE ---
    // Movemos la lista al Companion Object para que sea "Global" en la memoria
    // Así, si sales de la pantalla y vuelves, los datos (y tus ediciones) siguen ahí.
    companion object {
        private val dbLactarios = mutableListOf(
            Lactario(1, "Sala 3A - Principal", "Piso 3, Bloque A", "sala3a@hosp.com", "0991234567", "-2.90055", "-79.00245", 100),
            Lactario(2, "Sala 2B - RRHH", "Piso 2, Oficina 204", "rrhh@hosp.com", "0987654321", "-2.90100", "-79.00300", 100),
            Lactario(3, "Sala 1 - Recepción", "Planta Baja", "recepcion@hosp.com", "0981122334", "-2.89900", "-79.00100", 100),
            Lactario(4, "Lactario Sur", "Bloque Sur, Cafetería", "sur@hosp.com", "0999999999", "-2.90500", "-79.00500", 101)
        )
    }

    override suspend fun obtenerLactarios(): List<Lactario> {
        delay(500)
        return dbLactarios.toList()
    }

    override suspend fun obtenerLactarioPorId(id: Int): Lactario? {
        delay(200)
        return dbLactarios.find { it.id == id }
    }

    override suspend fun obtenerTodos(): List<Lactario> {
        return dbLactarios.toList()
    }

    override suspend fun obtenerInstituciones(): List<Institucion> {
        return emptyList()
    }

    override suspend fun guardarLactario(lactario: Lactario): Boolean {
        delay(800)
        return if (lactario.id == 0) {
            val nuevoId = (dbLactarios.maxOfOrNull { it.id } ?: 0) + 1
            dbLactarios.add(lactario.copy(id = nuevoId))
            true
        } else {
            val index = dbLactarios.indexOfFirst { it.id == lactario.id }
            if (index != -1) {
                dbLactarios[index] = lactario
                true
            } else {
                false
            }
        }
    }

    override suspend fun eliminarLactario(id: Int): Boolean {
        delay(500)
        val item = dbLactarios.find { it.id == id }
        return if (item != null) {
            dbLactarios.remove(item)
            true
        } else {
            false
        }
    }
}