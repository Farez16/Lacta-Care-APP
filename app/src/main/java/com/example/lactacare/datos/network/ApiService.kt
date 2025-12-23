package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.PacienteDto
import com.example.lactacare.datos.dto.ReservaDto
import com.example.lactacare.datos.dto.UsuarioResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

import com.example.lactacare.datos.dto.ContenedorLecheDto
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // 1. Obtener Doctores (URL confirmada en tu AdminEmployeeController)
    @GET("api/admin/empleados/listar-doctores")
    suspend fun obtenerDoctores(): Response<List<UsuarioResponseDto>>

    // 2. Obtener Pacientes (Optimizado)
    @GET("api/pacientes/listar-dto")
    suspend fun obtenerPacientes(): Response<List<UsuarioResponseDto>>

    // 3. Obtener Reservas (Para contar las de hoy)
    @GET("api/reservas")
    suspend fun obtenerReservas(): Response<List<ReservaDto>>

    // 4. Crear Empleado (Doctor/Admin)
    @POST("api/admin/empleados/crear")
    suspend fun crearEmpleado(@Body request: com.example.lactacare.datos.dto.CrearEmpleadoRequest): Response<Void>

    // 5. INVENTARIO (CONTENEDORES LECHE)
    @GET("api/contenedores-leche")
    suspend fun obtenerContenedoresLeche(): Response<List<ContenedorLecheDto>>

    @PUT("api/contenedores-leche/{id}")
    suspend fun actualizarContenedorLeche(
        @Path("id") id: Long, 
        @Body contenedor: ContenedorLecheDto
    ): Response<ContenedorLecheDto>

    // 6. SALAS DE LACTANCIA
    @GET("api/lactarios")
    suspend fun obtenerSalasLactancia(): Response<List<com.example.lactacare.datos.dto.SalaLactanciaDto>>

    // 7. DOCTOR
    @GET("api/reservas/fecha/{fecha}")
    suspend fun obtenerAgendaDoctor(
        @Path("fecha") fecha: String // Formato yyyy-MM-dd
    ): Response<List<com.example.lactacare.datos.dto.DoctorReservaDto>>

    @POST("api/atenciones")
    suspend fun crearAtencion(@Body request: com.example.lactacare.datos.dto.CrearAtencionRequest): Response<Any>

    // 8. PACIENTE
    @GET("api/reservas/paciente/{id}")
    suspend fun obtenerReservasPaciente(@Path("id") id: Long): Response<List<com.example.lactacare.datos.dto.ReservaPacienteDto>>

    @POST("api/reservas")
    suspend fun crearReserva(@Body request: com.example.lactacare.datos.dto.CrearReservaRequest): Response<Any>
}