package com.example.cuentas_clarasapp.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.repositories.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class AnalyticsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val fechaFiltroActual = MutableStateFlow(LocalDate.now())

    init {
        cargarAnaliticas()
    }

    fun refrescarContenido() {
        cargarAnaliticas()
    }

    fun cambiarMes(avanzar: Boolean) {
        fechaFiltroActual.value = if (avanzar) {
            fechaFiltroActual.value.plusMonths(1)
        } else {
            fechaFiltroActual.value.minusMonths(1)
        }
        cargarAnaliticas()
    }

    private fun cargarAnaliticas() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            try {
                val repository = AnalyticsRepository()
                val resultado = repository.obtenerAnaliticas()

                if (resultado.isSuccess) {
                    val dto = resultado.getOrNull()
                    if (dto != null) {
                        val categoriasMapeadas = dto.categorias.map { cat ->
                            CategoriaGastoData(
                                nombre = cat.categoria,
                                monto = cat.monto,
                                porcentaje = cat.porcentaje,
                                color = when(cat.categoria.lowercase()) {
                                    "alimentacion" -> androidx.compose.ui.graphics.Color(0xFFE54B4B)
                                    "transporte" -> androidx.compose.ui.graphics.Color(0xFF3A86FF)
                                    else -> androidx.compose.ui.graphics.Color(0xFF985EFF)
                                }
                            )
                        }
                        _uiState.value = AnalyticsUiState.Success(
                            data = ExpenseAnalyticsData(
                                mesAnioFiltro = "General",
                                montoTotalGastado = dto.total,
                                categorias = categoriasMapeadas
                            )
                        )
                    }
                } else {
                    val ex = resultado.exceptionOrNull()
                    _uiState.value = AnalyticsUiState.Error(
                        ex?.localizedMessage ?: "Error desconocido"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error("Error: ${e.message}")
            }
        }
    }
}
