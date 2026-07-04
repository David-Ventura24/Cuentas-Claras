package com.example.cuentas_clarasapp.data.api.home

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HomeResponseDto(
    @SerialName("error") val error: String? = null,
    @SerialName("nombre_usuario") val nombre_usuario: String? = "Usuario",
    @SerialName("cantidad_disponible") val cantidad_disponible: Double? = 0.0,
    @SerialName("monto_total_configurado") val monto_total_configurado: Double? = 0.0,
    @SerialName("periodo") val periodo: String? = "Sin configurar",
    @SerialName("ahorro") val ahorro: Double? = 0.0,
    @SerialName("porcentaje_ahorro") val porcentaje_ahorro: Int? = 0, // 🌟 Nuevo campo
    @SerialName("limite_diario") val limite_diario: Double? = 0.0,
    @SerialName("total_gastado_hoy") val total_gastado_hoy: Double? = 0.0,
    @SerialName("total_gastado_ciclo") val total_gastado_ciclo: Double? = 0.0,
    @SerialName("gastos_hoy") val gastos_hoy: List<GastoNetDto> = kotlin.collections.emptyList()
)

@Serializable
data class GastoNetDto(
    val id: Int,
    val categoria: String,
    val monto: Double,
    val fecha: String
)
