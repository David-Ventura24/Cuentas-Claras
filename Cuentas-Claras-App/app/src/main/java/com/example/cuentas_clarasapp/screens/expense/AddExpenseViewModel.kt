package com.example.cuentas_clarasapp.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuentas_clarasapp.data.repositories.FinanzasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val repository: FinanzasRepository // 🌟 Inyección correcta de la instancia del repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun onMontoChange(nuevoMonto: String) {
        _uiState.update { it.copy(monto = nuevoMonto) }
    }

    fun onCategoriaSelected(categoriaId: String) {
        _uiState.update { it.copy(categoriaId = categoriaId) }
    }

    fun onNotaChange(nuevaNota: String) {
        _uiState.update { it.copy(nota = nuevaNota) }
    }

    fun onPhotoSelected(uri: String) {
        _uiState.update { it.copy(fotoUri = uri) }
    }

    fun guardarGasto(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val montoInput = currentState.monto
        val categoriaId = currentState.categoriaId ?: ""
        val nota = currentState.nota

        val montoFloat = montoInput.toFloatOrNull() ?: 0f

        // Validaciones previas antes de tocar la base de datos
        val montoError = if (montoFloat <= 0f) "El monto debe ser mayor a cero" else null
        val categoriaError = if (categoriaId.isEmpty()) "Selecciona una categoría" else null

        if (montoError != null || categoriaError != null) {
            _uiState.update { it.copy(montoError = montoError, categoriaError = categoriaError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val categoriaNombre = when (categoriaId) {
                    "alimentacion" -> "Alimentación"
                    "transporte"   -> "Transporte"
                    "ocio"         -> "Ocio"
                    "compras"      -> "Compras"
                    "educacion"    -> "Educación"
                    else           -> "Otros"
                }

                repository.registrarGasto(
                    descripcion = nota,
                    monto = montoFloat.toDouble(),
                    categoriaId = categoriaId,
                    categoriaNombre = categoriaNombre
                )

                _uiState.update { it.copy(isSaving = false, guardadoExitoso = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, presupuestoError = "Error al guardar localmente") }
            }
        }
    }
}
