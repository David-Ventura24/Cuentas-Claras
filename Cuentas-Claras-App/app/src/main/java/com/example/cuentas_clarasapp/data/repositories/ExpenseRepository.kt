package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.api.expense.ExpenseRequestDto
import kotlin.Result

interface ExpenseRepository {
    suspend fun puedeRegistrarGasto(monto: Double, idUsuario: Int): Boolean
    suspend fun registrarGasto(request: ExpenseRequestDto): Result<Boolean>
}
