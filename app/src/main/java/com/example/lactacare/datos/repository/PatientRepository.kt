package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.CrearReservaRequest
import com.example.lactacare.datos.dto.ReservaPacienteDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.IPatientRepository
import javax.inject.Inject

class PatientRepository @Inject constructor(
    private val apiService: ApiService
) : IPatientRepository {

    override suspend fun obtenerMisReservas(pacienteId: Long): Result<List<ReservaPacienteDto>> {
        return try {
            val response = apiService.obtenerReservasPaciente(pacienteId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearReserva(request: CrearReservaRequest): Result<Boolean> {
        return try {
            val response = apiService.crearReserva(request)
            if (response.isSuccessful || response.code() == 201) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
