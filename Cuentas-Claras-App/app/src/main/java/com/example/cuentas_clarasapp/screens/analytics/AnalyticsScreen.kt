package com.example.cuentas_clarasapp.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
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
import com.example.cuentas_clarasapp.components.CuentasClarasBottomNav

private val Purple     = Color(0xFF985EFF)
private val BgDark     = Color(0xFF111013)
private val BgCard     = Color(0xFF1A1820)
private val TextMuted  = Color(0x4DFFFFFF)
private val TextDim    = Color(0x66FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController : NavController,
    viewModel: AnalyticsViewModel = viewModel() // Adaptado a AnalyticsViewModel
) {
    // Escucha reactiva del StateFlow bajo el ciclo de vida de la UI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Cuentas Claras", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2D1F54)) // Fondo morado oscuro superior del mockup
            )
        },
        bottomBar = {
            CuentasClarasBottomNav(navController = navController)
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple)
                }
            }
            is AnalyticsUiState.Success -> {
                AnalyticsContent(
                    data = state.data,
                    viewModel = viewModel,
                    innerPadding = innerPadding
                )
            }
            is AnalyticsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
}

@Composable
private fun AnalyticsContent(
    data: ExpenseAnalyticsData,
    viewModel: AnalyticsViewModel,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Análisis de gastos",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Fecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.cambiarMes(avanzar = false) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = Purple)
            }
            Text(
                text = data.mesAnioFiltro,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { viewModel.cambiarMes(avanzar = true) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = Purple)
            }
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
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CategoriaGastoRow(categoria: CategoriaGastoData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo de color identificador de la categoría
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(categoria.color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Etiqueta del nombre
        Text(
            text = categoria.nombre,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        // Metrica porcentual
        Text(
            text = "${"%.1f".format(categoria.porcentaje)}%",
            color = TextDim,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Saldo absoluto por categoría
        Text(
            text = "$${"%.2f".format(categoria.monto)}",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}