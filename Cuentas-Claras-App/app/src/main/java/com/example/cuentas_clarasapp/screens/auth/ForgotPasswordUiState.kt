package com.example.cuentas_clarasapp.screens.auth

data class ForgotPasswordUiState(
    val email: String = "",
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val step: Int = 1, // 1: Email, 2: Reset
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val passwordVisible: Boolean = false
)
