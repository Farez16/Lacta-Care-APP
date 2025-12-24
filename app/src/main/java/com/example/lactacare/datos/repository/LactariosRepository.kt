package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.SalaLactanciaDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.ILactariosRepository
import javax.inject.Inject

class LactariosRepository @Inject constructor(
    private val apiService: ApiService
) : ILactariosRepository {

    override suspend fun obtenerSalas(): Result<List<SalaLactanciaDto>> {
        return try {
            val response = apiService.obtenerSalasLactancia()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
