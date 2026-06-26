package com.example.cuentas_clarasapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val descripcion: String,
    val monto: Double,
    val categoriaId: String,
    val categoriaNombre: String,
    val fechaLong: Long
)