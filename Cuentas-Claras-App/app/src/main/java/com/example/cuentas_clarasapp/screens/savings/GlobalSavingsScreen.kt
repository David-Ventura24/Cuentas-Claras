package com.example.cuentas_clarasapp.screens.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.cuentas_clarasapp.data.local.entities.AhorroEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Purple = Color(0xFF985EFF)
private val BgDark = Color(0xFF111013)
private val BgCard = Color(0xFF1A1820)
private val TextDim = Color(0x66FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSavingsScreen(
    navController: NavController,
    viewModel: SavingsViewModel
) {
    //  Sincronizar datos al entrar
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        viewModel.refrescarAhorros()
        onPauseOrDispose { }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarDialogoRetiro by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ahorro Global", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (val state = uiState) {
                is SavingsUiState.Loading -> {
                    CircularProgressIndicator(color = Purple, modifier = Modifier.align(Alignment.Center))
                }
                is SavingsUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // TARJETA CONTENEDORA PRINCIPAL - BALANCE GLOBAL
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(BgCard)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Total Acumulado", color = TextDim, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$${"%.2f".format(state.ahorroGlobalNeto)}",
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { mostrarDialogoRetiro = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Retirar Fondos de Emergencia", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(text = "Historial de Movimientos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        // LISTA HISTÓRICA DE MOVIMIENTOS
                        if (state.listaMovimientos.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No hay movimientos registrados", color = TextDim, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.listaMovimientos) { movimiento ->
                                    MovimientoAhorroRow(movimiento = movimiento)
                                }
                            }
                        }
                    }
                }
                is SavingsUiState.Error -> {
                    Text(text = state.mensaje, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    // DIÁLOGO EMERGENTE PARA RETIROS
    if (mostrarDialogoRetiro) {
        var montoInput by remember { mutableStateOf("") }
        var motivoInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogoRetiro = false },
            containerColor = BgCard,
            title = { Text("Retirar del Ahorro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("El monto retirado se deducirá de tu alcancía global disponible.", color = TextDim, fontSize = 14.sp)
                    OutlinedTextField(
                        value = montoInput,
                        onValueChange = { montoInput = it },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = TextDim,
                            focusedLabelColor = Purple,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = motivoInput,
                        onValueChange = { motivoInput = it },
                        label = { Text("Motivo / Nota") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = TextDim,
                            focusedLabelColor = Purple,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val monto = montoInput.toDoubleOrNull() ?: 0.0
                        if (monto > 0) {
                            viewModel.realizarRetiroEmergencia(monto, motivoInput)
                            mostrarDialogoRetiro = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Text("Confirmar Retiro")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRetiro = false }) {
                    Text("Cancelar", color = TextDim)
                }
            }
        )
    }
}

@Composable
private fun MovimientoAhorroRow(movimiento: AhorroEntity) {
    val esIngreso = movimiento.tipo == "INGRESO"
    val colorMonto = if (esIngreso) Color(0xFF4ADE80) else Color(0xFFF87171)
    val prefijo = if (esIngreso) "+ $" else "- $"

    val fechaFormateada = remember(movimiento.fechaLong) {
        Instant.ofEpochMilli(movimiento.fechaLong)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = movimiento.nota, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = fechaFormateada, color = TextDim, fontSize = 12.sp)
        }

        Text(
            text = "$prefijo${"%.2f".format(movimiento.monto)}",
            color = colorMonto,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
