package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.expense.ExpenseRequestDto
import com.example.cuentas_clarasapp.data.api.expense.ExpenseResponseDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.Result

class ExpenseApiRepository : ExpenseRepository {

    override suspend fun puedeRegistrarGasto(monto: Double, idUsuario: Int): Boolean {
        return true
    }

    override suspend fun registrarGasto(request: ExpenseRequestDto): Result<Boolean> {
        return try {
            val token = ApiClient.obtenerTokenActual()
            val response = ApiClient.client.post("gastos") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                Result.success(true)
            } else {
                val body = response.body<ExpenseResponseDto>()
                Result.failure(Exception(body.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
