package com.example.cuentas_clarasapp.data.api.expense

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseRequestDto(
    val total_gastado: Double,
    val categoria: String,
    val img: String?,
    val fecha: String,
    val id_usuario: Int
)
