package com.example.lactacare.dominio
import com.example.lactacare.dominio.model.Bebe
interface BebeRepository {
    suspend fun obtenerBebesPorFamiliar(idFamiliar: Int): List<Bebe>
    suspend fun registrarBebe(bebe: Bebe): Boolean

    // --- AGREGA ESTA LÍNEA QUE FALTA ---
    suspend fun obtenerBebePorId(id: Int): Bebe?
}