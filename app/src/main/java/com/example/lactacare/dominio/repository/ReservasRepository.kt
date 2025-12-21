package com.example.lactacare.dominio.repository

import com.example.lactacare.dominio.model.Reservas

interface ReservasRepository {
    // Funciones para el Paciente (Mamá)
    suspend fun crearReserva(reserva: Reservas): Boolean
    suspend fun obtenerReservasPorPaciente(idPaciente: Int): List<Reservas> // <--- La que te falta
    suspend fun cancelarReserva(idReserva: Int): Boolean
    suspend fun obtenerProximaReservaPaciente(idPaciente: Int): Reservas?

    // Función para el Doctor (Agenda) - ¡Necesaria para la pantalla de Agenda!
    suspend fun obtenerReservasPorMedico(idMedico: Int): List<Reservas>
}