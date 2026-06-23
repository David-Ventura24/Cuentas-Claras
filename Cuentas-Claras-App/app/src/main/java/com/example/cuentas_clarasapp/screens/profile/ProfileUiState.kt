package com.example.cuentas_clarasapp.screens.profile

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(
        val nombre: String,
        val carrera: String,
        val moneda: String,
        val estadoCuenta: String
    ) : ProfileUiState
    data class Error(val mensaje: String) : ProfileUiState
}