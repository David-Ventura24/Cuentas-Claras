package com.example.cuentas_clarasapp.data.api.profile

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponseDto(
    val nombre: String,
    val carrera: String,
    val moneda: String,
    val estadoCuenta: String,
    val error: String? = null
)
