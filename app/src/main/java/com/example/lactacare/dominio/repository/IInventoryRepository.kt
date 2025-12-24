package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.ContenedorLecheDto

interface IInventoryRepository {
    suspend fun obtenerInventario(): Result<List<ContenedorLecheDto>>
    suspend fun actualizarContenedor(id: Long, contenedor: ContenedorLecheDto): Result<ContenedorLecheDto>
}
