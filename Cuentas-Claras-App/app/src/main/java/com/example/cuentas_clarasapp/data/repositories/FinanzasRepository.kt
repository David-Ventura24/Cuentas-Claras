package com.example.cuentas_clarasapp.data.repositories

import com.example.cuentas_clarasapp.data.model.GastoRegistrado

object FinanzasRepository {
    fun puedeRegistrarGasto(monto: Float): Boolean {
        // En una implementación real, esto consultaría el saldo disponible
        return true 
    }

    fun registrarGasto(gasto: GastoRegistrado) {
        // TODO: Implementar persistencia (Supabase/Room)
        println("Gasto registrado: $gasto")
    }
}
