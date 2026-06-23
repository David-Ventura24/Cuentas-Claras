package com.example.cuentas_clarasapp.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.components.CuentasClarasBottomNav
import com.example.cuentas_clarasapp.navigation.Routes

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.AddExpense) },
                containerColor = Purple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar nuevo gasto")
            }
        },
        bottomBar = {
            CuentasClarasBottomNav(navController = navController)
        }
    ) { innerPadding ->

        when (uiState) {
            is HomeUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple)
                }
            }
            is HomeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(text = "Ocurrió un error al cargar tus finanzas.", color = Color.Red, fontSize = 16.sp)
                }
            }
            is HomeUiState.Success -> {
                val datos = (uiState as HomeUiState.Success).data

                val saldoDisponible = datos.saldoDisponible
                val porcentajeAhorro = datos.porcentajeAhorro
                val periodo = datos.periodoPresupuesto
                val gastosTotalesCiclo = datos.gastosTotalesCiclo
                val gastoDiarioActual = datos.gastoDiarioActual
                val montoInicialConfigurado = datos.montoInicialConfigurado
                val limiteDiarioInicial = datos.limiteDiarioInicial

                val factorAhorro = if (porcentajeAhorro < 100) (100 - porcentajeAhorro) / 100.0 else 1.0
                val presupuestoBaseOriginal = if (periodo != "Sin configurar" && factorAhorro > 0) {
                    montoInicialConfigurado / factorAhorro
                } else 0.0
                val montoAhorradoMeta = presupuestoBaseOriginal * (porcentajeAhorro / 100.0)

                val dineroRestanteHoy = (limiteDiarioInicial - gastoDiarioActual).coerceAtLeast(0.0)
                val seExcedioLimiteDiario = gastoDiarioActual > limiteDiarioInicial && limiteDiarioInicial > 0.0

                val porcentajeRestanteDiarioFlotante = if (limiteDiarioInicial > 0.0 && !seExcedioLimiteDiario) {
                    (dineroRestanteHoy / limiteDiarioInicial).toFloat().coerceIn(0f, 1f)
                } else 0f

                val porcentajeRestanteGeneral = if (montoInicialConfigurado > 0) {
                    ((saldoDisponible / montoInicialConfigurado) * 100).toInt()
                } else 0

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // ENCABEZADO: Títulos más grandes y descriptivos
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Hola, David 👋", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (periodo != "Sin configurar") "$porcentajeRestanteGeneral% de tu presupuesto disponible" else "Sin presupuesto activo",
                                    color = Purple.copy(alpha = 0.9f),
                                    fontSize = 15.sp, // Aumentado para mejor lectura
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(28.dp) // Icono más grande
                                        .clickable { navController.navigate(Routes.Notifications) }
                                )
                                Box(
                                    modifier = Modifier
                                        .size(40.dp) // Contenedor del avatar más grande
                                        .clip(CircleShape)
                                        .background(Purple)
                                        .clickable { navController.navigate(Routes.Profile) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "D", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 1. CARD: BALANCE GENERAL LÍQUIDO
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "Balance disponible", color = TextMuted, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "$${String.format("%.2f", saldoDisponible)}",
                                color = Color.White,
                                fontSize = 46.sp, // Aumentado ligeramente para máxima claridad
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }

                    // 2. MONITOR DEL DÍA: Optimizada la jerarquía y legibilidad interna
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(BgCard)
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "LÍMITE SUGERIDO DE HOY",
                                color = Purple.copy(alpha = 0.95f),
                                fontSize = 13.sp, // Subió de 11.sp a 13.sp
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = "$${String.format("%.2f", limiteDiarioInicial)}",
                                color = Color.White,
                                fontSize = 38.sp, // Subió de 34.sp a 38.sp para que resalte mucho más
                                fontWeight = FontWeight.Black
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Progreso de tu jornada",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 15.sp, // Subió de 13.sp a 15.sp
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (seExcedioLimiteDiario) "¡Cupo Agotado!" else "${((porcentajeRestanteDiarioFlotante) * 100).toInt()}% disponible",
                                    color = if (seExcedioLimiteDiario) RedExpense else GreenProgress,
                                    fontSize = 14.sp, // Subió de 12.sp a 14.sp
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { porcentajeRestanteDiarioFlotante },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), // Barra un poco más gruesa (10.dp)
                                color = if (seExcedioLimiteDiario) RedExpense else Purple,
                                trackColor = Color.White.copy(alpha = 0.06f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Gastado hoy: $${String.format("%.2f", gastoDiarioActual)}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp // Subió de 12.sp a 14.sp
                                )
                                Text(
                                    text = "Disponible: $${String.format("%.2f", dineroRestanteHoy)}",
                                    color = if (dineroRestanteHoy > 0.0) GreenProgress else TextMuted,
                                    fontSize = 14.sp, // Subió de 12.sp a 14.sp
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (seExcedioLimiteDiario) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(OrangeWarning.copy(alpha = 0.1f))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Alerta de Ajuste: El límite diario fue superado. El sistema tomará fondos de las jornadas posteriores, reajustando tus márgenes matutinos automáticamente.",
                                        color = OrangeWarning,
                                        fontSize = 13.sp, // Subió de 11.sp a 13.sp para lectura clara de advertencias
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // 3. WIDGETS SECUNDARIOS: Tarjetas más amplias con textos más grandes
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = BgCard),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Meta Ahorro", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("$${String.format("%.2f", montoAhorradoMeta)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = BgCard),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Gastos Totales", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("$${String.format("%.2f", gastosTotalesCiclo)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. HISTORIAL DE GASTOS: Filas de ítems más grandes y cómodas
                    item {
                        Text(
                            text = "GASTOS DE HOY",
                            color = TextMuted,
                            fontSize = 13.sp, // Subió de 11.sp a 13.sp
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (datos.gastosDelDia.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay transacciones registradas hoy.",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 15.sp // Subió de 13.sp a 15.sp
                                )
                            }
                        }
                    } else {
                        items(datos.gastosDelDia) { gasto ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BgCard)
                                    .padding(16.dp), // Padding aumentado para un blanco táctil más cómodo
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp) // Icono contenedor más amplio
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Purple.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = obtenerIconoCategoriaHome(gasto.categoriaId),
                                            contentDescription = null,
                                            tint = Purple,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = gasto.nota.ifEmpty { "Gasto sin descripción" },
                                            color = Color.White,
                                            fontSize = 16.sp, // Subió de 14.sp a 16.sp
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(text = "Hoy", color = TextMuted, fontSize = 13.sp) // Subió de 11.sp a 13.sp
                                    }
                                }
                                Text(
                                    text = "-$${String.format("%.2f", gasto.monto)}",
                                    color = RedExpense,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}