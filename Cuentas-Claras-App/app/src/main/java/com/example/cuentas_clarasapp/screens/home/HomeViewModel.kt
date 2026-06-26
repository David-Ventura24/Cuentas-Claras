package com.example.cuentas_clarasapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.local.entities.GastoEntity
import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity
import com.example.cuentas_clarasapp.data.local.entities.PresupuestoEntity
import com.example.cuentas_clarasapp.data.repositories.FinanzasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HomeViewModel(
    private val repository: FinanzasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        conectarConRoom()
    }

    private fun conectarConRoom() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                combine(
                    repository.todosLosGastos,
                    repository.presupuestoActivo
                ) { listaEntidades: List<GastoEntity>, presupuestoConfig: PresupuestoEntity? ->

                    val hoy = LocalDate.now()

                    // Filtrar gastos de hoy para la sección "GASTOS DE HOY"
                    val entidadesDeHoy = listaEntidades.filter { entidad ->
                        val fechaGasto = Instant.ofEpochMilli(entidad.fechaLong)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        fechaGasto.isEqual(hoy)
                    }

                    // Mapeo al tipo que consume tu HomeScreen
                    val listaMapeadaHoy = entidadesDeHoy.map { entidad ->
                        GastoItemHome(
                            id = entidad.id.toString(),
                            monto = entidad.monto,
                            categoriaId = entidad.categoriaId,
                            nota = entidad.descripcion
                        )
                    }

                    // 🌟 Extraer configuraciones guardadas en Room (ahora viene el monto bruto original)
                    val montoOriginalBruto = presupuestoConfig?.montoMontoConfigurado ?: 0.0
                    val periodo = presupuestoConfig?.periodo ?: "Sin configurar"
                    val porcentajeAhorro = presupuestoConfig?.porcentajeAhorro ?: 0
                    val limiteDiarioInicial = presupuestoConfig?.limiteDiarioInicial ?: 0.0

                    // 🌟 1. Calcular el dinero destinado a ahorro de forma estática
                    val dineroAhorroMeta = montoOriginalBruto * (porcentajeAhorro / 100.0)

                    // 🌟 2. Dinero disponible para gastar al inicio del ciclo (antes de efectuar compras)
                    val dineroDisponibleInicial = montoOriginalBruto - dineroAhorroMeta

                    // 3. Gastos acumulados en la base de datos
                    val gastosTotales = listaEntidades.sumOf { it.monto }
                    val gastoDiarioActual = entidadesDeHoy.sumOf { it.monto }

                    // 🌟 4. Saldo disponible actual para la HomeScreen (Dinero Inicial Disponible - Gastos Totales)
                    val saldoDisponibleActualHome = (dineroDisponibleInicial - gastosTotales).coerceAtLeast(0.0)
                    val limiteSugeridoHoy = (limiteDiarioInicial - gastoDiarioActual).coerceAtLeast(0.0)

                    HomeData(
                        saldoDisponible = saldoDisponibleActualHome,
                        periodoPresupuesto = periodo,
                        porcentajeAhorro = porcentajeAhorro,
                        limiteDiarioSugerido = limiteSugeridoHoy,
                        montoInicialConfigurado = montoOriginalBruto,
                        limiteDiarioInicial = limiteDiarioInicial,
                        gastoDiarioActual = gastoDiarioActual,
                        gastosTotalesCiclo = gastosTotales,
                        gastosDelDia = listaMapeadaHoy
                    )
                }.collect { dataCalculada ->
                    _uiState.value = HomeUiState.Success(dataCalculada)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun actualizarPresupuestoDesdeConfiguracion(
        montoAInyectar: Float,
        nuevoPeriodo: String,
        nuevoPorcentajeAhorro: Float
    ) {
        viewModelScope.launch {
            // 1. Obtener los valores actuales que ya están cargados en el Success del uiState
            val estadoActual = _uiState.value
            val (montoBaseActual, limiteDiarioActualBase) = if (estadoActual is HomeUiState.Success) {
                Pair(estadoActual.data.montoInicialConfigurado, estadoActual.data.limiteDiarioInicial)
            } else {
                Pair(0.0, 0.0)
            }

            // 2. Sumar el nuevo monto al presupuesto base existente
            val nuevoMontoTotalBase = montoBaseActual + montoAInyectar.toDouble()

            // 3. El ahorro automático se calcula EXCLUSIVAMENTE sobre la nueva inyección de dinero
            val ahorroDeLaInyeccion = montoAInyectar * (nuevoPorcentajeAhorro / 100f)

            // 4. Calcular el nuevo límite diario sugerido de manera acumulativa
            // El dinero que realmente entra al disponible para gastar de esta inyección es: montoAInyectar - ahorroDeLaInyeccion
            val dineroDisponibleDeInyeccion = montoAInyectar - ahorroDeLaInyeccion
            val dias = if (nuevoPeriodo == "Mensual") 30 else 7

            // El nuevo límite diario inicial será el que ya tenía más la proporción diaria de la nueva inyección
            val nuevoLimiteDiarioInicial = limiteDiarioActualBase + (dineroDisponibleDeInyeccion / dias)

            // 5. Guardar la configuración acumulada en la base de datos
            repository.guardarConfiguracionPresupuesto(
                monto = nuevoMontoTotalBase,
                periodo = nuevoPeriodo, // Mantiene o actualiza el período (Mensual/Semanal)
                porcentajeAhorro = nuevoPorcentajeAhorro.toInt(), // Mantiene o actualiza el porcentaje para futuras inyecciones
                limiteDiario = nuevoLimiteDiarioInicial
            )

            // 6. Registrar el movimiento automático en la alcancía si la inyección generó ahorro
            if (ahorroDeLaInyeccion > 0f) {
                val abonoAutomatico = com.example.cuentas_clarasapp.data.local.entities.AhorroEntity(
                    monto = ahorroDeLaInyeccion.toDouble(),
                    tipo = "INGRESO",
                    nota = "Ahorro automático - Inyección $nuevoPeriodo",
                    fechaLong = java.time.Instant.now().toEpochMilli()
                )
                repository.registrarMovimientoAhorro(abonoAutomatico)
            }
        }
    }

    fun registrarNuevoGasto(monto: Double, categoriaId: String, nota: String) {
        viewModelScope.launch {
            val categoriaNombre = when (categoriaId) {
                "alimentacion" -> "Alimentación"
                "transporte"   -> "Transporte"
                "ocio"         -> "Ocio"
                "compras"      -> "Compras"
                "educacion"    -> "Educación"
                else           -> "Otros"
            }

            repository.registrarGasto(
                descripcion = nota,
                monto = monto,
                categoriaId = categoriaId,
                categoriaNombre = categoriaNombre
            )
        }
    }
}