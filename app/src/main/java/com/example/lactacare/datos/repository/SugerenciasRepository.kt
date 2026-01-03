package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.SugerenciaDto
import com.example.lactacare.datos.network.ApiService
import javax.inject.Inject

class SugerenciasRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun obtenerSugerencias(): Result<List<SugerenciaDto>> {
        return try {
            val response = apiService.obtenerSugerencias()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener sugerencias: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearSugerencia(sugerencia: SugerenciaDto): Result<SugerenciaDto> {
        return try {
            val response = apiService.crearSugerencia(sugerencia)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al crear: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarSugerencia(id: Int): Result<Unit> {
        return try {
            val response = apiService.eliminarSugerencia(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al eliminar: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
