package com.example.cuentas_clarasapp.data.api.budget

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BudgetRequestDto(
    @SerialName("cantidad_total") val cantidadTotal: Double,
    @SerialName("periodo") val periodo: String,
    @SerialName("porcentaje_ahorro") val porcentajeAhorro: Double
)
