package com.example.lactacare.vistas.theme

import androidx.compose.ui.graphics.Color

// --- 1. PALETAS POR ROL (Usadas en Home, Login y Registro) ---

// PACIENTE (MAMÁ) - Estilo Tailwind solicitado
val MomPrimary = Color(0xFFFFC0CB)       // #FFC0CB (Rosado Pastel)
val MomAccent = Color(0xFFFFDDE2)        // #FFDDE2 (Rosado Muy Claro)
val MomText = Color(0xFF545454)          // #545454 (Gris Oscuro)
val MomBackground = Color(0xFFFFF9FB)    // #FFF9FB (Blanco Rosado)

// DOCTOR (MÉDICO) - Azul Clínico
val DoctorPrimary = Color(0xFF42A5F5)    // Azul Botón
val DoctorText = Color(0xFF0D47A1)       // Azul Texto Fuerte
val DoctorBackground = Color(0xFFE3F2FD) // Azul Fondo Suave

// ADMINISTRADOR - Verde Gestión (UPDATED TO NEON DESIGN)
val NeonPrimary = Color(0xFFAEF353)      // Verde Lima Neón High Voltage
val NeonSecondary = NeonPrimary.copy(alpha = 0.15f) // Fondo sutil
val DarkCharcoal = Color(0xFF222222)     // Negro Carbón
val CleanBackground = Color(0xFFF8F9FA)  // Gris hueso casi blanco

val AdminPrimary = NeonPrimary
val AdminText = DarkCharcoal
val AdminBackground = NeonSecondary

// --- 2. NEUTROS Y ESTILO "CLEAN" ---
val TextoOscuroClean = Color(0xFF546E7A)
val BordeGrisClean = Color(0xFFE0E0E0)
val FondoBlancoClean = Color(0xFFFEFEFE)
val FondoSwitch = Color(0xFFF5F5F5)

// --- 3. ALIAS DE COMPATIBILIDAD ---
val SlateGray = TextoOscuroClean
val LightGray = BordeGrisClean
val OffWhite = FondoBlancoClean
val White = Color.White
val TextSecondary = TextoOscuroClean // Alias for Admin Secondary Text

// --- 4. COLORES ESPECÍFICOS (LACTARIOS, ESTADO E INVENTARIO) ---
val MintPrimary = NeonPrimary // Alias to Neon for legacy admin screens
val LactarioBg = CleanBackground
val StatusGreen = Color(0xFF22C55E)
val StatusRed = Color(0xFFEF4444)
val StatusYellow = Color(0xFFEAB308)
val SoftPink = Color(0xFFF7C5CC)
val DistanceBg = Color(0xFFE0F2F1)

// Colores Inventario (HTML Design)
val InvFondo = Color(0xFFF6F8F6)
val InvPinkPrimary = Color(0xFFF472B6)
val InvPinkSoft = Color(0xFFFCE7F3)
val InvGreenText = Color(0xFF15803D)
val InvGreenBg = Color(0xFFF0FDF4)
val InvBlueText = Color(0xFF1D4ED8)
val InvBlueBg = Color(0xFFEFF6FF)
val InvRedText = Color(0xFFB91C1C)
val InvRedBg = Color(0xFFFEF2F2)
val InvAmberBg = Color(0xFFFFFBEB)
val InvZincText = Color(0xFF52525B)
val InvZincBg = Color(0xFFF4F4F5)

// --- 5. COLORES DASHBOARD PACIENTE (NUEVOS) ---
val DashboardBg = InvFondo // Reutilizamos el mismo fondo gris claro #F6F8F6
val DashboardTextDark = Color(0xFF333333)
val DashboardTextLight = Color(0xFF6B7280)
val DashboardPinkIcon = InvPinkPrimary // Reutilizamos el rosa fuerte #F472B6
val DashboardShadow = Color(0x0D000000)