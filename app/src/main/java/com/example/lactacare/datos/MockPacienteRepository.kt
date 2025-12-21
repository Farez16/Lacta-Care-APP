package com.example.lactacare.datos

import com.example.lactacare.dominio.model.Paciente
import com.example.lactacare.dominio.repository.PacienteRepository
import kotlinx.coroutines.delay

class MockPacienteRepository : PacienteRepository {

    override suspend fun registrar(paciente: Paciente): Result<Boolean> {
        delay(2000)
        return if (paciente.cedula.isNotEmpty()) Result.success(true) else Result.failure(Exception("Falta cédula"))
    }

    override suspend fun login(correo: String, clave: String): Result<Boolean> {
        delay(1000)
        return Result.success(true)
    }

    // --- CORRECCIÓN: Usamos tus campos exactos ---
    override suspend fun obtenerPacientePorId(id: Int): Paciente? {
        delay(500)
        return Paciente(
            idPaciente = 1,
            cedula = "0101010101",
            primerNombre = "Sarah",    // Dato para el Home
            segundoNombre = "Maria",
            primerApellido = "Connor", // Dato para el Home
            segundoApellido = "Smith",
            correo = "sarah@lactacare.com",
            telefono = "0999999999",
            fechaNacimiento = "1995-05-20",
            discapacidad = "Ninguna",
            password = "password123",
            fotoPerfil = ""
        )
    }
}