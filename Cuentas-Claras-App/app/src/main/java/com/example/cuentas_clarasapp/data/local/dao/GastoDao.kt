package com.example.cuentas_clarasapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cuentas_clarasapp.data.local.entities.GastoEntity
import com.example.cuentas_clarasapp.data.local.entities.PresupuestoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    // --- GASTOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGasto(gasto: GastoEntity)

    @Query("SELECT * FROM gastos ORDER BY fechaLong DESC")
    fun obtenerTodosLosGastosFlow(): Flow<List<GastoEntity>>

    // --- PRESUPUESTO ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPresupuesto(presupuesto: PresupuestoEntity)

    @Query("SELECT * FROM presupuesto_config WHERE id = 'unico'")
    fun obtenerPresupuestoFlow(): Flow<PresupuestoEntity?> // Devuelve Flow para que sea reactivo si cambia
}