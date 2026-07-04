package com.example.cuentas_clarasapp.screens.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.repositories.SavingsRepository
import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.cuentas_clarasapp.data.api.savings.AhorroNetDto

class SavingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SavingsUiState>(SavingsUiState.Loading)
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    init {
        cargarAhorros()
    }

    fun refrescarAhorros() {
        cargarAhorros()
    }

    private fun cargarAhorros() {
        viewModelScope.launch {
            _uiState.value = SavingsUiState.Loading
            try {
                val repository = SavingsRepository()
                val resultado = repository.obtenerStatusAhorro()

                if (resultado.isSuccess) {
                    val dto = resultado.getOrNull()!!
                    val listaMapeada = dto.movimientos.map { m ->
                        AhorroEntity(
                            monto = m.monto,
                            tipo = m.tipo,
                            nota = m.nota,
                            fechaLong = try {
                                java.time.OffsetDateTime.parse(m.fecha).toInstant().toEpochMilli()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                        )
                    }
                    _uiState.value = SavingsUiState.Success(
                        ahorroGlobalNeto = dto.ahorro_neto,
                        listaMovimientos = listaMapeada
                    )
                } else {
                    val err = resultado.exceptionOrNull()?.message ?: "Error al obtener ahorros"
                    _uiState.value = SavingsUiState.Error(err)
                }
            } catch (e: Exception) {
                _uiState.value = SavingsUiState.Error("Excepción: ${e.message}")
            }
        }
    }

    fun realizarRetiroEmergencia(monto: Double, motivo: String) {
        // TODO
    }
}
