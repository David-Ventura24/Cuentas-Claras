package com.example.cuentas_clarasapp.screens.budget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetSetupViewModel : ViewModel() {

    // --- ESTADO DE FLUJO PARA LA UI (Carga, Éxito, Error) ---
    private val _uiState = MutableStateFlow<BudgetSetupUiState>(BudgetSetupUiState.Idle)
    val uiState: StateFlow<BudgetSetupUiState> = _uiState.asStateFlow()

    // --- VARIABLES DE ENTRADA CON OBSERVACIÓN COMPOSABLE ---
    // Usamos 'by mutableStateOf' para que Compose rastree los cambios al instante
    var montoInput by mutableStateOf("")
        private set

    var periodoSeleccionado by mutableStateOf("Mensual")
        private set

    var porcentajeAhorro by mutableFloatStateOf(10f)
        private set

    // --- PROPIEDADES COMPUTADAS EN TIEMPO REAL ---
    val presupuestoTotal: Float
        get() = montoInput.toFloatOrNull() ?: 0f

    val montoAhorro: Float
        get() = presupuestoTotal * (porcentajeAhorro / 100f)

    val saldoDisponibleParaGasto: Float
        get() = (presupuestoTotal - montoAhorro).coerceAtLeast(0f)

    val diasPeriodo: Int
        get() = if (periodoSeleccionado == "Mensual") 30 else 7

    val limiteDiarioSugerido: Float
        get() = if (presupuestoTotal > 0f) saldoDisponibleParaGasto / diasPeriodo else 0f


    // --- EVENTOS EMITIDOS DESDE LA UI ---

    fun onMontoChanged(nuevoMonto: String) {
        // Expresión regular: permite solo números y hasta dos decimales
        if (nuevoMonto.isEmpty() || nuevoMonto.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            montoInput = nuevoMonto
        }
    }

    fun onPeriodoChanged(nuevoPeriodo: String) {
        if (nuevoPeriodo == "Mensual" || nuevoPeriodo == "Semanal") {
            periodoSeleccionado = nuevoPeriodo
        }
    }

    fun onPorcentajeAhorroChanged(nuevoPorcentaje: Float) {
        porcentajeAhorro = nuevoPorcentaje.coerceIn(0f, 30f)
    }

    /**
     * Sincroniza y guarda el presupuesto en la capa de datos
     */
    fun guardarPresupuesto(onGastoGuardadoExitosamente: () -> Unit) {
        if (presupuestoTotal <= 0f) {
            _uiState.value = BudgetSetupUiState.Error("El monto debe ser mayor a cero.")
            return
        }

        viewModelScope.launch {
            _uiState.value = BudgetSetupUiState.Loading
            try {
                // ===========================================================================================
                // TODO: BACKEND INTEGRATION - PERSISTENCIA DE PRESUPUESTO
                // ===========================================================================================
                // Aquí conectarás tu DAO de Room y el servicio HTTP de Supabase/NodeJS:
                //
                // val entity = BudgetEntity(montoTotal = presupuestoTotal, periodo = periodoSeleccionado, ahorro = porcentajeAhorro)
                // budgetRepository.guardarPresupuesto(entity)
                // ===========================================================================================

                kotlinx.coroutines.delay(600) // Simulación de carga fluida
                _uiState.value = BudgetSetupUiState.Success
                onGastoGuardadoExitosamente()

            } catch (e: Exception) {
                _uiState.value = BudgetSetupUiState.Error(e.localizedMessage ?: "Error al guardar")
            }
        }
    }
}