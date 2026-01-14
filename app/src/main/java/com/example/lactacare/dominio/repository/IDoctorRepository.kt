package com.example.lactacare.dominio.repository

import com.example.lactacare.datos.dto.CrearAtencionRequest
import com.example.lactacare.datos.dto.DoctorReservaDto

interface IDoctorRepository {
    suspend fun obtenerAgendaDelDia(fecha: String): Result<List<DoctorReservaDto>>
    suspend fun crearAtencion(request: CrearAtencionRequest): Result<Boolean>
    suspend fun actualizarReserva(id: Long, reserva: DoctorReservaDto): Result<Boolean>
}
