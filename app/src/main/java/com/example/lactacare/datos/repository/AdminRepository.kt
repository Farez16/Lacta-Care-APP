package com.example.lactacare.datos.repository

import android.os.Build
import com.example.lactacare.datos.network.ApiService
import com.example.lactacare.dominio.model.ActividadReciente
import com.example.lactacare.dominio.model.DashboardAdminStats
import java.time.LocalDate
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {

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
                    titulo = "Reserva: ${reserva.nombreSala ?: "Sala"}",
                    subtitulo = "${reserva.nombrePaciente ?: "Paciente"} - ${reserva.horaInicio}",
                    esAlerta = false
                )
            }.reversed()

            // 5. Retornar objeto lleno
            DashboardAdminStats(
                totalUsuarios = pacientes.size,
                totalDoctores = doctores.size,
                citasHoy = citasHoyCount,
                alertasActivas = 0,
                actividadesRecientes = actividades
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

    suspend fun obtenerListaPacientes(): List<com.example.lactacare.datos.dto.PacienteDto> {
        return try {
            val response = apiService.obtenerPacientes()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
