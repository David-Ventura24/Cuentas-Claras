package com.example.cuentas_clarasapp.data.api.savings

import kotlinx.serialization.Serializable

@Serializable
data class SavingsResponseDto(
    val ahorro_neto: Double,
    val movimientos: List<AhorroNetDto>,
    val error: String? = null
)

@Serializable
data class AhorroNetDto(
    val id: Int,
    val monto: Double,
    val tipo: String, // INGRESO o RETIRO
    val nota: String,
    val fecha: String
)
