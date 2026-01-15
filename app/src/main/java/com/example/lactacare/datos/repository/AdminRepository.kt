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
    val pacientes: List<com.example.lactacare.datos.dto.UsuarioResponseDto>,
    val institucion: com.example.lactacare.dominio.model.Institucion? = null
)

class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun obtenerDatosCrudosReporte(): ReporteDataRaw {
        return try {
            val doctores = apiService.obtenerDoctores().body() ?: emptyList()
            val pacientes = apiService.obtenerPacientes().body() ?: emptyList()
            val reservas = apiService.obtenerReservas().body() ?: emptyList()
            
            val instituciones = apiService.obtenerInstituciones().body() ?: emptyList()
            
            ReporteDataRaw(reservas, doctores, pacientes, instituciones.firstOrNull())
        } catch (e: Exception) {
            ReporteDataRaw(emptyList(), emptyList(), emptyList(), null)
        }
    }

    suspend fun obtenerEstadisticas(periodo: String = "Mes"): DashboardAdminStats? {
        return try {
            // 1. Llamadas en paralelo
            val doctores = apiService.obtenerDoctores().body() ?: emptyList()
            val pacientes = apiService.obtenerPacientes().body() ?: emptyList()
            val reservas = apiService.obtenerReservas().body() ?: emptyList()

            // 2. Filtrar Reservas segÃºn Periodo (ImplementaciÃ³n Robusta)
            val hoy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.LocalDate.now()
            } else {
                 java.time.LocalDate.parse("2024-01-01") // Fallback
            }

            // Helper para parsear fechas de forma flexible
            fun parsearFecha(fechaStr: String?): java.time.LocalDate? {
                if (fechaStr.isNullOrEmpty()) return null
                return try {
                    if (fechaStr.contains("T")) {
                         java.time.LocalDateTime.parse(fechaStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
                    } else {
                         java.time.LocalDate.parse(fechaStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                } catch (e: Exception) {
                    try {
                        java.time.LocalDate.parse(fechaStr) // Default ISO
                    } catch (e2: Exception) {
                        null
                    }
                }
            }
            
            val reservasFiltradas = when (periodo) {
                "DÃ­a" -> reservas.filter { parsearFecha(it.fecha) == hoy }
                "Semana" -> {
                    val inicioSemana = hoy.minusDays(hoy.dayOfWeek.value.toLong() - 1) // Lunes
                    val finSemana = inicioSemana.plusDays(6) // Domingo
                    reservas.filter { 
                        val f = parsearFecha(it.fecha)
                        f != null && !f.isBefore(inicioSemana) && !f.isAfter(finSemana)
                    }
                }
                "Mes" -> reservas.filter { 
                     val f = parsearFecha(it.fecha)
                     f != null && f.month == hoy.month && f.year == hoy.year
                }
                else -> reservas 
            }

            // 3. Contar citas de HOY 
            val citasHoyCount = reservas.count { parsearFecha(it.fecha) == hoy }

            // 4. Crear "Fake" actividad reciente con las Ãºltimas reservas reales
            val actividades = reservas.takeLast(3).map { reserva ->
                ActividadReciente(
                    titulo = "Reserva: ${reserva.nombreSala ?: "Sala"}",
                    subtitulo = "${reserva.nombrePaciente ?: "Paciente"} ${reserva.apellidoPaciente ?: ""} - ${reserva.horaInicio}",
                    esAlerta = false
                )
            }.reversed()

            // 5. Calcular Reservas de la Ãšltima Semana (para el grÃ¡fico)
            val diasSemana = mutableListOf<Pair<String, Int>>()
            val dayNameFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE") 
            for (i in 6 downTo 0) {
                val fechaLoop = hoy.minusDays(i.toLong())
                // Contamos usando el helper
                val count = reservas.count { parsearFecha(it.fecha) == fechaLoop }
                diasSemana.add(Pair(fechaLoop.format(dayNameFormatter).uppercase().take(1), count))
            }

            // 6. CALCULO DE CRECIMIENTO (Mes Actual vs Mes Anterior) - Global
            val mesActual = hoy.monthValue
            val mesAnterior = hoy.minusMonths(1).monthValue
            val yearActual = hoy.year
            val yearAnterior = hoy.minusMonths(1).year // Cuidado con cambio de aÃ±o

            val citasMesActual = reservas.count { 
                val f = parsearFecha(it.fecha)
                f != null && f.monthValue == mesActual && f.year == yearActual
            }
            val citasMesAnterior = reservas.count { 
                 val f = parsearFecha(it.fecha)
                 f != null && f.monthValue == mesAnterior && f.year == yearAnterior
            }
            val crecimiento = if (citasMesAnterior > 0) ((citasMesActual - citasMesAnterior).toDouble() / citasMesAnterior) * 100 else 100.0



            // 7. Obtener Alertas Reales
            val alertas = apiService.obtenerAlertas().body() ?: emptyList()
            val alertasCount = alertas.size

            // 8. Obtener InstituciÃ³n
            val instituciones = apiService.obtenerInstituciones().body() ?: emptyList()
            val institucionPrincipal = instituciones.firstOrNull()

            // 9. Combinar Actividades
            val actividadesReservas = reservas.takeLast(5).map { reserva ->
                ActividadReciente(
                    titulo = "Reserva: ${reserva.nombreSala ?: "Sala"}",
                    subtitulo = "${reserva.nombrePaciente ?: "Paciente"} ${reserva.apellidoPaciente ?: ""} - ${reserva.horaInicio}",
                    esAlerta = false
                )
            }
            val actividadesAlertas = alertas.takeLast(5).map { alerta ->
                ActividadReciente(
                    titulo = "Alerta: ${alerta.tipoAlerta ?: "Sistema"}",
                    subtitulo = "${alerta.temperaturaAlerta}Â°C - ${alerta.fechaHoraAlerta}",
                    esAlerta = true
                )
            }
            val mixActividades = (actividadesAlertas + actividadesReservas).sortedByDescending { it.titulo }.take(5)

            // USAMOS LAS RESERVAS FILTRADAS PARA LA ESTADÃ STICA PRINCIPAL?
            // "Citas Hoy" suele ser global. "Pacientes" global. 
            // Quizas solo afecte al grafico? O a "Citas en Periodo"?
            // Por ahora, stats generales son globales, pero podrÃ­amos aÃ±adir un campo "citasFiltradas"
            // Pero Dashboard pule citasHoy. Vamos a devolver stats globales pero el grafico podria variar?
            // El usuario pidiÃ³ filtros. Lo lÃ³gico es que "Citas Hoy" cambie a "Citas Periodo"
            
            // Ajustamos 'citasHoy' para que refleje el conteo del periodo seleccionado si queremos reactividad
            val conteoPeriodo = reservasFiltradas.size

            DashboardAdminStats(
                totalUsuarios = pacientes.size,
                totalDoctores = doctores.size,
                citasHoy = citasHoyCount, // Mantenemos "Hoy" real por consistencia con etiqueta, o cambiamos semÃ¡ntica?
                // Mejor: Enviamos 'citasHoy' real para la tarjeta y usamos otro campo para el grafico si hiciera falta.
                // PERO, si el usuario filtra por "Mes", esperarÃ­a ver el de mes?
                // La UI dice "Citas Hoy". Vamos a dejarlo como HOY real.
                alertasActivas = alertasCount,
                actividadesRecientes = mixActividades,
                citasSemana = diasSemana,
                crecimientoCitas = crecimiento,
                institucion = institucionPrincipal
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
