package com.example.cuentas_clarasapp.screens.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity
import com.example.cuentas_clarasapp.data.repositories.FinanzasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant

class SavingsViewModel(
    private val repository: FinanzasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SavingsUiState>(SavingsUiState.Loading)
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        escucharDatosAhorro()
    }

    private fun escucharDatosAhorro() {
        viewModelScope.launch {
            _uiState.value = SavingsUiState.Loading
            try {
                // Combinamos de forma reactiva el balance neto total con la lista de movimientos históricos
                combine(
                    repository.ahorroGlobalNeto,
                    repository.todosLosMovimientosAhorro
                ) { neto, movimientos ->
                    SavingsUiState.Success(
                        ahorroGlobalNeto = neto ?: 0.0,
                        listaMovimientos = movimientos
                    )
                }.collect { estadoCombinado ->
                    _uiState.value = estadoCombinado
                }
            } catch (e: Exception) {
                _uiState.value = SavingsUiState.Error("Error al cargar ahorros: ${e.message}")
            }
        }
    }

    fun realizarRetiroEmergencia(monto: Double, motivo: String) {
        if (monto <= 0) return
        viewModelScope.launch {
            val retiro = AhorroEntity(
                monto = monto,
                tipo = "RETIRO",
                nota = motivo.ifBlank { "Retiro de emergencia" },
                fechaLong = Instant.now().toEpochMilli()
            )
            repository.registrarMovimientoAhorro(retiro)
        }
    }
}