package com.example.cuentas_clarasapp.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.model.GastoRegistrado
import com.example.cuentas_clarasapp.data.repositories.FinanzasRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddExpenseViewModel : ViewModel() {

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

    fun onPhotoSelected(uri: String) {
        _uiState.update { it.copy(fotoUri = uri) }
    }

    fun guardarGasto(onSuccess: () -> Unit) {
        val state = _uiState.value
        val montoFloat = state.monto.toFloatOrNull()

        // --- Validaciones ---
        var montoError: String? = null
        var categoriaError: String? = null

        if (state.monto.isBlank() || montoFloat == null || montoFloat <= 0f) {
            montoError = "Ingresa cuánto gastaste"
        }
        if (state.categoriaId == null) {
            categoriaError = "Selecciona una categoría"
        }

        if (montoError != null || categoriaError != null) {
            _uiState.update { it.copy(montoError = montoError, categoriaError = categoriaError) }
            return
        }

        // --- Validación de presupuesto ---
        if (!FinanzasRepository.puedeRegistrarGasto(montoFloat!!)) {
            _uiState.update {
                it.copy(presupuestoError = "No tienes suficiente presupuesto disponible")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // Simulación de red/DB
            delay(600)

            FinanzasRepository.registrarGasto(
                GastoRegistrado(
                    id = UUID.randomUUID().toString(),
                    categoriaId = state.categoriaId!!,
                    monto = montoFloat,
                    nota = state.nota,
                    fotoUri = state.fotoUri
                )
            )

            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
