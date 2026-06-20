package com.example.cuentas_clarasapp.screens.expense

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AddExpenseUiState>(AddExpenseUiState.Loading)
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        cargarAnalisis()
    }

    fun refrescarContenido() {
        cargarAnalisis()
    }

    // Lógica simulada para los botones de navegación de fechas
    fun cambiarMes(avanzar: Boolean) {
        // TODO: BACKEND INTEGRATION - Filtrar dinámicamente por mes/año desde la base de datos por medio de Visual Studio
    }

    private fun cargarAnalisis() {
        viewModelScope.launch {
            _uiState.value = AddExpenseUiState.Loading
            try {
                _uiState.value = AddExpenseUiState.Success(
                    data = ExpenseAnalyticsData(
                        mesAnioFiltro = "Mayo 2026",
                        montoTotalGastado = 313.00,
                        categorias = listOf(
                            CategoriaGastoData("Alimentación", 38.5f, 120.50, Color(0xFFF87171)), // Rojo suave del sistema
                            CategoriaGastoData("Educación", 27.2f, 85.00, Color(0xFF4ADE80)),    // Verde del sistema
                            CategoriaGastoData("Transporte", 14.4f, 45.00, Color(0xFF3A86FF)),   // Azul
                            CategoriaGastoData("Ahorro", 10.3f, 32.24, Color(0xFF985EFF)),       // Morado principal (Purple)
                            CategoriaGastoData("Restaurantes", 5.1f, 16.00, Color(0xFFFACC15)),  // Amarillo
                            CategoriaGastoData("Otros", 4.5f, 14.26, Color(0xFF70777A))          // Gris
                        )
                    )
                )
            } catch (e: Exception) {
                _uiState.value = AddExpenseUiState.Error("No se pudo conectar con el servidor")
            }
        }
    }
}