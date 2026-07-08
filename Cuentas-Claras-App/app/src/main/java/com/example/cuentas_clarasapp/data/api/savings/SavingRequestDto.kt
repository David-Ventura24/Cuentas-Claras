package com.example.cuentas_clarasapp.data.api.savings

import kotlinx.serialization.Serializable

@Serializable
data class SavingRequestDto(
    val monto: Double,
    val tipo: String,
    val nota: String
)
