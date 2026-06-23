package com.example.cuentas_clarasapp.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(
        HomeUiState.Success(
            HomeData(
                saldoDisponible = 0.0,
                periodoPresupuesto = "Sin configurar",
                porcentajeAhorro = 0,
                limiteDiarioSugerido = 0.0,
                montoInicialConfigurado = 0.0,
                limiteDiarioInicial = 0.0
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun actualizarPresupuestoDesdeConfiguracion(
        nuevoMonto: Float,
        nuevoPeriodo: String,
        nuevoPorcentajeAhorro: Float
    ) {
        _uiState.update { estadoActual ->
            val montoAhorro = nuevoMonto * (nuevoPorcentajeAhorro / 100f)
            val nuevoSaldoDisponible = (nuevoMonto - montoAhorro).coerceAtLeast(0f)
            val dias = if (nuevoPeriodo == "Mensual") 30 else 7
            val diarioSugerido = if (nuevoMonto > 0f) nuevoSaldoDisponible / dias else 0f

            HomeUiState.Success(
                data = HomeData(
                    saldoDisponible = nuevoSaldoDisponible.toDouble(),
                    periodoPresupuesto = nuevoPeriodo,
                    porcentajeAhorro = nuevoPorcentajeAhorro.toInt(),
                    limiteDiarioSugerido = diarioSugerido.toDouble(),
                    montoInicialConfigurado = nuevoSaldoDisponible.toDouble(),
                    limiteDiarioInicial = diarioSugerido.toDouble(),
                    gastoDiarioActual = 0.0,
                    gastosTotalesCiclo = 0.0,
                    gastosDelDia = emptyList()
                )
            )
        }
    }

    fun registrarNuevoGasto(monto: Double, categoriaId: String, nota: String) {
        _uiState.update { estadoActual ->
            if (estadoActual is HomeUiState.Success) {
                val datosPrevios = estadoActual.data
                val fechaHoy = java.time.LocalDate.now().toString()

                val nuevoObjetoGasto = GastoReciente(
                    id = UUID.randomUUID().toString(),
                    categoriaId = categoriaId,
                    nota = nota,
                    monto = monto,
                    fecha = fechaHoy
                )

                val nuevoSaldo = (datosPrevios.saldoDisponible - monto).coerceAtLeast(0.0)
                val nuevoGastoDiario = datosPrevios.gastoDiarioActual + monto
                val nuevosGastosTotales = datosPrevios.gastosTotalesCiclo + monto

                // Reducimos el límite diario sugerido según lo que va quedando para hoy
                val nuevoLimiteDiarioSugerido = (datosPrevios.limiteDiarioSugerido - monto).coerceAtLeast(0.0)
                val nuevaListaDelDia = listOf(nuevoObjetoGasto) + datosPrevios.gastosDelDia

                HomeUiState.Success(
                    data = datosPrevios.copy(
                        saldoDisponible = nuevoSaldo,
                        gastoDiarioActual = nuevoGastoDiario,
                        gastosTotalesCiclo = nuevosGastosTotales,
                        limiteDiarioSugerido = nuevoLimiteDiarioSugerido,
                        gastosDelDia = nuevaListaDelDia
                    )
                )
            } else {
                estadoActual
            }
        }
    }
}