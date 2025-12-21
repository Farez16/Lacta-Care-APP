package com.example.lactacare.datos

import com.example.lactacare.dominio.model.Bebe
import com.example.lactacare.dominio.repository.BebeRepository
import kotlinx.coroutines.delay

class MockBebeRepository : BebeRepository {

    // BASE DE DATOS EN MEMORIA (Ajustada a tu Entidad real de 5 campos)
    private val dbBebes = mutableListOf(
        Bebe(
            id = 500,
            idFamiliar = 1, // Pertenece al usuario logueado
            nombre = "Thiago",
            fechaNacimiento = "2024-06-15",
            genero = "Masculino"
        )
    )

    override suspend fun obtenerBebesPorFamiliar(idFamiliar: Int): List<Bebe> {
        delay(500)
        return dbBebes.filter { it.idFamiliar == idFamiliar }
    }

    // CORRECCIÓN: Renombramos 'agregarBebe' a 'registrarBebe'
    override suspend fun registrarBebe(bebe: Bebe): Boolean {
        delay(1000)
        return if (bebe.nombre.isNotEmpty()) {
            dbBebes.add(bebe)
            true
        } else {
            false
        }
    }

    override suspend fun obtenerBebePorId(id: Int): Bebe? {
        delay(200)
        return dbBebes.find { it.id == id }
    }
}