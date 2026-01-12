package com.example.lactacare.datos
import java.time.LocalDate
import java.time.Period
import java.util.Locale

object ValidationUtils {

    /**
     * Valida cédula ecuatoriana usando algoritmo módulo 10.
     * Reglas: 10 dígitos, provincia válida, tercer dígito < 6.
     */
    fun esCedulaEcuatorianaValida(cedula: String): Boolean {
        if (cedula.length != 10) return false
        if (!cedula.all { it.isDigit() }) return false

        try {
            val provincia = cedula.substring(0, 2).toInt()
            val tercerDigito = cedula.substring(2, 3).toInt()

            if (provincia !in 1..24) return false
            if (tercerDigito >= 6) return false // Solo personas naturales

            val coeficientes = intArrayOf(2, 1, 2, 1, 2, 1, 2, 1, 2)
            var suma = 0

            for (i in 0 until 9) {
                var valor = cedula[i].toString().toInt() * coeficientes[i]
                if (valor >= 10) valor -= 9
                suma += valor
            }

            val digitoVerificador = cedula[9].toString().toInt()
            val decenaSuperior = if (suma % 10 == 0) suma else (suma / 10 + 1) * 10
            val resultado = decenaSuperior - suma

            return resultado == digitoVerificador
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Valida que sea mayor de 18 años y que la fecha no sea futura.
     */
    fun esMayorDeEdad(fechaNacimiento: LocalDate): Boolean {
        val hoy = LocalDate.now()
        // 1. Validación de seguridad: No permitir fechas futuras ni el día de hoy
        if (fechaNacimiento.isAfter(hoy) || fechaNacimiento.isEqual(hoy)) {
            return false
        }
        // 2. Cálculo de edad exacto
        val edad = Period.between(fechaNacimiento, hoy).years
        return edad >= 18
    }

    fun sanitizarTexto(texto: String): String {
        return texto.trim().replace("\\s+".toRegex(), " ")
    }

    fun capitalizarNombre(texto: String): String {
        if (texto.isBlank()) return ""
        return texto.split(" ").joinToString(" ") { palabra ->
            palabra.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }

    fun validarPassword(password: String): String? {
        if (password.length < 8) return "Mínimo 8 caracteres"
        if (!password.any { it.isUpperCase() }) return "Debe tener al menos 1 mayúscula"
        return null
    }

    // Lógica inteligente para separar nombres de Google
    fun procesarNombreGoogle(nombreCompleto: String): NombresGoogle {
        val partes = nombreCompleto.trim().split("\\s+".toRegex())
        return when (partes.size) {
            0 -> NombresGoogle("", "", "", "")
            1 -> NombresGoogle(partes[0], "", "", "") // Solo un nombre
            2 -> NombresGoogle(partes[0], "", partes[1], "") // Nombre y Apellido
            3 -> NombresGoogle(partes[0], partes[1], partes[2], "") // Dos nombres, un apellido (aprox)
            4 -> NombresGoogle(partes[0], partes[1], partes[2], partes[3]) // Completo
            else -> {
                // Caso > 4 palabras: Asignar 2 primeros a nombres y resto a apellidos
                val primerNombre = partes[0]
                val segundoNombre = partes[1]
                val primerApellido = partes[2]
                val segundoApellido = partes.drop(3).joinToString(" ")
                NombresGoogle(primerNombre, segundoNombre, primerApellido, segundoApellido)
            }
        }
    }
}

data class NombresGoogle(
    val primerNombre: String,
    val segundoNombre: String,
    val primerApellido: String,
    val segundoApellido: String
)