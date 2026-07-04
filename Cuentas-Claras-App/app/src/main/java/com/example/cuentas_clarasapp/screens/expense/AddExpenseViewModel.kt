package com.example.cuentas_clarasapp.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.api.expense.ExpenseRequestDto
import com.example.cuentas_clarasapp.data.repositories.ExpenseRepository
import com.example.cuentas_clarasapp.data.repositories.ExpenseApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository = ExpenseApiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun onMontoChange(value: String) {
        _uiState.update { it.copy(monto = value, montoError = null, presupuestoError = null) }
    }

    fun onCategoriaSelected(id: String) {
        _uiState.update { it.copy(categoriaId = id, categoriaError = null) }
    }

    fun onNotaChange(value: String) {
        _uiState.update { it.copy(nota = value) }
    }

    fun onFotoUriChange(value: String?) {
        _uiState.update { it.copy(fotoUri = value) }
    }

    fun guardarGasto(onSuccess: () -> Unit) {
        val state = _uiState.value
        var tieneError = false

        val montoDouble = state.monto.toDoubleOrNull()
        if (montoDouble == null || montoDouble <= 0) {
            _uiState.update { it.copy(montoError = "Ingresa un monto válido mayor a 0") }
            tieneError = true
        }

        if (state.categoriaId == null) {
            _uiState.update { it.copy(categoriaError = "Selecciona una categoría") }
            tieneError = true
        }

        if (tieneError) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val idUsuarioReal = com.example.cuentas_clarasapp.data.api.ApiClient.obtenerUsuarioIdActual()

            if (!expenseRepository.puedeRegistrarGasto(montoDouble!!, idUsuarioReal)) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        presupuestoError = "No tienes suficiente presupuesto disponible"
                    )
                }
                return@launch
            }

            val requestDto = ExpenseRequestDto(
                total_gastado = montoDouble,
                categoria = state.categoriaId!!,
                img = state.fotoUri ?: state.categoriaId,
                fecha = "", // El servidor usará su propia hora ISO
                id_usuario = idUsuarioReal
            )

            val resultado = expenseRepository.registrarGasto(requestDto)

            if (resultado.isSuccess) {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        presupuestoError = resultado.exceptionOrNull()?.localizedMessage ?: "Error de red"
                    )
                }
            }
        }
    }
}
