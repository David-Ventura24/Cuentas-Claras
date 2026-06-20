package com.example.cuentas_clarasapp.screens.expense

import androidx.compose.ui.graphics.Color

// Modelo de datos para cada segmento de la gráfica y la lista inferior
data class CategoriaGastoData(
    val nombre: String,
    val porcentaje: Float,
    val monto: Double,
    val color: Color
)

// Datos globales requeridos por la pantalla
data class ExpenseAnalyticsData(
    val mesAnioFiltro: String = "Mayo 2026",
    val montoTotalGastado: Double = 313.00,
    val categorias: List<CategoriaGastoData> = emptyList()
)

// Estados de la UI para sincronización y manejo de peticiones de red asíncronas
sealed interface AddExpenseUiState {
    object Loading : AddExpenseUiState
    data class Success(val data: ExpenseAnalyticsData) : AddExpenseUiState
    data class Error(val mensaje: String) : AddExpenseUiState
}