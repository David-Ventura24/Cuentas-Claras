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
                android.util.Log.wtf("HomeVM_DEBUG", "1. UI envía -> Monto: $montoAInyectar, Porcentaje: $nuevoPorcentajeAhorro%")

                val porcentaje = nuevoPorcentajeAhorro / 100f
                val montoAhorroMeta = (montoAInyectar * porcentaje).toDouble()

                android.util.Log.wtf("HomeVM_DEBUG", "2. Monto de ahorro a registrar: $$montoAhorroMeta")

                val estadoActual = _uiState.value
                val saldoActual = if (estadoActual is HomeUiState.Success) estadoActual.data.saldoDisponible else 0.0

                val nuevoMontoTotal = if (saldoActual <= 0) {
                    montoAInyectar.toDouble()
                } else {
                    val brutoAnterior = (estadoActual as HomeUiState.Success).data.montoInicialConfigurado
                    brutoAnterior + montoAInyectar.toDouble()
                }

                val request = com.example.cuentas_clarasapp.data.api.budget.BudgetRequestDto(
                    cantidadTotal = nuevoMontoTotal,
                    periodo = nuevoPeriodo,
                    porcentajeAhorro = Math.round(nuevoPorcentajeAhorro).toDouble()
                )

                android.util.Log.wtf("HomeVM_DEBUG", "3. Enviando presupuesto a la API...")
                val resultadoPresupuesto = budgetRepository.guardarPresupuesto(request)

                if (resultadoPresupuesto.isSuccess) {
                    android.util.Log.wtf("HomeVM_DEBUG", "4. Presupuesto guardado. Registrando ahorro automático...")

                    // ✅ AHORA SÍ: registrar el ahorro automático si el porcentaje es mayor a 0
                    if (montoAhorroMeta > 0.0) {
                        val resultadoAhorro = savingsRepository.guardarAhorro(
                            monto = montoAhorroMeta,
                            tipo = "automatico",
                            nota = "Ahorro automático del ${nuevoPorcentajeAhorro.toInt()}% al inyectar fondos"
                        )

                        if (resultadoAhorro.isSuccess) {
                            android.util.Log.wtf("HomeVM_DEBUG", "5. Ahorro de $$montoAhorroMeta registrado correctamente.")
                        } else {
                            // El ahorro falló, pero no bloqueamos el flujo — el presupuesto ya se guardó
                            android.util.Log.e("HomeVM_DEBUG", "5. Ahorro falló: ${resultadoAhorro.exceptionOrNull()?.message}")
                        }
                    } else {
                        android.util.Log.wtf("HomeVM_DEBUG", "5. Porcentaje es 0%, no se registra ahorro.")
                    }

                    // Refrescar el estado del Home desde la API
                    val refresh = homeRepository.obtenerDatosHome()
                    if (refresh.isSuccess) {
                        val dto = refresh.getOrNull()!!
                        _uiState.value = HomeUiState.Success(
                            HomeData(
                                nombreUsuario = dto.nombre_usuario ?: "Usuario",
                                saldoDisponible = dto.cantidad_disponible ?: 0.0,
                                periodoPresupuesto = dto.periodo ?: "Sin configurar",
                                porcentajeAhorro = dto.porcentaje_ahorro ?: 0,
                                limiteDiarioSugerido = dto.limite_diario ?: 0.0,
                                limiteDiarioInicial = dto.limite_diario ?: 0.0,
                                montoInicialConfigurado = dto.monto_total_configurado ?: 0.0,
                                gastoDiarioActual = dto.total_gastado_hoy ?: 0.0,
                                gastosTotalesCiclo = dto.total_gastado_ciclo ?: 0.0,
                                gastosDelDia = dto.gastos_hoy.map { g ->
                                    GastoItemHome(g.id.toString(), g.monto, g.categoria, "")
                                }
                            )
                        )
                        onSuccess()
                    }
                } else {
                    android.util.Log.wtf("HomeVM_DEBUG", "Fallo en endpoint de presupuesto: ${resultadoPresupuesto.exceptionOrNull()?.message}")
                    onError(resultadoPresupuesto.exceptionOrNull()?.message ?: "Error al guardar presupuesto")
                }
            } catch (e: Exception) {
                android.util.Log.wtf("HomeVM_DEBUG", "Excepción general: ${e.message}")
                onError(e.message ?: "Error de red inesperado")
            }
        }
    }
}