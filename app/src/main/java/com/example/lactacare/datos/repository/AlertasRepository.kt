package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.SistemaAlertaDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.IAlertasRepository
import javax.inject.Inject

class AlertasRepository @Inject constructor(
    private val apiService: ApiService
) : IAlertasRepository {

    override suspend fun obtenerAlertas(): Result<List<SistemaAlertaDto>> {
        return try {
            val response = apiService.obtenerAlertas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
