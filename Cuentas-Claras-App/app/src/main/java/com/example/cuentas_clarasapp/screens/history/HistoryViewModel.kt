package com.example.cuentas_clarasapp.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.local.entities.GastoEntity
import com.example.cuentas_clarasapp.data.repositories.FinanzasRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryViewModel(
    private val repository: FinanzasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var fechaFiltroActual = LocalDate.now()
    private val formatterMeseAnio = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
    
    private var observationJob: Job? = null

    init {
        escucharDatos()
    }

    fun refrescarContenido() {
        escucharDatos()
    }

    fun cambiarMes(avanzar: Boolean) {
        fechaFiltroActual = if (avanzar) {
            fechaFiltroActual.plusMonths(1)
        } else {
            fechaFiltroActual.minusMonths(1)
        }
        escucharDatos()
    }

    private fun escucharDatos() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                combine(
                    repository.todosLosGastos,
                    repository.todosLosMovimientosAhorro
                ) { listaGastos, listaAhorros ->
                    
                    val gastosFiltrados = listaGastos.filter { gasto ->
                        val fechaGasto = Instant.ofEpochMilli(gasto.fechaLong)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        fechaGasto.month == fechaFiltroActual.month && fechaGasto.year == fechaFiltroActual.year
                    }

                    val ahorrosFiltrados = listaAhorros.filter { ahorro ->
                        val fechaAhorro = Instant.ofEpochMilli(ahorro.fechaLong)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        fechaAhorro.month == fechaFiltroActual.month && fechaAhorro.year == fechaFiltroActual.year
                    }

                    val transacciones = gastosFiltrados.map { gasto ->
                        GastoHistorial(
                            id = gasto.id.toString(),
                            descripcion = gasto.descripcion,
                            monto = gasto.monto,
                            categoriaId = gasto.categoriaId,
                            categoria = gasto.categoriaNombre,
                            fecha = Instant.ofEpochMilli(gasto.fechaLong)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toString(),
                            colorCategoria = androidx.compose.ui.graphics.Color.Gray
                        )
                    }

                    val totalCalculado = gastosFiltrados.sumOf { it.monto }
                    
                    val totalAhorrado = ahorrosFiltrados.sumOf { 
                        if (it.tipo == "INGRESO") it.monto else -it.monto 
                    }
                    
                    val mesAnioTexto = fechaFiltroActual.format(formatterMeseAnio)

                    HistoryData(
                        mesAnioFiltro = mesAnioTexto.replaceFirstChar { it.uppercase() },
                        totalGastadoMes = totalCalculado,
                        totalAhorradoMes = totalAhorrado,
                        transacciones = transacciones
                    )
                }.collect { data ->
                    _uiState.value = HistoryUiState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}