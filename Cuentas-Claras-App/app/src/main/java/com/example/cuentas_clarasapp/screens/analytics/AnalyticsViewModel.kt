package com.example.cuentas_clarasapp.screens.analytics

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.repositories.FinanzasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class AnalyticsViewModel(
    private val repository: FinanzasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    // Control dinámico de fechas usando la API de Java Time
    private val fechaFiltroActual = MutableStateFlow(LocalDate.now())

    init {
        escucharGastosDeRoom()
    }

    fun refrescarContenido() {
        escucharGastosDeRoom()
    }

    fun cambiarMes(avanzar: Boolean) {
        fechaFiltroActual.value = if (avanzar) {
            fechaFiltroActual.value.plusMonths(1)
        } else {
            fechaFiltroActual.value.minusMonths(1)
        }
    }

    private fun escucharGastosDeRoom() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            try {
                // Combinamos de forma reactiva el filtro del mes con los datos de Room
                fechaFiltroActual.collect { fechaFiltro ->
                    repository.todosLosGastos.collect { listaGastos ->

                        // 1. Formatear la etiqueta superior para la UI (Ej: "Mayo 2026")
                        val nombreMes = fechaFiltro.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        val textoMesAnio = "$nombreMes ${fechaFiltro.year}"

                        // 2. Filtrar los gastos que correspondan al mes y año del filtro actual usando fechaLong
                        val gastosFiltrados = listaGastos.filter { gasto ->
                            val fechaGasto = Instant.ofEpochMilli(gasto.fechaLong)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            fechaGasto.month == fechaFiltro.month && fechaGasto.year == fechaFiltro.year
                        }

                        // 3. Agrupar y calcular montos y porcentajes por categoría
                        val montoTotal = gastosFiltrados.sumOf { it.monto }

                        val listaCategoriasDinamicas = if (montoTotal > 0) {
                            gastosFiltrados.groupBy { it.categoriaId }.map { (categoriaId, listaDeGastos) ->
                                val totalCategoria = listaDeGastos.sumOf { it.monto }
                                val porcentaje = ((totalCategoria / montoTotal) * 100).toFloat()

                                CategoriaGastoData(
                                    nombre = obtenerNombreCategoria(categoriaId),
                                    porcentaje = porcentaje,
                                    monto = totalCategoria,
                                    color = obtenerColorCategoria(categoriaId)
                                )
                            }.sortedByDescending { it.monto }
                        } else {
                            emptyList()
                        }

                        // 4. Emitir el estado de éxito con los datos reales agrupados
                        _uiState.value = AnalyticsUiState.Success(
                            data = ExpenseAnalyticsData(
                                mesAnioFiltro = textoMesAnio,
                                montoTotalGastado = montoTotal,
                                categorias = listaCategoriasDinamicas
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error("No se pudieron cargar los datos de análisis: ${e.message}")
            }
        }
    }

    private fun obtenerNombreCategoria(id: String): String {
        return when (id.lowercase()) {
            "alimentacion" -> "Alimentación"
            "transporte"   -> "Transporte"
            "ocio"         -> "Ocio"
            "compras"      -> "Compras"
            "educacion"    -> "Educación"
            else           -> id.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    private fun obtenerColorCategoria(id: String): Color {
        return when (id.lowercase()) {
            "alimentacion" -> Color(0xFFF87171) // Rojo suave
            "educacion"    -> Color(0xFF4ADE80) // Verde
            "transporte"   -> Color(0xFF3A86FF) // Azul
            "ocio"         -> Color(0xFF985EFF) // Morado principal
            "compras"      -> Color(0xFFFACC15) // Amarillo
            else           -> Color(0xFF70777A) // Gris
        }
    }
}