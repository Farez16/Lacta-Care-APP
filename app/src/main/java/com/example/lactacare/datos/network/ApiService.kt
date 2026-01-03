package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.PacienteDto
import com.example.lactacare.datos.dto.ReservaDto
import com.example.lactacare.datos.dto.UsuarioResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

import com.example.lactacare.datos.dto.ContenedorLecheDto
import com.example.lactacare.datos.dto.PreguntaRequest
import retrofit2.http.Headers
import retrofit2.http.DELETE
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
    suspend fun obtenerLactarios(): Response<List<com.example.lactacare.datos.dto.SalaLactanciaDto>>

    @POST("api/lactarios")
    suspend fun crearLactario(@Body lactario: com.example.lactacare.datos.dto.SalaLactanciaDto): Response<com.example.lactacare.datos.dto.SalaLactanciaDto>

    @PUT("api/lactarios/{id}")
    suspend fun editarLactario(
        @Path("id") id: Long,
        @Body lactario: com.example.lactacare.datos.dto.SalaLactanciaDto
    ): Response<com.example.lactacare.datos.dto.SalaLactanciaDto>

    @DELETE("api/lactarios/{id}")
    suspend fun eliminarLactario(@Path("id") id: Long): Response<Unit>

    // --- ALERTAS ---
    @GET("api/sistema-alertas")
    suspend fun obtenerAlertas(): Response<List<com.example.lactacare.datos.dto.SistemaAlertaDto>>

    // --- REFRIGERADORES ---
    @GET("api/refrigeradores")
    suspend fun obtenerRefrigeradores(): Response<List<com.example.lactacare.datos.dto.RefrigeradorDto>>

    @POST("api/refrigeradores")
    suspend fun crearRefrigerador(@Body refri: com.example.lactacare.datos.dto.RefrigeradorDto): Response<com.example.lactacare.datos.dto.RefrigeradorDto>

    @PUT("api/refrigeradores/{id}")
    suspend fun editarRefrigerador(
        @Path("id") id: Long,
        @Body refri: com.example.lactacare.datos.dto.RefrigeradorDto
    ): Response<com.example.lactacare.datos.dto.RefrigeradorDto>

    @DELETE("api/refrigeradores/{id}")
    suspend fun eliminarRefrigerador(@Path("id") id: Long): Response<Unit>

    // --- SUGERENCIAS ---
    @GET("api/sugerencias")
    suspend fun obtenerSugerencias(): Response<List<com.example.lactacare.datos.dto.SugerenciaDto>>

    @POST("api/sugerencias")
    suspend fun crearSugerencia(@Body sugerencia: com.example.lactacare.datos.dto.SugerenciaDto): Response<com.example.lactacare.datos.dto.SugerenciaDto>

    @DELETE("api/sugerencias/{id}")
    suspend fun eliminarSugerencia(@Path("id") id: Int): Response<Unit>

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
    // CHATBOT
    @POST("api/chat/preguntar")
    @Headers("Accept: text/plain")
    suspend fun preguntarChatbot(@Body request: PreguntaRequest): Response<String>

}