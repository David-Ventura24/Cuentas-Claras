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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.components.CuentasClarasBottomNav
import com.example.cuentas_clarasapp.navigation.Routes

// Paleta de colores oficial de Cuentas Claras
private val Purple = Color(0xFF985EFF)
private val BgDark = Color(0xFF111013)
private val BgCard = Color(0xFF1A1820)
private val TextMuted = Color(0x4DFFFFFF)
private val GreenProgress = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    // Recolectamos el estado reactivo de la arquitectura compartida
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.Budget) },
                containerColor = Purple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Configurar Presupuesto")
            }
        },
        bottomBar = {
            CuentasClarasBottomNav(navController = navController)
        }
    ) { innerPadding ->

        when (uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple)
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ocurrió un error al cargar tus finanzas.",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
            is HomeUiState.Success -> {
                val datos = (uiState as HomeUiState.Success).data

                // --- PROCESAMIENTO DE TU CONFIGURACIÓN ACTUAL (INTACTO) ---
                val saldoDisponible = datos.saldoDisponible
                val limiteDiario = datos.limiteDiarioSugerido
                val porcentajeAhorro = datos.porcentajeAhorro
                val periodo = datos.periodoPresupuesto

                val gastosConsumidos = 0.0

                val factorAhorro = if (porcentajeAhorro < 100) (100 - porcentajeAhorro) / 100.0 else 1.0
                val presupuestoBaseOriginal = if (periodo != "Sin configurar" && factorAhorro > 0) {
                    saldoDisponible / factorAhorro
                } else {
                    0.0
                }

                val montoAhorradoMeta = presupuestoBaseOriginal * (porcentajeAhorro / 100.0)

                val porcentajeRestanteFlotante = if (presupuestoBaseOriginal > 0) {
                    ((saldoDisponible - gastosConsumidos) / presupuestoBaseOriginal).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }

                val textoPorcentajeRestante = if (periodo != "Sin configurar") {
                    "${(porcentajeRestanteFlotante * 100).toInt()}% restante"
                } else {
                    "0% restante"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // =========================================================
                    // ENCABEZADO PERSONALIZADO: SALUDO, NOTIFICACIONES Y PERFIL
                    // =========================================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Saludo al usuario
                        Text(
                            text = "Hola, David 👋",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Grupo de botones de acción rápidos (Notificaciones + Perfil)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono de Notificaciones
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .size(26.dp)
                                    .clickable {
                                        // TODO: Vincular a la pantalla de notificaciones motivacionales
                                    }
                            )

                            // Avatar Circular de Usuario
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Purple)
                                    .clickable {
                                        // TODO: Vincular a la pantalla de configuración de perfil
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // =========================================================
                    // 1. TARJETA: BALANCE DISPONIBLE LÍQUIDO
                    // =========================================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(BgCard)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Balance disponible",
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$${String.format("%.2f", saldoDisponible)}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (periodo != "Sin configurar") "Ciclo actual: $periodo" else "No hay un presupuesto activo",
                            color = Purple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // =========================================================
                    // 2. TARJETA: PROGRESO DEL CICLO DE GASTOS
                    // =========================================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(BgCard)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Presupuesto General",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = textoPorcentajeRestante,
                                color = if (porcentajeRestanteFlotante > 0.2f) GreenProgress else Color.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { porcentajeRestanteFlotante },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Purple,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$${String.format("%.2f", gastosConsumidos)} gastados",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Total: $${String.format("%.2f", presupuestoBaseOriginal)}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // =========================================================
                    // FILA: METAS DE AHORRO Y MÓDULO DE GASTOS TOTALES
                    // =========================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BgCard)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Meta Ahorro ($porcentajeAhorro%)",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$${String.format("%.2f", montoAhorradoMeta)}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Retenido del total",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BgCard)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Gastos Totales",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$${String.format("%.2f", gastosConsumidos)}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Consumo del ciclo",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // =========================================================
                    // 3. TARJETA: MONITOR DE LÍMITE DIARIO DISPONIBLE
                    // =========================================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(BgCard)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LÍMITE DIARIO SUGERIDO",
                            color = Purple.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "$${String.format("%.2f", limiteDiario)}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (limiteDiario > 0.0) "100% disponible hoy" else "Sin dinero asignado para hoy",
                            color = GreenProgress,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}