package com.example.cuentas_clarasapp.screens.budget

/**
 * Representa los estados de la pantalla de configuración de presupuesto.
 */
sealed interface BudgetSetupUiState {
    // Estado inicial estático mientras la UI se monta
    object Idle : BudgetSetupUiState

    // Estado de bloqueo visual mientas se persiste en Room / Supabase
    object Loading : BudgetSetupUiState

    // Estado de éxito que gatilla la navegación de retorno (popBackStack)
    object Success : BudgetSetupUiState

    // Captura de excepciones críticas en la capa de datos
    data class Error(val mensaje: String) : BudgetSetupUiState
}