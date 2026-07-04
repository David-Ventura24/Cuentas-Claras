package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.model.Budget
import com.example.cuentas_clarasapp.data.api.budget.BudgetRequestDto
import kotlin.Result

interface BudgetRepository {
    suspend fun obtenerPresupuesto(idUsuario: String): Result<Budget?>
    suspend fun guardarPresupuesto(request: BudgetRequestDto): Result<Boolean>
}
