package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.CrearReservaRequest
import com.example.lactacare.datos.dto.ReservaPacienteDto

interface IPatientRepository {
    suspend fun obtenerMisReservas(pacienteId: Long): Result<List<ReservaPacienteDto>>
    suspend fun crearReserva(request: CrearReservaRequest): Result<Boolean>
    suspend fun cancelarReserva(idReserva: Long): Result<Boolean>
}
