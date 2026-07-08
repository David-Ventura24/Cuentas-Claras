package com.example.cuentas_clarasapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.repositories.HomeRepository
import com.example.cuentas_clarasapp.data.repositories.BudgetApiRepository
import com.example.cuentas_clarasapp.data.repositories.SavingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository = HomeRepository(),
    private val budgetRepository: BudgetApiRepository = BudgetApiRepository(),
    private val savingsRepository: SavingsRepository = SavingsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatosFinancieros()
    }

    fun cargarDatosFinancieros() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                val resultado = homeRepository.obtenerDatosHome()

                if (resultado.isSuccess) {
                    val dto = resultado.getOrNull()!!
                    android.util.Log.d("HomeVM", "DTO RECIBIDO: $dto")

                    if (dto.error == null) {
                        val infoHome = HomeData(
                            nombreUsuario = dto.nombre_usuario ?: "Usuario",
                            saldoDisponible = dto.cantidad_disponible ?: 0.0,
                            periodoPresupuesto = dto.periodo ?: "Sin configurar",
                            porcentajeAhorro = dto.porcentaje_ahorro ?: 0,
                            limiteDiarioSugerido = dto.limite_diario ?: 0.0,
                            limiteDiarioInicial = dto.limite_diario ?: 0.0,
                            montoInicialConfigurado = dto.monto_total_configurado ?: 0.0,
                            gastoDiarioActual = dto.total_gastado_hoy ?: 0.0,
                            gastosTotalesCiclo = dto.total_gastado_ciclo ?: 0.0,
                            gastosDelDia = dto.gastos_hoy.map { gastoNet ->
                                GastoItemHome(
                                    id = gastoNet.id.toString(),
                                    monto = gastoNet.monto,
                                    categoriaId = gastoNet.categoria,
                                    nota = ""
                                )
                            }
                        )
                        android.util.Log.d("HomeVM", "ESTADO SUCCESS: $infoHome")
                        _uiState.value = HomeUiState.Success(infoHome)
                    } else {
                        android.util.Log.e("HomeVM", "ERROR EN DTO: ${dto.error}")
                        _uiState.value = HomeUiState.Error(dto.error)
                    }
                } else {
                    val errorMsg = resultado.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                    android.util.Log.e("HomeVM", "FALLO REPOSITORIO: $errorMsg")
                    _uiState.value = HomeUiState.Error(errorMsg)
                }

            } catch (excepcion: Exception) {
                _uiState.value =
                    HomeUiState.Error(excepcion.localizedMessage ?: "Error de red inesperado")
            }
        }
    }

    fun actualizarPresupuestoDesdeConfiguracion(
        montoAInyectar: Float,
        nuevoPeriodo: String,
        nuevoPorcentajeAhorro: Float,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val estadoActual = _uiState.value
                val datos = if (estadoActual is HomeUiState.Success) estadoActual.data else null

                // Si el balance es 0, empezamos de cero.
                // Si hay dinero, sumamos el BRUTO anterior (montoInicialConfigurado) para que el ahorro sea exacto.
                val nuevoMontoTotal = if (datos == null || datos.saldoDisponible <= 0) {
                    montoAInyectar.toDouble()
                } else {
                    datos.montoInicialConfigurado + montoAInyectar.toDouble()
                }

                val request = com.example.cuentas_clarasapp.data.api.budget.BudgetRequestDto(
                    cantidadTotal = nuevoMontoTotal,
                    periodo = nuevoPeriodo,
                    porcentajeAhorro = Math.round(nuevoPorcentajeAhorro).toDouble()
                )

                val budgetRepository = com.example.cuentas_clarasapp.data.repositories.BudgetApiRepository()
                val resultado = budgetRepository.guardarPresupuesto(request)

                if (resultado.isSuccess) {
                    cargarDatosFinancieros()
                    onSuccess()
                } else {
                    onError(resultado.exceptionOrNull()?.message ?: "Error")
                }
            } catch (e: Exception) {
                // 🌟 CORRECCIÓN: Solo mostramos el error, no sacamos al usuario
                onError(e.message ?: "Error de red")
            }
        }
    }
}