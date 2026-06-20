package com.example.cuentas_clarasapp.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.components.CuentasClarasBottomNav
import com.example.cuentas_clarasapp.navigation.Routes
import java.util.Locale

private val Purple = Color(0xFF985EFF)
private val BgDark = Color(0xFF111013)
private val BgCard = Color(0xFF1A1820)
private val TextMuted = Color(0x4DFFFFFF)
private val GreenSuccess = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupScreen(
    navController: NavController,
    viewModel: BudgetSetupViewModel,
    saldoActualHome: Float,
    periodoActualHome: String,
    ahorroActualHome: Float,
    onPresupuestoGuardado: (Double, String, Double) -> Unit
) {
    var montoInput by remember {
        mutableStateOf(if (saldoActualHome > 0f) saldoActualHome.toString() else "")
    }

    var periodoSeleccionado by remember {
        mutableStateOf(if (periodoActualHome != "Sin configurar") periodoActualHome else "Mensual")
    }

    var porcentajeAhorro by remember {
        mutableStateOf(if (ahorroActualHome > 0f) ahorroActualHome else 10f)
    }

    var mostrarExito by remember { mutableStateOf(false) }

    // --- CÁLCULOS MATEMÁTICOS PARA LA PROYECCIÓN DINÁMICA ---
    val montoActual = montoInput.toDoubleOrNull() ?: 0.0
    val montoAhorroDolares = montoActual * (porcentajeAhorro / 100.0)
    val saldoDisponibleDespuesAhorro = (montoActual - montoAhorroDolares).coerceAtLeast(0.0)
    val diasPeriodo = if (periodoSeleccionado == "Mensual") 30 else 7
    val gastoDiarioProyectado = if (montoActual > 0.0) saldoDisponibleDespuesAhorro / diasPeriodo else 0.0

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurar Presupuesto",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        bottomBar = {
            CuentasClarasBottomNav(navController = navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            if (mostrarExito) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GreenSuccess.copy(alpha = 0.15f))
                        .border(1.dp, GreenSuccess, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Éxito", tint = GreenSuccess)
                    Text(
                        text = "¡Presupuesto guardado exitosamente!",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // =========================================================
            // 1. INPUT DEL MONTO TOTAL
            // =========================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Monto Total Disponible",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = montoInput,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            montoInput = newValue
                        }
                    },
                    placeholder = { Text("Ej: 100.00", color = Color.White.copy(alpha = 0.2f)) },
                    prefix = { Text("$ ", color = Purple, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = BgDark.copy(alpha = 0.5f),
                        unfocusedContainerColor = BgDark.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // =========================================================
            // 2. SELECTOR DE PERIODO (MENSUAL / SEMANAL)
            // =========================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Periodo del Ciclo",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val opciones = listOf("Mensual", "Semanal")
                    opciones.forEach { opcion ->
                        val seleccionado = periodoSeleccionado == opcion
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (seleccionado) Purple else BgDark.copy(alpha = 0.5f))
                                .border(
                                    width = if (seleccionado) 0.dp else 1.dp,
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { periodoSeleccionado = opcion },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = opcion,
                                color = if (seleccionado) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // =========================================================
            // 3. BARRA DE RETENCIÓN DE AHORRO (LIMITADA DE 0% A 30%)
            // =========================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Porcentaje de Ahorro",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${porcentajeAhorro.toInt()}%",
                        color = Purple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = porcentajeAhorro,
                    onValueChange = { porcentajeAhorro = it },
                    valueRange = 0f..30f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = Purple,
                        activeTrackColor = Purple,
                        inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // =========================================================
            // 4. BLOQUES DE PROYECCIÓN EN GRANDE
            // =========================================================
            if (montoActual > 0.0) {
                // Tarjeta 1: Dinero real que queda libre para gastar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "DINERO DISPONIBLE PARA GASTOS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        // CORREGIDO: Comillas limpias sin barras invertidas
                        text = "$${String.format(Locale.getDefault(), "%.2f", saldoDisponibleDespuesAhorro)}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tarjeta 2: Límite diario calculado en vivo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgCard)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LÍMITE DIARIO ESTIMADO",
                        color = Purple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        // CORREGIDO: Comillas limpias sin barras invertidas
                        text = "$${String.format(Locale.getDefault(), "%.2f", gastoDiarioProyectado)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Basado en un ciclo de $diasPeriodo días",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp
                    )
                }
            }

            // =========================================================
            // BOTÓN DE ACCIÓN: GUARDAR PRESUPUESTO
            // =========================================================
            Button(
                onClick = {
                    if (montoActual > 0.0) {
                        onPresupuestoGuardado(
                            montoActual,
                            periodoSeleccionado,
                            porcentajeAhorro.toDouble()
                        )
                        mostrarExito = true
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Home) { inclusive = true }
                        }
                    }
                },
                enabled = montoInput.isNotEmpty() && montoActual > 0.0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    disabledContainerColor = Color.White.copy(alpha = 0.04f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Guardar Presupuesto",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}