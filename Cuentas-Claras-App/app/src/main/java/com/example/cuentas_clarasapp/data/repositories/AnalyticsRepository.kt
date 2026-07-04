package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.analytics.AnalyticsResponseDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.Result

class AnalyticsRepository {
    suspend fun obtenerAnaliticas(): Result<AnalyticsResponseDto> {
        return try {
            val token = ApiClient.obtenerTokenActual()
            val response = ApiClient.client.get("grafica") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<AnalyticsResponseDto>())
            } else {
                Result.failure(Exception("Error al obtener analíticas: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
