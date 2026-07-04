package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.budget.BudgetRequestDto
import com.example.cuentas_clarasapp.data.api.budget.BudgetResponseDto
import com.example.cuentas_clarasapp.data.model.Budget
import com.example.cuentas_clarasapp.data.model.toDomain
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.Result

class BudgetApiRepository : BudgetRepository {

    override suspend fun obtenerPresupuesto(idUsuario: String): Result<Budget?> {
        return try {
            val response = ApiClient.client.get("presupuestos") {
                parameter("id_usuario", idUsuario)
            }
            if (response.status == HttpStatusCode.OK) {
                val dto = response.body<BudgetResponseDto>()
                Result.success(dto.toDomain())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun guardarPresupuesto(request: BudgetRequestDto): Result<Boolean> {
        return try {
            val token = ApiClient.obtenerTokenActual()
            android.util.Log.d("BudgetRepo", "📡 Enviando presupuesto al servidor: $request")
            
            val response = ApiClient.client.post("presupuestos") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }



            if (response.status.value in 200..299) {
                Result.success(true)
            } else {
                val bodyText = response.bodyAsText()
                android.util.Log.e("BudgetRepo", " Error del servidor (Cuerpo): $bodyText")
                
                val errorMsg = try {
                    val errorDto = response.body<BudgetResponseDto>()
                    errorDto.error ?: "Error del servidor (${response.status.value})"
                } catch (e: Exception) {
                    "Error ${response.status.value}: $bodyText"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("BudgetRepo", " Fallo crítico de red o tiempo de espera agotado: ${e.message}")
            Result.failure(e)
        }
    }
}
