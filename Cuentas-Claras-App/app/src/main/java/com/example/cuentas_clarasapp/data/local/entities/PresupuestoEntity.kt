package com.example.cuentas_clarasapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presupuesto_config")
data class PresupuestoEntity(
    @PrimaryKey
    val id: String = "unico",
    val montoMontoConfigurado: Double,
    val periodo: String,
    val porcentajeAhorro: Int,
    val limiteDiarioInicial: Double
)