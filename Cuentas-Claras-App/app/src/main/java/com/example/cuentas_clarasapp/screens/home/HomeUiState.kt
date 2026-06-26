package com.example.cuentas_clarasapp.screens.home

// Representación de cada fila individual de gasto hoy
data class GastoItemHome(
    val id: String,
    val monto: Double,
    val categoriaId: String,
    val nota: String
)

data class HomeData(
    val saldoDisponible: Double,
    val periodoPresupuesto: String,
    val porcentajeAhorro: Int,
    val limiteDiarioSugerido: Double,
    val montoInicialConfigurado: Double = 0.0,
    val limiteDiarioInicial: Double = 0.0,
    val gastoDiarioActual: Double = 0.0,
    val gastosTotalesCiclo: Double = 0.0,
    val gastosDelDia: List<GastoItemHome> = emptyList()
)

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data class Error(val mensaje: String) : HomeUiState
}