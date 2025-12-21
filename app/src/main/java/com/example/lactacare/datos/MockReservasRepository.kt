package com.example.lactacare.datos

import com.example.lactacare.dominio.model.Reservas
import com.example.lactacare.dominio.repository.ReservasRepository
import kotlinx.coroutines.delay

class MockReservasRepository : ReservasRepository {

    // --- ESTA ES NUESTRA "BASE DE DATOS" CENTRALIZADA ---
    // Simulamos que el usuario logueado es siempre el ID 1.
    private val dbReservas = mutableListOf(
        // CITA REAL DEL USUARIO (ID 1)
        Reservas(
            id = 100,
            idPaciente = 1, // <--- ESTE ERES TÚ
            idLactario = 3, // Sala 3A
            estado = "Confirmada",
            fecha = "Hoy",
            horaInicio = "14:30",
            horaFin = "15:00"
        ),

        // CITA DE OTRA PERSONA (Para que veas otras salas ocupadas)
        Reservas(
            id = 101,
            idPaciente = 99, // <--- OTRA MAMÁ
            idLactario = 2, // Sala 2B
            estado = "Confirmada",
            fecha = "Hoy",
            horaInicio = "10:00",
            horaFin = "10:30"
        )
    )

    // 1. Obtener historial (Solo tus citas)
    override suspend fun obtenerReservasPorPaciente(idPaciente: Int): List<Reservas> {
        delay(500)
        // Filtramos la DB real por tu ID
        return dbReservas.filter { it.idPaciente == idPaciente }
    }

    // 2. Crear reserva (Añade a la DB real)
    override suspend fun crearReserva(reserva: Reservas): Boolean {
        delay(1000)
        return if (reserva.horaInicio.isNotEmpty()) {
            dbReservas.add(reserva) // ¡SE GUARDA EN MEMORIA!
            true
        } else {
            false
        }
    }

    // 3. Cancelar (Borra de la DB real)
    override suspend fun cancelarReserva(idReserva: Int): Boolean {
        delay(500)
        return dbReservas.removeIf { it.id == idReserva }
    }

    // 4. Próxima Reserva (Busca en la DB real)
    override suspend fun obtenerProximaReservaPaciente(idPaciente: Int): Reservas? {
        delay(500)
        // Buscamos dinámicamente si el usuario 1 tiene algo pendiente hoy
        return dbReservas.find {
            it.idPaciente == idPaciente &&
                    (it.estado == "Confirmada" || it.estado == "Pendiente")
        }
    }

    // 5. Agenda Global (Devuelve toda la DB para pintar el mapa)
    override suspend fun obtenerReservasPorMedico(idMedico: Int): List<Reservas> {
        delay(1000)
        return dbReservas // Devuelve todo para saber qué salas pintar de amarillo
    }
}