package com.example.cuentas_clarasapp.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.navigation.Routes
import java.util.Locale

private val Purple         = Color(0xFF985EFF)
private val BgDark         = Color(0xFF111013)
private val BgCard         = Color(0xFF1A1820)
private val TextMuted      = Color(0x66FFFFFF)
private val GreenProgress  = Color(0xFF4CAF50)
private val OrangeWarning  = Color(0xFFFF9800)
private val RedExpense     = Color(0xFFFF5252)

fun obtenerIconoCategoriaHome(id: String): ImageVector {
    return when (id) {
        "alimentacion" -> Icons.Default.Restaurant
        "transporte"   -> Icons.Default.DirectionsCar
        "ocio"         -> Icons.Default.SportsEsports
        "compras"      -> Icons.Default.ShoppingBag
        "educacion"    -> Icons.Default.School
        else           -> Icons.Default.MoreHoriz
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    paddingValues: PaddingValues,
    onFabVisibilityChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        var previousIndex = listState.firstVisibleItemIndex
        var previousScrollOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val subiendo = index < previousIndex ||
                    (index == previousIndex && offset < previousScrollOffset)
            val bajando = index > previousIndex ||
                    (index == previousIndex && offset > previousScrollOffset)

            when {
                subiendo -> onFabVisibilityChange(true)
                bajando -> onFabVisibilityChange(false)
            }

            previousIndex = index
            previousScrollOffset = offset
        }
    }

    when (uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Purple)
            }
        }
        is HomeUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Ocurrió un error al cargar tus finanzas.", color = Color.Red, fontSize = 15.sp)
            }
        }
        is HomeUiState.Success -> {
            val datos = (uiState as HomeUiState.Success).data

            val saldoDisponible = datos.saldoDisponible
            val porcentajeAhorro = datos.porcentajeAhorro
            val periodo = datos.periodoPresupuesto
            val gastoDiarioActual = datos.gastoDiarioActual
            val montoInicialConfigurado = datos.montoInicialConfigurado
            val limiteDiarioInicial = datos.limiteDiarioInicial

            val montoAhorradoMeta = montoInicialConfigurado * (porcentajeAhorro / 100.0)

            val dineroRestanteHoy = (limiteDiarioInicial - gastoDiarioActual).coerceAtLeast(0.0)
            val seExcedioLimiteDiario = gastoDiarioActual > limiteDiarioInicial && limiteDiarioInicial > 0.0

            val porcentajeRestanteDiarioFlotante = if (limiteDiarioInicial > 0.0 && !seExcedioLimiteDiario) {
                (dineroRestanteHoy / limiteDiarioInicial).toFloat().coerceIn(0f, 1f)
            } else 0f

            val porcentajeRestanteGeneral = if (montoInicialConfigurado > 0) {
                ((saldoDisponible / montoInicialConfigurado) * 100).toInt()
            } else 0

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark)
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Hola, David ", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (periodo != "Sin configurar") "$porcentajeRestanteGeneral% de tu presupuesto disponible" else "Sin presupuesto activo",
                                color = Purple.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { navController.navigate(Routes.Notifications) }
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Purple)
                                    .clickable { navController.navigate(Routes.Profile) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "D", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Balance disponible", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", saldoDisponible)}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(BgCard)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "LÍMITE SUGERIDO DE HOY",
                            color = Purple.copy(alpha = 0.95f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", limiteDiarioInicial)}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progreso de tu jornada",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (seExcedioLimiteDiario) "¡Cupo Agotado!" else "${((porcentajeRestanteDiarioFlotante) * 100).toInt()}% disponible",
                                color = if (seExcedioLimiteDiario) RedExpense else GreenProgress,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { porcentajeRestanteDiarioFlotante },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = if (seExcedioLimiteDiario) RedExpense else Purple,
                            trackColor = Color.White.copy(alpha = 0.06f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Gastado",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", gastoDiarioActual)}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Meta Ahorro",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", montoAhorradoMeta)}",
                                    color = Purple,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "GASTOS DE HOY",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                if (datos.gastosDelDia.isEmpty()) {
                    item {
                        Text(
                            text = "No has registrado gastos hoy.",
                            color = TextMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                } else {
                    items(datos.gastosDelDia) { gasto ->
                        GastoItemRow(gasto)
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun GastoItemRow(gasto: GastoItemHome) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = obtenerIconoCategoriaHome(gasto.categoriaId),
                contentDescription = null,
                tint = Purple,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = gasto.categoriaId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (gasto.nota.isNotEmpty()) {
                Text(
                    text = gasto.nota,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        // 🌟 CORREGIDO: Resalta las bajas / flujos negativos en color rojo
        Text(
            text = "-$${String.format(Locale.getDefault(), "%.2f", gasto.monto)}",
            color = RedExpense,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}