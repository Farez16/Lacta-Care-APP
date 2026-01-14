package com.example.lactacare.datos.repository

import android.os.Build
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.model.ActividadReciente
import com.example.lactacare.dominio.model.DashboardAdminStats
import java.time.LocalDate
import javax.inject.Inject

// DTO Crudo para Reportes
data class ReporteDataRaw(
    val reservas: List<com.example.lactacare.datos.dto.ReservaDto>,
    val doctores: List<com.example.lactacare.datos.dto.UsuarioResponseDto>,
    val pacientes: List<com.example.lactacare.datos.dto.UsuarioResponseDto>
)

class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun obtenerDatosCrudosReporte(): ReporteDataRaw {
        return try {
            val doctores = apiService.obtenerDoctores().body() ?: emptyList()
            val pacientes = apiService.obtenerPacientes().body() ?: emptyList()
            val reservas = apiService.obtenerReservas().body() ?: emptyList()
            
            ReporteDataRaw(reservas, doctores, pacientes)
        } catch (e: Exception) {
            ReporteDataRaw(emptyList(), emptyList(), emptyList())
        }
    }

    suspend fun obtenerEstadisticas(): DashboardAdminStats? {
        return try {
            // 1. Llamadas en paralelo (o secuenciales por simplicidad)
            val doctores = apiService.obtenerDoctores().body() ?: emptyList()
            val pacientes = apiService.obtenerPacientes().body() ?: emptyList()
            val reservas = apiService.obtenerReservas().body() ?: emptyList()

            // 2. Calcular fecha de hoy
            val hoy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate.now().toString()
            } else {
                "2025-12-22" // Fallback seguro
            }

            // 3. Contar citas de HOY
            val citasHoyCount = reservas.count { it.fecha == hoy }

            // 4. Crear "Fake" actividad reciente con las últimas reservas reales
            val actividades = reservas.takeLast(3).map { reserva ->
                ActividadReciente(
                    titulo = "Reserva: ${reserva.salaLactancia?.nombre ?: "Sala"}",
                    subtitulo = "${reserva.personaPaciente?.nombreCompleto ?: "Paciente"} - ${reserva.horaInicio}",
                    esAlerta = false
                )
            }.reversed()

            // 5. Calcular Reservas de la Última Semana (para el gráfico)
            val diasSemana = mutableListOf<Pair<String, Int>>()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dayNameFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE") // Ej: Lun, Mar

            // Iteramos los últimos 7 días (incluyendo hoy)
            for (i in 6 downTo 0) {
                val fecha = java.time.LocalDate.now().minusDays(i.toLong())
                val fechaStr = fecha.format(formatter)
                val count = reservas.count { it.fecha == fechaStr }
                diasSemana.add(Pair(fecha.format(dayNameFormatter).uppercase().take(1), count))
            }

            // 6. CALCULO DE CRECIMIENTO (Mes Actual vs Mes Anterior)
            val mesActual = java.time.LocalDate.now().monthValue
            val mesAnterior = java.time.LocalDate.now().minusMonths(1).monthValue
            
            // Filtramos asumiendo formato yyyy-MM-dd
            val citasMesActual = reservas.count { 
                try { java.time.LocalDate.parse(it.fecha, formatter).monthValue == mesActual } catch(e:Exception) { false } 
            }
            val citasMesAnterior = reservas.count {
                try { java.time.LocalDate.parse(it.fecha, formatter).monthValue == mesAnterior } catch(e:Exception) { false }
            }

            val crecimiento = if (citasMesAnterior > 0) {
                ((citasMesActual - citasMesAnterior).toDouble() / citasMesAnterior) * 100
            } else {
                if (citasMesActual > 0) 100.0 else 0.0
            }

            // 7. Retornar objeto lleno
            DashboardAdminStats(
                totalUsuarios = pacientes.size,
                totalDoctores = doctores.size,
                citasHoy = citasHoyCount,
                alertasActivas = 0, // Alertas reales requieren endpoint de alertas
                actividadesRecientes = actividades,
                citasSemana = diasSemana,
                crecimientoCitas = crecimiento
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun obtenerListaDoctores(): List<com.example.lactacare.datos.dto.UsuarioResponseDto> {
        return try {
            val response = apiService.obtenerDoctores()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerListaAdministradores(): List<com.example.lactacare.datos.dto.UsuarioResponseDto> {
        return try {
            val response = apiService.obtenerAdministradores()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerListaPacientes(): List<com.example.lactacare.datos.dto.UsuarioResponseDto> {
        return try {
            val response = apiService.obtenerPacientes()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun crearEmpleado(request: com.example.lactacare.datos.dto.CrearEmpleadoRequest): Result<Boolean> {
        return try {
            val response = apiService.crearEmpleado(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val errorMsg = response.message()
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- NUEVAS FUNCIONES FASE B ---

    suspend fun eliminarUsuario(id: Int): Result<Unit> {
        return try {
            val response = apiService.eliminarEmpleado(id)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error al eliminar"))
        } catch(e: Exception) { Result.failure(e) }
    }

    suspend fun crearEmpleadoConDetalles(
        request: com.example.lactacare.datos.dto.CrearEmpleadoRequest,
        horario: com.example.lactacare.datos.dto.HorariosEmpleadoDto?,
        dias: com.example.lactacare.datos.dto.DiasLaborablesEmpleadoDto?,
        salaId: Int?
    ): Result<Boolean> {
        return try {
            // 1. Crear Empleado Base
            val respEmpleado = apiService.crearEmpleado(request)
            if (!respEmpleado.isSuccessful || respEmpleado.body() == null) {
                val errorJson = respEmpleado.errorBody()?.string() ?: ""
                val errorMsg = try {
                    org.json.JSONObject(errorJson).getString("message")
                } catch (e: Exception) {
                    respEmpleado.message() // Fallback
                }
                return Result.failure(Exception(errorMsg))
            }
            val nuevoEmpleado = respEmpleado.body()!!
            
            // 2. Crear Horario (si existe)
            var horarioId: Int? = null
            if (horario != null) {
                val respHorario = apiService.crearHorarioEmpleado(horario)
                if (respHorario.isSuccessful) {
                    horarioId = respHorario.body()?.id
                }
            }

            // 3. Crear DÃ­as Laborables (si existe)
            var diasId: Int? = null
            if (dias != null) {
                val respDias = apiService.crearDiasLaborables(dias)
                if (respDias.isSuccessful) {
                    diasId = respDias.body()?.id
                }
            }

            // 4. Actualizar Empleado con Vinculaciones
            if (horarioId != null || diasId != null || salaId != null) {
                val updateDto = com.example.lactacare.datos.dto.PersonaEmpleadoUpdateDTO(
                    horarioEmpleadoId = horarioId,
                    diasLaborablesEmpleadoId = diasId,
                    salaLactanciaId = salaId,
                    // Reenviamos datos obligatorios si el PUT lo requiere, o solo los cambios.
                    // Asumimos que el PUT actualiza selectivamente segÃºn el DTO
                    cedula = request.cedula // A veces necesario para identificar
                )
                // OJO: nuevoEmpleado.id podrÃ­a ser Long, verificar DTO. Api espera Int
                val respUpdate = apiService.actualizarEmpleado(nuevoEmpleado.id.toInt(), updateDto)
                if (!respUpdate.isSuccessful) {
                    return Result.failure(Exception("Creado, pero fallÃ³ vinculaciÃ³n: ${respUpdate.message()}"))
                }
            }
            Result.success(true)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarEmpleadoCompleto(
        idEmpleado: Int,
        datosPersonales: com.example.lactacare.datos.dto.PersonaEmpleadoUpdateDTO,
        horario: com.example.lactacare.datos.dto.HorariosEmpleadoDto?,
        dias: com.example.lactacare.datos.dto.DiasLaborablesEmpleadoDto?,
        salaId: Int?
    ): Result<Boolean> {
        return try {
            var horarioId: Int? = null
            if (horario != null) {
                val respHorario = apiService.crearHorarioEmpleado(horario)
                if (respHorario.isSuccessful) horarioId = respHorario.body()?.id
            }

            var diasId: Int? = null
            if (dias != null) {
                val respDias = apiService.crearDiasLaborables(dias)
                if (respDias.isSuccessful) diasId = respDias.body()?.id
            }

            // Combinamos los datos personales con los nuevos IDs vinculados
            val updateDto = datosPersonales.copy(
                horarioEmpleadoId = horarioId ?: datosPersonales.horarioEmpleadoId, // Usamos el nuevo o conservamos el existente si se pasÃ³
                diasLaborablesEmpleadoId = diasId ?: datosPersonales.diasLaborablesEmpleadoId,
                salaLactanciaId = salaId ?: datosPersonales.salaLactanciaId
            )

            val respUpdate = apiService.actualizarEmpleado(idEmpleado, updateDto)
            if (respUpdate.isSuccessful) {
                Result.success(true)
            } else {
                val errorMsg = try {
                     val errorJson = respUpdate.errorBody()?.string() ?: ""
                     org.json.JSONObject(errorJson).getString("message")
                } catch(e: Exception) { respUpdate.message() }
                Result.failure(Exception(errorMsg))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
