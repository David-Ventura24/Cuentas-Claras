package com.example.cuentas_clarasapp.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.api.auth.LoginRequestDto
import com.example.cuentas_clarasapp.data.repositories.AuthApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthApiRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val request = LoginRequestDto(
                correo_electronico = state.email,
                contrasena = state.password
            )
            
            val respuesta = authRepository.login(request)
            
            _uiState.update { it.copy(isLoading = false) }
            
            if (respuesta.error == null && respuesta.token != null) {
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = respuesta.error ?: "Error al iniciar sesión") }
            }
        }
    }
}
