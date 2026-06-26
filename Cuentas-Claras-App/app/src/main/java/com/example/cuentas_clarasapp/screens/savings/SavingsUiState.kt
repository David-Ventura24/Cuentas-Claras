package com.example.cuentas_clarasapp.screens.savings

import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity

sealed interface SavingsUiState {
    object Loading : SavingsUiState
    data class Success(
        val ahorroGlobalNeto: Double,
        val listaMovimientos: List<AhorroEntity>
    ) : SavingsUiState
    data class Error(val mensaje: String) : SavingsUiState
}