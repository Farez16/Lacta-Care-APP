package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.IInventoryRepository
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val apiService: ApiService
) : IInventoryRepository {

    override suspend fun obtenerInventario(): Result<List<ContenedorLecheDto>> {
        return try {
            val response = apiService.obtenerContenedoresLeche()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarContenedor(id: Long, contenedor: ContenedorLecheDto): Result<ContenedorLecheDto> {
        return try {
            val response = apiService.actualizarContenedorLeche(id, contenedor)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
