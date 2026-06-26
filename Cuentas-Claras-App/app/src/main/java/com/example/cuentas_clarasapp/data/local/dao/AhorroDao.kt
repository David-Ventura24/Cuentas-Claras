package com.example.cuentas_clarasapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AhorroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMovimiento(movimiento: AhorroEntity)

    @Query("SELECT * FROM movimientos_ahorro ORDER BY fechaLong DESC")
    fun obtenerTodosLosMovimientos(): Flow<List<AhorroEntity>>

    @Query("SELECT SUM(CASE WHEN tipo = 'INGRESO' THEN monto ELSE -monto END) FROM movimientos_ahorro")
    fun obtenerAhorroGlobalNeto(): Flow<Double?>
}
