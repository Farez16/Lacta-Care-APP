package com.example.lactacare.datos

import com.example.lactacare.dominio.model.Medico
import com.example.lactacare.dominio.repository.MedicoRepository
import kotlinx.coroutines.delay

class MockMedicoRepository : MedicoRepository {

    override suspend fun obtenerMedicoPorId(id: Int): Medico? {
        delay(500)
        return Medico(
            idEmpleado = 1,
            cedula = "0102030405",
            primerNombre = "Juan",
            segundoNombre = "Carlos",
            primerApellido = "Perez",
            segundoApellido = "Lopez",
            correo = "doctor@hospital.com",
            telefono = "0991234567",
            password = "admin"
        )
    }

    override suspend fun login(correo: String, clave: String): Result<Boolean> {
        delay(1000)
        return Result.success(true)
    }

    // --- ESTA ES LA FUNCIÓN QUE TE FALTABA ---
    override suspend fun registrar(medico: Medico): Result<Boolean> {
        delay(1500) // Simulamos tiempo de registro
        // Aquí podríamos validar datos, pero en un Mock devolvemos éxito siempre
        return Result.success(true)
    }
}