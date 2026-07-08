package com.example.cuentas_clarasapp.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

private val Purple     = Color(0xFF985EFF)
private val BgDark     = Color(0xFF111013)
private val BgCard     = Color(0xFF1A1820)
private val TextDim    = Color(0x66FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController : NavController,
    viewModel: AnalyticsViewModel = viewModel(),
    paddingValues: PaddingValues = PaddingValues(0.dp) // Recibe los márgenes del Scaffold superior de la barra flotante
) {
    // Escucha reactiva del StateFlow bajo el ciclo de vida de la UI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //  PullToRefreshBox permite recargar arrastrando hacia abajo con ruedita morada
    PullToRefreshBox(
        isRefreshing = uiState is AnalyticsUiState.Loading,
        onRefresh = { viewModel.refrescarContenido() },
        contentAlignment = Alignment.TopCenter,
        indicator = {
            androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator(
                state = rememberPullToRefreshState(),
                isRefreshing = uiState is AnalyticsUiState.Loading,
                containerColor = BgCard,
                color = Purple, //  Ruedita morada
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(paddingValues)
    ) {
        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                // Si no hay datos previos, mostramos carga central
                if (uiState !is AnalyticsUiState.Success) {
                    CircularProgressIndicator(
                        color = Purple,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            is AnalyticsUiState.Success -> {
                AnalyticsContent(
                    data = state.data
                )
            }
            is AnalyticsUiState.Error -> {
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
private fun AnalyticsContent(
    data: ExpenseAnalyticsData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Análisis de gastos",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        //  Título de la gráfica (Sustituye al selector de fecha anterior)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Grafica de gastos",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta Contenedora Principal de la Gráfica
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgCard)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // GRÁFICA DE DONA INTERACTIVA
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f // Comienza en el eje superior vertical

                    data.categorias.forEach { categoria ->
                        val sweepAngle = (categoria.porcentaje / 100f) * 360f
                        drawArc(
                            color = categoria.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 36.dp.toPx()) // Grosor
                        )
                        startAngle += sweepAngle
                    }
                }

                // Indicador monetario
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$${"%.2f".format(data.montoTotalGastado)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DESGLOSE DE CATEGORÍAS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                data.categorias.forEach { categoria ->
                    CategoriaGastoRow(categoria = categoria)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun CategoriaGastoRow(categoria: CategoriaGastoData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(categoria.color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = categoria.nombre,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${"%.1f".format(categoria.porcentaje)}%",
            color = TextDim,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = "$${"%.2f".format(categoria.monto)}",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}