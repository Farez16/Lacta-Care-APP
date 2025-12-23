package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.PacienteDto
import com.example.lactacare.datos.dto.ReservaDto
import com.example.lactacare.datos.dto.UsuarioResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    // 1. Obtener Doctores (URL confirmada en tu AdminEmployeeController)
    @GET("api/admin/empleados/listar-doctores")
    suspend fun obtenerDoctores(): Response<List<UsuarioResponseDto>>

    // 2. Obtener Pacientes
    // NOTA: Verifica en tu 'PersonaPacienteRestController' que la ruta sea esta:
    @GET("api/pacientes")
    suspend fun obtenerPacientes(): Response<List<PacienteDto>>

    // 3. Obtener Reservas (Para contar las de hoy)
    // NOTA: Verifica en tu 'ReservaRestController' que la ruta sea esta:
    @GET("api/reservas")
    suspend fun obtenerReservas(): Response<List<ReservaDto>>
}