package com.example.lactacare.vistas.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// Modelo sellado para definir las rutas
sealed class ItemMenu(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    // --- RUTAS PACIENTE ---
    object PacienteInicio : ItemMenu("paciente_home", "Inicio", Icons.Default.Home)
    object PacienteBebe : ItemMenu("paciente_bebe", "Mi Bebé", Icons.Default.ChildCare)
    object PacienteChat : ItemMenu("paciente_chat", "Chat IA", Icons.Outlined.SmartToy)
    object PacientePerfil : ItemMenu("paciente_perfil", "Perfil", Icons.Default.Person)

    // --- RUTAS ADMIN / DOCTOR ---
    object AdminDashboard : ItemMenu("admin_dashboard", "Panel", Icons.Default.Dashboard)
    object AdminInventario : ItemMenu("admin_inventario", "Inventario", Icons.Default.Inventory)
    object AdminLactarios : ItemMenu("admin_lactarios", "Lactarios", Icons.Default.MeetingRoom)
    object AdminPerfil : ItemMenu("admin_perfil", "Perfil", Icons.Default.ManageAccounts)
}

// Listas estáticas para generar el menú
val menuPaciente = listOf(
    ItemMenu.PacienteInicio,
    ItemMenu.PacienteBebe,
    ItemMenu.PacienteChat,
    ItemMenu.PacientePerfil
)

val menuAdmin = listOf(
    ItemMenu.AdminDashboard,
    ItemMenu.AdminInventario,
    ItemMenu.AdminLactarios,
    ItemMenu.AdminPerfil
)