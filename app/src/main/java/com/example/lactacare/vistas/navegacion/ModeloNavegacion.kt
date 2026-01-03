package com.example.lactacare.vistas.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning 
import androidx.compose.material.icons.filled.Kitchen // Refrigeradores
import androidx.compose.material.icons.filled.Feedback // Sugerencias
import androidx.compose.material.icons.filled.Image // Imagenes
import androidx.compose.material.icons.outlined.SmartToy
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
    object AdminDashboard : ItemMenu("admin_home", "Inicio", Icons.Default.Home)
    object AdminLactarios : ItemMenu("admin_lactarios", "Lactarios", Icons.Default.MeetingRoom)
    object AdminRefrigeradores : ItemMenu("admin_refrigeradores", "Refrigeradores", Icons.Default.Kitchen)
    object AdminPerfil : ItemMenu("admin_perfil", "Perfil", Icons.Default.ManageAccounts)

    // Ruta Gestión Usuarios (No en menú inferior, acceso desde Dashboard)
    object AdminGestionUsuarios : ItemMenu("admin_gestion_usuarios", "Usuarios", Icons.Default.ManageAccounts)
    
    // Ruta Menu Lateral
    object AdminIA : ItemMenu("admin_ia", "IA", Icons.Default.Description)
    object AdminAlertas : ItemMenu("admin_alertas", "Alertas", Icons.Default.Warning)
    object AdminSugerencias : ItemMenu("admin_sugerencias", "Sugerencias", Icons.Default.Feedback)
    object AdminImagenes : ItemMenu("admin_imagenes", "Imágenes", Icons.Default.Image)
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
    ItemMenu.AdminLactarios,
    ItemMenu.AdminRefrigeradores,
    ItemMenu.AdminPerfil
)