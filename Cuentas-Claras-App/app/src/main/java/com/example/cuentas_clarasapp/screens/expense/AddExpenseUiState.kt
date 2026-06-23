package com.example.cuentas_clarasapp.screens.expense

data class AddExpenseUiState(
    val monto: String = "",
    val categoriaId: String? = null,
    val nota: String = "",
    val fotoUri: String? = null,
    val isSaving: Boolean = false,
    val montoError: String? = null,
    val categoriaError: String? = null,
    val presupuestoError: String? = null
)