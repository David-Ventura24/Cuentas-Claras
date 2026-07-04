package com.example.cuentas_clarasapp.data.model

import com.example.cuentas_clarasapp.data.api.budget.BudgetResponseDto

data class Budget(
    val id: Int,
    val idUsuario: Int,
    val cantidadTotal: Double,
    val ahorro: Double,
    val cantidadDisponible: Double,
    val periodo: String,
    val dineroDisponible: Double,
    val limiteDiario: Double
)

// Conversor de Red a Modelo de la App
fun BudgetResponseDto.toDomain(): Budget {
    val data = this.presupuesto ?: throw Exception("Presupuesto no encontrado")
    return Budget(
        id = data.id,
        idUsuario = data.idUsuario,
        cantidadTotal = data.cantidadTotal,
        ahorro = data.ahorro,
        cantidadDisponible = data.cantidadDisponible,
        periodo = data.periodo,
        dineroDisponible = data.dineroDisponible,
        limiteDiario = data.limiteDiario
    )
}
