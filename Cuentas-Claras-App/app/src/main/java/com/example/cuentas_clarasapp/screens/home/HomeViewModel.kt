package com.example.cuentas_clarasapp.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    // --- MODIFICACIÓN AQUÍ: Estado inicial en cero y periodo "Sin configurar" ---
    // Esto evita que la app invente datos falsos la primera vez que el usuario la abre.
    private val _uiState = MutableStateFlow<HomeUiState>(
        HomeUiState.Success(
            HomeData(
                saldoDisponible = 0.0,
                periodoPresupuesto = "Sin configurar",
                porcentajeAhorro = 0,
                limiteDiarioSugerido = 0.0
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * IMPORTANTE: Esta función conecta ambos componentes.
     * Recibe los parámetros configurados en la pantalla de presupuesto, realiza los
     * cálculos matemáticos en memoria y actualiza el estado de la HomeScreen inmediatamente.
     */
    fun actualizarPresupuestoDesdeConfiguracion(
        nuevoMonto: Float,
        nuevoPeriodo: String,
        nuevoPorcentajeAhorro: Float
    ) {
        _uiState.update { estadoActual ->
            if (estadoActual is HomeUiState.Success) {
                // 1. Calcular el monto líquido destinado a ahorro
                val montoAhorro = nuevoMonto * (nuevoPorcentajeAhorro / 100f)

                // 2. Restar el ahorro al presupuesto total para obtener lo disponible
                val nuevoSaldoDisponible = (nuevoMonto - montoAhorro).coerceAtLeast(0f)

                // 3. Determinar los días del ciclo según el período seleccionado
                val dias = if (nuevoPeriodo == "Mensual") 30 else 7

                // 4. Calcular el gasto diario sugerido final
                val diarioSugerido = if (nuevoMonto > 0f) nuevoSaldoDisponible / dias else 0f

                // Inyectar el nuevo objeto de datos refrescado en el flujo de la UI
                HomeUiState.Success(
                    data = estadoActual.data.copy(
                        saldoDisponible = nuevoSaldoDisponible.toDouble(),
                        periodoPresupuesto = nuevoPeriodo,
                        porcentajeAhorro = nuevoPorcentajeAhorro.toInt(),
                        limiteDiarioSugerido = diarioSugerido.toDouble()
                    )
                )
            } else {
                // Si la pantalla estaba cargando o en error, inicializa con los nuevos datos directamente
                val montoAhorro = nuevoMonto * (nuevoPorcentajeAhorro / 100f)
                val nuevoSaldoDisponible = (nuevoMonto - montoAhorro).coerceAtLeast(0f)
                val dias = if (nuevoPeriodo == "Mensual") 30 else 7
                val diarioSugerido = if (nuevoMonto > 0f) nuevoSaldoDisponible / dias else 0f

                HomeUiState.Success(
                    data = HomeData(
                        saldoDisponible = nuevoSaldoDisponible.toDouble(),
                        periodoPresupuesto = nuevoPeriodo,
                        porcentajeAhorro = nuevoPorcentajeAhorro.toInt(),
                        limiteDiarioSugerido = diarioSugerido.toDouble()
                    )
                )
            }
        }
    }

    /**
     * Simulación para futuras recargas manuales (Swipe-to-refresh) o llamadas a APIs/Room.
     */
    fun refrescarContenido() {
        // Por ahora se mantiene estático usando el estado actual de la memoria
    }
}