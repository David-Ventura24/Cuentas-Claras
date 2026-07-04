package com.example.cuentas_clarasapp.data.api.expense

import kotlinx.serialization.Serializable

@Serializable
data class ExpenseResponseDto(
    val mensaje: String? = null,
    val error: String? = null
)
