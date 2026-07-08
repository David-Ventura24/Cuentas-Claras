package com.example.cuentas_clarasapp.data.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(val correo_electronico: String)

@Serializable
data class ResetPasswordRequest(
    val correo_electronico: String,
    val token: String,
    val nueva_contrasena: String
)

@Serializable
data class ForgotPasswordResponse(
    val mensaje: String? = null,
    val error: String? = null
)