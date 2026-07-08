package com.example.cuentas_clarasapp.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.api.auth.ResetPasswordRequest
import com.example.cuentas_clarasapp.data.repositories.AuthApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthApiRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onTokenChange(value: String) {
        if (value.length <= 6) {
            _uiState.update { it.copy(token = value, errorMessage = null) }
        }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun solicitarCodigo() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo electrónico") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val respuesta = authRepository.solicitarRecuperacion(email)
            _uiState.update { it.copy(isLoading = false) }

            if (respuesta.error == null) {
                _uiState.update { it.copy(step = 2, successMessage = "Código enviado a tu correo") }
            } else {
                _uiState.update { it.copy(errorMessage = respuesta.error) }
            }
        }
    }

    fun restablecerContrasena(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.token.length < 6) {
            _uiState.update { it.copy(errorMessage = "Ingresa el código de 6 dígitos") }
            return
        }
        if (state.newPassword.length < 8) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres") }
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = ResetPasswordRequest(
                correo_electronico = state.email,
                token = state.token,
                nueva_contrasena = state.newPassword
            )
            val respuesta = authRepository.restablecerPassword(request)
            _uiState.update { it.copy(isLoading = false) }

            if (respuesta.error == null) {
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = respuesta.error) }
            }
        }
    }
}
