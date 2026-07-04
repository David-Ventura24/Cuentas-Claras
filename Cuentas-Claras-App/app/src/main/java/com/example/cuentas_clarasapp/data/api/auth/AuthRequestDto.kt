package com.example.cuentas_clarasapp.data.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val correo_electronico: String,
    val contrasena: String
)

@Serializable
data class RegistroRequestDto(
    val nombre: String,
    val correo_electronico: String,
    val contrasena: String
)
