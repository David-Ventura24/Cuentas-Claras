package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.savings.SavingRequestDto
import com.example.cuentas_clarasapp.data.api.savings.SavingsResponseDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.Result

class SavingsRepository {
    suspend fun obtenerStatusAhorro(): Result<SavingsResponseDto> {
        return try {
            val token = ApiClient.obtenerTokenActual()
            val response = ApiClient.client.get("ahorros/status") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<SavingsResponseDto>())
            } else {
                Result.failure(Exception("Error al obtener ahorros: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarAhorro(monto: Double, tipo: String, nota: String): Result<Unit> {
        return try {
            val token = ApiClient.obtenerTokenActual()
            val response = ApiClient.client.post("ahorros/movimiento") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(SavingRequestDto(monto, tipo, nota))
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al registrar ahorro: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NUEVO MÉTODO: Conecta directamente el retiro de emergencia con la API
    suspend fun registrarRetiroEmergencia(monto: Double, motivo: String): Result<Unit> {
        return try {
            val token = ApiClient.obtenerTokenActual()

            // Reutilizamos tu endpoint 'ahorros/movimiento' mandando explícitamente "RETIRO"
            val response = ApiClient.client.post("ahorros/movimiento") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(SavingRequestDto(monto = monto, tipo = "RETIRO", nota = motivo))
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error en el servidor al retirar: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}