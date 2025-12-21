package com.example.lactacare.dominio.model
import com.google.gson.annotations.SerializedName
data class Paciente(
    // Agregamos el ID como pediste (por defecto 0 para nuevos registros)
    // El servidor envía "id", Android lo guarda en "idPaciente"
    @SerializedName("id")
    val idPaciente: Int = 0,

    @SerializedName("cedula")
    val cedula: String = "",

    @SerializedName("primerNombre")
    val primerNombre: String = "",

    @SerializedName("segundoNombre")
    val segundoNombre: String = "",

    @SerializedName("primerApellido")
    val primerApellido: String = "",

    @SerializedName("segundoApellido")
    val segundoApellido: String = "",

    @SerializedName("correo")
    val correo: String = "",

    @SerializedName("telefono")
    val telefono: String = "",

    @SerializedName("fechaNacimiento")
    val fechaNacimiento: String = "",

    @SerializedName("discapacidad")
    val discapacidad: String? = null,

    @SerializedName("password")
    val password: String = "",

    // El servidor envía "imagenPerfil", Android lo guarda en "fotoPerfil"
    @SerializedName("imagenPerfil")
    val fotoPerfil: String = ""
)