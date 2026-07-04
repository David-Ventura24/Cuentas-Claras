package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.history.HistoryResponseDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.Result

class HistoryRepository {
    suspend fun obtenerHistorial(mes: Int, anio: Int): Result<HistoryResponseDto> {
        return try {
            val token = ApiClient.obtenerTokenActual()
            val response = ApiClient.client.get("gastos/historial") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("mes", mes)
                parameter("anio", anio)
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<HistoryResponseDto>())
            } else {
                Result.failure(Exception("Error servidor: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
