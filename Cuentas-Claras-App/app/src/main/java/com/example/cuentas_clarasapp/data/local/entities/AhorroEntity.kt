package com.example.cuentas_clarasapp.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos_ahorro")
data class AhorroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monto: Double,
    val tipo: String,
    val nota: String,
    val fechaLong: Long
)