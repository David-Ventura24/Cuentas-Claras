package com.example.cuentas_clarasapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.repositories.HomeRepository
import com.example.cuentas_clarasapp.data.api.ApiClient
import com.example.cuentas_clarasapp.data.api.home.HomeResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository = HomeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatosFinancieros()
    }

    fun cargarDatosFinancieros() {
        android.util.Log.d("HomeVM", "Refrescando datos financieros...")
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                val resultado = homeRepository.obtenerDatosHome()
                
                if (resultado.isSuccess) {
                    val dto = resultado.getOrNull()!!
                    if (dto.error == null) {
                        val infoHome = HomeData(
                            nombreUsuario = dto.nombre_usuario ?: "Usuario",
                            saldoDisponible = dto.cantidad_disponible ?: 0.0,
                            periodoPresupuesto = dto.periodo ?: "Sin configurar",
                            porcentajeAhorro = dto.porcentaje_ahorro ?: 0,
                            limiteDiarioSugerido = dto.limite_diario ?: 0.0,
                            limiteDiarioInicial = dto.limite_diario ?: 0.0,
                            //  Para que la barra sepa el total real
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
                        _uiState.value = HomeUiState.Success(infoHome)
                    } else {
                        _uiState.value = HomeUiState.Error(dto.error)
                    }
                } else {
                    _uiState.value = HomeUiState.Error(resultado.exceptionOrNull()?.localizedMessage ?: "Error desconocido")
                }

            } catch (excepcion: Exception) {
                _uiState.value = HomeUiState.Error(excepcion.localizedMessage ?: "Error de red inesperado")
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
                val montoBase = if (estadoActual is HomeUiState.Success) estadoActual.data.montoInicialConfigurado else 0.0
                val nuevoMontoTotal = montoBase + montoAInyectar.toDouble()

                val request = com.example.cuentas_clarasapp.data.api.budget.BudgetRequestDto(
                    cantidadTotal = nuevoMontoTotal,
                    periodo = nuevoPeriodo,
                    porcentajeAhorro = Math.round(nuevoPorcentajeAhorro).toDouble()
                )

                val budgetRepository = com.example.cuentas_clarasapp.data.repositories.BudgetApiRepository()
                val resultado = budgetRepository.guardarPresupuesto(request)

                if (resultado.isSuccess) {
                    cargarDatosFinancieros()
                    onSuccess() //  Esto cerrará la pantalla
                } else {
                    onError(resultado.exceptionOrNull()?.message ?: "Error al guardar")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error de conexión")
            }
        }
    }
}
