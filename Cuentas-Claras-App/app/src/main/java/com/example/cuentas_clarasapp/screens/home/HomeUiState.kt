package com.example.cuentas_clarasapp.screens.home

// Representación de un gasto rápido para la lista del Home
data class GastoHome(
    val id: String,
    val descripcion: String,
    val monto: Double,
    val categoria: String,
    val fecha: String,
    val fotoUrl: String? = null
)

// Datos que requiere el Home según el diseño del proyecto
data class HomeData(
    val nombreUsuario: String = "",
    val saldoDisponible: Double = 0.0,
    val limiteDiarioSugerido: Double = 0.0,
    val porcentajeAhorro: Int = 0,
    val periodoPresupuesto: String = "Mensual", // Semanal o Mensual
    val ultimosGastos: List<GastoHome> = emptyList()
)

// Estados de la UI para controlar pantallas de carga o errores de red
sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val data: HomeData) : HomeUiState
    data class Error(val mensaje: String) : HomeUiState
}