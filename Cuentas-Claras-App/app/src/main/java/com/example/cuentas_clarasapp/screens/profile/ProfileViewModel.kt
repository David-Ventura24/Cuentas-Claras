package com.example.cuentas_clarasapp.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        viewModelScope.launch {
            try {
                // Simulamos la lectura de la base de datos de Room
                delay(800)
                _uiState.value = ProfileUiState.Success(
                    nombre = "David Montoya",
                    carrera = "Informatics Engineering",
                    moneda = "USD ($)",
                    estadoCuenta = "Activa"
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("No se pudieron cargar los datos")
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            // Aquí irá la lógica para limpiar las SharedPreferences o borrar el token de sesión
        }
    }
}