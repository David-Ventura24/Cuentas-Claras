package com.example.cuentas_clarasapp.data.api.history

import kotlinx.serialization.Serializable

@Serializable
data class HistoryResponseDto(
    val error: String? = null,
    val totalGastadoMes: Double,
    val totalAhorradoMes: Double? = 0.0,
    val ahorro_neto: Double? = 0.0,
    val transacciones: List<TransaccionDto>
)

@Serializable
data class TransaccionDto(
    val id: String,
    val descripcion: String,
    val monto: Double,
    val categoria: String,
    val fecha: String,
    val colorHex: String
)