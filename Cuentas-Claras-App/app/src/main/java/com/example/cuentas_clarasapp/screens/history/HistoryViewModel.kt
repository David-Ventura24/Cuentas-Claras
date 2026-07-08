package com.example.cuentas_clarasapp.screens.history

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.repositories.HistoryRepository
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: HistoryRepository = HistoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var fechaActual = YearMonth.now()

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

                val resultado = historyRepository.obtenerHistorial(
                    mes = fechaActual.monthValue,
                    anio = fechaActual.year
                )

                if (resultado.isSuccess) {
                    val dto = resultado.getOrNull()!!
                    val transacciones = dto.transacciones.map { t ->
                        GastoHistorial(
                            id = t.id,
                            descripcion = t.descripcion,
                            monto = t.monto,
                            categoriaId = t.categoria,
                            categoria = t.categoria,
                            fecha = t.fecha,
                            colorCategoria = try {
                                Color(android.graphics.Color.parseColor(t.colorHex))
                            } catch (e: Exception) {
                                Color.Gray
                            }
                        )
                    }

                    // MODIFICADO: Ahora pasamos activamente totalAhorradoMes al estado de la UI
                    _uiState.value = HistoryUiState.Success(
                        data = HistoryData(
                            mesAnioFiltro = textoFiltro,
                            totalGastadoMes = dto.totalGastadoMes,
                            totalAhorradoMes = dto.totalAhorradoMes ?: dto.ahorro_neto ?: 0.0,
                            transacciones = transacciones
                        )
                    )
                } else {
                    _uiState.value = HistoryUiState.Error(resultado.exceptionOrNull()?.localizedMessage ?: "Error de conexión")
                }

            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error("Error al conectar con el servidor: ${e.message}")
            }
        }
    }
}