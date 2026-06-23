package com.example.cuentas_clarasapp.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.cuentas_clarasapp.screens.home.HomeUiState
import com.example.cuentas_clarasapp.screens.home.HomeViewModel

private val BgDark         = Color(0xFF111013)
private val BgCard         = Color(0xFF1A1820)
private val Purple         = Color(0xFF985EFF)
private val TextMuted      = Color(0x66FFFFFF)
private val GreenInfo      = Color(0xFF4CAF50)
private val OrangeWarning  = Color(0xFFFF9800)
private val RedCritical    = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel,
    homeViewModel: HomeViewModel
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val listaNotificaciones by notificationViewModel.notificaciones.collectAsStateWithLifecycle()

    // Cada vez que el estado del Home cambie, recalculamos las notificaciones reactivas
    LaunchedEffect(homeUiState) {
        if (homeUiState is HomeUiState.Success) {
            val datos = (homeUiState as HomeUiState.Success).data
            notificationViewModel.evaluarCondicionesFinancieras(datos)
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { innerPadding ->

        if (listaNotificaciones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                    Text(
                        text = "Tu estado financiero está al día. No tenés alertas pendientes.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp, // Tamaño grande accesible
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onTextLayout = {}
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(listaNotificaciones) { alerta ->

                    val (colorIcono, contenedorIcono, vectorIcono) = when (alerta.prioridad) {
                        NotificationPriority.INFO -> Triple(GreenInfo, GreenInfo.copy(alpha = 0.1f), Icons.Default.Info)
                        NotificationPriority.WARNING -> Triple(OrangeWarning, OrangeWarning.copy(alpha = 0.1f), Icons.Default.Warning)
                        NotificationPriority.CRITICAL -> Triple(RedCritical, RedCritical.copy(alpha = 0.1f), Icons.Default.Error)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(BgCard)
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(contenedorIcono),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = vectorIcono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = alerta.titulo,
                                    color = Color.White,
                                    fontSize = 17.sp, // Subió para excelente lectura
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = alerta.fecha,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = alerta.mensaje,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 15.sp, // Aumentado para que tu mamá lo lea fluido
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}