package com.example.cuentas_clarasapp.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.navigation.Routes
import com.google.android.libraries.intelligence.acceleration.Analytics

// --- Sistema de Diseño Atmosférico (Colores compartidos) ---
private val Purple       = Color(0xFF985EFF)
private val BgDark       = Color(0xFF111013)
private val BgCard       = Color(0xFF1A1820)
private val BorderCard   = Color(0x12FFFFFF)
private val TextMuted    = Color(0x4DFFFFFF)
private val TextDim      = Color(0x2AFFFFFF)

/**
 * Determina el color del indicador de progreso en función del dinero consumido.
 * @param spentPct Float que representa la fracción de dinero gastado [0.0f - 1.0f].
 */
private fun barColor(spentPct: Float): Color = when {
    spentPct <= 0.50f -> Color(0xFF4ADE80) // Estado Saludable: Verde (Consumido menos del 50%)
    spentPct <= 0.75f -> Color(0xFFFACC15) // Advertencia Moderada: Amarillo (Consumido entre 50% y 75%)
    spentPct <= 0.90f -> Color(0xFFFB923C) // Alerta Crítica: Naranja (Consumido entre 75% y 90%)
    else               -> Color(0xFFF87171) // Límite Superado / Zona de Riesgo: Rojo
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    // Escucha reactiva del StateFlow expuesto por el ViewModel bajo el ciclo de vida de la UI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO: BACKEND INTEGRATION - Vincular con el canal de navegación hacia Routes.AddExpense
                    // navController.navigate(Routes.AddExpense)
                },
                containerColor = Purple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar gasto", modifier = Modifier.size(24.dp))
            }
        },
        bottomBar = { HomeBottomNav(navController) }
    ) { innerPadding ->

        // Máquina de estados para control de concurrencia y peticiones de red asíncronas
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple)
                }
            }
            is HomeUiState.Success -> {
                HomeContent(
                    data = state.data,
                    navController = navController,
                    innerPadding = innerPadding
                )
            }
            is HomeUiState.Error -> {
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

// ─────────────────────────────────────────
// CAPA INTERMEDIA DE PROCESAMIENTO DE DATOS
// ─────────────────────────────────────────
@Composable
private fun HomeContent(
    data: HomeData,
    navController: NavController,
    innerPadding: PaddingValues
) {
    val saldoDisponibleActual = data.saldoDisponible.toFloat() // Dinero que el usuario tiene real para gastar

    // TODO: Extraer 'totalPresupuestoConfigurado' desde la tabla de configuraciones de presupuesto del usuario.
    val totalPresupuestoConfigurado = 350.00f

    // Cálculo correcto de la masa monetaria consumida
    val budgetSpent = (totalPresupuestoConfigurado - saldoDisponibleActual).coerceAtLeast(0f)

    // Normalización matemática estricta para evitar desbordamientos [0.0f a 1.0f]
    val budgetSpentPct = if (totalPresupuestoConfigurado > 0f) budgetSpent / totalPresupuestoConfigurado else 0f
    val budgetRemainingPct = (1f - budgetSpentPct).coerceIn(0f, 1f)
    val pctLeft = (budgetRemainingPct * 100).toInt()

    // --- SECCIÓN: CÁLCULOS ASOCIADOS AL LÍMITE DIARIO ---
    val dailySpent = data.ultimosGastos.filter { it.fecha.lowercase().contains("hoy") || it.fecha.lowercase().contains("horas") }
        .sumOf { it.monto.toDouble() }.toFloat()

    val dailyLimit = data.limiteDiarioSugerido.toFloat()

    val dailySpentPct = if (dailyLimit > 0f) dailySpent / dailyLimit else 0f
    val dailyRemainingPct = (1f - dailySpentPct).coerceIn(0f, 1f)
    val dailyPctLeft = (dailyRemainingPct * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader(userName = data.nombreUsuario)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Renderizado de Tarjeta de Presupuesto con evento de clic inyectado
            BudgetCard(
                saldoDisponible = saldoDisponibleActual,
                spent           = budgetSpent,
                progressPct     = budgetRemainingPct,
                pctLeft         = pctLeft,
                periodo         = data.periodoPresupuesto,
                onCardClick     = { navController.navigate(Routes.Budget) } // <-- Acción de navegación al hacer tap
            )

            // Fila de Indicadores Secundarios (Métricas de Ahorro Programado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val ahorroEstimado = totalPresupuestoConfigurado * (data.porcentajeAhorro / 100f)
                SmallCard(label = "Ahorros (${data.porcentajeAhorro}%)", amount = ahorroEstimado, sub = "Meta este mes", modifier = Modifier.weight(1f))
                SmallCard(label = "Gastos", amount = budgetSpent, sub = "Consumido total", modifier = Modifier.weight(1f))
            }

            // Renderizado de la Tarjeta de Consumo Diario
            DailyCard(
                spent       = dailySpent,
                limit       = dailyLimit,
                progressPct = dailyRemainingPct,
                pctLeft     = dailyPctLeft
            )

            SectionHeader(
                title = "Gastos recientes",
                linkText = "Ver historial",
                onLink = {
                    navController.navigate(Routes.History)
                }
            )

            GastosHoy(listaGastos = data.ultimosGastos)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ─────────────────────────────────────────
// COMPONENTES DE INTERFAZ GRÁFICA ATÓMICOS
// ─────────────────────────────────────────
@Composable
private fun HomeHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Buenos días", color = TextMuted, fontSize = 12.sp)
            Text(
                text = "Hola, $userName 👋",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(
                    onClick = { /* TODO: Vincular flujo de notificaciones push */ },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Purple)
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Purple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotEmpty()) userName.first().uppercase() else "U",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BudgetCard(
    saldoDisponible: Float,
    spent: Float,
    progressPct: Float,
    pctLeft: Int,
    periodo: String,
    onCardClick: () -> Unit // <-- Callback de escucha al tacto
) {
    val animPct = remember { Animatable(0f) }

    LaunchedEffect(progressPct) {
        animPct.animateTo(progressPct, tween(650, easing = EaseOutCubic))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .clickable { onCardClick() } // <-- Hace interactiva toda el área de la tarjeta
            .padding(18.dp)
    ) {
        Text("Presupuesto $periodo", color = TextMuted, fontSize = 11.sp, letterSpacing = 0.6.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$${"%.2f".format(saldoDisponible)}",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { animPct.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp)),
            color = barColor(1f - progressPct),
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$pctLeft% restante", color = TextDim, fontSize = 12.sp)
            Text("$${"%.2f".format(spent)} gastado", color = TextDim, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SmallCard(label: String, amount: Float, sub: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(14.dp)
    ) {
        Text(label.uppercase(), color = TextMuted, fontSize = 11.sp, letterSpacing = 0.6.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("$${"%.2f".format(amount)}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)
        Text(sub, color = TextDim, fontSize = 11.sp)
    }
}

@Composable
private fun DailyCard(spent: Float, limit: Float, progressPct: Float, pctLeft: Int) {
    val animPct = remember { Animatable(0f) }
    LaunchedEffect(progressPct) {
        animPct.animateTo(progressPct, tween(650, easing = EaseOutCubic))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Límite diario".uppercase(), color = TextMuted, fontSize = 11.sp, letterSpacing = 0.6.sp)
            Text("$pctLeft% disponible hoy", color = Purple.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("$${"%.2f".format(spent)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
            Text("de $${"%.2f".format(limit)}", color = TextMuted, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { animPct.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp)),
            color = barColor(1f - progressPct),
            trackColor = Color.White.copy(alpha = 0.08f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun SectionHeader(title: String, linkText: String, onLink: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onLink, contentPadding = PaddingValues(0.dp)) {
            Text(linkText, color = Purple.copy(alpha = 0.70f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun GastosHoy(listaGastos: List<GastoHome>) {
    if (listaGastos.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            Text("Aquí se mostrarán tus gastos del período", color = TextDim, fontSize = 13.sp)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listaGastos.forEach { gasto ->
                GastoRow(gasto)
            }
        }
    }
}

@Composable
private fun GastoRow(gasto: GastoHome) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Purple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Purple, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(gasto.descripcion, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(gasto.fecha, color = TextDim, fontSize = 11.sp)
        }
        Text("-$${"%.2f".format(gasto.monto)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HomeBottomNav(navController: NavController, currentRoute: Routes = Routes.Home) {
    NavigationBar(
        containerColor = BgDark,
        tonalElevation = 0.dp,
        modifier = Modifier.height(64.dp)
    ) {
        // Estructura de navegación con ruteo explícito para la pantalla de presupuesto
        val items = listOf(
            Triple("Inicio",      Icons.Outlined.Person, Routes.Home),
            Triple("Historial",   Icons.Outlined.DateRange, Routes.History),
            Triple("Gráficas",    Icons.Outlined.Analytics, Routes.AddExpense),
            Triple("Presupuesto", Icons.Outlined.Wallet, Routes.Budget),
        )
        items.forEachIndexed { index, (label, icon, route) ->
            NavigationBarItem(
                // Se evalúa dinámicamente según la pantalla activa para iluminar el icono correcto
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Routes.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Purple,
                    selectedTextColor   = Purple,
                    unselectedIconColor = Color.White.copy(alpha = 0.25f),
                    unselectedTextColor = Color.White.copy(alpha = 0.25f),
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}