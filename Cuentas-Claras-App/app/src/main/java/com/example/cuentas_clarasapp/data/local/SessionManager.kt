package com.example.cuentas_clarasapp.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("cuentas_claras_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val USER_ID = "user_id"
    }

    // Obtener el token para las peticiones
    fun obtenerToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Guardar la sesión al hacer login
    fun guardarSesion(token: String, userId: Int) {
        prefs.edit()
            .putString(USER_TOKEN, token)
            .putInt(USER_ID, userId)
            .apply()
    }

    // Recuperar el ID del usuario
    fun obtenerUsuarioId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }
}
