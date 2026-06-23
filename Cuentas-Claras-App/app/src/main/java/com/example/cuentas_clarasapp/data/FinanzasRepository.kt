package com.example.cuentas_clarasapp.data

object FinanzasRepository {
   
    fun puedeRegistrarGasto(monto: Float): Boolean {
        return true 
    }

    fun registrarGasto(gasto: GastoRegistrado) {
        // TODO: Implementar persistencia (Supabase, Room, etc.)
        println("Gasto registrado: $gasto")
    }
}
