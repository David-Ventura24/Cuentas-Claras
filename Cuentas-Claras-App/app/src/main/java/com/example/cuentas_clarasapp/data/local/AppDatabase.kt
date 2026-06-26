package com.example.cuentas_clarasapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cuentas_clarasapp.data.local.dao.GastoDao
import com.example.cuentas_clarasapp.data.local.dao.AhorroDao
import com.example.cuentas_clarasapp.data.local.entities.GastoEntity
import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity
import com.example.cuentas_clarasapp.data.local.entities.PresupuestoEntity

@Database(
    entities = [GastoEntity::class, PresupuestoEntity::class, AhorroEntity::class], //Agregada la nueva entidad de ahorros
    version = 2, // Incrementada la versión por cambio de esquema
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao
    abstract fun ahorroDao(): AhorroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cuentas_claras_db"
                )
                    .fallbackToDestructiveMigration() // 🌟 Opcional: Permite limpiar la DB automáticamente al cambiar de versión en desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}