package com.example.cuentas_clarasapp.data.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val mensaje: String? = null,
    val token: String? = null,
    val error: String? = null,
    val usuario: UsuarioDto? = null
)

@Serializable
data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val correo_electronico: String
)
