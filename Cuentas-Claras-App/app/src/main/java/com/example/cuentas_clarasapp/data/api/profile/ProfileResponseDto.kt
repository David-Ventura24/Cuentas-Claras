package com.example.cuentas_clarasapp.data.api.profile

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ProfileResponseDto(
    val nombre: String,
    val carrera: String,
    val moneda: String,
    @SerialName("estadoCuenta") val estadoCuenta: String = "Activa", //  Valor por defecto
    val error: String? = null
)
