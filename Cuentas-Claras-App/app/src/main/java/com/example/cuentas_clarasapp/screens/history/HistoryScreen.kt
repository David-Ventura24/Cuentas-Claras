package com.example.cuentas_clarasapp.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.components.CuentasClarasBottomNav

private val Purple     = Color(0xFF985EFF)
private val BgDark     = Color(0xFF111013)
private val BgCard     = Color(0xFF1A1820)
val TextDim    = Color(0x66FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Cuentas Claras", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2D1F54))
            )
        },
        bottomBar = {
            CuentasClarasBottomNav(navController = navController)
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple)
                }
            }
            is HistoryUiState.Success -> {
                HistoryContent(data = state.data, viewModel = viewModel, innerPadding = innerPadding)
            }
            is HistoryUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.mensaje, color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refrescarContenido() }) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryContent(
    data: HistoryData,
    viewModel: HistoryViewModel,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Historial de transacciones", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de fecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.cambiarMes(avanzar = false) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Purple)
            }
            Text(text = data.mesAnioFiltro, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { viewModel.cambiarMes(avanzar = true) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Purple)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Total resumen superior del mes
        Text(
            text = "Total gastado: $${"%.2f".format(data.totalGastadoMes)}",
            color = TextDim,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de gastos con scroll
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(data.transacciones) { transaccion ->
                ItemTransaccionRow(transaccion = transaccion)
            }
        }
    }
}

@Composable
private fun ItemTransaccionRow(transaccion: GastoHistorial) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo de categoría
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(transaccion.colorCategoria))

        Spacer(modifier = Modifier.width(16.dp))


        Column(modifier = Modifier.weight(1f)) {
            Text(text = transaccion.descripcion, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "${transaccion.categoria} • ${transaccion.fecha}", color = TextDim, fontSize = 12.sp)
        }

        // Monto
        Text(
            text = "- $${"%.2f".format(transaccion.monto)}",
            color = Color(0xFFF87171),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}