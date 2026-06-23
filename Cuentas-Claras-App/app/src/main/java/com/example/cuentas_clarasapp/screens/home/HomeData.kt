package com.example.cuentas_clarasapp.screens.home

// Representación de cada fila individual de gasto hoy
data class GastoReciente(
    val id: String,
    val categoriaId: String,
    val nota: String,
    val monto: Double,
    val fecha: String // Formato automático ISO (ej: "2026-06-22")
)

data class HomeData(
    val saldoDisponible: Double,
    val periodoPresupuesto: String,
    val porcentajeAhorro: Int,
    val limiteDiarioSugerido: Double,
    val montoInicialConfigurado: Double = 0.0, // Added to fix unresolved reference
    val limiteDiarioInicial: Double = 0.0,     // Added for progress calculation
    val gastoDiarioActual: Double = 0.0,    // 🌟 NUEVO: Lo gastado solo hoy
    val gastosTotalesCiclo: Double = 0.0,   // 🌟 NUEVO: El acumulado histórico del ciclo
    val gastosDelDia: List<GastoReciente> = emptyList() // 🌟 NUEVO: Para el espacio negro inferior
)

sealed interface HomeUiState {
    object Loading : HomeUiState
    object Error : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
}