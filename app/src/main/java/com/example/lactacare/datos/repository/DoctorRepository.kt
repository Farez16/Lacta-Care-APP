package com.example.lactacare.datos.repository

import com.example.lactacare.datos.dto.DoctorReservaDto
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.repository.IDoctorRepository
import javax.inject.Inject

class DoctorRepository @Inject constructor(
    private val apiService: ApiService
) : IDoctorRepository {

    override suspend fun obtenerAgendaDelDia(fecha: String): Result<List<DoctorReservaDto>> {
        return try {
            val response = apiService.obtenerAgendaDoctor(fecha)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun obtenerEstadisticasDoctor(fecha: String): Result<com.example.lactacare.datos.dto.DoctorEstadisticasDto> {
        return try {
            val response = apiService.obtenerEstadisticasDoctor(fecha)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearAtencion(request: com.example.lactacare.datos.dto.CrearAtencionRequest): Result<Boolean> {
        return try {
            val response = apiService.crearAtencion(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarReserva(id: Long, reserva: DoctorReservaDto): Result<Boolean> {
        return try {
            val response = apiService.actualizarReserva(id, reserva)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
