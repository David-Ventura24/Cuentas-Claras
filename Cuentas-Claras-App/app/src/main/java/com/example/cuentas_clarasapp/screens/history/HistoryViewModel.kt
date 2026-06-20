package com.example.cuentas_clarasapp.screens.history

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var fechaActual = YearMonth.of(2026, 5)

    init {
        cargarHistorial()
    }

    fun cambiarMes(avanzar: Boolean) {
        fechaActual = if (avanzar) fechaActual.plusMonths(1) else fechaActual.minusMonths(1)
        cargarHistorial()
    }

    fun refrescarContenido() {
        cargarHistorial()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                val mesNombre = fechaActual.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                    .replaceFirstChar { it.uppercase() }
                val textoFiltro = "$mesNombre ${fechaActual.year}"

                // Simulación de datos dinámicos basados en tu mockup
                val transaccionesSimuladas = if (fechaActual.monthValue == 5) {
                    listOf(
                        GastoHistorial("1", "Supermercado", 120.50, "Alimentación", "15 de Mayo", Color(0xFFE54B4B)),
                        GastoHistorial("2", "Matrícula Ciclo", 85.00, "Educación", "12 de Mayo", Color(0xFF2EC4B6)),
                        GastoHistorial("3", "Gasolina", 45.00, "Transporte", "10 de Mayo", Color(0xFF3A86FF)),
                        GastoHistorial("4", "Reserva de ahorro", 32.24, "Ahorro", "05 de Mayo", Color(0xFF8338EC)),
                        GastoHistorial("5", "Almuerzo Pizza", 16.00, "Restaurantes", "02 de Mayo", Color(0xFFF7A072)),
                        GastoHistorial("6", "Fotocopias", 14.26, "Otros", "01 de Mayo", Color(0xFF70777A))
                    )
                } else {
                    listOf(
                        GastoHistorial("7", "Compras generales", 75.00, "Alimentación", "04 de cada mes", Color(0xFFE54B4B)),
                        GastoHistorial("8", "Pasajes", 20.00, "Transporte", "02 de cada mes", Color(0xFF3A86FF))
                    )
                }

                val total = transaccionesSimuladas.sumOf { it.monto }

                _uiState.value = HistoryUiState.Success(
                    data = HistoryData(
                        mesAnioFiltro = textoFiltro,
                        totalGastadoMes = total,
                        transacciones = transaccionesSimuladas
                    )
                )
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error("Error al conectar con el servidor de Visual Studio")
            }
        }
    }
}