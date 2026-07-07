package com.example.cuentas_clarasapp.data.api

import android.content.Context
import com.example.cuentas_clarasapp.data.local.SessionManager
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    const val BASE_URL = "https://cuentas-claras-production-a337.up.railway.app/api/"

    private var sessionManager: SessionManager? = null

    fun inicializar(context: Context) {
        sessionManager = SessionManager(context)
    }

    // Función para obtener el token actualizado en cada llamada
    fun obtenerTokenActual(): String? = sessionManager?.obtenerToken()

    val client by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000 // 15 segundos
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 15000
            }

            install(DefaultRequest) {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }
    }

    fun obtenerUsuarioIdActual(): Int = sessionManager?.obtenerUsuarioId() ?: -1
}
