package com.example.cuentas_clarasapp.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.screens.home.HomeUiState
import com.example.cuentas_clarasapp.screens.home.HomeViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.text.toFloatOrNull

private val Purple    = Color(0xFF985EFF)
private val BgDark    = Color(0xFF111013)
private val BgCard    = Color(0xFF1A1820)
private val BgCardAlt = Color(0xFF211D2E)
private val TextDim   = Color(0x66FFFFFF)
private val TextMuted = Color(0x47FFFFFF)

enum class BudgetScreenMode {
    CONFIGURACION_INICIAL,
    VISTA_DETALLE,
    AGREGAR_FONDOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Inputs reactivos
    var montoInput by remember { mutableStateOf("") }
    var periodoSeleccionado by remember { mutableStateOf("Mensual") }
    var porcentajeAhorro by remember { mutableStateOf(10f) }
    var isSaving by remember { mutableStateOf(false) }

    var pantallaModo by remember { mutableStateOf(BudgetScreenMode.CONFIGURACION_INICIAL) }

    // Sincronización inicial limpia con Room
    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.Success) {
            val datos = (uiState as HomeUiState.Success).data
            if (datos.montoInicialConfigurado > 0.0) {
                if (pantallaModo == BudgetScreenMode.CONFIGURACION_INICIAL) {
                    pantallaModo = BudgetScreenMode.VISTA_DETALLE
                }
                // Si el usuario no está digitando en AGREGAR_FONDOS, mantenemos actualizados los periodos base
                if (pantallaModo != BudgetScreenMode.AGREGAR_FONDOS) {
                    periodoSeleccionado = datos.periodoPresupuesto
                    porcentajeAhorro = datos.porcentajeAhorro.toFloat()
                }
            } else {
                pantallaModo = BudgetScreenMode.CONFIGURACION_INICIAL
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val titulo = when (pantallaModo) {
                        BudgetScreenMode.CONFIGURACION_INICIAL -> "Configurar Presupuesto"
                        BudgetScreenMode.VISTA_DETALLE -> "Tu Presupuesto"
                        BudgetScreenMode.AGREGAR_FONDOS -> "Recalibrar Ciclo"
                    }
                    Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pantallaModo == BudgetScreenMode.AGREGAR_FONDOS) {
                            montoInput = ""
                            pantallaModo = BudgetScreenMode.VISTA_DETALLE
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark
    ) { innerScaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerScaffoldPadding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(color = Purple, modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    Text(text = state.mensaje, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Success -> {
                    val datosActuales = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp)
                            .padding(bottom = paddingValues.calculateBottomPadding())
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))

                        when (pantallaModo) {
                            // ==========================================
                            // ESTADO 1: CONFIGURACIÓN INICIAL
                            // ==========================================
                            BudgetScreenMode.CONFIGURACION_INICIAL -> {
                                Text(
                                    text = "Comienza configurando tu ciclo financiero de base para calcular tus topes diarios automáticos.",
                                    color = TextDim,
                                    fontSize = 14.sp
                                )

                                // Campo Monto
                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard).padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Monto inicial del ciclo", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    OutlinedTextField(
                                        value = montoInput,
                                        onValueChange = { montoInput = it },
                                        placeholder = { Text("Ej. 300.00", color = Color.White.copy(alpha = 0.2f)) },
                                        prefix = { Text("$ ", color = Purple, fontWeight = FontWeight.Bold) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Purple, unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedContainerColor = BgDark.copy(alpha = 0.5f), unfocusedContainerColor = BgDark.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                // Selector Periodo
                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard).padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Periodo del ciclo", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        listOf("Semanal", "Mensual").forEach { periodo ->
                                            FilterChip(
                                                selected = periodoSeleccionado == periodo,
                                                onClick = { periodoSeleccionado = periodo },
                                                label = { Text(periodo, fontSize = 13.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Purple, selectedLabelColor = Color.White,
                                                    containerColor = BgDark.copy(alpha = 0.5f), labelColor = TextDim
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Slider Ahorro
                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard).padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Ahorro automático", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("${porcentajeAhorro.toInt()}%", color = Purple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = porcentajeAhorro,
                                        onValueChange = { porcentajeAhorro = it },
                                        valueRange = 0f..50f,
                                        steps = 9,
                                        colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple, inactiveTrackColor = BgDark.copy(alpha = 0.5f))
                                    )
                                }

                                Button(
                                    onClick = {
                                        val monto = montoInput.toFloatOrNull() ?: 0f
                                        if (monto > 0f && !isSaving) {
                                            isSaving = true // Bloqueamos el botón
                                            homeViewModel.actualizarPresupuestoDesdeConfiguracion(
                                                montoAInyectar = monto,
                                                nuevoPeriodo = periodoSeleccionado,
                                                nuevoPorcentajeAhorro = porcentajeAhorro,
                                                onSuccess = {
                                                    isSaving = false
                                                    montoInput = ""
                                                    navController.popBackStack() //  Solo volvemos si el server dijo OK
                                                },
                                                onError = { error ->
                                                    scope.launch {
                                                        isSaving = false
                                                        snackbarHostState.showSnackbar(error)
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    enabled = (montoInput.toFloatOrNull() ?: 0f) > 0f && !isSaving,
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                                ) {
                                    Text("Guardar presupuesto", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // ==========================================
                            // ESTADO 2: VISTA DETALLE
                            // ==========================================
                            BudgetScreenMode.VISTA_DETALLE -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Brush.linearGradient(colors = listOf(BgCardAlt, BgCard)))
                                        .border(0.5.dp, Purple.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text("RESUMEN GENERAL DEL CICLO", color = Purple, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)

                                    StatusRow(label = "Presupuesto Total Ingresado", value = "$${String.format(Locale.getDefault(), "%.2f", datosActuales.montoInicialConfigurado)}")
                                    StatusRow(label = "Saldo Disponible", value = "$${String.format(Locale.getDefault(), "%.2f", datosActuales.saldoDisponible)}", showDivider = true)
                                    StatusRow(label = "Periodo del Ciclo", value = datosActuales.periodoPresupuesto, showDivider = true)
                                    StatusRow(label = "Porcentaje Ahorro Meta", value = "${datosActuales.porcentajeAhorro}%", showDivider = true)
                                }

                                Button(
                                    onClick = {
                                        montoInput = ""
                                        pantallaModo = BudgetScreenMode.AGREGAR_FONDOS
                                    },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                                ) {
                                    Text("Modificar o Agregar Fondos", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // ==========================================
                            // ESTADO 3: AGREGAR FONDOS / RECALIBRAR
                            // ==========================================
                            BudgetScreenMode.AGREGAR_FONDOS -> {
                                Text(
                                    text = "Modificá el periodo o porcentaje si lo deseás. Las métricas de topes diarios se adaptarán al nuevo flujo.",
                                    color = TextDim,
                                    fontSize = 13.sp
                                )

                                // Campo Monto Extra
                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard).padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Monto a añadir (Opcional si solo cambias periodos)", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    OutlinedTextField(
                                        value = montoInput,
                                        onValueChange = { montoInput = it },
                                        placeholder = { Text("0.00 (Dejar vacío si no sumás fondos)", color = Color.White.copy(alpha = 0.2f)) },
                                        prefix = { Text("$ ", color = Purple, fontWeight = FontWeight.Bold) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Purple, unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                            focusedContainerColor = BgDark.copy(alpha = 0.5f), unfocusedContainerColor = BgDark.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                // Permite cambiar el periodo a mitad del ciclo activo
                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard).padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Redefinir Periodo del ciclo", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        listOf("Semanal", "Mensual").forEach { periodo ->
                                            FilterChip(
                                                selected = periodoSeleccionado == periodo,
                                                onClick = { periodoSeleccionado = periodo },
                                                label = { Text(periodo, fontSize = 13.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Purple, selectedLabelColor = Color.White,
                                                    containerColor = BgDark.copy(alpha = 0.5f), labelColor = TextDim
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Permite cambiar el porcentaje a mitad del ciclo activo
                                Column(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(BgCard).padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Redefinir Ahorro Meta", color = TextDim, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("${porcentajeAhorro.toInt()}%", color = Purple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = porcentajeAhorro,
                                        onValueChange = { porcentajeAhorro = it },
                                        valueRange = 0f..50f,
                                        steps = 9,
                                        colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple, inactiveTrackColor = BgDark.copy(alpha = 0.5f))
                                    )
                                }

                                Button(
                                    onClick = {
                                        val monto = montoInput.toFloatOrNull() ?: 0f
                                        isSaving = true

                                        // Mandamos todo junto al ViewModel para recalcular Room limpiamente
                                        homeViewModel.actualizarPresupuestoDesdeConfiguracion(
                                            montoAInyectar = monto,
                                            nuevoPeriodo = periodoSeleccionado,
                                            nuevoPorcentajeAhorro = porcentajeAhorro,
                                            onSuccess = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("¡Ciclo recalibrado correctamente!")
                                                    montoInput = ""
                                                    isSaving = false
                                                    pantallaModo = BudgetScreenMode.VISTA_DETALLE
                                                }
                                            },
                                            onError = { error ->
                                                scope.launch {
                                                    isSaving = false
                                                    snackbarHostState.showSnackbar(error)
                                                }
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Confirmar Recalibración", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, showDivider: Boolean = false) {
    if (showDivider) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color.White.copy(alpha = 0.06f)).height(0.5.dp))
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextDim, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}