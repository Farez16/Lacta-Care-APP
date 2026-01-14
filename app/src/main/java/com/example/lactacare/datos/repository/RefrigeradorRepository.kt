package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.RefrigeradorDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.IRefrigeradorRepository
import javax.inject.Inject

class RefrigeradorRepository @Inject constructor(
    private val api: ApiService
) : IRefrigeradorRepository {

    override suspend fun obtenerRefrigeradores(): Result<List<RefrigeradorDto>> {
        return try {
            val response = api.obtenerRefrigeradores()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al listar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearRefrigerador(refri: RefrigeradorDto): Result<RefrigeradorDto> {
        return try {
            val response = api.crearRefrigerador(refri)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editarRefrigerador(id: Long, refri: RefrigeradorDto): Result<RefrigeradorDto> {
        return try {
            val response = api.editarRefrigerador(id, refri)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al editar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarRefrigerador(id: Long): Result<Unit> {
        return try {
            val response = api.eliminarRefrigerador(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
