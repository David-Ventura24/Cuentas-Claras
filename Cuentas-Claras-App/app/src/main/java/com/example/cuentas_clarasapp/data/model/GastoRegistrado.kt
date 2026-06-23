package com.example.cuentas_clarasapp.data.model

data class GastoRegistrado(
    val id: String,
    val categoriaId: String,
    val monto: Float,
    val nota: String,
    val fotoUri: String? = null,
    val fecha: Long = System.currentTimeMillis()
)
