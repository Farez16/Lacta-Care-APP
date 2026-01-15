package com.example.lactacare.datos.network

import com.example.lactacare.datos.dto.BloqueHorarioDto
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
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody

interface ApiService {

    // 1. Obtener Doctores (URL confirmada en tu AdminEmployeeController)
    @GET("api/admin/empleados/listar-doctores")
    suspend fun obtenerDoctores(): Response<List<UsuarioResponseDto>>

    @GET("api/admin/empleados/listar-administradores")
    suspend fun obtenerAdministradores(): Response<List<UsuarioResponseDto>>

    // 2. Obtener Pacientes (Optimizado)
    @GET("api/pacientes/listar-dto")
    suspend fun obtenerPacientes(): Response<List<UsuarioResponseDto>>

    // 3. Obtener Reservas (Para contar las de hoy)
    @GET("api/movil/reservas")
    suspend fun obtenerReservas(): Response<List<ReservaDto>>

    // 4. Crear Empleado (Doctor/Admin)
    @POST("api/admin/empleados/crear")
    suspend fun crearEmpleado(@Body request: com.example.lactacare.datos.dto.CrearEmpleadoRequest): Response<com.example.lactacare.datos.dto.UsuarioResponseDto>

    @PUT("api/empleados/{id}")
    suspend fun actualizarEmpleado(
        @Path("id") id: Int, 
        @Body request: com.example.lactacare.datos.dto.PersonaEmpleadoUpdateDTO
    ): Response<com.example.lactacare.datos.dto.UsuarioResponseDto>

    @DELETE("api/empleados/{id}")
    suspend fun eliminarEmpleado(@Path("id") id: Int): Response<Unit>

    // --- HORARIOS EMPLEADO ---
    @POST("api/horarios-empleado")
    suspend fun crearHorarioEmpleado(@Body horario: com.example.lactacare.datos.dto.HorariosEmpleadoDto): Response<com.example.lactacare.datos.dto.HorariosEmpleadoDto>

    // --- DIAS LABORABLES ---
    @POST("api/dias-laborables") // Asumiendo endpoint estÃ¡ndar, verificar Controller si es necesario
    suspend fun crearDiasLaborables(@Body dias: com.example.lactacare.datos.dto.DiasLaborablesEmpleadoDto): Response<com.example.lactacare.datos.dto.DiasLaborablesEmpleadoDto>

    // --- ROLES ---
    @GET("api/roles")
    suspend fun obtenerRoles(): Response<List<com.example.lactacare.datos.dto.RolDto>>

    // 5. INVENTARIO (CONTENEDORES LECHE)
    @GET("api/contenedores-leche")
    suspend fun obtenerContenedoresLeche(): Response<List<ContenedorLecheDto>>

    @POST("api/contenedores-leche")
    suspend fun crearContenedorLeche(@Body request: com.example.lactacare.datos.dto.CrearContenedorRequest): Response<ContenedorLecheDto>

    @PUT("api/contenedores-leche/{id}")
    suspend fun actualizarContenedorLeche(
        @Path("id") id: Long, 
        @Body contenedor: ContenedorLecheDto
    ): Response<ContenedorLecheDto>

    // 6. SALAS DE LACTANCIA
    @GET("api/lactarios/activos")
    suspend fun obtenerLactarios(): Response<List<com.example.lactacare.datos.dto.SalaLactanciaDto>>

    @POST("api/lactarios")
    suspend fun crearLactario(@Body lactario: com.example.lactacare.datos.dto.SalaLactanciaDto): Response<com.example.lactacare.datos.dto.SalaLactanciaDto>

    @POST("api/lactarios/con-cubiculos")
    suspend fun crearLactarioConCubiculos(@Body dto: com.example.lactacare.datos.dto.SalaLactanciaConCubiculosDTO): Response<Any>

    @PUT("api/lactarios/{id}")
    suspend fun editarLactario(
        @Path("id") id: Long,
        @Body lactario: com.example.lactacare.datos.dto.SalaLactanciaDto
    ): Response<com.example.lactacare.datos.dto.SalaLactanciaDto>

    @DELETE("api/lactarios/{id}")
    suspend fun eliminarLactario(@Path("id") id: Long): Response<Unit>

    @GET("api/reservas/disponibilidad/{idSala}/{fecha}")
    suspend fun obtenerDisponibilidad(
        @Path("idSala") idSala: Long,
        @Path("fecha") fecha: String
    ): Response<List<BloqueHorarioDto>>

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

    @PUT("api/reservas/{id}")
    suspend fun actualizarReserva(
        @Path("id") id: Long,
        @Body reserva: com.example.lactacare.datos.dto.DoctorReservaDto
    ): Response<Map<String, Any>>

    // 8. PACIENTE
    @GET("api/reservas/paciente/{id}")
    suspend fun obtenerReservasPaciente(@Path("id") id: Long): Response<List<com.example.lactacare.datos.dto.ReservaPacienteDto>>

    @POST("api/reservas")
    suspend fun crearReserva(@Body request: com.example.lactacare.datos.dto.CrearReservaRequest): Response<Any>

    @PATCH("api/reservas/{id}/cancelar")
    suspend fun cancelarReserva(@Path("id") id: Long): Response<Unit>
    
    // CHATBOT
    @POST("api/chat/preguntar")
    @Headers("Accept: text/plain")
    suspend fun preguntarChatbot(@Body request: PreguntaRequest): Response<String>

    // ==================== INVENTARIO PACIENTE ====================
    
    @GET("api/movil/inventario/paciente/{idPaciente}")
    suspend fun obtenerInventarioPaciente(
        @Path("idPaciente") idPaciente: Long
    ): List<ContenedorLecheDto>
    
    @PUT("api/movil/inventario/retirar/{idContenedor}")
    suspend fun retirarContenedor(
        @Path("idContenedor") idContenedor: Long
    ): Response<String>
    
    // ==================== CUBÍCULOS ====================
    
    @GET("api/movil/cubiculos/sala/{idSala}")
    suspend fun obtenerCubiculosSala(
        @Path("idSala") idSala: Long
    ): List<com.example.lactacare.datos.dto.CubiculoDto>

    // ==================== PERFIL PACIENTE ====================
    
    @GET("api/movil/perfil/paciente/{id}")
    suspend fun obtenerPerfilPaciente(
        @Path("id") id: Long
    ): Response<com.example.lactacare.datos.dto.PacientePerfilDto>

    @PUT("api/movil/perfil/paciente/{id}")
    suspend fun actualizarPerfilPaciente(
        @Path("id") id: Long,
        @Body request: com.example.lactacare.datos.dto.ActualizarPerfilRequest
    ): Response<Unit>

    // ==================== INSTITUCIONES ====================
    
    @GET("api/instituciones")
    suspend fun obtenerInstituciones(): Response<List<com.example.lactacare.dominio.model.Institucion>>

    @GET("api/instituciones/{id}")
    suspend fun obtenerInstitucion(@Path("id") id: Long): Response<com.example.lactacare.dominio.model.Institucion>

    @POST("api/instituciones")
    suspend fun crearInstitucion(@Body institucion: com.example.lactacare.dominio.model.Institucion): Response<com.example.lactacare.dominio.model.Institucion>

    @PUT("api/instituciones/{id}")
    suspend fun editarInstitucion(
        @Path("id") id: Long,
        @Body institucion: com.example.lactacare.dominio.model.Institucion
    ): Response<com.example.lactacare.dominio.model.Institucion>

    @DELETE("api/instituciones/{id}")
    suspend fun eliminarInstitucion(@Path("id") id: Long): Response<Unit>

    // ==================== IA - DOCUMENTOS (Chatbot) ====================
    
    @retrofit2.http.Multipart
    @POST("api/documentos/upload")
    suspend fun subirDocumento(@retrofit2.http.Part archivo: MultipartBody.Part): Response<Map<String, Any>>

    @GET("api/documentos")
    suspend fun listarDocumentos(): Response<List<com.example.lactacare.datos.dto.DocumentoDto>>

    @DELETE("api/documentos/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Long): Response<Map<String, Any>>
}