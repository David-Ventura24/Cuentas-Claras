package com.example.cuentas_clarasapp.screens.history


import androidx.compose.ui.graphics.Color

data class GastoHistorial(
    val id: String,
    val descripcion: String,
    val monto: Double,
    val categoria: String,
    val fecha: String,
    val colorCategoria: Color
)

data class HistoryData(
    val mesAnioFiltro: String = "Mayo 2026",
    val totalGastadoMes: Double = 313.00,
    val transacciones: List<GastoHistorial> = emptyList()
)

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val data: HistoryData) : HistoryUiState
    data class Error(val mensaje: String) : HistoryUiState
}