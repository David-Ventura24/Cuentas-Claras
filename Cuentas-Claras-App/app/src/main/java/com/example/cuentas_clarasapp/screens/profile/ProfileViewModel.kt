package com.example.cuentas_clarasapp.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.local.SessionManager
import com.example.cuentas_clarasapp.data.repositories.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val profileRepository = ProfileRepository()
    private val sessionManager = SessionManager(application.applicationContext)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    fun cargarDatosUsuario() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val resultado = profileRepository.obtenerPerfil()
                
                if (resultado.isSuccess) {
                    val dto = resultado.getOrNull()
                    if (dto != null) {
                        _uiState.value = ProfileUiState.Success(
                            nombre = dto.nombre,
                            carrera = dto.carrera,
                            moneda = dto.moneda,
                            estadoCuenta = dto.estadoCuenta
                        )
                    } else {
                        _uiState.value = ProfileUiState.Error("Respuesta vacía del servidor")
                    }
                } else {
                    val errorMsg = resultado.exceptionOrNull()?.message ?: "Error desconocido"
                    _uiState.value = ProfileUiState.Error("Fallo de red: $errorMsg")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Excepción: ${e.message}")
            }
        }
    }

    fun cerrarSesion(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            sessionManager.cerrarSesion()
            onNavigateToLogin()
        }
    }
}
