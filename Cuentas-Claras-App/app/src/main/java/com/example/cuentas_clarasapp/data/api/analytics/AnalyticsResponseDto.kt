package com.example.cuentas_clarasapp.data.api.analytics

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AnalyticsResponseDto(
    @SerialName("gastos_totales_general") val total: Double,
    @SerialName("distribucion") val categorias: List<CategoriaAnalyticsDto>,
    val error: String? = null
)

@Serializable
data class CategoriaAnalyticsDto(
    val categoria: String,
    @SerialName("total_gastado") val monto: Double, //  Sincronizado con el server.js
    val porcentaje: Float
)
