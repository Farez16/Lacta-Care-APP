package com.example.lactacare.vistas.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// Representa un botón en la barra de navegación
sealed class ItemMenu(
    val titulo: String,
    val icono: ImageVector,
    val id: String // Para saber qué pantalla mostrar
) {
    // 1. Ítems FIJOS (Para todos)
    object Inicio : ItemMenu("Inicio", Icons.Filled.Home, "inicio")
    object Perfil : ItemMenu("Perfil", Icons.Filled.Person, "perfil")

    // 2. Ítems PACIENTE (Mamá)
    object Chatbot : ItemMenu("Chatbot", Icons.Filled.SmartToy, "chatbot")
    object Registros : ItemMenu("Registros", Icons.Filled.History, "registros")

    // 3. Ítems MÉDICO
    object Agenda : ItemMenu("Agenda", Icons.Filled.DateRange, "agenda")
    object Pacientes : ItemMenu("Pacientes", Icons.Filled.Groups, "pacientes")

    // 4. Ítems ADMIN
    object Empleados : ItemMenu("Empleados", Icons.Filled.Badge, "empleados")
    object Lactarios : ItemMenu("Lactarios", Icons.Filled.Analytics, "Lactarios")
}