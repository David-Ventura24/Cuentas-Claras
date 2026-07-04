package com.example.cuentas_clarasapp.screens.budget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.budget.BudgetRequestDto
import com.example.cuentas_clarasapp.data.repositories.BudgetRepository
import com.example.cuentas_clarasapp.data.repositories.BudgetApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BudgetSetupViewModel(
    private val budgetRepository: BudgetRepository = BudgetApiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BudgetSetupUiState>(BudgetSetupUiState.Idle)
    val uiState: StateFlow<BudgetSetupUiState> = _uiState.asStateFlow()

    var montoInput by mutableStateOf("")
        private set

    var periodoSeleccionado by mutableStateOf("Mensual")
        private set

    var porcentajeAhorro by mutableFloatStateOf(10f)
        private set

    var tienePresupuestoActivo by mutableStateOf(false)
        private set

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

    fun onMontoChanged(nuevoMonto: String) {
        if (nuevoMonto.isEmpty() || nuevoMonto.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            montoInput = nuevoMonto
        }
    }

    fun onPeriodoChanged(nuevoPeriodo: String) {
        periodoSeleccionado = nuevoPeriodo
    }

    fun onPorcentajeAhorroChanged(nuevoPorcentaje: Float) {
        porcentajeAhorro = (Math.round(nuevoPorcentaje / 5f) * 5f).toFloat().coerceIn(0f, 30f)
    }

    fun cargarPresupuestoExistente(monto: Float, periodo: String, ahorro: Float) {
        montoInput = monto.toString()
        periodoSeleccionado = periodo
        porcentajeAhorro = ahorro
        tienePresupuestoActivo = true
    }

    fun guardarPresupuesto(onGastoGuardadoExitosamente: () -> Unit) {
        if (presupuestoTotal <= 0f) {
            _uiState.value = BudgetSetupUiState.Error("El monto debe ser mayor a cero.")
            return
        }

        _uiState.value = BudgetSetupUiState.Loading

        viewModelScope.launch {
            try {
                val requestDto = BudgetRequestDto(
                    cantidadTotal = presupuestoTotal.toDouble(),
                    periodo = periodoSeleccionado,
                    porcentajeAhorro = porcentajeAhorro.toInt().toDouble()
                )

                val resultado = budgetRepository.guardarPresupuesto(requestDto)

                if (resultado.isSuccess) {
                    _uiState.value = BudgetSetupUiState.Success
                    onGastoGuardadoExitosamente()
                } else {
                    _uiState.value = BudgetSetupUiState.Error(resultado.exceptionOrNull()?.localizedMessage ?: "Error")
                }
            } catch (e: Exception) {
                _uiState.value = BudgetSetupUiState.Error(e.localizedMessage ?: "Error de sesión")
            }
        }
    }
}
