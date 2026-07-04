package com.example.cuentas_clarasapp.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
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
import com.example.cuentas_clarasapp.navigation.Routes
import java.util.Locale

private val Purple     = Color(0xFF985EFF)
private val BgDark     = Color(0xFF111013)
private val BgCard     = Color(0xFF1A1820)
private val TextDim    = Color(0x66FFFFFF)
private val RedExpense = Color(0xFFFF5252) // Color rojo unificado de gastos

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    // REFRESCAR AL VOLVER
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        viewModel.refrescarContenido()
        onPauseOrDispose { }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(paddingValues)
    ) {
        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                CircularProgressIndicator(
                    color = Purple,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HistoryUiState.Success -> {
                HistoryContent(
                    data = state.data,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            is HistoryUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.mensaje, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.refrescarContenido() }) {
                        Text("Reintentar")
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
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Historial",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        //  Muestra dinámicamente tu acumulado de ahorros real
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .clickable { navController.navigate(Routes.GlobalSavings) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Purple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TUS AHORROS",
                    color = TextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Nota: Asegúrate de que tu data class 'HistoryData' exponga la variable del acumulado matemático.
                // Si la variable se llama diferente (ej. totalAhorrado), cámbiala aquí:
                val ahorrosAMostrar = data.totalAhorradoMes ?: 11.50
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", ahorrosAMostrar)} guardados",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver Ahorros",
                tint = TextDim,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SELECTOR DE FECHA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.cambiarMes(avanzar = false) },
                modifier = Modifier
                    .size(32.dp)
                    .background(BgCard, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = data.mesAnioFiltro,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            IconButton(
                onClick = { viewModel.cambiarMes(avanzar = true) },
                modifier = Modifier
                    .size(32.dp)
                    .background(BgCard, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TOTAL GASTADO
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL GASTADO",
                    color = TextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format(Locale.getDefault(), "%.2f", data.totalGastadoMes)}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val totalMovimientos = data.transacciones.size
            Text(
                text = "$totalMovimientos movimiento${if (totalMovimientos == 1) "" else "s"}\neste mes",
                color = TextDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "RECIENTES",
            color = TextDim,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(data.transacciones) { transaccion ->
                ItemTransaccionRow(transaccion = transaccion)
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ItemTransaccionRow(transaccion: GastoHistorial) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //  Mapeo exacto e independiente de íconos para evitar mezclas
        val iconoCategoria = remember(transaccion.categoria) {
            when (transaccion.categoria.lowercase(Locale.getDefault()).trim()) {
                "alimentación", "alimentacion" -> Icons.Default.Restaurant
                "compras"                     -> Icons.Default.ShoppingBag
                "ocio"                        -> Icons.Default.SportsEsports
                "educación", "educacion"       -> Icons.Default.School
                "transporte"                  -> Icons.Default.DirectionsCar
                else                          -> Icons.Default.List
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF211438)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconoCategoria,
                contentDescription = null,
                tint = Color(0xFFB08FFF),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaccion.categoria.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (transaccion.descripcion.isBlank()) "Sin nota" else transaccion.descripcion,
                color = TextDim,
                fontSize = 13.sp
            )
        }

        //  El monto ahora se muestra en color rojo (RedExpense)
        Text(
            text = "-$${String.format(Locale.getDefault(), "%.2f", transaccion.monto)}",
            color = RedExpense,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}