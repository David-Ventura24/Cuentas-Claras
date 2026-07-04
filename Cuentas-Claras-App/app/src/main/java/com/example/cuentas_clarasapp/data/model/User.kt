package com.example.cuentas_clarasapp.data.model

data class User(
    val id: String, // Unificado a String para soportar los identificadores de Supabase
    val nombre: String,
    val correo: String
)
