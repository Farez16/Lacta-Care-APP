package com.example.lactacare.datos

import com.example.lactacare.dominio.*
import com.example.lactacare.dominio.model.ActividadAdmin
import com.example.lactacare.dominio.model.Administrador
import com.example.lactacare.dominio.model.DashboardAdminStats
import com.example.lactacare.dominio.repository.AdminRepository
import kotlinx.coroutines.delay

class MockAdminRepository : AdminRepository {

    private val listaActividades = listOf(
        ActividadAdmin(1, "Mantenimiento Sala 3", "Reporte de limpieza pendiente", "ALERTA"),
        ActividadAdmin(2, "Nuevo Doctor Registrado", "Dr. Juan Pérez (Pediatría)", "NUEVO"),
        ActividadAdmin(3, "Reserva Cancelada", "Paciente Maria L. - 10:00 AM", "INFO"),
        ActividadAdmin(4, "Stock Bajo", "Frascos estériles en Recepción", "ALERTA")
    )

    override suspend fun obtenerAdminPorId(id: Int): Administrador? {
        delay(500)
        return Administrador(
            idEmpleado = 1,
            cedula = "0999999999",
            primerNombre = "Ana",
            primerApellido = "Gomez",
            correo = "admin@sistema.com",

            fotoPerfil = ""
        )
    }

    override suspend fun obtenerEstadisticas(): DashboardAdminStats {
        delay(1000)
        return DashboardAdminStats(
            totalUsuarios = 1240,
            totalDoctores = 45,
            citasHoy = 89,
            alertasActivas = 3,
            actividadesRecientes = listaActividades
        )
    }

    override suspend fun login(correo: String, clave: String): Result<Boolean> {
        delay(1000)
        return Result.success(true)
    }

    override suspend fun registrar(admin: Administrador): Result<Boolean> {
        delay(1500)
        return Result.success(true)
    }
}