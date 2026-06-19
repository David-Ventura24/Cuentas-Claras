package com.example.cuentas_clarasapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun refrescarContenido() {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Simulamos una carga de datos inicial
                _uiState.value = HomeUiState.Success(
                    data = HomeData(
                        nombreUsuario = "David",
                        saldoDisponible = 1250.50,
                        limiteDiarioSugerido = 45.0,
                        porcentajeAhorro = 15,
                        periodoPresupuesto = "Mensual",
                        ultimosGastos = listOf(
                            GastoHome("1", "Supermercado", 45.20, "Comida", "Hoy"),
                            GastoHome("2", "Gasolina", 60.0, "Transporte", "Ayer")
                        )
                    )
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("No se pudo conectar con el servidor")
            }
        }
    }
}
