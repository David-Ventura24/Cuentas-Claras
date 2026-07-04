package com.example.cuentas_clarasapp.data.api.budget

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BudgetResponseDto(
    val mensaje: String? = null,
    val error: String? = null,
    val presupuesto: BudgetDataDto? = null
)

@Serializable
data class BudgetDataDto(
    @SerialName("id") val id: Int,
    @SerialName("id_usuario") val idUsuario: Int,
    @SerialName("cantidad_total") val cantidadTotal: Double,
    @SerialName("ahorro") val ahorro: Double,
    @SerialName("cantidad_disponible") val cantidadDisponible: Double,
    @SerialName("periodo") val periodo: String,
    @SerialName("dinero_disponible") val dineroDisponible: Double,
    @SerialName("limite_diario") val limiteDiario: Double
)
