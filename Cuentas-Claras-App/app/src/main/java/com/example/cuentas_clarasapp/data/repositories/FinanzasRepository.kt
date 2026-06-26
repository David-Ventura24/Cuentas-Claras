package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.local.dao.GastoDao
import com.example.cuentas_clarasapp.data.local.dao.AhorroDao
import com.example.cuentas_clarasapp.data.local.entities.GastoEntity
import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity
import com.example.cuentas_clarasapp.data.local.entities.PresupuestoEntity
import kotlinx.coroutines.flow.Flow

class FinanzasRepository(
    private val gastoDao: GastoDao,
    private val ahorroDao: AhorroDao // 🌟 Agregado el nuevo DAO
) {
    // Lógica existente de Gastos
    val todosLosGastos: Flow<List<GastoEntity>> = gastoDao.obtenerTodosLosGastosFlow()

    suspend fun insertarGasto(gasto: GastoEntity) {
        gastoDao.insertarGasto(gasto)
    }

    suspend fun registrarGasto(
        descripcion: String,
        monto: Double,
        categoriaId: String,
        categoriaNombre: String
    ) {
        val nuevoGasto = GastoEntity(
            descripcion = descripcion,
            monto = monto,
            categoriaId = categoriaId,
            categoriaNombre = categoriaNombre,
            fechaLong = System.currentTimeMillis()
        )
        insertarGasto(nuevoGasto)
    }

    // 🌟 NUEVA LÓGICA DE AHORROS
    val todosLosMovimientosAhorro: Flow<List<AhorroEntity>> = ahorroDao.obtenerTodosLosMovimientos()
    val ahorroGlobalNeto: Flow<Double?> = ahorroDao.obtenerAhorroGlobalNeto()

    suspend fun registrarMovimientoAhorro(movimiento: AhorroEntity) {
        ahorroDao.insertarMovimiento(movimiento)
    }

    // 🌟 LÓGICA DE PRESUPUESTO
    val presupuestoActivo: Flow<PresupuestoEntity?> = gastoDao.obtenerPresupuestoFlow()

    suspend fun guardarConfiguracionPresupuesto(
        monto: Double,
        periodo: String,
        porcentajeAhorro: Int,
        limiteDiario: Double
    ) {
        val entity = PresupuestoEntity(
            montoMontoConfigurado = monto,
            periodo = periodo,
            porcentajeAhorro = porcentajeAhorro,
            limiteDiarioInicial = limiteDiario
        )
        gastoDao.guardarPresupuesto(entity)
    }
}
