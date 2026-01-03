package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.RefrigeradorDto
import com.example.lactacare.datos.network.ApiService
import javax.inject.Inject

class RefrigeradoresRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun obtenerRefrigeradores(): Result<List<RefrigeradorDto>> {
        return try {
            val response = apiService.obtenerRefrigeradores()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearRefrigerador(refri: RefrigeradorDto): Result<RefrigeradorDto> {
        return try {
            val response = apiService.crearRefrigerador(refri)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al crear: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editarRefrigerador(id: Long, refri: RefrigeradorDto): Result<RefrigeradorDto> {
        return try {
            val response = apiService.editarRefrigerador(id, refri)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al editar: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarRefrigerador(id: Long): Result<Unit> {
        return try {
            val response = apiService.eliminarRefrigerador(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
