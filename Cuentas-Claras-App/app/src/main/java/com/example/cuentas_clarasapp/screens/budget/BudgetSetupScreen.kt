package com.example.cuentas_clarasapp.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

private val Purple     = Color(0xFF985EFF)
private val BgDark     = Color(0xFF111013)
private val BgCard     = Color(0xFF1A1820)
private val TextMuted  = Color(0x4DFFFFFF)
private val TextDim    = Color(0x2AFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSetupScreen(
    navController: NavController,
    viewModel: BudgetSetupViewModel
) {
    // Escucha de forma segura los cambios en el flujo de estado de la operación (Loading/Success/Error)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Efecto de escucha: Si el backend responde exitosamente, saca la pantalla del Backstack
    LaunchedEffect(uiState) {
        if (uiState is BudgetSetupUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Configurar Presupuesto", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
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
            Spacer(modifier = Modifier.height(4.dp))

            // 1. SELECCIÓN DE PERÍODO (Mensual / Semanal)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Cada cuánto recibes tu dinero?", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Mensual", "Semanal").forEach { periodo ->
                        val esSeleccionado = viewModel.periodoSeleccionado == periodo
                        Button(
                            onClick = { viewModel.onPeriodoChanged(periodo) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (esSeleccionado) Purple else BgCard,
                                contentColor = if (esSeleccionado) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(periodo, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 2. INPUT DE MONTO TOTAL (Conexión directa al ViewModel)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Monto total del presupuesto", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = viewModel.montoInput,
                    onValueChange = { viewModel.onMontoChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00", color = TextDim) },
                    prefix = { Text("$ ", color = Color.White, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = BgCard,
                        unfocusedContainerColor = BgCard,
                        focusedBorderColor = Purple,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // 3. SLIDER INTERACTIVO DE PORCENTAJE DE AHORRO (Tope máximo 30%)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Meta de ahorro", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${viewModel.porcentajeAhorro.toInt()}%",
                        color = Purple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Dinero destinado a guardarse automáticamente.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Slider(
                    value = viewModel.porcentajeAhorro,
                    onValueChange = { viewModel.onPorcentajeAhorroChanged(it) },
                    valueRange = 0f..30f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = Purple,
                        activeTrackColor = Purple,
                        inactiveTrackColor = Color.White.copy(alpha = 0.08f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monto ahorrado:", color = TextMuted, fontSize = 12.sp)
                    Text("$${"%.2f".format(viewModel.montoAhorro)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // 4. DISPLAY DEL LÍMITE DIARIO (¡Ahora sí se actualiza en vivo!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Gasto diario sugerido".uppercase(),
                    color = Purple.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${"%.2f".format(viewModel.limiteDiarioSugerido)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Basado en un saldo disponible de $${"%.2f".format(viewModel.saldoDisponibleParaGasto)} para ${viewModel.diasPeriodo} días.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. BOTÓN DE GUARDADO (Comentado y estructurado con lógica asíncrona)
            Button(
                onClick = {
                    // ===========================================================================================
                    // LOGICA DE GUARDADO COMENTADA
                    // ===========================================================================================
                    // El botón llama al método del ViewModel pasándole una función lambda vacía corporativa.
                    // Dentro del ViewModel se procesará el almacenamiento local (Room) y remoto (Supabase / Node).
                    // Una vez completada la escritura con éxito, el LaunchedEffect de arriba sacará de forma
                    // automática esta pantalla devolviendo al usuario al HomeScreen con sus datos frescos.
                    // ===========================================================================================

                    viewModel.guardarPresupuesto(onGastoGuardadoExitosamente = {})
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 8.dp),
                // Se deshabilita si el presupuesto es cero o si ya se está ejecutando la llamada en segundo plano
                enabled = viewModel.presupuestoTotal > 0f && uiState !is BudgetSetupUiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    disabledContainerColor = BgCard
                )
            ) {
                if (uiState is BudgetSetupUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Presupuesto", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
