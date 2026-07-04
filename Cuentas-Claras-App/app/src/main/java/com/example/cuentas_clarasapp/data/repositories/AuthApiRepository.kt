package com.example.cuentas_clarasapp.data.repositories

import android.content.Context
import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.auth.AuthResponseDto
import com.example.cuentas_clarasapp.data.api.auth.LoginRequestDto
import com.example.cuentas_clarasapp.data.api.auth.RegistroRequestDto
import com.example.cuentas_clarasapp.data.local.SessionManager
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.*

class AuthApiRepository(context: Context) {

    private val sessionManager = SessionManager(context)

    suspend fun login(request: LoginRequestDto): AuthResponseDto {
        return try {
            val response = ApiClient.client.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val result: AuthResponseDto = response.body()

            if (result.error == null && result.token != null && result.usuario?.id != null) {
                sessionManager.guardarSesion(token = result.token, userId = result.usuario.id)
            }
            result
        } catch (e: Exception) {
            AuthResponseDto(error = e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun registrar(request: RegistroRequestDto): AuthResponseDto {
        return try {
            val response = ApiClient.client.post("auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val result: AuthResponseDto = response.body()

            if (result.error == null && result.token != null && result.usuario?.id != null) {
                sessionManager.guardarSesion(token = result.token, userId = result.usuario.id)
            }
            result
        } catch (e: Exception) {
            AuthResponseDto(error = e.localizedMessage ?: "Error de conexión")
        }
    }

    suspend fun recuperarPassword(correo: String): AuthResponseDto {
        return try {
            val response = ApiClient.client.post("auth/recuperar-password") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("correo_electronico" to correo))
            }
            response.body()
        } catch (e: Exception) {
            AuthResponseDto(error = e.localizedMessage ?: "Error de conexión")
        }
    }
}
