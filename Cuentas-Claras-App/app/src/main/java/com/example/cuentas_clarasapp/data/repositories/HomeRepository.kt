package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.home.HomeResponseDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.Result

class HomeRepository {

    suspend fun obtenerDatosHome(): Result<HomeResponseDto> {
        return try {
            val token = ApiClient.obtenerTokenActual()
            val response = ApiClient.client.get("home") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.CacheControl, "no-store, no-cache, must-revalidate, proxy-revalidate")
                header(HttpHeaders.Pragma, "no-cache")
                header(HttpHeaders.Expires, "0")
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<HomeResponseDto>()
                android.util.Log.d("HomeRepo", "CUERPO RECIBIDO: $body")
                Result.success(body)
            } else {
                android.util.Log.e("HomeRepo", "ERROR SERVER: ${response.status}")
                Result.failure(Exception("Error al conectar con el servidor: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
